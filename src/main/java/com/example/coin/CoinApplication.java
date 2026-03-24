// src/main/java/com/example/coin/CoinApplication.java
// .env 읽어서 DB_USERNAME/DB_PASSWORD/KAKAO_* 세팅하고 스프링 부트 구동.

package com.example.coin;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // JPA Auditing 켜기
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJpaAuditing // JPA Auditing 켜기
public class CoinApplication {

	public static void main(String[] args) {

		// 1) .env 로드 (.env 없으면 무시)
		Dotenv dotenv = Dotenv.configure()
				.directory(System.getProperty("user.dir"))
				.filename(".env")
				.ignoreIfMissing()
				.load(); // 더 정확하게 .env 디렉토리 명시.

		// 2) .env -> 시스템 환경변수 -> 기본값 순으로 선택
		String dbUser          = pick(dotenv.get("DB_USERNAME"), System.getenv("DB_USERNAME"), "");
		String dbPass          = pick(dotenv.get("DB_PASSWORD"), System.getenv("DB_PASSWORD"), "");
		String kakaoClientId   = pick(dotenv.get("KAKAO_CLIENT_ID"), System.getenv("KAKAO_CLIENT_ID"), "");
		String kakaoClientSecret = pick(dotenv.get("KAKAO_CLIENT_SECRET"), System.getenv("KAKAO_CLIENT_SECRET"), "");
		String jwtSecret = pick(dotenv.get("JWT_SECRET"), System.getenv("JWT_SECRET"), "");
		if (!jwtSecret.isBlank()) {
            System.setProperty("JWT_SECRET", jwtSecret);
        }
		String port            = pick(dotenv.get("SERVER_PORT"), System.getenv("SERVER_PORT"), "8080");

		// 인원 추가를 위한 테스트의 경우 .env같은거 건드릴 필요 없이 이 코드만 주석처리 하면 됨
		// 화이트리스트용 카카오 ID 목록
		String allowedKakaoIds   = pick(dotenv.get("ALLOWED_KAKAO_IDS"),
				System.getenv("ALLOWED_KAKAO_IDS"),
				"");

		// 3) Spring에서 ${...}로 참조할 수 있도록 System Property에 주입
		if (!dbUser.isBlank())            System.setProperty("DB_USERNAME", dbUser);
		if (!dbPass.isBlank())            System.setProperty("DB_PASSWORD", dbPass);
		if (!kakaoClientId.isBlank())     System.setProperty("KAKAO_CLIENT_ID", kakaoClientId);
		if (!kakaoClientSecret.isBlank()) System.setProperty("KAKAO_CLIENT_SECRET", kakaoClientSecret);
		if (!jwtSecret.isBlank())       System.setProperty("JWT_SECRET", jwtSecret);
		if (!port.isBlank())              System.setProperty("SERVER_PORT", port);

		// 인원 추가를 위한 테스트의 경우 .env같은거 건드릴 필요 없이 이 코드만 주석처리 하면 됨
		// .env 의 ALLOWED_KAKAO_IDS 를 프로퍼티로 올려주기
		// application.yml 에서 ${ALLOWED_KAKAO_IDS:} 로 그대로 참조하므로..
		if (!allowedKakaoIds.isBlank()) {
			System.setProperty("ALLOWED_KAKAO_IDS", allowedKakaoIds);
		}

		SpringApplication.run(CoinApplication.class, args);
	}

	private static String pick(String a, String b, String fallback) {
		if (a != null && !a.isBlank()) return a;
		if (b != null && !b.isBlank()) return b;
		return fallback;
	}
}
