package pl.dawid0604.pcforum.category.service.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.OAUTH2;

/**
 * <p>
 *     Configuration class to configure OpenAPI 3.0 for
 *     Category Service API documentation.
 * </p>
 * @see OpenAPI
 */
@Configuration
@SuppressWarnings("unused")
class SwaggerConfig {

    /**
     * <p>
     *     OAuth 2.0 issuer URI for Keycloak provider integration.
     *     Used to construct authorization and token endpoints.
     * </p>
     */
    private final String issuerUri;

    /**
     * <p>
     *     Gateway Microservice URL to ensure proper service communicate.
     * </p>
     */
    private final String gatewayServiceUrl;

    /**
     * <p>
     *     The project owner name displayed in API documentation
     *     contact information.
     * </p>
     */
    private final String projectOwner;

    /**
     * <p>
     *     The project owner URL website displayed in API documentation
     *     contact information.
     * </p>
     */
    private final String projectOwnerUrl;

    /**
     * <p>
     *     Constructs Swagger configuration with externalized properties.
     * </p>
     * @param incomingIssuerUri OAuth 2.0 issuer URI from {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}
     * @param incomingProjectOwner project owner name from {@code custom.swagger.projectOwner}
     * @param incomingProjectOwnerUrl project owner URL from {@code custom.swagger.projectOwnerUrl}
     * @param incomingGatewayServiceUrl API gateway URL from {@code custom.gatewayServiceUrl}
     * @apiNote Constructor injection enables immutable configuration and proper dependency management
     */
    SwaggerConfig(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            final String incomingIssuerUri,

            @Value("${custom.swagger.projectOwner}")
            final String incomingProjectOwner,

            @Value("${custom.swagger.projectOwnerUrl}")
            final String incomingProjectOwnerUrl,

            @Value("${custom.gatewayServiceUrl}")
            final String incomingGatewayServiceUrl) {

        this.issuerUri = incomingIssuerUri;
        this.projectOwner = incomingProjectOwner;
        this.projectOwnerUrl = incomingProjectOwnerUrl;
        this.gatewayServiceUrl = incomingGatewayServiceUrl;
    }

    /**
     * <p>
     *     Creates and configures the {@link OpenAPI} {@link Bean}.
     * </p>
     * @return configures {@link OpenAPI} specification for Category Service.
     * @see #getInfo()
     * @see #getSecurityItem()
     * @see #getServers()
     * @see #getComponents()
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(getInfo())
                            .addSecurityItem(getSecurityItem())
                            .servers(getServers())
                            .components(getComponents());
    }

    /**
     * <p>
     *     Configures server definitions for client communication routing.
     * </p>
     *
     * <p>
     *     Defines server endpoints that Swagger UI and API clients should
     *     use for making requests. Routes only through Gateway Service.
     * </p>
     * @return list of {@link Server} for API Gateway routing.
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private List<Server> getServers() {
        return List.of(
                new Server().url(gatewayServiceUrl + "/category")
        );
    }

    /**
     * <p>
     *     Creates API metadata and contact information for documentation header.
     * </p>
     * @return {@link Info} object with proper data.
     * @see #getContact()
     */
    private Info getInfo() {
        return new Info().title("Category Service APIs")
                         .description("Category Service")
                         .contact(getContact());
    }

    /**
     * <p>
     *     Provides project contact information for API documentation.
     * </p>
     * @return {@link Contact} object with proper owner details.
     */
    private Contact getContact() {
        return new Contact().name(projectOwner)
                            .url(projectOwnerUrl);
    }

    /**
     * <p>
     *     Defines reusable security schemes for consistent authentication
     *     patterns.
     * </p>
     *
     * <p>
     *     Establishes named security scheme ({@code security_auth}) that can be
     *     referenced by controller annotations throughout the application. This
     *     promotes consistency and reduces duplication in security documentation.
     * </p>
     * @return {@link Components} object containing security scheme definitions.
     * @see #getSecurityScheme()
     */
    private Components getComponents() {
        return new Components().addSecuritySchemes("security_auth", getSecurityScheme());
    }

    /**
     * <p>
     *     Configures OAuth 2.0 security scheme for interactive authentication testing.
     * </p>
     *
     * <p>
     *     Enables Swagger UI users to authenticate through OAuth 2.0 Authorization Code
     *     flow, allowing direct API testing from documentation interface with proper
     *     JWT token and automatic header injection.
     * </p>
     * @return {@link SecurityScheme} configured for OAuth 2.0 with Authorization code flow.
     * @see #getSecurityFlows()
     */
    private SecurityScheme getSecurityScheme() {
        return new SecurityScheme().in(HEADER)
                                   .type(OAUTH2)
                                   .flows(getSecurityFlows());
    }

    /**
     * <p>
     *     Creates global security requirement for all API endpoints.
     * </p>
     * @return mandating Authorization header.
     */
    @SuppressWarnings("PMD.LooseCoupling")
    private SecurityRequirement getSecurityItem() {
        return new SecurityRequirement().addList("Authorization");
    }

    /**
     * <p>
     *     Defines OAuth 2.0 Authorization Code flow configuration.
     * </p>
     *
     * <p>
     *     Configures complete OAuth 2.0 flow with authorization and
     *     token URLs derived from issuer URI. Supports standard
     *     OpenID Connect discovery for Keycloak.
     * </p>
     * @return {@link OAuthFlows} with Authorization Code flow configuration.
     * @see #getAuthorizationUrl()
     * @see #getTokenUrl()
     * @see #getFlowScopes()
     */
    private OAuthFlows getSecurityFlows() {
        return new OAuthFlows().authorizationCode(
                new OAuthFlow().authorizationUrl(getAuthorizationUrl())
                               .tokenUrl(getTokenUrl())
                               .scopes(getFlowScopes())
        );
    }

    /**
     * <p>
     *     Defines OAuth 2.0 scopes required for API access.
     * </p>
     * @return {@link Scopes} containing OpenID Connect scope requirements.
     */
    @SuppressWarnings("PMD.LooseCoupling")
    private Scopes getFlowScopes() {
        return new Scopes().addString("openid", "openid scope");
    }

    /**
     * <p>
     *     Constructs OAuth 2.0 token endpoint URL from issuer URI.
     * </p>
     * @return complete token endpoint URL for OAuth 2.0 flow.
     */
    private String getTokenUrl() {
        return issuerUri + "/protocol/openid-connect/token";
    }

    /**
     * <p>
     *     Constructs OAuth 2.0 authorization endpoint URL from issuer URI.
     * </p>
     * @return complete authorization endpoint URL for OAuth 2.0 flow.
     */
    private String getAuthorizationUrl() {
        return issuerUri + "/protocol/openid-connect/auth";
    }
}
