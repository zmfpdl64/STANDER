package stander.stander.controller.Rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;

import java.util.*;

@Slf4j
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
                int hour = time % (60 * 60 * 24) / (60 * 60);   //화면에 예약한 사람과 남은시간을 보여주기 위해 일:시:분:초로 나타냈다.
                int minute = time % (60 * 60) / 60;
                int second = time % 60;

                String[] user_name = seat.getMember().getName().split("");
                if (user_name.length >= 3) {
                    int length = user_name.length;
                    for (int i = 1; i < length - 1; i++) {  //익명성을 위해 앞글자와 뒷글자만 빼고 모자이크 처리했다.
                        user_name[i] = "*";
                    }
                } else {
                    user_name[0] = "*";
                }
                String left_time = String.join("", user_name) + "\n" + day + "일 " + hour + "시간 " + minute + "분" + second + "초";
                System.out.println(left_time);
                map.put("seat" + seat.getSeat_num(), left_time);
        }
            return map;

        } catch(Exception e) {
            return null;
        }
    }

    @PostMapping("/rest_reserve/complete") //실제로 안드로이드에서 서버로 예약을 요청하는 url이다.
    public Map<String, Object> reserve_complete(@RequestParam Map<String, String> map) {
        try{
            Map<String, Object> map1 = new HashMap<>();
            long id = Long.parseLong(map.get("id"));
            Member member = memberService.findById(id);

            if (seatService.check_sit(id)) {    //누군가 앉아있다면 오류 1번 발생
                map1.put("status", "1");
                map1.put("seat", null);
                return map1;
            }
            else if (seatService.check_member(member)) {    //한사람이 두번 예약 했다면 발생
                map1.put("status", "2");
                map1.put("seat", null);
                return map1;
            }
            else if(member.getTime() == 0) {  //사용자가 시간이 없다면 오류 발생
                map1.put("status", "3");
                map1.put("seat", null);
                return map1;
            }
            String url = "http://"+ ip +":8080/open/" + member.getId();   //모든 조건이 충족했다면 예약을 진행하고 qr코드를 발행한다.
            member.setQr(url);
            Seat seat = new Seat();
            seat.setMember(member);
            seat.setSeat_num(map.get("seat_num"));
            seat.setPresent_use(true);
            seat.setCheck_in(new Date());
            member.setSeat(seat);
            seatService.save(seat);
            map1.put("member", member);

            return map1;
        }
        catch (Exception e) {
//            System.out.println(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }
    @PostMapping("/rest_pay")
    public String rest_pay(@RequestParam Map<String,String> map) {
        try {
            Long id = Long.parseLong(map.get("id"));
            int time = Integer.parseInt(map.get("time"));
            Member member = memberService.findById(id); //사용자가 시간과 id를 넘기면 해당 회원에게 시간을 넣어주는 url이다.
            member.setTime(member.getTime() + time);
            memberService.modify(member);
            return "ok";
        }
        catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/rest_reserve/clear")
    public String clear(@RequestParam Map<String, String> map) {
        try{
            Long id = Long.parseLong(map.get("id"));
            Member member = memberService.findById(id); // 퇴실하기 버튼을 눌렀을 떄 해당 회원을 찾고 좌석 컬럼과 qr컬럼을 공백으로 만들어 퇴실처리를 한다.
            member.setSeat(null);
            member.setQr(null);

            seatService.clearOne(member);

            return "ok";
        }
        catch(Exception e) {
            return null;
        }
    }
    @PostMapping("/rest_reserve/clearall")  //사용할 일 없지만 모든 좌석을 초기화 시킨다.
    public String clear() {
        try {
            seatService.clearAll();
            return "ok";
        } catch (Exception e) {
            return null;
        }
    }
}
