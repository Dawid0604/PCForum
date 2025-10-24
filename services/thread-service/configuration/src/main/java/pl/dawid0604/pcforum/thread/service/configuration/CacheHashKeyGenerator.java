package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

@Slf4j
@Component
class CacheHashKeyGenerator implements KeyGenerator {
    private static final String DELIMITER = "_";
    private static final String HASH_ALGORITHM = "MD5";

    @NonNull
    @Override
    public Object generate(

            @NonNull
            final Object target,

            @NonNull
            final Method method,

            @NonNull
            final Object... params) {

        if(ArrayUtils.isEmpty(params)) {
            throw new IllegalArgumentException("Params not present");
        }

        try {
            final String combinedParams = Arrays.stream(params)
                                                .map(CacheHashKeyGenerator::paramToString)
                                                .collect(joining(DELIMITER));

            final MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            final byte[] hashBytes = md.digest(combinedParams.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                         .encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            log.warn("{} algorithm not available, falling back to Objects.hash", HASH_ALGORITHM, e);
            return Objects.hash(params);
        }
    }

    private static String paramToString(final Object param) {
        if (param instanceof Object[] array) {
            return Arrays.deepToString(array);
        }
        if (param instanceof int[] array) {
            return Arrays.toString(array);
        }
        if (param instanceof long[] array) {
            return Arrays.toString(array);
        }
        if (param instanceof double[] array) {
            return Arrays.toString(array);
        }
        if (param instanceof boolean[] array) {
            return Arrays.toString(array);
        }
        if (param instanceof byte[] array) {
            return Arrays.toString(array);
        }
        if (param instanceof short[] array) {
            return Arrays.toString(array);
        }
        if (param instanceof float[] array) {
            return Arrays.toString(array);
        }
        if (param instanceof char[] array) {
            return Arrays.toString(array);
        }

        return param.toString();
    }
}
