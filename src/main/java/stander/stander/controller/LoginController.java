package stander.stander.controller;


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
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private SeatService seatService;

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping
    public String post_login() {
        return "login/login";
    }

    @PostMapping
    public String check_login(@ModelAttribute @Valid LoginForm loginForm, BindingResult errors, HttpServletRequest request, HttpServletResponse response
            , Model model) {
        if(errors.hasErrors()) {
            Map<String, String> validatorResult = new HashMap<>();
            for(FieldError error : errors.getFieldErrors()) {
                String key = String.format("valid_%s", error.getField());
                model.addAttribute(key, error.getDefaultMessage());
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
    }

    @PostMapping("/join")
    public String create_join(@ModelAttribute @Valid MemberForm memberForm, BindingResult errors, Model model) {

        if (errors.hasErrors()) {
            Map<String, String> validatorResult = new HashMap<>();
            for(FieldError error : errors.getFieldErrors()) {
                String key = String.format("valid_%s", error.getField());
                model.addAttribute(key, error.getDefaultMessage());
            }
            return "login/join";
        }

        Member member = new Member();
        member.setName(memberForm.getName());
        member.setUsername(memberForm.getUsername());
        member.setPassword(memberForm.getPassword());
        member.setAge(Long.parseLong(memberForm.getAge()));
        member.setEmail(memberForm.getEmail());
        member.setGender(memberForm.getGender());
        memberService.join(member);
        model.addAttribute("member", memberForm);
//        return "login/join";
        return "redirect:/";
    }

    @GetMapping("/findpassword")
    public String findpassword() {



        return "login/findpassword";
    }

    @PostMapping("/findpassword")
    public String post_findpassword(@RequestParam("username") String username, Model model) throws MessagingException {

        Member member = memberService.findByUsername(username);
//        log.info("member.getEmail = {}          username = {}", member.getUsername(), username);
        try{
            if(member.getUsername().equals(username)) {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                mimeMessageHelper.setFrom(from);
                mimeMessageHelper.setTo(member.getEmail());
                mimeMessageHelper.setSubject("[STNADER] 비밀번호 안내");
                mimeMessageHelper.addInline("icon", new File("C:/images/icon.jpg"));


                StringBuilder body = new StringBuilder();
                body.append("<center><img src=\"http://localhost:8080/img/icon1.jpg\"><h1 style=\"color:#87CEEB; \">" +
                        "비밀번호</h1><br><br><h1>안녕하세요 "+ member.getName()+"님"  +"</h1><br> 고객님의 비밀번호는: "
                        + member.getPassword() +"입니다." +"</center>");
                mimeMessageHelper.setText(body.toString(), true);
                javaMailSender.send(mimeMessage);
            }
            model.addAttribute("msg", "메일이 발송됐습니다");
            return "login/findpassword";
        }
        catch(Exception e) {
            log.info(String.valueOf(e));
            model.addAttribute("msg", "오류가 발생했습니다");
            return "login/findpassword";
        }

    }




    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }


}
