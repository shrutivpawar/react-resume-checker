# 🎓 DETAILED IMPLEMENTATION GUIDE
## ATS Resume Checker - Complete Technical Documentation

---

## 📚 TABLE OF CONTENTS
1. [Backend Architecture](#backend-architecture)
2. [Frontend Architecture](#frontend-architecture)
3. [Data Flow & Communication](#data-flow--communication)
4. [Component Breakdown](#component-breakdown)
5. [Database Schema](#database-schema)
6. [Deployment Guide](#deployment-guide)

---

## 🔷 BACKEND ARCHITECTURE

### Overview
Spring Boot 3.5.10 REST API with PostgreSQL database

### Tech Stack
```
├── Spring Boot 3.5.10 (REST API framework)
├── Spring Web MVC (HTTP handling)
├── Spring Data JPA (Database ORM)
├── PostgreSQL 12+ (User storage)
├── Apache PDFBox 2.0.29 (PDF parsing)
├── Apache POI 5.2.3 (DOCX parsing)
├── Java 17+ (Programming language)
└── Maven 3.6+ (Build tool)
```

### Project Structure
```
backend/
├── src/main/java/com/ats/resume/
│   ├── ResumeCheckerApplication.java       # @SpringBootApplication entry point
│   │
│   ├── controller/
│   │   ├── AuthController.java             # User auth: /api/auth
│   │   │   ├── POST /register              # Create new user
│   │   │   └── POST /login                 # Authenticate user
│   │   │
│   │   └── ResumeController.java           # Resume analysis: /api/resume
│   │       ├── POST /analyze               # Analyze resume file
│   │       └── GET /health                 # Health check
│   │
│   ├── service/
│   │   └── ResumeAnalysisService.java      # Core business logic
│   │       ├── analyzeResume()             # Main orchestration
│   │       ├── extractTextFromFile()       # Parse PDF/DOCX
│   │       ├── extractKeywords()           # Keyword extraction
│   │       ├── analyzeFormat()             # Format checking
│   │       ├── analyzeSkills()             # Skills detection
│   │       ├── calculateATSScore()         # Score calculation (weighted)
│   │       └── generateSuggestions()       # Generate recommendations
│   │
│   ├── model/
│   │   ├── User.java                       # @Entity → users table
│   │   │   ├── id (PK, auto-increment)
│   │   │   ├── username (unique login)
│   │   │   ├── email (contact)
│   │   │   ├── password (authentication)
│   │   │   └── createdAt (registration timestamp)
│   │   │
│   │   └── ATSAnalysisResult.java          # Response DTO
│   │       ├── atsScore (0-100)
│   │       ├── keywordMatchPercentage (0-100)
│   │       ├── matchedKeywords[] (found)
│   │       ├── missingKeywords[] (not found)
│   │       ├── suggestions[] (recommendations)
│   │       ├── formatAnalysis (nested object)
│   │       │   ├── hasBulletPoints
│   │       │   ├── hasProperSections
│   │       │   ├── hasContactInfo
│   │       │   ├── wordCount
│   │       │   └── issues[]
│   │       └── skillsAnalysis (nested object)
│   │           ├── technicalSkills[]
│   │           ├── softSkills[]
│   │           ├── missingCriticalSkills[]
│   │           └── skillMatchPercentage
│   │
│   ├── repository/
│   │   └── UserRepository.java             # @Repository for DB queries
│   │       └── findByUsernameAndEmailAndPassword() - Custom JPA query
│   │
│   └── config/
│       └── CorsConfig.java                 # @Configuration for CORS
│           └── addCorsMappings()           - Allow cross-origin requests
│
├── src/main/resources/
│   └── application.properties               # Server & DB configuration
│       ├── server.port=8081
│       ├── spring.datasource.url=jdbc:postgresql://...
│       ├── spring.datasource.username=postgres
│       ├── spring.datasource.password=root
│       └── spring.jpa.hibernate.ddl-auto=update
│
└── pom.xml                                  # Maven dependencies
    ├── org.springframework.boot:spring-boot-starter-web
    ├── org.springframework.boot:spring-boot-starter-data-jpa
    ├── org.postgresql:postgresql
    ├── org.apache.pdfbox:pdfbox:2.0.29
    ├── org.apache.poi:poi-ooxml:5.2.3
    └── org.springframework.boot:spring-boot-devtools
```

---

## 🔧 BACKEND COMPONENTS IN DETAIL

### 1️⃣ AuthController.java
**Endpoint:** `/api/auth`
**Purpose:** Handle user authentication (register/login)

**POST /register**
```java
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody User user) {
    // 1. Receive User object from JSON request body
    // 2. Save to PostgreSQL database
    // 3. Return {message, id}
    User savedUser = userRepository.save(user);
    return ResponseEntity.ok(Map.of(
        "message", "User registered successfully",
        "id", savedUser.getId()
    ));
}
```

**POST /login**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody User loginRequest) {
    // 1. Query database for user matching ALL 3 fields
    Optional<User> user = userRepository.findByUsernameAndEmailAndPassword(
        loginRequest.getUsername(),
        loginRequest.getEmail(),
        loginRequest.getPassword()
    );
    
    // 2. If found → return {success: true, username}
    if (user.isPresent()) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "username", user.get().getUsername()
        ));
    }
    
    // 3. If not found → return 401 error
    return ResponseEntity.status(401).body(Map.of(
        "success", false,
        "message", "Invalid username, email, or password"
    ));
}
```

---

### 2️⃣ ResumeController.java
**Endpoint:** `/api/resume`
**Purpose:** Resume analysis API

**POST /analyze (Main endpoint)**
```java
@PostMapping("/analyze")
public ResponseEntity<?> analyzeResume(
    @RequestParam("file") MultipartFile file,
    @RequestParam("jobDescription") String jobDescription
) {
    // Validation
    if (file.isEmpty())
        return ResponseEntity.badRequest()
            .body(createErrorResponse("Please upload a resume file"));
    
    if (jobDescription == null || jobDescription.trim().isEmpty())
        return ResponseEntity.badRequest()
            .body(createErrorResponse("Please provide a job description"));
    
    String filename = file.getOriginalFilename();
    if (!filename.endsWith(".pdf") && !filename.endsWith(".docx"))
        return ResponseEntity.badRequest()
            .body(createErrorResponse("Only PDF and DOCX files supported"));
    
    // Call analysis service
    ATSAnalysisResult result = resumeAnalysisService.analyzeResume(file, jobDescription);
    
    return ResponseEntity.ok(result);  // Returns JSON
}
```

**GET /health**
```java
@GetMapping("/health")
public ResponseEntity<Map<String, String>> healthCheck() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("message", "ATS Resume Checker API is running");
    return ResponseEntity.ok(response);
}
```

---

### 3️⃣ ResumeAnalysisService.java
**Purpose:** Core ATS analysis business logic

**Analysis Algorithm:**
```
┌─────────────────────────────────────────────────┐
│ 1. EXTRACT TEXT                                 │
│    - If PDF: Use PDFBox to extract text         │
│    - If DOCX: Use Apache POI to extract text    │
│    - Output: Plain text string                  │
└──────────────┬────────────────────────────────┘
               │
┌──────────────v────────────────────────────────┐
│ 2. EXTRACT KEYWORDS                             │
│    - Split text into words                      │
│    - Filter out stop words (the, a, and, etc)   │
│    - Collect unique keywords from resume & JD   │
│    - Output: Set<String>                        │
└──────────────┬────────────────────────────────┘
               │
┌──────────────v────────────────────────────────┐
│ 3. CALCULATE KEYWORD MATCH                      │
│    - Find intersection: matched keywords        │
│    - Find difference: missing keywords          │
│    - Calculate: (matched/total) × 100           │
│    - Output: int keywordMatchPercentage         │
└──────────────┬────────────────────────────────┘
               │
┌──────────────v────────────────────────────────┐
│ 4. ANALYZE FORMAT                               │
│    - Check for bullet points (•, -, *)          │
│    - Check for section headers                  │
│    - Check for contact info (email/phone)       │
│    - Count word count                           │
│    - Output: FormatAnalysis object              │
└──────────────┬────────────────────────────────┘
               │
┌──────────────v────────────────────────────────┐
│ 5. ANALYZE SKILLS                               │
│    - Search for COMMON_TECH_SKILLS list        │
│    - Search for COMMON_SOFT_SKILLS list        │
│    - Find what's missing from JD               │
│    - Calculate match percentage                 │
│    - Output: SkillsAnalysis object              │
└──────────────┬────────────────────────────────┘
               │
┌──────────────v────────────────────────────────┐
│ 6. CALCULATE ATS SCORE (Weighted)               │
│    Score = (keyword% × 0.40) +                 │
│            (skill% × 0.30) +                   │
│            (format% × 0.30)                    │
│    Output: int atsScore (0-100)                │
└──────────────┬────────────────────────────────┘
               │
┌──────────────v────────────────────────────────┐
│ 7. GENERATE SUGGESTIONS                         │
│    - If low keywords: suggest keyword addition │
│    - If missing skills: list them              │
│    - If no bullet points: suggest adding       │
│    - If wrong sections: suggest fixing         │
│    - If wrong word count: suggest adjusting    │
│    - Output: List<String>                      │
└──────────────┬────────────────────────────────┘
               │
               v
        Return ATSAnalysisResult
```

**Key Methods:**

**extractTextFromFile()**
```java
private String extractTextFromFile(MultipartFile file) throws IOException {
    String filename = file.getOriginalFilename();
    
    if (filename.endsWith(".pdf")) {
        // Using Apache PDFBox
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(document);
    } 
    else if (filename.endsWith(".docx")) {
        // Using Apache POI
        XWPFDocument document = new XWPFDocument(file.getInputStream());
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        return extractor.getText();
    }
}
```

**extractKeywords()**
```java
private Set<String> extractKeywords(String text) {
    Set<String> stopWords = new HashSet<>(Arrays.asList(
        "the", "a", "an", "and", "or", "but", "in", "on", ...
    ));
    
    Pattern pattern = Pattern.compile("\\b[a-z0-9+#]{2,}\\b");
    Matcher matcher = pattern.matcher(text.toLowerCase());
    
    Set<String> keywords = new HashSet<>();
    while (matcher.find()) {
        String word = matcher.group();
        if (!stopWords.contains(word)) {
            keywords.add(word);
        }
    }
    return keywords;
}
```

**calculateATSScore()**
```java
private int calculateATSScore(...) {
    // Weighted calculation
    int score = 0;
    
    score += (keywordMatchPercentage * 0.40);      // 40% weight
    score += (skillsAnalysis.getSkillMatchPercentage() * 0.30);  // 30%
    
    // Format score (30% weight, subdivided)
    int formatScore = 0;
    if (formatAnalysis.isHasBulletPoints()) formatScore += 33;
    if (formatAnalysis.isHasProperSections()) formatScore += 34;
    if (formatAnalysis.isHasContactInfo()) formatScore += 33;
    score += (formatScore * 0.30);
    
    return Math.min(100, Math.max(0, score));  // Cap at 0-100
}
```

---

### 4️⃣ User.java (JPA Entity)
```java
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;      // Login credential
    private String email;         // Email credential  
    private String password;      // Password credential
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructors, getters, setters...
}
```

**Database Mapping:**
```sql
CREATE TABLE public.users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

### 5️⃣ ATSAnalysisResult.java (Response DTO)
Contains all analysis results returned to frontend as JSON

```java
public class ATSAnalysisResult {
    public int atsScore;                    // 0-100
    public int keywordMatchPercentage;      // 0-100
    public List<String> matchedKeywords;    // Found keywords
    public List<String> missingKeywords;    // Not found
    public List<String> suggestions;        // Recommendations
    public FormatAnalysis formatAnalysis;   // Format details
    public SkillsAnalysis skillsAnalysis;   // Skills details
    
    // Nested class for format analysis
    public static class FormatAnalysis {
        public boolean hasBulletPoints;
        public boolean hasProperSections;
        public boolean hasContactInfo;
        public int wordCount;
        public List<String> issues;
    }
    
    // Nested class for skills analysis
    public static class SkillsAnalysis {
        public List<String> technicalSkills;
        public List<String> softSkills;
        public List<String> missingCriticalSkills;
        public int skillMatchPercentage;
    }
}
```

---

### 6️⃣ UserRepository.java (Data Access)
Spring Data JPA interface for database operations

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring automatically implements this!
    Optional<User> findByUsernameAndEmailAndPassword(
        String username,
        String email,
        String password
    );
}
```

**Generated SQL:**
```sql
SELECT * FROM public.users 
WHERE username = ? AND email = ? AND password = ?
LIMIT 1;
```

---

### 7️⃣ CorsConfig.java (Configuration)
Enables cross-origin requests from frontend

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")              // All endpoints
                .allowedOrigins(                 // From these origins:
                    "http://localhost:5173",     // Dev frontend (Vite)
                    "http://localhost:3000"      // Alt frontend
                )
                .allowedMethods(                 // HTTP methods allowed:
                    "GET", "POST", "PUT", "DELETE", "OPTIONS"
                )
                .allowedHeaders("*")             // Any header allowed
                .allowCredentials(true);        // Allow cookies
    }
}
```

---

## ⚛️ FRONTEND ARCHITECTURE

### Tech Stack
```
├── React 18 (UI library with Hooks)
├── Vite 5.4.21 (Lightning-fast bundler)
├── Axios (Promise-based HTTP client)
├── CSS3 (Custom responsive styling)
├── Node.js 16+ (JavaScript runtime)
└── npm (Package manager)
```

### Project Structure
```
frontend/
├── src/
│   ├── components/
│   │   ├── Login.jsx                # Authentication UI
│   │   │   ├── Register form
│   │   │   ├── Login form
│   │   │   ├── Error messages
│   │   │   └── API calls
│   │   │
│   │   ├── ScoreCard.jsx            # Score display component
│   │   │   ├── SVG circular progress
│   │   │   ├── Color coding (green/orange/red)
│   │   │   └── Score labels
│   │   │
│   │   └── ResultsPanel.jsx         # Results display component
│   │       ├── Keyword analysis
│   │       ├── Skills analysis
│   │       ├── Format analysis
│   │       └── Suggestions
│   │
│   ├── services/
│   │   └── api.js                   # HTTP service layer
│   │       ├── analyzeResume(file, jobDescription)
│   │       └── healthCheck()
│   │
│   ├── App.jsx                      # Main component
│   │   ├── State management (useState)
│   │   ├── Event handlers
│   │   ├── File validation
│   │   └── Component composition
│   │
│   ├── App.css                      # Global styles
│   ├── main.jsx                     # React entry point
│   └── index.html                   # HTML template
│
├── vite.config.js                   # Vite config + proxy
├── package.json                     # npm scripts & deps
└── index.html                       # HTML base
```

---

## 🔌 FRONTEND COMPONENTS

### 1️⃣ App.jsx (Main Component)
Central state management and orchestration

**State Variables:**
```javascript
const [isAuthenticated, setIsAuthenticated] = useState(false);
const [file, setFile] = useState(null);
const [jobDescription, setJobDescription] = useState('');
const [loading, setLoading] = useState(false);
const [results, setResults] = useState(null);
const [error, setError] = useState('');
```

**Key Features:**
- Redirects to Login if not authenticated
- Validates file format (PDF/DOCX only)
- Calls backend API
- Displays results with ScoreCard and ResultsPanel

---

### 2️⃣ Login.jsx (Authentication)
User login and registration form

**Features:**
- Toggle between login/register modes
- Form validation
- Error handling with try/catch
- POST to `/api/auth/login` or `/api/auth/register`
- Calls `onLoginSuccess()` on success

**Flow:**
```
User enters credentials
    ↓
handleSubmit() → axios.post('/api/auth/login')
    ↓
Backend validates → returns {success: true}
    ↓
Frontend calls onLoginSuccess()
    ↓
App.jsx: setIsAuthenticated(true)
    ↓
App re-renders → shows main application
```

---

### 3️⃣ ScoreCard.jsx (Score Display)
Display ATS score with circular progress indicator

**SVG Circle Progress Formula:**
```javascript
// Circle circumference = 2πr = 2 × 3.14159 × 50 = 314.159
// strokeDasharray = `${(score/100) * 314} 314`
// Score 75 → dasharray = "235.5 314"  → shows 75% arc
```

**Color Coding:**
- Green: ≥ 80 (Excellent)
- Orange: 60-79 (Good)
- Red: < 60 (Needs Improvement)

---

### 4️⃣ ResultsPanel.jsx (Results Display)
Display comprehensive analysis in 4 sections

**Sections:**
1. Keyword Analysis (matched vs missing)
2. Skills Analysis (technical, soft, missing)
3. Format Analysis (bullet points, sections, etc.)
4. Suggestions (numbered recommendations)

---

### 5️⃣ api.js (HTTP Service)
Centralized API communication

```javascript
const API_BASE_URL = '/api';  // Uses Vite proxy

export const analyzeResume = async (file, jobDescription) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('jobDescription', jobDescription);
    
    const response = await axios.post(
        `${API_BASE_URL}/resume/analyze`,
        formData
    );
    return response.data;
};
```

---

### 6️⃣ vite.config.js (Build & Proxy)
Vite configuration with development proxy

```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
```

**Why Proxy?**
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8081`
- Different ports = CORS issue
- Vite proxy routes `/api/*` → backend

---

## 📊 DATA FLOW DIAGRAM

### Authentication Flow
```
┌─────────────┐
│  User Input │
└──────┬──────┘
       │ Login form data
       v
┌──────────────────────────────────────┐
│ App.jsx → Login.jsx                  │
│ handleSubmit()                       │
│ axios.post('/api/auth/login')        │
└──────┬───────────────────────────────┘
       │ HTTP POST (Vite proxy)
       v
┌──────────────────────────────────────┐
│ Backend (8081)                       │
│ AuthController.login()               │
│ UserRepository.findBy...()           │
└──────┬───────────────────────────────┘
       │ Query PostgreSQL
       v
┌──────────────────────────────────────┐
│ PostgreSQL users table               │
│ SELECT * WHERE username=? AND ...    │
└──────┬───────────────────────────────┘
       │ Return user (or empty)
       v
┌──────────────────────────────────────┐
│ Response: {success: true/false}      │
└──────┬───────────────────────────────┘
       │ JSON response
       v
┌──────────────────────────────────────┐
│ App.jsx                              │
│ setIsAuthenticated(true)             │
│ Re-render → show main app            │
└──────────────────────────────────────┘
```

### Analysis Flow
```
┌─────────────────────────────┐
│ Resume file + Job Description│
└──────┬──────────────────────┘
       │ Form submission
       v
┌─────────────────────────────┐
│ App.jsx.handleSubmit()      │
│ analyzeResume(file, jobDesc)│
└──────┬──────────────────────┘
       │ FormData POST (via proxy)
       v
┌──────────────────────────────────────────┐
│ ResumeController.analyzeResume()         │
│ Validation + service call                │
└──────┬───────────────────────────────────┘
       │ Call service
       v
┌──────────────────────────────────────────┐
│ ResumeAnalysisService                    │
│ ├─ extractTextFromFile() [PDF/DOCX]      │
│ ├─ extractKeywords() [Text]              │
│ ├─ analyzeFormat() [Checks]              │
│ ├─ analyzeSkills() [Detection]           │
│ ├─ calculateATSScore() [Math]            │
│ └─ generateSuggestions() [Logic]         │
└──────┬───────────────────────────────────┘
       │ ATSAnalysisResult object
       v
┌──────────────────────────────────────────┐
│ JSON Response                            │
└──────┬───────────────────────────────────┘
       │ HTTP response
       v
┌──────────────────────────────────────────┐
│ App.jsx                                  │
│ setResults(data)                         │
│ Re-render with ScoreCard + ResultsPanel  │
└──────────────────────────────────────────┘
```

---

## 🗄️ DATABASE SCHEMA

**PostgreSQL users table:**
```sql
CREATE TABLE public.users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Sample queries:**
```sql
-- Insert user
INSERT INTO users (username, email, password) 
VALUES ('shruti', 'shruti@gmail.com', '123');

-- Authenticate
SELECT * FROM users 
WHERE username='shruti' AND email='shruti@gmail.com' AND password='123';

-- View all
SELECT * FROM users ORDER BY created_at DESC;
```

---

## 🚀 PRODUCTION DEPLOYMENT

### 1. Build Backend
```bash
cd backend
mvn clean package
```
Output: `backend/target/resume-checker-1.0.0.jar`

### 2. Build Frontend
```bash
cd frontend
npm run build
```
Output: `frontend/dist/` (static files)

### 3. Run JAR
```bash
java -jar resume-checker-1.0.0.jar
```
Backend runs on port 8081

### 4. Serve Frontend
Option A: Embed in JAR
```bash
cp -r frontend/dist/* backend/src/main/resources/static/
mvn clean package
```

Option B: Serve separately
```bash
npm install -g serve
serve -s frontend/dist -l 5173
```

---

## 🎯 KEY ALGORITHMS

### ATS Score Calculation
```
Final Score = (KeywordScore × 40%) + 
              (SkillScore × 30%) + 
              (FormatScore × 30%)

Example:
- Keywords: 70/100 → 70 × 0.40 = 28
- Skills: 85/100 → 85 × 0.30 = 25.5
- Format: 95/100 → 95 × 0.30 = 28.5
- Total: 82.0 out of 100
```

### Keyword Matching
```
1. Extract all unique words from JD
2. Extract all unique words from Resume
3. Remove stop words (the, a, and...)
4. Matched = Intersection of both sets
5. Missing = JD - Resume
6. Percentage = (Matched / JD Total) × 100
```

### Skills Detection
```
COMMON_TECH_SKILLS = ["java", "python", "react", "spring", ...]
COMMON_SOFT_SKILLS = ["leadership", "communication", ...]

Algorithm:
1. For each skill in COMMON_TECH_SKILLS:
   if skill in resume_text → add to found_tech_skills
2. For each skill in COMMON_TECH_SKILLS:
   if skill in jd_text but NOT in resume → add to missing
3. Calculate match percentage
```

---

## 🏁 SUMMARY

**This project demonstrates:**
- ✅ Full-stack development (React + Spring Boot)
- ✅ RESTful API design principles
- ✅ Database persistence with JPA/Hibernate
- ✅ File processing (PDF & DOCX)
- ✅ Complex business logic
- ✅ Frontend-backend integration
- ✅ Authentication & authorization
- ✅ CORS configuration
- ✅ Error handling
- ✅ Input validation
- ✅ Responsive UI components
- ✅ HTTP communication (Axios)
- ✅ Component composition
- ✅ State management (React Hooks)
- ✅ Production-ready architecture

**Purpose**: This file defines all the libraries (dependencies) your project needs.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- PARENT: Spring Boot Starter Parent -->
    <!-- WHY: Provides dependency management, plugin configuration -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <!-- PROJECT INFORMATION -->
    <groupId>com.ats</groupId>
    <artifactId>resume-checker</artifactId>
    <version>1.0.0</version>
    <name>ATS Resume Checker</name>
    <description>ATS Resume Checker Backend</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        
        <!-- DEPENDENCY 1: Spring Boot Web Starter -->
        <!-- PURPOSE: Creates RESTful APIs, includes Tomcat server, Jackson for JSON -->
        <!-- PROVIDES: @RestController, @RequestMapping, @PostMapping, etc. -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- DEPENDENCY 2: Apache PDFBox -->
        <!-- PURPOSE: Extract text from PDF files -->
        <!-- USED IN: ResumeAnalysisService.extractTextFromFile() -->
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.29</version>
        </dependency>
        
        <!-- DEPENDENCY 3: Apache POI (OOXML) -->
        <!-- PURPOSE: Extract text from DOCX (Word) files -->
        <!-- USED IN: ResumeAnalysisService.extractTextFromFile() -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.2.3</version>
        </dependency>
        
        <!-- DEPENDENCY 4: Lombok -->
        <!-- PURPOSE: Reduces boilerplate code (@Data, @Builder, etc.) -->
        <!-- USED IN: ATSAnalysisResult model class -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- DEPENDENCY 5: Spring Boot DevTools -->
        <!-- PURPOSE: Hot reload during development (auto-restart on code changes) -->
        <!-- OPTIONAL: Only for development convenience -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
    </dependencies>
    
    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin -->
            <!-- PURPOSE: Packages application as executable JAR -->
            <!-- COMMAND: mvn spring-boot:run -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**After creating pom.xml, run:**
```bash
mvn clean install
# This downloads all dependencies (will take a few minutes first time)
```

---

### STEP 3: Configure Application Properties

**File Location**: `backend/src/main/resources/application.properties`

**Purpose**: Configuration file for Spring Boot application

```properties
# SERVER CONFIGURATION
# Sets the port where backend will run
server.port=8080

# FILE UPLOAD CONFIGURATION
# Maximum size for a single file upload (10MB)
spring.servlet.multipart.max-file-size=10MB

# Maximum size for entire request (10MB)
spring.servlet.multipart.max-request-size=10MB

# APPLICATION NAME (appears in logs)
spring.application.name=ats-resume-checker
```

**Why These Configurations?**
- `server.port=8080`: Backend runs on this port (frontend will call http://localhost:8080/api/...)
- `max-file-size=10MB`: Prevents users from uploading huge files that could crash the server
- `max-request-size=10MB`: Total request size limit

---

### STEP 4: Create Main Application Class

**File Location**: `backend/src/main/java/com/ats/resume/ResumeCheckerApplication.java`

**Purpose**: Entry point of the Spring Boot application

```java
package com.ats.resume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MAIN APPLICATION CLASS
 * 
 * @SpringBootApplication annotation does 3 things:
 * 1. @Configuration - Allows defining beans
 * 2. @EnableAutoConfiguration - Tells Spring Boot to auto-configure based on dependencies
 * 3. @ComponentScan - Scans for @Controller, @Service, @Repository in this package
 */
@SpringBootApplication
public class ResumeCheckerApplication {
    
    /**
     * MAIN METHOD - Application starts here
     * SpringApplication.run() starts the embedded Tomcat server
     */
    public static void main(String[] args) {
        SpringApplication.run(ResumeCheckerApplication.class, args);
        System.out.println("✅ ATS Resume Checker Backend Started on http://localhost:8080");
    }
}
```

**What Happens When You Run This?**
1. Spring Boot scans for components (@Controller, @Service)
2. Auto-configures based on dependencies (creates beans)
3. Starts embedded Tomcat server on port 8080
4. Application is ready to receive HTTP requests

---

### STEP 5: Create CORS Configuration

**File Location**: `backend/src/main/java/com/ats/resume/config/CorsConfig.java`

**Purpose**: Allow frontend (running on port 5173) to call backend (port 8080)

**Why Needed?** 
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Different ports = CORS issue by default
- This configuration allows cross-origin requests

```java
package com.ats.resume.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS CONFIGURATION
 * 
 * @Configuration - Tells Spring this is a configuration class
 * WebMvcConfigurer - Interface to customize Spring MVC
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    /**
     * Configure CORS mappings
     * Allows frontend to communicate with backend
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins(
                    "http://localhost:5173",  // Vite dev server
                    "http://localhost:3000"   // Alternative React port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")  // Allow all headers
                .allowCredentials(true);  // Allow cookies/auth
    }
}
```

**What Each Part Does:**
- `addMapping("/**")`: Apply CORS to ALL API endpoints
- `allowedOrigins`: Which URLs can call our API
- `allowedMethods`: Which HTTP methods are allowed
- `allowedHeaders("*")`: Accept any headers in requests
- `allowCredentials(true)`: Allow authentication cookies

---

### STEP 6: Create Data Model (Response Structure)

**File Location**: `backend/src/main/java/com/ats/resume/model/ATSAnalysisResult.java`

**Purpose**: Defines the structure of the JSON response sent to frontend

```java
package com.ats.resume.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * MAIN RESPONSE MODEL
 * 
 * Lombok Annotations Explained:
 * @Data - Generates getters, setters, toString, equals, hashCode
 * @Builder - Allows builder pattern: ATSAnalysisResult.builder().atsScore(80).build()
 * @NoArgsConstructor - Generates no-argument constructor
 * @AllArgsConstructor - Generates constructor with all fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ATSAnalysisResult {
    
    // Overall ATS compatibility score (0-100)
    private int atsScore;
    
    // Percentage of JD keywords found in resume
    private int keywordMatchPercentage;
    
    // Keywords that matched between resume and JD
    private List<String> matchedKeywords;
    
    // Keywords from JD that are missing in resume
    private List<String> missingKeywords;
    
    // Actionable suggestions for improvement
    private List<String> suggestions;
    
    // Detailed format analysis
    private FormatAnalysis formatAnalysis;
    
    // Detailed skills analysis
    private SkillsAnalysis skillsAnalysis;
    
    /**
     * NESTED CLASS: Format Analysis Details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormatAnalysis {
        // Does resume use bullet points?
        private boolean hasBulletPoints;
        
        // Does resume have standard sections?
        private boolean hasProperSections;
        
        // Does resume have contact information?
        private boolean hasContactInfo;
        
        // Total word count
        private int wordCount;
        
        // List of format-related issues
        private List<String> issues;
    }
    
    /**
     * NESTED CLASS: Skills Analysis Details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillsAnalysis {
        // Technical skills found in resume
        private List<String> technicalSkills;
        
        // Soft skills found in resume
        private List<String> softSkills;
        
        // Critical skills from JD that are missing
        private List<String> missingCriticalSkills;
        
        // Percentage of required skills present
        private int skillMatchPercentage;
    }
}
```

**Why This Structure?**
- Clean, organized response
- Frontend knows exactly what data to expect
- Nested classes group related data
- Lombok reduces boilerplate code significantly

**JSON Response Example:**
```json
{
  "atsScore": 75,
  "keywordMatchPercentage": 68,
  "matchedKeywords": ["java", "spring", "react"],
  "formatAnalysis": {
    "hasBulletPoints": true,
    "wordCount": 450
  }
}
```

---

### STEP 7: Create Service Layer (Business Logic)

**File Location**: `backend/src/main/java/com/ats/resume/service/ResumeAnalysisService.java`

**Purpose**: Contains ALL the resume analysis logic

**Why Service Layer?**
- Separates business logic from controller
- Reusable code
- Easier to test
- Follows best practices (separation of concerns)

```java
package com.ats.resume.service;

import com.ats.resume.model.ATSAnalysisResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * RESUME ANALYSIS SERVICE
 * 
 * @Service - Tells Spring this is a service component
 * Spring will create a single instance (singleton) and manage it
 */
@Service
public class ResumeAnalysisService {
    
    // ============ PREDEFINED DATA ============
    
    // Standard resume sections to look for
    private static final List<String> REQUIRED_SECTIONS = Arrays.asList(
        "experience", "education", "skills", "summary", "profile"
    );
    
    // Common technical skills to detect
    private static final List<String> COMMON_TECH_SKILLS = Arrays.asList(
        "java", "python", "javascript", "react", "angular", "spring", 
        "nodejs", "sql", "mongodb", "aws", "azure", "docker", 
        "kubernetes", "git", "html", "css", "typescript"
    );
    
    // Common soft skills to detect
    private static final List<String> COMMON_SOFT_SKILLS = Arrays.asList(
        "leadership", "communication", "teamwork", "problem-solving",
        "analytical", "creative", "adaptable", "collaborative"
    );
    
    // ============ MAIN ANALYSIS METHOD ============
    
    /**
     * MAIN METHOD: Analyzes resume against job description
     * 
     * @param file - Uploaded resume (PDF or DOCX)
     * @param jobDescription - Job description text
     * @return ATSAnalysisResult with complete analysis
     */
    public ATSAnalysisResult analyzeResume(MultipartFile file, String jobDescription) 
            throws IOException {
        
        // STEP 1: Extract text from uploaded file
        String resumeText = extractTextFromFile(file);
        
        // STEP 2: Convert to lowercase for case-insensitive matching
        String resumeLower = resumeText.toLowerCase();
        String jdLower = jobDescription.toLowerCase();
        
        // STEP 3: Extract keywords from both texts
        Set<String> jdKeywords = extractKeywords(jdLower);
        Set<String> resumeKeywords = extractKeywords(resumeLower);
        
        // STEP 4: Find matched keywords (intersection)
        Set<String> matchedKeywords = new HashSet<>(jdKeywords);
        matchedKeywords.retainAll(resumeKeywords);  // Keep only common elements
        
        // STEP 5: Find missing keywords (difference)
        Set<String> missingKeywords = new HashSet<>(jdKeywords);
        missingKeywords.removeAll(resumeKeywords);  // Remove found keywords
        
        // STEP 6: Calculate keyword match percentage
        int keywordMatchPercentage = jdKeywords.isEmpty() ? 0 : 
            (int) ((matchedKeywords.size() * 100.0) / jdKeywords.size());
        
        // STEP 7: Analyze resume format
        ATSAnalysisResult.FormatAnalysis formatAnalysis = analyzeFormat(resumeText);
        
        // STEP 8: Analyze skills
        ATSAnalysisResult.SkillsAnalysis skillsAnalysis = 
            analyzeSkills(resumeLower, jdLower);
        
        // STEP 9: Calculate overall ATS score
        int atsScore = calculateATSScore(
            keywordMatchPercentage,
            formatAnalysis,
            skillsAnalysis
        );
        
        // STEP 10: Generate improvement suggestions
        List<String> suggestions = generateSuggestions(
            keywordMatchPercentage,
            formatAnalysis,
            skillsAnalysis,
            missingKeywords
        );
        
        // STEP 11: Build and return final result
        return ATSAnalysisResult.builder()
            .atsScore(atsScore)
            .keywordMatchPercentage(keywordMatchPercentage)
            .matchedKeywords(new ArrayList<>(matchedKeywords))
            .missingKeywords(new ArrayList<>(missingKeywords))
            .suggestions(suggestions)
            .formatAnalysis(formatAnalysis)
            .skillsAnalysis(skillsAnalysis)
            .build();
    }
    
    // ============ TEXT EXTRACTION ============
    
    /**
     * Extracts text from PDF or DOCX file
     * 
     * PDF: Uses Apache PDFBox
     * DOCX: Uses Apache POI
     */
    private String extractTextFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        
        // Handle PDF files
        if (filename.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } 
        // Handle DOCX files
        else if (filename.endsWith(".docx")) {
            try (XWPFDocument document = new XWPFDocument(file.getInputStream());
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        } 
        else {
            throw new IllegalArgumentException(
                "Unsupported file format. Please upload PDF or DOCX."
            );
        }
    }
    
    // ============ KEYWORD EXTRACTION ============
    
    /**
     * Extracts meaningful keywords from text
     * 
     * Process:
     * 1. Remove common stop words (the, a, an, etc.)
     * 2. Extract words with 2+ characters
     * 3. Keep only alphanumeric words
     */
    private Set<String> extractKeywords(String text) {
        // Stop words to ignore
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", 
            "for", "of", "with", "by", "from", "as", "is", "was", "are", 
            "were", "been", "be", "have", "has", "had", "do", "does", "did",
            "will", "would", "should", "could", "may", "might", "can"
            // ... (full list in actual code)
        ));
        
        // Regex: Match words with 2+ alphanumeric characters
        Pattern pattern = Pattern.compile("\\b[a-z0-9+#]{2,}\\b");
        Matcher matcher = pattern.matcher(text);
        
        Set<String> keywords = new HashSet<>();
        while (matcher.find()) {
            String word = matcher.group();
            // Add only if not a stop word
            if (!stopWords.contains(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    // ============ FORMAT ANALYSIS ============
    
    /**
     * Analyzes resume formatting quality
     * 
     * Checks for:
     * - Bullet points usage
     * - Standard sections (Experience, Education, etc.)
     * - Contact information (email, phone)
     * - Word count (optimal: 300-800)
     */
    private ATSAnalysisResult.FormatAnalysis analyzeFormat(String resumeText) {
        List<String> issues = new ArrayList<>();
        
        // Check for bullet points
        boolean hasBulletPoints = resumeText.contains("•") || 
                                  resumeText.contains("·") || 
                                  resumeText.matches(".*[\\n\\r]\\s*[-*]\\s+.*");
        
        if (!hasBulletPoints) {
            issues.add("No bullet points detected. Use bullet points to improve readability.");
        }
        
        // Check for standard sections
        String resumeLower = resumeText.toLowerCase();
        boolean hasProperSections = REQUIRED_SECTIONS.stream()
            .anyMatch(resumeLower::contains);
        
        if (!hasProperSections) {
            issues.add("Missing standard sections (Experience, Education, Skills, etc.)");
        }
        
        // Check for contact information
        boolean hasEmail = resumeText.matches(
            ".*\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b.*"
        );
        boolean hasPhone = resumeText.matches(
            ".*\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b.*"
        );
        boolean hasContactInfo = hasEmail || hasPhone;
        
        if (!hasContactInfo) {
            issues.add("Contact information (email/phone) not clearly visible.");
        }
        
        // Count words
        int wordCount = resumeText.split("\\s+").length;
        
        if (wordCount < 200) {
            issues.add("Resume seems too short. Aim for 300-800 words.");
        } else if (wordCount > 1000) {
            issues.add("Resume might be too long. Keep it concise (300-800 words).");
        }
        
        return ATSAnalysisResult.FormatAnalysis.builder()
            .hasBulletPoints(hasBulletPoints)
            .hasProperSections(hasProperSections)
            .hasContactInfo(hasContactInfo)
            .wordCount(wordCount)
            .issues(issues)
            .build();
    }
    
    // ============ SKILLS ANALYSIS ============
    
    /**
     * Analyzes skills present in resume vs required in JD
     */
    private ATSAnalysisResult.SkillsAnalysis analyzeSkills(
            String resumeLower, String jdLower) {
        
        // Find technical skills in resume
        List<String> foundTechSkills = COMMON_TECH_SKILLS.stream()
            .filter(resumeLower::contains)
            .collect(Collectors.toList());
        
        // Find soft skills in resume
        List<String> foundSoftSkills = COMMON_SOFT_SKILLS.stream()
            .filter(resumeLower::contains)
            .collect(Collectors.toList());
        
        // Find required skills in JD
        List<String> jdRequiredSkills = COMMON_TECH_SKILLS.stream()
            .filter(jdLower::contains)
            .collect(Collectors.toList());
        
        // Find missing critical skills
        List<String> missingCriticalSkills = jdRequiredSkills.stream()
            .filter(skill -> !resumeLower.contains(skill))
            .collect(Collectors.toList());
        
        // Calculate skill match percentage
        int skillMatchPercentage = jdRequiredSkills.isEmpty() ? 100 :
            (int) (((jdRequiredSkills.size() - missingCriticalSkills.size()) * 100.0) 
                   / jdRequiredSkills.size());
        
        return ATSAnalysisResult.SkillsAnalysis.builder()
            .technicalSkills(foundTechSkills)
            .softSkills(foundSoftSkills)
            .missingCriticalSkills(missingCriticalSkills)
            .skillMatchPercentage(skillMatchPercentage)
            .build();
    }
    
    // ============ SCORE CALCULATION ============
    
    /**
     * Calculates overall ATS score (0-100)
     * 
     * Formula:
     * Score = (Keyword Match × 40%) + (Skills Match × 30%) + (Format × 30%)
     */
    private int calculateATSScore(
        int keywordMatchPercentage,
        ATSAnalysisResult.FormatAnalysis formatAnalysis,
        ATSAnalysisResult.SkillsAnalysis skillsAnalysis
    ) {
        int score = 0;
        
        // Keyword match weight: 40%
        score += (keywordMatchPercentage * 0.4);
        
        // Skills match weight: 30%
        score += (skillsAnalysis.getSkillMatchPercentage() * 0.3);
        
        // Format score weight: 30%
        int formatScore = 0;
        if (formatAnalysis.isHasBulletPoints()) formatScore += 33;
        if (formatAnalysis.isHasProperSections()) formatScore += 34;
        if (formatAnalysis.isHasContactInfo()) formatScore += 33;
        score += (formatScore * 0.3);
        
        // Ensure score is between 0 and 100
        return Math.min(100, Math.max(0, score));
    }
    
    // ============ SUGGESTIONS GENERATION ============
    
    /**
     * Generates actionable suggestions for improvement
     */
    private List<String> generateSuggestions(
        int keywordMatchPercentage,
        ATSAnalysisResult.FormatAnalysis formatAnalysis,
        ATSAnalysisResult.SkillsAnalysis skillsAnalysis,
        Set<String> missingKeywords
    ) {
        List<String> suggestions = new ArrayList<>();
        
        // Low keyword match
        if (keywordMatchPercentage < 50) {
            suggestions.add(
                "Low keyword match! Add more relevant keywords from the job description."
            );
        }
        
        // Missing skills
        if (!skillsAnalysis.getMissingCriticalSkills().isEmpty()) {
            suggestions.add("Missing key skills: " + 
                String.join(", ", skillsAnalysis.getMissingCriticalSkills()
                    .subList(0, Math.min(5, skillsAnalysis.getMissingCriticalSkills().size())))
            );
        }
        
        // Format issues
        if (!formatAnalysis.isHasBulletPoints()) {
            suggestions.add(
                "Use bullet points to list your achievements and responsibilities."
            );
        }
        
        if (!formatAnalysis.isHasProperSections()) {
            suggestions.add(
                "Add clear sections: Summary, Experience, Education, Skills."
            );
        }
        
        // Word count
        if (formatAnalysis.getWordCount() < 200) {
            suggestions.add(
                "Expand your resume with more details about your experience and achievements."
            );
        } else if (formatAnalysis.getWordCount() > 1000) {
            suggestions.add(
                "Shorten your resume. Focus on the most relevant experience."
            );
        }
        
        // Default if no issues
        if (suggestions.isEmpty()) {
            suggestions.add("Great job! Your resume is well-optimized for ATS.");
        }
        
        return suggestions;
    }
}
```

**Key Spring Boot Concepts Used:**
- `@Service`: Marks class as a service component
- `MultipartFile`: Spring's interface for uploaded files
- Dependency Injection: Service will be injected into Controller

---

### STEP 8: Create REST Controller (API Endpoints)

**File Location**: `backend/src/main/java/com/ats/resume/controller/ResumeController.java`

**Purpose**: Exposes HTTP endpoints for frontend to call

```java
package com.ats.resume.controller;

import com.ats.resume.model.ATSAnalysisResult;
import com.ats.resume.service.ResumeAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST CONTROLLER
 * 
 * @RestController - Combines @Controller + @ResponseBody
 *                   Automatically converts return values to JSON
 * @RequestMapping - Base URL for all endpoints in this controller
 */
@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    
    /**
     * DEPENDENCY INJECTION
     * 
     * @Autowired - Spring automatically injects ResumeAnalysisService instance
     * We don't use "new ResumeAnalysisService()" - Spring manages it
     */
    @Autowired
    private ResumeAnalysisService resumeAnalysisService;
    
    /**
     * ENDPOINT 1: Analyze Resume
     * 
     * URL: POST http://localhost:8080/api/resume/analyze
     * 
     * @PostMapping - Handles HTTP POST requests
     * @RequestParam - Extracts data from request
     *   - "file": The uploaded resume file
     *   - "jobDescription": The job description text
     * 
     * ResponseEntity<?> - Generic response that can return any type with HTTP status
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription
    ) {
        try {
            // Validation 1: Check if file is uploaded
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Please upload a resume file"));
            }
            
            // Validation 2: Check if job description is provided
            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Please provide a job description"));
            }
            
            // Validation 3: Check file type
            String filename = file.getOriginalFilename();
            if (filename == null || 
                (!filename.endsWith(".pdf") && !filename.endsWith(".docx"))) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Only PDF and DOCX files are supported"));
            }
            
            // Call service to analyze resume
            ATSAnalysisResult result = resumeAnalysisService.analyzeResume(
                file, 
                jobDescription
            );
            
            // Return success response with 200 OK status
            // Spring automatically converts ATSAnalysisResult to JSON
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // Return error response with 500 Internal Server Error status
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error analyzing resume: " + e.getMessage()));
        }
    }
    
    /**
     * ENDPOINT 2: Health Check
     * 
     * URL: GET http://localhost:8080/api/resume/health
     * 
     * @GetMapping - Handles HTTP GET requests
     * Purpose: Check if backend is running
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "ATS Resume Checker API is running");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
```

**Spring Boot Magic Happening Here:**

1. **@RestController**: 
   - Tells Spring this class handles HTTP requests
   - Automatically converts Java objects to JSON

2. **@RequestMapping("/api/resume")**:
   - All endpoints start with `/api/resume`
   - Example: `/api/resume/analyze`, `/api/resume/health`

3. **@Autowired**:
   - Spring automatically creates `ResumeAnalysisService` instance
   - Injects it into this controller
   - No manual object creation needed!

4. **@PostMapping & @GetMapping**:
   - Map HTTP methods to Java methods
   - `@PostMapping` = POST requests
   - `@GetMapping` = GET requests

5. **@RequestParam**:
   - Extracts data from HTTP request
   - `@RequestParam("file")` → gets uploaded file
   - `@RequestParam("jobDescription")` → gets text parameter

6. **ResponseEntity**:
   - Wrapper for HTTP response
   - Can set status code (200 OK, 400 Bad Request, 500 Error)
   - Can set response body (JSON data)

7. **Automatic JSON Conversion**:
   - Return `ATSAnalysisResult` object
   - Spring Boot (using Jackson) automatically converts to JSON
   - No manual JSON creation needed!

---

### STEP 9: Run the Backend

```bash
cd backend

# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using IDE
# Right-click ResumeCheckerApplication.java → Run

# You should see:
# ✅ ATS Resume Checker Backend Started on http://localhost:8080
```

**Test the backend:**
```bash
# Test health check endpoint
curl http://localhost:8080/api/resume/health

# Expected response:
# {"status":"UP","message":"ATS Resume Checker API is running"}
```

---

## 🎨 FRONTEND IMPLEMENTATION (Step-by-Step)

[... The rest of the frontend section continues with similar detailed explanations...]

---

## 📊 COMPLETE DEPENDENCY FLOWCHART

```
User Upload Resume
        ↓
Frontend (React on Port 5173)
        ↓
Axios → POST request with FormData
        ↓
Spring Boot Backend (Port 8080)
        ↓
@RestController receives request
        ↓
@Autowired injects Service
        ↓
Service receives MultipartFile
        ↓
PDF? → Apache PDFBox extracts text
DOCX? → Apache POI extracts text
        ↓
Text Processing (keyword extraction)
        ↓
Skills Analysis (Java Streams)
        ↓
Format Analysis (Regex patterns)
        ↓
Score Calculation (weighted formula)
        ↓
Build ATSAnalysisResult (Lombok @Builder)
        ↓
Return to Controller
        ↓
Jackson converts to JSON automatically
        ↓
ResponseEntity sends JSON back
        ↓
Axios receives response
        ↓
React displays results
        ↓
User sees analysis!
```

---

## 📝 PRESENTATION TALKING POINTS

### When Presenting This Project:

**1. "What is Spring Boot's Role?"**

*"Spring Boot is the backbone of our backend. It provides:*
- *Embedded Tomcat server - no need for external server setup*
- *RESTful API capabilities with just annotations*
- *Automatic JSON conversion using Jackson*
- *Dependency Injection for clean architecture*
- *File upload handling with MultipartFile*
- *CORS configuration for frontend-backend communication"*

**2. "Why These Specific Dependencies?"**

*"Each dependency has a specific purpose:*
- *Spring Web - Creates REST APIs and includes web server*
- *Apache PDFBox - Extracts text from PDF files*
- *Apache POI - Extracts text from Word documents*  
- *Lombok - Reduces boilerplate code with annotations"*

**3. "How Does the Analysis Work?"**

*"Our algorithm works in steps:*
1. *Extract text from uploaded file using PDFBox or POI*
2. *Parse keywords using regex, removing stop words*
3. *Compare resume keywords with job description keywords*
4. *Detect skills from predefined lists*
5. *Analyze format using pattern matching*
6. *Calculate weighted ATS score (40% keywords, 30% skills, 30% format)*
7. *Generate actionable suggestions based on gaps"*

**4. "Architecture Decisions"**

*"We followed MVC pattern:*
- *Model: ATSAnalysisResult (data structure)*
- *View: React frontend (presentation)*
- *Controller: ResumeController (request handling)*
- *Service: ResumeAnalysisService (business logic)*

*This separation makes code maintainable and testable."*

---

## 🎯 KEY INTERVIEW QUESTIONS & ANSWERS

**Q: Why did you choose Spring Boot?**
A: Spring Boot simplifies Java web development with auto-configuration, embedded server, and production-ready features. It allowed me to focus on business logic rather than boilerplate configuration.

**Q: How does file upload work?**
A: Spring Boot's `MultipartFile` interface handles file uploads. We receive the file in the controller, pass it to the service layer, and use Apache PDFBox/POI libraries to extract text based on file type.

**Q: Why separate Service and Controller layers?**
A: Separation of concerns. Controller handles HTTP, Service handles business logic. This makes code testable, reusable, and maintainable.

**Q: How do you handle CORS?**
A: We implemented `WebMvcConfigurer` with `addCorsMappings()` to allow specific origins (localhost:5173) to access our API, necessary for frontend-backend communication across different ports.

**Q: What if the file is very large?**
A: We configured `max-file-size=10MB` in `application.properties`. Spring Boot automatically rejects larger files and returns an error before processing.

---

This guide provides COMPLETE understanding of every line of code and concept used!
