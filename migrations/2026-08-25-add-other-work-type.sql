-- Adiciona a categoria geral "Other" para obras que nao se encaixam nas demais.
-- Rodar em bancos ja existentes. Bancos novos ja saem prontos pelo docker-initial-sql.sql.
-- A tabela other_work tambem e criada pelo Hibernate (ddl-auto: update), mas o CHECK
-- abaixo precisa ser alterado na mao, senao o insert com type='Other' e rejeitado.

ALTER TABLE public.obras DROP CONSTRAINT IF EXISTS obras_type_check;

ALTER TABLE public.obras ADD CONSTRAINT obras_type_check
    CHECK (type IN ('LibraLiterature', 'Multimedia', 'Poem', 'Article', 'Cordel',
                    'Essay', 'ShortStory', 'Tale', 'Art', 'Infographic', 'Other'));

CREATE TABLE IF NOT EXISTS public.other_work
(
    id        uuid NOT NULL,
    "content" text NULL,
    url       text NULL,
    image_url text NULL,
    CONSTRAINT other_work_pkey PRIMARY KEY (id),
    CONSTRAINT fk_other_work_on_id FOREIGN KEY (id) REFERENCES public.obras (id)
);
