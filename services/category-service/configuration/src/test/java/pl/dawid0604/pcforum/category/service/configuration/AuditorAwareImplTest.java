package pl.dawid0604.pcforum.category.service.configuration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.core.Response;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.platform.commons.util.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

class AuditorAwareImplTest {
    private static final Faker FAKER = new Faker();

    @Nested
    @DisplayName("Unit tests")
    @ExtendWith(MockitoExtension.class)
    class UnitTests {

        @Mock
        private SecurityContext securityContext;

        @Mock
        private Authentication authentication;

        @Mock
        private JwtAuthenticationToken jwtAuthenticationToken;

        @Mock
        private Jwt jwt;

        @InjectMocks
        private AuditorAwareImpl auditorAware;

        @BeforeEach
        void setUp() {
            SecurityContextHolder.setContext(securityContext);
        }

        @AfterEach
        void tearDown() {
            SecurityContextHolder.clearContext();
        }

        @RepeatedTest(20)
        @DisplayName("Should get current authenticated auditor via Jwt")
        void shouldGetCurrentAuthenticatedAuditorViaJwt() {
            // Given
            final String expectedUsername = FAKER.options()
                                                 .option(
                                                         FAKER.name().name(),
                                                         FAKER.internet().emailAddress(),
                                                         FAKER.name().lastName(),
                                                         FAKER.funnyName().name()
                                                 );

            when(securityContext.getAuthentication())
                    .thenReturn(jwtAuthenticationToken);

            when(jwtAuthenticationToken.getToken())
                    .thenReturn(jwt);

            when(jwt.getClaimAsString("preferred_username"))
                    .thenReturn(expectedUsername);

            // When
            final Optional<String> possibleAuditor = auditorAware.getCurrentAuditor();

            // Then
            Assertions.assertThat(possibleAuditor)
                      .isPresent()
                      .contains(expectedUsername);

            verify(authentication, never()).getPrincipal();
        }

        @RepeatedTest(20)
        @DisplayName("Should get current authenticated auditor via Authentication")
        void shouldGetCurrentAuthenticatedAuditorViaAuthentication() {
            // Given
            final String expectedUsername = FAKER.options()
                                                 .option(
                                                         FAKER.name().name(),
                                                         FAKER.name().lastName(),
                                                         FAKER.funnyName().name()
                                                 );

            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(expectedUsername);

            // When
            final Optional<String> possibleAuditor = auditorAware.getCurrentAuditor();

            // Then
            Assertions.assertThat(possibleAuditor)
                      .isPresent()
                      .contains(expectedUsername);

            verifyNoInteractions(jwt);
        }

        @Test
        @DisplayName("Should return empty when principal is not User")
        void shouldReturnEmptyWhenPrincipalIsNotUser() {
            // Given
            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(AnonymousAuthenticationToken.class);

            // When
            final Optional<String> possibleAuditor = auditorAware.getCurrentAuditor();

            // Then
            Assertions.assertThat(possibleAuditor)
                      .isEmpty();

            verifyNoInteractions(jwt);
        }
    }

    @Nested
    @SpringBootTest(classes = AuditorAwareImpl.class)
    @TestPropertySource(properties = {
            "spring.cloud.config.enabled=false",
            "logging.level.org.springframework.security=DEBUG"
    })
    @DisplayName("Integration tests")
    class IntegrationTests {

        @Autowired
        @SuppressWarnings("unused")
        private AuditorAwareImpl auditorAware;

        @RepeatedTest(20)
        @DisplayName("Should integrate with Spring Security Context")
        void shouldIntegrateWithSpringSecurityContext() {
            // Given
            final String expectedUsername = FAKER.options()
                                                 .option(
                                                         FAKER.name().name(),
                                                         FAKER.name().lastName(),
                                                         FAKER.funnyName().name()
                                                 );

            final Authentication authentication = new UsernamePasswordAuthenticationToken(
                    expectedUsername, FAKER.internet().password()
            );

            SecurityContextHolder.getContext()
                                 .setAuthentication(authentication);

            // When
            final Optional<String> result = auditorAware.getCurrentAuditor();

            // Then
            Assertions.assertThat(result)
                    .isPresent()
                    .contains(expectedUsername);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("Should return empty with Anonymous user context")
        void shouldReturnEmptyWithAnonymousContext() {
            // Given
            // When
            final Optional<String> result = auditorAware.getCurrentAuditor();

            // Then
            Assertions.assertThat(result)
                      .isEmpty();
        }

        @RepeatedTest(20)
        @DisplayName("Should integrate with Jwt Authentication Token")
        void shouldIntegrateWithJwtAuthenticationToken() {
            // Given
            final String expectedUsername = FAKER.options()
                                                 .option(
                                                         FAKER.name().name(),
                                                         FAKER.name().lastName(),
                                                         FAKER.funnyName().name()
                                                 );

            final Jwt jwt = Jwt.withTokenValue("anyToken")
                               .header("alg", "HS256")
                               .claim("sub", "anySub")
                               .claim("preferred_username", expectedUsername)
                               .build();

            final SecurityContext freshContext = SecurityContextHolder.createEmptyContext();
                                  freshContext.setAuthentication(new JwtAuthenticationToken(jwt));

            SecurityContextHolder.setContext(freshContext);

            // When
            final Optional<String> result = auditorAware.getCurrentAuditor();

            // Then
            Assertions.assertThat(result)
                      .isPresent()
                      .get()
                      .isEqualTo(expectedUsername);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should integrate with Jwt Authentication Token")
        void shouldReturnEmptyWhenPreferredUsernameIsMissing(final String preferredUsername) {
            // Given
            final Jwt jwt = Jwt.withTokenValue("anyToken")
                               .header("alg", "HS256")
                               .claim("sub", "anySub")
                               .claim("preferred_username", preferredUsername)
                               .build();

            final SecurityContext freshContext = SecurityContextHolder.createEmptyContext();
                                  freshContext.setAuthentication(new JwtAuthenticationToken(jwt));

            SecurityContextHolder.setContext(freshContext);

            // When
            final Optional<String> result = auditorAware.getCurrentAuditor();

            // Then
            Assertions.assertThat(result)
                      .isEmpty();
        }

        @Nested
        @Testcontainers
        @DisplayName("Keycloak tests")
        class KeycloakTests {

            @Container
            static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.2")
                                                             .withRealmImportFile("test-realm.json")
                                                             .withEnv("KEYCLOAK_ADMIN", "admin")
                                                             .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                                                             .waitingFor(
                                                                     Wait.forHttp("/realms/master")
                                                                             .forPort(8080)
                                                                             .forStatusCode(200)
                                                                             .withStartupTimeout(Duration.ofSeconds(180))
                                                             );

            @Autowired
            @SuppressWarnings("unused")
            private AuditorAwareImpl auditorAware;

            private Keycloak keycloakClient;
            private static final String REALM = "test-realm";
            private static final String CLIENT_ID = "test-client";
            private static final String CLIENT_SECRET = "test-secret";
            private static final String USERNAME = FAKER.internet().emailAddress();
            private static final String PASSWORD = FAKER.internet().password();

            @BeforeEach
            void setUp() {
                keycloakClient = KeycloakBuilder.builder()
                                                .serverUrl(KEYCLOAK.getAuthServerUrl())
                                                .realm("master")
                                                .clientId("admin-cli")
                                                .username(KEYCLOAK.getAdminUsername())
                                                .password(KEYCLOAK.getAdminPassword())
                                                .build();

                createUsers();
            }

            @AfterEach
            void tearDown() {
                deleteUsers();

                if(keycloakClient != null) {
                    keycloakClient.close();
                }
            }

            @Test
            @DisplayName("Should integrate with realistic Keycloak scenario")
            void shouldIntegrateWithRealisticKeycloak() {
                // Given
                final String accessToken = getTokenFromKeycloak();
                final Jwt jwt = parseAndValidateJwt(accessToken);

                final JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);
                final SecurityContext freshContext = SecurityContextHolder.createEmptyContext();
                                      freshContext.setAuthentication(token);

                SecurityContextHolder.setContext(freshContext);

                // When
                final Optional<String> result = auditorAware.getCurrentAuditor();

                // Then
                Assertions.assertThat(result)
                          .isPresent()
                          .contains(USERNAME);

                Assertions.assertThat(jwt.getClaimAsString("iss"))
                          .contains(KEYCLOAK.getAuthServerUrl());

                Assertions.assertThat(jwt.getClaimAsString("preferred_username"))
                          .contains(USERNAME);
            }

            private void createUsers() {
                final RealmResource realmResource = keycloakClient.realm(REALM);
                final UsersResource usersResource = realmResource.users();
                final List<UserRepresentation> existingUser = usersResource.search(USERNAME);

                if(!existingUser.isEmpty()) {
                    return;
                }

                final UserRepresentation user = new UserRepresentation();
                                         user.setUsername(USERNAME);
                                         user.setEmail(USERNAME);
                                         user.setFirstName(FAKER.name().firstName());
                                         user.setLastName(FAKER.name().lastName());
                                         user.setEnabled(true);

                final Response response = usersResource.create(user);

                if(response.getStatus() != 201) {
                    throw new RuntimeException("Failed creation");
                }

                final String userId = extractUserIdFromResponse(response);
                final CredentialRepresentation credential = new CredentialRepresentation();
                                               credential.setType(CredentialRepresentation.PASSWORD);
                                               credential.setValue(PASSWORD);
                                               credential.setTemporary(false);

                usersResource.get(userId).resetPassword(credential);
            }

            private void deleteUsers() {
                final RealmResource realmResource = keycloakClient.realm(REALM);
                final UsersResource usersResource = realmResource.users();

                for(final UserRepresentation user: usersResource.search(USERNAME)) {
                    usersResource.get(user.getId())
                                 .remove();
                }
            }

            private String extractUserIdFromResponse(final Response response) {
                return Optional.ofNullable(response)
                               .map(r -> r.getHeaderString("Location"))
                               .filter(StringUtils::isNotBlank)
                               .map(v -> v.split("/"))
                               .map(sv -> sv[sv.length - 1])
                               .orElseThrow(() -> new RuntimeException("Location header is not present"));
            }

            private String getTokenFromKeycloak() {
                final String tokenUrl = KEYCLOAK.getAuthServerUrl() + "/realms/" + REALM + "/protocol/openid-connect/token";
                final RestTemplate restTemplate = new RestTemplate();
                final HttpHeaders httpHeaders = new HttpHeaders();
                                  httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                final HttpEntity<MultiValueMap<String, String>> request = getMultiValueMapHttpEntity(httpHeaders);

                @SuppressWarnings("rawtypes")
                final ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

                if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return (String) response.getBody().get("access_token");
                }

                throw new RuntimeException("Failed to obtain token from Keycloak");
            }

            private HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(final HttpHeaders httpHeaders) {
                final MultiValueMap<String, String> body = new LinkedMultiValueMap<>(5);
                                                    body.add("grant_type", "password");
                                                    body.add("client_id", CLIENT_ID);
                                                    body.add("client_secret", CLIENT_SECRET);
                                                    body.add("username", USERNAME);
                                                    body.add("password", PASSWORD);

                return new HttpEntity<>(body, httpHeaders);
            }

            private Jwt parseAndValidateJwt(final String token) {
                final String jwtSetUri = KEYCLOAK.getAuthServerUrl() + "/realms/" + REALM + "/protocol/openid-connect/certs";
                final NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwtSetUri)
                                                                    .jwsAlgorithm(SignatureAlgorithm.RS256)
                                                                    .build();

                final List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
                                                      validators.add(new JwtTimestampValidator());
                                                      validators.add(new JwtIssuerValidator(KEYCLOAK.getAuthServerUrl() + "/realms/" + REALM));

                final OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(validators);

                jwtDecoder.setJwtValidator(validator);
                return jwtDecoder.decode(token);
            }
        }
    }
}
