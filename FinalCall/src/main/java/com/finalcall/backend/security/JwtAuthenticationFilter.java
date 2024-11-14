package com.finalcall.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Use a 256-bit key (32 bytes). For example, a random 32 character string:
    public static final String SECRET_KEY = "MySuperSecretJWTKeyMustBe32Bytes!";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        logger.debug("JwtAuthenticationFilter processing request: " + request.getRequestURI());

        String header = request.getHeader("Authorization");
        logger.debug("Authorization header: " + header);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            logger.debug("Extracted token: " + token);
            try {
                var claims = Jwts.parserBuilder()
                                 .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                                 .build()
                                 .parseClaimsJws(token)
                                 .getBody();

                String username = claims.getSubject();
                logger.debug("Parsed username from token: " + username);

                if (username != null) {
                    var authToken = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set for user " + username);
                }
            } catch (Exception e) {
                logger.debug("Invalid token or error parsing token: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("No valid Authorization header found, proceeding without authentication");
        }
        filterChain.doFilter(request, response);
    }
}
