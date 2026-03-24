// src/main/resources/static/js/dashboard-analytics.js

// =======================================
// 브리핑보드 + 수익률 그래프 전용 모듈
//  - Summary / Daily Stats API 연동
//  - 수익률 그래프: 누적 수익률(%) 기준
// =======================================

// 수익률 차트 인스턴스
let profitChartInstance = null;

/**
 * 브리핑보드 업데이트
 * - summary: /api/summary 응답 객체
 *
 * summary.principalKrw      : 원금(순투입 자본)
 * summary.currentKrw        : 현재 금액 (원금 + 누적 실현 손익)
 * summary.totalDepositKrw   : 총 입금액
 * summary.totalWithdrawKrw  : 총 출금액
 * summary.dayDepositKrw     : 선택 날짜 입금액
 * summary.dayWithdrawKrw    : 선택 날짜 출금액
 * summary.totalPnlKrw       : 누적 실현 손익
 */
function updateBriefingBoard(summary) {
    const principalEl = document.getElementById("briefing-principal");
    const currentEl = document.getElementById("briefing-current");
    const rateEl = document.getElementById("briefing-rate");
    const depositEl = document.getElementById("briefing-total-deposit");
    const withdrawEl = document.getElementById("briefing-total-withdraw");
    const dayDepositEl = document.getElementById("briefing-day-deposit");
    const dayWithdrawEl = document.getElementById("briefing-day-withdraw");
    const totalPnlEl = document.getElementById("briefing-total-pnl");

    if (!principalEl || !currentEl || !rateEl) {
        return;
    }

    // summary 없으면 전부 0으로 초기화
    if (!summary) {
        principalEl.textContent = "0원";
        currentEl.textContent = "0원";
        rateEl.textContent = "0%";
        if (depositEl) depositEl.textContent = "0원";
        if (withdrawEl) withdrawEl.textContent = "0원";
        if (dayDepositEl) dayDepositEl.textContent = "0원";
        if (dayWithdrawEl) dayWithdrawEl.textContent = "0원";
        if (totalPnlEl) totalPnlEl.textContent = "0원";

        currentEl.classList.remove("trade-pnl-positive", "trade-pnl-negative");
        if (totalPnlEl) {
            totalPnlEl.classList.remove("trade-pnl-positive", "trade-pnl-negative");
        }
        return;
    }

    // 숫자 추출 (BigDecimal이 문자열로 와도 처리 가능하게 변환)
    const principal =
        summary.principalKrw != null ? Number(summary.principalKrw) : 0;
    const current =
        summary.currentKrw != null ? Number(summary.currentKrw) : 0;
    const totalDeposit =
        summary.totalDepositKrw != null ? Number(summary.totalDepositKrw) : 0;
    const totalWithdraw =
        summary.totalWithdrawKrw != null ? Number(summary.totalWithdrawKrw) : 0;

    const dayDeposit =
        summary.dayDepositKrw != null ? Number(summary.dayDepositKrw) : 0;
    const dayWithdraw =
        summary.dayWithdrawKrw != null ? Number(summary.dayWithdrawKrw) : 0;

    const totalPnl =
        summary.totalPnlKrw != null ? Number(summary.totalPnlKrw) : (current - principal);

    // 1) 원금/현재 금액 표시
    principalEl.textContent = formatNumberKR(principal) + "원";
    currentEl.textContent = formatNumberKR(current) + "원";

    // 2) 누적 수익률 (원금이 0이면 0%)
    let rate = 0;
    currentEl.classList.remove("trade-pnl-positive", "trade-pnl-negative");

    if (principal !== 0) {
        const diff = current - principal;
        rate = (diff / principal) * 100;
        const sign = rate > 0 ? "+" : "";
        rateEl.textContent = sign + rate.toFixed(2) + "%";

        if (diff > 0) {
            currentEl.classList.add("trade-pnl-positive");
        } else if (diff < 0) {
            currentEl.classList.add("trade-pnl-negative");
        }
    } else {
        rateEl.textContent = "0%";
    }

    // 3) 총 입금/출금
    if (depositEl) {
        depositEl.textContent = formatNumberKR(totalDeposit) + "원";
    }
    if (withdrawEl) {
        withdrawEl.textContent = formatNumberKR(totalWithdraw) + "원";
    }

    // 3-1) 선택 날짜 입금/출금
    if (dayDepositEl) {
        dayDepositEl.textContent = formatNumberKR(dayDeposit) + "원";
    }
    if (dayWithdrawEl) {
        dayWithdrawEl.textContent = formatNumberKR(dayWithdraw) + "원";
    }

    // 4) 누적 실현 손익 (색상도 손익에 따라)
    if (totalPnlEl) {
        const signPnl = totalPnl > 0 ? "+" : "";
        totalPnlEl.textContent = signPnl + formatNumberKR(totalPnl) + "원";

        totalPnlEl.classList.remove("trade-pnl-positive", "trade-pnl-negative");
        if (totalPnl > 0) {
            totalPnlEl.classList.add("trade-pnl-positive");
        } else if (totalPnl < 0) {
            totalPnlEl.classList.add("trade-pnl-negative");
        }
    }
}

/**
 * 수익률 차트 초기화
 * - labels: x축 라벨 배열 (예: ["12-01","12-02",...])
 * - rates:  누적 수익률 값 배열 (number, % 단위)
 */
function initProfitChart(labels, rates) {
    const ctx = document.getElementById("profitChart");
    if (!ctx) return;

    // 기존 차트가 있으면 파괴 후 재생성
    if (profitChartInstance) {
        profitChartInstance.destroy();
    }

    profitChartInstance = new Chart(ctx, {
        type: "line",
        data: {
            labels: labels,
            datasets: [
                {
                    label: "누적 수익률(%)",
                    data: rates,
                    borderColor: "#AD0100",
                    backgroundColor: "rgba(173, 1, 0, 0.25)",
                    tension: 0.3,
                    pointRadius: 3,
                    pointHoverRadius: 4,
                    fill: true,
                },
            ],
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            const y = context.parsed.y || 0;
                            const sign = y > 0 ? "+" : "";
                            return sign + y.toFixed(2) + "%";
                        },
                    },
                },
            },
            scales: {
                x: {
                    ticks: {
                        color: "#9E9EA2",
                    },
                    grid: {
                        display: false,
                    },
                },
                y: {
                    ticks: {
                        color: "#9E9EA2",
                        callback: function (value) {
                            return value + "%";
                        },
                    },
                    grid: {
                        color: "rgba(158,158,162,0.25)",
                    },
                },
            },
        },
    });

    // 하단 요약 텍스트에 "최종 누적 수익률" 표시
    const summaryEl = document.getElementById("profitSummary");
    if (summaryEl && rates && rates.length > 0) {
        const lastRate = rates[rates.length - 1] || 0;
        const sign = lastRate > 0 ? "+" : "";
        summaryEl.textContent = sign + lastRate.toFixed(2) + "%";

        summaryEl.classList.remove("trade-pnl-positive", "trade-pnl-negative");
        if (lastRate > 0) {
            summaryEl.classList.add("trade-pnl-positive");
        } else if (lastRate < 0) {
            summaryEl.classList.add("trade-pnl-negative");
        }
    }
}

/**
 * Summary API 호출
 * GET /api/summary 또는 /api/summary?date=YYYY-MM-DD
 */
async function fetchSummary(targetDate) {
    try {
        let url = "/api/summary";
        if (targetDate) {
            const params = new URLSearchParams();
            params.set("date", targetDate);
            url += "?" + params.toString();
        }

        const res = await fetch(url, {
            method: "GET",
            headers: {
                Accept: "application/json",
                ...authHeader(),
            },
        });

        if (res.status === 401 || res.status === 403) {
            window.location.href = "/index.html";
            return null;
        }

        if (!res.ok) {
            console.error("요약 정보 조회 실패", res.status);
            return null;
        }

        return await res.json();
    } catch (e) {
        console.error("요약 정보 조회 중 오류", e);
        return null;
    }
}

/**
 * 일별 통계 API 호출
 * GET /api/stats/daily?days=7[&endDate=YYYY-MM-DD]
 */
async function fetchDailyStats(days, endDate) {
    const d = days || 7;
    try {
        let url = `/api/stats/daily?days=${encodeURIComponent(d)}`;
        if (endDate) {
            url += `&endDate=${encodeURIComponent(endDate)}`;
        }

        const res = await fetch(url, {
            method: "GET",
            headers: {
                Accept: "application/json",
                ...authHeader(),
            },
        });

        if (res.status === 401 || res.status === 403) {
            window.location.href = "/index.html";
            return [];
        }

        if (!res.ok) {
            console.error("일별 통계 조회 실패", res.status);
            return [];
        }

        return await res.json();
    } catch (e) {
        console.error("일별 통계 조회 중 오류", e);
        return [];
    }
}

/**
 * 선택 날짜 기준으로
 * - Summary / DailyStats 를 다시 불러와
 *   브리핑보드 / 수익률 그래프를 갱신한다.
 */
async function updateAnalyticsForDate(dateStr) {
    // 1) 요약 정보 → 브리핑보드
    const summary = await fetchSummary(dateStr);
    if (summary) {
        updateBriefingBoard(summary);
    } else {
        updateBriefingBoard(null);
    }

    // 2) 일별 통계 → 수익률 그래프
    const stats = await fetchDailyStats(7, dateStr);
    if (!stats || stats.length === 0) {
        initProfitChart([], []);
        return;
    }

    const labels = [];
    const rates = [];

    stats.forEach((item) => {
        const dateStrVal = item.tradeDate;
        if (!dateStrVal) return;

        // "YYYY-MM-DD" → "MM-DD"
        const label = String(dateStrVal).substring(5);
        labels.push(label);

        const rateVal =
            item.cumulativePnlRate != null ? Number(item.cumulativePnlRate) : 0;
        rates.push(rateVal);
    });

    initProfitChart(labels, rates);
}

/**
 * 대시보드 분석 영역 초기화
 * - 처음 로딩 시: 오늘 날짜 기준으로 Summary / DailyStats 조회
 */
async function initDashboardAnalytics() {
    let baseDate = currentTradeDate;
    if (!baseDate) {
        const today = new Date();
        baseDate = formatDateYmd(today);
    }
    await updateAnalyticsForDate(baseDate);
}
