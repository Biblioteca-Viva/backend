package org.bibliotecaviva.backend.application.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png"
    );

    /**
     * Faz upload de uma imagem para o Cloudinary e retorna a URL segura.
     *
     * @param file arquivo de imagem (JPG ou PNG)
     * @return URL pública da imagem no Cloudinary
     * @throws IllegalArgumentException se o arquivo for vazio ou tiver formato inválido
     * @throws RuntimeException se ocorrer erro no upload
     */
    public String uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image"
            ));
            String secureUrl = (String) result.get("secure_url");
            if (secureUrl == null) {
                secureUrl = (String) result.get("url");
            }
            return secureUrl;
        } catch (IOException e) {
            log.error("Erro ao realizar upload para o Cloudinary", e);
            throw new RuntimeException("Falha ao realizar upload da imagem", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem não pode ser vazio");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean validMime = contentType != null
                && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
        boolean validExtension = filename != null
                && (filename.toLowerCase().endsWith(".jpg")
                || filename.toLowerCase().endsWith(".jpeg")
                || filename.toLowerCase().endsWith(".png"));

        if (!validMime && !validExtension) {
            throw new IllegalArgumentException(
                    "Formato de imagem inválido. Apenas JPG/JPEG e PNG são permitidos");
        }
    }
}
