package pl.dawid0604.pcforum.frontend.service.configuration;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static lombok.AccessLevel.PACKAGE;

@NoArgsConstructor(access = PACKAGE)
@AnalyzeClasses(packages= "pl.dawid0604.pcforum.frontend.service.configuration")
class ArchitectureTest {
    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("pl.dawid0604.pcforum.frontend.service.configuration");

    @Nested
    @DisplayName("Naming conventions")
    class NamingConventions {

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
        @DisplayName("Config classes should be annotated with @Configuration")
        void configurationClassesShouldBeProperAnnotated() {
            // Given
            // When
            // Then
            classes().that()
                     .haveSimpleNameContaining("Config")
                     .should().beAnnotatedWith(Configuration.class)
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
            slices().matching("pl.dawid0604.pcforum.frontend.service.(*)..")
                    .should().beFreeOfCycles()
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Class structure")
    class ClassStructure {

        @Test
        @DisplayName("Classes should be package-private")
        void classesShouldBePackagePrivate() {
            // Given
            // When
            // Then
            classes().that()
                    .haveSimpleName("CacheConfig")
                    .should().bePackagePrivate()
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
