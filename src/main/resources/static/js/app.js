// src/main/resources/static/js/app.js
// HTML/CSS 건드리지 않고 기능별 네임스페이스 구조로 정리.
// - 동작/DOM/CSS 클래스/ID는 기존과 동일하게 유지
// - 전역 함수명(loadTradesByDate 등)을 유지해야 다른 스크립트/콘솔 호출/기존 습관과의 충돌이 없음
// - 내부 구현만 App.* 네임스페이스로 묶음

(() => {
    "use strict";

    // ================================
    // App 네임스페이스
    // ================================
    const App = {};

    // ================================
    // 전역 상태 (App.State)
    // ================================
    App.State = {
        // 선택 날짜 기준 "통합 로그(매매+입출금)" 페이지 크기
        TRADE_PAGE_SIZE: 10,

        // null이면 "오늘", yyyy-MM-dd 문자열이면 특정 날짜
        currentTradeDate: null,

        // 커서 기반 페이지네이션 상태
        logPrevCursor: null, // 현재 페이지의 첫 아이템 커서
        logNextCursor: null, // 현재 페이지의 마지막 아이템 커서
        logHasPrev: false,
        logHasNext: false,

        // 캘린더 상태 (년/월)
        calendarYear: null,
        calendarMonth: null,
    };

    // ================================
    // 유틸 (App.Util)
    // ================================
    App.Util = {
        formatNumberKR(value) {
            if (value === null || value === undefined) return "-";
            if (typeof value === "string") {
                const num = Number(value);
                if (!Number.isFinite(num)) return value;
                return num.toLocaleString("ko-KR");
            }
            return value.toLocaleString("ko-KR");
        },

        formatPnl(value) {
            if (value === null || value === undefined) return "0원";
            const num = Number(value);
            if (!Number.isFinite(num)) return "0원";
            const sign = num > 0 ? "+" : "";
            return sign + App.Util.formatNumberKR(num) + "원";
        },

        formatTime(localDateTimeStr) {
            if (!localDateTimeStr) return "";
            // "2025-12-04T09:15:10" -> "09:15"
            const t = localDateTimeStr.split("T")[1] || "";
            return t.substring(0, 5);
        },

        formatDateYmd(dateObj) {
            const y = dateObj.getFullYear();
            const m = String(dateObj.getMonth() + 1).padStart(2, "0");
            const d = String(dateObj.getDate()).padStart(2, "0");
            return `${y}-${m}-${d}`;
        },

        /**
         * Authorization 헤더 wrapper
         * - dashboard-analytics.js 에서도 그대로 authHeader() 를 사용하도록
         *   AuthUtil.authHeader() 를 thin wrapper 로 제공
         */
        authHeader() {
            if (!window.AuthUtil) return {};
            return AuthUtil.authHeader();
        },
    };

    // ================================
    // 통합 로그 (App.Logs)
    // ================================
    App.Logs = {
        getEffectiveDateStr() {
            const s = App.State.currentTradeDate;
            if (s) return s;
            return App.Util.formatDateYmd(new Date());
        },

        resetPagingState() {
            App.State.logPrevCursor = null;
            App.State.logNextCursor = null;
            App.State.logHasPrev = false;
            App.State.logHasNext = false;
        },

        buildLogsUrl(dateStr, cursor, dir) {
            const params = new URLSearchParams();
            if (dateStr) params.set("date", dateStr);
            params.set("size", String(App.State.TRADE_PAGE_SIZE));
            params.set("dir", dir || "next");
            if (cursor) params.set("cursor", cursor);
            return "/api/logs?" + params.toString();
        },

        async loadToday() {
            App.State.currentTradeDate = null;
            App.Logs.resetPagingState();

            const dateStr = App.Logs.getEffectiveDateStr();
            const url = App.Logs.buildLogsUrl(dateStr, null, "next");
            await App.Logs.fetchAndRender(url);
        },

        async loadByDate(dateStr) {
            App.State.currentTradeDate = dateStr;
            App.Logs.resetPagingState();

            const url = App.Logs.buildLogsUrl(dateStr, null, "next");
            await App.Logs.fetchAndRender(url);
        },

        async loadPrevPage() {
            if (!App.State.logHasPrev || !App.State.logPrevCursor) return;

            const dateStr = App.Logs.getEffectiveDateStr();
            const url = App.Logs.buildLogsUrl(dateStr, App.State.logPrevCursor, "prev");
            await App.Logs.fetchAndRender(url);
        },

        async loadNextPage() {
            if (!App.State.logHasNext || !App.State.logNextCursor) return;

            const dateStr = App.Logs.getEffectiveDateStr();
            const url = App.Logs.buildLogsUrl(dateStr, App.State.logNextCursor, "next");
            await App.Logs.fetchAndRender(url);
        },

        // 공통 fetch 처리
        async fetchAndRender(url) {
            try {
                const res = await fetch(url, {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        ...App.Util.authHeader(),
                    },
                });

                if (res.status === 401 || res.status === 403) {
                    window.location.href = "/index.html";
                    return;
                }

                if (!res.ok) {
                    console.error("Failed to fetch logs:", res.status);
                    App.UI.renderEmptyTradeList();
                    return;
                }

                const body = await res.json();

                // body.items: 통합 로그 리스트
                const items = body.items || [];

                App.State.logPrevCursor = body.prevCursor || null;
                App.State.logNextCursor = body.nextCursor || null;
                App.State.logHasPrev = !!body.hasPrev;
                App.State.logHasNext = !!body.hasNext;

                App.UI.renderTradeList(items);
                App.UI.renderTradePagination();
            } catch (e) {
                console.error("Error fetching logs:", e);
                App.UI.renderEmptyTradeList();
            }
        },
    };

    // ================================
    // UI 렌더링 (App.UI)
    // ================================
    App.UI = {
        renderEmptyTradeList() {
            const ul = document.getElementById("trade-log-list");
            if (!ul) return;

            ul.innerHTML = "";
            const li = document.createElement("li");
            li.className = "trade-log-item";
            li.textContent = "해당 날짜의 로그가 없습니다.";
            ul.appendChild(li);

            const pagination = document.getElementById("trade-pagination");
            if (pagination) {
                pagination.innerHTML = "";
            }
        },

        renderTradeList(items) {
            const ul = document.getElementById("trade-log-list");
            if (!ul) return;

            ul.innerHTML = "";

            if (!items || items.length === 0) {
                App.UI.renderEmptyTradeList();
                return;
            }

            items.forEach((t) => {
                const li = document.createElement("li");
                li.className = "trade-log-item";

                // 좌측 뱃지(매수/매도/입금/출금)
                const sideDiv = document.createElement("div");

                const side = (t.side || "").toUpperCase();

                let badgeClass = "trade-side ";
                let badgeText = "-";

                if (side === "BUY") {
                    badgeClass += "trade-side-buy";
                    badgeText = "매수";
                } else if (side === "SELL") {
                    badgeClass += "trade-side-sell";
                    badgeText = "매도";
                } else if (side === "DEPOSIT") {
                    // 입금/출금 전용 클래스(색상은 별도 CSS 파일에서 오버라이드)
                    badgeClass += "trade-side-deposit";
                    badgeText = "입금";
                } else if (side === "WITHDRAWAL") {
                    // 입금/출금 전용 클래스(색상은 별도 CSS 파일에서 오버라이드)
                    badgeClass += "trade-side-withdraw";
                    badgeText = "출금";
                }

                sideDiv.className = badgeClass;
                sideDiv.textContent = badgeText;

                // 중앙 금액/수량(or 메모)
                const mainDiv = document.createElement("div");
                mainDiv.className = "trade-main";

                const amountDiv = document.createElement("div");
                amountDiv.className = "trade-amount";
                amountDiv.textContent = App.Util.formatNumberKR(t.amountKrw) + "원";

                const volumeDiv = document.createElement("div");
                volumeDiv.className = "trade-volume";

                // TRADE면 BTC 수량, CAPITAL이면 memo 표시(없으면 "-")
                if (side === "BUY" || side === "SELL") {
                    const volumeVal = t.volume != null ? Number(t.volume) : 0;
                    volumeDiv.textContent = volumeVal.toFixed(4) + " BTC";
                } else {
                    const memo =
                        t.memo != null && String(t.memo).trim() !== ""
                            ? String(t.memo)
                            : "-";
                    volumeDiv.textContent = memo;
                }

                mainDiv.appendChild(amountDiv);
                mainDiv.appendChild(volumeDiv);

                // 우측 시간/손익(or "-")
                const metaDiv = document.createElement("div");
                metaDiv.className = "trade-meta";

                const timeDiv = document.createElement("div");
                timeDiv.className = "trade-time";
                timeDiv.textContent = App.Util.formatTime(t.tradeTime);

                const pnlDiv = document.createElement("div");

                if (side === "BUY" || side === "SELL") {
                    const pnlNum = t.profitLossKrw != null ? Number(t.profitLossKrw) : 0;
                    pnlDiv.className =
                        "trade-pnl " +
                        (pnlNum >= 0 ? "trade-pnl-positive" : "trade-pnl-negative");
                    pnlDiv.textContent = App.Util.formatPnl(pnlNum);
                } else {
                    pnlDiv.className = "trade-pnl";
                    pnlDiv.textContent = "-";
                }

                metaDiv.appendChild(timeDiv);
                metaDiv.appendChild(pnlDiv);

                li.appendChild(sideDiv);
                li.appendChild(mainDiv);
                li.appendChild(metaDiv);

                ul.appendChild(li);
            });
        },

        // 페이지네이션 렌더링 (커서 기반: 이전/다음만)
        renderTradePagination() {
            const container = document.getElementById("trade-pagination");
            if (!container) return;

            container.innerHTML = "";

            // Prev
            const prevBtn = document.createElement("button");
            prevBtn.className = "trade-page-btn" + (!App.State.logHasPrev ? " disabled" : "");
            prevBtn.textContent = "〈";
            if (App.State.logHasPrev) {
                prevBtn.addEventListener("click", () => {
                    App.Logs.loadPrevPage();
                });
            }
            container.appendChild(prevBtn);

            // Next
            const nextBtn = document.createElement("button");
            nextBtn.className = "trade-page-btn" + (!App.State.logHasNext ? " disabled" : "");
            nextBtn.textContent = "〉";
            if (App.State.logHasNext) {
                nextBtn.addEventListener("click", () => {
                    App.Logs.loadNextPage();
                });
            }
            container.appendChild(nextBtn);
        },
    };

    // ================================
    // 알림 사이드바 (App.Notifications)
    // ================================
    App.Notifications = {
        initSidebar() {
            const sidebar = document.getElementById("notification-sidebar");
            const backdrop = document.getElementById("notification-backdrop");
            const bellButton = document.getElementById("notification-btn"); // 헤더 종 아이콘
            const closeBtn = document.getElementById("notification-close-btn");

            if (bellButton && sidebar && backdrop) {
                bellButton.addEventListener("click", () => {
                    sidebar.classList.add("open");
                    backdrop.classList.add("show");
                    App.Notifications.loadSettings();
                });
            }

            if (closeBtn) {
                closeBtn.addEventListener("click", App.Notifications.closeSidebar);
            }

            if (backdrop) {
                backdrop.addEventListener("click", App.Notifications.closeSidebar);
            }

            // 토글 변경 시 서버로 저장
            document.querySelectorAll(".ios-toggle").forEach((label) => {
                const checkbox = label.querySelector("input[type='checkbox']");
                if (!checkbox) return;
                checkbox.addEventListener("change", () => {
                    App.Notifications.saveSettings();
                });
            });
        },

        closeSidebar() {
            const sidebar = document.getElementById("notification-sidebar");
            const backdrop = document.getElementById("notification-backdrop");

            if (sidebar) sidebar.classList.remove("open");
            if (backdrop) backdrop.classList.remove("show");
        },

        async loadSettings() {
            try {
                const res = await fetch("/api/notifications/settings", {
                    method: "GET",
                    headers: {
                        Accept: "application/json",
                        ...App.Util.authHeader(),
                    },
                });

                if (!res.ok) {
                    console.error("알림 설정 조회 실패", res.status);
                    return;
                }

                const data = await res.json();
                App.Notifications.setToggleChecked("buy", !!data.enableBuy);
                App.Notifications.setToggleChecked("sell", !!data.enableSell);
                App.Notifications.setToggleChecked("error", !!data.enableError);
            } catch (e) {
                console.error("알림 설정 조회 중 오류", e);
            }
        },

        setToggleChecked(key, checked) {
            const label = document.querySelector(`.ios-toggle[data-key='${key}']`);
            if (!label) return;

            const checkbox = label.querySelector("input[type='checkbox']");
            if (!checkbox) return;

            checkbox.checked = checked;
        },

        async saveSettings() {
            const enableBuy = App.Notifications.getToggleChecked("buy");
            const enableSell = App.Notifications.getToggleChecked("sell");
            const enableError = App.Notifications.getToggleChecked("error");

            const body = {
                enableBuy: enableBuy,
                enableSell: enableSell,
                enableError: enableError,
            };

            try {
                const res = await fetch("/api/notifications/settings", {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                        ...App.Util.authHeader(),
                    },
                    body: JSON.stringify(body),
                });

                if (!res.ok) {
                    console.error("알림 설정 저장 실패", res.status);
                }
            } catch (e) {
                console.error("알림 설정 저장 중 오류", e);
            }
        },

        getToggleChecked(key) {
            const label = document.querySelector(`.ios-toggle[data-key='${key}']`);
            if (!label) return false;

            const checkbox = label.querySelector("input[type='checkbox']");
            return !!(checkbox && checkbox.checked);
        },
    };

    // ================================
    // 캘린더 (App.Calendar)
    // ================================
    App.Calendar = {
        init() {
            const now = new Date();
            App.State.calendarYear = now.getFullYear();
            App.State.calendarMonth = now.getMonth(); // 0~11

            App.Calendar.render();

            const prevBtn = document.getElementById("calendar-prev-btn");
            const nextBtn = document.getElementById("calendar-next-btn");

            if (prevBtn) {
                prevBtn.addEventListener("click", () => App.Calendar.move(-1));
            }
            if (nextBtn) {
                nextBtn.addEventListener("click", () => App.Calendar.move(1));
            }
        },

        move(deltaMonth) {
            App.State.calendarMonth += deltaMonth;

            if (App.State.calendarMonth < 0) {
                App.State.calendarMonth = 11;
                App.State.calendarYear -= 1;
            } else if (App.State.calendarMonth > 11) {
                App.State.calendarMonth = 0;
                App.State.calendarYear += 1;
            }

            App.Calendar.render();
        },

        render() {
            const titleEl = document.getElementById("calendarTitle");
            const gridEl = document.getElementById("calendar-grid");
            if (!titleEl || !gridEl) return;

            titleEl.textContent = `${App.State.calendarYear}. ${String(
                App.State.calendarMonth + 1
            ).padStart(2, "0")}`;

            gridEl.innerHTML = "";

            const dayNames = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];
            dayNames.forEach((name) => {
                const cell = document.createElement("div");
                cell.className = "calendar-day-name";
                cell.textContent = name;
                gridEl.appendChild(cell);
            });

            const firstDay = new Date(App.State.calendarYear, App.State.calendarMonth, 1);
            const lastDate = new Date(App.State.calendarYear, App.State.calendarMonth + 1, 0).getDate();
            const startWeekday = firstDay.getDay(); // 0=SUN

            // 앞쪽 빈칸
            for (let i = 0; i < startWeekday; i++) {
                const empty = document.createElement("div");
                empty.className = "calendar-day empty";
                gridEl.appendChild(empty);
            }

            const today = new Date();
            const todayStr = App.Util.formatDateYmd(today);

            for (let d = 1; d <= lastDate; d++) {
                const btn = document.createElement("button");
                btn.type = "button";
                btn.className = "calendar-day";
                btn.textContent = String(d);

                const dateStr = `${App.State.calendarYear}-${String(App.State.calendarMonth + 1).padStart(
                    2,
                    "0"
                )}-${String(d).padStart(2, "0")}`;

                if (dateStr === todayStr) {
                    btn.classList.add("is-today");
                }

                if (
                    (App.State.currentTradeDate && App.State.currentTradeDate === dateStr) ||
                    (!App.State.currentTradeDate && dateStr === todayStr)
                ) {
                    btn.classList.add("is-selected");
                }

                btn.addEventListener("click", () => {
                    App.State.currentTradeDate = dateStr;
                    App.Logs.loadByDate(dateStr);

                    // 선택 날짜 기준으로 브리핑보드 + 수익률 그래프도 갱신
                    if (typeof updateAnalyticsForDate === "function") {
                        updateAnalyticsForDate(dateStr);
                    }

                    App.Calendar.render();
                });

                gridEl.appendChild(btn);
            }
        },
    };

    // ================================
    // FCM 더미 알림 테스트 (App.Test)
    // ================================
    App.Test = {
        async sendTestNotification() {
            try {
                const res = await fetch("/api/notifications/test", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        ...App.Util.authHeader(),
                    },
                });

                if (!res.ok) {
                    console.error("테스트 알림 호출 실패", res.status);
                }
            } catch (e) {
                console.error("테스트 알림 호출 중 오류", e);
            }
        },
    };

    // ================================
    // 초기화 (App.Bootstrap)
    // ================================
    App.Bootstrap = {
        init() {
            if (!window.AuthUtil) {
                console.error("AuthUtil이 로드되지 않았습니다. /js/auth-util.js 포함 여부를 확인하세요.");
                window.location.href = "/index.html";
                return;
            }

            // 탭/창 종료 시 정리 로직 세팅
            AuthUtil.setupTabCloseCleanup();

            // JWT 로드 (URL 쿼리 또는 저장소)
            const token = AuthUtil.getJwtTokenFromUrlOrStorage();
            if (!token) {
                window.location.href = "/index.html";
                return;
            }

            // 로그아웃 버튼 초기화
            AuthUtil.initLogoutButton();

            App.Notifications.initSidebar();
            App.Calendar.init();
            App.Logs.loadToday();

            // 브리핑보드 + 수익률 그래프 초기화 (오늘 기준)
            if (typeof initDashboardAnalytics === "function") {
                initDashboardAnalytics();
            }
        },
    };

    // ================================
    // (호환용) 기존 전역 함수명 유지
    // - HTML/CSS 0수정 + 기존 호출 습관/다른 스크립트에서의 참조 가능성 대비
    // ================================
    window.formatNumberKR = App.Util.formatNumberKR;
    window.formatPnl = App.Util.formatPnl;
    window.formatTime = App.Util.formatTime;
    window.formatDateYmd = App.Util.formatDateYmd;
    window.authHeader = App.Util.authHeader;

    window.loadTodayTrades = () => App.Logs.loadToday();
    window.loadTradesByDate = (dateStr) => App.Logs.loadByDate(dateStr);
    window.loadPrevLogsPage = () => App.Logs.loadPrevPage();
    window.loadNextLogsPage = () => App.Logs.loadNextPage();

    window.sendTestNotification = () => App.Test.sendTestNotification();

    // ================================
    // DOMContentLoaded
    // ================================
    document.addEventListener("DOMContentLoaded", () => {
        App.Bootstrap.init();
    });
})();
