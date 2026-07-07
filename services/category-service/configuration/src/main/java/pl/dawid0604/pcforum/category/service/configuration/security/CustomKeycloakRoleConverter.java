package pl.dawid0604.pcforum.category.service.configuration.security;

import lombok.NoArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PACKAGE;

/**
 * <p>
 *     Custom converter to convert Keycloak roles to Spring Security Roles.
 * </p>
 *
 * <p>
 *     Thanks it we can use annotations such as {@link org.springframework.security.access.prepost.PreAuthorize} etc.,
 * </p>
 */
@NoArgsConstructor(access = PACKAGE)
class CustomKeycloakRoleConverter implements Converter<Jwt, List<GrantedAuthority>> {

    /**
     * <p>
     *     Method to convert {@link Jwt} keycloak roles to Spring Security roles.
     * </p>
     *
     * <p>
     *     <strong>Example of converting:</strong>
     *     <ul>
     *         <li>
     *             Keycloak role <b>USER</b> is converted to <b>ROLE_USER</b>
     *         </li>
     *
     *         <li>
     *             Keycloak role <b>ADMIN</b> is converted to <b>ROLE_ADMIN</b>
     *         </li>
     *     </ul>
     * </p>
     * @param jwt the source object to convert, which cannot be null.
     * @return {@link List} containing {@link GrantedAuthority} user authorities.
     */
    @Override
    public List<GrantedAuthority> convert(@NonNull final Jwt jwt) {
        final Map<String, Object> realmAsMap = jwt.getClaimAsMap("realm_access");
        List<GrantedAuthority> result = emptyList();

        if (realmAsMap != null) {
            final Object roles = realmAsMap.get("roles");

            if (roles instanceof List<?> list) {
                result = list.stream()
                             .map(String.class::cast)
                             .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                             .collect(toList());
            }
        }

        return result;
    }
}
