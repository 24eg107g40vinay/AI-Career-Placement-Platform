package com.example.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.career.model.Application;
import com.example.career.model.Company;
import com.example.career.model.EmploymentType;
import com.example.career.model.Job;
import com.example.career.model.Recruiter;
import com.example.career.model.Role;
import com.example.career.model.Skill;
import com.example.career.model.Student;
import com.example.career.model.User;
import com.example.career.repository.ApplicationRepository;
import com.example.career.repository.JobRepository;
import com.example.career.repository.StudentRepository;
import com.example.career.repository.UserRepository;

@DataJpaTest
class CareerDataModelTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void persistsUserAndStudentProfileWithUniqueEmailLookup() {
        User user = userRepository.save(new User("Sam Student", "sam@example.test", "hashed-password", Role.STUDENT));
        Student student = studentRepository.save(new Student(user));
        entityManager.flush();
        entityManager.clear();

        Optional<User> storedUser = userRepository.findByEmailIgnoreCase("SAM@example.test");
        assertThat(storedUser).isPresent();
        assertThat(studentRepository.findByUserId(user.getId())).isPresent();
        assertThat(student.getId()).isNotNull();
    }

    @Test
    void preventsDuplicateApplicationsForTheSameStudentAndJob() {
        User studentUser = entityManager.persist(new User("Sam Student", "sam@example.test", "hashed", Role.STUDENT));
        Student student = entityManager.persist(new Student(studentUser));
        User recruiterUser = entityManager.persist(new User("Rae Recruiter", "rae@example.test", "hashed", Role.RECRUITER));
        Recruiter recruiter = entityManager.persist(new Recruiter(recruiterUser));
        Company company = entityManager.persist(new Company("Example Co", null, null, "Remote"));
        recruiter.setCompany(company);
        Job job = entityManager.persist(new Job(
                "Junior Engineer", "Build useful software", "Remote",
                com.example.career.model.EmploymentType.FULL_TIME, null, 0, recruiter, null));
        entityManager.flush();

        applicationRepository.save(new Application(student, job));
        entityManager.flush();

        assertThatThrownBy(() -> {
            applicationRepository.save(new Application(student, job));
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void searchesActiveJobsByKeywordLocationTypeAndSkill() {
        User recruiterUser = entityManager.persist(new User("Rae Recruiter", "rae@example.test", "hashed", Role.RECRUITER));
        Recruiter recruiter = entityManager.persist(new Recruiter(recruiterUser));
        Company company = entityManager.persist(new Company("Example Co", null, null, "Remote"));
        recruiter.setCompany(company);
        Skill skill = entityManager.persist(new Skill("Java"));
        Job job = new Job("Junior Java Engineer", "Build APIs", "Remote",
                EmploymentType.FULL_TIME, new BigDecimal("70000"), 1, recruiter, LocalDate.now().plusDays(30));
        job.getSkills().add(skill);
        entityManager.persist(job);
        entityManager.flush();

        List<Job> results = jobRepository.searchActiveJobs("engineer", "remote",
                EmploymentType.FULL_TIME, "java");

        assertThat(results).extracting(Job::getTitle).containsExactly("Junior Java Engineer");
        assertThat(jobRepository.searchActiveJobs(null, null, null, null))
                .extracting(Job::getTitle)
                .containsExactly("Junior Java Engineer");
    }

    @Test
    void searchesPagedJobsAcrossSkillsAndCombinesFilters() {
        User recruiterUser = entityManager.persist(new User("Rae Recruiter", "rae@example.test", "hashed", Role.RECRUITER));
        Recruiter recruiter = entityManager.persist(new Recruiter(recruiterUser));
        Company company = entityManager.persist(new Company("Example Co", null, null, "Hyderabad"));
        recruiter.setCompany(company);
        Skill springBoot = entityManager.persist(new Skill("Spring Boot"));
        Skill java = entityManager.persist(new Skill("Java"));

        Job matchingJob = new Job("Backend Engineer", "Build APIs", "Hyderabad",
                EmploymentType.FULL_TIME, new BigDecimal("90000"), 2, recruiter, null);
        matchingJob.getSkills().add(springBoot);
        matchingJob.getSkills().add(java);
        entityManager.persist(matchingJob);

        Job otherLocation = new Job("Spring Developer", "Build services", "Bengaluru",
                EmploymentType.FULL_TIME, new BigDecimal("85000"), 2, recruiter, null);
        otherLocation.getSkills().add(springBoot);
        entityManager.persist(otherLocation);
        entityManager.flush();

        Page<Job> results = jobRepository.searchActiveJobs(
                "spring%boot",
                "hyderabad",
                "example",
                EmploymentType.FULL_TIME,
                "boot",
                1,
                3,
                new BigDecimal("80000"),
                new BigDecimal("100000"),
                null,
                "RELEVANCE",
                PageRequest.of(0, 1));

        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent()).extracting(Job::getTitle).containsExactly("Backend Engineer");
        assertThat(results.getTotalPages()).isEqualTo(1);
    }

    @Test
    void ranksKeywordMatchesByTitleSkillCompanyThenDescription() {
        User recruiterUser = entityManager.persist(new User("Rae Recruiter", "rae@example.test", "hashed", Role.RECRUITER));
        Recruiter recruiter = entityManager.persist(new Recruiter(recruiterUser));
        Company otherCompany = entityManager.persist(new Company("Example Co", null, null, "Remote"));
        recruiter.setCompany(otherCompany);

        User keywordRecruiterUser = entityManager.persist(
                new User("Jay Recruiter", "jay@example.test", "hashed", Role.RECRUITER));
        Recruiter keywordRecruiter = entityManager.persist(new Recruiter(keywordRecruiterUser));
        Company keywordCompany = entityManager.persist(new Company("Java Systems", null, null, "Remote"));
        keywordRecruiter.setCompany(keywordCompany);

        Job descriptionMatch = new Job("Product Analyst", "Uses Java for reporting", "Remote",
                EmploymentType.FULL_TIME, null, 0, recruiter, null);
        entityManager.persist(descriptionMatch);

        Job companyMatch = new Job("Platform Engineer", "Build platform services", "Remote",
                EmploymentType.FULL_TIME, null, 0, keywordRecruiter, null);
        entityManager.persist(companyMatch);

        Skill java = entityManager.persist(new Skill("Java"));
        Job skillMatch = new Job("Backend Engineer", "Build APIs", "Remote",
                EmploymentType.FULL_TIME, null, 0, recruiter, null);
        skillMatch.getSkills().add(java);
        entityManager.persist(skillMatch);

        Skill javaScript = entityManager.persist(new Skill("JavaScript"));
        Job unrelatedPartialSkillMatch = new Job("Frontend Engineer", "Build web interfaces", "Remote",
                EmploymentType.FULL_TIME, null, 0, recruiter, null);
        unrelatedPartialSkillMatch.getSkills().add(javaScript);
        entityManager.persist(unrelatedPartialSkillMatch);

        Skill javaEe = entityManager.persist(new Skill("Java EE"));
        Job partialSkillMatch = new Job("Enterprise Engineer", "Build enterprise services", "Remote",
                EmploymentType.FULL_TIME, null, 0, recruiter, null);
        partialSkillMatch.getSkills().add(javaEe);
        entityManager.persist(partialSkillMatch);

        Job titleMatch = new Job("Java Developer", "Build APIs", "Remote",
                EmploymentType.FULL_TIME, null, 0, recruiter, null);
        entityManager.persist(titleMatch);
        entityManager.flush();

        Page<Job> results = jobRepository.searchActiveJobs(
                "java", null, null, null, null, null, null, null, null, null,
                "RELEVANCE", PageRequest.of(0, 10));

        assertThat(results.getContent()).extracting(Job::getTitle).containsExactly(
                "Java Developer", "Backend Engineer", "Enterprise Engineer", "Platform Engineer", "Product Analyst");

        Page<Job> exactPhraseResults = jobRepository.searchActiveJobs(
                "java%developer", null, null, null, null, null, null, null, null, null,
                "RELEVANCE", PageRequest.of(0, 10));
        assertThat(exactPhraseResults.getContent()).extracting(Job::getTitle).containsExactly("Java Developer");
    }
}
