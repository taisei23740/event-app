package event_app.controller;

import event_app.repository.UserRepository;
import event_app.service.EventApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import event_app.ApplyEventResult;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EventApplicationService eventApplicationService;


    @Test
    void 未ログインの場合はログイン画面にリダイレクトする() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/event")
                .param("eventId", "1")
        )
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .status().is3xxRedirection()
        )
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .redirectedUrl("/login")
        );
    }

    @Test
    void ログイン済みの場合は申し込み処理が呼ばれる() throws Exception {
        when(eventApplicationService.applyEvent(1, 100))
            .thenReturn(ApplyEventResult.SUCCESS);

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/event")
                .param("eventId", "1")
                .sessionAttr("userId", 100)
        )
        .andExpect(status().isOk())
        .andExpect(view().name("application-completed"));

        verify(eventApplicationService).applyEvent(1, 100);
    
    }
}