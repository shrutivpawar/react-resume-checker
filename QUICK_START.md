# 🚀 Quick Start Guide

## For Linux/Mac Users

### Option 1: Automated Setup
```bash
chmod +x setup.sh
./setup.sh
```

### Option 2: Manual Setup

**Terminal 1 - Backend:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm install
npm run dev
```

**Open Browser:**
```
http://localhost:5173
```

---

## For Windows Users

### Option 1: Automated Setup
Double-click `setup.bat` or run:
```cmd
setup.bat
```

### Option 2: Manual Setup

**Command Prompt 1 - Backend:**
```cmd
cd backend
mvn clean install
mvn spring-boot:run
```

**Command Prompt 2 - Frontend:**
```cmd
cd frontend
npm install
npm run dev
```

**Open Browser:**
```
http://localhost:5173
```

---

## ✅ Verification

1. Backend should be running on: `http://localhost:8080`
2. Frontend should be running on: `http://localhost:5173`
3. Test backend health: `http://localhost:8080/api/resume/health`

---

## 📝 Usage Steps

1. **Upload Resume**: Click to upload your PDF or DOCX resume
2. **Paste Job Description**: Copy and paste the job description
3. **Analyze**: Click "Analyze Resume" button
4. **View Results**: See your ATS score and recommendations

---

## 🔧 Troubleshooting

### Backend won't start
- Ensure Java 17+ is installed: `java -version`
- Check port 8080 is free: Change in `application.properties`
- Run: `mvn clean install` again

### Frontend won't start
- Ensure Node.js 16+ is installed: `node -v`
- Delete `node_modules` and run `npm install` again
- Check port 5173 is free

### Can't upload files
- Ensure file is PDF or DOCX format
- File size should be less than 10MB
- Check backend is running

---

## 🎯 Test the Application

Use these sample inputs to test:

**Sample Job Description:**
```
We are looking for a Senior Java Developer with 5+ years of experience.
Required skills: Java, Spring Boot, React, SQL, AWS, Docker, Kubernetes.
Experience with microservices architecture and RESTful APIs is essential.
Strong communication and leadership skills required.
```

**Create a sample resume** with these keywords to see how matching works!

---

## 📊 Understanding Your Score

- **80-100**: Excellent match! Your resume is well-optimized.
- **60-79**: Good match. Minor improvements needed.
- **40-59**: Fair match. Address missing keywords and skills.
- **0-39**: Needs improvement. Review suggestions carefully.

---

## 💡 Tips for Best Results

1. Use exact keywords from the job description
2. Include both technical and soft skills
3. Use bullet points for achievements
4. Keep resume between 300-800 words
5. Include contact information clearly
6. Add standard sections: Summary, Experience, Education, Skills

---

**Need help? Check the full README.md for detailed documentation!**
