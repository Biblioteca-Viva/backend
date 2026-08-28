package org.bibliotecaviva.backend.application.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    void uploadImageShouldReturnSecureUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", "fake-image-data".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test/image.jpg"));

        String result = cloudinaryService.uploadImage(file);

        assertEquals("https://res.cloudinary.com/test/image.jpg", result);
        verify(uploader).upload(any(byte[].class), any(Map.class));
    }

    @Test
    void uploadImageShouldFallbackToUrlWhenSecureUrlIsNull() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.png", "image/png", "fake-image-data".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("url", "http://res.cloudinary.com/test/image.png"));

        String result = cloudinaryService.uploadImage(file);

        assertEquals("http://res.cloudinary.com/test/image.png", result);
    }

    @Test
    void uploadImageShouldThrowWhenFileIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cloudinaryService.uploadImage(null));

        assertEquals("Arquivo de imagem não pode ser vazio", exception.getMessage());
    }

    @Test
    void uploadImageShouldThrowWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", new byte[0]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cloudinaryService.uploadImage(file));

        assertEquals("Arquivo de imagem não pode ser vazio", exception.getMessage());
    }

    @Test
    void uploadImageShouldThrowWhenFormatIsInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "document.pdf", "application/pdf", "fake-pdf-data".getBytes());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cloudinaryService.uploadImage(file));

        assertEquals("Formato de imagem inválido. Apenas JPG e PNG são permitidos", exception.getMessage());
    }

    @Test
    void uploadImageShouldAcceptJpegExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.jpeg", "image/jpeg", "fake-image-data".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test/image.jpeg"));

        String result = cloudinaryService.uploadImage(file);

        assertNotNull(result);
    }

    @Test
    void uploadImageShouldThrowRuntimeExceptionOnIOError() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", "fake-image-data".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenThrow(new IOException("Connection refused"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cloudinaryService.uploadImage(file));

        assertEquals("Falha ao realizar upload da imagem", exception.getMessage());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void uploadImageShouldAcceptValidExtensionEvenWithNullContentType() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.png", null, "fake-image-data".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test/image.png"));

        String result = cloudinaryService.uploadImage(file);

        assertNotNull(result);
    }
}
