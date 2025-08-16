package pl.dawid0604.pcforum.gateway.service.configuration;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages= "pl.dawid0604.pcforum.gateway.service.configuration")
class ArchitectureTest {
    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("pl.dawid0604.pcforum.gateway.service.configuration");

    @Nested
    @DisplayName("Naming conventions")
    class NamingConventions {

        @Test
        @DisplayName("Service classes should end with 'Service' phrase")
        void serviceClassesShouldEndWithProperPhrase() {
            // Given
            // When
            // Then
            classes().that()
                     .areAnnotatedWith(Service.class)
                     .should().haveSimpleNameEndingWith("Service")
                     .check(importedClasses);
        }

        @Test
        @DisplayName("Configuration classes should end with 'Config' phrase")
        void configurationClassesShouldEndWithProperPhrase() {
            // Given
            // When
            // Then
            classes().that()
                     .areAnnotatedWith(Configuration.class)
                     .should().haveSimpleNameEndingWith("Config")
                     .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Annotations")
    class Annotations {

        @Test
        @DisplayName("Cache methods should be annotated with @Cacheable")
        void cacheMethodsShouldBeProperAnnotated() {
            // Given
            // When
            // Then
            methods().that()
                     .haveName("getRouteDefinitions")
                     .should().beAnnotatedWith(Cacheable.class)
                     .check(importedClasses);
        }

        @Test
        @DisplayName("Service classes should be annotated with @Service")
        void serviceClassesShouldBeProperAnnotated() {
            // Given
            // When
            // Then
            classes().that()
                     .haveSimpleNameContaining("Service")
                     .should().beAnnotatedWith(Service.class)
                     .check(importedClasses);
        }

        @Test
        @DisplayName("Config classes should be annotated with @Service")
        void configurationClassesShouldBeProperAnnotated() {
            // Given
            // When
            // Then
            classes().that()
                     .haveSimpleNameContaining("Config")
                     .should().beAnnotatedWith(Configuration.class)
                     .check(importedClasses);
        }

        @Test
        @DisplayName("Classes that should not be annotated by any Spring annotations")
        void classesThatShouldNotBeAnnotatedByAnySpringAnnotations() {
            // Given
            // When
            // Then
            classes().that()
                     .haveSimpleName("Constants")
                     .should().onlyAccessClassesThat(JavaClass.Predicates.resideOutsideOfPackage("org.springframework"))
                     .check(importedClasses);

            classes().that()
                     .haveSimpleName("SwaggerMapper")
                     .should().onlyAccessClassesThat(JavaClass.Predicates.resideOutsideOfPackage("org.springframework"))
                     .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Dependencies")
    class Dependencies {

        @Test
        @DisplayName("No cycles in package dependencies")
        void noPackageCycles() {
            // Given
            // When
            // Then
            slices().matching("pl.dawid0604.pcforum.gateway.service.(*)..")
                    .should().beFreeOfCycles()
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Class structure")
    class ClassStructure {

        @Test
        @DisplayName("Classes should be final")
        void classesShouldBeFinal() {
            // Given
            // When
            // Then
            classes().that()
                    .haveSimpleNameEndingWith("Mapper")
                    .or().haveSimpleName("Constants")
                    .should().haveModifier(JavaModifier.FINAL)
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Classes should be package-private")
        void classesShouldBePackagePrivate() {
            // Given
            // When
            // Then
            classes().that()
                    .haveSimpleName("CacheConfig")
                    .or().haveSimpleName("Constants")
                    .or().haveSimpleName("SchedulerConfig")
                    .or().haveSimpleName("SwaggerConfig")
                    .or().haveSimpleName("SwaggerMapper")
                    .or().haveSimpleName("SwaggerRouteService")
                    .should().bePackagePrivate()
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Classes should have private constructors")
        void classesShouldHavePrivateConstructors() {
            // Given
            // When
            // Then
            classes().that()
                    .haveSimpleName("Constants")
                    .or().haveSimpleName("SwaggerMapper")
                    .should().haveOnlyPrivateConstructors()
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Classes should have package-private constructors")
        void classesShouldHavePackagePrivateConstructors() {
            // Given
            // When
            // Then
            constructors().that()
                          .areDeclaredInClassesThat().haveSimpleName("CacheConfig")
                          .or().areDeclaredInClassesThat().haveSimpleName("SchedulerConfig")
                          .or().areDeclaredInClassesThat().haveSimpleName("SwaggerConfig")
                          .or().areDeclaredInClassesThat().haveSimpleName("SwaggerRouteService")
                          .should().notHaveModifier(JavaModifier.PRIVATE)
                          .andShould().notHaveModifier(JavaModifier.PUBLIC)
                          .andShould().notHaveModifier(JavaModifier.PROTECTED)
                          .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Field rules")
    class FieldRules {

        @Test
        @DisplayName("Fields should be final where possible")
        void fieldsShouldBeFinalWherePossible() {
           // Given
           // When
           // Then
           fields().that()
                   .areDeclaredInClassesThat().areAnnotatedWith(Service.class)
                   .should().beFinal()
                   .check(importedClasses);
        }

        @Test
        @DisplayName("Static fields should be final")
        void staticFieldsShouldBeFinal() {
            // Given
            // When
            // Then
            fields().that()
                    .areStatic()
                    .should().beFinal()
                    .check(importedClasses);
        }
    }
}
