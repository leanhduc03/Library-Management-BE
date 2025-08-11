package com.spring.LibraryManagement.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.LibraryManagement.Exception.JwtAuthenticationException;
import com.spring.LibraryManagement.Service.JwtService;
import com.spring.LibraryManagement.Service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader);
        final String jwt;
        final String username;

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.info("No JWT token found in header");
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);
            log.info("Extracted JWT: {}", jwt);
            username = jwtService.extractUsernameFromToken(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtService.validateTokenForUser(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authenticated user: {}", username);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("JWT token đã hết hạn", e);
            handleError(response, HttpStatus.UNAUTHORIZED, "Token đã hết hạn. Vui lòng đăng nhập lại hoặc làm mới token.");
        } catch (JwtAuthenticationException | JwtException e) {
            log.error("JWT token không hợp lệ", e);
            handleError(response, HttpStatus.UNAUTHORIZED, "Token không hợp lệ. Vui lòng đăng nhập lại.");
        } catch (Exception e) {
            log.error("Lỗi xác thực", e);
            handleError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống xác thực.");
        }
    }

    private void handleError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", message);
        errorDetails.put("status", status.value());

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
