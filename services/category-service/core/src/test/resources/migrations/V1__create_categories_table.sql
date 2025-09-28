BEGIN;
CREATE SEQUENCE IF NOT EXISTS category_id_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE categories(
    id BIGINT NOT NULL DEFAULT nextval('category_id_sequence'),
    public_id VARCHAR(21) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    icon_path VARCHAR(512),
    parent_id BIGINT,
    created_by varchar(32),
    modified_by varchar(32),
    created_date timestamp,
    last_modified_date timestamp,

    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_parent_id_category_name UNIQUE (parent_id, name),
    CONSTRAINT uk_public_id UNIQUE (public_id),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id)
        REFERENCES categories (id) ON DELETE SET NULL
);

CREATE INDEX idx_categories_parent_id ON categories (parent_id);
CREATE INDEX idx_public_id ON categories (public_id);

COMMIT;