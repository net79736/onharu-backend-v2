/**
 * k6 handleSummary 용 간단 HTML 대시보드 (브라우저에서 지표 확인)
 */
function esc(s) {
	if (s === undefined || s === null) return '';
	return String(s)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;');
}

function num(v, digits = 2) {
	if (v === undefined || v === null || Number.isNaN(v)) return '—';
	return typeof v === 'number' ? v.toFixed(digits) : esc(String(v));
}

export function buildHtmlReport(data) {
	const m = data.metrics || {};
	const state = data.state || {};
	const root = data.root_group || {};
	const checks = root.checks || [];

	const httpReqs = m.http_reqs?.values?.count ?? 0;
	const iterCount = m.iterations?.values?.count ?? 0;
	const failRate = m.http_req_failed?.values?.rate;
	const checkRate = m.checks?.values?.rate;
	const dur = m.http_req_duration?.values || {};
	const iterDur = m.iteration_duration?.values || {};
	const durationMs = state.testRunDurationMs;

	const rows = checks
		.map(
			(c) => `
    <tr>
      <td>${esc(c.name)}</td>
      <td class="num">${c.passes ?? 0}</td>
      <td class="num fail">${c.fails ?? 0}</td>
    </tr>`
		)
		.join('');

	const failPct = failRate !== undefined ? (failRate * 100).toFixed(2) : '—';
	const checkPct = checkRate !== undefined ? (checkRate * 100).toFixed(2) : '—';

	return `<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>onharu k6 리포트</title>
  <style>
    :root { --bg:#0f1419; --card:#1a2332; --text:#e7e9ea; --muted:#8b98a5; --ok:#00ba7c; --bad:#f4212e; --bar:#1d9bf0; }
    * { box-sizing: border-box; }
    body { font-family: ui-sans-serif, system-ui, sans-serif; background: var(--bg); color: var(--text); margin: 0; padding: 24px; line-height: 1.5; }
    h1 { font-size: 1.25rem; margin: 0 0 8px; }
    .sub { color: var(--muted); font-size: 0.875rem; margin-bottom: 24px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .card { background: var(--card); border-radius: 12px; padding: 16px 18px; }
    .card .label { color: var(--muted); font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.04em; }
    .card .value { font-size: 1.5rem; font-weight: 600; margin-top: 4px; }
    .bar-wrap { height: 8px; background: #2f3b4a; border-radius: 4px; margin-top: 12px; overflow: hidden; }
    .bar { height: 100%; background: var(--bar); border-radius: 4px; }
    table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid #2f3b4a; }
    th { color: var(--muted); font-weight: 500; }
    td.num { font-variant-numeric: tabular-nums; }
    td.fail { color: var(--bad); }
    .footer { margin-top: 24px; color: var(--muted); font-size: 0.8rem; }
  </style>
</head>
<body>
  <h1>onharu k6 부하 테스트 결과</h1>
  <p class="sub">로컬 파일로 열람 중 · 생성 시각 ${esc(new Date().toISOString())}</p>

  <div class="grid">
    <div class="card">
      <div class="label">총 HTTP 요청</div>
      <div class="value">${esc(String(httpReqs))}</div>
    </div>
    <div class="card">
      <div class="label">이터레이션 수</div>
      <div class="value">${esc(String(iterCount))}</div>
    </div>
    <div class="card">
      <div class="label">실행 시간</div>
      <div class="value">${durationMs != null ? num(durationMs / 1000, 1) + 's' : '—'}</div>
    </div>
    <div class="card">
      <div class="label">HTTP 실패율</div>
      <div class="value" style="color:${failRate > 0.05 ? 'var(--bad)' : 'var(--ok)'}">${failPct}%</div>
      <div class="bar-wrap"><div class="bar" style="width:${Math.min(100, (Number(failPct) || 0))}%"></div></div>
    </div>
    <div class="card">
      <div class="label">체크 통과율</div>
      <div class="value" style="color:${checkRate >= 0.95 ? 'var(--ok)' : 'var(--bad)'}">${checkPct}%</div>
    </div>
  </div>

  <div class="card" style="margin-bottom:16px">
    <div class="label">응답 시간 (http_req_duration, ms)</div>
    <div style="margin-top:8px;font-size:0.9rem;color:var(--muted)">
      avg ${num(dur.avg, 2)} · med ${num(dur.med, 2)} · p(95) ${num(dur['p(95)'], 2)} · max ${num(dur.max, 2)}
    </div>
  </div>

  <div class="card" style="margin-bottom:16px">
    <div class="label">이터레이션 소요 (iteration_duration, ms)</div>
    <div style="margin-top:8px;font-size:0.9rem;color:var(--muted)">
      avg ${num(iterDur.avg, 2)} · med ${num(iterDur.med, 2)} · p(95) ${num(iterDur['p(95)'], 2)} · max ${num(iterDur.max, 2)}
    </div>
  </div>

  <div class="card">
    <div class="label">체크 상세</div>
    <table>
      <thead><tr><th>이름</th><th>통과</th><th>실패</th></tr></thead>
      <tbody>${rows || '<tr><td colspan="3">체크 없음</td></tr>'}</tbody>
    </table>
  </div>

  <p class="footer">k6 · report-last.html · 원본 JSON은 터미널 stdout 또는 --out json= 으로 저장 가능</p>
</body>
</html>`;
}
