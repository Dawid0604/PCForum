package pl.dawid0604.pcforum.category.service.core;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import jakarta.persistence.EntityNotFoundException;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dawid0604.pcforum.category.service.commons.Constants;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryEntityCreateDto;
import pl.dawid0604.pcforum.category.service.commons.dto.CategoryWrapperDto;
import pl.dawid0604.pcforum.category.service.persistence.CategoryEntity;
import pl.dawid0604.pcforum.category.service.persistence.CategoryEntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.mockito.BDDMockito.*;

class CategoryEntityServiceImplTest {

    @Nested
    @DisplayName("Unit tests")
    @ExtendWith(MockitoExtension.class)
    class UnitTests {

        @Mock
        private CategoryEntityMapper mapper;

        @Mock
        private CategoryEntityRepository repository;

        @InjectMocks
        private CategoryEntityServiceImpl service;

        private static final Faker FAKER = new Faker();

        @Nested
        @DisplayName("getCategories() tests")
        class GetCategoriesMethodTests {

            @Test
            @DisplayName("Should return categories when ParentId is null")
            void shouldReturnCategoriesWhenParentIdIsNull() throws ExecutionException, InterruptedException {
                // Given
                final List<CategoryEntity> mockEntities = createThreeMockCategoryEntities();
                final List<CategoryWrapperDto> expectedDtos = createThreeMockCategoryWrapperDtos();

                when(repository.findAllCustom())
                        .thenReturn(mockEntities);

                when(mapper.wrap(any(CategoryEntity.class), anyList()))
                        .thenReturn(expectedDtos.get(0), expectedDtos.get(1), expectedDtos.get(2));

                // When
                final CompletableFuture<List<CategoryWrapperDto>> result = service.getCategories(null);
                final List<CategoryWrapperDto> actualResult = result.get();

                // Then
                Assertions.assertThat(actualResult)
                          .hasSize(3);

                verify(repository).findAllCustom();
                verify(repository, never()).findByParentId(anyString());
            }

            @Test
            @DisplayName("Should return categories by parent root id")
            void shouldReturnCategoriesByParentRootId() throws ExecutionException, InterruptedException {
                // Given
                final String parentId = NanoIdUtils.randomNanoId();
                final List<CategoryEntity> mockEntities = createThreeMockCategoryEntities();
                final List<CategoryWrapperDto> expectedDtos = createThreeMockCategoryWrapperDtos();

                when(repository.findByParentId(parentId))
                        .thenReturn(mockEntities);

                when(mapper.wrap(any(CategoryEntity.class), anyList()))
                        .thenReturn(expectedDtos.get(0), expectedDtos.get(1), expectedDtos.get(2));

                // When
                final CompletableFuture<List<CategoryWrapperDto>> result = service.getCategories(parentId);
                final List<CategoryWrapperDto> actualResult = result.get();

                // Then
                Assertions.assertThat(actualResult)
                          .hasSize(2);

                verify(repository).findByParentId(parentId);
                verify(repository, never()).findAllCustom();
            }

            @Test
            @DisplayName("Should return categories by parent subcategory id")
            void shouldReturnCategoriesByParentSubcategoryId() throws ExecutionException, InterruptedException {
                // Given
                final String parentId = NanoIdUtils.randomNanoId();
                final CategoryEntity parentCategory = CategoryEntity.builder()
                                                                    .id(FAKER.number().randomNumber(3))
                                                                    .publicId(parentId)
                                                                    .build();

                final List<CategoryEntity> mockEntities = List.of(
                        parentCategory,
                        createMockCategoryEntity(parentCategory),
                        createMockCategoryEntity(parentCategory)
                );

                final CategoryWrapperDto firstDto = createMockCategoryWrapperDto();
                final CategoryWrapperDto secondDto = createMockCategoryWrapperDto();

                final List<CategoryWrapperDto> expectedDtos = List.of(
                        firstDto, secondDto,
                        createMockCategoryWrapperDto(List.of(firstDto, secondDto))
                );

                when(repository.findByParentId(parentId))
                        .thenReturn(mockEntities);

                when(mapper.wrap(any(CategoryEntity.class), eq(emptyList())))
                        .thenReturn(expectedDtos.get(1), expectedDtos.get(0));

                when(mapper.wrap(parentCategory, List.of(secondDto, firstDto)))
                        .thenReturn(expectedDtos.get(2));

                // When
                final CompletableFuture<List<CategoryWrapperDto>> result = service.getCategories(parentId);
                final List<CategoryWrapperDto> actualResult = result.get();

                // Then
                Assertions.assertThat(actualResult)
                          .hasSize(2);

                verify(repository).findByParentId(parentId);
                verify(repository, never()).findAllCustom();
            }

            @Test
            @DisplayName("Should return empty list when no categories found")
            void shouldReturnEmptyCategoriesWhenNoFound() throws ExecutionException, InterruptedException {
                // Given
                final String parentId = NanoIdUtils.randomNanoId();

                // When
                final CompletableFuture<List<CategoryWrapperDto>> result = service.getCategories(parentId);
                final List<CategoryWrapperDto> actualResult = result.get();

                // Then
                Assertions.assertThat(actualResult)
                          .isEmpty();

                verify(repository).findByParentId(parentId);
                verify(repository, never()).findAllCustom();
                verifyNoInteractions(mapper);
            }

            @Test
            @DisplayName("Should build hierarchical category tree")
            void shouldBuildHierarchicalCategoryTree() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntity rootCategory = createMockCategoryEntity(null);
                final CategoryEntity secondRootCategory = createMockCategoryEntity(null);
                final CategoryEntity childCategory = createMockCategoryEntity(rootCategory);
                final List<CategoryEntity> categories = List.of(
                        rootCategory, secondRootCategory, childCategory
                );

                final CategoryWrapperDto childDto = createMockCategoryWrapperDto(childCategory.getPublicId());
                final CategoryWrapperDto rootDto = createMockCategoryWrapperDto(rootCategory.getPublicId());
                final CategoryWrapperDto secondRootDto = createMockCategoryWrapperDto(secondRootCategory.getPublicId());

                when(repository.findAllCustom())
                        .thenReturn(categories);

                when(mapper.wrap(eq(childCategory), any()))
                        .thenReturn(childDto);

                when(mapper.wrap(eq(rootCategory), any()))
                        .thenReturn(rootDto);

                when(mapper.wrap(eq(secondRootCategory), any()))
                        .thenReturn(secondRootDto);

                // When
                final CompletableFuture<List<CategoryWrapperDto>> result = service.getCategories(null);
                final List<CategoryWrapperDto> actualResult = result.get();

                // Then
                Assertions.assertThat(actualResult)
                          .hasSize(2);

                Assertions.assertThat(actualResult.getFirst())
                          .isEqualTo(rootDto);

                Assertions.assertThat(actualResult.getLast())
                          .isEqualTo(secondRootDto);

                verify(repository).findAllCustom();
                verify(repository, never()).findByParentId(anyString());
            }

            @Test
            @DisplayName("Is root category when parent is null")
            void isRootCategoryWhenParentIsNull() {
                // Given
                final CategoryEntity category = CategoryEntity.builder().build();

                // When
                final Boolean result = ReflectionTestUtils.invokeMethod(service, "isRootCategory", category, null);

                // Then
                Assertions.assertThat(result)
                          .isTrue();
            }

            @Test
            @DisplayName("Is root category when publicId matches")
            void isRootCategoryWhenPublicIdMatches() {
                // Given
                final String publicId = NanoIdUtils.randomNanoId();

                final CategoryEntity category = CategoryEntity.builder()
                                                              .publicId(publicId)
                                                              .build();

                // When
                final Boolean result = ReflectionTestUtils.invokeMethod(service, "isRootCategory", category, publicId);

                // Then
                Assertions.assertThat(result)
                          .isTrue();
            }

            @Test
            @DisplayName("Is not root category when publicId does not matches")
            void isNotRootCategoryWhenPublicIdDoesNotMatches() {
                // Given
                final String publicId = NanoIdUtils.randomNanoId();

                final CategoryEntity category = CategoryEntity.builder()
                                                              .publicId(NanoIdUtils.randomNanoId())
                                                              .parent(CategoryEntity.builder().build())
                                                              .build();

                // When
                final Boolean result = ReflectionTestUtils.invokeMethod(service, "isRootCategory", category, publicId);

                // Then
                Assertions.assertThat(result)
                          .isFalse();
            }
        }

        @Nested
        @DisplayName("getCategory() tests")
        class GetCategoryMethodTests {

            @Test
            @DisplayName("Should return category when found")
            void shouldReturnCategoryWhenFound() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntity parentMockEntity = createMockCategoryEntity(null);
                final CategoryEntity mockEntity = createMockCategoryEntity(parentMockEntity);

                final String mockEntityPublicId = mockEntity.getPublicId();
                final CategoryWrapperDto expectedDto = createMockCategoryWrapperDto(mockEntityPublicId);

                when(repository.findByPublicId(mockEntityPublicId))
                        .thenReturn(Optional.of(mockEntity));

                when(mapper.wrap(mockEntity))
                        .thenReturn(expectedDto);

                // When
                final CompletableFuture<Optional<CategoryWrapperDto>> result = service.getCategory(mockEntityPublicId);
                final Optional<CategoryWrapperDto> actualResult = result.get();

                // Then
                Assertions.assertThat(actualResult)
                          .isPresent()
                          .hasValue(expectedDto);

                verify(repository, never()).findAllCustom();
            }

            @Test
            @DisplayName("Should return empty optional when category not found")
            void shouldReturnEmptyOptionalWhenCategoryNotFound() throws ExecutionException, InterruptedException {
                // Given
                final String categoryPublicId = NanoIdUtils.randomNanoId();

                // When
                final CompletableFuture<Optional<CategoryWrapperDto>> result = service.getCategory(categoryPublicId);
                final Optional<CategoryWrapperDto> actualResult = result.get();

                // Then
                Assertions.assertThat(actualResult)
                          .isEmpty();

                verify(repository, never()).findAllCustom();
                verify(repository).findByPublicId(categoryPublicId);
                verifyNoInteractions(mapper);
            }
        }

        @Nested
        @DisplayName("save() tests")
        class SaveMethodTests {

            @Test
            @DisplayName("Should save root category successfully")
            void shouldSaveCategorySuccessfully() {
                // Given
                final CategoryEntityCreateDto payload = createMockCreateDto(null);
                final CategoryEntity savedEntity = createMockCategoryEntity(null);

                when(repository.save(any(CategoryEntity.class)))
                        .thenReturn(savedEntity);

                // When
                final String result = service.save(payload);

                // Then
                Assertions.assertThat(result)
                          .isEqualTo(savedEntity.getPublicId());

                verify(repository).save(any(CategoryEntity.class));
                verify(repository, never()).findByPublicId(anyString());
            }

            @Test
            @DisplayName("Should save subcategory category successfully")
            void shouldSaveSubcategorySuccessfully() {
                // Given
                final CategoryEntity parentEntity = createMockCategoryEntity(null);
                final CategoryEntityCreateDto payload = createMockCreateDto(parentEntity.getPublicId());
                final CategoryEntity savedEntity = createMockCategoryEntity(parentEntity);

                when(repository.save(any(CategoryEntity.class)))
                        .thenReturn(savedEntity);

                when(repository.findByPublicId(parentEntity.getPublicId()))
                        .thenReturn(Optional.of(parentEntity));

                // When
                final String result = service.save(payload);

                // Then
                Assertions.assertThat(result)
                          .isEqualTo(savedEntity.getPublicId());

                verify(repository).save(any(CategoryEntity.class));
            }

            @Test
            @DisplayName("Should throw exception when parent not found")
            void shouldThrowExceptionWhenParentNotFound() {
                // Given
                final String parentId = NanoIdUtils.randomNanoId();
                final CategoryEntityCreateDto payload = createMockCreateDto(parentId);

                // When
                // Then
                Assertions.assertThatThrownBy(() -> service.save(payload))
                          .isInstanceOf(EntityNotFoundException.class);

                verify(repository, never()).save(any(CategoryEntity.class));
                verify(repository).findByPublicId(parentId);
            }

            @Test
            @DisplayName("Should handle DataIntegrityViolationException with saveRecover() method")
            void shouldHandleDataIntegrityViolationExceptionWithSavRecoverMethod() {
                // Given
                final CategoryEntityCreateDto payload = createMockCreateDto(null);
                final DataIntegrityViolationException exception = new DataIntegrityViolationException(
                        FAKER.text()
                                .text(5, 30, true)
                );

                // When
                // Then
                Assertions.assertThatThrownBy(() -> service.saveRecover(exception, payload))
                          .isInstanceOf(IllegalStateException.class);
            }

        }

        private static CategoryEntity createMockCategoryEntity(final CategoryEntity parent) {
            return CategoryEntity.builder()
                    .publicId(NanoIdUtils.randomNanoId())
                    .id((long) FAKER.number().numberBetween(1, 1000))
                    .name(FAKER.text().text(1, 25, true))
                    .parent(parent)
                    .createdDate(FAKER.timeAndDate().future())
                    .description(
                            FAKER.options()
                                 .option(
                                         FAKER.lorem().sentence(5),
                                         null,
                                         ""
                                 )
                    )
                    .iconPath(
                            FAKER.options()
                                 .option(
                                         FAKER.internet().url(),
                                          null,
                                          ""
                                 )
                    )
                    .lastModifiedDate(
                            FAKER.options()
                                 .option(
                                         FAKER.timeAndDate().future(),
                                         null
                                 )
                    )
                    .modifiedBy(
                            FAKER.options()
                                 .option(
                                         FAKER.name().firstName(),
                                         null,
                                         ""
                                 ))
                    .build();
        }

        private static CategoryDto createMockCategoryDto() {
            return new CategoryDto(
                    NanoIdUtils.randomNanoId(),
                    FAKER.text().text(5, 25, true),
                    FAKER.options()
                         .option(
                                 FAKER.lorem().sentence(5),
                                 null,
                                 ""
                         ),
                    FAKER.options()
                         .option(
                                 FAKER.internet().url(),
                                 null,
                                 ""
                         )
            );
        }

        private static CategoryDto createMockCategoryDto(final String publicId) {
            return new CategoryDto(
                    publicId,
                    FAKER.text().text(5, 25, true),
                    FAKER.options()
                         .option(
                                 FAKER.lorem().sentence(5),
                                 null,
                                 ""
                         ),
                    FAKER.options()
                         .option(
                                 FAKER.internet().url(),
                                 null,
                                 ""
                         )
            );
        }

        private static CategoryEntityCreateDto createMockCreateDto(final String parentId) {
            return new CategoryEntityCreateDto(
                    FAKER.text().text(5, 25, true),
                    FAKER.options()
                         .option(
                                 FAKER.lorem().sentence(5),
                                 null,
                                 ""
                         ),
                    FAKER.options()
                         .option(
                                 FAKER.internet().url(),
                                 null,
                                 ""
                         ),
                    parentId
            );
        }

        private static CategoryWrapperDto createMockCategoryWrapperDto() {
            return new CategoryWrapperDto(
                    createMockCategoryDto(),
                    FAKER.number().randomDigit(),
                    FAKER.number().randomDigit(),
                    emptyList()
            );
        }

        private static CategoryWrapperDto createMockCategoryWrapperDto(final List<CategoryWrapperDto> subcategories) {
            return new CategoryWrapperDto(
                    createMockCategoryDto(),
                    FAKER.number().randomDigit(),
                    FAKER.number().randomDigit(),
                    subcategories
            );
        }

        private static CategoryWrapperDto createMockCategoryWrapperDto(final String publicId) {
            return new CategoryWrapperDto(
                    createMockCategoryDto(publicId),
                    FAKER.number().randomDigit(),
                    FAKER.number().randomDigit(),
                    emptyList()
            );
        }

        private static List<CategoryWrapperDto> createThreeMockCategoryWrapperDtos() {
            return IntStream.range(0, 3)
                            .mapToObj(i -> createMockCategoryWrapperDto())
                            .toList();
        }

        private static List<CategoryEntity> createThreeMockCategoryEntities() {
            return IntStream.range(0, 3)
                            .mapToObj(i -> createMockCategoryEntity(null))
                            .toList();
        }
    }

    @Nested
    @DisplayName("Integration tests")
    @SpringBootTest(classes = IntegrationTestBase.class)
    @TestPropertySource(properties = {
            "resilience4j.circuitbreaker.instances.category-service.enabled=false",
            "resilience4j.timelimiter.instances.category-service.enabled=false",
            "resilience4j.retry.instances.category-service.enabled=false",
            "spring.flyway.locations=classpath:migrations",
            "logging.level.org.hibernate.SQL=DEBUG",
    })
    class IntegrationTests extends IntegrationTestBase {

        @MockitoSpyBean
        @SuppressWarnings("unused")
        private CategoryEntityService categoryEntityService;

        @MockitoSpyBean
        @SuppressWarnings("unused")
        private CategoryEntityRepository categoryEntityRepository;

        @Autowired
        @SuppressWarnings("unused")
        private CacheManager cacheManager;

        private static final Faker FAKER = new Faker();

        @BeforeEach
        void clear() {
            categoryEntityRepository.deleteAll();
            requireNonNull(cacheManager.getCache(Constants.CACHE_KEY))
                                       .clear();
        }

        @Nested
        @DisplayName("Category operations")
        class CategoryOperations {

            @Test
            @DisplayName("Should create and retrieve root category")
            void shouldCreateAndRetrieveRootCategory() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntityCreateDto createDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                // When
                final String publicId = categoryEntityService.save(createDto);
                final CompletableFuture<Optional<CategoryWrapperDto>> result = categoryEntityService.getCategory(publicId);
                final Optional<CategoryWrapperDto> retrievedCategory = result.get();

                // Then
                Assertions.assertThat(retrievedCategory)
                          .isPresent()
                          .get()
                          .satisfies(w -> {
                             Assertions.assertThat(w.category())
                                       .isNotNull()
                                       .extracting(CategoryDto::name)
                                       .isEqualTo(createDto.name());

                             Assertions.assertThat(w.category())
                                       .isNotNull()
                                       .extracting(CategoryDto::description)
                                       .isEqualTo(createDto.description());

                             Assertions.assertThat(w.subCategories())
                                       .isEmpty();
                          });
            }

            @Test
            @DisplayName("Should create and retrieve subcategory [Single level of nesting]")
            void shouldCreateAndRetrieveSingleLevelOfNestingSubCategory() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                final String rootPublicId = categoryEntityService.save(rootCreateDto);
                final CategoryEntityCreateDto subCategoryCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        rootPublicId
                );

                final String subCategoryPublicId = categoryEntityService.save(subCategoryCreateDto);

                // When
                final CompletableFuture<Optional<CategoryWrapperDto>> subCategoryResult = categoryEntityService.getCategory(subCategoryPublicId);
                final Optional<CategoryWrapperDto> retrievedSubCategory = subCategoryResult.get();
                final CompletableFuture<List<CategoryWrapperDto>> rootSubcategoriesFuture = categoryEntityService.getCategories(rootPublicId);
                final List<CategoryWrapperDto> rootSubcategories = rootSubcategoriesFuture.get();

                // Then
                Assertions.assertThat(retrievedSubCategory)
                          .isPresent()
                          .get()
                          .satisfies(w -> {
                             Assertions.assertThat(w.category())
                                       .isNotNull()
                                       .extracting(CategoryDto::name)
                                       .isEqualTo(subCategoryCreateDto.name());

                             Assertions.assertThat(w.category())
                                       .isNotNull()
                                       .extracting(CategoryDto::description)
                                       .isEqualTo(subCategoryCreateDto.description());

                             Assertions.assertThat(w.subCategories())
                                       .isEmpty();
                          });

                Assertions.assertThat(rootSubcategories)
                          .extracting(CategoryWrapperDto::category)
                          .extracting(CategoryDto::publicId)
                          .containsExactly(subCategoryPublicId);
            }

            @Test
            @DisplayName("Should create and retrieve subcategory [Multi level of nesting ; parentId is present]")
            void shouldCreateAndRetrieveMultiLevelOfNestingSubCategory() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                final String rootPublicId = categoryEntityService.save(rootCreateDto);
                final CategoryEntityCreateDto subCategoryCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        rootPublicId
                );

                final String subCategoryPublicId = categoryEntityService.save(subCategoryCreateDto);
                final CategoryEntityCreateDto subCategoryOfSubCategoryCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        subCategoryPublicId
                );

                final String subCategoryOfSubCategoryPublicId = categoryEntityService.save(subCategoryOfSubCategoryCreateDto);

                // When
                final CompletableFuture<List<CategoryWrapperDto>> categoriesFuture = categoryEntityService.getCategories(rootPublicId);
                final List<CategoryWrapperDto> retrievedCategories = categoriesFuture.get();

                // Then
                Assertions.assertThat(retrievedCategories)
                          .hasSize(1)
                          .satisfies(categories -> {
                              Assertions.assertThat(categories.getFirst())
                                        .extracting(CategoryWrapperDto::category)
                                        .extracting(CategoryDto::publicId)
                                        .isEqualTo(subCategoryPublicId);

                              Assertions.assertThat(categories.getFirst().subCategories())
                                        .extracting(CategoryWrapperDto::category)
                                        .extracting(CategoryDto::publicId)
                                        .containsExactly(subCategoryOfSubCategoryPublicId);
                          });
            }

            @Test
            @DisplayName("Should create and retrieve subcategory [Multi level of nesting ; parentId is not present]")
            void shouldCreateAndRetrieveMultiLevelOfNestingSubCategoryWithNullableParentId() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                final String rootPublicId = categoryEntityService.save(rootCreateDto);
                final CategoryEntityCreateDto subCategoryCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        rootPublicId
                );

                final String subCategoryPublicId = categoryEntityService.save(subCategoryCreateDto);
                final CategoryEntityCreateDto subCategoryOfSubCategoryCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        subCategoryPublicId
                );

                final String subCategoryOfSubCategoryPublicId = categoryEntityService.save(subCategoryOfSubCategoryCreateDto);

                // When
                final CompletableFuture<List<CategoryWrapperDto>> categoriesFuture = categoryEntityService.getCategories(null);
                final List<CategoryWrapperDto> retrievedCategories = categoriesFuture.get();

                // Then
                Assertions.assertThat(retrievedCategories)
                          .hasSize(1)
                          .satisfies(categories -> {
                              Assertions.assertThat(categories.getFirst())
                                        .extracting(CategoryWrapperDto::category)
                                        .extracting(CategoryDto::publicId)
                                        .isEqualTo(rootPublicId);

                              Assertions.assertThat(categories.getFirst().subCategories())
                                        .extracting(CategoryWrapperDto::category)
                                        .extracting(CategoryDto::publicId)
                                        .containsExactly(subCategoryPublicId);

                              Assertions.assertThat(categories.getFirst().subCategories().getFirst().subCategories())
                                        .extracting(CategoryWrapperDto::category)
                                        .extracting(CategoryDto::publicId)
                                        .containsExactly(subCategoryOfSubCategoryPublicId);
                          });
            }
        }

        @Nested
        @DisplayName("Caching tests")
        class CachingTests {

            @Test
            @DisplayName("Should cache category results")
            void shouldCacheCategoryResults() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                categoryEntityService.save(rootCreateDto);

                // When
                categoryEntityService.getCategories(null).get();
                categoryEntityService.getCategories(null).get();

                // Then
                verify(categoryEntityRepository).findAllCustom();
            }
        }

        @Nested
        @DisplayName("Error handling tests")
        class ErrorHandlingTests {

            @Test
            @DisplayName("Should throw EntityNotFoundException for invalid parent while saving")
            void shouldThrowEntityNotFoundExceptionForInvalidParentWhileSaving() {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        "xyz"
                );

                // When
                // Then
                Assertions.assertThatThrownBy(() -> categoryEntityService.save(rootCreateDto))
                          .isInstanceOf(EntityNotFoundException.class);
            }

        }

        @Nested
        @DisplayName("saving() Retry mechanism tests")
        class SavingRetryTests {

            @Test
            @DisplayName("Should succeed after 2 retries due to DataIntegrityViolationException")
            void shouldSucceededAfterTwoRetries() {
                // Given
                final String generatedPublicId = NanoIdUtils.randomNanoId();
                final AtomicInteger callCounter = new AtomicInteger();
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                doAnswer(inv -> {
                    if (callCounter.incrementAndGet() <= 2) {
                        throw new DataIntegrityViolationException("Unique constraint violation");
                    }

                    return CategoryEntity.builder()
                            .publicId(generatedPublicId)
                            .build();

                }).when(categoryEntityRepository).save(any(CategoryEntity.class));

                // When
                final String result = categoryEntityService.save(rootCreateDto);

                // Then
                Assertions.assertThat(result)
                        .isEqualTo(generatedPublicId);

                Assertions.assertThat(callCounter.get())
                        .isEqualTo(3);

                verify(categoryEntityRepository, times(3)).save(any(CategoryEntity.class));
            }

            @Test
            @DisplayName("Should trigger @Recover method after all retries fail")
            void shouldTriggerMethodAfterAllRetriesFail() {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                doThrow(DataIntegrityViolationException.class)
                        .when(categoryEntityRepository).save(any(CategoryEntity.class));

                // When
                // Then
                Assertions.assertThatThrownBy(() -> categoryEntityService.save(rootCreateDto))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Unable to save category, try again after");

                verify(categoryEntityRepository, times(3)).save(any(CategoryEntity.class));
            }

            @Test
            @DisplayName("Should not retry for EntityNotFoundException")
            void shouldNotRetryForEntityNotFoundException() {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        NanoIdUtils.randomNanoId()
                );

                // When
                // Then
                Assertions.assertThatThrownBy(() -> categoryEntityService.save(rootCreateDto))
                        .isInstanceOf(EntityNotFoundException.class);

                verify(categoryEntityRepository, never()).save(any(CategoryEntity.class));
            }
        }

        @Nested
        @DisplayName("Fallback tests")
        class FallbackTests {

            @Test
            @DisplayName("Should trigger getCategory() fallback")
            void shouldTriggerGetCategoryFallbackMethod() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                final String categoryId = categoryEntityService.save(rootCreateDto);

                doAnswer(inv ->
                        CompletableFuture.supplyAsync(
                                () -> Optional.of(
                                        new CategoryDto(
                                                NanoIdUtils.randomNanoId(),
                                                rootCreateDto.name(),
                                                rootCreateDto.description(),
                                                rootCreateDto.iconPath()
                                        )
                                ),
                                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                        )
                ).when(categoryEntityService).getCategory(categoryId);

                // When
                final CompletableFuture<Optional<CategoryWrapperDto>> resultFuture = categoryEntityService.getCategory(categoryId);
                final Optional<CategoryWrapperDto> result = resultFuture.get();

                // Then
                Assertions.assertThat(result)
                          .isEmpty();
            }
            @Test
            @DisplayName("Should trigger getCategories() fallback")
            void shouldTriggerGetCategoriesFallbackMethod() throws ExecutionException, InterruptedException {
                // Given
                final CategoryEntityCreateDto rootCreateDto = new CategoryEntityCreateDto(
                        FAKER.commerce().department(),
                        FAKER.lorem().sentence(),
                        FAKER.internet().url(),
                        null
                );

                final String categoryId = categoryEntityService.save(rootCreateDto);

                doAnswer(inv ->
                        CompletableFuture.supplyAsync(
                                () -> List.of(
                                        new CategoryWrapperDto(
                                                new CategoryDto(
                                                        NanoIdUtils.randomNanoId(),
                                                        rootCreateDto.name(),
                                                        rootCreateDto.description(),
                                                        rootCreateDto.iconPath()
                                                ),
                                                emptyList()
                                        )
                                ),
                                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                        )
                ).when(categoryEntityService).getCategories(categoryId);

                // When
                final CompletableFuture<List<CategoryWrapperDto>> resultFuture = categoryEntityService.getCategories(categoryId);
                final List<CategoryWrapperDto> result = resultFuture.get();

                // Then
                Assertions.assertThat(result)
                          .isEmpty();
            }
        }
    }
}