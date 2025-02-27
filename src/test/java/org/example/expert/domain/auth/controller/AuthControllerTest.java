package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@MockBean(JpaMetamodelMappingContext.class) // <-- EnableJpaAuditing 을 별도 클래스에서 적용했으면 이거 안써도 됨
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    public void 회원가입_성공() throws Exception {
        // given
        SignupRequest request = new SignupRequest("tester@test.com", "Tester123", "user");
        SignupResponse response = new SignupResponse("mockToken");

        given(authService.signup(any(SignupRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bearerToken").value("mockToken"));
    }

    @Test
    public void 유효하지_않은_이메일로_인한_회원가입_실패() throws Exception {
        // given
        SignupRequest request = new SignupRequest("aa.com", "Tester123", "user");

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void 유효하지_않은_비밀번호로_인한_회원가입_실패() throws Exception {
        // given
        SignupRequest request = new SignupRequest("a@a.com", "tester", "user");

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void 모든_필드_누락으로_인한_회원가입_실패() throws Exception {
        // given
        SignupRequest request = new SignupRequest("", "", "");

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void 로그인_성공() throws Exception {
        // given
        SigninRequest request = new SigninRequest("a@a.com", "password");
        SigninResponse response = new SigninResponse("mockToken");

        given(authService.signin(any(SigninRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/signin")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bearerToken").value("mockToken"));
    }

    @Test
    public void 이메일_누락으로_인한_로그인_실패() throws Exception {
        // given
        SigninRequest request = new SigninRequest("", "password");

        // when & then
        mockMvc.perform(post("/auth/signin")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void 비밀번호_누락으로_인한_로그인_실패() throws Exception {
        // given
        SigninRequest request = new SigninRequest("a@a.com", "");

        // when & then
        mockMvc.perform(post("/auth/signin")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void 모든_필드_누락으로_인한_로그인_실패() throws Exception {
        // given
        SigninRequest request = new SigninRequest("", "password");

        // when & then
        mockMvc.perform(post("/auth/signin")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").exists());
    }
}