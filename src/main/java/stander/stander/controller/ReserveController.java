package stander.stander.controller;

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

    @GetMapping
    public String reserve(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
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

        List<Seat> result = sitService.findUseSeat();
        if (result == null) {
            return "reserve/reserve";
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
            List<Seat> sits = sitService.findAll();

            if( sits != null && sitService.check_member(member) ) { //중복이 있을때 참

                sortSit(sits);

                model.addAttribute("sits", sits);


                model.addAttribute("member", member);
                redirectAttributes.addFlashAttribute("msg", "중복으로 예약이 되어있습니다.");
                return "redirect:/reserve";
            }

            if(sitService.check_sit(id)) {
                sortSit(sits);
                model.addAttribute("sits", sits);
                model.addAttribute("member", member);
                redirectAttributes.addFlashAttribute("msg", "좌석이 이미 예약되어 있습니다");

                return "redirect:/reserve";
            }


            if(member.getTime() == 0) {
                model.addAttribute("msg", "시간 충전이 필요합니다");
                model.addAttribute("num", num);
                model.addAttribute("member", member);
                return "reserve/price";
            }

            String url = "http://localhost:8080/open/" + member.getId();

            String file_path = fileDir + member.getId() + "/";
            String file_name = "QRCODE.jpg";
            String full_path = file_path + file_name;
            QRUtil.makeQR(url, file_path, file_name);

            member.setQr(url);
            memberService.modify(member);

            Seat seat = new Seat();
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
        sits.sort(new Comparator<Seat>() {
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
        sitService.clearAll();
//        List<Sit> sits = sitService.findAll();
//        model.addAttribute("sits", sits);
        model.addAttribute("msg", "전체 퇴실이 완료 되었습니다.");

        return "redirect:/reserve";
    }
    @GetMapping("/clear/{id}")
    public String clearOne(@PathVariable("id") String id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

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
        redirectAttributes.addFlashAttribute("msg", "퇴실이 완료 되었습니다.");
        member.setQr(null);
        memberService.modify(member);

        sitService.clearOne(member);

        return "redirect:/reserve";
    }

    @GetMapping("/set_sit")
    public void reserve() {
        for(int i = 0; i < 30; i++) {
            Seat sit = new Seat();
            sitService.save(sit);
        }
    } //db에 좌석 생성
}
