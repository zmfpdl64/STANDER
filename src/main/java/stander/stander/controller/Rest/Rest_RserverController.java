package stander.stander.controller.Rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Rest_RserverController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private SeatService seatService;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/rest_reserve")
    public Map<String, String> rest_reserve() {
        List<Seat> result = seatService.findUseSeat();
        Map<String, String> map = new HashMap<>();
        if (result == null) {
            return null;
        }

        for (Seat seat : result) {
            int time = seat.getMember().getTime();
            int day = time / (60 * 60 * 24);  // day *
            int hour = time % (60 * 60 * 24) / (60 * 60);
            int minute = time % (60 * 60) / 60;
            int second = time % 60;

            String[] user_name = seat.getMember().getName().split("");
            if (user_name.length >= 3) {
                int length = user_name.length;
                for (int i = 1; i < length - 1; i++) {
                    user_name[i] = "*";
                }
            } else {
                user_name[0] = "*";
            }
            String left_time = String.join("", user_name) + "<br/>" + day + "일 " + hour + "시간 " + minute + "분" + second + "초";
            System.out.println(left_time);
            map.put("seat" + seat.getSeat_num(), left_time);
        }
        return map;
    }

    @GetMapping("/rest_reserve/complete")
    public String reserve_complete(@RequestParam("id") long id, @RequestParam("seat_num") String seat_num) {
        Member member = memberService.findById(id);
        if (seatService.check_sit(id)) {
            return "1";
        }
        if (seatService.check_member(member)) {
            return "2";
        }
        String url = "http://localhost:8080/open/" + member.getId();
        member.setQr(url);
        Seat seat = new Seat();
        seat.setMember(member);
        seat.setSeat_num(String.valueOf(seat_num));
        seat.setPresent_use(true);
        seat.setCheck_in(new Date());
        seatService.save(seat);

        return "ok";
    }

    @GetMapping("/rest_pay")
    public String rest_pay(@RequestParam("id") Long id, @RequestParam(required = false, name = "time") int time) {
        Member member = memberService.findById(id);
        member.setTime(member.getTime() + time);
        memberService.modify(member);
        return "ok";
    }

    @GetMapping("/rest_reserve/clear")
    public String clear(@RequestParam("id") Long id) {
        Member member = memberService.findById(id);
        if(member == null) return null;

        seatService.clearOne(member);

        return "ok";


    }
}
