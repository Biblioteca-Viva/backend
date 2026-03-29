package org.bibliotecaviva.backend.aplication.services;

import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.aplication.dtos.request.WorkRequestDTO;
import org.bibliotecaviva.backend.aplication.dtos.response.WorkResponseDTO;
import org.bibliotecaviva.backend.aplication.mappers.WorkMapper;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.infrastructure.repositories.WorkRepository;
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
    public List<WorkResponseDTO> getAll(){
        return workRepository.findAllSummary()
                .stream()
                .map(workMapper::toWorkDTO)
                .toList();
    }

    public WorkResponseDTO getById(UUID id){
        var work = workRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " +id+ " não encontrada"));
        return workMapper.toDTO(work);
    }

    public void delete(UUID id){
        this.getById(id);
        workRepository.deleteById(id);
    }
}
