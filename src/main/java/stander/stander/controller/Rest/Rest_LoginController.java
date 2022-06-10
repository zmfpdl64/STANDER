package stander.stander.controller.Rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import stander.stander.model.Entity.Member;
import stander.stander.model.Form.LoginForm;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;

@RestController
public class Rest_LoginController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private SeatService seatService;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${file.dir}")
    private String fileDir;

    @ResponseBody
    @PostMapping("/rest_login")
    public Member rest_login(@RequestParam("username")String username, @RequestParam("password") String password) {
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername(username);
        loginForm.setPassword(password);
        Member member = memberService.login(loginForm);
        return member;
    }

    @ResponseBody
    @PostMapping("/rest_join")
    public String rest_join(@RequestParam("name") String name,
                            @RequestParam("username") String username,
                            @RequestParam("password") String password,
                            @RequestParam("email") String email) {
        try {
            Member member;
            member = set_join(name, username, password, email);
            memberService.join(member);
            return "ok";
        }catch(Exception e){
            return null;
        }

    }

    private Member set_join(String name, String username, String password, String email) {
        Member member = new Member();
        member.setName(name);
        member.setUsername(username);
        member.setPassword(password);
        member.setEmail(email);
        return member;
    }
}
