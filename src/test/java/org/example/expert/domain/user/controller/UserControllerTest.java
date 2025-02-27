package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserReadService;
import org.example.expert.domain.user.service.UserWriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
class UserControllerTest {

    @MockBean
    private UserRepository repository;

    @MockBean
    private UserWriteService userWriteService;

    @MockBean
    private UserReadService userReadService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void 유저_단건_조회에_성공한다() throws Exception {
        // given
        Long userId = 1L;
        User mockUser = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", userId);
        UserResponse response = new UserResponse(userId, "a@a.com");

        given(userReadService.getUserResponse(userId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/users/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("a@a.com"));
    }

    @Test
    public void userId를_찾지_못해_단건_조회에_실패한다() throws Exception {
        // given
        given(userReadService.getUserResponse(1L)).willThrow(new InvalidRequestException("유저를 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/users/{userId}", 1L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."));
    }

//    @Test
//    public void 비밀번호_교체에_성공한다() throws Exception {
//        // given
//        User user = new User("a@a.com", "password", UserRole.USER);
//        ReflectionTestUtils.setField(user, "id", 1L);
//        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "newPassword");
//
//        doNothing().when(userWriteService).changePassword(1L, request);
//
//        // when & then
//        mockMvc.perform(put("/users")
//                .contentType(APPLICATION_JSON)
//                .content(new ObjectMapper().writeValueAsString(request)))
//            .andExpect(status().isOk());
//    }

}