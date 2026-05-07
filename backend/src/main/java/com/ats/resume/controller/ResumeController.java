package com.ats.resume.controller;

import com.ats.resume.model.ATSAnalysisResult;
import com.ats.resume.service.ResumeAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "http://localhost:5173") 
public class ResumeController {
    
    @Autowired
    private ResumeAnalysisService resumeAnalysisService;
    
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Please upload a resume file"));
            }
            
            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Please provide a job description"));
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Filename cannot be null"));
            }
            String filenameLower = filename.toLowerCase(Locale.ROOT);
            if (!filenameLower.endsWith(".pdf") && !filenameLower.endsWith(".docx") && !filenameLower.endsWith(".doc")) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Only PDF, DOC, and DOCX files are supported"));
            }
            
            // Analyze resume
            ATSAnalysisResult result = resumeAnalysisService.analyzeResume(file, jobDescription);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error analyzing resume: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "ATS Resume Checker API is running");
        return ResponseEntity.ok(response);
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
