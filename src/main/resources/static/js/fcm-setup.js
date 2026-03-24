// src/main/resources/static/js/fcm-setup.js
// 웹 브라우저 FCM 설정 + 디바이스 토큰 등록 + 포그라운드 알림 표시

import {
    initializeApp,
    getApps,
    getApp,
} from "https://www.gstatic.com/firebasejs/12.6.0/firebase-app.js";

import {
    getMessaging,
    getToken,
    onMessage,
} from "https://www.gstatic.com/firebasejs/12.6.0/firebase-messaging.js";

// Firebase 웹 앱 설정 (파이어베이스 콘솔에서 복사해온 값)
const firebaseConfig = {
    apiKey: "AIzaSyDr4k8zOn5L-DqEj4iErRtismlTn617pIo",
    authDomain: "coin-auto-trader-29808.firebaseapp.com",
    projectId: "coin-auto-trader-29808",
    storageBucket: "coin-auto-trader-29808.firebasestorage.app",
    messagingSenderId: "734558765534",
    appId: "1:734558765534:web:becdcf8bb5140e33791d4f",
};

// Firebase 웹 푸시 인증서 키 (VAPID public key)
const VAPID_KEY =
    "BHMN9LdKMlfr4-GNK7mg55g00v_73gyRbCrUYzmqr3GCGDnDVQ7vry8TiXP7YVqJu4vShLGp968Evn03KIIl7gI";

// JWT는 이제 sessionStorage에만 저장한다고 가정
function getJwtToken() {
    return sessionStorage.getItem("coin_jwt_token");
}

// FCM 토큰은 로그인 세션과 직접 상관없으니 localStorage 그대로 사용
function getSavedFcmToken() {
    return localStorage.getItem("coin_fcm_token");
}

function saveFcmToken(token) {
    localStorage.setItem("coin_fcm_token", token);
}

// Firebase App 인스턴스 얻기
function getFirebaseApp() {
    if (!getApps().length) {
        return initializeApp(firebaseConfig);
    }
    return getApp();
}

// 브라우저에서 FCM 토큰 발급 후 서버에 등록
async function registerWebPushToken() {
    if (typeof window === "undefined" || !("Notification" in window)) {
        console.warn("[FCM] 이 브라우저에서는 Notification API를 지원하지 않습니다.");
        return;
    }

    if (Notification.permission === "denied") {
        console.warn("[FCM] 알림 권한이 거부되어 FCM 토큰을 등록할 수 없습니다.");
        return;
    }

    if (Notification.permission === "default") {
        try {
            const permission = await Notification.requestPermission();
            if (permission !== "granted") {
                console.warn("[FCM] 사용자가 알림 권한을 허용하지 않았습니다.");
                return;
            }
        } catch (e) {
            console.error("[FCM] 알림 권한 요청 중 오류:", e);
            return;
        }
    }

    const jwt = getJwtToken();
    if (!jwt) {
        console.warn("[FCM] JWT 토큰이 없어 디바이스 토큰을 서버에 등록할 수 없습니다.");
        return;
    }

    try {
        const app = getFirebaseApp();
        const messaging = getMessaging(app);

        const currentToken = await getToken(messaging, {
            vapidKey: VAPID_KEY,
        });

        if (!currentToken) {
            console.warn("[FCM] FCM 토큰을 가져오지 못했습니다.");
            return;
        }

        const savedToken = getSavedFcmToken();
        if (savedToken && savedToken === currentToken) {
            console.log("[FCM] 이미 등록된 FCM 토큰입니다. 재등록 생략.");
            return;
        }

        const res = await fetch("/api/notifications/device-token", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + jwt,
            },
            body: JSON.stringify({
                fcmToken: currentToken,
                platform: "WEB",
            }),
        });

        if (!res.ok) {
            console.error("[FCM] 디바이스 토큰 등록 실패. status=", res.status);
            return;
        }

        console.log("[FCM] 디바이스 토큰 등록 성공:", currentToken);
        saveFcmToken(currentToken);
    } catch (e) {
        console.error("[FCM] 디바이스 토큰 등록 중 오류:", e);
    }
}

// 포그라운드(대시보드 탭 열려 있을 때) 수신 처리
function setupForegroundMessageHandler() {
    try {
        const app = getFirebaseApp();
        const messaging = getMessaging(app);

        onMessage(messaging, (payload) => {
            console.log("[FCM] 포그라운드 메시지 수신:", payload);

            const data = payload.data || {};
            const title = data.title || "Coin Auto Trader";
            const body = data.body || "";

            if (Notification.permission === "granted") {
                try {
                    new Notification(title, {
                        body: body,
                        icon: "/img/logo-icon.png",
                    });
                } catch (e) {
                    console.error("[FCM] Notification 표시 중 오류:", e);
                }
            }
        });
    } catch (e) {
        console.error("[FCM] 포그라운드 메시지 핸들러 설정 중 오류:", e);
    }
}

// DOM 로드 후 FCM 초기화
document.addEventListener("DOMContentLoaded", () => {
    const jwt = getJwtToken();
    if (!jwt) {
        console.warn("[FCM] JWT 토큰이 없어 FCM 초기화를 건너뜁니다.");
        return;
    }

    registerWebPushToken().catch((e) => {
        console.error("[FCM] registerWebPushToken() 호출 중 예외:", e);
    });

    setupForegroundMessageHandler();
});
