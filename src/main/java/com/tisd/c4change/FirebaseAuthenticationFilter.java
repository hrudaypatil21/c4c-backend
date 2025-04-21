package com.tisd.c4change;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
    private final FirebaseAuth firebaseAuth;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Add this debug logging at the very start of the method
        logger.debug("Processing request to: {} {}", request.getMethod(), request.getRequestURI());
        logger.debug("Request headers: ");
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                logger.debug("{}: {}", headerName, request.getHeader(headerName)));

        // Skip authentication for public endpoints
        if (shouldSkipAuthentication(request)) {
            logger.debug("Skipping authentication for this request");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header must be provided");
            return;
        }

        String token = authHeader.substring(7);
        logger.debug("Token received (truncated): {}...", token.substring(0, Math.min(10, token.length())));

        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            logger.info("Authenticated user: {}", decodedToken.getUid());

            // Set the principal as just the UID string
            // In FirebaseAuthenticationFilter
            Authentication authentication = new FirebaseAuthenticationToken(
                    decodedToken.getUid(), // Principal is the UID string
                    decodedToken
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            logger.error("Firebase authentication failed", e);
            logger.debug("Request to {} requires auth: {}", request.getRequestURI(),
                    !shouldSkipAuthentication(request));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Firebase token");
        }
    }

    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/login")
                || path.startsWith("/api/register")
                || path.equals("/api/test/public")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}