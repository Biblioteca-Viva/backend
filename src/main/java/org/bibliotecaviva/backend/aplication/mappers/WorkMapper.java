package org.bibliotecaviva.backend.aplication.mappers;


import org.bibliotecaviva.backend.aplication.dtos.request.*;
import org.bibliotecaviva.backend.aplication.dtos.response.WorkResponseDTO;
import org.bibliotecaviva.backend.aplication.dtos.response.audiovisual.LibraLiteratureResponseDTO;
import org.bibliotecaviva.backend.aplication.dtos.response.audiovisual.MultimediaResponseDTO;
import org.bibliotecaviva.backend.aplication.dtos.response.textual.*;
import org.bibliotecaviva.backend.aplication.dtos.response.visual.ArtResponseDTO;
import org.bibliotecaviva.backend.aplication.dtos.response.visual.InfographicReponseDTO;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.WorkSummary;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;
import org.bibliotecaviva.backend.domain.entities.audiovisual.Multimedia;
import org.bibliotecaviva.backend.domain.entities.textual.*;
import org.bibliotecaviva.backend.domain.entities.visual.Art;
import org.bibliotecaviva.backend.domain.entities.visual.Infographic;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface WorkMapper {

    WorkResponseDTO toWorkDTO(WorkSummary summary);
    default WorkResponseDTO toDTO(Work work) {
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
            default ->
                    throw new IllegalArgumentException("Tipo de Work não mapeado: " + work.getClass().getSimpleName());
        };
    }

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
    LibraLiterature toEntity(LibraLiteratureRequestDTO libraLiteratureRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LibraLiterature partialUpdate(LibraLiteratureRequestDTO libraLiteratureRequestDTO, @MappingTarget LibraLiterature libraLiterature);

    Multimedia toEntity(MultimediaRequestDTO multimediaRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Multimedia partialUpdate(MultimediaRequestDTO multimediaRequestDTO, @MappingTarget Multimedia multimedia);

    Article toEntity(ArticleRequestDTO articleRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Article partialUpdate(ArticleRequestDTO articleRequestDTO, @MappingTarget Article article);

    Cordel toEntity(CordelRequestDTO cordelRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Cordel partialUpdate(CordelRequestDTO cordelRequestDTO, @MappingTarget Cordel cordel);

    Essay toEntity(EssayRequestDTO essayRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Essay partialUpdate(EssayRequestDTO essayRequestDTO, @MappingTarget Essay essay);

    ShortStory toEntity(ShortStoryRequestDTO shortStoryRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ShortStory partialUpdate(ShortStoryRequestDTO shortStoryRequestDTO, @MappingTarget ShortStory shortStory);

    Tale toEntity(TaleRequestDTO taleRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Tale partialUpdate(TaleRequestDTO taleRequestDTO, @MappingTarget Tale tale);

    Art toEntity(ArtRequestDTO artRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Art partialUpdate(ArtRequestDTO artRequestDTO, @MappingTarget Art art);

    Infographic toEntity(InfographicRequestDTO infographicRequestDTO);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Infographic partialUpdate(InfographicRequestDTO infographicRequestDTO, @MappingTarget Infographic infographic);
}
