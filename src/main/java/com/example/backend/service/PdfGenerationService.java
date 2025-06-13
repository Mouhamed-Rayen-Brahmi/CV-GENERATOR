package com.example.backend.service;

import com.example.backend.model.CV;

public interface PdfGenerationService {
    byte[] generatePdfFromCV(CV cv);
    byte[] generatePdfFromTemplate(String templateName, Object data);
}