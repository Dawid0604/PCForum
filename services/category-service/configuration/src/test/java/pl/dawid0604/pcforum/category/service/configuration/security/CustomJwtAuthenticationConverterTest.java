package pl.dawid0604.pcforum.category.service.configuration.security;

import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomJwtAuthenticationConverter Tests")
class CustomJwtAuthenticationConverterTest {

    private static final Faker FAKER = new Faker();

    @Mock
    private Converter<Jwt, List<GrantedAuthority>> converter;

    @InjectMocks
    private CustomJwtAuthenticationConverter customConverterImpl;

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

        final List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_MODERATOR")
        );

        final Jwt jwt = Jwt.withTokenValue("anyToken")
                           .header("alg", algorithm)
                           .claim("sub", sub)
                           .claim("preferred_username", username)
                           .build();

        when(converter.convert(jwt))
                .thenReturn(authorities);

        // When
        final AbstractAuthenticationToken token = customConverterImpl.convert(jwt);

        // Then
        Assertions.assertThat(token)
                  .isNotNull()
                  .isInstanceOf(JwtAuthenticationToken.class);

        Assertions.assertThat(token)
                  .extracting(AbstractAuthenticationToken::getAuthorities)
                  .isEqualTo(authorities);

        Assertions.assertThat(token)
                  .extracting(AbstractAuthenticationToken::getName)
                  .isEqualTo(sub.toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when sub claim is invalid")
    void shouldThrowExceptionWhenSubClaimIsInvalid(final String sub) {
        // Given
        final Jwt jwt = Jwt.withTokenValue("anyToken")
                           .header("alg", "RS256")
                           .claim("sub", sub)
                           .build();

        // When
        // Then
        Assertions.assertThatThrownBy(() -> customConverterImpl.convert(jwt))
                  .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(converter);
    }
}