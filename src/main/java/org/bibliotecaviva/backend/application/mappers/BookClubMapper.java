package org.bibliotecaviva.backend.application.mappers;

import org.bibliotecaviva.backend.application.dtos.request.BookClubRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.BookClubResponseDTO;
import org.bibliotecaviva.backend.domain.entities.BookClub;
import org.bibliotecaviva.backend.domain.entities.User;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookClubMapper {
    @Mapping(target = "organizer", source = "organizer")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookCoverUrl", ignore = true)
    BookClub toEntity(BookClubRequestDTO bookClubRequestDTO, User organizer);


    @Mapping(target = "organizerName", source = "bookClub.organizer.name")
    @Mapping(target = "averageRating", source = "averageRating")
    BookClubResponseDTO toDto(BookClub bookClub, Long participantsCount, BigDecimal averageRating);

    default String map(User user) {
        return user != null ? user.getId().toString() : null;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookCoverUrl", ignore = true)
    BookClub partialUpdate(BookClubRequestDTO bookClubRequestDTO, @MappingTarget BookClub bookClub);
}