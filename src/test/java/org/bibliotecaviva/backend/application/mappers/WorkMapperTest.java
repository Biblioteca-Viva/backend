package org.bibliotecaviva.backend.application.mappers;

import org.bibliotecaviva.backend.application.dtos.response.textual.ArticleResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.textual.CordelResponseDTO;
import org.bibliotecaviva.backend.domain.entities.textual.Cordel;
import org.bibliotecaviva.backend.domain.entities.textual.Poem;
import org.bibliotecaviva.backend.domain.entities.visual.Art;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkMapperTest {

    private final WorkMapper mapper = Mappers.getMapper(WorkMapper.class);

    @Test
    void cordelShouldExposeLinkedIllustration() {
        Art art = Art.builder().id(UUID.randomUUID()).title("Cover Art")
                .url("https://example.com/cover.png").build();
        Cordel cordel = cordel();
        cordel.setIllustration(art);

        CordelResponseDTO response = (CordelResponseDTO) mapper.toDTO(cordel, 4L, 2L);

        assertEquals(cordel.getId(), response.id());
        assertEquals("External Author", response.author());
        assertEquals("ABAB", response.rhymeScheme());
        assertEquals(4L, response.likeCount());
        assertEquals(2L, response.commentCount());
        assertNotNull(response.illustration());
        assertEquals(art.getId(), response.illustration().id());
        assertEquals("Cover Art", response.illustration().title());
        assertEquals("https://example.com/cover.png", response.illustration().url());
    }

    @Test
    void cordelShouldReturnNullIllustrationWhenItIsNotLinked() {
        CordelResponseDTO response = mapper.toCordelResponseDTO(cordel(), 0L, 0L);

        assertNull(response.illustration());
    }

    @Test
    void poemShouldBeDispatchedAndMapCommonWorkFields() {
        Poem poem = Poem.builder().id(UUID.randomUUID()).title("Poem").authorName("Poet")
                .publicationDate(LocalDateTime.now().minusDays(1)).description("Poem description")
                .content("Poem content").studentClass("Class A").viewCount(7L)
                .rhymeScheme("AABB").poemType("Sonnet").build();

        var response = mapper.toDTO(poem, 3L, 1L);

        assertInstanceOf(ArticleResponseDTO.class, response);
        ArticleResponseDTO poemResponse = (ArticleResponseDTO) response;
        assertEquals(poem.getId(), poemResponse.id());
        assertEquals("Poem", poemResponse.title());
        assertEquals("Poet", poemResponse.author());
        assertEquals("Poem", poemResponse.type());
        assertEquals("Poem content", poemResponse.content());
        assertEquals(7L, poemResponse.viewCount());
        assertEquals(3L, poemResponse.likeCount());
        assertEquals(1L, poemResponse.commentCount());
    }

    private static Cordel cordel() {
        return Cordel.builder().id(UUID.randomUUID()).title("Cordel").authorName("External Author")
                .publicationDate(LocalDateTime.now().minusDays(1)).description("Cordel description")
                .content("Cordel content").rhymeScheme("ABAB").studentClass("Class A")
                .viewCount(5L).build();
    }
}
