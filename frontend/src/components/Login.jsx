import React, { useState } from 'react';
import axios from 'axios'; 
import '../App.css'; 

const Login = ({ onLoginSuccess }) => {
  const [isRegistering, setIsRegistering] = useState(false);
  const [formData, setFormData] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    try {
      if (isRegistering) {
        await axios.post('/api/auth/register', formData);
        setMessage('Registration successful! Please login.');
        setIsRegistering(false);
      } else {
        const response = await axios.post('/api/auth/login', formData);
        console.log('Login response:', response.data);
       
        if (response.data.success === true) {
          console.log('Login successful, redirecting...');
          onLoginSuccess(); 
        } else {
          setError('Invalid credentials. Check username, email, and password.');
        }
      }
    } catch (err) {
      console.error('Login error:', err);
      console.error('Error response:', err.response?.data);
      console.error('Error status:', err.response?.status);
      
      if (err.response?.status === 401) {
        setError('Invalid credentials. Check username, email, and password.');
      } else if (err.message === 'Network Error') {
        setError('❌ Cannot connect to backend. Make sure server is running on port 8081.');
      } else {
        setError(err.response?.data?.message || `Server error: ${err.message}. Please try again.`);
      }
    }
  };

  return (
    <div className="app">
      <div className="container" style={{ maxWidth: '450px', marginTop: '100px' }}>
        <header className="header">
          <h1 className="title">🎯 {isRegistering ? 'Create Account' : 'Welcome Back'}</h1>
          <p className="subtitle">Please enter your details to continue</p>
        </header>

        <form onSubmit={handleSubmit} className="upload-form">
          <div className="form-section">
            <label className="form-label">Username</label>
            <input 
              type="text" name="username" className="file-input" style={{padding: '10px'}}
              onChange={handleChange} required 
            />
          </div>

          <div className="form-section">
            <label className="form-label">Email Address</label>
            <input 
              type="email" name="email" className="file-input" style={{padding: '10px'}}
              onChange={handleChange} required 
            />
          </div>

          <div className="form-section">
            <label className="form-label">Password</label>
            <input 
              type="password" name="password" className="file-input" style={{padding: '10px'}}
              onChange={handleChange} required 
            />
          </div>

          {error && <div className="error-message">⚠️ {error}</div>}
          {message && <div style={{color: '#28a745', marginBottom: '10px'}}>✓ {message}</div>}

          <div className="button-group">
            <button type="submit" className="btn btn-primary" style={{width: '100%'}}>
              {isRegistering ? 'Register' : 'Sign In'}
            </button>
          </div>

          <p style={{ textAlign: 'center', marginTop: '20px' }}>
            {isRegistering ? 'Already have an account?' : "Don't have an account?"}
            <button 
              type="button" 
              onClick={() => setIsRegistering(!isRegistering)}
              style={{ background: 'none', border: 'none', color: '#6366f1', cursor: 'pointer', fontWeight: 'bold' }}
            >
              {isRegistering ? ' Login here' : ' Register here'}
            </button>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Login;