package com.kh.coupang.config;

import com.kh.coupang.domain.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT 인증 필터 : security는 filter를 통해 걸르고 들어감
@Component @Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 클라이언트에서 보낸 토큰을 받아서 사용자 확인 후 인증 처리
        String token = parseBearerToken(request);
        log.info("token : " + token);
        if(token != null && !token.equalsIgnoreCase("null")){
            // 토큰이 맞는지 체크해서 로그인 유저 정보 추출
            Member user = tokenProvider.validateGetUser(token);
            log.info("user" + user);

            // 추출된 인증 정보를 필터링에 사용할 수 있도록 context에 등록
            AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 인증 관련 시큐리티가 인정하도록
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
        }
        // JWT 토큰 필터 생성! -> securityConfig로
        filterChain.doFilter(request, response);
    }

    // 요청한 뒤 내가 원하는 부분만 추출
    // 추출하는 메서드 생성
    private String parseBearerToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        // 항상 토큰이 있는 경우가 아니기 때문에 (회원가입, 로그인 같은 경우)
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")){
            // 'Bearer ' 이후로의 값들만 가져오기 위함
            return bearerToken.substring(7);
        }
        return null;
    }
}
