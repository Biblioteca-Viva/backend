package org.bibliotecaviva.backend.application.mappers;

import org.bibliotecaviva.backend.application.dtos.request.BookClubRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.BookClubResponseDTO;
import org.bibliotecaviva.backend.domain.entities.BookClub;
import org.bibliotecaviva.backend.domain.entities.User;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookClubMapper {
    @Mapping(target = "organizer", source = "organizer")
    @Mapping(target = "id",ignore = true)
    BookClub toEntity(BookClubRequestDTO bookClubRequestDTO, User organizer);


    @Mapping(target = "organizerId", source = "bookClub.organizer")
    BookClubResponseDTO toDto(BookClub bookClub, Long participantsCount);

    default String map(User user) {
        return user != null ? user.getId().toString() : null;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    BookClub partialUpdate(BookClubRequestDTO bookClubRequestDTO, @MappingTarget BookClub bookClub);
}