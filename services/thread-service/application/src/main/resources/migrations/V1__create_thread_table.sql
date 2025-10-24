BEGIN;

CREATE SEQUENCE IF NOT EXISTS thread_id_sequence
    START WITH 1
    INCREMENT BY 1
    CACHE 20;

CREATE TABLE IF NOT EXISTS threads(
    id                  BIGINT          NOT NULL    DEFAULT nextval('thread_id_sequence'),
    public_id           VARCHAR(21)     NOT NULL,
    title               VARCHAR(128)    NOT NULL,
    content             TEXT            NOT NULL,
    category_id         VARCHAR(21)     NOT NULL,
    user_id             VARCHAR(21)     NOT NULL,
    number_of_views     bigint          NOT NULL    DEFAULT 0,
    status              VARCHAR(32),
    created_by          VARCHAR(32),
    modified_by         VARCHAR(32),
    created_date        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    last_modified_date  TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_threads
        PRIMARY KEY (id),

    CONSTRAINT uk_public_id
        UNIQUE (public_id),

     CONSTRAINT ck_public_id_format
        CHECK (public_id ~ '^[A-Za-z0-9_-]{21}$'),

     CONSTRAINT ck_category_id_format
        CHECK (category_id ~ '^[A-Za-z0-9_-]{21}$'),

     CONSTRAINT ck_user_id_format
        CHECK (user_id ~ '^[A-Za-z0-9_-]{21}$'),

     CONSTRAINT ck_title_length
        CHECK (LENGTH(TRIM(title)) > 0),

     CONSTRAINT ck_content_length
        CHECK (LENGTH(TRIM(content)) > 0),

     CONSTRAINT ck_number_of_views_length
        CHECK (number_of_views >= 0)
);

CREATE INDEX IF NOT EXISTS idx_user_id
    ON threads (user_id, status, created_date DESC)
    INCLUDE (
        id, public_id, title,
        status, number_of_views, created_date
    );

CREATE INDEX IF NOT EXISTS idx_category_id
    ON threads (category_id, status, created_date DESC)
    INCLUDE (
        id, public_id, title, user_id,
        status, number_of_views, created_date
    );

CREATE INDEX IF NOT EXISTS idx_public_id
    ON threads (public_id)
    INCLUDE (
        id, title, content, user_id,
        status, number_of_views, created_date
    );

---------------------------------------------
CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE threads
ADD COLUMN IF NOT EXISTS search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('simple', coalesce(title, '')), 'A')   ||
    setweight(to_tsvector('simple', coalesce(content, '')), 'B')
) STORED;

CREATE INDEX IF NOT EXISTS idx_threads_fts
    ON threads USING GIN (search_vector);

CREATE INDEX IF NOT EXISTS idx_title_trgm
    ON threads USING GIN (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_content_trgm
    ON threads USING GIN (content gin_trgm_ops);

CREATE OR REPLACE FUNCTION match_by_title_content(search_query TEXT)
RETURNS TABLE (
    thread_id               BIGINT,
    thread_public_id        VARCHAR(21),
    thread_title            VARCHAR(128),
    thread_user_id          VARCHAR(21),
    thread_content          TEXT,
    thread_status           VARCHAR(32),
    thread_created_date     TIMESTAMP,
    total_count             BIGINT
) AS $$
DECLARE
   search_query_length INT;
BEGIN
    search_query_length := LENGTH(TRIM(search_query));

    IF search_query IS NULL OR search_query_length < 2 THEN
        RETURN;
    END IF;

    RETURN QUERY
    SELECT
        t.id,
        t.public_id,
        t.title,
        t.user_id,
        t.content,
        t.status,
        t.created_date,
        COUNT(*) OVER() AS total_count
     FROM
        threads t,
        websearch_to_tsquery('simple', search_query) AS query
     WHERE
        t.search_vector @@ query
     ORDER BY
        ts_rank(t.search_vector, query) DESC,
        t.created_date DESC;

     IF NOT FOUND AND search_query_length >= 3 THEN
        RETURN QUERY
        SELECT
            t.id,
            t.public_id,
            t.title,
            t.user_id,
            t.content,
            t.status,
            t.created_date,
            COUNT(*) OVER() AS total_count
         FROM
            threads t
         WHERE
            t.title % search_query OR
            t.content % search_query
         ORDER BY
            GREATEST(
                similarity(t.title, search_query),
                similarity(t.content, search_query)
            ) DESC,
            t.created_date DESC;
      END IF;
END;
$$ LANGUAGE plpgsql STABLE;

COMMIT;