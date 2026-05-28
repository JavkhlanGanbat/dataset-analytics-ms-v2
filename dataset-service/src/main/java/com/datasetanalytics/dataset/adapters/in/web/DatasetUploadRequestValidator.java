package com.datasetanalytics.dataset.adapters.in.web;

import com.datasetanalytics.dataset.core.domain.DatasetValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class DatasetUploadRequestValidator {
    private static final Set<String> CSV_CONTENT_TYPES = Set.of(
            "text/csv",
            "application/csv",
            "application/vnd.ms-excel"
    );

    public void validate(String name, MultipartFile file) {
        if (name == null || name.isBlank()) {
            throw new DatasetValidationException("Dataset name is required.");
        }
        if (file == null) {
            throw new DatasetValidationException("CSV file is required.");
        }
        if (file.isEmpty()) {
            throw new DatasetValidationException("CSV file must not be empty.");
        }
        if (!looksLikeCsv(file)) {
            throw new DatasetValidationException("Only CSV files are supported.");
        }
    }

    private boolean looksLikeCsv(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            return true;
        }

        String contentType = file.getContentType();
        return contentType != null && CSV_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }
}
