package com.example.career.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.career.model.Company;
import com.example.career.model.EmploymentType;
import com.example.career.model.Job;
import com.example.career.model.Recruiter;
import com.example.career.model.Role;
import com.example.career.model.Skill;
import com.example.career.model.User;
import com.example.career.repository.CompanyRepository;
import com.example.career.repository.JobRepository;
import com.example.career.repository.RecruiterRepository;
import com.example.career.repository.SkillRepository;
import com.example.career.repository.UserRepository;

@Component
@ConditionalOnProperty(prefix = "app.sample-data", name = "enabled", havingValue = "true")
public class SampleJobDataInitializer implements ApplicationRunner {
    private static final List<SampleJob> SAMPLE_JOBS = List.of(
            new SampleJob("Northstar Digital", "Java Developer", "Build and maintain Java services and REST APIs.",
                    "Bengaluru", EmploymentType.FULL_TIME, 800000, 0, List.of("Java", "Spring Boot", "MySQL", "Git")),
            new SampleJob("Northstar Digital", "Spring Boot Engineer", "Develop secure microservices and production APIs.",
                    "Hyderabad", EmploymentType.FULL_TIME, 1200000, 2, List.of("Java", "Spring Boot", "MySQL", "Docker")),
            new SampleJob("Veridian Systems", "React Frontend Developer", "Build accessible, responsive product experiences.",
                    "Bengaluru", EmploymentType.FULL_TIME, 900000, 1, List.of("React", "JavaScript", "CSS", "TypeScript")),
            new SampleJob("Veridian Systems", "Full Stack Developer", "Deliver features across a React frontend and Java backend.",
                    "Remote", EmploymentType.FULL_TIME, 1400000, 3, List.of("React", "Java", "Spring Boot", "PostgreSQL")),
            new SampleJob("Aster Analytics", "Python Developer", "Build data processing services and internal APIs.",
                    "Pune", EmploymentType.FULL_TIME, 850000, 1, List.of("Python", "Django", "PostgreSQL", "REST APIs")),
            new SampleJob("Aster Analytics", "Data Analyst", "Turn operational data into useful product and business insights.",
                    "Mumbai", EmploymentType.FULL_TIME, 700000, 1, List.of("SQL", "Python", "Power BI", "Excel")),
            new SampleJob("Aster Analytics", "Machine Learning Engineer", "Develop and deploy machine learning pipelines.",
                    "Hyderabad", EmploymentType.FULL_TIME, 1800000, 3, List.of("Python", "Machine Learning", "TensorFlow", "SQL")),
            new SampleJob("Blue Oak Technologies", "Frontend Engineer", "Create performant web interfaces for customer products.",
                    "Chennai", EmploymentType.FULL_TIME, 950000, 2, List.of("React", "TypeScript", "HTML", "CSS")),
            new SampleJob("Blue Oak Technologies", "Backend Engineer", "Design reliable services and data-backed APIs.",
                    "Delhi", EmploymentType.FULL_TIME, 1100000, 2, List.of("Java", "Spring Boot", "MySQL", "Redis")),
            new SampleJob("Blue Oak Technologies", "DevOps Engineer", "Improve deployment automation and cloud reliability.",
                    "Remote", EmploymentType.FULL_TIME, 1600000, 3, List.of("Docker", "Kubernetes", "AWS", "CI/CD")),
            new SampleJob("Orbit Health", "Software Engineer", "Build and support secure healthcare platform features.",
                    "Bengaluru", EmploymentType.FULL_TIME, 1000000, 1, List.of("Java", "React", "REST APIs", "SQL")),
            new SampleJob("Orbit Health", "Data Engineer", "Build dependable data pipelines for analytics products.",
                    "Hyderabad", EmploymentType.FULL_TIME, 1500000, 3, List.of("Python", "SQL", "Apache Spark", "AWS")),
            new SampleJob("Northstar Digital", "QA Automation Engineer", "Create automated test coverage for web services.",
                    "Pune", EmploymentType.FULL_TIME, 900000, 2, List.of("Selenium", "Java", "API Testing", "CI/CD")),
            new SampleJob("Veridian Systems", "Cloud Engineer", "Operate and improve cloud infrastructure and observability.",
                    "Mumbai", EmploymentType.CONTRACT, 1700000, 4, List.of("AWS", "Terraform", "Docker", "Linux")),
            new SampleJob("Aster Analytics", "Product Engineer", "Partner with design and product to ship customer-focused features.",
                    "Remote", EmploymentType.FULL_TIME, 1300000, 2, List.of("React", "TypeScript", "Node.js", "SQL")),
            new SampleJob("Orbit Health", "Machine Learning Engineer", "Ship model-backed features with measurable outcomes.",
                    "Bengaluru", EmploymentType.FULL_TIME, 2000000, 4, List.of("Python", "PyTorch", "Machine Learning", "Docker")),
            new SampleJob("Blue Oak Technologies", "Java Full Stack Developer", "Build end-to-end business applications.",
                    "Chennai", EmploymentType.FULL_TIME, 1250000, 2, List.of("Java", "Spring Boot", "React", "MySQL")),
            new SampleJob("Northstar Digital", "Python Backend Developer", "Build scalable backend services and integrations.",
                    "Delhi", EmploymentType.INTERNSHIP, 500000, 0, List.of("Python", "FastAPI", "PostgreSQL", "Docker")));

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final RecruiterRepository recruiterRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleJobDataInitializer(
            JobRepository jobRepository,
            UserRepository userRepository,
            RecruiterRepository recruiterRepository,
            CompanyRepository companyRepository,
            SkillRepository skillRepository,
            PasswordEncoder passwordEncoder) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.recruiterRepository = recruiterRepository;
        this.companyRepository = companyRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (jobRepository.countByActiveTrue() > 0) {
            return;
        }

        for (SampleJob sample : SAMPLE_JOBS) {
            Recruiter recruiter = getOrCreateRecruiter(sample.companyName());
            Job job = new Job(sample.title(), sample.description(), sample.location(),
                    sample.employmentType(), BigDecimal.valueOf(sample.salary()), sample.experience(),
                    recruiter, LocalDate.now().plusDays(30));
            for (String skillName : sample.skills()) {
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName)));
                job.getSkills().add(skill);
            }
            jobRepository.save(job);
        }
    }

    private Recruiter getOrCreateRecruiter(String companyName) {
        String email = "sample-recruiter-" + companyName.toLowerCase().replaceAll("[^a-z0-9]+", "-")
                + "@career-platform.local";
        return recruiterRepository.findByUserEmailIgnoreCase(email).orElseGet(() -> {
            Company company = companyRepository.findAll().stream()
                    .filter(candidate -> candidate.getName().equals(companyName))
                    .findFirst()
                    .orElseGet(() -> companyRepository.save(new Company(companyName,
                            "Development sample company for local job-search testing.",
                            null,
                            "India")));
            User user = new User("Sample Recruiter", email,
                    passwordEncoder.encode(UUID.randomUUID().toString()), Role.RECRUITER);
            user.setEnabled(false);
            Recruiter recruiter = new Recruiter(user);
            recruiter.setCompany(company);
            userRepository.save(user);
            return recruiterRepository.save(recruiter);
        });
    }

    private record SampleJob(
            String companyName,
            String title,
            String description,
            String location,
            EmploymentType employmentType,
            long salary,
            int experience,
            List<String> skills) {}
}
