package stander.stander.controller.NonRest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;
import stander.stander.web.SessionConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Controller
@Slf4j
public class MainController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private SeatService sitService;

    @Value("${file.dir}")
    private String fileDir;

    @Value("${ip.address}")
    private String ip;

//    public MainController(MemberService memberService, SitService sitService) {
//        this.memberService = memberService;
//        this.sitService = sitService;
//    }


//    @GetMapping("/test")
//    public String layout() {
//        return "layout/layout";
//    }
////    @GetMapping("/test2")
////    public String test() {
////        return "test";
////    }


    @GetMapping("/")    //웹서버의 홈페이지이다.
    public String home(HttpServletRequest request, Model model) {

//        log.info("hello");
        return "menu/home";
    }

    @GetMapping("/about_cafe")  //카페를 소개하는 url이다.
    public String about_cafe() {
        return "menu/about_cafe";
    }


    @GetMapping("/map")     //지도를 볼 수 있는 url이다.
    public String map() {
        return "menu/map";
    }

    @GetMapping("/mypage")  //로그인한 사용자의 정보를 확인할 수 있는 마이페이지이다.
    public String mypage(@RequestParam(name = "time", required = false) String payment_time ,HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            model.addAttribute("msg", "로그인이 필요합니다");    //세션이 존재 하지 않으면 다시 홈으로 이동한다.

            return "menu/home";
        }
        Member member = (Member) session.getAttribute(SessionConstants.LOGIN_MEMBER);    //세션 저장소에서 해당 member를 찾아낸다.

        if (member == null) {
            model.addAttribute("msg", "회원이 존재하지 않습니다");   //회원이 존재하지 않으면 다시 홈으로 이동한다.

            return "menu/home";
        }

        List<Seat> same_Member = sitService.find_Usage_History(member);
        SimpleDateFormat simple = new SimpleDateFormat("yyyy년 MM월 dd일 a HH시 mm분");  //남은시간을 나타내기 위한 폼이다.
        List<String> usage_histories = new ArrayList<>();   //사용했던 내역들을 담기 위한 List이다.
        for (Seat seat : same_Member) {    // 반복문을 통해 현재 check_in 컬럼이 null이 아니면 데이터를 추가한다.
            if (seat.getCheck_in() != null) {
                usage_histories.add(simple.format(seat.getCheck_in()) + " 입실하셨습니다.");
            }
            if (seat.getCheck_out() != null) { //이것도 마찬가지이다.
                usage_histories.add(simple.format(seat.getCheck_out()) + " 퇴실하셨습니다.");
            }
        }
        if(payment_time != null) {  //마이페이지에 매개변수가 전달되지 않을때의 예외처리를 위해 구현했다.
            member.setTime(member.getTime() + Integer.parseInt(payment_time));
            memberService.modify(member);
            log.info("member.getTime() = {}", member.getTime() + Integer.parseInt(payment_time));
            model.addAttribute("msg", "결제 되었습니다.");
        }


        //남은시간을 00일 00시 00분 으로 나타내기 위한 식이다.
        int time = member.getTime();
        int day = time / (60 * 60 * 24);  // day *
        int hour = time % (60 * 60 * 24) / (60 * 60);
        int minute = time % (60 * 60) / 60;
        int second = time % 60;
        String user_name = member.getUsername();
        String left_time = day + "일 " + hour + "시간 " + minute + "분";

        model.addAttribute("member", member);
        model.addAttribute("usage_histories", usage_histories);
        model.addAttribute("left_time", left_time);    //FRONT END에 전달해준다.

        return "menu/mypage";
    }

}
