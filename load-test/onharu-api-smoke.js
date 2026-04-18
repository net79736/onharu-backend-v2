import { check, sleep } from 'k6';
import http from 'k6/http';
import { Counter } from 'k6/metrics';

/**
 * onharu-backend-v2 API 부하/스모크 테스트 (k6)
 *
 * 사전: 앱 기동 (기본 http://localhost:8080)
 * 실행: ./run.sh
 *       BASE_URL=https://staging.example.com ./run.sh
 *       ONHARU_VUS=50 ONHARU_ITERATIONS=20 ./run.sh
 *       OPENAPI_PATH=/api-docs/json ./run.sh
 */

const successCounter = new Counter('onharu_success_count');
const failureCounter = new Counter('onharu_failure_count');

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
/** OpenAPI JSON — application-swagger.yaml 의 springdoc.api-docs.path (기본 /v3/api-docs 아님) */
const openApiPath = __ENV.OPENAPI_PATH || '/api-docs/json';
const vus = Number(__ENV.ONHARU_VUS || 20);
const iterations = Number(__ENV.ONHARU_ITERATIONS || 30);
const maxDuration = __ENV.ONHARU_MAX_DURATION || '2m';

export const options = {
	scenarios: {
		onharu_api_smoke: {
			executor: 'per-vu-iterations',
			vus,
			iterations,
			maxDuration,
		},
	},
	thresholds: {
		http_req_failed: ['rate<0.05'],
		http_req_duration: ['p(95)<3000'],
	},
};

function getJson(url, tags) {
	const params = {
		tags,
		headers: { Accept: 'application/json' },
		timeout: '10s',
	};
	return http.get(url, params);
}

export default function () {
	let ok = true;

	// 공개 GET — 등급 목록 (SecurityConfig PUBLIC)
	const levelsRes = getJson(`${baseUrl}/api/levels`, { name: 'GET /api/levels' });
	const levelsOk = check(levelsRes, {
		'GET /api/levels status 200': (r) => r.status === 200,
	});
	if (!levelsOk) ok = false;

	// OpenAPI JSON (springdoc 커스텀 경로)
	const docsRes = getJson(`${baseUrl}${openApiPath}`, { name: 'GET OpenAPI JSON' });
	const docsOk = check(docsRes, {
		[`GET ${openApiPath} status 200`]: (r) => r.status === 200,
	});
	if (!docsOk) ok = false;

	if (ok) {
		successCounter.add(1);
	} else {
		failureCounter.add(1);
	}

	sleep(0.1);
}

export function handleSummary(data) {
	const successCount = data.metrics.onharu_success_count?.values?.count || 0;
	const failureCount = data.metrics.onharu_failure_count?.values?.count || 0;
	const totalReqs = data.metrics.http_reqs?.values?.count || 0;

	console.log('\n=== onharu k6 요약 ===');
	console.log(`BASE_URL: ${baseUrl}`);
	console.log(`HTTP 요청 수: ${totalReqs}`);
	console.log(`시나리오 성공(이터레이션) 수: ${successCount}`);
	console.log(`시나리오 실패(이터레이션) 수: ${failureCount}`);

	return {
		stdout: JSON.stringify(data, null, 2),
	};
}
