package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;

/**
 * Implementation of the {@link AuditorAware} interface
 * providing information about the current auditor (e.g., logged user).
 *
 * <p>
 *     The current implementation returns empty response {@link Optional},
 *     which means that information about the auditor is unavailable.
 * </p>
 * @see AuditorAware
 */
@Component("auditorAwareImpl")
@NoArgsConstructor(access = PACKAGE)
class AuditorAwareImpl implements AuditorAware<String> {

    /**
     * Returns the identifier of the current auditor (e.g., username).
     * @return {@link Optional} containing username from the JWT token or
     * authentication principal.
     * @see JwtAuthenticationToken
     * @see Authentication
     */
    @NonNull
    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public Optional<String> getCurrentAuditor() {
        final Authentication authentication = SecurityContextHolder.getContext()
                                                                   .getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return Optional.ofNullable(jwtToken.getToken())
                           .map(t -> t.getClaimAsString("preferred_username"))
                           .filter(StringUtils::isNotBlank);
        }

        return Optional.ofNullable(authentication)
                       .map(Authentication::getPrincipal)
                       .filter(String.class::isInstance)
                       .map(String.class::cast)
                       .filter(a -> !Strings.CI.contains(a, "anonymous"));
    }
}
