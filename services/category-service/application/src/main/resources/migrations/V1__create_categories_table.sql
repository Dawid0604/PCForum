BEGIN;

CREATE SEQUENCE IF NOT EXISTS category_id_sequence
       START WITH 1
       INCREMENT BY 1
       CACHE 20;

CREATE TABLE IF NOT EXISTS categories(
    id                  bigint          NOT NULL    DEFAULT nextval('category_id_sequence'),
    public_id           VARCHAR(21)     NOT NULL,
    name                VARCHAR(128)    NOT NULL,
    description         VARCHAR(255),
    icon_path           VARCHAR(512),
    parent_id           BIGINT,
    created_by          VARCHAR(32),
    modified_by         VARCHAR(32),
    created_date        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    last_modified_date  TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_categories
        PRIMARY KEY (id),

    CONSTRAINT uk_public_id
        UNIQUE (public_id),

    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id)
        REFERENCES categories (id)
        ON DELETE CASCADE,

     CONSTRAINT ck_public_id_format
        CHECK (public_id ~ '^[A-Za-z0-9_-]{21}$'),

     CONSTRAINT ck_name_length
        CHECK (LENGTH(TRIM(name)) > 0),

     CONSTRAINT ck_icon_path_format
        CHECK (icon_path IS NULL OR icon_path ~* '^https?://.+'),

     CONSTRAINT ck_no_self_reference
        CHECK (id != parent_id)
);

CREATE INDEX IF NOT EXISTS idx_categories_parent_id
    ON categories (parent_id)
    INCLUDE (id, public_id, name, icon_path, description);

CREATE UNIQUE INDEX IF NOT EXISTS uk_name_with_parent_id
    ON categories (parent_id, name)
    WHERE parent_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_name_without_parent_id
    ON categories (name)
    WHERE parent_id IS NULL;

COMMIT;