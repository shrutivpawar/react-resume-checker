import React, { useState } from 'react';
import { analyzeResume } from './services/api';
import ScoreCard from './components/ScoreCard';
import ResultsPanel from './components/ResultsPanel';
import Login from './components/Login';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [file, setFile] = useState(null);
  const [jobDescription, setJobDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [error, setError] = useState('');

  if (!isAuthenticated) {
    return <Login onLoginSuccess={() => setIsAuthenticated(true)} />;
  }

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      const fileType = selectedFile.name.split('.').pop().toLowerCase();
      if (fileType !== 'pdf' && fileType !== 'docx') {
        setError('Please upload a PDF or DOCX file');
        setFile(null);
        return;
      }
      setFile(selectedFile);
      setError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!file) {
      setError('Please upload your resume');
      return;
    }
    
    if (!jobDescription.trim()) {
      setError('Please enter a job description');
      return;
    }

    setLoading(true);
    setError('');
    setResults(null);

    try {
      const data = await analyzeResume(file, jobDescription);
      setResults(data);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to analyze resume. Please try again.');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setJobDescription('');
    setResults(null);
    setError('');
    document.getElementById('fileInput').value = '';
  };

  return (
    <div className="app">
      <div className="container">
        <header className="header">
          <h1 className="title">🎯 ATS Resume Checker</h1>
          <p className="subtitle">
            Upload your resume and job description to get instant ATS compatibility analysis
          </p>
        </header>

        <div className="main-content">
          <form onSubmit={handleSubmit} className="upload-form">
            <div className="form-section">
              <label className="form-label">
                📄 Upload Resume (PDF or DOCX)
              </label>
              <div className="file-upload">
                <input
                  id="fileInput"
                  type="file"
                  accept=".pdf,.docx"
                  onChange={handleFileChange}
                  className="file-input"
                />
                {file && (
                  <div className="file-name">
                    ✓ {file.name}
                  </div>
                )}
              </div>
            </div>

            <div className="form-section">
              <label className="form-label">
                📋 Job Description
              </label>
              <textarea
                value={jobDescription}
                onChange={(e) => setJobDescription(e.target.value)}
                placeholder="Paste the job description here..."
                className="textarea"
                rows="8"
              />
            </div>

            {error && (
              <div className="error-message">
                ⚠️ {error}
              </div>
            )}

            <div className="button-group">
              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary"
              >
                {loading ? '⏳ Analyzing...' : '🚀 Analyze Resume'}
              </button>
              {results && (
                <button
                  type="button"
                  onClick={handleReset}
                  className="btn btn-secondary"
                >
                  🔄 Reset
                </button>
              )}
            </div>
          </form>

          {results && (
            <>
              <div className="scores-container">
                <ScoreCard
                  score={results.atsScore}
                  label="Overall ATS Score"
                  description="How well your resume matches the job"
                />
                <ScoreCard
                  score={results.keywordMatchPercentage}
                  label="Keyword Match"
                  description="Percentage of JD keywords found"
                />
                <ScoreCard
                  score={results.skillsAnalysis.skillMatchPercentage}
                  label="Skills Match"
                  description="Required skills coverage"
                />
              </div>

              <ResultsPanel results={results} />
            </>
          )}
        </div>

        <footer className="footer">
          <p>Built with React + Vite + Spring Boot</p>
        </footer>
      </div>
    </div>
  );
}

export default App;
