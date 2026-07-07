package pl.dawid0604.pcforum.thread.service.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pl.dawid0604.pcforum.thread.service.commons.Constants;
import pl.dawid0604.pcforum.thread.service.commons.RequestContext;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Order(1)
@Component
@RequiredArgsConstructor(access = PACKAGE)
class RequestContextInterceptor implements HandlerInterceptor {
    private static final String MISSING_IDEMPOTENCY_HEADER_DEFAULT_MESSAGE;
    private static final String INVALID_IDEMPOTENCY_HEADER_DEFAULT_MESSAGE;
    private final RequestContext context;

    static {
        MISSING_IDEMPOTENCY_HEADER_DEFAULT_MESSAGE = """
                    {
                        "error": "%s header is required for POST requests"
                    }
                """.formatted(Constants.IDEMPOTENCY_KEY_HEADER);

        INVALID_IDEMPOTENCY_HEADER_DEFAULT_MESSAGE = """
                    {
                        "error": "Invalid %s header format"
                    }
                """.formatted(Constants.IDEMPOTENCY_KEY_HEADER);
    }

    @Override
    public boolean preHandle(

            @NonNull
            final HttpServletRequest request,

            @NonNull
            final HttpServletResponse response,

            @NonNull
            final Object handler) throws IOException {

        final Authentication authentication = SecurityContextHolder.getContext()
                                                                   .getAuthentication();

        if(isAuthenticated(authentication) && authentication instanceof JwtAuthenticationToken authenticationToken) {
            final String username = authenticationToken.getToken()
                                                       .getClaimAsString("preferred_username");

            context.setUsername(username);
            context.setAuthenticated(true);

            if(!checkIdempotency(request, response)) {
                return false;
            }
        }

        context.setRequestTime(Instant.now());
        return true;
    }

    private boolean checkIdempotency(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String method = request.getMethod();
        boolean success = false;

        if(Strings.CS.equals(method, HttpMethod.POST.name())) {
            final String idempotencyKeyHeaderValue = request.getHeader(Constants.IDEMPOTENCY_KEY_HEADER);

            if(isBlank(idempotencyKeyHeaderValue)) {
               handleMissingIdempotencyKey(response);

            } else if(!isValidIdempotencyKey(idempotencyKeyHeaderValue)) {
                handleInvalidIdempotencyKey(response);

            } else {
                context.setIdempotencyKey(idempotencyKeyHeaderValue);
                success = true;
            }
        }

        return success;
    }

    private void handleMissingIdempotencyKey(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(APPLICATION_JSON);
        response.getWriter()
                .write(MISSING_IDEMPOTENCY_HEADER_DEFAULT_MESSAGE);
    }

    private void handleInvalidIdempotencyKey(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(APPLICATION_JSON);
        response.getWriter()
                .write(INVALID_IDEMPOTENCY_HEADER_DEFAULT_MESSAGE);
    }

    private boolean isValidIdempotencyKey(final String idempotencyKey) {
        try {
            UUID.fromString(idempotencyKey);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isAuthenticated(final Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}
