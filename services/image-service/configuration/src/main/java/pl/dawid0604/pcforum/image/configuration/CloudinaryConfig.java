package pl.dawid0604.pcforum.image.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static lombok.AccessLevel.PACKAGE;

/**
 * Spring configuration class for setting up the Cloudinary
 * client bean.
 *
 * <p>
 *     This configuration reads Cloudinary credentials from
 *     application properties and creates a {@link Cloudinary}
 *     bean that can be injected and used throughout the
 *     application to upload and manage images on the Cloudinary
 *     platform.
 * </p>
 */
@Configuration
@NoArgsConstructor(access = PACKAGE)
@SuppressWarnings({ "unused", "PMD.CommentDefaultAccessModifier" })
class CloudinaryConfig {

    /**
     * Cloudinary cloud name.
     */
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    /**
     * Cloudinary API key.
     */
    @Value("${cloudinary.api-key}")
    private String apiKey;

    /**
     * Cloudinary API secret.
     */
    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    /**
     * Creates and configures a Cloudinary instance.
     * @return a configured {@link Cloudinary} bean
     */
    @Bean
    Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}
