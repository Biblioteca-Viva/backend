CREATE TABLE public.users
(
    id             uuid         NOT NULL,
    email          varchar(255) NOT NULL,
    "name"         varchar(255) NOT NULL,
    "password"     varchar(255) NOT NULL,
    session_version bigint       DEFAULT 0 NOT NULL,
    "role"         varchar(255) NOT NULL,
    account_status varchar(255) NOT NULL,
    CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY (ARRAY[('CURADOR':: character varying)::text, ('ALUNO':: character varying)::text, ('ADMIN':: character varying)::text])
        ) )
);

CREATE TABLE public.password_reset_challenges
(
    id                     uuid                     NOT NULL,
    user_id                uuid                     NOT NULL,
    code_hash              varchar(64)              NOT NULL,
    code_expires_at        timestamp with time zone NOT NULL,
    failed_attempts        integer                  NOT NULL,
    last_sent_at           timestamp with time zone NOT NULL,
    reset_token_hash       varchar(64)              NULL,
    reset_token_expires_at timestamp with time zone NULL,
    verified               boolean                  NOT NULL,
    CONSTRAINT password_reset_challenges_pkey PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_challenge_user UNIQUE (user_id),
    CONSTRAINT uk_password_reset_challenge_token UNIQUE (reset_token_hash),
    CONSTRAINT fk_password_reset_challenge_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE TABLE public.refresh_tokens
(
    id                     uuid                     NOT NULL,
    user_id                uuid                     NOT NULL,
    token_hash             varchar(64)              NOT NULL,
    expires_at             timestamp with time zone NOT NULL,
    revoked                boolean                  NOT NULL DEFAULT false,
    created_at             timestamp with time zone NOT NULL,
    replaced_by_token_hash varchar(64)              NULL,
    family_id              uuid                     NOT NULL DEFAULT gen_random_uuid(),
    CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON public.refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_id ON public.refresh_tokens (family_id);

CREATE TABLE public.book_club
(
    id            uuid NOT NULL,
    book_author   varchar(255) NULL,
    book_name     varchar(255) NULL,
    book_synopses text NULL,
    "date"        timestamp(6) NULL,
    "location"    varchar(255) NULL,
    organizer_id  uuid NULL,
    book_cover_url  TEXT NULL,
    CONSTRAINT book_club_pkey PRIMARY KEY (id),
    CONSTRAINT fkj2h7pqx20fvjniqtykxi4fhjy FOREIGN KEY (organizer_id) REFERENCES public.users (id)
);

CREATE TABLE public.book_club_participants
(
    book_club_id uuid NOT NULL,
    users_id     uuid NOT NULL,
    CONSTRAINT book_club_participants_pkey PRIMARY KEY (book_club_id, users_id),
    CONSTRAINT fkesu8g6exu57qfatejeedwi404 FOREIGN KEY (users_id) REFERENCES public.users (id),
    CONSTRAINT fkm5dptso14xmdvd4q1ru25wbn FOREIGN KEY (book_club_id) REFERENCES public.book_club (id)
);

create table book_club_reviews
(
    id           uuid         not null,
    content      varchar(200) not null,
    created_at   timestamp(6) not null,
    rating       DECIMAL(2,1) not null check (rating >= 0 and rating <= 5),
    book_club_id uuid         not null,
    user_id      uuid         not null,
    PRIMARY KEY (id),
    CONSTRAINT FKg1ejaghxfur5gnq4gr12gf1uh FOREIGN KEY (book_club_id) REFERENCES book_club (id),
    CONSTRAINT FK6n0blip1lvkyh90jbl1jwusx6 FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE public.obras
(
    "type"           varchar(31)    NOT NULL,
    id               uuid           NOT NULL,
    description      text NULL,
    publication_date timestamp(6) NULL,
    title            varchar(255) NULL,
    users_id         uuid NULL,
    author_name       varchar(255) NULL,
    student_class varchar(255) NOT NULL,
    view_count       int8 DEFAULT 0 NOT NULL,
    CONSTRAINT obras_pkey PRIMARY KEY (id),
    CONSTRAINT obras_type_check CHECK (((type)::text = ANY (ARRAY[('LibraLiterature':: character varying)::text, ('Multimedia':: character varying)::text,('Poem':: character varying)::text, ('Article':: character varying)::text, ('Cordel':: character varying)::text, ('Essay':: character varying)::text, ('ShortStory':: character varying)::text, ('Tale':: character varying)::text, ('Art':: character varying)::text, ('Infographic':: character varying)::text, ('Other':: character varying)::text])
        ) ),
    CONSTRAINT fk2fptp0tpi0hv70i3cf78aev1t FOREIGN KEY (users_id) REFERENCES public.users(id)
);

CREATE TABLE public.poem
(
    id           UUID NOT NULL,
    content      TEXT,
    rhyme_scheme VARCHAR(255),
    poem_type    VARCHAR(255),
    CONSTRAINT pk_poem PRIMARY KEY (id),
    CONSTRAINT FK_POEM_ON_ID FOREIGN KEY (id) REFERENCES obras (id)
);
CREATE TABLE public.short_story
(
    "content" text NULL,
    id        uuid NOT NULL,
    CONSTRAINT short_story_pkey PRIMARY KEY (id),
    CONSTRAINT fks0rnqq3wya1a06se7ca9gvjuh FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.tale
(
    "content" text NULL,
    genre     text NULL,
    id        uuid NOT NULL,
    CONSTRAINT tale_pkey PRIMARY KEY (id),
    CONSTRAINT fksqd2w8hahrvrb273ko7abkp64 FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.art
(
    url TEXT NULL,
    id  uuid NOT NULL,
    CONSTRAINT art_pkey PRIMARY KEY (id),
    CONSTRAINT fkay06evh3m16uloy5a1ao7hg3h FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.article
(
    "content" text NULL,
    id        uuid NOT NULL,
    CONSTRAINT article_pkey PRIMARY KEY (id),
    CONSTRAINT fkst2v95jo66vjd7ssmfcluunjg FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.other_work
(
    id        uuid NOT NULL,
    "content" text NULL,
    url       text NULL,
    image_url text NULL,
    CONSTRAINT other_work_pkey PRIMARY KEY (id),
    CONSTRAINT fk_other_work_on_id FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public."comments"
(
    id         uuid         NOT NULL,
    "content"  varchar(200) NOT NULL,
    created_at timestamp(6) NOT NULL,
    user_id    uuid         NOT NULL,
    work_id    uuid         NOT NULL,
    CONSTRAINT comments_pkey PRIMARY KEY (id),
    CONSTRAINT fk8d1dy8kaxda6262222bx5tkkd FOREIGN KEY (work_id) REFERENCES public.obras (id),
    CONSTRAINT fk8omq0tc18jd43bu5tjh6jvraq FOREIGN KEY (user_id) REFERENCES public.users (id)
);

CREATE TABLE public.cordel
(
    "content"    text NULL,
    rhyme_scheme varchar(255) NULL,
    id           uuid NOT NULL,
    illustration_id uuid NULL,
    CONSTRAINT cordel_pkey PRIMARY KEY (id),
    CONSTRAINT  ilustrationfkey FOREIGN KEY (illustration_id) REFERENCES public.art (id),
    CONSTRAINT fkgwf8wt7aq919plt7il9iwjfsd FOREIGN KEY (illustration_id) REFERENCES public.obras (id)
);

CREATE TABLE public.essay
(
    "content"         text NULL,
    feedback          text NULL,
    rate              int4 NULL,
    theme             varchar(255) NULL,
    theme_description text NULL,
    id                uuid NOT NULL,
    CONSTRAINT essay_pkey PRIMARY KEY (id),
    CONSTRAINT fkqgocmbwt6v8pbhu48jy70jm54 FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.infographic
(
    url TEXT NULL,
    id  uuid NOT NULL,
    CONSTRAINT infographic_pkey PRIMARY KEY (id),
    CONSTRAINT fkq96d3tb77dwci88t9hq2lhcq5 FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.libra_literature
(
    duration int8 NULL,
    url TEXT NULL,
    id       uuid NOT NULL,
    CONSTRAINT libra_literature_pkey PRIMARY KEY (id),
    CONSTRAINT fkox8sb6jjxrhxa7ncugwoj8dqn FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.likes
(
    user_id uuid NOT NULL,
    work_id uuid NOT NULL,
    CONSTRAINT likes_pkey PRIMARY KEY (user_id, work_id),
    CONSTRAINT fknvx9seeqqyy71bij291pwiwrg FOREIGN KEY (user_id) REFERENCES public.users (id),
    CONSTRAINT fksfrbjomqnofi38udcrbwsj7q7 FOREIGN KEY (work_id) REFERENCES public.obras (id)
);

CREATE TABLE public.multimedia
(
    duration int8 NULL,
    url TEXT NULL,
    id       uuid NOT NULL,
    CONSTRAINT multimedia_pkey PRIMARY KEY (id),
    CONSTRAINT fkd84k95871jtaji3mfjtb0td8a FOREIGN KEY (id) REFERENCES public.obras (id)
);

CREATE TABLE public.comment_replies
(
    id         uuid         NOT NULL,
    content    varchar(200) NOT NULL,
    comment_id uuid         NOT NULL,
    user_id    uuid         NOT NULL,
    created_at timestamp(6) NOT NULL,
    CONSTRAINT comment_replies_pkey PRIMARY KEY (id),
    CONSTRAINT uk_comment_replies_comment UNIQUE (comment_id),
    CONSTRAINT fk_comment_replies_comment FOREIGN KEY (comment_id)
        REFERENCES public.comments (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_replies_user FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);

CREATE TABLE public.comment_likes
(
    comment_id uuid NOT NULL,
    user_id    uuid NOT NULL,
    CONSTRAINT comment_likes_pkey PRIMARY KEY (comment_id, user_id),
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id)
        REFERENCES public.comments (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE CASCADE
);