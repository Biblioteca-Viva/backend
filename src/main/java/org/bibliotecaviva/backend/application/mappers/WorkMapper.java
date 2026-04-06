package org.bibliotecaviva.backend.application.mappers;


import org.bibliotecaviva.backend.application.dtos.request.audiovisual.LibraLiteratureRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.MultimediaRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.ArticleRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.CordelRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.EssayRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.ShortStoryRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.TaleRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.ArtRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.InfographicRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.*;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.LibraLiteratureResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.MultimediaResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.textual.*;
import org.bibliotecaviva.backend.application.dtos.response.visual.ArtResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.visual.InfographicReponseDTO;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.WorkSummary;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;
import org.bibliotecaviva.backend.domain.entities.audiovisual.Multimedia;
import org.bibliotecaviva.backend.domain.entities.textual.*;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.visual.Art;
import org.bibliotecaviva.backend.domain.entities.visual.Infographic;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WorkMapper {

    default WorkResponse toWorkDTO(WorkSummary summary){
        return toWorkSummary(summary);
    }

    default WorkResponse toDTO(Work work) {
        return switch (work) {
            case LibraLiterature w -> toLibraLiteratureResponseDTO(w);
            case Multimedia w -> toMultimediaResponseDTO(w);
            case Article w -> toArticleResponseDTO(w);
            case Cordel w -> toCordelResponseDTO(w);
            case Essay w -> toEssayResponseDTO(w);
            case ShortStory w -> toShortStoryResponseDTO(w);
            case Tale w -> toTaleResponseDTO(w);
            case Art w -> toArtResponseDTO(w);
            case Infographic w -> toInfographicReponseDTO(w);
            default -> throw new IllegalStateException("Unexpected value: " + work);
        };
    }

    default String map(User user) {
        return user.getName();
    }

    // mapeamento pra work summary
    WorkResponseDTO toWorkSummary (WorkSummary work);

    // mapeamentos específicos de cada entidade
    LibraLiteratureResponseDTO toLibraLiteratureResponseDTO(LibraLiterature libraLiterature);
    MultimediaResponseDTO toMultimediaResponseDTO(Multimedia Multimedia);
    ArticleResponseDTO toArticleResponseDTO(Article article);
    CordelResponseDTO toCordelResponseDTO(Cordel cordel);
    EssayResponseDTO toEssayResponseDTO(Essay essay);
    ShortStoryResponseDTO toShortStoryResponseDTO(ShortStory shortStory);
    TaleResponseDTO toTaleResponseDTO(Tale tale);
    ArtResponseDTO toArtResponseDTO(Art art);
    InfographicReponseDTO toInfographicReponseDTO(Infographic infographic);

    // daqui pra baixo separar ppor classe do mapper se prcisar, ver depois
    @Mapping(target = "author",ignore = true)
    LibraLiterature toEntity(LibraLiteratureRequestDTO libraLiteratureRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "author",ignore = true)
    void partialUpdate(LibraLiteratureRequestDTO libraLiteratureRequestDTO, @MappingTarget LibraLiterature libraLiterature);

    @Mapping(target = "author",ignore = true)
    Multimedia toEntity(MultimediaRequestDTO multimediaRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    @Mapping(target = "author",ignore = true)
    void partialUpdate(MultimediaRequestDTO multimediaRequestDTO, @MappingTarget Multimedia multimedia);

    @Mapping(target = "author",ignore = true)
    Article toEntity(ArticleRequestDTO articleRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    @Mapping(target = "author",ignore = true)
    void partialUpdate(ArticleRequestDTO articleRequestDTO, @MappingTarget Article article);

    @Mapping(target = "author",ignore = true)
    Cordel toEntity(CordelRequestDTO cordelRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    @Mapping(target = "author",ignore = true)
    void partialUpdate(CordelRequestDTO cordelRequestDTO, @MappingTarget Cordel cordel);

    @Mapping(target = "author",ignore = true)
    Essay toEntity(EssayRequestDTO essayRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    @Mapping(target = "author",ignore = true)
    void partialUpdate(EssayRequestDTO essayRequestDTO, @MappingTarget Essay essay);

    @Mapping(target = "author",ignore = true)
    ShortStory toEntity(ShortStoryRequestDTO shortStoryRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    @Mapping(target = "author",ignore = true)
    void partialUpdate(ShortStoryRequestDTO shortStoryRequestDTO, @MappingTarget ShortStory shortStory);

    @Mapping(target = "author",ignore = true)
    Tale toEntity(TaleRequestDTO taleRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    @Mapping(target = "author",ignore = true)
    void partialUpdate(TaleRequestDTO taleRequestDTO, @MappingTarget Tale tale);

    @Mapping(target = "author",ignore = true)
    Art toEntity(ArtRequestDTO artRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)    @Mapping(target = "author",ignore = true)
    void partialUpdate(ArtRequestDTO artRequestDTO, @MappingTarget Art art);

    @Mapping(target = "author",ignore = true)
    Infographic toEntity(InfographicRequestDTO infographicRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)     @Mapping(target = "author",ignore = true)
    void partialUpdate(InfographicRequestDTO infographicRequestDTO, @MappingTarget Infographic infographic);
}
