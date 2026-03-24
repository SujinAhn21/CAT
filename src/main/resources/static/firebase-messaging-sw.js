// src/main/resources/static/firebase-messaging-sw.js
// ----------------------------------------------------
// Firebase Cloud Messaging용 서비스 워커 파일
// - 브라우저가 백그라운드에서 푸시를 받을 때 사용
// - /firebase-messaging-sw.js 경로로 서빙되어야 함
// ----------------------------------------------------

// Firebase v9+ 호환(compat) 라이브러리 로드
// 서비스워커에서는 ES module import를 쓰기 힘들어서,
// 공식 문서에서도 importScripts + compat 버전을 사용한다.
importScripts("https://www.gstatic.com/firebasejs/12.6.0/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/12.6.0/firebase-messaging-compat.js");

// 현재 Firebase 프로젝트 설정 (웹 설정에서 복사한 값 사용)
const firebaseConfig = {
  apiKey: "AIzaSyDr4k8zOn5L-DqEj4iErRtismlTn617pIo",
  authDomain: "coin-auto-trader-29808.firebaseapp.com",
  projectId: "coin-auto-trader-29808",
  storageBucket: "coin-auto-trader-29808.firebasestorage.app",
  messagingSenderId: "734558765534",
  appId: "1:734558765534:web:becdcf8bb5140e33791d4f"
};

// Firebase 초기화
firebase.initializeApp(firebaseConfig);

// FCM Messaging 인스턴스
const messaging = firebase.messaging();

// ----------------------------------------------------
// 백그라운드 메시지 수신 핸들러
// - 브라우저가 열려 있지 않거나 백그라운드에 있을 때
//   푸시를 받으면 이 콜백이 호출된다.
// ----------------------------------------------------
messaging.onBackgroundMessage(function (payload) {
  console.log("[firebase-messaging-sw.js] 백그라운드 메시지 수신:", payload);

  // payload.notification 이 있으면 우선 사용,
  // 없으면 data.title / data.body 사용
  const notificationTitle =
    (payload.notification && payload.notification.title) ||
    (payload.data && payload.data.title) ||
    "Coin Auto Trader";

  const notificationBody =
    (payload.notification && payload.notification.body) ||
    (payload.data && payload.data.body) ||
    "";

  const notificationOptions = {
    body: notificationBody,
    // 아이콘 경로는 있으면 사용, 없으면 생략해도 동작한다.
    // 프로젝트에 맞게 수정 가능함.
    icon: "/img/favicon-192.png"
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});

// ----------------------------------------------------
// 알림 클릭 시 대시보드로 포커스/열기
// ----------------------------------------------------
self.addEventListener("notificationclick", function (event) {
  event.notification.close();

  event.waitUntil(
    clients
      .matchAll({ type: "window", includeUncontrolled: true })
      .then((clientList) => {
        for (const client of clientList) {
          if (client.url.includes("/dashboard.html") && "focus" in client) {
            return client.focus();
          }
        }
        if (clients.openWindow) {
          return clients.openWindow("/dashboard.html");
        }
      })
  );
});
