package org.devjeans.sid.domain.launchedProject.controller;

import org.devjeans.sid.domain.launchedProject.dto.LaunchProjectDTO.BasicInfoLaunchedProjectResponse;
import org.devjeans.sid.domain.launchedProject.dto.LaunchProjectDTO.ListLaunchedProjectResponse;
import org.devjeans.sid.domain.launchedProject.dto.LaunchProjectDTO.SaveLaunchedProjectRequest;
import org.devjeans.sid.domain.launchedProject.dto.LaunchProjectDTO.UpdateLaunchedProjectRequest;
import org.devjeans.sid.domain.launchedProject.dto.LaunchedProjectMemberDTO.LaunchedProjectMemberResponse;
import org.devjeans.sid.domain.launchedProject.dto.LaunchedProjectScrapDTO.LaunchedProjectScrapResponse;
import org.devjeans.sid.domain.launchedProject.dto.LaunchedProjectTechStackDTO.LaunchedProjectTechStackResponse;
import org.devjeans.sid.domain.launchedProject.entity.LaunchedProject;
import org.devjeans.sid.domain.launchedProject.service.LaunchedProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/launched-project")
public class LaunchedProjectController {

    private final LaunchedProjectService launchedProjectService;

    @Autowired
    public LaunchedProjectController(LaunchedProjectService launchedProjectService){
        this.launchedProjectService = launchedProjectService;
    }

    // 완성된 프로젝트 글 등록
    @PostMapping("/register")
    public ResponseEntity<Long> register(@RequestBody SaveLaunchedProjectRequest saveRequest){
        LaunchedProject launchedProject = launchedProjectService.register(saveRequest);
        return ResponseEntity.ok(launchedProject.getId()); // 등록된 글 id 반환
    }

    // 완성된 프로젝트 글 수정
    @PostMapping("/update/{launchedProjectId}")
    public ResponseEntity<Object> update(@PathVariable Long launchedProjectId,
                                         @RequestBody UpdateLaunchedProjectRequest updateRequest){
        LaunchedProject launchedProject = launchedProjectService.update(launchedProjectId, updateRequest);
        return ResponseEntity.ok(launchedProject.getId()); // 수정된 글 id 반환
    }

    @GetMapping("/is-scrapped/{id}")
    public ResponseEntity<Boolean> isScrapped(@PathVariable Long id){
        Boolean isScrapped = launchedProjectService.isScrapped(id);
        return ResponseEntity.ok(isScrapped);
    }

    @PostMapping("/scrap/{id}")
    public ResponseEntity<LaunchedProjectScrapResponse> addScrap(@PathVariable Long id){
        LaunchedProjectScrapResponse scrapResponse = launchedProjectService.addScrap(id);
        return ResponseEntity.ok(scrapResponse);
    }

    @DeleteMapping("/scrap/{id}")
    public ResponseEntity<LaunchedProjectScrapResponse> removeScrap(@PathVariable Long id){
        LaunchedProjectScrapResponse scrapResponse = launchedProjectService.removeScrap(id);
        return ResponseEntity.ok(scrapResponse);
    }

    // Launched-Project id로 Launched-Project 기본정보 조회 (+ 조회수, 스크랩 수)
    @GetMapping("/detail/{id}/basic-info")
    public ResponseEntity<BasicInfoLaunchedProjectResponse> getDetailBasicInfo(@PathVariable Long id) {
        BasicInfoLaunchedProjectResponse basicInfo = launchedProjectService.getBasicInfo(id);
        return ResponseEntity.ok(basicInfo);
    }

    // Launched-Project id로 프로젝트에 사용된 기술스택(TechStack)리스트 조회
    @GetMapping("/detail/{id}/tech-stacks")
    public ResponseEntity<List<LaunchedProjectTechStackResponse>> getTechStacks(@PathVariable Long id){
        List<LaunchedProjectTechStackResponse> stacks = launchedProjectService.getTechStacks(id);
        return ResponseEntity.ok(stacks);
    }

    // Launched-Project id로 프로젝트에 참여한 회원(LaunchedProjectMember)리스트 조회
    @GetMapping("/detail/{id}/members")
    public ResponseEntity<List<LaunchedProjectMemberResponse>> getMembers(@PathVariable Long id){
        List<LaunchedProjectMemberResponse> members = launchedProjectService.getProjectMembers(id);
        return ResponseEntity.ok(members);
    }

//    @GetMapping("/detail/{id}")

//    // Launched-Project id로 해당 글에 눌린 scrap(사이다) 조회 => 기능삭제
//    @GetMapping("/detail/{id}/scraps")
//    public ResponseEntity<List<LaunchedProjectScrapResponse>> getScraps(@PathVariable Long id){
//        List<LaunchedProjectScrapResponse> scraps = launchedProjectService.getScraps(id);
//        return ResponseEntity.ok(scraps);
//    }

    // Launched-Project의 전체 리스트(페이지) 조회
    @GetMapping("/list")
    public ResponseEntity<List<ListLaunchedProjectResponse>> getList(
            @RequestParam(defaultValue = "recent") String sorted) {

        List<ListLaunchedProjectResponse> launchedProjectList = launchedProjectService.getList(sorted);
        return ResponseEntity.ok(launchedProjectList);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String message = launchedProjectService.delete(id);
        return ResponseEntity.ok(message);
    }

}
