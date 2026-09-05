
-- Cria a tabela de notícias.
-- Rodar em bancos já existentes. Bancos novos já saem prontos pelo docker-initial-sql.sql.
-- O Hibernate (ddl-auto: update) cria a tabela automaticamente,
-- mas esta migration garante consistência em ambientes de produção.

CREATE TABLE IF NOT EXISTS public.news (
    id          uuid         NOT NULL,
    title       varchar(255) NOT NULL,
    content     text         NOT NULL,
    image_url   text         NULL,
    author_id   uuid         NOT NULL,
    created_at  timestamp(6) NOT NULL,
    updated_at  timestamp(6) NULL,
    CONSTRAINT news_pkey PRIMARY KEY (id),
    CONSTRAINT fk_news_author FOREIGN KEY (author_id) REFERENCES public.users (id)
);

CREATE INDEX IF NOT EXISTS idx_news_created_at ON public.news (created_at DESC);
