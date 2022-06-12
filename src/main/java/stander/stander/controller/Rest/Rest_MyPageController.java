package stander.stander.controller.Rest;

import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Rest_MyPageController {

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

    @PostMapping("/rest_mypage")
    public Map<String, Object> rest_mypage(@RequestParam Map<String, String> map1) {
        try {
            Map<String, Object> map = new HashMap<>();
            Member member = memberService.findById(Long.parseLong(map1.get("id")));     //안드로이드에서 id값을 입력받아 해당 회원을 찾는다.
            int time = member.getTime();
            int day = time / (60 * 60 * 24);  // day *
            int hour = time % (60 * 60 * 24) / (60 * 60);
            int minute = time % (60 * 60) / 60;
            int second = time % 60;
            String user_name = member.getUsername();
            String left_time = day + "일 " + hour + "시간 " + minute + "분";
            map.put("member", member); //                                       멤버와 남은시간과 좌석을 담아 안드로이드에 전달해준다.
            map.put("time", left_time);
            if( member.getSeat() == null) return map;
            map.put("seat_num", member.getSeat().getSeat_num());
            return map;
        }
        catch (Exception e) {

            e.printStackTrace();
            return null;
        }

    }
    @PostMapping("/rest_mypage/qr")
    public String check_qr(@RequestParam Map<String, String> map) {
        try{
            Member member = memberService.findById(Long.parseLong(map.get("id")));
            if(member.getQr() == null) {        //지금 현재 예약 상태인지 확인하는 코드이다.
                return null;
            }
            return "ok";
        }
        catch (Exception e) {
            return null;
        }

    }

    @PostMapping("/rest_mypage/use_history")
    public List<String> use_history(@RequestParam Map<String, String> map) {

        try {
            Long id = Long.parseLong(map.get("id"));
            Member member = memberService.findById(id);     //통신을 하려는 id의 멤버를 찾아내고 이용내역들을 반환해준다.
            List<String> his = new ArrayList<>();
            List<Seat> result = seatService.find_Usage_History(member);
            SimpleDateFormat simple = new SimpleDateFormat("yyyy년 MM월 dd일 a HH시 mm분");
            for (Seat seat : result) {
                if (seat.getCheck_in() != null) {
                    his.add(simple.format(seat.getCheck_in()) + " 입실하셨습니다.");
                }
                if (seat.getCheck_out() != null) {
                    his.add(simple.format(seat.getCheck_out()) + " 퇴실하셨습니다.");
                }
            }
            return his;
        }
        catch (Exception e){
            return null;
        }

    }
}
