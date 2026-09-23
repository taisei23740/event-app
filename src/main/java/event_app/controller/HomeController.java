package event_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import event_app.repository.UserRepository;
import event_app.service.EventApplicationService;
import jakarta.servlet.http.Cookie;
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
        HttpSession session) {

        Integer userId = userRepository.findUserId(email, password);

        if (userId != null) {
            session.setAttribute("userId", userId);
            System.out.println("ログイン成功");
        }
        return "login";
    }

    @GetMapping("/event")
    public String event() {
        return "event";
    }

    @PostMapping("/event")
    public String eventPost(@RequestParam int eventId, HttpServletRequest request, HttpSession session, Model model) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            model.addAttribute("errorMessage","Cookieが見つかりません");
            return "event-error";
        }

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            model.addAttribute("errorMessage","ユーザーIDが見つかりません");
            return "event-error";
        }

        String errorMessage = eventApplicationService.checkEvent(eventId);
        
        if (errorMessage != null) {
            model.addAttribute("errorMessage",errorMessage);
            return "event-error";
        }

        errorMessage = eventApplicationService.alreadyApplied(eventId,userId);

        if (errorMessage != null) {
            model.addAttribute("errorMessage",errorMessage);
            return "event-error";
        }

        errorMessage = eventApplicationService.capaOver(eventId);

        if (errorMessage != null) {
            model.addAttribute("errorMessage",errorMessage);
            return "event-error";
        }

        errorMessage = eventApplicationService.checkApplicationDeadline(eventId);

        if (errorMessage != null) {
            model.addAttribute("errorMessage",errorMessage);
            return "event-error";
        }

        eventApplicationService.apply(eventId, userId);
        System.out.println("申し込み完了");

        return "application-completed";
    }
}