package pl.dawid0604.pcforum.category.service.configuration.security;

import io.github.bucket4j.Bucket;
import net.datafaker.Faker;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dawid0604.pcforum.category.service.commons.exception.RateLimitException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitInterceptor Tests")
class RateLimitInterceptorTest {
    private static final Faker FAKER = new Faker();
    private final RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor();

    @Nested
    @DisplayName("PreHandle Tests")
    class PreHandleTests {

        @Test
        @DisplayName("Should use authenticated user for Rate Limit")
        void shouldUseAuthenticatedUserForRateLimit() {
            try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
                // Given
                final String username = FAKER.internet().emailAddress();
                final MockHttpServletRequest request = new MockHttpServletRequest();
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
                final SecurityContext securityContext = mock(SecurityContext.class);
                final Jwt jwt = mock(Jwt.class);

                mockedContext.when(SecurityContextHolder::getContext)
                             .thenReturn(securityContext);

                when(securityContext.getAuthentication())
                        .thenReturn(token);

                when(token.getToken())
                        .thenReturn(jwt);

                when(token.isAuthenticated())
                        .thenReturn(true);

                when(jwt.getClaimAsString("sub"))
                        .thenReturn(username);

                // When
                final boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

                @SuppressWarnings("unchecked")
                final Map<String, BucketWrapper> userBuckets = (Map<String, BucketWrapper>)
                        ReflectionTestUtils.getField(rateLimitInterceptor, "userBuckets");

                // Then
                Assertions.assertThat(result)
                          .isTrue();

                Assertions.assertThat(userBuckets)
                          .extracting(m -> m.get(username))
                          .isNotNull();

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Limit"))
                          .isNotNull()
                          .extracting(Integer::parseInt)
                          .isEqualTo(BucketType.USER.getLimit());

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Remaining"))
                          .isNotNull()
                          .extracting(Integer::parseInt)
                          .isEqualTo(BucketType.USER.getBurstLimit() - 1);

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Reset"))
                          .isNotNull();
            }
        }

        @Test
        @DisplayName("Should use RemoteAddr header when user is not authenticated")
        void shouldUseRemoteAddrHeaderWhenUserIsNotAuthenticated() {
            try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
                // Given
                final MockHttpServletRequest request = new MockHttpServletRequest();
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
                final SecurityContext securityContext = mock(SecurityContext.class);

                mockedContext.when(SecurityContextHolder::getContext)
                             .thenReturn(securityContext);

                when(securityContext.getAuthentication())
                        .thenReturn(token);

                // When
                final boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

                @SuppressWarnings("unchecked")
                final Map<String, BucketWrapper> ipBuckets = (Map<String, BucketWrapper>)
                        ReflectionTestUtils.getField(rateLimitInterceptor, "ipBuckets");

                // Then
                Assertions.assertThat(result)
                          .isTrue();

                Assertions.assertThat(ipBuckets)
                          .extracting(m -> m.get(request.getRemoteAddr()))
                          .isNotNull();

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Limit"))
                          .isNotNull()
                          .extracting(Integer::parseInt)
                          .isEqualTo(BucketType.IP.getLimit());

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Remaining"))
                          .isNotNull()
                          .extracting(Integer::parseInt)
                          .isEqualTo(BucketType.IP.getBurstLimit() - 1);

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Reset"))
                          .isNotNull();

                verify(token, never()).getToken();
            }
        }

        @Test
        @DisplayName("Should use RemoteAddr header when authenticated user has nullable UserId")
        void shouldUseRemoteAddrHeaderWhenAuthenticatedUserHasNullableUserId() {
            try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
                //Given
                final MockHttpServletRequest request = new MockHttpServletRequest();
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
                final SecurityContext securityContext = mock(SecurityContext.class);
                final Jwt jwt = mock(Jwt.class);

                mockedContext.when(SecurityContextHolder::getContext)
                             .thenReturn(securityContext);

                when(securityContext.getAuthentication())
                        .thenReturn(token);

                when(token.getToken())
                        .thenReturn(jwt);

                when(token.isAuthenticated())
                        .thenReturn(true);

                // When
                final boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

                @SuppressWarnings("unchecked")
                final Map<String, BucketWrapper> ipBuckets = (Map<String, BucketWrapper>)
                        ReflectionTestUtils.getField(rateLimitInterceptor, "ipBuckets");

                // Then
                Assertions.assertThat(result)
                          .isTrue();

                Assertions.assertThat(ipBuckets)
                          .extracting(m -> m.get(request.getRemoteAddr()))
                          .isNotNull();

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Limit"))
                          .isNotNull()
                          .extracting(Integer::parseInt)
                          .isEqualTo(BucketType.IP.getLimit());

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Remaining"))
                          .isNotNull()
                          .extracting(Integer::parseInt)
                          .isEqualTo(BucketType.IP.getBurstLimit() - 1);

                Assertions.assertThat(response)
                          .extracting(r -> r.getHeader("X-RateLimit-Reset"))
                          .isNotNull();

                verify(jwt).getClaimAsString("sub");
            }
        }

        @Test
        @DisplayName("Should use authenticated user for Rate Limit and exceed limits")
        void shouldUseAuthenticatedUserForRateLimitAndExceedLimits() {
            try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
                // Given
                final String username = FAKER.internet().emailAddress();
                final MockHttpServletRequest request = new MockHttpServletRequest();
                final MockHttpServletResponse response = new MockHttpServletResponse();
                final JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
                final SecurityContext securityContext = mock(SecurityContext.class);
                final Jwt jwt = mock(Jwt.class);

                mockedContext.when(SecurityContextHolder::getContext)
                             .thenReturn(securityContext);

                when(securityContext.getAuthentication())
                        .thenReturn(token);

                when(token.getToken())
                        .thenReturn(jwt);

                when(token.isAuthenticated())
                        .thenReturn(true);

                when(jwt.getClaimAsString("sub"))
                        .thenReturn(username);

                // When
                for (int i = 0; i < BucketType.USER.getBurstLimit(); i++) {
                    rateLimitInterceptor.preHandle(request, response, new Object());
                }

                Assertions.assertThatThrownBy(() -> rateLimitInterceptor.preHandle(request, response, new Object()))
                          .isExactlyInstanceOf(RateLimitException.class)
                          .satisfies(e -> {
                              Assertions.assertThat(((RateLimitException) e).getLimitHeader())
                                        .isEqualTo(Pair.of("X-RateLimit-Limit", String.valueOf(BucketType.USER.getLimit())));

                              Assertions.assertThat(((RateLimitException) e).getLimitRemainingHeader())
                                        .isEqualTo(Pair.of("X-RateLimit-Remaining", "0"));

                              Assertions.assertThat(((RateLimitException) e).getLimitResetHeader())
                                        .isNotNull();

                              Assertions.assertThat(((RateLimitException) e).getRetryAfterHeader())
                                        .isNotNull();
                          });

                @SuppressWarnings("unchecked")
                final Map<String, BucketWrapper> userBuckets = (Map<String, BucketWrapper>)
                        ReflectionTestUtils.getField(rateLimitInterceptor, "userBuckets");

                Assertions.assertThat(userBuckets)
                          .extracting(m -> m.get(username))
                          .isNotNull();
            }
        }

        @Test
        @DisplayName("Should use IP address from RemoteAddr header for Rate Limit")
        void shouldUseIpAddressFromRemoteAddrHeaderForRateLimit() {
            // Given
            final MockHttpServletRequest request = new MockHttpServletRequest();
            final MockHttpServletResponse response = new MockHttpServletResponse();

            final String randomIp = FAKER.internet().publicIpV4Address();
            request.setRemoteAddr(randomIp);

            // When
            final boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

            @SuppressWarnings("unchecked")
            final Map<String, BucketWrapper> ipBuckets = (Map<String, BucketWrapper>)
                    ReflectionTestUtils.getField(rateLimitInterceptor, "ipBuckets");

            // Then
            Assertions.assertThat(result)
                      .isTrue();

            Assertions.assertThat(ipBuckets)
                      .extracting(m -> m.get(randomIp))
                      .isNotNull();

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Limit"))
                      .isNotNull()
                      .extracting(Integer::parseInt)
                      .isEqualTo(BucketType.IP.getLimit());

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Remaining"))
                      .isNotNull()
                      .extracting(Integer::parseInt)
                      .isEqualTo(BucketType.IP.getBurstLimit() - 1);

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Reset"))
                      .isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when RemoteAddr header is missing")
        void shouldThrowExceptionWhenRemoteAddrHeaderIsMissing() {
            // Given
            final MockHttpServletRequest request = new MockHttpServletRequest();
            final MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("");

            // When
            // Then
            Assertions.assertThatThrownBy(() -> rateLimitInterceptor.preHandle(request, response, new Object()))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("The IP request cannot be extracted:: Request: " + request);

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Limit"))
                      .isNull();

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Remaining"))
                      .isNull();

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Reset"))
                      .isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP"
        })
        @DisplayName("Should use IP address for Rate Limit via Ip Headers")
        void shouldUseIpAddressForRateLimitViaIpHeaders(final String header) {
            // Given
            final MockHttpServletRequest request = new MockHttpServletRequest();
            final MockHttpServletResponse response = new MockHttpServletResponse();

            final String randomIp = FAKER.internet().publicIpV4Address();
            request.addHeader(header, randomIp);

            // When
            final boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

            @SuppressWarnings("unchecked")
            final Map<String, BucketWrapper> ipBuckets = (Map<String, BucketWrapper>)
                    ReflectionTestUtils.getField(rateLimitInterceptor, "ipBuckets");

            // Then
            Assertions.assertThat(result)
                      .isTrue();

            Assertions.assertThat(ipBuckets)
                      .extracting(m -> m.get(randomIp))
                      .isNotNull();

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Limit"))
                      .isNotNull()
                      .extracting(Integer::parseInt)
                      .isEqualTo(BucketType.IP.getLimit());

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Remaining"))
                      .isNotNull()
                      .extracting(Integer::parseInt)
                      .isEqualTo(BucketType.IP.getBurstLimit() - 1);

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Reset"))
                      .isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP"
        })
        @DisplayName("Should use IP address for Rate Limit via Ip Headers and split multiple IPs")
        void shouldUseIpAddressForRateLimitViaIpHeadersAndSplitMultipleIPs(final String header) {
            // Given
            final MockHttpServletRequest request = new MockHttpServletRequest();
            final MockHttpServletResponse response = new MockHttpServletResponse();

            final String firstIp = FAKER.internet().publicIpV4Address();
            final String secondIp = FAKER.internet().publicIpV4Address();
            final String randomIp = firstIp + "," + secondIp;

            request.addHeader(header, randomIp);

            // When
            final boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

            @SuppressWarnings("unchecked")
            final Map<String, BucketWrapper> ipBuckets = (Map<String, BucketWrapper>)
                    ReflectionTestUtils.getField(rateLimitInterceptor, "ipBuckets");

            // Then
            Assertions.assertThat(result)
                      .isTrue();

            Assertions.assertThat(ipBuckets)
                      .extracting(m -> m.get(firstIp))
                      .isNotNull();

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Limit"))
                      .isNotNull()
                      .extracting(Integer::parseInt)
                      .isEqualTo(BucketType.IP.getLimit());

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Remaining"))
                      .isNotNull()
                      .extracting(Integer::parseInt)
                      .isEqualTo(BucketType.IP.getBurstLimit() - 1);

            Assertions.assertThat(response)
                      .extracting(r -> r.getHeader("X-RateLimit-Reset"))
                      .isNotNull();
        }

        @Test
        @DisplayName("Should use IP address from RemoteAddr header for Rate Limit and exceed limits")
        void shouldUseIpAddressForRateLimitAndExceedLimits() {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            final MockHttpServletResponse response = new MockHttpServletResponse();

            final String randomIp = FAKER.internet().publicIpV4Address();
            request.setRemoteAddr(randomIp);

            // When
            for (int i = 0; i < BucketType.IP.getBurstLimit(); i++) {
                rateLimitInterceptor.preHandle(request, response, new Object());
            }

            Assertions.assertThatThrownBy(() -> rateLimitInterceptor.preHandle(request, response, new Object()))
                      .isExactlyInstanceOf(RateLimitException.class)
                      .satisfies(e -> {
                          Assertions.assertThat(((RateLimitException) e).getLimitHeader())
                                    .isEqualTo(Pair.of("X-RateLimit-Limit", String.valueOf(BucketType.IP.getLimit())));

                          Assertions.assertThat(((RateLimitException) e).getLimitRemainingHeader())
                                    .isEqualTo(Pair.of("X-RateLimit-Remaining", "0"));

                          Assertions.assertThat(((RateLimitException) e).getLimitResetHeader())
                                    .isNotNull();

                          Assertions.assertThat(((RateLimitException) e).getRetryAfterHeader())
                                    .isNotNull();
                      });

            @SuppressWarnings("unchecked")
            final Map<String, BucketWrapper> ipBuckets = (Map<String, BucketWrapper>)
                    ReflectionTestUtils.getField(rateLimitInterceptor, "ipBuckets");

            Assertions.assertThat(ipBuckets)
                      .extracting(m -> m.get(randomIp))
                      .isNotNull();

        }

        @Test
        @DisplayName("Should throw exception with proper message")
        void shouldThrowExceptionWithProperMessage() {
            // Given
            final String key = FAKER.lorem().sentence();

            final BucketWrapper bucketWrapper = new BucketWrapper(
                    key, BucketType.IP, mock(Bucket.class)
            );

            // When
            final Exception exception = ReflectionTestUtils.invokeMethod(RateLimitInterceptor.HeaderMapper.class, "getBucketException", bucketWrapper);

            // Then
            Assertions.assertThat(exception)
                      .asInstanceOf(InstanceOfAssertFactories.type(IllegalStateException.class))
                      .extracting(Exception::getMessage)
                      .isEqualTo("Cannot resolve bucket for key: " + key);
        }
    }

    @Nested
    @DisplayName("ClearUnusedBuckets Tests")
    class ClearUnusedBucketsTests {

        @Test
        @DisplayName("Should remove unused buckets")
        void shouldClearUnusedBuckets() {
            // Given
            final String firstIp = FAKER.internet().publicIpV4Address();
            final String secondIp = FAKER.internet().publicIpV4Address();
            final String thirdIp = FAKER.internet().publicIpV4Address();

            final String firstUsername = FAKER.internet().emailAddress();
            final String secondUsername = FAKER.internet().emailAddress();
            final String thirdUsername = FAKER.internet().emailAddress();

            final Map<String, BucketWrapper> ipBucketsBefore = new HashMap<>(Map.of(
                    firstIp, new BucketWrapper(firstIp, BucketType.IP, mock(Bucket.class)),
                    secondIp, new BucketWrapper(secondIp, BucketType.IP, mock(Bucket.class)),
                    thirdIp, new BucketWrapper(thirdIp, BucketType.IP, mock(Bucket.class))
            ));

            final Map<String, BucketWrapper> userBucketsBefore = new HashMap<>(Map.of(
                    firstUsername, new BucketWrapper(firstUsername, BucketType.USER, mock(Bucket.class)),
                    secondUsername, new BucketWrapper(secondUsername, BucketType.USER, mock(Bucket.class)),
                    thirdUsername, new BucketWrapper(thirdUsername, BucketType.USER, mock(Bucket.class))
            ));

            ReflectionTestUtils.setField(ipBucketsBefore.get(firstIp), "lastUsageDate", new AtomicReference<>(LocalDateTime.now().minusHours(3)));
            ReflectionTestUtils.setField(ipBucketsBefore.get(secondIp), "lastUsageDate", new AtomicReference<>(LocalDateTime.now().minusHours(2)));

            ReflectionTestUtils.setField(userBucketsBefore.get(firstUsername), "lastUsageDate", new AtomicReference<>(LocalDateTime.now().minusHours(3)));
            ReflectionTestUtils.setField(userBucketsBefore.get(secondUsername), "lastUsageDate", new AtomicReference<>(LocalDateTime.now().minusHours(2)));

            ReflectionTestUtils.setField(rateLimitInterceptor, "ipBuckets", ipBucketsBefore);
            ReflectionTestUtils.setField(rateLimitInterceptor, "userBuckets", userBucketsBefore);

            // When
            rateLimitInterceptor.clearUnusedBuckets();

            // Then
            @SuppressWarnings("unchecked")
            final Map<String, BucketWrapper> ipBucketsAfter =
                    (Map<String, BucketWrapper>) ReflectionTestUtils.getField(rateLimitInterceptor, "ipBuckets");

            @SuppressWarnings("unchecked")
            final Map<String, BucketWrapper> userBucketsAfter = (Map<String, BucketWrapper>)
                    ReflectionTestUtils.getField(rateLimitInterceptor, "userBuckets");

            Assertions.assertThat(ipBucketsAfter)
                      .isNotNull()
                      .hasSize(1)
                      .containsExactly(Map.entry(thirdIp, ipBucketsBefore.get(thirdIp)));

            Assertions.assertThat(userBucketsAfter)
                      .isNotNull()
                      .hasSize(1)
                      .containsExactly(Map.entry(thirdUsername, userBucketsBefore.get(thirdUsername)));
        }
    }
}