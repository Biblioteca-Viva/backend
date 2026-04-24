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
import org.bibliotecaviva.backend.application.dtos.response.LikeResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;
import org.bibliotecaviva.backend.application.dtos.response.HomePageDashboardResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkSummaryResponseDTO;
import org.bibliotecaviva.backend.application.mappers.WorkMapper;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;
import org.bibliotecaviva.backend.domain.entities.audiovisual.Multimedia;
import org.bibliotecaviva.backend.domain.entities.textual.*;
import org.bibliotecaviva.backend.domain.entities.visual.Art;
import org.bibliotecaviva.backend.domain.entities.visual.Infographic;
import org.bibliotecaviva.backend.domain.enums.WorkTypes;
import org.bibliotecaviva.backend.domain.exceptions.UserNotFoundException;
import org.bibliotecaviva.backend.domain.exceptions.WorkAlreadyExistsException;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistance.repository.CommentRepository;
import org.bibliotecaviva.backend.persistance.repository.UserRepository;
import org.bibliotecaviva.backend.persistance.repository.WorkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor

public class WorkService {

    private final WorkRepository workRepository;
    private final WorkMapper workMapper;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    /**
     * Puxa direto da tabela works usando uma interface com atributos genericos
     * para evitar fazer joins desnecessários
     */
    public Page<WorkSummaryResponseDTO> getAll(WorkTypes type, Pageable pageable) {
        String types = type == null ? null : WorkTypes.fromString(type.name()).getValue();
        return workRepository.findAllSummary(types, pageable)
                .map(workMapper::toWorkSummary);
    }

    // todo: - verificar view count (vai dar gargalo ficar fazendo update assim)
    // - da pra melhorar a performace pq ta fazendo o join com todas as tabelas
    // desnecessariamente
    public WorkResponse getById(UUID id) {
        var work = workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + id + " não encontrada"));
        workRepository.incrementViewCount(id);
        return workMapper.toDTO(work, workRepository.getLikeCount(id), commentRepository.countByWork_Id(id));
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
        // todo: pode verificar por tipo tambem, ver isso dps
        if (workRepository.existsWorkByAuthorAndTitle(author, work.getTitle())) {
            throw new WorkAlreadyExistsException("Obra com mesmo título já existe para este autor");
        }
        return workMapper.toDTO(workRepository.save(work), 0L, 0L);
    }

    @Transactional
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
        // todo: verificar update, provvavelmente pode quebrar regra de author e nome de
        // obra
        return workMapper.toDTO(workRepository.save(work), workRepository.getLikeCount(id),
                commentRepository.countByWork_Id(id));
    }

    @Transactional
    public void delete(UUID id) {
        workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + id + " não encontrada"));
        workRepository.deleteById(id);
    }

    public List<UUID> getLikedWorkIds(User user) {
        return userRepository.findLikedWorkIdsByUserId(user.getId());
    }

    // TODO: fazer sistema melhor de like ou colocar um limitador de request ou
    // cachear ( mesmo problema das views)
    // pode dar gargalo fazer update toda hora assim
    @Transactional
    public LikeResponseDTO like(UUID workId, User user) {
        workRepository.findById(workId)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + workId + " não encontrada"));
        var userId = user.getId();

        userRepository.likeWork(userId, workId);

        return new LikeResponseDTO(true, workRepository.getLikeCount(workId));
    }

    @Transactional
    public LikeResponseDTO unLike(UUID workId, User user) {
        workRepository.findById(workId)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + workId + " não encontrada"));
        var userId = user.getId();

        userRepository.unlikeWork(userId, workId);

        return new LikeResponseDTO(false, workRepository.getLikeCount(workId));
    }

    public HomePageDashboardResponseDTO getFrontPageData() {
        var counts = workRepository.countPerType().stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
        var works = Arrays.stream(WorkTypes.values())
                .flatMap(type -> workRepository.findTop5ByType(type.getValue()).stream())
                .map(workMapper::toWorkSummary)
                .toList();
        var mostLikes = workRepository.getMostLikedWorks().stream().map(workMapper::toWorkSummary).toList();
        return new HomePageDashboardResponseDTO(
                counts.getOrDefault("LibraLiterature", 0L).intValue(),
                counts.getOrDefault("Multimedia", 0L).intValue(),
                counts.getOrDefault("Article", 0L).intValue(),
                counts.getOrDefault("Cordel", 0L).intValue(),
                counts.getOrDefault("Essay", 0L).intValue(),
                counts.getOrDefault("ShortStory", 0L).intValue(),
                counts.getOrDefault("Tale", 0L).intValue(),
                counts.getOrDefault("Art", 0L).intValue(),
                counts.getOrDefault("Infographic", 0L).intValue(),
                works,
                mostLikes);
    }

    public Long countWorks() {
        return workRepository.count();
    }
}
