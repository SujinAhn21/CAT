// src/main/java/com/example/coin/config/FirebaseConfig.java
// Firebase Admin SDK 초기화 + FirebaseMessaging 빈 제공

package com.example.coin.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    /**
     * FirebaseMessaging Bean
     * - resources/firebase/serviceAccountKey.json 기준
     * - 이미 FirebaseApp 이 초기화 되어 있으면 재사용
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (FirebaseApp.getApps() != null && !FirebaseApp.getApps().isEmpty()) {
            // 이미 초기화된 앱이 있으면 그걸 사용
            FirebaseApp app = FirebaseApp.getApps().get(0);
            return FirebaseMessaging.getInstance(app);
        }

        // classpath:firebase/serviceAccountKey.json 기준
        ClassPathResource resource = new ClassPathResource("firebase/serviceAccountKey.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        return FirebaseMessaging.getInstance(app);
    }
}
