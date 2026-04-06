package org.bibliotecaviva.backend.application.dtos.request;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/*
 * usado so pra sinaliazr que os outros sao subtipos
 */
@Schema(hidden = true)
public interface WorkRequest {
    String title();

    String author(); // ver comoo vai mandar isso no front tbm, por enqunato ta mandando email

    LocalDateTime publicationDate();

    String description();
    //view count
    //likes
}
