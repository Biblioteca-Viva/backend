package org.bibliotecaviva.backend.application.mappers;

import org.bibliotecaviva.backend.application.dtos.request.NewsRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.NewsResponseDTO;
import org.bibliotecaviva.backend.domain.entities.News;
import org.bibliotecaviva.backend.domain.entities.User;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface NewsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", source = "author")
    News toEntity(NewsRequestDTO dto, User author);

    @Mapping(target = "authorName", source = "author.name")
    NewsResponseDTO toDto(News news);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    News partialUpdate(NewsRequestDTO dto, @MappingTarget News news);
}
