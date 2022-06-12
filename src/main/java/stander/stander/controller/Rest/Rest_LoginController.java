package stander.stander.controller.Rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.model.Form.LoginForm;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Value("${ip.address}")
    private String ip;

    @ResponseBody
    @PostMapping("/rest-login") //안드로이드에서 rest api를 이용한 로그인 방식이다.
    public Member rest_login(@ModelAttribute LoginForm loginForm){
        try {
            loginForm.setUsername(loginForm.getUsername());
            loginForm.setUsername(loginForm.getUsername());     //사용자에게 아이디 비밀번호를 입력받아 회원이 존재하면 로그인을 진행한다.
            Member member = memberService.login(loginForm);

            return member;
        }
        catch (Exception e ) {
            return null;
        }

    }

    @ResponseBody
    @PostMapping("/rest_join")  //안드로이드에서 name, username, password, email을 입력받아 회원가입하는 코드이다.
    public String rest_join(@RequestParam Map<String, String> map) {
        try {

            Member member;
            member = set_join(map.get("name"), map.get("username"), map.get("password"), map.get("email"));
            memberService.join(member); //사용자에게 회원가입하기 위한 입력값을 받고 회원가입에 성공하면 ok 문자열을 전달한다.
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
