# Backend Biblioteca Viva

## Run
### Run Everything with Docker Compose

```powershell
docker compose up --build
```

Backend starts at: `http://localhost:8080`

### Database migrations

A fresh database is created from `docker-initial-sql.sql`, so a clean
`docker compose up` needs nothing else.

An **existing** database needs the scripts in `migrations/` applied by hand, in
filename order. Hibernate runs with `ddl-auto: update`, which creates new tables
but never alters existing constraints — that is what these scripts are for.

```powershell
# replace the two values with DB_USERNAME and DB_NAME from your .env
Get-Content migrations/2026-08-25-add-other-work-type.sql | docker compose exec -T db psql -U <DB_USERNAME> -d <DB_NAME>
```

Skipping a migration usually shows up as a constraint violation on insert, not
as a startup error.


## Docs

After the app is running, open for documentation:

- Scalar UI: `http://localhost:8080/scalar`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Work Types

Every work is stored in the `obras` table with a `type` discriminator, plus a
child table holding the fields specific to that type (JPA `JOINED` inheritance).
Each type has its own creation endpoint; all of them share `GET /work`,
`GET /work/{id}`, likes, comments and delete.

| Type              | Create endpoint            | `?type=` filter    | Specific fields                            |
|-------------------|----------------------------|--------------------|--------------------------------------------|
| Article           | `/work/articles`           | `ARTICLE`          | `content`                                  |
| Poem              | `/work/poems`              | `POEM`             | `content`, `rhymeScheme`, `poemType`       |
| Cordel            | `/work/cordels`            | `CORDEL`           | `content`, `rhymeScheme`, `artName`        |
| Essay             | `/work/essays`             | `ESSAY`            | `content`, `rate`, `theme`, `feedback`     |
| ShortStory        | `/work/short-stories`      | `SHORT_STORY`      | `content`                                  |
| Tale              | `/work/tales`              | `TALE`             | `content`, `genre`                         |
| Art               | `/work/arts`               | `ART`              | `url` *(upload)*                           |
| Infographic       | `/work/infographics`       | `INFOGRAPHIC`      | `url` *(upload)*                           |
| Multimedia        | `/work/multimedias`        | `MULTIMEDIA`       | `url`, `duration`                          |
| LibraLiterature   | `/work/libra-literatures`  | `LIBRA_LITERATURE` | `url`, `duration`                          |
| Other             | `/work/others`             | `OTHER`            | `content`, `url` *(opt)*, `imageUrl` *(upload, opt)* |

`Other` is the general category, for works that do not fit any of the others.
Its `url` field is optional: it accepts an omitted key or an empty string, and
only a genuinely malformed address returns `400`. Its image is optional too and
is sent as a file, never as a URL. When present, `imageUrl` is used as the
thumbnail in listings and on the home dashboard.

### Image uploads

`arts`, `infographics` and `others` take `multipart/form-data` instead of a JSON
body, on both create and update:

- part `data` (`application/json`): the request DTO, which carries no image field;
- part `image` (file): JPG/JPEG or PNG, uploaded to Cloudinary, whose public URL
  is persisted (`url` for visual works, `imageUrl` for `others`).

The `image` part is required for `arts` and `infographics` on create, and
optional everywhere else. On update, omitting it keeps the current image.

Adding a type means: a new entity, request and response DTO, one value in
`WorkTypes`, one case in `WorkMapper` and `WorkService`, the endpoints in
`WorkController`, a counter in `HomePageDashboardResponseDTO`, and a migration
extending the `obras_type_check` constraint.

## Registered Users

| Username  | Password | Email               | Role    |
|-----------|----------|---------------------|---------|
| admin     | 123456   | admin@teste.com     | ADMIN   |
| aluno1    | 123456     | aluno1@teste.com    | ALUNO   ||
| aluno2    | 123456     | aluno1@teste.com    | ALUNO   ||
| aluno3    | 123456     | aluno1@teste.com    | ALUNO   ||
| professor | 123456     | professor@teste.com | CURADOR ||
