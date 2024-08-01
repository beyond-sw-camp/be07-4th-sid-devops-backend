package org.devjeans.sid.domain.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devjeans.sid.domain.member.entity.Member;
import org.devjeans.sid.domain.member.repository.MemberRepository;
import org.devjeans.sid.domain.project.dto.scrap.ScrapResponse;
import org.devjeans.sid.domain.project.entity.Project;
import org.devjeans.sid.domain.project.entity.ProjectScrap;
import org.devjeans.sid.domain.project.repository.ProjectRepository;
import org.devjeans.sid.domain.project.repository.ProjectScrapRepository;
import org.devjeans.sid.global.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.devjeans.sid.global.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.Set;

import static org.devjeans.sid.global.exception.exceptionType.MemberExceptionType.MEMBER_NOT_FOUND;
import static org.devjeans.sid.global.exception.exceptionType.ProjectExceptionType.PROJECT_NOT_FOUND;

@Component
public class ProjectScheduler {
    private static final String PROJECT_SCRAP_COUNT = "project_scrap_count:";
    private static final String MEMBER_SCRAP_LIST = "member_scrap_list:";
    private static final String VIEWS_KEY_PREFIX="project_views:";
    private final SecurityUtil securityUtil;
    private final ProjectRepository projectRepository;
    private final ProjectScrapRepository projectScrapRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final RedisTemplate<String, Object> scrapRedisTemplate;
    @Autowired
    public ProjectScheduler(SecurityUtil securityUtil, @Qualifier("scrapRedisTemplate") RedisTemplate<String, Object> scrapRedisTemplate, ProjectRepository projectRepository, ProjectScrapRepository projectScrapRepository, MemberRepository memberRepository, @Qualifier("viewRedisTemplate")RedisTemplate<String, String> redisTemplate) {
        this.securityUtil = securityUtil;
        this.projectRepository = projectRepository;
        this.projectScrapRepository = projectScrapRepository;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
        this.scrapRedisTemplate =scrapRedisTemplate;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    @Transactional
    public void projectSchedule(){

        Page<Project> projects = projectRepository.findByIsClosed(Pageable.unpaged(),"N");
        for(Project p : projects){
            if(p.getDeadline().isBefore(LocalDateTime.now())){
                p.updateIsClosed("Y");
                projectRepository.save(p);
            }
        }
    }

    @Qualifier("viewRedisTemplate")
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void syncViews(){
        for(Project p : projectRepository.findAll()){
            // 조회수 저장
            String key = VIEWS_KEY_PREFIX+p.getId();
            String value = redisTemplate.opsForValue().get(key);
            Long view = 0L;
            if(value!=null) {
                view = Long.parseLong(value);
                p.setViews(view);
                projectRepository.save(p);
            }
        }
    }


    @Qualifier("scrapRedisTemplate")
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void syncScraps() {
        //set(MEMBER_SCRAP_LIST+memberId).members = project id들
        // projectScrapRepository에 있는지 체크
        // 없다면 생성 후 save
        for(Member member:memberRepository.findAll()){
            String key = MEMBER_SCRAP_LIST+member.getId();
            SetOperations<String,Object> setOperations = scrapRedisTemplate.opsForSet();
            Set<Object> projects = setOperations.members(key); // 한 회원이 scrap한 프로젝트 목록

            if(projects!=null){
                for(Object projectId : projects){
                    Long id = Long.parseLong(projectId.toString());
                    Project project = projectRepository.findById(id).orElseThrow(()->new BaseException(PROJECT_NOT_FOUND));
                    // 해당 projectScrap이 이미 있는지 체크 후 없다면 저장
                    if(projectScrapRepository.findByProjectIdAndMemberId(id, member.getId())==null) {
                        ProjectScrap projectScrap = new ProjectScrap();
                        projectScrap.setProject(project);
                        projectScrap.setMember(member);
                        projectScrapRepository.save(projectScrap);
                    }
                }
            }
        }

        // project count도 복구
        for(Project p : projectRepository.findAll()){
            // scrap 저장
            String projectKey = PROJECT_SCRAP_COUNT + p.getId();
            ValueOperations<String, Object> valueOperations = scrapRedisTemplate.opsForValue();
            Object count = valueOperations.get(projectKey);
            if(count !=null){
                Long views = Long.valueOf(count.toString());
                p.setScrapCount(views);
                projectRepository.save(p);
            }
        }
    }
}
