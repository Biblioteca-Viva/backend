package org.bibliotecaviva.backend.application.dtos.response;

import java.util.List;

public record HomePageDashboardResponseDTO(
        Integer libraLiteratureCount,
        Integer multimediaCount,
        Integer articleCount,
        Integer cordelCount,
        Integer essayCount,
        Integer shortStoryCount,
        Integer taleCount,
        Integer artCount,
        Integer infographicCount,
        List<WorkSummaryResponseDTO> works
) {
}

