package pl.dawid0604.pcforum.category.service.commons.dto;

/**
 * <p>
 *     Auxiliary class containing constants such as. swagger etc.
 * </p>
 */
@SuppressWarnings("PMD.LongVariable")
final class Constants {

    /**
     * <p>
     *     Maximum {@code Name} field length.
     * </p>
     */
    static final int NAME_MAX_LENGTH = 128;

    /**
     * <p>
     *     Example of {@code Name} field.
     * </p>
     */
    static final String NAME_EXAMPLE_MESSAGE = "Processors";

    /**
     * <p>
     *     Maximum {@code Description} field length.
     * </p>
     */
    static final int DESCRIPTION_MAX_LENGTH = 255;

    /**
     * <p>
     *     Example of {@code Description} field.
     * </p>
     */
    static final String DESCRIPTION_EXAMPLE_MESSAGE = "lorem ipsum";

    /**
     * <p>
     *     NanoId regex field.
     * </p>
     */
    static final String NANO_ID_REGEX = "^[A-Za-z0-9_-]{21}$";

    /**
     * <p>
     *     Example of NanoId field.
     * </p>
     */
    static final String NANO_ID_EXAMPLE_MESSAGE = "Epvc81_boiyhn5_d5dfXp";

    /**
     * <p>
     *     Field {@code IconPath} regex.
     * </p>
     */
    static final String ICON_PATH_REGEX = "^https?://\\S+\\.(svg|png|jpe?g|webp)$";

    /**
     * <p>
     *     Example of {@code IconPath} field.
     * </p>
     */
    static final String ICON_PATH_EXAMPLE = "https://xyz.com/images/category.svg";

    /**
     * <p>
     *     Field {@code PublicId} message when it's blank.
     * </p>
     */
    static final String PUBLIC_ID_NOT_BLANK_MESSAGE = "PublicId cannot be blank";

    /**
     * <p>
     *     Field {@code PublicId} message when it's null.
     * </p>
     */
    static final String PUBLIC_ID_NOT_NULL_MESSAGE = "PublicId cannot be null";

    /**
     * <p>
     *     Field {@code PublicId} message when it's not NanoId.
     * </p>
     */
    static final String PUBLIC_ID_PATTERN_MESSAGE = "PublicId must be a valid NanoId";

    /**
     * <p>
     *     Field {@code PublicId} schema description.
     * </p>
     */
    static final String PUBLIC_ID_SCHEMA_DESCRIPTION =
            "Unique PublicId of the category (NanoId format)";

    /**
     * <p>
     *     Field {@code Name} message when it's blank.
     * </p>
     */
    static final String NAME_NOT_BLANK_MESSAGE = "Name cannot be blank";

    /**
     * <p>
     *     Field {@code Name} message when it's null.
     * </p>
     */
    static final String NAME_NOT_NULL_MESSAGE = "Name cannot be null";

    /**
     * <p>
     *     Field {@code Name} schema description.
     * </p>
     */
    static final String NAME_SCHEMA_DESCRIPTION = "Name of the category";

    /**
     * <p>
     *     Field {@code Name} message when it's greater than {@link #NAME_MAX_LENGTH} characters.
     * </p>
     */
    static final String NAME_SIZE_MESSAGE =
            "Name cannot be greater than " + NAME_MAX_LENGTH + " characters";

    /**
     * <p>
     *     Field {@code Description} message when it's greater than
     *     {@link #DESCRIPTION_MAX_LENGTH} characters.
     * </p>
     */
    static final String DESCRIPTION_SIZE_MESSAGE =
            "Description cannot be greater than " + DESCRIPTION_MAX_LENGTH + " characters";

    /**
     * <p>
     *     Field {@code Description} schema description.
     * </p>
     */
    static final String DESCRIPTION_SCHEMA_MESSAGE = "Description of the category";

    /**
     * <p>
     *     Field {@code IconPath} message when it's invalid.
     * </p>
     */
    static final String ICON_PATH_PATTERN_MESSAGE =
            "Icon path must be a valid absolute path to image";

    /**
     * <p>
     *     Field {@code IconPath} schema description.
     * </p>
     */
    static final String ICON_PATH_SCHEMA_DESCRIPTION =
            "Absolute path to the category icon image";

    /**
     * <p>
     *     Field {@code ParentId} message when it's invalid.
     * </p>
     */
    static final String PARENT_ID_PATTERN_MESSAGE = "ParentId must be a valid NanoId";


    /**
     * <p>
     *     Field {@code ParentId} schema description.
     * </p>
     */
    static final String PARENT_ID_SCHEMA_DESCRIPTION =
            "Unique ParentId of the category (NanoId format)";

    /**
     * <p>
     *     Instantiation of this class is prohibited.
     * </p>
     */
    private Constants() { }
}
