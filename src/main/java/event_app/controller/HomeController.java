package event_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import event_app.ApplyEventResult;
import event_app.repository.UserRepository;
import event_app.service.EventApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;


@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final EventApplicationService eventApplicationService;

    public HomeController(UserRepository userRepository, EventApplicationService eventApplicationService) {
        this.userRepository = userRepository;
        this.eventApplicationService = eventApplicationService;
    }   

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(
        @RequestParam String email,
        @RequestParam String password,
        HttpSession session,
        Model model,
        HttpServletRequest request) {

        Integer userId = userRepository.findUserId(email, password);

        if (userId != null) {
            request.changeSessionId();
            session.setAttribute("userId", userId);
            System.out.println("ログイン成功");
            return "redirect:/event";
        } else {
            model.addAttribute("errorMessage","メールアドレスまたはパスワードが違います");
            return "login-error";
        }
    }

    @GetMapping("/event")
    public String event() {
        return "event";
    }

    @PostMapping("/event")
    public String eventPost(@RequestParam int eventId, HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        ApplyEventResult result = eventApplicationService.applyEvent(eventId,userId);
        
        if (result == ApplyEventResult.SUCCESS) {
            return "application-completed";
        }

        String errorMessage = switch (result) {
            case EVENT_NOT_FOUND -> "指定されたイベントが見つかりませんでした。イベント一覧から選び直してください";
            case ALREADY_APPLIED -> "既に申し込み済みです";
            case FULL -> "定員オーバーです";
            case CLOSED -> "申し込み期限を過ぎています";
            case SUCCESS -> throw new IllegalStateException("成功は先に処理する");
        };

        model.addAttribute("errorMessage",errorMessage);
        return "event-error";
    }
}