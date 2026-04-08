package org.bibliotecaviva.backend.application.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bibliotecaviva.backend.application.dtos.request.WorkRequest;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.LibraLiteratureRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.MultimediaRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.*;
import org.bibliotecaviva.backend.application.dtos.request.visual.ArtRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.InfographicRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;
import org.bibliotecaviva.backend.application.mappers.WorkMapper;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;
import org.bibliotecaviva.backend.domain.entities.audiovisual.Multimedia;
import org.bibliotecaviva.backend.domain.entities.textual.*;
import org.bibliotecaviva.backend.domain.entities.visual.Art;
import org.bibliotecaviva.backend.domain.entities.visual.Infographic;
import org.bibliotecaviva.backend.domain.exceptions.UserNotFoundException;
import org.bibliotecaviva.backend.domain.exceptions.WorkAlreadyExistsException;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistance.repository.UserRepository;
import org.bibliotecaviva.backend.persistance.repository.WorkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor

public class WorkService {

    private final WorkRepository workRepository;
    private final WorkMapper workMapper;
    private final UserRepository userRepository;

    /*
     * Puxa todos da tabela works usando uma interface com atributos específicos
     * para nao requisitar tudo do banco
     */
    public List<WorkResponse> getAll(String type) {
        return workRepository.findAllSummary(type)
                .stream()
                .map(workMapper::toWorkDTO)
                .toList();
    }

    //todo: resolver os 1 milhao de join, mandar o tipo na req ou pegar sla cachear dps se ficar pesado,
    // pra evitar ficar fazendo update toda hr no banco pras views
    public WorkResponse getById(UUID id) {
        var work = workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + id + " não encontrada"));
        workRepository.incrementViewCount(id);
        return workMapper.toDTO(work, workRepository.getLikeCount(id));
    }

    //TODO: cachear se necessario ou fazer um sistema de like mais complexo pra evitar ficar dando update toda hor
    // string por enquanto somente pra debug, dps void
    @Transactional
    public String like(UUID workId, User user) {
        workRepository.findById(workId)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + workId + " não encontrada"));
        var userId = user.getId();
        var liked = false;
        if (userRepository.existsLike(userId, workId)) {
            userRepository.unlikeWork(userId, workId);
        } else {
            userRepository.likeWork(userId, workId);
            liked = true;
        }

        return String.format("Obra com id %s: %s com sucesso",workId, liked ? "curtida" : "descurtida");
    }

    @Transactional
    public void delete(UUID id) {
        workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra não encontrada"));
        workRepository.deleteById(id);
    }

    @Transactional
    public <T extends WorkRequest> WorkResponse create(T dto) {

        User author = userRepository.findByEmail(dto.author())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com email: " + dto.author()));

        Work work = switch (dto) {
            case EssayRequestDTO d -> workMapper.toEntity(d);
            case ArtRequestDTO d -> workMapper.toEntity(d);
            case CordelRequestDTO d -> workMapper.toEntity(d);
            case ShortStoryRequestDTO d -> workMapper.toEntity(d);
            case TaleRequestDTO d -> workMapper.toEntity(d);
            case ArticleRequestDTO d -> workMapper.toEntity(d);
            case InfographicRequestDTO d -> workMapper.toEntity(d);
            case MultimediaRequestDTO d -> workMapper.toEntity(d);
            case LibraLiteratureRequestDTO d -> workMapper.toEntity(d);
            default -> throw new IllegalArgumentException(
                    "Tipo não mapeado: " + dto.getClass().getSimpleName());
        };
        work.setAuthor(author);
        work.setViewCount(0L);
        //todo: pode verificar por tipo tambem, ver isso dps
        if (workRepository.existsWorkByAuthorAndTitle(author, work.getTitle())) {
            throw new WorkAlreadyExistsException("Obra com mesmo título já existe para este autor");
        }
        return workMapper.toDTO(workRepository.save(work), 0L);
    }

    public <T extends WorkRequest> WorkResponse update(UUID id, T dto) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra não encontrada"));
        switch (dto) {
            case EssayRequestDTO d -> workMapper.partialUpdate(d, (Essay) work);
            case ArtRequestDTO d -> workMapper.partialUpdate(d, (Art) work);
            case CordelRequestDTO d -> workMapper.partialUpdate(d, (Cordel) work);
            case ShortStoryRequestDTO d -> workMapper.partialUpdate(d, (ShortStory) work);
            case TaleRequestDTO d -> workMapper.partialUpdate(d, (Tale) work);
            case ArticleRequestDTO d -> workMapper.partialUpdate(d, (Article) work);
            case InfographicRequestDTO d -> workMapper.partialUpdate(d, (Infographic) work);
            case MultimediaRequestDTO d -> workMapper.partialUpdate(d, (Multimedia) work);
            case LibraLiteratureRequestDTO d -> workMapper.partialUpdate(d, (LibraLiterature) work);
            default -> throw new IllegalArgumentException(
                    "Tipo não mapeado: " + dto.getClass().getSimpleName());
        }

        if (dto.author() != null && !dto.author().isBlank()) {
            User user = userRepository.findByEmail(dto.author())
                    .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com email: " + dto.author()));
            work.setAuthor(user);
        }
        return workMapper.toDTO(workRepository.save(work), workRepository.getLikeCount(id));
    }
}
