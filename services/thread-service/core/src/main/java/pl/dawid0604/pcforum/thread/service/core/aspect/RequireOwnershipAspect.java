package pl.dawid0604.pcforum.thread.service.core.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;
import pl.dawid0604.pcforum.thread.service.commons.RequestContext;
import pl.dawid0604.pcforum.thread.service.commons.annotation.RequireOwnership;
import pl.dawid0604.pcforum.thread.service.persistence.repository.ThreadEntityRepository;

import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor(access = PACKAGE)
class RequireOwnershipAspect {
    private final ThreadEntityRepository threadEntityRepository;
    private final RequestContext requestContext;

    @Before("@annotation(requireOwnership)")
    public void checkOwnership(final JoinPoint joinPoint, final RequireOwnership requireOwnership) {
        if(!requestContext.isAuthenticated()) {
            return;
        }

        if((requireOwnership.allowAdmin()     && requestContext.hasAdminRole()) ||
           (requireOwnership.allowModerator() && requestContext.hasModeratorRole())) {

            return;
        }

        final String username = requestContext.getUsername();
        final String resourceId = extractResourceId(joinPoint, requireOwnership.identifier())
                                    .orElseThrow();

        if(!threadEntityRepository.existsByPublicIdAndUserId(resourceId, username)) {
            throw new AuthorizationDeniedException(requireOwnership.message());
        }
    }

    private Optional<String> extractResourceId(final JoinPoint joinPoint, final String identifierFieldName) {
        final Object[] args = joinPoint.getArgs();

        for(final Object arg: args) {
            try {
                final BeanWrapper wrapper = new BeanWrapperImpl(arg);

                if(wrapper.isReadableProperty(identifierFieldName)) {
                    final Object value = wrapper.getPropertyValue(identifierFieldName);

                    if(value != null) {
                        return Optional.of(value.toString());
                    }

                } else if(arg instanceof String str && isNotBlank(str)) {
                   return Optional.of(str);
                }

            } catch (Exception e) {
                log.debug("Property {} not found on {}", identifierFieldName, arg.getClass().getSimpleName());
            }
        }

        return Optional.empty();
    }
}

