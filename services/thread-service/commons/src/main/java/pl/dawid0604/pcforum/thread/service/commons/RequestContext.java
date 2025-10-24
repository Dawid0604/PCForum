package pl.dawid0604.pcforum.thread.service.commons;

import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

@ToString
@Component
@RequestScope
public class RequestContext {

    @Getter
    private final String requestId = UUID.randomUUID()
                                         .toString();

    @Getter
    private String username;

    @Getter
    private Instant requestTime;

    private List<String> roles = new ArrayList<>();

    @Getter
    private String idempotencyKey;

    private Boolean isAuthenticated;

    public void setUsername(final String username) {
        if(this.username == null) {
            this.username = username;
        }
    }

    public void setIdempotencyKey(final String idempotencyKey) {
        if(this.idempotencyKey == null) {
            this.idempotencyKey = idempotencyKey;
        }
    }

    public void setRequestTime(final Instant requestTime) {
        if(this.requestTime == null) {
            this.requestTime = requestTime;
        }
    }

    public void setRoles(final List<String> roles) {
        if(isEmpty(this.roles) && roles != null) {
            this.roles = new ArrayList<>(roles);
        }
    }

    public void setAuthenticated(final boolean authenticated) {
        if(this.isAuthenticated == null) {
            isAuthenticated = authenticated;
        }
    }

    public boolean isAuthenticated() {
        return Boolean.TRUE.equals(isAuthenticated);
    }

    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public boolean hasAdminRole() {
        return roles.stream()
                    .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ADMIN"));
    }

    public boolean hasModeratorRole() {
        return roles.stream()
                    .anyMatch(r -> r.equals("ROLE_MODERATOR") || r.equals("MODERATOR"));
    }
}
