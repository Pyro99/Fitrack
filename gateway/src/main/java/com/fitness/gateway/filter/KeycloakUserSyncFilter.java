package com.fitness.gateway.filter;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        RegisterRequest registerRequest = getUserDetails(token);

        if (registerRequest == null) {
            log.error("Unable to parse user details from token.");
            return chain.filter(exchange); // Skip processing if token details are invalid
        }

        if (userId == null) {
            userId = registerRequest.getKeycloakId();
        }

        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId).flatMap(exists -> {
                if (!exists) {
                    // If the user does not exist in the database, register them
                    return userService.registerUser(registerRequest);
                }
                return Mono.empty();
            }).onErrorResume(e -> {
                log.error("Exception during user sync: {}", e.getMessage());
                return Mono.empty();
            }).then(Mono.defer(() -> {
                // Add the user to the request headers only after all processing
                ServerHttpRequest mutatedRequest = exchange.getRequest()
                        .mutate()
                        .header("X-User-ID", finalUserId)
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }));
        }
        return chain.filter(exchange);
    }

    private RegisterRequest getUserDetails(String token) {
        try {
            String authToken = token.replace("Bearer ", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(authToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claims.getStringClaim("email"));
            registerRequest.setKeycloakId(claims.getStringClaim("sub"));
            registerRequest.setPassword("password");
            registerRequest.setFirstName(claims.getStringClaim("given_name"));
            registerRequest.setLastName(claims.getStringClaim("family_name"));

            return registerRequest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}