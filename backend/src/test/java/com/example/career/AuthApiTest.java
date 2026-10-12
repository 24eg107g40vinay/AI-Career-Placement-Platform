package com.example.career;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.career.model.Role;
import com.example.career.model.User;
import com.example.career.repository.UserRepository;
import com.example.career.security.JwtService;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.frontend-url=https://career-placement-demo.vercel.app,http://localhost:5173"
})
@AutoConfigureMockMvc
class AuthApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void exposesOnlyMinimalPublicHealthInformation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void allowsConfiguredFrontendOriginsToCallAuthEndpoints() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://career-placement-demo.vercel.app")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://career-placement-demo.vercel.app"));

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void registersStudentWithHashedPasswordAndReturnsJwt() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jamie Student",
                                  "email": "jamie@example.test",
                                  "password": "SafePassword123",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.email").value("jamie@example.test"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "JAMIE@example.test",
                                  "password": "SafePassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(isEmptyOrNullString())));
    }

    @Test
    void doesNotAllowPublicAdminRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Admin",
                                  "email": "admin@example.test",
                                  "password": "SafePassword123",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Administrator accounts cannot be registered publicly"));
    }

    @Test
    void recruiterAccountsCanOnlyBeCreatedByAdministrators() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unapproved Recruiter",
                                  "email": "unapproved-recruiter@example.test",
                                  "password": "SafePassword123",
                                  "role": "RECRUITER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Recruiter accounts can only be created by an administrator"));

        String studentToken = registerAndGetToken("Student", "student-admin-api@example.test", "STUDENT");
        String recruiterRequest = """
                {
                  "name": "Authorized Recruiter",
                  "email": "authorized-recruiter@example.test",
                  "password": "InitialPassword123"
                }
                """;
        mockMvc.perform(post("/api/admin/recruiters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recruiterRequest))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/admin/recruiters")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recruiterRequest))
                .andExpect(status().isForbidden());

        User administrator = userRepository.save(new User("Authorized Admin", "authorized-admin@example.test",
                passwordEncoder.encode("AdminPassword123"), Role.ADMIN));
        mockMvc.perform(post("/api/admin/recruiters")
                        .header("Authorization", bearer(jwtService.createToken(administrator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recruiterRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("authorized-recruiter@example.test"))
                .andExpect(jsonPath("$.role").value("RECRUITER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "authorized-recruiter@example.test",
                                  "password": "InitialPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("RECRUITER"));
    }

    @Test
    void returnsStructuredUnauthorizedResponseForProtectedRoutes() throws Exception {
        mockMvc.perform(get("/api/private-test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void recruiterStudentWorkflowCreatesJobMatchesSkillsAndPreventsDuplicateApplications() throws Exception {
        User administrator = userRepository.save(new User("Workflow Admin", "workflow-admin@example.test",
                passwordEncoder.encode("AdminPassword123"), Role.ADMIN));
        String administratorToken = jwtService.createToken(administrator);
        mockMvc.perform(post("/api/admin/recruiters")
                        .header("Authorization", bearer(administratorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Recruiter One",
                                  "email": "recruiter@example.test",
                                  "password": "SafePassword123"
                                }
                                """))
                .andExpect(status().isCreated());
        MvcResult recruiterLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "recruiter@example.test",
                                  "password": "SafePassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String recruiterToken = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(recruiterLogin.getResponse().getContentAsString()).get("token").asText();
        mockMvc.perform(put("/api/recruiters/company")
                        .header("Authorization", bearer(recruiterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Example Engineering",
                                  "description": "A software company",
                                  "website": "https://example.test",
                                  "location": "Remote"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Example Engineering"));

        mockMvc.perform(get("/api/recruiters/dashboard")
                        .header("Authorization", bearer(recruiterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeJobs").value(0));

        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", bearer(recruiterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Java Developer",
                                  "description": "Build software services",
                                  "location": "Remote",
                                  "employmentType": "FULL_TIME",
                                  "salary": 80000,
                                  "experience": 1,
                                  "deadline": "2099-12-31",
                                  "skills": ["Java", "Spring Boot", "MySQL"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company").value("Example Engineering"))
                .andReturn();
        long jobId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(jobResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/jobs/search").param("keyword", "  JAVA   DEVELOPER  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Java Developer"));
        mockMvc.perform(get("/api/jobs/search").param("keyword", "Spring Boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Java Developer"));
        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "java")
                        .param("location", "remote")
                        .param("employmentType", "FULL_TIME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Java Developer"));

        String studentToken = registerAndGetToken("Student One", "student@example.test", "STUDENT");
        mockMvc.perform(post("/api/students/profile/skills")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Java"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/students/profile")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "education": "B.Tech Computer Science",
                                  "location": "Bengaluru",
                                  "experienceYears": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experienceYears").value(2));
        mockMvc.perform(get("/api/students/profile")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experienceYears").value(2));

        MvcResult applicationResult = mockMvc.perform(post("/api/applications/" + jobId)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"coverMessage": "I am interested in this role."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andReturn();

        mockMvc.perform(post("/api/applications/" + jobId)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"coverMessage": ""}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/recommendations/jobs")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchPercentage").value(33))
                .andExpect(jsonPath("$[0].matchedSkills[0]").value("Java"))
                .andExpect(jsonPath("$[0].missingSkills.length()").value(2));

        mockMvc.perform(get("/api/applications/job/" + jobId)
                        .header("Authorization", bearer(recruiterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentEmail").value("student@example.test"));

        long applicationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(applicationResult.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(put("/api/applications/" + applicationId + "/status")
                        .header("Authorization", bearer(recruiterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "INTERVIEW"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEW"));
        mockMvc.perform(get("/api/recruiters/dashboard")
                        .header("Authorization", bearer(recruiterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interviews").value(1));

        mockMvc.perform(get("/api/admin/applications")
                        .header("Authorization", bearer(administratorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(applicationId));
        mockMvc.perform(put("/api/applications/" + applicationId + "/status")
                        .header("Authorization", bearer(administratorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "SHORTLISTED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "What should I learn for Java development?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedByProvider").value(false))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Spring Boot")))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("four-step plan")));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "backend engineer"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedByProvider").value(false))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("REST APIs")))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("database design")));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "How can I prepare for a behavioral interview?",
                                  "history": [
                                    {"role": "USER", "content": "I am applying for an internship"},
                                    {"role": "ASSISTANT", "content": "What role are you targeting?"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("STAR stories")))
                .andExpect(jsonPath("$.generatedByProvider").value(false));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "इस वेबसाइट पर नौकरी के लिए आवेदन कैसे करूँ?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Find Jobs")))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("आवेदन")))
                .andExpect(jsonPath("$.generatedByProvider").value(false));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "¿Cómo solicito un empleo en esta plataforma?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Applications")))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("solicitar")))
                .andExpect(jsonPath("$.generatedByProvider").value(false));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "मैं इंटरव्यू की तैयारी कैसे करूँ?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("इंटरव्यू")))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("STAR")))
                .andExpect(jsonPath("$.generatedByProvider").value(false));

        mockMvc.perform(get("/api/students/profile")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Student One"));

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCanUploadDownloadAndDeletePrivatePdfResume() throws Exception {
        String studentToken = registerAndGetToken("Resume Student", "resume@example.test", "STUDENT");
        String otherStudentToken = registerAndGetToken("Other Student", "other-resume@example.test", "STUDENT");
        MockMultipartFile invalid = new MockMultipartFile(
                "file", "resume.txt", MediaType.TEXT_PLAIN_VALUE, "not a PDF".getBytes());
        mockMvc.perform(multipart("/api/students/resumes")
                        .file(invalid)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isBadRequest());

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "resume.pdf", MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4\nNorthstar resume test\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        MvcResult uploaded = mockMvc.perform(multipart("/api/students/resumes")
                        .file(pdf)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("resume.pdf"))
                .andReturn();
        long resumeId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(uploaded.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/students/resumes")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(resumeId));
        mockMvc.perform(get("/api/students/resumes/" + resumeId + "/download")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_PDF));
        mockMvc.perform(get("/api/students/resumes/" + resumeId + "/download")
                        .header("Authorization", bearer(otherStudentToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/students/resumes/" + resumeId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isNoContent());
    }

    private String registerAndGetToken(String name, String email, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "SafePassword123",
                                  "role": "%s"
                                }
                                """.formatted(name, email, role)))
                .andExpect(status().isCreated())
                .andReturn();
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
