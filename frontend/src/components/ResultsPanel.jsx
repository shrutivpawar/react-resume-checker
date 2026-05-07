import React from 'react';

const ResultsPanel = ({ results }) => {
  if (!results) return null;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Analysis Results</h2>

      {/* Keywords Section */}
      <div style={styles.section}>
        <h3 style={styles.sectionTitle}>📝 Keyword Analysis</h3>
        <div style={styles.grid}>
          <div style={styles.keywordBox}>
            <h4 style={styles.boxTitle}>Matched Keywords ({results.matchedKeywords.length})</h4>
            <div style={styles.tagContainer}>
              {results.matchedKeywords.slice(0, 10).map((keyword, index) => (
                <span key={index} style={{ ...styles.tag, ...styles.tagGreen }}>
                  {keyword}
                </span>
              ))}
              {results.matchedKeywords.length > 10 && (
                <span style={styles.tagMore}>+{results.matchedKeywords.length - 10} more</span>
              )}
            </div>
          </div>
          <div style={styles.keywordBox}>
            <h4 style={styles.boxTitle}>Missing Keywords ({results.missingKeywords.length})</h4>
            <div style={styles.tagContainer}>
              {results.missingKeywords.slice(0, 10).map((keyword, index) => (
                <span key={index} style={{ ...styles.tag, ...styles.tagRed }}>
                  {keyword}
                </span>
              ))}
              {results.missingKeywords.length > 10 && (
                <span style={styles.tagMore}>+{results.missingKeywords.length - 10} more</span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Skills Section */}
      <div style={styles.section}>
        <h3 style={styles.sectionTitle}>💼 Skills Analysis</h3>
        <div style={styles.grid}>
          <div style={styles.keywordBox}>
            <h4 style={styles.boxTitle}>Technical Skills</h4>
            <div style={styles.tagContainer}>
              {results.skillsAnalysis.technicalSkills.map((skill, index) => (
                <span key={index} style={{ ...styles.tag, ...styles.tagBlue }}>
                  {skill}
                </span>
              ))}
              {results.skillsAnalysis.technicalSkills.length === 0 && (
                <span style={styles.noData}>No technical skills detected</span>
              )}
            </div>
          </div>
          <div style={styles.keywordBox}>
            <h4 style={styles.boxTitle}>Missing Critical Skills</h4>
            <div style={styles.tagContainer}>
              {results.skillsAnalysis.missingCriticalSkills.map((skill, index) => (
                <span key={index} style={{ ...styles.tag, ...styles.tagOrange }}>
                  {skill}
                </span>
              ))}
              {results.skillsAnalysis.missingCriticalSkills.length === 0 && (
                <span style={styles.noData}>All required skills present! ✓</span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Format Issues */}
      <div style={styles.section}>
        <h3 style={styles.sectionTitle}>📋 Format Analysis</h3>
        <div style={styles.formatGrid}>
          <div style={styles.formatItem}>
            <span style={results.formatAnalysis.hasBulletPoints ? styles.checkYes : styles.checkNo}>
              {results.formatAnalysis.hasBulletPoints ? '✓' : '✗'}
            </span>
            <span>Bullet Points</span>
          </div>
          <div style={styles.formatItem}>
            <span style={results.formatAnalysis.hasProperSections ? styles.checkYes : styles.checkNo}>
              {results.formatAnalysis.hasProperSections ? '✓' : '✗'}
            </span>
            <span>Proper Sections</span>
          </div>
          <div style={styles.formatItem}>
            <span style={results.formatAnalysis.hasContactInfo ? styles.checkYes : styles.checkNo}>
              {results.formatAnalysis.hasContactInfo ? '✓' : '✗'}
            </span>
            <span>Contact Info</span>
          </div>
          <div style={styles.formatItem}>
            <span style={styles.wordCount}>📄</span>
            <span>{results.formatAnalysis.wordCount} words</span>
          </div>
        </div>
        {results.formatAnalysis.issues.length > 0 && (
          <div style={styles.issuesList}>
            <h4 style={styles.issuesTitle}>Issues Found:</h4>
            {results.formatAnalysis.issues.map((issue, index) => (
              <div key={index} style={styles.issueItem}>⚠️ {issue}</div>
            ))}
          </div>
        )}
      </div>

      {/* Suggestions */}
      <div style={styles.section}>
        <h3 style={styles.sectionTitle}>💡 Suggestions for Improvement</h3>
        <div style={styles.suggestionsList}>
          {results.suggestions.map((suggestion, index) => (
            <div key={index} style={styles.suggestionItem}>
              <span style={styles.suggestionNumber}>{index + 1}</span>
              <span>{suggestion}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: {
    backgroundColor: 'white',
    borderRadius: '12px',
    padding: '32px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    marginTop: '24px',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: '24px',
    borderBottom: '2px solid #e5e7eb',
    paddingBottom: '12px',
  },
  section: {
    marginBottom: '32px',
  },
  sectionTitle: {
    fontSize: '20px',
    fontWeight: '600',
    color: '#374151',
    marginBottom: '16px',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
    gap: '16px',
  },
  keywordBox: {
    backgroundColor: '#f9fafb',
    borderRadius: '8px',
    padding: '16px',
  },
  boxTitle: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#1f2937',
    marginBottom: '12px',
  },
  tagContainer: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '8px',
  },
  tag: {
    padding: '6px 12px',
    borderRadius: '6px',
    fontSize: '13px',
    fontWeight: '500',
  },
  tagGreen: {
    backgroundColor: '#d1fae5',
    color: '#065f46',
  },
  tagRed: {
    backgroundColor: '#fee2e2',
    color: '#991b1b',
  },
  tagBlue: {
    backgroundColor: '#dbeafe',
    color: '#1e40af',
  },
  tagOrange: {
    backgroundColor: '#fed7aa',
    color: '#9a3412',
  },
  tagMore: {
    padding: '6px 12px',
    borderRadius: '6px',
    fontSize: '13px',
    color: '#6b7280',
    fontStyle: 'italic',
  },
  noData: {
    color: '#6b7280',
    fontStyle: 'italic',
    fontSize: '14px',
  },
  formatGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '16px',
    marginBottom: '16px',
  },
  formatItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    backgroundColor: '#f9fafb',
    padding: '12px 16px',
    borderRadius: '8px',
    fontSize: '15px',
  },
  checkYes: {
    color: '#10b981',
    fontSize: '20px',
    fontWeight: 'bold',
  },
  checkNo: {
    color: '#ef4444',
    fontSize: '20px',
    fontWeight: 'bold',
  },
  wordCount: {
    fontSize: '20px',
  },
  issuesList: {
    backgroundColor: '#fef3c7',
    borderRadius: '8px',
    padding: '16px',
    marginTop: '16px',
  },
  issuesTitle: {
    fontSize: '15px',
    fontWeight: '600',
    color: '#92400e',
    marginBottom: '8px',
  },
  issueItem: {
    fontSize: '14px',
    color: '#78350f',
    marginBottom: '6px',
  },
  suggestionsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  suggestionItem: {
    display: 'flex',
    gap: '12px',
    padding: '12px 16px',
    backgroundColor: '#eff6ff',
    borderRadius: '8px',
    fontSize: '15px',
    color: '#1e40af',
    borderLeft: '4px solid #3b82f6',
  },
  suggestionNumber: {
    fontWeight: 'bold',
    minWidth: '24px',
  },
};

export default ResultsPanel;
