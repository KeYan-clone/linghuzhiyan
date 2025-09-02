package org.linghu.user.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;

/**
 * 测试专用的JWT配置
 */
@TestConfiguration
public class TestJwtConfig {

    @Bean
    @Primary
    public JwtTokenProvider testJwtTokenProvider() {
        return new TestJwtTokenProvider();
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter testJwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    /**
     * 测试用的JWT Token Provider
     */
    public static class TestJwtTokenProvider extends JwtTokenProvider {
        
        @Override
        public String getUsernameFromToken(String token) {
            return "testuser";
        }

        @Override
        public Boolean validateToken(String token) {
            return true;
        }

        @Override
        public Authentication getAuthentication(String token) {
            return new UsernamePasswordAuthenticationToken(
                "testuser", 
                null, 
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        @Override
        public String getTokenHeader() {
            return "Authorization";
        }

        @Override
        public String getTokenHead() {
            return "Bearer";
        }
    }
}
