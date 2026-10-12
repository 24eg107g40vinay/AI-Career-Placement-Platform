package com.example.career.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.career.model.EmploymentType;
import com.example.career.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);
    java.util.Optional<Job> findByIdAndRecruiterUserEmailIgnoreCase(Long id, String email);
    List<Job> findByRecruiterUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    long countByRecruiterUserEmailIgnoreCaseAndActiveTrue(String email);
    long countByActiveTrue();

    @Query("""
            select distinct job from Job job
            left join job.skills skill
            where job.active = true
              and (:keyword is null or lower(job.title) like lower(concat('%', :keyword, '%'))
                   or lower(job.description) like lower(concat('%', :keyword, '%')))
              and (:location is null or lower(job.location) like lower(concat('%', :location, '%')))
              and (:employmentType is null or job.employmentType = :employmentType)
              and (:skillName is null or lower(skill.name) = lower(:skillName))
            order by job.createdAt desc
            """)
    List<Job> searchActiveJobs(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("employmentType") EmploymentType employmentType,
            @Param("skillName") String skillName);

    @Query(
            value = """
                    select job from Job job
                    left join job.recruiter recruiter
                    left join recruiter.company company
                    where job.active = true
                      and (:keyword is null or lower(job.title) like lower(concat('%', :keyword, '%'))
                           or lower(job.description) like lower(concat('%', :keyword, '%'))
                           or lower(company.name) like lower(concat('%', :keyword, '%'))
                           or exists (select requiredSkill.id from Job skillJob
                                      join skillJob.skills requiredSkill
                                      where skillJob.id = job.id
                                        and (lower(requiredSkill.name) = replace(:keyword, '%', ' ')
                                             or lower(requiredSkill.name) like concat(replace(:keyword, '%', ' '), ' %')
                                             or lower(requiredSkill.name) like concat('% ', replace(:keyword, '%', ' '), ' %')
                                             or lower(requiredSkill.name) like concat('% ', replace(:keyword, '%', ' ')))))
                      and (:location is null or lower(job.location) like lower(concat('%', :location, '%')))
                      and (:companyName is null or lower(company.name) like lower(concat('%', :companyName, '%')))
                      and (:employmentType is null or job.employmentType = :employmentType)
                      and (:skillName is null or exists (select matchingSkill.id from Job skillFilterJob
                                      join skillFilterJob.skills matchingSkill
                                      where skillFilterJob.id = job.id
                                        and lower(matchingSkill.name) like lower(concat('%', :skillName, '%'))))
                      and (:minExperience is null or job.experience >= :minExperience)
                      and (:maxExperience is null or job.experience <= :maxExperience)
                      and (:minSalary is null or job.salary >= :minSalary)
                      and (:maxSalary is null or job.salary <= :maxSalary)
                      and (:postedAfter is null or job.createdAt >= :postedAfter)
                    order by
                      case when :sortBy = 'RELEVANCE' and :keyword is not null
                                and lower(job.title) = replace(:keyword, '%', ' ') then 0
                           when :sortBy = 'RELEVANCE' and :keyword is not null
                                and lower(job.title) like lower(concat(replace(:keyword, '%', ' '), '%')) then 1
                           when :sortBy = 'RELEVANCE' and :keyword is not null
                                and lower(job.title) like lower(concat('%', :keyword, '%')) then 2
                           when :sortBy = 'RELEVANCE' and :keyword is not null
                                and exists (select matchingSkill.id from Job skillJob
                                            join skillJob.skills matchingSkill
                                            where skillJob.id = job.id
                                              and lower(matchingSkill.name) = replace(:keyword, '%', ' ')) then 3
                           when :sortBy = 'RELEVANCE' and :keyword is not null
                                and exists (select matchingSkill.id from Job skillJob
                                            join skillJob.skills matchingSkill
                                            where skillJob.id = job.id
                                              and (lower(matchingSkill.name) = replace(:keyword, '%', ' ')
                                                   or lower(matchingSkill.name) like concat(replace(:keyword, '%', ' '), ' %')
                                                   or lower(matchingSkill.name) like concat('% ', replace(:keyword, '%', ' '), ' %')
                                                   or lower(matchingSkill.name) like concat('% ', replace(:keyword, '%', ' ')))) then 4
                           when :sortBy = 'RELEVANCE' and :keyword is not null
                                and lower(company.name) like lower(concat('%', :keyword, '%')) then 5
                           when :sortBy = 'RELEVANCE' and :keyword is not null
                                and lower(job.description) like lower(concat('%', :keyword, '%')) then 6
                           when :sortBy = 'RELEVANCE' then 7 else 0 end asc,
                      case when :sortBy = 'SALARY_HIGH' then job.salary end desc,
                      case when :sortBy = 'SALARY_LOW' then job.salary end asc,
                      job.createdAt desc
                    """,
            countQuery = """
                    select count(job) from Job job
                    left join job.recruiter recruiter
                    left join recruiter.company company
                    where job.active = true
                      and (:keyword is null or lower(job.title) like lower(concat('%', :keyword, '%'))
                           or lower(job.description) like lower(concat('%', :keyword, '%'))
                           or lower(company.name) like lower(concat('%', :keyword, '%'))
                           or exists (select requiredSkill.id from Job skillJob
                                      join skillJob.skills requiredSkill
                                      where skillJob.id = job.id
                                        and (lower(requiredSkill.name) = replace(:keyword, '%', ' ')
                                             or lower(requiredSkill.name) like concat(replace(:keyword, '%', ' '), ' %')
                                             or lower(requiredSkill.name) like concat('% ', replace(:keyword, '%', ' '), ' %')
                                             or lower(requiredSkill.name) like concat('% ', replace(:keyword, '%', ' ')))))
                      and (:location is null or lower(job.location) like lower(concat('%', :location, '%')))
                      and (:companyName is null or lower(company.name) like lower(concat('%', :companyName, '%')))
                      and (:employmentType is null or job.employmentType = :employmentType)
                      and (:skillName is null or exists (select matchingSkill.id from Job skillFilterJob
                                      join skillFilterJob.skills matchingSkill
                                      where skillFilterJob.id = job.id
                                        and lower(matchingSkill.name) like lower(concat('%', :skillName, '%'))))
                      and (:minExperience is null or job.experience >= :minExperience)
                      and (:maxExperience is null or job.experience <= :maxExperience)
                      and (:minSalary is null or job.salary >= :minSalary)
                      and (:maxSalary is null or job.salary <= :maxSalary)
                      and (:postedAfter is null or job.createdAt >= :postedAfter)
                    """)
    Page<Job> searchActiveJobs(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("companyName") String companyName,
            @Param("employmentType") EmploymentType employmentType,
            @Param("skillName") String skillName,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            @Param("minSalary") BigDecimal minSalary,
            @Param("maxSalary") BigDecimal maxSalary,
            @Param("postedAfter") Instant postedAfter,
            @Param("sortBy") String sortBy,
            Pageable pageable);
}
