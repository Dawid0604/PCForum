package pl.dawid0604.pcforum.thread.service.commons.request;

import java.time.Instant;

public interface RequestContext {

    /**
     * @return identifier of the current request.
     */
    String getRequestId();

    /**
     * @return username of the current request authenticated user
     */
    String getUsername();

    /**
     * @return timestamp of the current request
     */
    Instant getTimestamp();

    /**
     * @return idempotency key of the current request
     */
    String getIdempotencyKey();

    /**
     * @return {@code true} when current request user is authenticated
     * otherwise {@code false}
     */
    boolean isAuthenticated();

    /**
     * @return {@code true} when current request user containing
     * {@code ADMIN_ROLE} otherwise false
     */
    boolean hasAdminRole();

    /**
     * @return {@code true} when current request user containing
     * {@code MODERATOR_ROLE} otherwise false
     */
    boolean hasModeratorRole();
}
