package com.ats.resume.model;

import java.util.List;

public class ATSAnalysisResult {
    private int atsScore;
    private int keywordMatchPercentage;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private List<String> suggestions;
    private FormatAnalysis formatAnalysis;
    private SkillsAnalysis skillsAnalysis;

    public ATSAnalysisResult() {
    }

    public ATSAnalysisResult(int atsScore, int keywordMatchPercentage, List<String> matchedKeywords,
                             List<String> missingKeywords, List<String> suggestions,
                             FormatAnalysis formatAnalysis, SkillsAnalysis skillsAnalysis) {
        this.atsScore = atsScore;
        this.keywordMatchPercentage = keywordMatchPercentage;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
        this.suggestions = suggestions;
        this.formatAnalysis = formatAnalysis;
        this.skillsAnalysis = skillsAnalysis;
    }

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public int getKeywordMatchPercentage() {
        return keywordMatchPercentage;
    }

    public void setKeywordMatchPercentage(int keywordMatchPercentage) {
        this.keywordMatchPercentage = keywordMatchPercentage;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public FormatAnalysis getFormatAnalysis() {
        return formatAnalysis;
    }

    public void setFormatAnalysis(FormatAnalysis formatAnalysis) {
        this.formatAnalysis = formatAnalysis;
    }

    public SkillsAnalysis getSkillsAnalysis() {
        return skillsAnalysis;
    }

    public void setSkillsAnalysis(SkillsAnalysis skillsAnalysis) {
        this.skillsAnalysis = skillsAnalysis;
    }

    public static class FormatAnalysis {
        private boolean hasBulletPoints;
        private boolean hasProperSections;
        private boolean hasContactInfo;
        private int wordCount;
        private List<String> issues;

        public FormatAnalysis() {
        }

        public FormatAnalysis(boolean hasBulletPoints, boolean hasProperSections,
                            boolean hasContactInfo, int wordCount, List<String> issues) {
            this.hasBulletPoints = hasBulletPoints;
            this.hasProperSections = hasProperSections;
            this.hasContactInfo = hasContactInfo;
            this.wordCount = wordCount;
            this.issues = issues;
        }

        public boolean isHasBulletPoints() {
            return hasBulletPoints;
        }

        public void setHasBulletPoints(boolean hasBulletPoints) {
            this.hasBulletPoints = hasBulletPoints;
        }

        public boolean isHasProperSections() {
            return hasProperSections;
        }

        public void setHasProperSections(boolean hasProperSections) {
            this.hasProperSections = hasProperSections;
        }

        public boolean isHasContactInfo() {
            return hasContactInfo;
        }

        public void setHasContactInfo(boolean hasContactInfo) {
            this.hasContactInfo = hasContactInfo;
        }

        public int getWordCount() {
            return wordCount;
        }

        public void setWordCount(int wordCount) {
            this.wordCount = wordCount;
        }

        public List<String> getIssues() {
            return issues;
        }

        public void setIssues(List<String> issues) {
            this.issues = issues;
        }
    }

    public static class SkillsAnalysis {
        private List<String> technicalSkills;
        private List<String> softSkills;
        private List<String> missingCriticalSkills;
        private int skillMatchPercentage;

        public SkillsAnalysis() {
        }

        public SkillsAnalysis(List<String> technicalSkills, List<String> softSkills,
                            List<String> missingCriticalSkills, int skillMatchPercentage) {
            this.technicalSkills = technicalSkills;
            this.softSkills = softSkills;
            this.missingCriticalSkills = missingCriticalSkills;
            this.skillMatchPercentage = skillMatchPercentage;
        }

        public List<String> getTechnicalSkills() {
            return technicalSkills;
        }

        public void setTechnicalSkills(List<String> technicalSkills) {
            this.technicalSkills = technicalSkills;
        }

        public List<String> getSoftSkills() {
            return softSkills;
        }

        public void setSoftSkills(List<String> softSkills) {
            this.softSkills = softSkills;
        }

        public List<String> getMissingCriticalSkills() {
            return missingCriticalSkills;
        }

        public void setMissingCriticalSkills(List<String> missingCriticalSkills) {
            this.missingCriticalSkills = missingCriticalSkills;
        }

        public int getSkillMatchPercentage() {
            return skillMatchPercentage;
        }

        public void setSkillMatchPercentage(int skillMatchPercentage) {
            this.skillMatchPercentage = skillMatchPercentage;
        }
    }
}
