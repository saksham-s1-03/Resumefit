package com.resumeai.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfService {
    public String extractText(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".pdf")) throw new IllegalArgumentException("Please upload a PDF file.");
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(doc).trim();
            if (text.length() < 50)
                throw new IllegalArgumentException("Could not read text from this PDF. Is it a scanned image?");
            return text.length() > 15000 ? text.substring(0, 15000) : text;
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("This PDF could not be opened.");
        }
    }
}
