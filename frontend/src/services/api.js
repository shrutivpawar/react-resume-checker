import axios from 'axios';

const API_BASE_URL = '/api';

export const analyzeResume = async (file, jobDescription) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('jobDescription', jobDescription);

  const response = await axios.post(`${API_BASE_URL}/resume/analyze`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

export const healthCheck = async () => {
  const response = await axios.get(`${API_BASE_URL}/resume/health`);
  return response.data;
};
