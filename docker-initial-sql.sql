-- public.users definição

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users
(
    id             uuid         NOT NULL,
    email          varchar(255) NOT NULL,
    "name"         varchar(255) NOT NULL,
    "password"     varchar(255) NOT NULL,
    "role"         varchar(255) NOT NULL,
    account_status varchar(255) NOT NULL,
    CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY (ARRAY[('CURADOR':: character varying)::text, ('ALUNO':: character varying)::text, ('ADMIN':: character varying)::text])
) )
);


-- public.book_club definição

-- Drop table

-- DROP TABLE public.book_club;

CREATE TABLE public.book_club
(
    id            uuid NOT NULL,
    book_author   varchar(255) NULL,
    book_name     varchar(255) NULL,
    book_synopses text NULL,
    "date"        timestamp(6) NULL,
    "location"    varchar(255) NULL,
    organizer_id  uuid NULL,
    CONSTRAINT book_club_pkey PRIMARY KEY (id),
    CONSTRAINT fkj2h7pqx20fvjniqtykxi4fhjy FOREIGN KEY (organizer_id) REFERENCES public.users (id)
);


-- public.book_club_participants definição

-- Drop table

-- DROP TABLE public.book_club_participants;

CREATE TABLE public.book_club_participants
(
    book_club_id uuid NOT NULL,
    users_id     uuid NOT NULL,
    CONSTRAINT book_club_participants_pkey PRIMARY KEY (book_club_id, users_id),
    CONSTRAINT fkesu8g6exu57qfatejeedwi404 FOREIGN KEY (users_id) REFERENCES public.users (id),
    CONSTRAINT fkm5dptso14xmdvd4q1ru25wbn FOREIGN KEY (book_club_id) REFERENCES public.book_club (id)
);


-- public.obras definição

-- Drop table

-- DROP TABLE public.obras;

CREATE TABLE public.obras
(
    "type"           varchar(31)    NOT NULL,
    id               uuid           NOT NULL,
    description      text NULL,
    publication_date timestamp(6) NULL,
    title            varchar(255) NULL,
    users_id         uuid NULL,
    view_count       int8 DEFAULT 0 NOT NULL,
    CONSTRAINT obras_pkey PRIMARY KEY (id),
    CONSTRAINT obras_type_check CHECK (((type)::text = ANY (ARRAY[('LibraLiterature':: character varying)::text, ('Multimedia':: character varying)::text, ('Article':: character varying)::text, ('Cordel':: character varying)::text, ('Essay':: character varying)::text, ('ShortStory':: character varying)::text, ('Tale':: character varying)::text, ('Art':: character varying)::text, ('Infographic':: character varying)::text])
) ),
	CONSTRAINT fk2fptp0tpi0hv70i3cf78aev1t FOREIGN KEY (users_id) REFERENCES public.users(id)
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
    url TEXT NULL,
    id  uuid NOT NULL,
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


-- public."comments" definição

-- Drop table

-- DROP TABLE public."comments";

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


-- public.cordel definição

-- Drop table

-- DROP TABLE public.cordel;

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


-- public.essay definição

-- Drop table

-- DROP TABLE public.essay;

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


-- public.infographic definição

-- Drop table

-- DROP TABLE public.infographic;

CREATE TABLE public.infographic
(
    url TEXT NULL,
    id  uuid NOT NULL,
    CONSTRAINT infographic_pkey PRIMARY KEY (id),
    CONSTRAINT fkq96d3tb77dwci88t9hq2lhcq5 FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.libra_literature definição

-- Drop table

-- DROP TABLE public.libra_literature;

CREATE TABLE public.libra_literature
(
    duration int8 NULL,
    url TEXT NULL,
    id       uuid NOT NULL,
    CONSTRAINT libra_literature_pkey PRIMARY KEY (id),
    CONSTRAINT fkox8sb6jjxrhxa7ncugwoj8dqn FOREIGN KEY (id) REFERENCES public.obras (id)
);


-- public.likes definição

-- Drop table

-- DROP TABLE public.likes;

CREATE TABLE public.likes
(
    user_id uuid NOT NULL,
    work_id uuid NOT NULL,
    CONSTRAINT likes_pkey PRIMARY KEY (user_id, work_id),
    CONSTRAINT fknvx9seeqqyy71bij291pwiwrg FOREIGN KEY (user_id) REFERENCES public.users (id),
    CONSTRAINT fksfrbjomqnofi38udcrbwsj7q7 FOREIGN KEY (work_id) REFERENCES public.obras (id)
);


-- public.multimedia definição

-- Drop table

-- DROP TABLE public.multimedia;

CREATE TABLE public.multimedia
(
    duration int8 NULL,
    url TEXT NULL,
    id       uuid NOT NULL,
    CONSTRAINT multimedia_pkey PRIMARY KEY (id),
    CONSTRAINT fkd84k95871jtaji3mfjtb0td8a FOREIGN KEY (id) REFERENCES public.obras (id)
);
-- Seed data for Biblioteca Viva domain entities.
-- Important: this script assumes tables already exist (Hibernate `ddl-auto=create` or migrations).
-- password: 123456

INSERT INTO users (id, email, name, password, role, account_status)
VALUES ('e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf', 'aluno1@teste.com', 'aluno1',
        '$2y$10$GFIf48kDF3iZ1gDdCbKIVe2u51YJ2p9BdHhAokyEzc9CU6l0Ol/QO', 'ALUNO', 'ACTIVE'),
       ('eca64533-6dbd-465b-863c-bb540fecdc61', 'aluno2@teste.com', 'aluno2',
        '$2y$10$GFIf48kDF3iZ1gDdCbKIVe2u51YJ2p9BdHhAokyEzc9CU6l0Ol/QO', 'ALUNO', 'ACTIVE'),
       ('4c9f354b-0780-4cdb-b76f-d43e54ea3644', 'aluno3@teste.com', 'aluno3',
        '$2y$10$GFIf48kDF3iZ1gDdCbKIVe2u51YJ2p9BdHhAokyEzc9CU6l0Ol/QO', 'ALUNO', 'ACTIVE'),
       ('455150bd-8e40-498c-8005-cca9cefa9099', 'professor@teste.com', 'professor',
        '$2y$10$GFIf48kDF3iZ1gDdCbKIVe2u51YJ2p9BdHhAokyEzc9CU6l0Ol/QO', 'CURADOR', 'ACTIVE'),
       ('febc5d09-cc12-4bc0-b29c-2cde56102619', 'admin@teste.com', 'admin',
        '$2y$10$GFIf48kDF3iZ1gDdCbKIVe2u51YJ2p9BdHhAokyEzc9CU6l0Ol/QO', 'ADMIN', 'ACTIVE');

-- ========================================================
-- Aluno 1: e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf
-- Aluno 2: eca64533-6dbd-465b-863c-bb540fecdc61
-- Aluno 3: 4c9f354b-0780-4cdb-b76f-d43e54ea3644
-- ========================================================
-- ============================================================
-- BIBLIOTECA VIVA — SEED DE DADOS REALISTAS
-- Mantém todos os IDs e user_ids originais
-- ============================================================

-- ============================================================
-- WORKS (tabela base da hierarquia)
-- ============================================================
INSERT INTO obras (id, title, users_id, publication_date, description, type)
VALUES
    -- Aluno 1 (e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf)



    ('44444444-4444-4444-4444-444444444001',
     'A Janela Azul',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2023-11-21',
     'Crônica sobre memória afetiva e o olhar de infância, narrada pela perspectiva de uma menina que encontra no azul de uma janela a fronteira entre o cotidiano e a imaginação.',
     'ShortStory'),

    ('44444444-4444-4444-4444-444444444002',
     'Chuva Mansa',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-02-14',
     'Crônica ambientada em uma tarde chuvosa em Garanhuns, que acompanha dois amigos num alpendre e o silêncio carregado de uma despedida que nenhum dos dois consegue nomear.',
     'ShortStory'),

    ('44444444-4444-4444-4444-444444444003',
     'O Trem das Seis',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-10-01',
     'Crônica de observação sobre os passageiros de um trem do interior, explorando o que as pessoas carregam em silêncio e o que o movimento cotidiano revela sobre memória e tempo.',
     'ShortStory'),

    ('55555555-5555-5555-5555-555555555003',
     'O Pássaro Dourado',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-08-03',
     'Fábula sobre escuta e coletividade: um pássaro estranho chega a uma floresta de cantos solitários e, sem impor nada, transforma a forma como todos se relacionam com a própria voz.',
     'Tale'),

    ('55555555-5555-5555-5555-555555555001',
     'A Pedra Encantada',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2023-12-30',
     'Conto de fantasia regional ambientado em território Xukuru, em que o surgimento misterioso de uma pedra em forma humana divide a aldeia entre o medo e a curiosidade — até que a chuva responde.',
     'Tale'),

    ('55555555-5555-5555-5555-555555555002',
     'O Vento Norte',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-03-18',
     'Conto popular sobre uma menina perdida na caatinga que segue um vento com direção própria, adaptado de narrativa oral preservando a cadência dos contadores de histórias da região.',
     'Tale'),

    ('11111111-1111-1111-1111-111111111001',
     'Reportagem sobre o Rio Capibaribe',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-03-10',
     'Reportagem escolar sobre a história e os desafios ambientais do Rio Capibaribe, abordando a poluição, o desmatamento das margens e iniciativas de preservação da bacia hidrográfica.',
     'Article'),

    ('22222222-2222-2222-2222-222222222001',
     'Cordel da Feira Popular',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2023-09-12',
     'Cordel sobre personagens e histórias da feira local, retratando o vendedor de rapadura, a barraqueira de tapioca e o repentista que anima as manhãs de sábado.',
     'Cordel'),

    ('33333333-3333-3333-3333-333333333001',
     'Redação ENEM: Inclusão Digital',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-04-05',
     'Redação dissertativa-argumentativa sobre o acesso à tecnologia como fator determinante na ampliação ou redução das desigualdades sociais no Brasil contemporâneo.',
     'Essay'),



    ('66666666-6666-6666-6666-666666666001',
     'Cartaz Digital: Semana da Leitura',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-01-05',
     'Peça visual produzida para divulgar as atividades da Semana Cultural da Escola, com identidade gráfica baseada em tipografia expressiva e cores vibrantes.',
     'Art'),

    ('66616666-6666-6666-6666-666666666001',
     'Ilustração: Cordel do Sertão Vivo',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2026-01-04',
     'Ilustração para cordel',
     'Art'),

    ('66626666-6666-6666-6666-666666666001',
     'Ilustração: Cordel da Juventude',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2026-01-04',
     'Ilustração para cordel',
     'Art'),

    ('66636666-6666-6666-6666-666666666001',
     'Ilustração: Cordel da Feira Popular',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2026-01-04',
     'Ilustração para cordel',
     'Art'),

    ('77777777-7777-7777-7777-777777777001',
     'Infográfico: Ciclo da Água',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2023-10-09',
     'Infográfico educativo explicando as etapas do ciclo hidrológico — evaporação, condensação, precipitação e infiltração — com linguagem acessível ao público escolar.',
     'Infographic'),

    ('88888888-8888-8888-8888-888888888001',
     'Curta: Vozes da Juventude',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-02-01',
     'Curta-metragem escolar com depoimentos de estudantes sobre perspectivas de futuro, educação e sonhos profissionais no contexto do semiárido nordestino.',
     'Multimedia'),

    ('99999999-9999-9999-9999-999999999001',
     'Literatura em Libras: O Menino e a Lua',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '2024-03-03',
     'Narrativa sinalizada adaptada para o público infantojuvenil, baseada em um conto original sobre um menino que tenta alcançar a lua e aprende sobre limites e sonhos.',
     'LibraLiterature'),

    -- Aluno 2 (eca64533-6dbd-465b-863c-bb540fecdc61)
    ('11111111-1111-1111-1111-111111111002',
     'Artigo: Literatura na Comunidade',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-05-01',
     'Reflexão sobre o impacto de projetos comunitários de leitura em bairros periféricos, com base em entrevistas realizadas em duas iniciativas locais de Garanhuns.',
     'Article'),

    ('22222222-2222-2222-2222-222222222002',
     'Cordel do Sertão Vivo',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-01-19',
     'Versos em linguagem popular sobre memória, resistência e cultura do sertão nordestino, com referências às tradições do vaqueiro, da cantoria e das festas de São João.',
     'Cordel'),

    ('33333333-3333-3333-3333-333333333002',
     'Redação ENEM: Sustentabilidade Urbana',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-06-10',
     'Discussão sobre os desafios da mobilidade urbana, gestão de resíduos sólidos e implementação de políticas públicas ambientais em municípios de médio porte.',
     'Essay'),



    ('66666666-6666-6666-6666-666666666002',
     'Ilustração: Jardim da Escola',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-04-12',
     'Arte digital inspirada no espaço de convivência da escola, retratando o jardim central com suas flores nativas, a mangueira centenária e os estudantes em roda de conversa.',
     'Art'),

    ('77777777-7777-7777-7777-777777777002',
     'Infográfico: Alimentação Saudável',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-05-23',
     'Resumo visual com orientações sobre hábitos alimentares saudáveis baseados na alimentação regional, destacando frutas, verduras e proteínas do semiárido nordestino.',
     'Infographic'),

    ('88888888-8888-8888-8888-888888888002',
     'Vídeo-poema: Mar de Dentro',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-06-28',
     'Vídeo-poema autoral com leitura dramatizada e trilha sonora original, explorando a relação entre o sertão e o mar como metáforas de distância, pertencimento e saudade.',
     'Multimedia'),

    ('99999999-9999-9999-9999-999999999002',
     'Literatura em Libras: A Festa da Rua',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '2024-07-17',
     'Performance em Libras baseada em crônica comunitária, narrando as memórias de uma festa junina de rua e a solidariedade entre vizinhos durante os preparativos.',
     'LibraLiterature'),

    -- Aluno 3 (4c9f354b-0780-4cdb-b76f-d43e54ea3644)
    ('11111111-1111-1111-1111-111111111003',
     'Matéria Especial: Vozes da Escola',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-08-15',
     'Coletânea de entrevistas com estudantes do ensino médio sobre o processo de produção textual, revelando desafios, descobertas e a relação de cada um com a escrita criativa.',
     'Article'),

    ('22222222-2222-2222-2222-222222222003',
     'Cordel da Juventude',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-07-02',
     'Narrativa em cordel sobre sonhos e trajetórias de jovens estudantes do semiárido, abordando vestibular, primeiro emprego, partida para a cidade e saudade da terra natal.',
     'Cordel'),

    ('33333333-3333-3333-3333-333333333003',
     'Redação ENEM: Leitura e Cidadania',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-09-08',
     'Argumentação sobre o papel das bibliotecas escolares como espaços de transformação social, cidadania e combate às desigualdades no acesso ao conhecimento.',
     'Essay'),


    ('66666666-6666-6666-6666-666666666003',
     'Quadrinho Curto: Biblioteca Viva',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-09-20',
     'História em quadrinhos sobre o cotidiano da biblioteca escolar, com personagens estudantis que descobrem mundos através dos livros e formam um clube secreto de leitores.',
     'Art'),

    ('77777777-7777-7777-7777-777777777003',
     'Infográfico: História do Bairro',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-11-11',
     'Linha do tempo visual com marcos históricos da comunidade escolar, desde a fundação do bairro até a inauguração da biblioteca, integrando fotos antigas e depoimentos de moradores.',
     'Infographic'),

    ('88888888-8888-8888-8888-888888888003',
     'Entrevista: Mestres da Comunidade',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-10-22',
     'Série de entrevistas em vídeo com lideranças locais e artistas populares, documentando saberes tradicionais, histórias de vida e o papel da cultura na identidade comunitária.',
     'Multimedia'),

    ('99999999-9999-9999-9999-999999999003',
     'Literatura em Libras: Pequenas Coragens',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '2024-12-02',
     'Narração visual em Libras sobre autonomia e pertencimento, adaptada de um conto original que celebra as pequenas vitórias cotidianas de uma criança surda na escola regular.',
     'LibraLiterature');


-- ============================================================
-- ARTICLE (subtipo: Artigo / Reportagem / Matéria)
-- ============================================================
INSERT INTO article (id, content)
VALUES ('11111111-1111-1111-1111-111111111001',
        'O Rio Capibaribe nasce na Serra do Jacarará, no município de Poço de José de Moura, e percorre cerca de 240 quilômetros até desaguar no Oceano Atlântico, no Recife. Ao longo de seu curso, o rio abastece dezenas de municípios pernambucanos e é palco de uma rica biodiversidade. No entanto, nas últimas décadas, o avanço da urbanização desordenada, o lançamento de efluentes domésticos sem tratamento e o desmatamento das matas ciliares comprometeram seriamente a qualidade de suas águas. Em visita à margem do rio próxima à cidade de Caruaru, esta reportagem observou a presença de resíduos sólidos, coloração turva da água e ausência de vegetação nativa nas margens. Especialistas ouvidos pela equipe apontam que a recuperação do rio depende de ações integradas entre poder público, setor privado e sociedade civil, incluindo programas de saneamento básico, reflorestamento das margens e educação ambiental nas escolas. Algumas iniciativas já estão em curso: o Projeto Capibaribe Vivo reúne voluntários para mutirões de limpeza mensais e realiza atividades de conscientização com estudantes da rede pública. A recuperação do rio é possível, mas exige comprometimento de todos.'),

       ('11111111-1111-1111-1111-111111111002',
        'Estudos recentes revelam que comunidades com acesso a projetos estruturados de incentivo à leitura apresentam melhora significativa nos indicadores de aprendizagem e redução do abandono escolar. Em Garanhuns, dois projetos locais vêm transformando esse cenário: o "Ler é Viver", que atua em escolas públicas do bairro Alto do Casarão, e o "Leitura em Movimento", desenvolvido por uma ONG em parceria com a biblioteca municipal. Nesta reportagem, conversamos com coordenadores, voluntários e famílias beneficiadas para entender como a leitura coletiva muda vidas e fortalece laços comunitários. "Antes, minha filha mal pegava um livro. Hoje ela lê para os irmãos mais novos toda tarde", conta dona Maria das Graças, mãe de três crianças atendidas pelo projeto. Os números corroboram a experiência: segundo levantamento interno, 78% dos participantes melhoraram o desempenho em interpretação de texto no semestre seguinte à entrada no programa. O artigo defende que a democratização do acesso à literatura é uma política pública urgente e que modelos comunitários de incentivo à leitura merecem maior investimento e reconhecimento institucional.'),

       ('11111111-1111-1111-1111-111111111003',
        'Esta matéria especial reúne seis entrevistas com estudantes do 2º e 3º ano do ensino médio de uma escola pública de Garanhuns, com o objetivo de compreender como cada um deles se relaciona com a escrita e a produção textual. Os relatos revelam universos distintos: há quem escreva diários desde os doze anos, quem só tenha descoberto o prazer da escrita em uma oficina literária oferecida pela escola, e quem ainda enfrente bloqueio criativo toda vez que precisa começar um texto. A jornalista-estudante responsável pela matéria conduziu as entrevistas ao longo de três semanas, com questões abertas sobre inspiração, dificuldades e sonhos relacionados à escrita. Um dado chamou atenção: dos seis entrevistados, cinco citaram professoras — e não professores — como as maiores incentivadoras de sua relação com a linguagem escrita. A matéria também traz um box com dicas de escrita criativa sugeridas pelos próprios estudantes, mostrando que o conhecimento sobre o ofício da escrita já está sendo construído de forma colaborativa nas salas de aula.');


-- ============================================================
-- ART (Arte visual / Quadrinho / Cartaz)
-- ============================================================
INSERT INTO art (id, url)
VALUES ('66666666-6666-6666-6666-666666666001',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ4n2xFm8T2OKwmXBZnGvIxpqB1rKVvtKrEUw&s'),
       ('66666666-6666-6666-6666-666666666002',
        'https://i.pinimg.com/originals/00/58/69/0058690f7f7a613325d957e89822eceb.jpg'),
       ('66666666-6666-6666-6666-666666666003',
        'https://sme.goiania.go.gov.br/conexaoescola/wp-content/uploads/2024/03/image-17.png'),
       ('66616666-6666-6666-6666-666666666001', 'https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgCT-BwTM9XX75W_0gWyqC-C0pyMJ3L0BZQg4ZbE_zQyswGLN6Ja1UHLuSoBiLvSYNqkVmcTF5L2P-xf2QlmS02F1P9HKs9tMq1t6jZtxBnTOSy5jc8jG7oKnqhY0vV17zN4dT8HCx6pqe2/s0/Casal+xilo+6.jpg'),
       ('66626666-6666-6666-6666-666666666001', 'https://img.freepik.com/vetores-premium/cordel-brasileiro-mulher-com-cacto-e-papagaio-estilo-xilogravura_419911-1863.jpg?semt=ais_hybrid&w=740&q=80'),
       ('66636666-6666-6666-6666-666666666001', 'https://d1o6h00a1h5k7q.cloudfront.net/imagens/img_m/13347/6027474.jpg');

-- ============================================================
-- CORDEL
-- ============================================================


INSERT INTO cordel (id, content, rhyme_scheme,illustration_id)
VALUES ('22222222-2222-2222-2222-222222222001',
        E'Na feira de Garanhuns\nTem coisa que não tem par\nO cheiro de coentro fresco\nQue faz a gente sonhar\nO Seu Antônio de Souza\nVende rapadura no ar\n\nA dona Graça da tapioca\nChega cedo toda manhã\nCom sua frigideira quente\nE sua mão de artesã\nFaz goma com jeito e graça\nNuma dança pernambucã\n\nO repentista Zé Aboio\nChega com seu violão\nDesafia quem vier\nNo grito e no coração\nA feira ri e balança\nAo som de sua canção\n\nVem gente de todo canto\nDe Lajedo e de Bom Conselho\nTraz feijão, milho e abóbora\nCarregado no velho espelho\nDa vida simples do campo\nQue reluz como um vermelho',
        'ABABCC',
        '66616666-6666-6666-6666-666666666001'),

       ('22222222-2222-2222-2222-222222222002',
        E'O sertão não é só seca\nNem só pedra e solidão\nÉ memória que não morre\nNa voz do velho sertão\nÉ cultura que resiste\nNo peito do nordestão\n\nTem a história dos ancestrais\nQue plantaram nessa terra\nSangue e suor derramados\nNo tempo de paz e guerra\nUma herança que se guarda\nComo pedra que não erra\n\nO cangaceiro virou lenda\nO vaqueiro virou poema\nA rendeira virou símbolo\nDe um povo que não se trema\nNordeste é raiz e flor\nQue nenhuma seca drema',
        'AABBCC',
        '66626666-6666-6666-6666-666666666001'),

       ('22222222-2222-2222-2222-222222222003',
        E'Somos jovens do sertão\nCom olho no horizonte\nBuscamos o conhecimento\nFeito água numa fonte\nNossa escola é nossa força\nNosso estudo é nossa fronte\n\nEstudamos de manhã cedo\nE voltamos pela noite\nCarregando nosso sonho\nQue o cansaço não açoite\nPois o saber é a chave\nQue o futuro nos acoite\n\nDedico esse cordel\nA cada jovem que luta\nQue enfrenta o vestibular\nCom garra, fé e conduta\nO diploma não é fim\nMas começo de uma luta',
        'ABABAB',
        '66636666-6666-6666-6666-666666666001');


-- ============================================================
-- ESSAY (Redação ENEM)
-- ============================================================
INSERT INTO essay (id, content, rate, theme, theme_description, feedback)
VALUES ('33333333-3333-3333-3333-333333333001',
        'A Revolução Industrial do século XVIII inaugurou uma era marcada pela aceleração tecnológica e pela profunda transformação nas relações de trabalho e comunicação. Séculos depois, a chamada Revolução Digital prometia democratizar o acesso à informação e ao conhecimento — promessa parcialmente cumprida. No Brasil contemporâneo, a exclusão digital permanece como um dos principais mecanismos de reprodução das desigualdades sociais, impedindo que parcelas significativas da população exerçam plenamente sua cidadania em um mundo cada vez mais mediado por telas e algoritmos. Dados do IBGE revelam que, em 2022, cerca de 82 milhões de brasileiros tinham acesso à internet, mas a qualidade e a finalidade desse acesso variam drasticamente conforme a renda: enquanto jovens de classes mais abastadas utilizam a rede para estudar, criar e empreender, jovens periféricos frequentemente acessam a internet apenas pelo celular pré-pago, sem condições de participar de processos seletivos online, cursos ou plataformas educacionais. Para reverter esse quadro, é necessária uma atuação articulada entre Estado, setor privado e sociedade civil. O poder público deve ampliar a infraestrutura de conectividade em áreas rurais e periferias urbanas, garantindo acesso gratuito e de qualidade nas escolas públicas. As empresas de tecnologia devem ser responsabilizadas pela oferta de tarifas sociais. E a escola precisa assumir seu papel de formadora de cidadãos digitais críticos, capazes de navegar com autonomia e segurança no ambiente virtual.',
        920,
        'Inclusão digital',
        'Impactos da exclusão digital na formação de jovens brasileiros e no exercício da cidadania.',
        'Excelente articulação de repertório sociocultural com a tese central. A proposta de intervenção é detalhada, com agente, ação e efeito bem definidos. Sugere-se aprofundar a discussão sobre o papel das políticas públicas municipais, além das federais, para enriquecer a argumentação.'),

       ('33333333-3333-3333-3333-333333333002',
        'As cidades brasileiras vivem uma contradição estrutural: crescem em população e em área urbanizada, mas não crescem em infraestrutura, planejamento e serviços públicos de qualidade. Esse desequilíbrio se manifesta de forma aguda nas questões de mobilidade urbana e gestão de resíduos sólidos, dois pilares fundamentais da sustentabilidade das metrópoles e das cidades médias. No campo da mobilidade, o modelo centrado no automóvel particular, incentivado por décadas de políticas tributárias e de crédito, gerou congestionamentos crônicos, aumento das emissões de gases de efeito estufa e exclusão dos trabalhadores que dependem do transporte coletivo. Cidades como Curitiba e São Paulo experimentam soluções distintas — o BRT curitibano e o Bilhete Único paulistano —, mas ambas ainda convivem com sistemas sobrecarregados e desiguais. Quanto aos resíduos, a Política Nacional de Resíduos Sólidos, aprovada em 2010, estabeleceu metas ambiciosas de reciclagem e logística reversa, mas sua implementação esbarra em falta de recursos, de articulação intergovernamental e de cultura de separação dos resíduos pela população. A construção de cidades mais sustentáveis exige a integração entre planejamento urbano, educação ambiental e participação social, com políticas públicas que tratem mobilidade e resíduos como dimensões indissociáveis do direito à cidade.',
        880,
        'Sustentabilidade urbana',
        'Desafios de mobilidade e gestão de resíduos em centros urbanos brasileiros.',
        'Estrutura dissertativa sólida com boa progressão temática. O repertório histórico e legal é pertinente. Para a próxima versão, aprofunde o repertório sociocultural na conclusão — uma referência filosófica ou literária fortaleceria o fechamento do texto e demonstraria maior domínio de competência 2.'),

       ('33333333-3333-3333-3333-333333333003',
        'Em 1948, a Declaração Universal dos Direitos Humanos proclamou o direito à educação como fundamental e inalienável. Décadas depois, no Brasil, esse direito ancora-se na Constituição Federal de 1988 e na Lei de Diretrizes e Bases da Educação. No entanto, o acesso à leitura e às bibliotecas escolares — instrumento essencial para a efetivação do direito à educação — permanece precário e desigual. Segundo o Censo Escolar de 2022, apenas 33% das escolas públicas brasileiras possuem biblioteca ou sala de leitura, número que cai para 19% nas escolas rurais. Essa ausência não é um dado neutro: ela reflete e perpetua a exclusão de parcelas da população do acesso ao conhecimento, à cultura e ao exercício pleno da cidadania. A filósofa Hannah Arendt definia a cidadania como o direito a ter direitos; nesse sentido, a biblioteca escolar é um espaço político, não apenas pedagógico — ela viabiliza o acesso à palavra, à história e ao pensamento crítico que sustentam a participação democrática. Para transformar essa realidade, o Estado deve priorizar a implementação do Programa Nacional Biblioteca da Escola (PNBE), com financiamento adequado, formação de mediadores de leitura e criação de acervos diversificados e representativos das culturas regionais. Ler é um ato de cidadania. Garantir esse ato a todos é uma obrigação do Estado e uma escolha de sociedade.',
        960,
        'Leitura e cidadania',
        'Relação entre bibliotecas escolares, acesso ao conhecimento e participação social no Brasil.',
        'Texto excepcional. A progressão argumentativa é rigorosa e a coesão textual, impecável. O uso da referência de Hannah Arendt é sofisticado e bem integrado à tese. A proposta de intervenção menciona o PNBE com clareza e responsabilidade do agente. Parabéns — nota máxima justificada.');


-- ============================================================
-- SHORT_STORY (Crônicas)
-- ============================================================
INSERT INTO short_story (id, content)
VALUES
    ('44444444-4444-4444-4444-444444444001',
     'Toda casa de infância tem uma janela que guarda o mundo inteiro. Na minha, era a do quarto dos fundos, pintada de azul desbotado, com a tinta lascando nas bordas como se tentasse mostrar outra cor por baixo. Nas férias de julho, quando a garoa da serra fechava a rua e não tinha pra onde ir, eu ficava horas ali. Não era tédio — era uma espécie de escuta. O quintal do vizinho, a mangueira que nunca deu manga, o gato amarelo que aparecia toda tarde no muro. A janela azul me ensinou que observar é uma forma de pertencer a um lugar. Hoje moro longe daquela rua. Mas toda vez que chove fino e o frio cheira a terra molhada, eu volto àquela janela. E fico me perguntando se o gato amarelo ainda aparece.'),

    ('44444444-4444-4444-4444-444444444002',
     'Garanhuns tem um jeito próprio de chover. Não é a chuva grossa do litoral nem o temporal rápido do sertão — é uma chuva mansa, de sermão longo, que começa sem avisar e para quando quer. Certa tarde de fevereiro, fui pego por uma dessas chuvas na saída da escola junto com meu amigo Felipe. A gente se enfiou debaixo de um alpendre de loja fechada e ficou olhando a rua virar rio. Não falamos muito. Mas foi naquele silêncio molhado que eu entendi que ele ia embora pro Recife no fim do mês, e que as coisas iam mudar de um jeito que a gente ainda não sabia nomear. A chuva parou. A gente se despediu como se fosse qualquer tarde. Às vezes acho que a chuva entendeu melhor do que a gente o que estava acontecendo ali.'),

    ('44444444-4444-4444-4444-444444444003',
     'O trem das seis era sempre a mesma coisa e nunca era igual. Os rostos mudavam, as sacolas mudavam, o cansaço tinha graduações diferentes dependendo do dia da semana. Eu pegava esse trem toda quarta-feira, e fui aprendendo a ler as pessoas como se fossem texto. A senhora do tricô que nunca olhava pro fio — ela sabia de cor cada ponto, cada nó. O rapaz do fone vermelho que desenhava no caderno pequeno — nunca vi o que ele desenhava, mas imagino que era algo que não cabia em palavras. E a dona Conceição do banco do fundo, que fechava os olhos no balançar do vagão com uma expressão que não era sono — era lembrança. O trem das seis me ensinou que as pessoas carregam mundos inteiros e não dizem nada. Basta saber olhar.');


-- ============================================================
-- TALE (Contos)
-- ============================================================
INSERT INTO tale (id, content, genre)
VALUES
    ('55555555-5555-5555-5555-555555555003',
     'Havia uma floresta onde todos os pássaros cantavam sozinhos. Cada um tinha sua melodia, sua hora, seu galho preferido. Era bonito, mas era também um pouco triste — os cantos se perdiam no vento sem encontrar resposta. Um dia chegou à floresta um pássaro dourado que ninguém conhecia. Ele não era o maior, nem o mais colorido, mas tinha o hábito estranho de pousar ao lado dos outros e escutar antes de cantar. Depois de escutar, acrescentava apenas uma nota — uma só — que completava o que o outro havia começado. Foi a corruíra — o menor de todos — que percebeu primeiro o que ele fazia. Tentou, tímida, completar a melodia. O pássaro dourado a olhou e assobiou de volta, começando onde ela tinha parado. Aos poucos a floresta foi parando para ouvir. Até o gavião, que nunca havia cantado na frente de ninguém, completou um trecho numa manhã de neblina, e ninguém riu. Quando o pássaro dourado partiu — tão silenciosamente quanto chegou — a floresta já era outra. Não mais um conjunto de cantos solitários, mas uma conversa longa e sem fim.',
     'fábula'),

    ('55555555-5555-5555-5555-555555555001',
     'Quando a aldeia acordou naquela manhã, a pedra já estava lá. Não havia trilha que levasse até ela, não havia marca de pé nem de roda no chão de terra vermelha ao redor. Era grande como uma mulher sentada, com as mãos espalmadas sobre os joelhos e os olhos voltados para o nascente — mas não tinha olhos de pedra; tinha uma superfície lisa e escura que capturava a luz do sol de um jeito que deixava os mais velhos desconfortáveis. Iara foi a primeira a se aproximar. Tinha doze anos e a reputação de não ter medo de nada. Colocou a mão na pedra fria e sentiu — ela jura até hoje — um batimento. Lento, profundo, como o de alguém dormindo. Os mais velhos da aldeia convocaram uma reunião naquela noite. Decidiram não tocar na pedra, não cercá-la, não tentar mover. Só observar. Três semanas depois, as primeiras nuvens de chuva apareceram num céu que estava seco há sete meses. Ninguém disse que foi a pedra. Mas todo mundo pensou.',
     'fantasia regional'),

    ('55555555-5555-5555-5555-555555555002',
     'A menina tinha andado três horas pela caatinga quando percebeu que não sabia mais de onde tinha vindo. O sol estava no meio do céu — o pior lugar possível para quem precisa se orientar — e a única coisa que se movia era o vento, vindo do norte com um cheiro de poeira antiga. Ela parou, bebeu o resto da água do cantil e avaliou suas opções: nenhuma boa. Foi então que o vento mudou. Não ficou mais forte — ficou diferente. Tinha uma direção que parecia querer alguma coisa, um sopro que empurrava levemente para a esquerda do caminho que ela estava seguindo. A menina desconfiou. Mas desconfiança não mata a sede. Seguiu para a esquerda. Meia hora depois encontrou o açude — pequeno, metade evaporado, mas com água. Quando terminou de beber e olhou para o horizonte, o vento já era outro, igual a todos os outros. Ela nunca contou essa história pra ninguém, porque sabia que ninguém ia acreditar. Mas na vida toda, quando estava em dúvida, fechava os olhos e esperava um vento do norte.',
     'conto popular');


-- ============================================================
-- INFOGRAPHIC
-- ============================================================
INSERT INTO infographic (id, url)
VALUES ('77777777-7777-7777-7777-777777777001',
        'https://prof.brunocarvalho.pt/wp-content/uploads/2020/04/ciclo_agua-750x445.jpg'),
       ('77777777-7777-7777-7777-777777777002',
        'https://nutritotal.com.br/pro/wp-content/uploads/2013/05/helathy-plate.jpg.webp'),
       ('77777777-7777-7777-7777-777777777003',
        'https://s2-g1.glbimg.com/w5Ff85xZ_kBVpg655v_luBVzp3U=/0x0:1200x1800/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_59edd422c0c84a879bd37670ae4f538a/internal_photos/bs/2023/w/y/lo1WvfQwCMMH6fs1puCA/infografico-curitiba-em-numeros.png');


-- ============================================================
-- MULTIMEDIA (Curta / Vídeo-poema / Entrevista)
-- durations em segundos
-- ============================================================
INSERT INTO multimedia (id, url, duration)
VALUES ('88888888-8888-8888-8888-888888888001',
        'https://www.youtube.com/watch?v=dQw4w9WgXcQ&pp=ygUXbmV2ZXIgZ29ubmEgZ2l2ZSB5b3UgdXDSBwkJ1AoBhyohjO8%3D',
        400),
       ('88888888-8888-8888-8888-888888888002',
        'https://www.youtube.com/watch?v=dQw4w9WgXcQ&pp=ygUXbmV2ZXIgZ29ubmEgZ2l2ZSB5b3UgdXDSBwkJ1AoBhyohjO8%3D',
        180),
       ('88888888-8888-8888-8888-888888888003',
        'https://www.youtube.com/watch?v=dQw4w9WgXcQ&pp=ygUXbmV2ZXIgZ29ubmEgZ2l2ZSB5b3UgdXDSBwkJ1AoBhyohjO8%3D',
        300);


-- ============================================================
-- LIBRA_LITERATURE (Literatura em Libras)
-- durations em segundos
-- ============================================================
INSERT INTO libra_literature (id, url, duration)
VALUES ('99999999-9999-9999-9999-999999999001',
        'https://www.youtube.com/watch?v=dQw4w9WgXcQ&pp=ygUXbmV2ZXIgZ29ubmEgZ2l2ZSB5b3UgdXDSBwkJ1AoBhyohjO8%3D',
        600),
       ('99999999-9999-9999-9999-999999999002',
        'https://www.youtube.com/watch?v=dQw4w9WgXcQ&pp=ygUXbmV2ZXIgZ29ubmEgZ2l2ZSB5b3UgdXDSBwkJ1AoBhyohjO8%3D',
        460),
       ('99999999-9999-9999-9999-999999999003',
        'https://www.youtube.com/watch?v=dQw4w9WgXcQ&pp=ygUXbmV2ZXIgZ29ubmEgZ2l2ZSB5b3UgdXDSBwkJ1AoBhyohjO8%3D',
        378);



INSERT INTO book_club (id, book_author, book_name, book_synopses, date, location, organizer_id)
VALUES ('11111111-1111-1111-1111-111111111001', 'Jorge Amado', 'Gabriela, Cravo e Canela',
        'A história de Gabriela, uma jovem baiana que chega a Ilhéus para trabalhar como empregada doméstica e acaba conquistando o coração de Nacib, um árabe dono de bar. O romance se desenrola em meio às transformações sociais e culturais do Brasil dos anos 1920, explorando temas como amor, poder e identidade.',
        '2026-04-15', 'Biblioteca Municipal', '455150bd-8e40-498c-8005-cca9cefa9099'),
       ('22222222-2222-2222-2222-222222222002', 'Clarice Lispector', 'A Hora da Estrela',
        'A narrativa acompanha Macabéa, uma jovem nordestina que se muda para o Rio de Janeiro em busca de uma vida melhor. Através de uma escrita introspectiva e poética, Clarice Lispector explora a solidão, a busca por identidade e as contradições da existência humana.',
        '2026-04-30', 'Centro Cultural', '455150bd-8e40-498c-8005-cca9cefa9099'),
       ('33333333-3333-3333-3333-333333333003', 'Machado de Assis', 'Dom Casmurro',
        'A história de Bentinho e Capitu, narrada por Bentinho em um tom melancólico e ambíguo. O romance aborda temas como ciúme, traição e a complexidade das relações humanas, deixando o leitor questionando a veracidade dos acontecimentos narrados.',
        '2026-04-25', 'Escola Estadual', '455150bd-8e40-498c-8005-cca9cefa9099');

-- ============================================================
-- BIBLIOTECA VIVA — SEED DE COMENTÁRIOS
-- Aluno 1: e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf
-- Aluno 2: eca64533-6dbd-465b-863c-bb540fecdc61
-- Aluno 3: 4c9f354b-0780-4cdb-b76f-d43e54ea3644
-- ============================================================

INSERT INTO public.comments (id, content, created_at, user_id, work_id)
VALUES

    -- -------------------------------------------------------
    -- Comentários na Reportagem sobre o Rio Capibaribe (Aluno 1)
    -- -------------------------------------------------------
    ('a1000001-0000-0000-0000-000000000001',
     'Que reportagem importante! A gente vê o rio todo dia e não percebe o quanto ele tá sofrendo.',
     '2024-03-12 09:15:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '11111111-1111-1111-1111-111111111001'),

    ('a1000001-0000-0000-0000-000000000002',
     'Minha família mora perto do Capibaribe em Caruaru. Tudo que você descreveu é real, parabéns pela coragem.',
     '2024-03-13 14:40:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '11111111-1111-1111-1111-111111111001'),

    ('a1000001-0000-0000-0000-000000000003',
     'Adorei a parte do Projeto Capibaribe Vivo! Vou pesquisar pra ver se consigo participar.',
     '2024-03-15 11:02:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '11111111-1111-1111-1111-111111111001'),

    -- Corrigindo ordem de parâmetros: (id, content, created_at, user_id, work_id)
    -- -------------------------------------------------------
    -- Comentários no Cordel da Feira Popular (Aluno 1)
    -- -------------------------------------------------------
    ('a2000001-0000-0000-0000-000000000001',
     'Li em voz alta pra minha avó e ela reconheceu o Seu Antônio de Souza hahaha. Muito bom!',
     '2023-09-14 10:30:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '22222222-2222-2222-2222-222222222001'),

    ('a2000001-0000-0000-0000-000000000002',
     'A última estrofe ficou perfeita. Dá pra sentir o cheiro da feira só de ler.',
     '2023-09-16 16:55:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '22222222-2222-2222-2222-222222222001'),

    -- -------------------------------------------------------
    -- Comentários na Redação ENEM: Inclusão Digital (Aluno 1)
    -- -------------------------------------------------------
    ('a3000001-0000-0000-0000-000000000001',
     'Nota 920 bem merecida. A proposta de intervenção ficou muito clara e detalhada.',
     '2024-04-07 08:20:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '33333333-3333-3333-3333-333333333001'),

    ('a3000001-0000-0000-0000-000000000002',
     'Você usou o dado do IBGE de forma muito inteligente. Aprendi muito lendo essa redação.',
     '2024-04-08 19:10:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '33333333-3333-3333-3333-333333333001'),

    -- -------------------------------------------------------
    -- Comentários no Conto da Janela Azul (Aluno 1)
    -- -------------------------------------------------------
    ('a4000001-0000-0000-0000-000000000001',
     'Esse final me deixou arrepiado. A janela azul por dentro também — que imagem linda.',
     '2023-11-23 21:05:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '44444444-4444-4444-4444-444444444001'),

    ('a4000001-0000-0000-0000-000000000002',
     'Parece que eu já vivi isso, sei lá. A gente ficava horas olhando pela janela nas férias.',
     '2023-11-25 17:30:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '44444444-4444-4444-4444-444444444001'),

    -- -------------------------------------------------------
    -- Comentários na Fábula do Pássaro Dourado (Aluno 1)
    -- -------------------------------------------------------
    ('a5000003-0000-0000-0000-000000000001',
     '"Ele simplesmente seguiu. É assim que deve ser quem quer ajudar de verdade." Isso me tocou muito.',
     '2024-08-05 13:45:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '55555555-5555-5555-5555-555555555003'),

    ('a5000003-0000-0000-0000-000000000002',
     'Lembrei do professor que tivemos no 7º ano lendo esse texto. Ele também ficava escutando antes de falar.',
     '2024-08-07 09:00:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '55555555-5555-5555-5555-555555555003'),

    -- -------------------------------------------------------
    -- Comentários no Infográfico: Ciclo da Água (Aluno 1)
    -- -------------------------------------------------------
    ('a7000001-0000-0000-0000-000000000001',
     'Usei esse infográfico pra estudar pra prova de biologia. Ficou muito bem explicado!',
     '2023-10-11 20:15:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '77777777-7777-7777-7777-777777777001'),

    ('a7000001-0000-0000-0000-000000000002',
     'Fácil de entender mesmo sem o professor explicar. Isso é difícil de conseguir num infográfico.',
     '2023-10-13 15:50:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '77777777-7777-7777-7777-777777777001'),

    -- -------------------------------------------------------
    -- Comentários no Curta: Vozes da Juventude (Aluno 1)
    -- -------------------------------------------------------
    ('a8000001-0000-0000-0000-000000000001',
     'Me vi em vários depoimentos desse curta. Obrigada por dar voz pra gente.',
     '2024-02-03 22:10:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '88888888-8888-8888-8888-888888888001'),

    ('a8000001-0000-0000-0000-000000000002',
     'A edição ficou muito boa, especialmente a transição entre os depoimentos. Ficou fluido.',
     '2024-02-05 18:40:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '88888888-8888-8888-8888-888888888001'),

    -- -------------------------------------------------------
    -- Comentários em Literatura em Libras: O Menino e a Lua (Aluno 1)
    -- -------------------------------------------------------
    ('a9000001-0000-0000-0000-000000000001',
     'Minha prima é surda e eu assisti com ela. Ela ficou emocionada. Trabalho lindo.',
     '2024-03-05 14:20:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '99999999-9999-9999-9999-999999999001'),

    ('a9000001-0000-0000-0000-000000000002',
     'A expressão facial durante a narração é muito rica. Dá pra entender mesmo sem saber Libras.',
     '2024-03-06 11:35:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '99999999-9999-9999-9999-999999999001'),

    -- -------------------------------------------------------
    -- Comentários no Artigo: Literatura na Comunidade (Aluno 2)
    -- -------------------------------------------------------
    ('b1000002-0000-0000-0000-000000000001',
     'O depoimento da dona Maria das Graças me fez chorar. Isso é o impacto real que a leitura tem.',
     '2024-05-03 10:05:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '11111111-1111-1111-1111-111111111002'),

    ('b1000002-0000-0000-0000-000000000002',
     'Queria muito ver esse projeto se expandir pra outras escolas. Você já apresentou pra direção?',
     '2024-05-04 16:30:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '11111111-1111-1111-1111-111111111002'),

    -- -------------------------------------------------------
    -- Comentários no Cordel do Sertão Vivo (Aluno 2)
    -- -------------------------------------------------------
    ('b2000002-0000-0000-0000-000000000001',
     'Cada estrofe parece uma pintura. Você tem um dom sério pra linguagem do cordel.',
     '2024-01-21 09:50:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '22222222-2222-2222-2222-222222222002'),

    ('b2000002-0000-0000-0000-000000000002',
     'A rima AABBCC dá um ritmo diferente, mais cadenciado. Ficou ótimo pra leitura em voz alta.',
     '2024-01-22 14:15:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '22222222-2222-2222-2222-222222222002'),

    -- -------------------------------------------------------
    -- Comentários na Redação ENEM: Sustentabilidade Urbana (Aluno 2)
    -- -------------------------------------------------------
    ('b3000002-0000-0000-0000-000000000001',
     'A comparação entre Curitiba e São Paulo foi muito bem construída. Enriqueceu bastante o argumento.',
     '2024-06-12 21:00:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '33333333-3333-3333-3333-333333333002'),

    ('b3000002-0000-0000-0000-000000000002',
     'Citou a PNRS de 2010! Isso é nível avançado. Nota 880 é ótima mas merecia mais ainda.',
     '2024-06-13 08:45:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '33333333-3333-3333-3333-333333333002'),

    -- -------------------------------------------------------
    -- Comentários no Conto da Chuva Mansa (Aluno 2)
    -- -------------------------------------------------------
    ('b4000002-0000-0000-0000-000000000001',
     'O silêncio com textura diferente no final... que jeito bonito de descrever algo tão difícil de dizer.',
     '2024-02-16 20:30:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '44444444-4444-4444-4444-444444444002'),

    ('b4000002-0000-0000-0000-000000000002',
     'A chuva transformando o bairro em outro lugar é exatamente o que acontece aqui em Garanhuns.',
     '2024-02-17 11:20:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '44444444-4444-4444-4444-444444444002'),

    -- -------------------------------------------------------
    -- Comentários na Lenda da Pedra Encantada (Aluno 2)
    -- -------------------------------------------------------
    ('b5000001-0000-0000-0000-000000000001',
     'Fiz uma pesquisa sobre os Xukuru de Ororubá no ano passado. Você retratou com muito respeito.',
     '2024-01-02 17:10:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '55555555-5555-5555-5555-555555555001'),

    ('b5000001-0000-0000-0000-000000000002',
     'Aquela cena dos cantos de chuva arrepia. Narrativa muito poderosa.',
     '2024-01-03 10:55:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '55555555-5555-5555-5555-555555555001'),

    -- -------------------------------------------------------
    -- Comentários no Vídeo-poema: Mar de Dentro (Aluno 2)
    -- -------------------------------------------------------
    ('b8000002-0000-0000-0000-000000000001',
     'A trilha original combinou perfeitamente com a voz. Quem compôs? Ficou muito bom.',
     '2024-06-30 19:25:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '88888888-8888-8888-8888-888888888002'),

    ('b8000002-0000-0000-0000-000000000002',
     'Sertão e mar como metáforas de saudade... isso é poesia de verdade. Compartilhei com a turma.',
     '2024-07-01 08:00:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '88888888-8888-8888-8888-888888888002'),

    -- -------------------------------------------------------
    -- Comentários em Literatura em Libras: A Festa da Rua (Aluno 2)
    -- -------------------------------------------------------
    ('b9000002-0000-0000-0000-000000000001',
     'Assistimos em sala com a professora de AEE. Toda turma ficou encantada com a expressividade.',
     '2024-07-19 15:40:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '99999999-9999-9999-9999-999999999002'),

    ('b9000002-0000-0000-0000-000000000002',
     'A festa junina em Libras ficou tão viva! Deu vontade de dançar só de ver.',
     '2024-07-20 10:15:00',
     '4c9f354b-0780-4cdb-b76f-d43e54ea3644',
     '99999999-9999-9999-9999-999999999002'),

    -- -------------------------------------------------------
    -- Comentários na Matéria Especial: Vozes da Escola (Aluno 3)
    -- -------------------------------------------------------
    ('c1000003-0000-0000-0000-000000000001',
     'Fui uma das entrevistadas! Ver minhas palavras assim, organizadas e contextualizadas, foi surreal.',
     '2024-08-17 12:00:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '11111111-1111-1111-1111-111111111003'),

    ('c1000003-0000-0000-0000-000000000002',
     'Esse dado sobre professoras como incentivadoras me fez refletir muito sobre representatividade.',
     '2024-08-18 20:50:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '11111111-1111-1111-1111-111111111003'),

    -- -------------------------------------------------------
    -- Comentários no Cordel da Juventude (Aluno 3)
    -- -------------------------------------------------------
    ('c2000003-0000-0000-0000-000000000001',
     'Cada verso parece que saiu da minha própria cabeça. A gente vive exatamente isso aqui no sertão.',
     '2024-07-04 09:35:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '22222222-2222-2222-2222-222222222003'),

    ('c2000003-0000-0000-0000-000000000002',
     'A última sextilha sobre o diploma não ser fim mas começo... isso é muito maduro pra nossa idade.',
     '2024-07-05 17:20:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '22222222-2222-2222-2222-222222222003'),

    -- -------------------------------------------------------
    -- Comentários na Redação ENEM: Leitura e Cidadania (Aluno 3)
    -- -------------------------------------------------------
    ('c3000003-0000-0000-0000-000000000001',
     'Nota 960! Merecidíssima. A Hannah Arendt encaixou perfeitamente na tese sobre cidadania.',
     '2024-09-10 07:50:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '33333333-3333-3333-3333-333333333003'),

    ('c3000003-0000-0000-0000-000000000002',
     'Essa redação deveria ser usada como modelo na escola. Sério. Muito acima da média.',
     '2024-09-11 14:00:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '33333333-3333-3333-3333-333333333003'),

    -- -------------------------------------------------------
    -- Comentários no Conto do Trem das Seis (Aluno 3)
    -- -------------------------------------------------------
    ('c4000003-0000-0000-0000-000000000001',
     'A Dona Conceição guardando o amendoim no bolso no final... chorei de verdade.',
     '2024-10-03 22:30:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '44444444-4444-4444-4444-444444444003'),

    ('c4000003-0000-0000-0000-000000000002',
     'O cheiro de óleo e chuva velha no vagão me transportou completamente. Ótima ambientação.',
     '2024-10-04 09:10:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '44444444-4444-4444-4444-444444444003'),

    -- -------------------------------------------------------
    -- Comentários no Conto Popular do Vento Norte (Aluno 3)
    -- -------------------------------------------------------
    ('c5000002-0000-0000-0000-000000000001',
     'A voz do avô narrando ficou tão presente no texto. Você preservou a oralidade muito bem.',
     '2024-03-20 16:45:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '55555555-5555-5555-5555-555555555002'),

    ('c5000002-0000-0000-0000-000000000002',
     'Quem ajuda de verdade não espera agradecer. Essa frase fica. Muito boa a adaptação.',
     '2024-03-21 11:00:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '55555555-5555-5555-5555-555555555002'),

    -- -------------------------------------------------------
    -- Comentários no Infográfico: História do Bairro (Aluno 3)
    -- -------------------------------------------------------
    ('c7000003-0000-0000-0000-000000000001',
     'Meu bisavô aparece numa das fotos antigas! Não sabia que ele estava nessa história. Obrigada.',
     '2024-11-13 19:00:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '77777777-7777-7777-7777-777777777003'),

    ('c7000003-0000-0000-0000-000000000002',
     'Integrar fotos e depoimentos numa linha do tempo foi muito criativo. Ficou rico visualmente.',
     '2024-11-14 08:30:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '77777777-7777-7777-7777-777777777003'),

    -- -------------------------------------------------------
    -- Comentários na Entrevista: Mestres da Comunidade (Aluno 3)
    -- -------------------------------------------------------
    ('c8000003-0000-0000-0000-000000000001',
     'Reconheci o Mestre Severino na entrevista! Ele é incrível. Ótima escolha de entrevistado.',
     '2024-10-24 17:55:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '88888888-8888-8888-8888-888888888003'),

    ('c8000003-0000-0000-0000-000000000002',
     'Documentar esses saberes é urgente. Você fez um trabalho que vai além da escola.',
     '2024-10-25 10:20:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '88888888-8888-8888-8888-888888888003'),

    -- -------------------------------------------------------
    -- Comentários em Literatura em Libras: Pequenas Coragens (Aluno 3)
    -- -------------------------------------------------------
    ('c9000003-0000-0000-0000-000000000001',
     'Que encerramento perfeito pro ano! A história da criança surda na escola regular precisa ser contada.',
     '2024-12-04 21:15:00',
     'e9f2ed4a-2f1b-462b-82c9-0caa80ea7ebf',
     '99999999-9999-9999-9999-999999999003'),

    ('c9000003-0000-0000-0000-000000000002',
     'A expressão de pertencimento no rosto durante a narração diz tudo. Muito sensível e bonito.',
     '2024-12-05 09:40:00',
     'eca64533-6dbd-465b-863c-bb540fecdc61',
     '99999999-9999-9999-9999-999999999003');
