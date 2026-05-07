import React from 'react';

const ScoreCard = ({ score, label, description }) => {
  const getScoreColor = (score) => {
    if (score >= 80) return '#10b981';
    if (score >= 60) return '#f59e0b';
    return '#ef4444';
  };

  const getScoreLabel = (score) => {
    if (score >= 80) return 'Excellent';
    if (score >= 60) return 'Good';
    if (score >= 40) return 'Fair';
    return 'Needs Improvement';
  };

  return (
    <div style={styles.card}>
      <div style={styles.scoreCircle}>
        <svg width="120" height="120" viewBox="0 0 120 120">
          <circle
            cx="60"
            cy="60"
            r="50"
            fill="none"
            stroke="#e5e7eb"
            strokeWidth="10"
          />
          <circle
            cx="60"
            cy="60"
            r="50"
            fill="none"
            stroke={getScoreColor(score)}
            strokeWidth="10"
            strokeDasharray={`${(score / 100) * 314} 314`}
            strokeLinecap="round"
            transform="rotate(-90 60 60)"
          />
        </svg>
        <div style={styles.scoreText}>
          <div style={styles.scoreNumber}>{score}</div>
          <div style={styles.scoreLabel}>{getScoreLabel(score)}</div>
        </div>
      </div>
      <h3 style={styles.label}>{label}</h3>
      <p style={styles.description}>{description}</p>
    </div>
  );
};

const styles = {
  card: {
    backgroundColor: 'white',
    borderRadius: '12px',
    padding: '24px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    textAlign: 'center',
    minWidth: '200px',
  },
  scoreCircle: {
    position: 'relative',
    display: 'inline-block',
    marginBottom: '16px',
  },
  scoreText: {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    textAlign: 'center',
  },
  scoreNumber: {
    fontSize: '32px',
    fontWeight: 'bold',
    color: '#1f2937',
  },
  scoreLabel: {
    fontSize: '12px',
    color: '#6b7280',
    marginTop: '4px',
  },
  label: {
    fontSize: '18px',
    fontWeight: '600',
    color: '#1f2937',
    margin: '0 0 8px 0',
  },
  description: {
    fontSize: '14px',
    color: '#6b7280',
    margin: '0',
  },
};

export default ScoreCard;
