package com.resumeai.controller;

import com.resumeai.model.Analysis;
import com.resumeai.repo.AnalysisRepository;
import com.resumeai.service.AiService;
import com.resumeai.service.PdfService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {
    private final AnalysisRepository repo;
    private final PdfService pdf;
    private final AiService ai;

    public AnalysisController(AnalysisRepository repo, PdfService pdf, AiService ai) {
        this.repo = repo; this.pdf = pdf; this.ai = ai;
    }

    private Map<String, Object> view(Analysis a, boolean full) {
        var m = new java.util.LinkedHashMap<String, Object>();
        m.put("id", a.getId());
        m.put("fileName", a.getFileName());
        m.put("jobTitle", a.getJobTitle());
        m.put("matchScore", a.getMatchScore());
        m.put("createdAt", a.getCreatedAt().toString());
        if (full) m.put("result", a.getResultJson()); // JSON string; frontend parses it
        return m;
    }

    @PostMapping(consumes = "multipart/form-data")
    public Map<String, Object> create(Authentication auth,
                                      @RequestParam("resume") MultipartFile resume,
                                      @RequestParam("jobTitle") String jobTitle,
                                      @RequestParam("jobDescription") String jobDescription) {
        if (jobTitle.isBlank() || jobDescription.trim().length() < 30)
            throw new IllegalArgumentException("Add a job title and a job description of at least 30 characters.");
        Long userId = (Long) auth.getPrincipal();
        String text = pdf.extractText(resume);
        String json = ai.analyze(text, jobTitle.trim(), jobDescription.trim());
        Analysis saved = repo.save(new Analysis(userId, resume.getOriginalFilename(),
                jobTitle.trim(), ai.scoreOf(json), json));
        return view(saved, true);
    }

    @GetMapping
    public List<Map<String, Object>> list(Authentication auth) {
        return repo.findByUserIdOrderByCreatedAtDesc((Long) auth.getPrincipal())
                .stream().map(a -> view(a, false)).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(Authentication auth, @PathVariable Long id) {
        return view(repo.findByIdAndUserId(id, (Long) auth.getPrincipal())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)), true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        Analysis a = repo.findByIdAndUserId(id, (Long) auth.getPrincipal())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repo.delete(a);
        return ResponseEntity.noContent().build();
    }
}
