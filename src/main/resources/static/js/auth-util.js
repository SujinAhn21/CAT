// src/main/resources/static/js/auth-util.js
// -------------------------------
// - JWT 저장/조회/삭제
// - URL ?token= 처리
// - Authorization 헤더 생성
// - 로그아웃 버튼 초기화
// - (이전) 탭/창 종료 시 토큰/세션 정리
// -------------------------------

(function (global) {
    const AUTH_TOKEN_KEY = "coin_jwt_token";

    /**
     * JWT 토큰 저장
     * - 세션 단위(sessionStorage)로만 저장한다.
     * - 과거 버전에서 localStorage에 남아있을 수 있는 토큰은 보안 목적상 제거한다.
     */
    function setAuthToken(token) {
        if (!token) return;

        try {
            sessionStorage.setItem(AUTH_TOKEN_KEY, token);
        } catch (e) {
            // 무시
        }

        // 과거 잔재 정리(남아있으면 위험하므로 제거)
        try {
            localStorage.removeItem(AUTH_TOKEN_KEY);
        } catch (e) {
            // 무시
        }
    }

    /**
     * JWT 토큰 조회
     * - sessionStorage에서만 읽는다.
     * - localStorage fallback을 쓰면 "브라우저 종료 후에도 자동로그인"이 발생한다.
     */
    function getAuthToken() {
        try {
            return sessionStorage.getItem(AUTH_TOKEN_KEY);
        } catch (e) {
            return null;
        }
    }

    /**
     * JWT 토큰 삭제
     * - 세션/로컬 모두 제거
     *   (로컬은 과거 버전 잔재 정리 목적)
     */
    function clearAuthToken() {
        try {
            sessionStorage.removeItem(AUTH_TOKEN_KEY);
        } catch (e) {
            // 무시
        }

        try {
            localStorage.removeItem(AUTH_TOKEN_KEY);
        } catch (e) {
            // 무시
        }
    }

    /**
     * URL 쿼리 또는 저장소에서 JWT 가져오기
     * 1) /dashboard.html?token=... 으로 들어온 경우:
     *    - token 값을 저장소에 저장
     *    - 주소창에서 token 쿼리 제거
     * 2) 그 외:
     *    - sessionStorage 에서 읽기
     */
    function getJwtTokenFromUrlOrStorage() {
        const params = new URLSearchParams(window.location.search);
        const tokenFromUrl = params.get("token");

        if (tokenFromUrl) {
            setAuthToken(tokenFromUrl);

            // 주소창에서 token 쿼리 제거
            params.delete("token");
            const newQuery = params.toString();
            const newUrl =
                window.location.pathname + (newQuery ? "?" + newQuery : "");
            window.history.replaceState({}, "", newUrl);

            return tokenFromUrl;
        }

        return getAuthToken();
    }

    /**
     * Authorization 헤더 생성
     */
    function authHeader() {
        const token = getAuthToken();
        if (!token) return {};
        return {
            Authorization: "Bearer " + token,
        };
    }

    /**
     * 로그아웃 버튼 초기화
     * - 토큰 삭제
     * - /logout 로 이동 (SecurityConfig + KakaoLogoutSuccessHandler 동작)
     */
    function initLogoutButton() {
        const logoutBtn = document.getElementById("logout-btn");
        if (!logoutBtn) return;

        logoutBtn.addEventListener("click", () => {
            clearAuthToken();
            window.location.href = "/logout";
        });
    }

    /**
     * 탭/창 종료 시 토큰 정리 + 서버 로그아웃 시도
     *
     * 기존에는 beforeunload/unload에 토큰 삭제를 걸어두었는데,
     * - 브라우저/상황에 따라 이벤트가 안정적이지 않다.
     * - 새로고침에도 발동해 토큰이 지워지는 부작용이 있다.
     *
     * "브라우저 X로 닫으면 다음엔 재로그인" 요구는
     * sessionStorage 전용 운영으로 해결되므로, 여기서는 과거 localStorage 잔재만 정리한다.
     */
    function setupTabCloseCleanup() {
        try {
            localStorage.removeItem(AUTH_TOKEN_KEY);
        } catch (e) {
            // 무시
        }
    }

    // 전역 객체로 노출
    global.AuthUtil = {
        AUTH_TOKEN_KEY,
        setAuthToken,
        getAuthToken,
        clearAuthToken,
        getJwtTokenFromUrlOrStorage,
        authHeader,
        initLogoutButton,
        setupTabCloseCleanup,
    };
})(window);
