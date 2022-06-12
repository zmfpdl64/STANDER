package stander.stander.controller.NonRest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.qr.QRUtil;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;
import stander.stander.web.SessionConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/reserve")
public class ReserveController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private SeatService sitService;

    @Value("${file.dir}")
    private String fileDir;

    @Value("${ip.address}")
    private String ip;

    @GetMapping //예약한 사람들을 확인할 수 있는 URL이다.
    public String reserve(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false);    //로그인 한 사람만 들어올 수 있다.
        if (session != null) {
            Member member = (Member) session.getAttribute(SessionConstants.LOGIN_MEMBER);
            if (member != null) {
                model.addAttribute("member", member);
            } else {
                model.addAttribute("msg", "회원정보가 없습니다");
                return "menu/home";
            }
        } else {
            model.addAttribute("msg", "세션이 없습니다");
            return "menu/home";
        }

        List<Seat> result = sitService.findUseSeat(); //현재 사용 중인 좌석들을 불러온다.
        if (result == null) {
            return "reserve/reserve";
        }

        for (Seat seat : result) {
            int time = seat.getMember().getTime();
            int day = time / (60 * 60 * 24);  // day *
            int hour = time % (60 * 60 * 24) / (60 * 60);   //현재 화면에 띄우기 위해 일: 시 : 분: 초 로 STRING을 만드는 작업이다.
            int minute = time % (60 * 60) / 60;
            int second = time % 60;

            String[] user_name = seat.getMember().getName().split("");  //사용자 이름을 list화 시킨 후 앞 글자와 뒷글자만 빼고 모두 *로 치환한다.
            if (user_name.length >= 3) {
                int length = user_name.length;
                for(int i = 1; i < length-1; i++) {
                    user_name[i] = "*";
                }
            }
            else{
                user_name[0] = "*";
            }
            String left_time = String.join("", user_name) + "<br/>" + day + "일 " + hour + "시간 " + minute + "분" + second + "초";
            System.out.println(left_time);
            model.addAttribute("sit" + seat.getSeat_num(), left_time);
        }
        return "reserve/reserve";
    }

    @GetMapping("/price")
    public String price(@RequestParam(name = "num", required=false) String num, Model model, HttpServletRequest request
    , RedirectAttributes redirectAttributes) {

        if (null != num) {
            Long id = Long.parseLong(num);


            HttpSession session = request.getSession(false);    //현재 로그인한 이용자인지 확인한다.
            if(session == null) {
                model.addAttribute("msg", "로그인이 필요합니다");
                return "login/login";
            }

            Member member = (Member) session.getAttribute(SessionConstants.LOGIN_MEMBER);
            if(member == null) {
                model.addAttribute("msg", "회원이 존재하지 않습니다");
                return "login/join";
            }
            List<Seat> sits = sitService.findAll();

            if( sits != null && sitService.check_member(member) ) { //중복이 있을때 참  예외처리

                sortSit(sits);

                model.addAttribute("sits", sits);


                model.addAttribute("member", member);
                redirectAttributes.addFlashAttribute("msg", "중복으로 예약이 되어있습니다.");
                return "redirect:/reserve";
            }

            if(sitService.check_sit(id)) {   //한사람이 두 좌석 예약했을 때 참 오류 발생
                sortSit(sits);
                model.addAttribute("sits", sits);
                model.addAttribute("member", member);
                redirectAttributes.addFlashAttribute("msg", "좌석이 이미 예약되어 있습니다");

                return "redirect:/reserve";
            }


            if(member.getTime() == 0) { //현재 시간이 없다면 시간 충전 페이지로 이동
                model.addAttribute("msg", "시간 충전이 필요합니다");
                model.addAttribute("num", num);
                model.addAttribute("member", member);
                return "reserve/price";
            }

            String url = "http://"+ ip +":8080/open/" + member.getId();
            //예약을 완료했을 때 IOT에서 이용할 URL 주소를 member.qr 컬럼에 저장한다.
            String file_path = fileDir + member.getId() + "/";
            String file_name = "QRCODE.jpg";
            String full_path = file_path + file_name;
            QRUtil.makeQR(url, file_path, file_name);   // 로컬 저장장치에 새로운 qr코드를 생성하는 코드이다.

            member.setQr(url);
            memberService.modify(member);   //Entity 어노테이션으로 인해 저자안해줘도 되지만 저장하는 함수

            Seat seat = new Seat();         //내가 지정한 좌석에 대해 정보를 저장한다.
            seat.setMember(member);
            seat.setSeat_num(String.valueOf(id));
            seat.setPresent_use(true);
            seat.setCheck_in(new Date());
            sitService.save(seat);

            redirectAttributes.addFlashAttribute("msg", "예약이 완료되었습니다");

            return "redirect:/mypage";
        }

        HttpSession session = request.getSession(false);
        if(session == null) {
            model.addAttribute("msg", "로그인이 필요합니다");
            return "login/login";
        }

        Member member = (Member) session.getAttribute(SessionConstants.LOGIN_MEMBER);
        if(member == null) {
            model.addAttribute("msg", "회원이 존재하지 않습니다");
            return "login/join";
        }
        model.addAttribute("member", member);


        return "reserve/price";

    }

    private void sortSit(List<Seat> sits) {
        sits.sort(new Comparator<Seat>() {          // Seat 클래스의 매개변수 id값을 기준으로 오름차순으로 정렬했다.
            @Override
            public int compare(Seat o1, Seat o2) {
                return (int) (o1.getId() - o2.getId());
            }
        });
    }

    @PostMapping("/price")
    public String price_post(@RequestParam(name = "num", required=false) String num, Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if(session == null) {
            model.addAttribute("msg", "로그인이 필요합니다");
            return "login/login";
        }

        Member member = (Member) session.getAttribute(SessionConstants.LOGIN_MEMBER);
        if(member == null) {
            model.addAttribute("msg", "회원이 존재하지 않습니다");
            return "login/join";
        }

        return "menu/mypage";
    }

    @GetMapping("/clear")
    public String clear(Model model) {
        List<Seat> result = sitService.findUseSeat();   //현재 예약 내역을 모두 삭제하는 함수이다.
        for(Seat s : result) {
            s.getMember().setSeat(null);
        }

        sitService.clearAll();
//        List<Sit> sits = sitService.findAll();
//        model.addAttribute("sits", sits);
        model.addAttribute("msg", "전체 퇴실이 완료 되었습니다.");

        return "redirect:/reserve";
    }
    @GetMapping("/clear/{id}")      //퇴실하기 버튼을 눌렀을 때 동작되는 함수이다.
    public String clearOne(@PathVariable("id") String id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);
        if(session == null) {
            model.addAttribute("msg", "로그인이 필요합니다");        //세션을 확인하여 로그인한적 있는 사용자인지 확인한다.
            return "login/login";
        }

        Member member = (Member) session.getAttribute(SessionConstants.LOGIN_MEMBER);
        if(member == null) {
            model.addAttribute("msg", "회원이 존재하지 않습니다");
            return "login/join";
        }
        redirectAttributes.addFlashAttribute("msg", "퇴실이 완료 되었습니다.");
        member.setQr(null);
        member.setSeat(null);       //퇴실할때 없어져야할 데이터들을 모두 삭제시키고 실행한다.
        memberService.modify(member);

        sitService.clearOne(member);

        return "redirect:/reserve";
    }

}
