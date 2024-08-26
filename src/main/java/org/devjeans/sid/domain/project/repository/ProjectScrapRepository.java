package org.devjeans.sid.domain.project.repository;

import org.devjeans.sid.domain.member.entity.Member;
import org.devjeans.sid.domain.project.entity.Project;
import org.devjeans.sid.domain.project.entity.ProjectScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.devjeans.sid.domain.projectScrap.entity.ProjectScrap;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProjectScrapRepository extends JpaRepository<ProjectScrap,Long> {
    Page<ProjectScrap> findByProjectId(Long projectId,Pageable pageable);
    Page<ProjectScrap> findByMemberId(Long memberId,Pageable pageable);
    ProjectScrap findByProjectIdAndMemberId(Long projectId, Long memberId);
    Boolean existsByProjectAndMember(Project project, Member member);
    @Query("SELECT COUNT(ps) FROM ProjectScrap ps WHERE ps.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

}
