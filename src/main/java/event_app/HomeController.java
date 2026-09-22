package event_app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;


@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private  final EventApplicationRepository eventApplicationRepository;

    public HomeController(UserRepository userRepository,EventRepository eventRepository,EventApplicationRepository eventApplicationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventApplicationRepository = eventApplicationRepository;
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

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("JSESSIONID")) {
                String sessionId = cookie.getValue();

                System.out.println("SessionID:" + sessionId);
                System.out.println("UserID:" + userId);
            }
        }

        boolean exists = eventRepository.existsByEventId(eventId);
        
        if (!exists) {
            model.addAttribute("errorMessage","イベントが見つかりません");
            return "event-error";
        }

        boolean alreadyApplied = eventApplicationRepository.existsByEventIdAndUserId(eventId,userId);

        if (alreadyApplied) {
            model.addAttribute("errorMessage","既に申し込み済みです");
            return "event-error";
        }

        boolean full_capacity = eventApplicationRepository.isFull(eventId);

        if (full_capacity) {
            model.addAttribute("errorMessage","定員オーバーです");
            return "event-error";
        }

        boolean registrationOpen = eventRepository.isRegistrationOpen(eventId);

        if (!registrationOpen) {
            model.addAttribute("errorMessage","応募期限を過ぎています");
            return "event-error";
        }

        eventApplicationRepository.add_apply(eventId, userId);
        System.out.println("申し込み完了");

        return "application-completed";
    }
}