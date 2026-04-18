#!/usr/bin/env bash
set -euo pipefail

# onharu-backend-v2 k6 부하 테스트
#
# ./run.sh 만 실행:
#   - K6_COOKIE / K6_BEARER_TOKEN 없음 → 공개 API 스모크 (인증 불필요, HTML 리포트 없음)
#   - 쿠키/토큰 있음 → 예약+채팅 도메인 + report-last.html (성공 시 브라우저)
#
# 도메인 테스트를 꼭 쓰려면:
#   export K6_COOKIE='JSESSIONID=...'
#   ./run.sh
#
# 스크립트 고정:
#   K6_SCRIPT=onharu-api-smoke.js ./run.sh
#   K6_SCRIPT=onharu-domains-load.js K6_COOKIE=... ./run.sh
#
# 기타: BASE_URL, ONHARU_VUS(기본100), ONHARU_ITERATIONS, ONHARU_MAX_DURATION, ONHARU_SLEEP_SEC
#       ONHARU_CHAT_MODE=send(기본, STOMP 전송) | list(GET 목록만)
#       K6_CHAT_ROOM_ID / K6_SENDER_ID (전송 시 setup 생략), K6_RESERVATION_ROLE, OPEN_REPORT=0
#
# 사전: https://k6.io/docs/getting-started/installation/

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v k6 >/dev/null 2>&1; then
  echo "k6 가 설치되어 있지 않습니다. https://k6.io/docs/getting-started/installation/" >&2
  exit 1
fi

if [[ -n "${K6_SCRIPT:-}" ]]; then
  RESOLVED_SCRIPT="$K6_SCRIPT"
elif [[ -n "${K6_COOKIE:-}" || -n "${K6_BEARER_TOKEN:-}" ]]; then
  RESOLVED_SCRIPT="onharu-domains-load.js"
  echo "→ 인증 사용: 예약+채팅 도메인 테스트 (${RESOLVED_SCRIPT})" >&2
else
  RESOLVED_SCRIPT="onharu-api-smoke.js"
  echo "→ 인증 없음: 공개 API 스모크 (${RESOLVED_SCRIPT}). 도메인 테스트는 export K6_COOKIE=... 후 ./run.sh" >&2
fi

DOMAINS_BASENAME="onharu-domains-load.js"
IS_DOMAINS=0
[[ "$(basename "$RESOLVED_SCRIPT")" == "$DOMAINS_BASENAME" ]] && IS_DOMAINS=1

set +e
k6 run "$RESOLVED_SCRIPT"
code=$?
set -e

# 도메인 스크립트가 성공했을 때만 HTML 열기 (스모크는 리포트 미생성 · 이전 파일 오열림 방지)
if [[ "$code" -eq 0 ]] && [[ "$IS_DOMAINS" -eq 1 ]] && [[ "${OPEN_REPORT:-1}" == "1" ]] && [[ -f report-last.html ]]; then
  echo ""
  echo "HTML 리포트: ${SCRIPT_DIR}/report-last.html"
  if [[ "$(uname -s)" == "Darwin" ]]; then
    open "${SCRIPT_DIR}/report-last.html"
  elif command -v xdg-open >/dev/null 2>&1; then
    xdg-open "${SCRIPT_DIR}/report-last.html"
  fi
fi

exit "$code"
