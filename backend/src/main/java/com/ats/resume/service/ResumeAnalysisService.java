package com.ats.resume.service;

import com.ats.resume.model.ATSAnalysisResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Locale;

@Service
public class ResumeAnalysisService {
    
    private static final List<String> REQUIRED_SECTIONS = Arrays.asList(
        "experience", "education", "skills", "summary", "profile"
    );
    
    private static final List<String> COMMON_TECH_SKILLS = Arrays.asList(
        "java", "python", "javascript", "react", "angular", "spring", "nodejs",
        "sql", "mongodb", "aws", "azure", "docker", "kubernetes", "git",
        "html", "css", "typescript", "c++", "c#", "ruby", "php", "swift"
    );
    
    private static final List<String> COMMON_SOFT_SKILLS = Arrays.asList(
        "leadership", "communication", "teamwork", "problem-solving",
        "analytical", "creative", "adaptable", "collaborative"
    );
    
    public ATSAnalysisResult analyzeResume(MultipartFile file, String jobDescription) throws IOException {
        String resumeText = extractTextFromFile(file);
        String normalizedResumeText = normalizeText(resumeText);
        String normalizedJobDescription = normalizeText(jobDescription);
        
        String resumeLower = normalizedResumeText.toLowerCase(Locale.ROOT);
        String jdLower = normalizedJobDescription.toLowerCase(Locale.ROOT);
        
        Set<String> jdKeywords = extractKeywords(jdLower);
        Set<String> resumeKeywords = extractKeywords(resumeLower);
       
        Set<String> matchedKeywords = new HashSet<>(jdKeywords);
        matchedKeywords.retainAll(resumeKeywords);
        
        Set<String> missingKeywords = new HashSet<>(jdKeywords);
        missingKeywords.removeAll(resumeKeywords);
        
        int keywordMatchPercentage = jdKeywords.isEmpty() ? 0 : 
            (int) ((matchedKeywords.size() * 100.0) / jdKeywords.size());
        
        ATSAnalysisResult.FormatAnalysis formatAnalysis = analyzeFormat(resumeText);
        
        ATSAnalysisResult.SkillsAnalysis skillsAnalysis = analyzeSkills(resumeLower, jdLower);
        
        int atsScore = calculateATSScore(
            keywordMatchPercentage,
            formatAnalysis,
            skillsAnalysis
        );
        
        List<String> suggestions = generateSuggestions(
            keywordMatchPercentage,
            formatAnalysis,
            skillsAnalysis,
            missingKeywords
        );
        
        return new ATSAnalysisResult(
            atsScore,
            keywordMatchPercentage,
            new ArrayList<>(matchedKeywords),
            new ArrayList<>(missingKeywords),
            suggestions,
            formatAnalysis,
            skillsAnalysis
        );
    }
    
    private String extractTextFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        
        String filenameLower = filename.toLowerCase(Locale.ROOT);
        if (filenameLower.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else if (filenameLower.endsWith(".docx")) {
            return extractTextFromDocx(file);
        } else if (filenameLower.endsWith(".doc")) {
            return extractTextFromDoc(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please upload PDF, DOC or DOCX.");
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = createPdfTextStripper();
            String text = Optional.ofNullable(stripper.getText(document)).orElse("");

            if (text.isBlank()) {
                text = extractTextFromPdfByPage(document);
            }

            return cleanupExtractedText(text);
        }
    }

    private PDFTextStripper createPdfTextStripper() throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setWordSeparator(" ");
        stripper.setLineSeparator(" ");
        stripper.setParagraphEnd(" ");
        stripper.setPageStart(" ");
        stripper.setPageEnd(" ");
        return stripper;
    }

    private String extractTextFromPdfByPage(PDDocument document) throws IOException {
        PDFTextStripper stripper = createPdfTextStripper();
        StringBuilder buffer = new StringBuilder();
        int pageCount = document.getNumberOfPages();

        for (int page = 1; page <= pageCount; page++) {
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            buffer.append(Optional.ofNullable(stripper.getText(document)).orElse(""));
            buffer.append(" ");
        }

        return cleanupExtractedText(buffer.toString());
    }

    private String cleanupExtractedText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\u00A0\\u200B\\u200C\\u200D]+", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    private String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream());
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return cleanupExtractedText(Optional.ofNullable(extractor.getText()).orElse(""));
        }
    }

    private String extractTextFromDoc(MultipartFile file) throws IOException {
        try (HWPFDocument document = new HWPFDocument(file.getInputStream());
             WordExtractor extractor = new WordExtractor(document)) {
            return cleanupExtractedText(Optional.ofNullable(extractor.getText()).orElse(""));
        }
    }

    
    private Set<String> extractKeywords(String text) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "been",
            "be", "have", "has", "had", "do", "does", "did", "will", "would",
            "should", "could", "may", "might", "can", "about", "into", "through",
            "during", "before", "after", "above", "below", "up", "down", "out",
            "over", "under", "again", "further", "then", "once", "here", "there",
            "when", "where", "why", "how", "all", "both", "each", "few", "more",
            "most", "other", "some", "such", "only", "own", "same", "so", "than",
            "too", "very", "this", "that", "these", "those"
        ));
        
        Pattern pattern = Pattern.compile("\\b[0-9a-z+#]{2,}\\b");
        Matcher matcher = pattern.matcher(text);
        
        Set<String> keywords = new HashSet<>();
        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);
            if (!stopWords.contains(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    private boolean containsWord(String text, String word) {
        if (text == null || word == null || word.isBlank()) {
            return false;
        }
        Pattern pattern = Pattern.compile("(?<!\\w)" + Pattern.quote(word.toLowerCase(Locale.ROOT)) + "(?!\\w)");
        return pattern.matcher(text).find();
    }
    
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replaceAll("[\\r\\n]+", " ")
            .replaceAll("[^a-zA-Z0-9+#]+", " ")
            .trim();
    }
    
    private ATSAnalysisResult.FormatAnalysis analyzeFormat(String resumeText) {
        List<String> issues = new ArrayList<>();
        
        boolean hasBulletPoints = resumeText.contains("•") || 
                                  resumeText.contains("·") || 
                                  resumeText.matches(".*[\\n\\r]\\s*[-*]\\s+.*");
        
        if (!hasBulletPoints) {
            issues.add("No bullet points detected. Use bullet points to improve readability.");
        }
        
        String resumeLower = resumeText.toLowerCase();
        boolean hasProperSections = REQUIRED_SECTIONS.stream()
            .anyMatch(resumeLower::contains);
        
        if (!hasProperSections) {
            issues.add("Missing standard sections (Experience, Education, Skills, etc.)");
        }
        
        boolean hasEmail = resumeText.matches(".*\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b.*");
        boolean hasPhone = resumeText.matches(".*\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b.*");
        boolean hasContactInfo = hasEmail || hasPhone;
        
        if (!hasContactInfo) {
            issues.add("Contact information (email/phone) not clearly visible.");
        }
        
        int wordCount = resumeText.split("\\s+").length;
        
        if (wordCount < 200) {
            issues.add("Resume seems too short. Aim for 300-800 words.");
        } else if (wordCount > 1000) {
            issues.add("Resume might be too long. Keep it concise (300-800 words).");
        }
        
        return new ATSAnalysisResult.FormatAnalysis(
            hasBulletPoints,
            hasProperSections,
            hasContactInfo,
            wordCount,
            issues
        );
    }
    
    private ATSAnalysisResult.SkillsAnalysis analyzeSkills(String resumeLower, String jdLower) {
        // Find technical skills using exact word matching
        List<String> foundTechSkills = COMMON_TECH_SKILLS.stream()
            .filter(skill -> containsWord(resumeLower, skill))
            .collect(Collectors.toList());
        
        List<String> foundSoftSkills = COMMON_SOFT_SKILLS.stream()
            .filter(skill -> containsWord(resumeLower, skill))
            .collect(Collectors.toList());
        
        List<String> jdRequiredSkills = COMMON_TECH_SKILLS.stream()
            .filter(skill -> containsWord(jdLower, skill))
            .collect(Collectors.toList());
        
        List<String> missingCriticalSkills = jdRequiredSkills.stream()
            .filter(skill -> !containsWord(resumeLower, skill))
            .collect(Collectors.toList());
        
        int skillMatchPercentage = jdRequiredSkills.isEmpty() ? 100 :
            (int) (((jdRequiredSkills.size() - missingCriticalSkills.size()) * 100.0) / jdRequiredSkills.size());
        
        return new ATSAnalysisResult.SkillsAnalysis(
            foundTechSkills,
            foundSoftSkills,
            missingCriticalSkills,
            skillMatchPercentage
        );
    }
    
    private int calculateATSScore(
        int keywordMatchPercentage,
        ATSAnalysisResult.FormatAnalysis formatAnalysis,
        ATSAnalysisResult.SkillsAnalysis skillsAnalysis
    ) {
        int score = 0;
        
        score += (keywordMatchPercentage * 0.4);
        
        score += (skillsAnalysis.getSkillMatchPercentage() * 0.3);
        
        int formatScore = 0;
        if (formatAnalysis.isHasBulletPoints()) formatScore += 33;
        if (formatAnalysis.isHasProperSections()) formatScore += 34;
        if (formatAnalysis.isHasContactInfo()) formatScore += 33;
        score += (formatScore * 0.3);
        
        return Math.min(100, Math.max(0, score));
    }
    
    private List<String> generateSuggestions(
        int keywordMatchPercentage,
        ATSAnalysisResult.FormatAnalysis formatAnalysis,
        ATSAnalysisResult.SkillsAnalysis skillsAnalysis,
        Set<String> missingKeywords
    ) {
        List<String> suggestions = new ArrayList<>();
        
        if (keywordMatchPercentage < 50) {
            suggestions.add("Low keyword match! Add more relevant keywords from the job description.");
        }
        
        if (!skillsAnalysis.getMissingCriticalSkills().isEmpty()) {
            suggestions.add("Missing key skills: " + 
                String.join(", ", skillsAnalysis.getMissingCriticalSkills().subList(0, 
                    Math.min(5, skillsAnalysis.getMissingCriticalSkills().size()))));
        }
        
        if (!formatAnalysis.isHasBulletPoints()) {
            suggestions.add("Use bullet points to list your achievements and responsibilities.");
        }
        
        if (!formatAnalysis.isHasProperSections()) {
            suggestions.add("Add clear sections: Summary, Experience, Education, Skills.");
        }
        
        if (!formatAnalysis.isHasContactInfo()) {
            suggestions.add("Ensure your contact information (email, phone) is clearly visible.");
        }
        
        if (formatAnalysis.getWordCount() < 200) {
            suggestions.add("Expand your resume with more details about your experience and achievements.");
        } else if (formatAnalysis.getWordCount() > 1000) {
            suggestions.add("Shorten your resume. Focus on the most relevant experience.");
        }
        
        if (!missingKeywords.isEmpty() && missingKeywords.size() > 5) {
            List<String> topMissing = missingKeywords.stream()
                .limit(5)
                .collect(Collectors.toList());
            suggestions.add("Consider adding these keywords: " + String.join(", ", topMissing));
        }
        
        if (skillsAnalysis.getTechnicalSkills().isEmpty()) {
            suggestions.add("Add a dedicated Skills section with relevant technical skills.");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Great job! Your resume is well-optimized for ATS.");
        }
        
        return suggestions;
    }
}
