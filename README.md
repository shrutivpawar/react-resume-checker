# 🎯 ATS Resume Checker

A full-stack application to analyze resumes for Applicant Tracking System (ATS) compatibility. Built with **React (Vite)** frontend and **Spring Boot 3.5.10** backend with **PostgreSQL** database.

## 📋 Features

- **User Authentication**: Secure login/registration system with PostgreSQL
- **Resume Upload**: Support for PDF and DOCX formats  
- **ATS Score Calculation**: Overall compatibility score (0-100)
- **Keyword Analysis**: Match keywords between resume and job description
- **Skills Detection**: Identify technical and soft skills
- **Format Analysis**: Check for proper resume structure and word count
- **Missing Skills**: Highlight critical skills missing from resume
- **Actionable Suggestions**: Get specific recommendations for improvement

## 🏗️ Architecture

### Frontend (React + Vite)
- **React 18** with Hooks for state management
- **Vite** for fast development and optimized building
- **Axios** for HTTP API communication
- **Responsive CSS** with custom styling
- **Vite Proxy** for backend API routing

### Backend (Spring Boot 3.5.10)
- **Spring Boot 3.5.10** with Spring Web MVC
- **Spring Data JPA** for database persistence
- **PostgreSQL** for user data storage
- **Apache PDFBox 2.0.29** for PDF parsing
- **Apache POI 5.2.3** for DOCX parsing
- **CORS Configuration** for secure cross-origin requests

---

## 📁 Project Structure

```
ats-resume-checker/
├── backend/
│   ├── src/main/java/com/ats/resume/
│   │   ├── ResumeCheckerApplication.java          # Spring Boot entry point
│   │   ├── controller/
│   │   │   ├── AuthController.java                # User login/registration
│   │   │   └── ResumeController.java              # Resume analysis endpoints
│   │   ├── service/
│   │   │   └── ResumeAnalysisService.java         # Core ATS analysis logic
│   │   ├── model/
│   │   │   ├── User.java                          # User entity for DB
│   │   │   └── ATSAnalysisResult.java             # Response DTO with analysis results
│   │   ├── repository/
│   │   │   └── UserRepository.java                # Database queries
│   │   └── config/
│   │       └── CorsConfig.java                    # CORS settings
│   ├── src/main/resources/
│   │   └── application.properties                 # Database & server config
│   └── pom.xml                                     # Maven dependencies
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Login.jsx                          # Login/Register form
│   │   │   ├── ScoreCard.jsx                      # Displays score circles
│   │   │   └── ResultsPanel.jsx                   # Shows detailed results
│   │   ├── services/
│   │   │   └── api.js                             # API service layer
│   │   ├── App.jsx                                # Main app component
│   │   ├── App.css                                # Global styles
│   │   └── main.jsx                               # React entry point
│   ├── index.html                                  # HTML template
│   ├── vite.config.js                             # Vite config with proxy
│   └── package.json                               # Dependencies & scripts
│
└── README.md                                       # This file
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (verify: `java -version`)
- **Maven 3.6+** (verify: `mvn -version`)
- **Node.js 16+** and npm (verify: `node -v`, `npm -v`)
- **PostgreSQL 12+** (verify: `psql -version`)

### Database Setup

1. Create PostgreSQL database:
```sql
psql -U postgres
CREATE DATABASE ats_db;
```

2. Verify connection:
```bash
psql -U postgres -d ats_db -c "SELECT 1;"
```

### Backend Setup

1. Navigate to backend:
```bash
cd backend
```

2. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

✅ Backend runs on **http://localhost:8081**

### Frontend Setup

1. Navigate to frontend:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```bash
npm run dev
```

✅ Frontend runs on **http://localhost:5173** (with API proxy to backend)

---

## 📖 Usage

1. Open browser: http://localhost:5173
2. **Login/Register** with your credentials
3. Upload resume (PDF or DOCX)
4. Paste job description
5. Click "Analyze Resume"
6. View ATS score, keyword matches, and suggestions

---

## 🔧 Backend Components

### 1. **AuthController** (`/api/auth`)
Handles user authentication (login/registration)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/register` | POST | Register new user |
| `/login` | POST | Login user |

**Request Body:**
```json
{
  "username": "shruti",
  "email": "shruti@gmail.com",
  "password": "123"
}
```

---

### 2. **ResumeController** (`/api/resume`)
Handles resume analysis requests

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/analyze` | POST | Analyze resume (form-data) |
| `/health` | GET | Health check |

**Form Data:**
- `file`: Resume file (PDF/DOCX)
- `jobDescription`: Job description text

---

### 3. **ResumeAnalysisService** (Core Logic)
The brain of the application - performs all ATS analysis

**Key Methods:**

| Method | Purpose |
|--------|---------|
| `analyzeResume()` | Main analysis orchestrator |
| `extractTextFromFile()` | Parses PDF/DOCX using PDFBox/POI |
| `extractKeywords()` | Extracts and filters keywords |
| `analyzeFormat()` | Checks bullet points, sections, contact info, word count |
| `analyzeSkills()` | Identifies technical/soft skills |
| `calculateATSScore()` | Computes weighted score (40% keywords + 30% skills + 30% format) |
| `generateSuggestions()` | Creates actionable recommendations |

**Analysis Algorithm:**
1. Extract text from PDF/DOCX file
2. Extract keywords from resume and job description
3. Calculate keyword match percentage
4. Analyze resume format quality
5. Detect technical and soft skills
6. Calculate weighted ATS score:
   - Keyword match: 40%
   - Skills match: 30%
   - Format quality: 30%
7. Generate improvement suggestions

---

### 4. **User Model** (Database Entity)
Represents user in PostgreSQL database

**Fields:**
```java
- id (Long)          // Primary key, auto-generated
- username (String)  // User login name
- email (String)     // User email
- password (String)  // User password (plaintext for demo)
- createdAt (LocalDateTime) // Account creation timestamp
```

---

### 5. **ATSAnalysisResult Model** (Response DTO)
Data Transfer Object containing analysis results

**Main Fields:**
```java
- atsScore (int)                    // Overall score 0-100
- keywordMatchPercentage (int)      // Percentage of matched keywords
- matchedKeywords (List<String>)    // Keywords found in resume
- missingKeywords (List<String>)    // Keywords not in resume
- suggestions (List<String>)        // Improvement recommendations

- formatAnalysis (FormatAnalysis)   // Nested object:
  - hasBulletPoints (boolean)
  - hasProperSections (boolean)
  - hasContactInfo (boolean)
  - wordCount (int)
  - issues (List<String>)

- skillsAnalysis (SkillsAnalysis)   // Nested object:
  - technicalSkills (List<String>)
  - softSkills (List<String>)
  - missingCriticalSkills (List<String>)
  - skillMatchPercentage (int)
```

---

### 6. **UserRepository** (Database Layer)
Spring Data JPA interface for database queries

**Key Methods:**
- `findByUsernameAndEmailAndPassword()` - Authenticates user
- `save()` - Stores new user in database

---

### 7. **CorsConfig** (Configuration)
Enables cross-origin requests from frontend to backend

**Allowed Origins:**
- `http://localhost:5173` (development frontend)
- `http://localhost:3000` (alternative frontend)

**Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS

---

## ⚛️ Frontend Components

### 1. **App.jsx** (Main Component)
Central state management and routing

**State Variables:**
```javascript
- isAuthenticated  // Login state
- file            // Selected resume file
- jobDescription  // Pasted job description
- loading         // Analysis in progress
- results         // Analysis results
- error           // Error messages
```

**Key Features:**
- Redirects to Login if not authenticated
- Handles file validation (PDF/DOCX only)
- Calls backend API for analysis
- Displays ScoreCard and ResultsPanel

---

### 2. **Login.jsx** (Authentication Component)
User registration and login form

**Features:**
- Toggle between register/login modes
- Form validation
- Error handling
- API calls to backend
- Calls `onLoginSuccess()` callback on successful login

**Form Fields:**
- Username
- Email Address
- Password

---

### 3. **ScoreCard.jsx** (Display Component)
Displays circular score indicators

**Props:**
```javascript
score       // Numeric score (0-100)
label       // Card title
description // Card subtitle
```

**Features:**
- SVG circular progress indicator
- Color coding:
  - Green ✓ (80+)
  - Orange ⚠️ (60-79)
  - Red ✗ (below 60)
- Score labels: Excellent, Good, Fair, Needs Improvement

---

### 4. **ResultsPanel.jsx** (Results Component)
Displays comprehensive analysis results

**Displays:**
1. **Keyword Analysis** - Matched/missing keywords
2. **Skills Analysis** - Technical, soft, missing skills
3. **Format Analysis** - Checkmarks for bullet points, sections, contact info
4. **Suggestions** - Numbered improvement recommendations

**Features:**
- Collapsible sections
- Color-coded tags
- Word count display
- Issue highlighting

---

### 5. **api.js** (Service Layer)
API communication with backend

**Functions:**
```javascript
analyzeResume(file, jobDescription)
  - Calls: POST /api/resume/analyze
  - Sends: FormData with file and jobDescription
  - Returns: ATSAnalysisResult

healthCheck()
  - Calls: GET /api/resume/health
  - Returns: Health status
```

---

### 6. **vite.config.js** (Build Configuration)
Vite configuration with proxy for API calls

**Key Settings:**
```javascript
port: 5173
proxy: {
  '/api': {
    target: 'http://localhost:8081',  // Backend URL
    changeOrigin: true
  }
}
```

**Why Proxy?**
- Allows frontend to call `/api/*` endpoints
- Automatically routes to `http://localhost:8081/api/*`
- Prevents CORS issues in development

---

## 📊 Data Flow Diagram

```
User (Browser)
    |
    | 1. Enter credentials
    v
[Login.jsx] ──POST /api/auth/login──> [AuthController]
                                        |
                                        v
                                    [UserRepository]
                                        |
                                        v
                                    [PostgreSQL]
    |
    | 2. Upload resume & job description
    v
[App.jsx] ──POST /api/resume/analyze──> [ResumeController]
                                         |
                                         v
                                    [ResumeAnalysisService]
                                         |
         ┌───────────┬────────────┬─────┴─────────┐
         |           |            |               |
         v           v            v               v
    [Extract  [Extract     [Analyze    [Analyze
     Text]    Keywords]    Format]     Skills]
         |           |            |               |
         └───────────┴────────────┴─────┬─────────┘
                                        v
                                 [ATSAnalysisResult]
    |
    | 3. Display results
    v
[ResultsPanel.jsx] <──response── [JSON]
[ScoreCard.jsx]
```

---

## 🔌 API Response Example

```json
{
  "atsScore": 75,
  "keywordMatchPercentage": 68,
  "matchedKeywords": ["java", "spring", "react", "sql"],
  "missingKeywords": ["kubernetes", "aws", "docker"],
  "suggestions": [
    "Add more relevant keywords from the job description.",
    "Missing key skills: kubernetes, aws, docker",
    "Use bullet points to list your achievements.",
    "Add clear sections: Summary, Experience, Education, Skills."
  ],
  "formatAnalysis": {
    "hasBulletPoints": true,
    "hasProperSections": true,
    "hasContactInfo": true,
    "wordCount": 450,
    "issues": []
  },
  "skillsAnalysis": {
    "technicalSkills": ["java", "spring", "react", "sql"],
    "softSkills": ["leadership", "communication"],
    "missingCriticalSkills": ["kubernetes", "aws"],
    "skillMatchPercentage": 70
  }
}
```

---

## 🛠️ Technologies & Versions

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.5.10 | REST API framework |
| Spring Web MVC | 3.5.10 | HTTP request handling |
| Spring Data JPA | 3.5.10 | Database persistence |
| PostgreSQL | 12+ | User data storage |
| Apache PDFBox | 2.0.29 | PDF text extraction |
| Apache POI | 5.2.3 | DOCX text extraction |
| Java | 17+ | Programming language |
| Maven | 3.6+ | Dependency management |

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| React | 18.x | UI library |
| Vite | 5.4.21 | Build tool |
| Axios | Latest | HTTP client |
| CSS3 | N/A | Styling |
| Node.js | 16+ | JavaScript runtime |
| npm | Latest | Package management |

---

## 🚀 Production Deployment

### Build Backend
```bash
cd backend
mvn clean package
```
Output: `backend/target/resume-checker-1.0.0.jar`

### Build Frontend
```bash
cd frontend
npm run build
```
Output: `frontend/dist/` (static files)

### Run JAR
```bash
java -jar resume-checker-1.0.0.jar
```

---


## ⚙️ Technologies Used

### Frontend
- **React 18**: Modern UI with Hooks
- **Vite**: Lightning-fast build tool
- **Axios**: Promise-based HTTP client
- **CSS3**: Custom responsive styling

### Backend
- **Spring Boot 3.5.10**: Production-grade framework
- **Spring Data JPA**: Object-relational mapping
- **PostgreSQL**: Relational database
- **Apache PDFBox**: PDF processing
- **Apache POI**: Office document processing

## 📦 Building for Production

### Frontend
```bash
cd frontend
npm run build
```

### Backend
```bash
cd backend
mvn clean package
```

## 🐛 Troubleshooting

### Backend Issues
| Issue | Solution |
|-------|----------|
| Port 8081 in use | Change `server.port` in application.properties |
| PostgreSQL connection fails | Verify DB running: `psql -U postgres` |
| File upload fails | Check file size in application.properties |

### Frontend Issues
| Issue | Solution |
|-------|----------|
| CORS errors | Verify CorsConfig.java allows localhost:5173 |
| API returns 404 | Check backend is running on 8081 |
| File upload not working | Ensure file is PDF or DOCX |


[![Watch the demo]([https://img.youtube.com/vi/YOUR_VIDEO_ID/maxresdefault.jpg](https://upload.wikimedia.org/wikipedia/commons/e/ef/Youtube_logo.png?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original))](https://youtu.be/aFusGbcipms?si=WYladM_kDgba9-ia) 

Contributions welcome! Submit issues or pull requests.

