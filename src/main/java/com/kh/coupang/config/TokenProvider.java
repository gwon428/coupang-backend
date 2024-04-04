package com.kh.coupang.config;

import com.kh.coupang.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class TokenProvider {
    // 시크릿 키 발급
    private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String create(Member user){
        // String으로 전환할 때 compact()
        // 2. jwt 토큰 발급!
        return Jwts.builder()
                // 서명?
                .signWith(secretKey)
                // id 하나만 담을 때
//                .setSubject(user.getId())
                // 여러 개 담을 때 -> map 형식으로 들어감
                .setClaims(Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                        ))
                // 토큰 시간 저장
                .setIssuedAt(new Date())
                // 지금으로부터 1일 정도만 저장되는 상태
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .compact();
    }
    // 검증 절차 -> User 정보 가져오기
    public Member validateGetUser(String token){
        // builder와 반대 같은 parser
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token).getBody();

        return Member.builder()
                .id((String) claims.get("id"))
                .name((String) claims.get("name"))
                .email((String) claims.get("email"))
                .role((String) claims.get("role"))
                .build();
    }

}
