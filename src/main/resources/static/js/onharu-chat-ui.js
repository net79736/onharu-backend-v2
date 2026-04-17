(function (global) {
  'use strict';

  /**
   * 채팅 테스트 UI (REST + STOMP)
   *
   * ─── 의존성 ───────────────────────────────────────
   *   deps.apiJson(method, url, body?)  : REST API 호출 함수
   *   deps.getCurrentUser()             : 현재 로그인 유저 객체 반환
   *   window.StompJs                    : @stomp/stompjs 라이브러리
   *
   * ─── 사용법 ───────────────────────────────────────
   *   initOnharuChatUi({ apiJson, getCurrentUser });
   *
   * ─── 목차 ─────────────────────────────────────────
   *   0. 의존성 / 헬퍼
   *      - getMe()            현재 로그인 유저 반환
   *      - getMyId()          현재 유저 ID (숫자)
   *
   *   1. DOM 참조 (el)
   *
   *   2. 상태 (State)
   *      - roomState          활성 채팅방 상태 (id, messages, cursor…)
   *      - newChatState       새 채팅방 모달 상태 (pick, suggest…)
   *
   *   3. 유틸
   *      - formatDate()       날짜 문자열 (2025년 4월 18일)
   *      - formatTime()       시간 문자열 (오후 03:00)
   *      - formatRelative()   상대 시간 (방금 / N분 전 / N시간 전)
   *      - escapeHtml()       XSS 방지 이스케이프
   *
   *   4. 채팅방 목록
   *      - loadChatRooms()    GET /api/chats
   *      - renderChatRooms()  목록 HTML 렌더링
   *      - highlightActiveRoom()
   *
   *   5. 채팅방 열기
   *      - openChatRoom()     상태 초기화 → 입장 → 메시지 로드 → STOMP 연결
   *
   *   6. 메시지 로드 (커서 기반 페이징)
   *      - loadMessages()     GET /api/chats/{id}/messages
   *                           scroll 상단 도달 시 자동 추가 로드
   *
   *   7. 메시지 렌더링
   *      - renderMessages()   전체 메시지 목록 재렌더링
   *      - buildMyMessage()   내 말풍선 HTML
   *      - buildOtherMessage() 상대 말풍선 HTML
   *
   *   8. STOMP WebSocket
   *      - connectStomp()     ws:// 연결 + /topic/chat/{id} 구독
   *      - onStompMessage()   실시간 메시지 수신 처리
   *      - disconnectStomp()  연결 해제
   *      - setConnStatus()    연결 상태 UI 텍스트 업데이트
   *      - setChatInputEnabled() 입력창/전송버튼 활성화 토글
   *
   *   9. 메시지 전송
   *      - sendChatMessage()  STOMP publish → /app/chat/send
   *
   *  10. 채팅방 나가기
   *      - DELETE /api/chats/{id} → disconnectStomp
   *
   *  11. 채팅 모달 열기/닫기
   *      - closeChatModal()
   *
   *  12. 새 채팅방 — 유저 검색 자동완성
   *      - searchUsers()      GET /api/users/search?keyword=
   *      - scheduleSearch()   디바운스 (280ms)
   *      - pickUser()         자동완성 선택 확정
   *      - syncSuggestHighlight() 키보드 하이라이트 동기화
   *      - resetNewChatForm() 폼 초기화
   *
   *  13. 새 채팅방 모달 열기/닫기/제출
   *      - showNewChatError()
   *      - POST /api/chats → openChatRoom()
   */
  function initOnharuChatUi(deps) {
    if (!deps || typeof deps.apiJson !== 'function' || typeof deps.getCurrentUser !== 'function') {
      console.error('[onharu-chat-ui] deps.apiJson, deps.getCurrentUser 이 필요합니다.');
      return;
    }

    // ─────────────────────────────────────────────
    // 0. 의존성 / 헬퍼
    // ─────────────────────────────────────────────

    var api = deps.apiJson;

    /** 현재 로그인 유저 정보. userId(또는 id), name(또는 loginId) 사용 */
    function getMe() {
      return deps.getCurrentUser();
    }

    /** 현재 유저 ID (숫자) */
    function getMyId() {
      var me = getMe();
      return me ? Number(me.userId ?? me.id) : null;
    }

    // ─────────────────────────────────────────────
    // 1. DOM 참조 (showModal 시점에 확정되므로 지연 조회 불필요)
    // ─────────────────────────────────────────────

    var el = {
      // 채팅 메인 모달
      chatModal:       document.getElementById('chatModal'),
      chatRoomList:    document.getElementById('chatRoomList'),
      chatEmpty:       document.getElementById('chatEmpty'),
      chatActive:      document.getElementById('chatActive'),
      chatMessages:    document.getElementById('chatMessages'),
      chatInput:       document.getElementById('chatInput'),
      chatHeaderName:  document.getElementById('chatHeaderName'),
      chatHeaderAvatar:document.getElementById('chatHeaderAvatar'),
      chatConnStatus:  document.getElementById('chatConnStatus'),
      btnChatSend:     document.getElementById('btnChatSend'),
      // 새 채팅방 모달
      newChatModal:      document.getElementById('newChatModal'),
      newChatMsg:        document.getElementById('newChatMsg'),
      newChatLoginSearch:document.getElementById('newChatLoginSearch'),
      newChatSuggest:    document.getElementById('newChatSuggest'),
      newChatSelected:   document.getElementById('newChatSelected'),
      newChatTargetId:   document.getElementById('newChatTargetId'),
      newChatRoomName:   document.getElementById('newChatRoomName'),
    };

    // ─────────────────────────────────────────────
    // 2. 상태 (State)
    //    흩어진 변수들을 목적별 객체로 묶어 관리
    // ─────────────────────────────────────────────

    /** 현재 열린 채팅방 관련 상태 */
    var roomState = {
      id: null,           // 활성 채팅방 ID
      name: '',           // 활성 채팅방 이름
      messages: [],       // 로드된 메시지 배열 (최신순 — index 0 이 가장 최근)
      oldestCursorId: null, // 커서 페이징: 마지막으로 로드한 가장 오래된 메시지 ID
      isLoadingMore: false, // 추가 로딩 중 플래그 (중복 요청 방지)
    };

    /** STOMP WebSocket 클라이언트 */
    var stompClient = null;

    /** 새 채팅방 생성 모달 관련 상태 */
    var newChatState = {
      pick: null,            // 선택된 유저 { userId, loginId, userType }
      suggestRows: [],       // 검색 결과 목록
      highlightIdx: -1,      // 키보드로 하이라이트된 행 인덱스
      searchTimer: null,     // 디바운스 타이머
      isComposing: false,    // IME 조합 중 여부 (한글 입력 중 Enter 방지)
    };

    /** IME 조합 중 여부 (채팅 입력창) */
    var chatComposing = false;

    // ─────────────────────────────────────────────
    // 3. 날짜/시간 포맷 유틸
    // ─────────────────────────────────────────────

    function formatDate(iso) {
      return new Date(iso).toLocaleDateString('ko-KR', {
        timeZone: 'Asia/Seoul', year: 'numeric', month: 'long', day: 'numeric',
      });
    }

    function formatTime(iso) {
      return new Date(iso).toLocaleTimeString('ko-KR', {
        timeZone: 'Asia/Seoul', hour: '2-digit', minute: '2-digit',
      });
    }

    function formatRelative(iso) {
      if (!iso) return '';
      var diff = Date.now() - new Date(iso).getTime();
      if (diff < 60_000)   return '방금';
      if (diff < 3_600_000) return Math.floor(diff / 60_000) + '분 전';
      if (diff < 86_400_000) return Math.floor(diff / 3_600_000) + '시간 전';
      return formatDate(iso);
    }

    function escapeHtml(s) {
      if (!s) return '';
      return String(s)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
    }

    // ─────────────────────────────────────────────
    // 4. 채팅방 목록
    // ─────────────────────────────────────────────

    async function loadChatRooms() {
      try {
        var data = await api('GET', '/api/chats');
        var rooms = data?.chatRoomResponses ?? [];
        renderChatRooms(rooms);
      } catch (e) {
        el.chatRoomList.innerHTML =
          '<div class="p-4 text-xs text-rose-600">목록 로딩 실패: ' + escapeHtml(e.message || e) + '</div>';
      }
    }

    function renderChatRooms(rooms) {
      if (!rooms.length) {
        el.chatRoomList.innerHTML =
          '<div class="flex-1 flex flex-col items-center justify-center p-6 text-center">'
          + '<div class="text-3xl mb-2">📭</div>'
          + '<p class="text-sm text-slate-500">채팅방이 없습니다</p>'
          + '<p class="text-xs text-slate-400 mt-1">새 채팅을 시작해 보세요</p>'
          + '</div>';
        return;
      }

      el.chatRoomList.innerHTML = rooms.map(function (room) {
        var name = room.chatParticipants?.join(', ') || '(알 수 없음)';
        var isActive = roomState.id === room.chatRoomId;
        var unread = room.unreadMessageCount || 0;

        return '<div class="chat-room-item flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-slate-100 border-b border-slate-100'
          + (isActive ? ' bg-emerald-50' : '')
          + '" data-room-id="' + room.chatRoomId + '" data-room-name="' + escapeHtml(name) + '">'
          + '<div class="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-800 text-sm font-bold shrink-0">'
          + escapeHtml(name.charAt(0)) + '</div>'
          + '<div class="flex-1 min-w-0">'
          + '<div class="flex items-center justify-between gap-2">'
          + '<span class="text-sm font-semibold text-slate-900 truncate">' + escapeHtml(name) + '</span>'
          + '<span class="text-[10px] text-slate-400 shrink-0">' + formatRelative(room.lastMessageTime) + '</span>'
          + '</div>'
          + '<div class="flex items-center justify-between gap-2 mt-0.5">'
          + '<p class="text-xs text-slate-500 truncate">' + escapeHtml(room.lastMessage || '') + '</p>'
          + (unread > 0
            ? '<span class="shrink-0 min-w-[18px] h-[18px] flex items-center justify-center rounded-full bg-emerald-700 text-white text-[10px] font-bold px-1">'
              + (unread > 99 ? '99+' : unread) + '</span>'
            : '')
          + '</div></div></div>';
      }).join('');
    }

    /** 현재 활성 채팅방 항목을 시각적으로 하이라이트 */
    function highlightActiveRoom() {
      el.chatRoomList.querySelectorAll('.chat-room-item').forEach(function (item) {
        item.classList.toggle('bg-emerald-50', Number(item.dataset.roomId) === roomState.id);
      });
    }

    el.chatRoomList.addEventListener('click', function (e) {
      var item = e.target.closest('.chat-room-item');
      if (!item) return;
      openChatRoom(Number(item.dataset.roomId), item.dataset.roomName || '');
    });

    // ─────────────────────────────────────────────
    // 5. 채팅방 열기
    // ─────────────────────────────────────────────

    async function openChatRoom(roomId, roomName) {
      if (roomState.id === roomId) return; // 이미 열린 방이면 무시

      // 기존 WebSocket 연결 종료 후 상태 초기화
      disconnectStomp();
      roomState.id = roomId;
      roomState.name = roomName;
      roomState.messages = [];
      roomState.oldestCursorId = null;

      // UI: 빈 화면 숨기고 채팅 영역 표시
      el.chatEmpty.classList.add('hidden');
      el.chatActive.classList.remove('hidden');
      el.chatHeaderName.textContent = roomName;
      el.chatHeaderAvatar.textContent = roomName.charAt(0);
      el.chatMessages.innerHTML = '<div class="text-center py-6 text-xs text-slate-400">메시지 로딩 중…</div>';
      setChatInputEnabled(false);
      highlightActiveRoom();

      // 입장 처리 (읽음 처리) → 메시지 로드 → STOMP 연결 순서 보장
      try {
        await api('POST', '/api/chats/' + roomId);
      } catch (e) {
        console.warn('[chat] 입장(읽음 처리) 실패', e);
      }

      await loadMessages(roomId, null);
      loadChatRooms(); // 목록의 unread 뱃지 갱신
      connectStomp(roomId);
    }

    // ─────────────────────────────────────────────
    // 6. 메시지 로드 (REST, 커서 기반 페이징)
    //
    //    서버 응답 순서: 최신 메시지가 앞(index 0)에 위치한다고 가정
    //    → roomState.messages 도 최신순 유지
    //    → 화면 렌더링 시 reverse() 로 시간순 정렬
    // ─────────────────────────────────────────────

    async function loadMessages(roomId, cursorId) {
      if (roomState.isLoadingMore) return;
      roomState.isLoadingMore = true;

      try {
        var url = '/api/chats/' + roomId + '/messages' + (cursorId ? '?cursorId=' + cursorId : '');
        var data = await api('GET', url);
        var msgs = data?.chatRoomMessageResponses ?? [];

        if (cursorId) {
          // 더 보기: 기존 배열 뒤에 과거 메시지 추가
          roomState.messages = roomState.messages.concat(msgs);
        } else {
          // 최초 로드
          roomState.messages = msgs;
        }

        // 다음 페이지 커서 = 이번에 받은 메시지 중 가장 오래된 ID
        if (msgs.length > 0) {
          roomState.oldestCursorId = msgs[msgs.length - 1].chatMessageId;
        }

        renderMessages();

        // 최초 로드 시에만 스크롤을 맨 아래로 이동
        if (!cursorId) {
          el.chatMessages.scrollTop = el.chatMessages.scrollHeight;
        }
      } catch (e) {
        el.chatMessages.innerHTML =
          '<div class="text-center py-6 text-xs text-rose-600">로딩 실패: ' + escapeHtml(e.message || e) + '</div>';
      } finally {
        roomState.isLoadingMore = false;
      }
    }

    /** 스크롤이 상단 근처에 닿으면 이전 메시지 더 불러오기 */
    el.chatMessages.addEventListener('scroll', function () {
      var nearTop = el.chatMessages.scrollTop < 40;
      var canLoadMore = roomState.oldestCursorId && !roomState.isLoadingMore && roomState.id;
      if (!nearTop || !canLoadMore) return;

      var prevScrollHeight = el.chatMessages.scrollHeight;
      loadMessages(roomState.id, roomState.oldestCursorId).then(function () {
        // 더 불러온 후 스크롤 위치가 튀지 않도록 보정
        el.chatMessages.scrollTop = el.chatMessages.scrollHeight - prevScrollHeight;
      });
    });

    // ─────────────────────────────────────────────
    // 7. 메시지 렌더링
    // ─────────────────────────────────────────────

    function renderMessages() {
      if (!roomState.messages.length) {
        el.chatMessages.innerHTML =
          '<div class="text-center py-8 text-xs text-slate-400">아직 메시지가 없습니다. 첫 메시지를 보내보세요!</div>';
        return;
      }

      // roomState.messages 는 최신순 → 화면에는 오름차순(과거→최신)으로 표시
      var chronological = roomState.messages.slice().reverse();
      var myId = getMyId();
      var prevDate = '';

      el.chatMessages.innerHTML = chronological.map(function (msg) {
        var html = '';
        var date = formatDate(msg.createdAt);

        // 날짜가 바뀌면 날짜 구분선 삽입
        if (date !== prevDate) {
          prevDate = date;
          html += '<div class="flex items-center gap-3 my-3">'
            + '<div class="flex-1 h-px bg-slate-200"></div>'
            + '<span class="text-[11px] text-slate-400 shrink-0">' + date + '</span>'
            + '<div class="flex-1 h-px bg-slate-200"></div>'
            + '</div>';
        }

        var time = formatTime(msg.createdAt);
        // ID 기반 판별 우선, sender 필드가 없을 경우 이름으로 fallback
        var isMine = myId != null && msg.sender != null
          ? Number(msg.sender) === myId
          : (msg.senderName && msg.senderName === (getMe()?.name ?? getMe()?.loginId));

        html += isMine ? buildMyMessage(msg.content, time) : buildOtherMessage(msg.senderName, msg.content, time);
        return html;
      }).join('');
    }

    function buildMyMessage(content, time) {
      return '<div class="flex justify-end items-end gap-1.5 mb-1">'
        + '<span class="text-[10px] text-slate-400 shrink-0 pb-0.5">' + time + '</span>'
        + '<div class="max-w-[70%] px-3 py-2 rounded-l-xl rounded-br-xl text-sm text-white whitespace-pre-wrap break-words" style="background:#2B6A5C;border:1px solid #2B6A5C;">'
        + escapeHtml(content)
        + '</div></div>';
    }

    function buildOtherMessage(senderName, content, time) {
      var initial = escapeHtml((senderName || '?').charAt(0));
      return '<div class="flex items-end gap-1.5 mb-1">'
        + '<div class="w-7 h-7 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 text-[10px] font-bold shrink-0">' + initial + '</div>'
        + '<div class="max-w-[70%]">'
        + '<p class="text-[10px] text-slate-500 mb-0.5 ml-1">' + escapeHtml(senderName || '알 수 없음') + '</p>'
        + '<div class="px-3 py-2 rounded-r-xl rounded-bl-xl border border-slate-200 bg-white text-sm text-slate-900 whitespace-pre-wrap break-words">'
        + escapeHtml(content)
        + '</div></div>'
        + '<span class="text-[10px] text-slate-400 shrink-0 pb-0.5">' + time + '</span>'
        + '</div>';
    }

    // ─────────────────────────────────────────────
    // 8. STOMP WebSocket
    // ─────────────────────────────────────────────

    var STOMP = {
      wsPath: '/ws-chat',
      topicRoom:   function (roomId) { return '/topic/chat/' + roomId; },
      sendDest:    '/app/chat/send',
    };

    function connectStomp(roomId) {
      if (!window.StompJs) {
        setConnStatus('STOMP 라이브러리 로드 실패', true);
        return;
      }

      var wsProto = location.protocol === 'https:' ? 'wss' : 'ws';

      stompClient = new StompJs.Client({
        brokerURL: wsProto + '://' + location.host + STOMP.wsPath,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      stompClient.onConnect = function () {
        setConnStatus('연결됨', false);
        setChatInputEnabled(true);
        el.chatInput.focus();

        // 특정 방(roomId) 채널을 구독합니다. 
        // "이 주소로 오는 메시지는 다 나한테 전달해줘"라고 우체국에 등록하는 것과 같으며, 
        // 새로운 메시지가 올 때마다 내부 콜백 함수(onStompMessage)가 실행됩니다.
        stompClient.subscribe(STOMP.topicRoom(roomId), function (frame) {
          onStompMessage(frame, roomId);
        });
      };

      stompClient.onDisconnect = function () {
        setConnStatus('연결 끊김', true);
        setChatInputEnabled(false);
      };

      stompClient.onStompError = function (frame) {
        console.error('[STOMP] error', frame);
        setConnStatus('오류 발생', true);
        setChatInputEnabled(false);
      };

      stompClient.activate(); // 서버와 연결된 "파이프라인"을 뚫는 작업. (한번만 실행)
    }

    /** STOMP 구독으로 새 메시지 수신 시 처리 */
    function onStompMessage(frame, roomId) {
      try {
        var newMsg = JSON.parse(frame.body);

        // 중복 수신 방지 (REST로 이미 로드한 메시지를 STOMP로 다시 받는 경우)
        var isDuplicate = roomState.messages.some(function (m) {
          return m.chatMessageId === newMsg.chatMessageId;
        });
        if (isDuplicate) return;

        roomState.messages.unshift(newMsg); // 최신순 배열의 맨 앞에 추가
        renderMessages();
        el.chatMessages.scrollTop = el.chatMessages.scrollHeight;

        // 현재 열린 방이면 읽음 처리
        if (Number(roomId) === roomState.id) {
          api('POST', '/api/chats/' + roomId)
            .then(loadChatRooms)
            .catch(function (e) { console.warn('[chat] 실시간 읽음 처리 실패', e); });
        }
      } catch (e) {
        console.error('[STOMP] 메시지 파싱 실패', e);
      }
    }

    function disconnectStomp() {
      if (!stompClient) return;
      try { stompClient.deactivate(); } catch (e) { /* 무시 */ }
      stompClient = null;
      setChatInputEnabled(false);
      setConnStatus('', false);
    }

    function setConnStatus(text, isError) {
      if (!el.chatConnStatus) return;
      el.chatConnStatus.classList.toggle('hidden', !text);
      el.chatConnStatus.textContent = text;
      el.chatConnStatus.className = 'text-[11px] mt-1 ' + (isError ? 'text-rose-500' : 'text-emerald-600');
    }

    function setChatInputEnabled(enabled) {
      el.chatInput.disabled = !enabled;
      el.btnChatSend.disabled = !enabled;
    }

    // ─────────────────────────────────────────────
    // 9. 메시지 전송
    // ─────────────────────────────────────────────

    function sendChatMessage() {
      var content = el.chatInput.value.trim();
      var canSend = content && stompClient?.connected && roomState.id;
      if (!canSend) return;

      stompClient.publish({
        destination: STOMP.sendDest,
        body: JSON.stringify({
          chatRoomId: roomState.id,
          senderId:   getMyId() ?? 0,
          content:    content,
        }),
      });

      el.chatInput.value = '';
      el.chatInput.style.height = 'auto';
      el.chatInput.focus();
    }

    el.btnChatSend.addEventListener('click', sendChatMessage);

    el.chatInput.addEventListener('compositionstart', function () { chatComposing = true; });
    el.chatInput.addEventListener('compositionend',   function () { chatComposing = false; });
    el.chatInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && !e.shiftKey && !chatComposing) {
        e.preventDefault();
        sendChatMessage();
      }
    });
    el.chatInput.addEventListener('input', function () {
      this.style.height = 'auto';
      this.style.height = Math.min(this.scrollHeight, 96) + 'px';
    });

    // ─────────────────────────────────────────────
    // 10. 채팅방 나가기
    // ─────────────────────────────────────────────

    document.getElementById('btnLeaveChat').addEventListener('click', async function () {
      if (!roomState.id || !confirm('이 채팅방을 나가시겠습니까?')) return;
      try {
        await api('DELETE', '/api/chats/' + roomState.id);
        disconnectStomp();
        roomState.id = null;
        el.chatActive.classList.add('hidden');
        el.chatEmpty.classList.remove('hidden');
        loadChatRooms();
      } catch (e) {
        alert('나가기 실패: ' + escapeHtml(e.message || e));
      }
    });

    // ─────────────────────────────────────────────
    // 11. 채팅 메인 모달 열기/닫기
    // ─────────────────────────────────────────────

    function closeChatModal() {
      disconnectStomp();
      roomState.id = null;
      el.chatActive.classList.add('hidden');
      el.chatEmpty.classList.remove('hidden');
      el.chatModal.close();
    }

    document.getElementById('btnOpenChat').addEventListener('click', function () {
      if (!getMe()) {
        alert('로그인 후 이용할 수 있습니다.');
        return;
      }
      el.chatModal.showModal();
      loadChatRooms();
    });

    document.getElementById('btnChatModalClose').addEventListener('click', closeChatModal);

    el.chatModal.addEventListener('click', function (e) {
      if (e.target === el.chatModal) closeChatModal();
    });

    // ─────────────────────────────────────────────
    // 12. 새 채팅방 생성 — 유저 검색 자동완성
    // ─────────────────────────────────────────────

    function resetNewChatForm() {
      newChatState.pick = null;
      newChatState.suggestRows = [];
      newChatState.highlightIdx = -1;
      if (newChatState.searchTimer) {
        clearTimeout(newChatState.searchTimer);
        newChatState.searchTimer = null;
      }
      el.newChatMsg.textContent = '';
      el.newChatMsg.className = 'text-xs text-slate-500 min-h-[1rem]';
      if (el.newChatTargetId)   el.newChatTargetId.value = '';
      if (el.newChatRoomName)   el.newChatRoomName.value = '';
      if (el.newChatLoginSearch) el.newChatLoginSearch.value = '';
      if (el.newChatSuggest) {
        el.newChatSuggest.innerHTML = '';
        el.newChatSuggest.classList.add('hidden');
      }
      if (el.newChatSelected) {
        el.newChatSelected.textContent = '';
        el.newChatSelected.classList.add('hidden');
      }
    }

    /** 자동완성 목록의 키보드 하이라이트 상태 동기화 */
    function syncSuggestHighlight() {
      if (!el.newChatSuggest) return;
      el.newChatSuggest.querySelectorAll('.new-chat-suggest-item').forEach(function (item, i) {
        var active = i === newChatState.highlightIdx;
        item.classList.toggle('bg-emerald-50', active);
        item.classList.toggle('ring-1', active);
        item.classList.toggle('ring-inset', active);
        item.classList.toggle('ring-emerald-200', active);
      });
    }

    /** 자동완성에서 유저 선택 확정 */
    function pickUser(user) {
      if (!user?.userId) return;
      newChatState.pick = { userId: user.userId, loginId: user.loginId, userType: user.userType };
      if (el.newChatTargetId) el.newChatTargetId.value = String(user.userId);
      if (el.newChatSelected) {
        el.newChatSelected.classList.remove('hidden');
        el.newChatSelected.textContent = '선택됨: ' + user.loginId + ' · ' + (user.userType || '');
      }
      // 드롭다운 닫기
      if (el.newChatSuggest) {
        el.newChatSuggest.innerHTML = '';
        el.newChatSuggest.classList.add('hidden');
      }
      newChatState.suggestRows = [];
      newChatState.highlightIdx = -1;
    }

    /** 로그인 ID 키워드로 유저 검색 (GET /api/users/search) */
    async function searchUsers(keyword) {
      keyword = (keyword || '').trim();
      if (keyword.length < 1) {
        if (el.newChatSuggest) {
          el.newChatSuggest.innerHTML = '';
          el.newChatSuggest.classList.add('hidden');
        }
        newChatState.suggestRows = [];
        newChatState.highlightIdx = -1;
        return;
      }

      try {
        var data = await api('GET', '/api/users/search?keyword=' + encodeURIComponent(keyword));
        var users = data?.users ?? [];
        newChatState.suggestRows = users;
        newChatState.highlightIdx = users.length ? 0 : -1;

        if (!el.newChatSuggest) return;

        if (!users.length) {
          el.newChatSuggest.innerHTML = '<div class="px-3 py-2 text-xs text-slate-500">검색 결과가 없습니다.</div>';
          el.newChatSuggest.classList.remove('hidden');
          return;
        }

        el.newChatSuggest.innerHTML = users.map(function (u, idx) {
          return '<button type="button" class="new-chat-suggest-item w-full text-left px-3 py-2 text-sm hover:bg-slate-50 border-b border-slate-100 last:border-0" data-idx="' + idx + '">'
            + '<span class="font-medium text-slate-900">' + escapeHtml(u.loginId) + '</span>'
            + '<span class="ml-2 text-[11px] text-slate-500">' + escapeHtml(String(u.userType || '')) + '</span>'
            + '</button>';
        }).join('');
        el.newChatSuggest.classList.remove('hidden');
        syncSuggestHighlight();
      } catch (e) {
        if (el.newChatSuggest) {
          el.newChatSuggest.innerHTML =
            '<div class="px-3 py-2 text-xs text-rose-600">' + escapeHtml(e.message || String(e)) + '</div>';
          el.newChatSuggest.classList.remove('hidden');
        }
      }
    }

    if (el.newChatLoginSearch) {
      el.newChatLoginSearch.addEventListener('compositionstart', function () {
        newChatState.isComposing = true;
      });
      el.newChatLoginSearch.addEventListener('compositionend', function () {
        newChatState.isComposing = false;
        scheduleSearch(el.newChatLoginSearch.value);
      });
      el.newChatLoginSearch.addEventListener('input', function () {
        if (newChatState.isComposing) return;
        scheduleSearch(this.value);
      });
      el.newChatLoginSearch.addEventListener('keydown', function (e) {
        var dropdownVisible = el.newChatSuggest && !el.newChatSuggest.classList.contains('hidden');

        if (!dropdownVisible) {
          // 드롭다운 닫힌 상태: Enter 로 바로 제출
          if (e.key === 'Enter' && newChatState.pick) {
            e.preventDefault();
            document.getElementById('btnNewChatSubmit').click();
          }
          return;
        }

        switch (e.key) {
          case 'ArrowDown':
            e.preventDefault();
            newChatState.highlightIdx = Math.min(newChatState.highlightIdx + 1, newChatState.suggestRows.length - 1);
            syncSuggestHighlight();
            break;
          case 'ArrowUp':
            e.preventDefault();
            newChatState.highlightIdx = Math.max(newChatState.highlightIdx - 1, 0);
            syncSuggestHighlight();
            break;
          case 'Enter':
            e.preventDefault();
            if (newChatState.suggestRows[newChatState.highlightIdx]) {
              pickUser(newChatState.suggestRows[newChatState.highlightIdx]);
            }
            break;
          case 'Escape':
            el.newChatSuggest.classList.add('hidden');
            newChatState.highlightIdx = -1;
            break;
        }
      });
    }

    function scheduleSearch(value) {
      if (newChatState.searchTimer) clearTimeout(newChatState.searchTimer);
      newChatState.searchTimer = setTimeout(function () { searchUsers(value); }, 280);
    }

    if (el.newChatSuggest) {
      // mousedown 에서 preventDefault → blur 이벤트 없이 클릭 처리
      el.newChatSuggest.addEventListener('mousedown', function (e) { e.preventDefault(); });
      el.newChatSuggest.addEventListener('click', function (e) {
        var item = e.target.closest('.new-chat-suggest-item');
        if (!item) return;
        pickUser(newChatState.suggestRows[Number(item.dataset.idx)]);
      });
    }

    // ─────────────────────────────────────────────
    // 13. 새 채팅방 생성 모달 — 열기/닫기/제출
    // ─────────────────────────────────────────────

    document.getElementById('btnNewChat').addEventListener('click', function () {
      resetNewChatForm();
      el.newChatModal.showModal();
      setTimeout(function () { el.newChatLoginSearch?.focus(); }, 0);
    });

    document.getElementById('btnNewChatClose').addEventListener('click', function () {
      resetNewChatForm();
      el.newChatModal.close();
    });

    el.newChatModal.addEventListener('click', function (e) {
      if (e.target === el.newChatModal) {
        resetNewChatForm();
        el.newChatModal.close();
      }
    });

    document.getElementById('btnNewChatSubmit').addEventListener('click', async function () {
      if (!newChatState.pick?.userId) {
        showNewChatError('검색 목록에서 상대를 선택해 주세요. (↑↓ 이동, Enter 확정)');
        return;
      }

      var roomName = el.newChatRoomName?.value.trim()
        || newChatState.pick.loginId + '님과의 채팅';

      try {
        var data = await api('POST', '/api/chats', {
          name:     roomName,
          roomType: 'ONE_TO_ONE',
          targetId: Number(newChatState.pick.userId),
        });
        resetNewChatForm();
        el.newChatModal.close();
        await loadChatRooms();
        if (data?.chatRoomId) {
          openChatRoom(data.chatRoomId, roomName);
        }
      } catch (e) {
        showNewChatError('생성 실패: ' + escapeHtml(e.message || e));
      }
    });

    function showNewChatError(msg) {
      el.newChatMsg.textContent = msg;
      el.newChatMsg.className = 'text-xs text-rose-600 min-h-[1rem]';
    }

  } // end initOnharuChatUi

  global.initOnharuChatUi = initOnharuChatUi;

})(typeof window !== 'undefined' ? window : this);