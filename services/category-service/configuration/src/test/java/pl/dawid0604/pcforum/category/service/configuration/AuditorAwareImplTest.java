package pl.dawid0604.pcforum.category.service.configuration;

import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static lombok.AccessLevel.PACKAGE;

@ExtendWith(MockitoExtension.class)
@NoArgsConstructor(access = PACKAGE)
class AuditorAwareImplTest {

    @Spy
    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @Nested
    @DisplayName("Success")
    @NoArgsConstructor(access = PACKAGE)
    class Success {

        @Test
        void shouldReturnEmptyAuditor() {
            // Given
            // When
            // Then
            Assertions.assertThat(auditorAware.getCurrentAuditor())
                      .isEmpty();
        }
    }
}
