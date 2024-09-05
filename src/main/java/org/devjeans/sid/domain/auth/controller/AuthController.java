package org.devjeans.sid.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devjeans.sid.domain.auth.JwtTokenProvider;
import org.devjeans.sid.domain.auth.dto.KakaoProfileResDto;
import org.devjeans.sid.domain.auth.dto.CommonResDto;
import org.devjeans.sid.domain.auth.dto.LoginReqDto;
import org.devjeans.sid.domain.auth.entity.KakaoProfile;
import org.devjeans.sid.domain.auth.entity.OAuthToken;
import org.devjeans.sid.domain.auth.service.AuthService;
import org.devjeans.sid.domain.member.dto.RegisterMemberRequest;
import org.devjeans.sid.domain.auth.entity.KakaoRedirect;
import org.devjeans.sid.domain.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
// RequiredArgsConstructor : AutoWired 생성자 주입이 딱히 필요없고
// final 이 붙은 bean객체들을 자동으로 주입해준다
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {
    @Value("${jwt.secretKey}")
    private String secretKey;

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(KakaoRedirect kakaoRedirect) throws JsonProcessingException {
        KakaoProfile kakaoProfile = authService.login(kakaoRedirect);

//            가입자 or 비가입자 체크해서 처리
        Member originMember = authService.getMemberByKakaoId(kakaoProfile.getId());
//        TODO: 탈퇴한 회원일 경우 처리해야함
//        System.out.println(originMember);
        if(originMember == null) {
//           신규 회원일경우 errorResponse에 소셜id를 담아 예외를 프론트로 던지기
//            프론트는 예외일경우 회원가입 화면으로 이동하여 회원가입 정보와 소셜id를 담아 다시 회원가입 요청
            CommonResDto commonResDto = new CommonResDto(HttpStatus.UNAUTHORIZED,"기존 회원이 아닙니다. 회원가입을 진행해주세요.",new KakaoProfileResDto(kakaoProfile.getId(),kakaoProfile.getKakao_account().email));
            return new ResponseEntity<>(commonResDto,HttpStatus.UNAUTHORIZED);
        }
        String jwtToken = jwtTokenProvider.createToken(String.valueOf(originMember.getId()),originMember.getRole().toString());
        Map<String,Object> loginInfo = new HashMap<>();
        loginInfo.put("id",originMember.getId());
        loginInfo.put("token",jwtToken);
//            로그인 처리
        return new ResponseEntity<>(loginInfo,HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerMember(@RequestBody RegisterMemberRequest dto) {
        authService.registerMember(dto);
        return new ResponseEntity<>("register success!!", HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMember() {
//        String tmp = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = authService.delete();
        return new ResponseEntity<>(member.getDeletedAt().toString(), HttpStatus.OK);
    }

    // 클라이언트에서 토큰을 받아 로그인 처리
    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto dto) throws JsonProcessingException {
        OAuthToken oAuthToken = authService.getAccessToken(dto.getCode());
        KakaoProfile kakaoProfile = authService.getKakaoProfile(oAuthToken.getAccess_token());

        Member originMember = authService.getMemberByKakaoId(kakaoProfile.getId());
//        TODO: 탈퇴한 회원일 경우 처리해야함
        if(originMember == null) {
//           신규 회원일경우 errorResponse에 소셜id를 담아 예외를 프론트로 던지기
//            프론트는 예외일경우 회원가입 화면으로 이동하여 회원가입 정보와 소셜id를 담아 다시 회원가입 요청
            CommonResDto commonResDto = new CommonResDto(HttpStatus.UNAUTHORIZED,"기존 회원이 아닙니다. 회원가입을 진행해주세요.",new KakaoProfileResDto(kakaoProfile.getId(),kakaoProfile.getKakao_account().email));
            return new ResponseEntity<>(commonResDto,HttpStatus.UNAUTHORIZED);
        }
        String jwtToken = jwtTokenProvider.createToken(String.valueOf(originMember.getId()),originMember.getRole().toString());
        Map<String,Object> loginInfo = new HashMap<>();
        loginInfo.put("id",originMember.getId());
        loginInfo.put("token",jwtToken);
//            로그인 처리
        return new ResponseEntity<>(loginInfo,HttpStatus.OK);
    }

    //== TODO: 삭제 ==//
    @GetMapping("/memberId/{memberId}")
    public String getToken(@PathVariable("memberId") Long memberId) {
        return jwtTokenProvider.createToken(String.valueOf(memberId), "USER");
    }

}
