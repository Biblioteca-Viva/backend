package org.bibliotecaviva.backend.application.services;

import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.*;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.LibraLiteratureRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.MultimediaRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.ArticleRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.CordelRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.EssayRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.ShortStoryRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.TaleRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.ArtRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.InfographicRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponseDTO;
import org.bibliotecaviva.backend.application.mappers.WorkMapper;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;
import org.bibliotecaviva.backend.domain.entities.audiovisual.Multimedia;
import org.bibliotecaviva.backend.domain.entities.textual.*;
import org.bibliotecaviva.backend.domain.entities.visual.Art;
import org.bibliotecaviva.backend.domain.entities.visual.Infographic;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistance.repository.WorkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class WorkService {

    private final WorkRepository workRepository;
    private final WorkMapper workMapper;

    /*
     * Puxa todos da tabela works usando uma interface com atributos específicos
     * para nao requisitar tudo do banco
     */
    public List<WorkResponseDTO> getAll(String type) {
        return workRepository.findAllSummary(type)
                .stream()
                .map(workMapper::toWorkDTO)
                .toList();
    }

    public WorkResponseDTO getById(UUID id) {
        var work = workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + id + " não encontrada"));
        return workMapper.toDTO(work);
    }

    public void delete(UUID id) {
        workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra não encontrada"));   
        workRepository.deleteById(id);
    }

    public <T extends WorkRequest> WorkResponseDTO create(T dto) {
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
        return workMapper.toDTO(workRepository.save(work));
    }

    public <T extends WorkRequest> WorkResponseDTO update(UUID id, T dto) {
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
        return workMapper.toDTO(workRepository.save(work));
    }
}
