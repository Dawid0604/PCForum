package pl.dawid0604.pcforum.thread.service.core.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import pl.dawid0604.pcforum.thread.service.commons.RequestContext;
import pl.dawid0604.pcforum.thread.service.commons.annotation.RequireCreationPermission;
import pl.dawid0604.pcforum.thread.service.commons.annotation.RequireDeletePermission;
import pl.dawid0604.pcforum.thread.service.commons.annotation.RequireEditPermission;
import pl.dawid0604.pcforum.thread.service.commons.exception.DeniedPermissionException;
import pl.dawid0604.pcforum.thread.service.persistence.repository.ThreadEntityRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static java.time.temporal.ChronoUnit.MINUTES;
import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor(access = PACKAGE)
class RequirePermissionAspect {
    private final ThreadEntityRepository threadEntityRepository;
    private final RequestContext requestContext;

    @Before("@annotation(requireCreationPermission)")
    public void checkCreationPermission(final RequireCreationPermission requireCreationPermission) {
        if(isBypassingPermissionChecks()) {
            return;
        }

        checkTimeBasedPermission(
                () -> threadEntityRepository.findLastThreadCreationTime(requestContext.getUsername()),
                requireCreationPermission.interval(),
                "Interval between thread creation duration is %d minutes, try again later".formatted(
                        requireCreationPermission.interval()
                )
        );
    }

    @Before("@annotation(requireEditPermission)")
    public void checkEditPermission(final RequireEditPermission requireEditPermission) {
        if(isBypassingPermissionChecks()) {
            return;
        }

        checkTimeBasedPermission(
                () -> threadEntityRepository.findLastThreadModifiedTime(requestContext.getUsername()),
                requireEditPermission.interval(),
                "Interval between thread edit duration is %d minutes, try again later".formatted(
                        requireEditPermission.interval()
                )
        );

        checkNoPostsExist(
                () -> 0,
                "Thread with posts cannot be edit"
        );
    }

    @Before("@annotation(requireDeletePermission)")
    public void checkDeletePermission(

            @SuppressWarnings("unused")
            final RequireDeletePermission requireDeletePermission) {

        if(isBypassingPermissionChecks()) {
            return;
        }

        checkNoPostsExist(
                () -> 0,
                "Thread with posts cannot be removed"
        );
    }

    private boolean isBypassingPermissionChecks() {
        return requestContext.hasAdminRole() || requestContext.hasModeratorRole();
    }

    private void checkNoPostsExist(final LongSupplier numberOfPostsSupplier, final String message) {
        // OpenFeign to fetch value - temporary solution because post service is not implemented currently
        if(numberOfPostsSupplier.getAsLong() > 0) {
            throw new DeniedPermissionException(message);
        }
    }

    private void checkTimeBasedPermission(final Supplier<Optional<Instant>> lastActionTimeSupplier,
                                          final int requiredIntervalInMinutes,
                                          final String message) {

        final Optional<Instant> possibleDateTime = lastActionTimeSupplier.get();

        if(possibleDateTime.isPresent()) {
            final long diff = MINUTES.between(possibleDateTime.get(), Instant.now()) - 120;

            if(diff < requiredIntervalInMinutes) {
                throw new DeniedPermissionException(message);
            }
        }
    }
}
