package stander.stander.controller.NonRest;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import stander.stander.model.Entity.Member;
import stander.stander.model.Form.LoginForm;
import stander.stander.model.Form.MemberForm;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;
import stander.stander.web.SessionConstants;

import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.*;
import javax.validation.Valid;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/login")   //로그인을 담당하는 url입니다.
public class LoginController {

    @Autowired
    private MemberService memberService;    //memberService라는 회원관리 클래스를 스프링부트에 등록시켜줍니다.
    @Autowired
    private SeatService seatService;        //seatService라는 좌석관리 클래스를 스프링부트에 등록시켜줍니다.

    private final JavaMailSender javaMailSender;    //비밀번호를 잃어버린 사용자에게 메일을 전송해주는 클래스입니다.

    @Value("${spring.mail.username}")       //application.yml 에서 설정한 mail의 주소입니다.
    private String from;

    @Value("${file.dir}")                   //application.yml에서 설정한 내가 저장할 file들의 주소입니다.
    private String fileDir;

    @Value("${ip.address}")
    private String ip;

    @GetMapping
    public String post_login() {
        return "login/login";
    }   //로그인 페이지로 렌더링 시켜줍니다.

    @PostMapping
    public String check_login(@ModelAttribute @Valid LoginForm loginForm, BindingResult errors, HttpServletRequest request, HttpServletResponse response
            , Model model) {    //model/Form/MemberForm에서 정해놓은 규격에 어긋나게 데이터가 들어오면 error를 포함한 반환값들이 다시 login페이지로 전달됩니다.
        if(errors.hasErrors()) {
            Map<String, String> validatorResult = new HashMap<>();
            for(FieldError error : errors.getFieldErrors()) {
                String key = String.format("valid_%s", error.getField());   //로그인 페이지에 오류메시지를 띄우기 위한 문자열 만들기
                model.addAttribute(key, error.getDefaultMessage());         //로그인 페이지에 전달하기 위한 작업
            }
            return "login/login";
        }
        Member member = memberService.login(loginForm);
        if(member != null) {
            HttpSession session = request.getSession(); //현재 사용자의 세션이 없으면 생성해서 전달
            session.setAttribute(SessionConstants.LOGIN_MEMBER, member);   //내 세션 저장소에 key: value형태로 저장
            return "redirect:/mypage";
        }
        model.addAttribute("member", loginForm);
        model.addAttribute("msg", "아이디나 비밀번호가 일치하지 않습니다");
        return "login/login";
    }


    @GetMapping("/join")
    public String post_join() {
        return "login/join";
    }   //회원가입 페이지로 렌더링해줍니다.

    @PostMapping("/join")                               //회원가입 POST신호가 전달되면 해당 함수를 실행합니다.
    public String create_join(@ModelAttribute @Valid MemberForm memberForm, BindingResult errors, Model model) {

        if (errors.hasErrors()) {   //로그인과 동일하게 model/Form/MemberForm에서 규격에 따라 진행한다.
            Map<String, String> validatorResult = new HashMap<>();
            for(FieldError error : errors.getFieldErrors()) {
                String key = String.format("valid_%s", error.getField());
                model.addAttribute(key, error.getDefaultMessage());
            }
            return "login/join";
        }

        Member member = new Member();
        member.setName(memberForm.getName());
        member.setUsername(memberForm.getUsername());   //위의 유효성검증이 끝났으면 해당 정보들을 저장하여 회원가입을 한다.
        member.setPassword(memberForm.getPassword());
        member.setAge(Long.parseLong(memberForm.getAge()));
        member.setEmail(memberForm.getEmail());
        member.setGender(memberForm.getGender());
        memberService.join(member);
        model.addAttribute("member", memberForm);
//        return "login/join";
        return "redirect:/";    //이 후 리다이렉트를 통한 홈페이지로 이동한다.
    }

    @GetMapping("/findpassword")
    public String findpassword() {  //비밀번호를 찾기위한 주소



        return "login/findpassword";
    }

    @PostMapping("/findpassword")   //사용자에게 해당 url로 post 신호가 오면 실행이 된다.
    public String post_findpassword(@RequestParam("username") String username, Model model) throws MessagingException {

        Member member = memberService.findByUsername(username);
//        log.info("member.getEmail = {}          username = {}", member.getUsername(), username);
        try{
            if(member.getUsername().equals(username)) {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage(); //보낼 메신져를 생성하고
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); //데이터 형식을 맞춰준다.
                mimeMessageHelper.setFrom(from); //보낼 계정
                mimeMessageHelper.setTo(member.getEmail()); //받을 계정
                mimeMessageHelper.setSubject("[STNADER] 비밀번호 안내");


                StringBuilder body = new StringBuilder();
                body.append("<center><img src=\"http://"+ ip +":8080/img/icon1.jpg\"><h1 style=\"color:#87CEEB; \">" +
                        "비밀번호</h1><br><br><h1>안녕하세요 "+ member.getName()+"님"  +"</h1><br> 고객님의 비밀번호는: "
                        + member.getPassword() +"입니다." +"</center>");
                mimeMessageHelper.setText(body.toString(), true); //원하는 메시지를 담고 데이터를 저장한다.
                javaMailSender.send(mimeMessage);       // 자바메일센더를 통해 메시지를 발송한다.
            }
            model.addAttribute("msg", "메일이 발송됐습니다");
            return "login/findpassword"; //이후 다시 렌더링을한다.
        }
        catch(Exception e) {
            log.info(String.valueOf(e));
            model.addAttribute("msg", "오류가 발생했습니다");
            return "login/findpassword";
        }

    }




    @GetMapping("/logout")  //로그아웃 url이다.
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            session.invalidate();   //세션을 없에는 작업이다.
        }
        return "redirect:/";        //홈으로 리다이렉트해준다.
    }


}
