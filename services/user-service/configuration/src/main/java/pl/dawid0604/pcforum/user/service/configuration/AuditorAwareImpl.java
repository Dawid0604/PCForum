package pl.dawid0604.pcforum.user.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;

/**
 * Implementation of the {@link AuditorAware} interface
 * providing information about the current auditor (e.g., logged user).
 *
 * <p>
 *     The current implementation returns empty response {@link Optional},
 *     which means that information about the auditor is unavailable.
 * </p>
 *
 * <b>This component is prepared for further development</b>
 * @see AuditorAware
 */
@Component("auditorAwareImpl")
@NoArgsConstructor(access = PACKAGE)
@SuppressWarnings("PMD.CommentSize")
class AuditorAwareImpl implements AuditorAware<String> {

    /**
     * Returns the identifier of the current auditor (e.g., username).
     * @return empty {@link Optional}, since auditor retrieval is
     * not yet completed.
     */
    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.empty();
    }
}
