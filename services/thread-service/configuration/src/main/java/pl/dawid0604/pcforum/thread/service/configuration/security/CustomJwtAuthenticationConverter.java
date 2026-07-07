package pl.dawid0604.pcforum.thread.service.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * <p>Custom authentication converter to convert {@link Jwt} to {@link JwtAuthenticationToken}.</p>
 */
@RequiredArgsConstructor(access = PACKAGE)
class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * <p>
     *     Converter which convert {@link Jwt} roles to Spring Security Roles.
     * </p>
     * @see CustomKeycloakRoleConverter
     */
    private final Converter<Jwt, List<GrantedAuthority>> converter;

    /**
     * <p>
     *     Claim which contains current user username.
     * </p>
     */
    private static final String PRINCIPAL_CLAIM_NAME = "sub";

    /**
     * <p>
     *     Method to convert {@link Jwt} to {@link JwtAuthenticationToken} object.
     * </p>
     * @param jwt the source object to convert, which cannot be null.
     * @throws IllegalArgumentException when Jwt token claim {@link #PRINCIPAL_CLAIM_NAME} is missing or empty
     * @return {@link JwtAuthenticationToken} object with name claim and authorities.
     */
    @Override
    public AbstractAuthenticationToken convert(@NonNull final Jwt jwt) {
        final String principalClaimValue = jwt.getClaimAsString(PRINCIPAL_CLAIM_NAME);

        if (isBlank(principalClaimValue)) {
            throw new IllegalArgumentException("Principal claim '" + PRINCIPAL_CLAIM_NAME + "' is missing or empty");
        }

        final List<GrantedAuthority> authorities = this.converter.convert(jwt);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }
}
