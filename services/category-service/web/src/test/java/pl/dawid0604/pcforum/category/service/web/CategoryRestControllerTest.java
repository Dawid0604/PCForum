package pl.dawid0604.pcforum.category.service.web;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryEntityCreateDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryWrapperDto;
import pl.dawid0604.pcforum.category.service.core.CategoryEntityService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PACKAGE;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@DisplayName("CategoryRestController Tests")
class CategoryRestControllerTest {

    @Nested
    @EnableMethodSecurity
    @DisplayName("Unit tests")
    @Import(UnitTests.TestSecurityConfig.class)
    @WebMvcTest(controllers = CategoryRestController.class)
    class UnitTests {

        @Autowired
        @SuppressWarnings("unused")
        private MockMvc mockMvc;

        @MockitoBean
        @SuppressWarnings("unused")
        private CategoryEntityService categoryEntityService;

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        @ParameterizedTest
        @DisplayName("Should create and return location header")
        @MethodSource("shouldCreateAsAdminUserAndReturnLocationHeader")
        void shouldCreateAsAdminUserAndReturnLocationHeader(final String username, final String[] roles) throws Exception {
            // Given
            final String publicCategoryId = NanoIdUtils.randomNanoId();
            final CategoryEntityCreateDto payload = Utils.createCategoryEntityCreateMock();

            when(categoryEntityService.save(payload))
                    .thenReturn(publicCategoryId);

            // When
            // Then
            mockMvc.perform(
                        post("").contentType(APPLICATION_JSON_VALUE)
                                          .content(OBJECT_MAPPER.writeValueAsString(payload))
                                          .with(user(username).roles(roles))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                            .andExpect(header().stringValues("Location", getLocationHeaderValue(publicCategoryId)));
        }

        private static Stream<Arguments> shouldCreateAsAdminUserAndReturnLocationHeader() {
            return Stream.of(
                    Arguments.of("admin", new String[] { "ADMIN", "MODERATOR", "USER" }),
                    Arguments.of("admin", new String[] { "ADMIN", "MODERATOR" }),
                    Arguments.of("admin", new String[] { "ADMIN" }),

                    Arguments.of("moderator", new String[] { "ADMIN", "MODERATOR" }),
                    Arguments.of("moderator", new String[] {"MODERATOR" })
            );
        }

        @Test
        @WithMockUser(
                username = "user",
                password = "user",
                roles = "USER"
        )
        @DisplayName("should not create as User")
        void shouldNotCreateAsUserAndReturnLocationHeader() throws Exception {
            // Given
            final CategoryEntityCreateDto payload = Utils.createCategoryEntityCreateMock();

            // When
            // Then
            mockMvc.perform(
                        post("").contentType(APPLICATION_JSON_VALUE)
                                          .content(OBJECT_MAPPER.writeValueAsString(payload))
                    )
                    .andDo(print())
                    .andExpect(status().isForbidden())
                            .andExpect(header().doesNotExist("Location"));

            verifyNoInteractions(categoryEntityService);
        }

        private String getLocationHeaderValue(final String publicCategoryId) {
            return "http://localhost/" + publicCategoryId;
        }

        @TestConfiguration
        @EnableMethodSecurity
        static class TestSecurityConfig {

            @Bean
            @SuppressWarnings("unused")
            SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http.csrf(CsrfConfigurer::disable)
                           .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                           .build();
            }
        }
    }

    @Nested
    @DisplayName("Integration tests")
    @SpringBootTest(
            webEnvironment = RANDOM_PORT,
            classes = IntegrationTests.IntegrationTestBase.class
    )
    @TestPropertySource(
            properties = {
                    "spring.datasource.url=jdbc:h2:mem:testdb",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.application.name=xyz"
            }
    )
    class IntegrationTests {

        @LocalServerPort
        @SuppressWarnings("unused")
        private int port;

        @MockitoBean
        @SuppressWarnings("unused")
        private CategoryEntityService categoryEntityService;

        @BeforeEach
        void setUp() {
            RestAssured.port = port;
        }

        @Nested
        @DisplayName("getAll() tests")
        class GetAllTests {

            @Test
            @DisplayName("Should get all without parentId")
            void shouldGetAllWithoutParentId() {
                // Given
                final List<CategoryWrapperDto> categories = List.of(
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock()
                );

                when(categoryEntityService.getCategories(null))
                        .thenReturn(CompletableFuture.supplyAsync(() -> categories));

                // When
                // Then
                RestAssured.given()
                        .when()
                        .get("/all")
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(categories.size()));
            }

            @Test
            @DisplayName("Should get all with parentId and return empty list")
            void shouldGetAllWithParentIdAndReturnEmptyList() {
                // Given
                when(categoryEntityService.getCategories(null))
                        .thenReturn(CompletableFuture.supplyAsync(Collections::emptyList));

                // When
                // Then
                RestAssured.given()
                           .when()
                           .get("/all")
                           .then()
                           .statusCode(204)
                           .body(emptyString());
            }

            @Test
            @DisplayName("Should get all with parentId")
            void shouldGetAllWithParentId() {
                // Given
                final String parentId = NanoIdUtils.randomNanoId();
                final List<CategoryWrapperDto> categories = List.of(
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock(),
                        Utils.createCategoryWrapperDtoMock()
                );

                when(categoryEntityService.getCategories(parentId))
                        .thenReturn(CompletableFuture.supplyAsync(() -> categories));

                // When
                // Then
                RestAssured.given()
                        .when()
                        .param("parentId", parentId)
                        .get("/all")
                        .then()
                        .statusCode(200)
                        .body("size()", equalTo(categories.size()));
            }

            @Test
            @DisplayName("Should get all and return empty list")
            void shouldGetAllAndReturnEmptyList() {
                // Given
                final String parentId = NanoIdUtils.randomNanoId();

                when(categoryEntityService.getCategories(parentId))
                        .thenReturn(CompletableFuture.supplyAsync(Collections::emptyList));

                // When
                // Then
                RestAssured.given()
                           .when()
                           .param("parentId", parentId)
                           .get("/all")
                           .then()
                           .statusCode(204)
                           .body(emptyString());
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "xyz")
            @DisplayName("Should not get all when parentId is invalid")
            void shouldNotGetAllWhenParentIdIsInvalid(final String parentId) {
                // Given
                when(categoryEntityService.getCategories(parentId))
                        .thenReturn(CompletableFuture.supplyAsync(Collections::emptyList));

                // When
                // Then
                RestAssured.given()
                           .when()
                           .param("parentId", parentId)
                           .get("/all")
                           .then()
                           .statusCode(400)
                           .body(Matchers.notNullValue());
            }
        }

        @Nested
        @DisplayName("getCategory() tests")
        class GetCategoryTests {

            @Test
            @DisplayName("Should get category and found")
            void shouldGetCategoryAndFound() {
                // Given
                final String categoryId = NanoIdUtils.randomNanoId();
                final CategoryWrapperDto category = Utils.createCategoryWrapperDtoMock();

                when(categoryEntityService.getCategory(categoryId))
                        .thenReturn(CompletableFuture.supplyAsync(() -> Optional.of(category)));

                // When
                final CategoryWrapperDto body = RestAssured.given()
                                                           .when()
                                                           .get("/{categoryId}", categoryId)
                                                           .then()
                                                           .statusCode(200)
                                                           .extract()
                                                           .as(CategoryWrapperDto.class);

                // Then
                Assertions.assertThat(body)
                          .isNotNull()
                          .isEqualTo(category);
            }

            @Test
            @DisplayName("Should get category and no found")
            void shouldGetCategoryAndNoFound() {
                // Given
                final String categoryId = NanoIdUtils.randomNanoId();

                when(categoryEntityService.getCategory(categoryId))
                        .thenReturn(CompletableFuture.supplyAsync(Optional::empty));

                // When
                // Then
                RestAssured.given()
                           .when()
                           .get("/{categoryId}", categoryId)
                           .then()
                           .statusCode(404)
                           .body(emptyString());
            }
        }

        @Nested
        @DisplayName("create() tests")
        class CreateTests {
            private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

            @RepeatedTest(20)
            @DisplayName("Should not create when payload is invalid")
            void shouldNotCreateWhenPayloadIsInvalid() throws JsonProcessingException {
                // Given
                final CategoryEntityCreateDto invalidPayload = Utils.createInvalidCategoryEntityCreateMock();

                // When
                RestAssured.given()
                           .contentType(ContentType.JSON)
                           .body(OBJECT_MAPPER.writeValueAsString(invalidPayload))
                           .post()
                           .then()
                           .statusCode(400)
                           .header("Location", emptyOrNullString())
                           .body(Matchers.notNullValue());
            }
        }

        @EnableAutoConfiguration
        @SpringBootConfiguration
        @NoArgsConstructor(access = PACKAGE)
        @ComponentScan(basePackages = "pl.dawid0604.pcforum.category.service.web")
        private static class IntegrationTestBase { }
    }

    @NoArgsConstructor(access = NONE)
    static final class Utils {
        private static final Faker FAKER = new Faker();

        public static CategoryWrapperDto createCategoryWrapperDtoMock() {
            return new CategoryWrapperDto(
                    new CategoryDto(
                            NanoIdUtils.randomNanoId(),
                            FAKER.lorem().sentence(),
                            FAKER.lorem().sentence(),
                            "https://" + FAKER.domain().fullDomain("pcforum") + "/image.png"
                    ),
                    FAKER.number().numberBetween(1, 100),
                    FAKER.number().numberBetween(1, 100),
                    emptyList()
            );
        }

        public static CategoryEntityCreateDto createCategoryEntityCreateMock() {
            return new CategoryEntityCreateDto(
                    FAKER.lorem().sentence(),
                    FAKER.lorem().sentence(),
                    "https://" + FAKER.domain().fullDomain("pcforum") + "/image.png",
                    FAKER.options()
                         .option(null, NanoIdUtils.randomNanoId())
            );
        }

        public static CategoryEntityCreateDto createInvalidCategoryEntityCreateMock() {
            return new CategoryEntityCreateDto(
                    FAKER.options()
                         .option(null, ""),

                    FAKER.options()
                         .option(null, "", "ftp://"),

                    FAKER.lorem().sentence(),
                    FAKER.options()
                         .option(null, NanoIdUtils.randomNanoId().substring(0, 19))
            );
        }
    }
}