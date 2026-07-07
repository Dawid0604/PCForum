package pl.dawid0604.pcforum.category.service.configuration.security;

import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomKeycloakRoleConverter Tests")
class CustomKeycloakRoleConverterTest {
    private static final Faker FAKER = new Faker();
    private final CustomKeycloakRoleConverter converter = new CustomKeycloakRoleConverter();

    @RepeatedTest(20)
    @DisplayName("Should successfully convert Jwt")
    void shouldSuccessfullyConvertJwt() {
        // Given
        final String username = FAKER.options()
                                     .option(
                                             FAKER.name().name(),
                                             FAKER.name().lastName(),
                                             FAKER.funnyName().name(),
                                             FAKER.internet().emailAddress()
                                     );

        final UUID sub = UUID.randomUUID();

        final String algorithm = FAKER.options()
                                      .option("HS256", "RS256");

        final List<String> roles = FAKER.options()
                                        .option(
                                                List.of("USER"),
                                                List.of("ADMIN"),
                                                List.of("MODERATOR"),

                                                List.of("USER", "ADMIN"),
                                                List.of("ADMIN", "USER"),
                                                List.of("MODERATOR", "ADMIN", "USER")
                                        );

        final Jwt jwt = Jwt.withTokenValue("anyToken")
                           .header("alg", algorithm)
                           .claim("sub", sub)
                           .claim("preferred_username", username)
                           .claim("realm_access", Map.of("roles", roles))
                           .build();

        // When
        final List<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        Assertions.assertThat(authorities)
                  .isNotNull()
                  .isNotEmpty()
                  .extracting(GrantedAuthority::getAuthority)
                  .asInstanceOf(InstanceOfAssertFactories.LIST)
                  .containsExactlyElementsOf(roles.stream()
                                                  .map(r -> "ROLE_" + r)
                                                  .toList());
    }

    @Test
    @DisplayName("Should return empty list when realm_access claim is missing")
    void shouldReturnEmptyListWhenRealmAccessClaimIsMissing() {
        // Given
        final String username = FAKER.options()
                                     .option(
                                             FAKER.name().name(),
                                             FAKER.name().lastName(),
                                             FAKER.funnyName().name(),
                                             FAKER.internet().emailAddress()
                                     );

        final UUID sub = UUID.randomUUID();

        final String algorithm = FAKER.options()
                                      .option("HS256", "RS256");

        final Jwt jwt = Jwt.withTokenValue("anyToken")
                           .header("alg", algorithm)
                           .claim("sub", sub)
                           .claim("preferred_username", username)
                           .build();

        // When
        final List<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        Assertions.assertThat(authorities)
                  .isNotNull()
                  .isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when roles claim is missing")
    void shouldReturnEmptyListWhenRolesClaimIsMissing() {
        // Given
        final String username = FAKER.options()
                                     .option(
                                             FAKER.name().name(),
                                             FAKER.name().lastName(),
                                             FAKER.funnyName().name(),
                                             FAKER.internet().emailAddress()
                                     );

        final UUID sub = UUID.randomUUID();

        final String algorithm = FAKER.options()
                                      .option("HS256", "RS256");

        final Jwt jwt = Jwt.withTokenValue("anyToken")
                           .header("alg", algorithm)
                           .claim("sub", sub)
                           .claim("preferred_username", username)
                           .claim("realm_access", Map.of("anyKey", "anyValue"))
                           .build();

        // When
        final List<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        Assertions.assertThat(authorities)
                  .isNotNull()
                  .isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when roles claim is not list")
    void shouldReturnEmptyListWhenRolesClaimIsNotList() {
        // Given
        final String username = FAKER.options()
                                     .option(
                                             FAKER.name().name(),
                                             FAKER.name().lastName(),
                                             FAKER.funnyName().name(),
                                             FAKER.internet().emailAddress()
                                     );

        final UUID sub = UUID.randomUUID();

        final String algorithm = FAKER.options()
                                      .option("HS256", "RS256");

        final Jwt jwt = Jwt.withTokenValue("anyToken")
                           .header("alg", algorithm)
                           .claim("sub", sub)
                           .claim("preferred_username", username)
                           .claim("realm_access", Map.of("roles", Set.of()))
                           .build();

        // When
        final List<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        Assertions.assertThat(authorities)
                  .isNotNull()
                  .isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when roles claim is empty list")
    void shouldReturnEmptyListWhenRolesClaimIsEmptyList() {
        // Given
        final String username = FAKER.options()
                                     .option(
                                             FAKER.name().name(),
                                             FAKER.name().lastName(),
                                             FAKER.funnyName().name(),
                                             FAKER.internet().emailAddress()
                                     );

        final UUID sub = UUID.randomUUID();

        final String algorithm = FAKER.options()
                                      .option("HS256", "RS256");

        final Jwt jwt = Jwt.withTokenValue("anyToken")
                           .header("alg", algorithm)
                           .claim("sub", sub)
                           .claim("preferred_username", username)
                           .claim("realm_access", Map.of("roles", List.of()))
                           .build();

        // When
        final List<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        Assertions.assertThat(authorities)
                  .isNotNull()
                  .isEmpty();
    }
}