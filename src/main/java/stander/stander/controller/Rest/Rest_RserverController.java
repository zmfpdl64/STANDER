package stander.stander.controller.Rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;

import java.util.*;

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

    @Value("${ip.address}")
    private String ip;

    @PostMapping("/rest_reserve")
    public Map<String, String> rest_reserve() {
        try{
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
                String left_time = String.join("", user_name) + "\n" + day + "일 " + hour + "시간 " + minute + "분" + second + "초";
                System.out.println(left_time);
                map.put("seat" + seat.getSeat_num(), left_time);
                return map;
        }

        } catch(Exception e) {
            return null;
        }
        return null;
    }

    @PostMapping("/rest_reserve/complete")
    public String reserve_complete(@RequestParam("id") long id, @RequestParam("seat_num") String seat_num) {
        try{
            Member member = memberService.findById(id);
            if (seatService.check_sit(id)) {
                return "1";
            }
            if (seatService.check_member(member)) {
                return "2";
            }
            if(member.getTime() == 0) {
                return "3";
            }
            String url = "http://"+ ip +":8080/open/" + member.getId();
            member.setQr(url);
            Seat seat = new Seat();
            seat.setMember(member);
            seat.setSeat_num(String.valueOf(seat_num));
            seat.setPresent_use(true);
            seat.setCheck_in(new Date());
            seatService.save(seat);

            return "ok";
        }
        catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return null;
        }

    }

    @PostMapping("/rest_pay")
    public String rest_pay(@RequestParam("id") Long id, @RequestParam(required = false, name = "time") int time) {
        try {
            Member member = memberService.findById(id);
            member.setTime(member.getTime() + time);
            memberService.modify(member);
            return "ok";
        }
        catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/rest_reserve/clear/{id}")
    public String clear(@PathVariable("id") Long id) {
        try{
            Member member = memberService.findById(id);
            if(member == null) return null;

            seatService.clearOne(member);

            return "ok";
        }
        catch(Exception e) {
            return null;
        }



    }
}
