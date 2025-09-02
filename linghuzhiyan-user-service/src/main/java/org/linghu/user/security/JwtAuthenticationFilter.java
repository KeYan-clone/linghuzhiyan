package org.linghu.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String jwtSecret;
    private final String tokenHeader;
    private final String tokenHead;

    public JwtAuthenticationFilter(String jwtSecret, String tokenHeader, String tokenHead) {
        this.jwtSecret = jwtSecret;
        this.tokenHeader = tokenHeader;
        this.tokenHead = tokenHead;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(tokenHeader);
        String prefix = tokenHead + " ";
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(prefix)) {
            String token = authHeader.substring(prefix.length());
            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
                String username = claims.getSubject();
                Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);

                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = User.builder().username(username).password("").authorities(authorities).build();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.warn("JWT 解析失败: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
        List<GrantedAuthority> list = new ArrayList<>();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?>) {
            for (Object r : (List<Object>) rolesClaim) {
                if (r != null) list.add(new SimpleGrantedAuthority(prefixRole(r.toString())));
            }
        } else {
            Object role = claims.get("role");
            if (role != null) list.add(new SimpleGrantedAuthority(prefixRole(role.toString())));
        }
        return list;
    }

    private String prefixRole(String role) {
        String r = role.trim();
        return r.toUpperCase().startsWith("ROLE_") ? r.toUpperCase() : ("ROLE_" + r.toUpperCase());
    }
}
