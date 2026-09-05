package org.bibliotecaviva.backend.application.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bibliotecaviva.backend.application.dtos.response.imports.ImportUsersErrorDTO;
import org.bibliotecaviva.backend.application.dtos.response.imports.ImportUsersResponseDTO;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.InvalidImportFileException;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UsersImportService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ImportUsersResponseDTO importUsers(MultipartFile file) {
        validateFile(file);

        List<ImportUsersErrorDTO> errors = new ArrayList<>();
        List<User> usersToSave = new ArrayList<>();
        Set<String> emailsInFile = new HashSet<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new InvalidImportFileException("A planilha esta vazia");
            }

            DataFormatter formatter = new DataFormatter();
            Row header = sheet.getRow(0);
            Map<String, Integer> columns = readHeader(header, formatter);

            int totalRows = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (isEmptyRow(row, formatter)) {
                    continue;
                }

                totalRows++;

                String name = readCell(row, columns.get("name"), formatter);
                String email = readCell(row, columns.get("email"), formatter);
                String password = readCell(row, columns.get("password"), formatter);

                List<String> rowErrors = validateRow(name, email, password, emailsInFile);

                if (!rowErrors.isEmpty()) {
                    errors.add(new ImportUsersErrorDTO(rowIndex + 1, email, String.join("; ", rowErrors)));
                    continue;
                }

                emailsInFile.add(email);

                User user = User.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .role(Role.ALUNO)
                        .accountStatus(Status.ACTIVE)
                        .likedWorks(new HashSet<>())
                        .build();

                usersToSave.add(user);
            }

            userRepository.saveAll(usersToSave);

            return new ImportUsersResponseDTO(
                    totalRows,
                    usersToSave.size(),
                    errors.size(),
                    errors);
        } catch (IOException e) {
            throw new InvalidImportFileException("Nao foi possivel ler a planilha.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImportFileException("Nao foi possivel localizar a planilha.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new InvalidImportFileException("O arquivo precisa estar no formato .xlsx");
        }
    }

    private Map<String, Integer> readHeader(Row header, DataFormatter formatter) {
        if (header == null) {
            throw new InvalidImportFileException("A planilha precisa ter cabecalho.");
        }

        Map<String, Integer> columns = new HashMap<>();

        for (Cell cell : header) {
            String value = formatter.formatCellValue(cell).trim().toLowerCase(Locale.ROOT);
            columns.put(value, cell.getColumnIndex());
        }

        if (!columns.containsKey("name") || !columns.containsKey("email") || !columns.containsKey("password")) {
            throw new InvalidImportFileException("A planilha precisa ter as colunas: name, email, password");
        }

        return columns;
    }

    private List<String> validateRow(String name, String email, String password, Set<String> emailsInFile) {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank() || name.length() < 2 || name.length() > 100) {
            errors.add("Nome deve conter entre 2 e 100 caracteres");
        }

        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Email invalido");
        } else if (emailsInFile.contains(email)) {
            errors.add("Email duplicado na planilha");
        } else if (userRepository.existsByEmail(email)) { //todo: n+1
            errors.add("Email ja cadastrado");
        }

        if (password == null || password.isBlank() || password.length() < 6) {
            errors.add("Senha deve conter no minimo 6 caracteres");
        }

        return errors;
    }

    private String readCell(Row row, Integer columnIndex, DataFormatter formatter) {
        if (row == null || columnIndex == null) {
            return "";
        }

        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private boolean isEmptyRow(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }

        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
