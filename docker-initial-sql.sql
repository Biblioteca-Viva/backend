-- public.users definição

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users
(
    id         uuid         NOT NULL,
    email      varchar(255) NOT NULL,
    "name"     varchar(255) NOT NULL,
    "password" varchar(255) NOT NULL,
    "role"     varchar(255) NOT NULL,
    CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY
                                        ((ARRAY ['CURADOR'::character varying, 'ALUNO'::character varying, 'ADMIN'::character varying])::text[])))
);


-- public.obras definição

-- Drop table

-- DROP TABLE public.obras;

CREATE TABLE public.obras
(
    "type"           varchar(31)  NOT NULL,
    id               uuid         NOT NULL,
    description      text         NULL,
    publication_date timestamp(6) NULL,
    title            varchar(255) NULL,
    users_id         uuid         NULL,
    CONSTRAINT obras_pkey PRIMARY KEY (id),
    CONSTRAINT obras_type_check CHECK (((type)::text = ANY
                                        ((ARRAY ['LibraLiterature'::character varying, 'Multimedia'::character varying, 'Article'::character varying, 'Cordel'::character varying, 'Essay'::character varying, 'ShortStory'::character varying, 'Tale'::character varying, 'Art'::character varying, 'Infographic'::character varying])::text[]))),
    CONSTRAINT fk2fptp0tpi0hv70i3cf78aev1t FOREIGN KEY (users_id) REFERENCES public.users (id)
);


-- public.short_story definição

-- Drop table

-- DROP TABLE public.short_story;

CREATE TABLE public.short_story
(
    "content" text NULL,
    id        uuid NOT NULL,
    CONSTRAINT short_story_pkey PRIMARY KEY (id),
    CONSTRAINT fks0rnqq3wya1a06se7ca9gvjuh FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.tale definição

-- Drop table

-- DROP TABLE public.tale;

CREATE TABLE public.tale
(
    "content" text NULL,
    genre     text NULL,
    id        uuid NOT NULL,
    CONSTRAINT tale_pkey PRIMARY KEY (id),
    CONSTRAINT fksqd2w8hahrvrb273ko7abkp64 FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.art definição

-- Drop table

-- DROP TABLE public.art;

CREATE TABLE public.art
(
    url varchar(255) NULL,
    id  uuid         NOT NULL,
    CONSTRAINT art_pkey PRIMARY KEY (id),
    CONSTRAINT fkay06evh3m16uloy5a1ao7hg3h FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.article definição

-- Drop table

-- DROP TABLE public.article;

CREATE TABLE public.article
(
    "content" text NULL,
    id        uuid NOT NULL,
    CONSTRAINT article_pkey PRIMARY KEY (id),
    CONSTRAINT fkst2v95jo66vjd7ssmfcluunjg FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.cordel definição

-- Drop table

-- DROP TABLE public.cordel;

CREATE TABLE public.cordel
(
    "content"    text         NULL,
    rhyme_scheme varchar(255) NULL,
    id           uuid         NOT NULL,
    CONSTRAINT cordel_pkey PRIMARY KEY (id),
    CONSTRAINT fkgwf8wt7aq919plt7il9iwjfsd FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.essay definição

-- Drop table

-- DROP TABLE public.essay;

CREATE TABLE public.essay
(
    "content"         text         NULL,
    feedback          text         NULL,
    rate              int4         NULL,
    theme             varchar(255) NULL,
    theme_description text         NULL,
    id                uuid         NOT NULL,
    CONSTRAINT essay_pkey PRIMARY KEY (id),
    CONSTRAINT fkqgocmbwt6v8pbhu48jy70jm54 FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.infographic definição

-- Drop table

-- DROP TABLE public.infographic;

CREATE TABLE public.infographic
(
    url varchar(255) NULL,
    id  uuid         NOT NULL,
    CONSTRAINT infographic_pkey PRIMARY KEY (id),
    CONSTRAINT fkq96d3tb77dwci88t9hq2lhcq5 FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.libra_literature definição

-- Drop table

-- DROP TABLE public.libra_literature;

CREATE TABLE public.libra_literature
(
    duration int8         NULL,
    url      varchar(255) NULL,
    id       uuid         NOT NULL,
    CONSTRAINT libra_literature_pkey PRIMARY KEY (id),
    CONSTRAINT fkox8sb6jjxrhxa7ncugwoj8dqn FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.multimedia definição

-- Drop table

-- DROP TABLE public.multimedia;

CREATE TABLE public.multimedia
(
    duration int8         NULL,
    url      varchar(255) NULL,
    id       uuid         NOT NULL,
    CONSTRAINT multimedia_pkey PRIMARY KEY (id),
    CONSTRAINT fkd84k95871jtaji3mfjtb0td8a FOREIGN KEY (id) REFERENCES public.obras (id)
);
-- Seed data for Biblioteca Viva domain entities.
-- Important: this script assumes tables already exist (Hibernate `ddl-auto=create` or migrations).

INSERT INTO users (id, email, name, password, role)
VALUES ('e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf', 'aluno1@teste.com', 'aluno1', '123456', 'ALUNO'),
       ('eca64533-6dbd-465b-863c-bb540fecdc61', 'aluno2@teste.com', 'aluno2', '123456', 'ALUNO'),
       ('4c9f354b-0780-4cdb-b76f-d43e54ea3644', 'aluno3@teste.com', 'aluno3', '123456', 'ALUNO'),
       ('455150bd-8e40-498c-8005-cca9cefa9099', 'professor@teste.com', 'professor', '123456', 'CURADOR'),
       ('febc5d09-cc12-4bc0-b29c-2cde56102619', 'admin@admin.com', 'admin', '123456', 'ADMIN');

-- ========================================================
-- Aluno 1: e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf
-- Aluno 2: eca64533-6dbd-465b-863c-bb540fecdc61
-- Aluno 3: 4c9f354b-0780-4cdb-b76f-d43e54ea3644
-- ========================================================

INSERT INTO obras (id, title, users_id, publication_date, description, type)
VALUES
    -- Obras atribuídas ao Aluno 1
    ('11111111-1111-1111-1111-111111111001', 'Reportagem sobre o Rio Capibaribe',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf', '2024-03-10',
     'Reportagem escolar sobre a historia e os desafios ambientais do Rio Capibaribe.', 'Article'),
    ('22222222-2222-2222-2222-222222222001', 'Cordel da Feira Popular', 'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2023-09-12', 'Cordel sobre personagens e historias da feira local.', 'Cordel'),
    ('33333333-3333-3333-3333-333333333001', 'Redacao ENEM: Inclusao Digital', 'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-04-05', 'Redacao dissertativa sobre acesso tecnologico e desigualdade social.', 'Essay'),
    ('66666666-6666-6666-6666-666666666001', 'Cartaz Digital: Semana da Leitura',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf', '2024-01-05',
     'Peca visual produzida para divulgar atividades da semana cultural.', 'Art'),
    ('44444444-4444-4444-4444-444444444001', 'Conto da Janela Azul', 'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2023-11-21', 'Conto curto sobre imaginacao e amizade durante as ferias.', 'ShortStory'),
    ('55555555-5555-5555-5555-555555555003', 'Fabula do Passaro Dourado', 'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-08-03', 'Narrativa simbolica sobre coragem, cuidado e coletividade.', 'Tale'),
    ('77777777-7777-7777-7777-777777777001', 'Infografico: Ciclo da Agua', 'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2023-10-09', 'Infografico educativo explicando etapas do ciclo da agua.', 'Infographic'),
    ('88888888-8888-8888-8888-888888888001', 'Curta: Vozes da Juventude', 'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-02-01', 'Curta-metragem escolar com depoimentos sobre futuro e educacao.', 'Multimedia'),
    ('99999999-9999-9999-9999-999999999001', 'Literatura em Libras: O Menino e a Lua',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf', '2024-03-03', 'Narrativa sinalizada adaptada para publico infantojuvenil.',
     'LibraLiterature'),

    -- Obras atribuídas ao Aluno 2
    ('11111111-1111-1111-1111-111111111002', 'Artigo: Literature na Comunidade', 'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-05-01', 'Reflexao sobre o impacto de projetos de leitura em bairros perifericos.', 'Article'),
    ('22222222-2222-2222-2222-222222222002', 'Cordel do Sertao Vivo', 'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-01-19', 'Versos sobre memoria, resistencia e cultura do sertao.', 'Cordel'),
    ('33333333-3333-3333-3333-333333333002', 'Redacao ENEM: Sustentabilidade Urbana',
     'eca64533-6dbd-465b-863c-bb540fecdc61', '2024-06-10',
     'Discussao sobre mobilidade, residuos e politicas publicas locais.', 'Essay'),
    ('44444444-4444-4444-4444-444444444002', 'Conto da Chuva Mansa', 'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-02-14', 'Historia breve ambientada em uma tarde chuvosa no interior.', 'ShortStory'),
    ('55555555-5555-5555-5555-555555555001', 'Lenda da Pedra Encantada', 'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2023-12-30', 'Recontagem de narrativa popular com elementos fantasticos regionais.', 'Tale'),
    ('66666666-6666-6666-6666-666666666002', 'Ilustracao: Jardim da Escola', 'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-04-12', 'Arte digital inspirada no espaco de convivencia da escola.', 'Art'),
    ('77777777-7777-7777-7777-777777777002', 'Infografico: Alimentacao Saudavel',
     'eca64533-6dbd-465b-863c-bb540fecdc61', '2024-05-23', 'Resumo visual com orientacoes de habitos alimentares.',
     'Infographic'),
    ('88888888-8888-8888-8888-888888888002', 'Video-poema: Mar de Dentro', 'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-06-28', 'Video-poema autoral com leitura dramatizada e trilha original.', 'Multimedia'),
    ('99999999-9999-9999-9999-999999999002', 'Literatura em Libras: A Festa da Rua',
     'eca64533-6dbd-465b-863c-bb540fecdc61', '2024-07-17', 'Performance em Libras baseada em cronica comunitaria.',
     'LibraLiterature'),

    -- Obras atribuídas ao Aluno 3
    ('11111111-1111-1111-1111-111111111003', 'Materia Especial: Vozes da Escola',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644', '2024-08-15',
     'Coletanea de entrevistas com estudantes sobre producao textual.', 'Article'),
    ('22222222-2222-2222-2222-222222222003', 'Cordel da Juventude', '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-07-02', 'Narrativa em cordel sobre sonhos e trajetorias de jovens estudantes.', 'Cordel'),
    ('33333333-3333-3333-3333-333333333003', 'Redacao ENEM: Leitura e Cidadania',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644', '2024-09-08',
     'Argumentacao sobre bibliotecas escolares como espacos de transformacao.', 'Essay'),
    ('44444444-4444-4444-4444-444444444003', 'Conto do Trem das Seis', '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-10-01', 'Narrativa sobre encontros cotidianos e escolhas inesperadas.', 'ShortStory'),
    ('55555555-5555-5555-5555-555555555002', 'Conto Popular do Vento Norte', '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-03-18', 'Historia oral adaptada para linguagem escrita escolar.', 'Tale'),
    ('66666666-6666-6666-6666-666666666003', 'Quadrinho Curto: Biblioteca Viva', '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-09-20', 'Historia em quadrinhos sobre o cotidiano da biblioteca escolar.', 'Art'),
    ('77777777-7777-7777-7777-777777777003', 'Infografico: Historia do Bairro', '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-11-11', 'Linha do tempo visual com marcos historicos da comunidade.', 'Infographic'),
    ('88888888-8888-8888-8888-888888888003', 'Entrevista: Mestres da Comunidade',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644', '2024-10-22',
     'Serie de entrevistas com liderancas locais e artistas populares.', 'Multimedia'),
    ('99999999-9999-9999-9999-999999999003', 'Literatura em Libras: Pequenas Coragens',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644', '2024-12-02', 'Narracao visual sobre autonomia e pertencimento.',
     'LibraLiterature');
-- =========================
-- Entidades Concretas
-- =========================

INSERT INTO article (id, content)
VALUES ('11111111-1111-1111-1111-111111111001',
        'Texto jornalistico com introducao, desenvolvimento e conclusao sobre o rio.'),
       ('11111111-1111-1111-1111-111111111002', 'Artigo opinativo com dados de leitura e analise social.'),
       ('11111111-1111-1111-1111-111111111003', 'Materia especial com entrevistas e contexto historico local.');

INSERT INTO cordel (id, content, rhyme_scheme)
VALUES ('22222222-2222-2222-2222-222222222001', 'Versos em sextilhas descrevendo cenas da feira.', 'ABABCC'),
       ('22222222-2222-2222-2222-222222222002', 'Cordel em linguagem popular sobre memoria regional.', 'AABBCC'),
       ('22222222-2222-2222-2222-222222222003', 'Poema narrativo celebrando experiencias estudantis.', 'ABABAB');

INSERT INTO essay (id, content, rate, theme, theme_description, feedback)
VALUES ('33333333-3333-3333-3333-333333333001', 'Argumentacao sobre acesso digital e politicas inclusivas.', 920,
        'Inclusao digital', 'Impactos da exclusao digital na formacao de jovens.',
        'Boa articulacao de repertorio e proposta de intervencao consistente.'),
       ('33333333-3333-3333-3333-333333333002', 'Texto dissertativo sobre sustentabilidade nas cidades.', 880,
        'Sustentabilidade urbana', 'Desafios de mobilidade e gestao de residuos em centros urbanos.',
        'Estrutura solida; aprofundar repertorio sociocultural na conclusao.'),
       ('33333333-3333-3333-3333-333333333003', 'Defesa da leitura como direito e instrumento de cidadania.', 960,
        'Leitura e cidadania', 'Relacao entre bibliotecas publicas, escola e participacao social.',
        'Excelente progressao argumentativa e coesao textual.');

INSERT INTO short_story (id, content)
VALUES ('44444444-4444-4444-4444-444444444001', 'Uma menina descobre que sua janela revela mundos imaginarios.'),
       ('44444444-4444-4444-4444-444444444002', 'Dois amigos percorrem a cidade sob chuva em busca de abrigo.'),
       ('44444444-4444-4444-4444-444444444003', 'Passageiros compartilham lembrancas no ultimo trem da tarde.');

INSERT INTO tale (id, content, genre)
VALUES ('55555555-5555-5555-5555-555555555001', 'Narrativa tradicional sobre uma pedra com poderes ancestrais.',
        'fantasia regional'),
       ('55555555-5555-5555-5555-555555555002', 'Historia oral adaptada de um velho conto de vento e viagem.',
        'conto popular'),
       ('55555555-5555-5555-5555-555555555003', 'Fabula moral sobre um passaro que ensina cooperacao.', 'fabula');

INSERT INTO art (id, url)
VALUES ('66666666-6666-6666-6666-666666666001', 'https://midia.bibliotecaviva.local/artes/cartaz-semana-leitura.png'),
       ('66666666-6666-6666-6666-666666666002', 'https://midia.bibliotecaviva.local/artes/jardim-escola.png'),
       ('66666666-6666-6666-6666-666666666003',
        'https://midia.bibliotecaviva.local/artes/quadrinho-biblioteca-viva.pdf');

INSERT INTO infographic (id, url)
VALUES ('77777777-7777-7777-7777-777777777001', 'https://midia.bibliotecaviva.local/infograficos/ciclo-agua.svg'),
       ('77777777-7777-7777-7777-777777777002',
        'https://midia.bibliotecaviva.local/infograficos/alimentacao-saudavel.svg'),
       ('77777777-7777-7777-7777-777777777003', 'https://midia.bibliotecaviva.local/infograficos/historia-bairro.svg');

INSERT INTO multimedia (id, url)
VALUES ('88888888-8888-8888-8888-888888888001', 'https://midia.bibliotecaviva.local/videos/vozes-juventude.mp4'),
       ('88888888-8888-8888-8888-888888888002', 'https://midia.bibliotecaviva.local/videos/mar-de-dentro.mp4'),
       ('88888888-8888-8888-8888-888888888003', 'https://midia.bibliotecaviva.local/videos/mestres-comunidade.mp4');

INSERT INTO libra_literature (id, url)
VALUES ('99999999-9999-9999-9999-999999999001', 'https://midia.bibliotecaviva.local/libras/menino-lua.mp4'),
       ('99999999-9999-9999-9999-999999999002', 'https://midia.bibliotecaviva.local/libras/festa-rua.mp4'),
       ('99999999-9999-9999-9999-999999999003', 'https://midia.bibliotecaviva.local/libras/pequenas-coragens.mp4');