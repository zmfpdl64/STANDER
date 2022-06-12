package stander.stander.controller.NonRest;

import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import stander.stander.model.Entity.Member;
import stander.stander.qr.QRUtil;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;
import stander.stander.web.SessionConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/qr")
public class QRController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private SeatService sitService;

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/create")      //QR코드 생성하는 주소이다.
    public String makeQr(HttpServletRequest request)
            throws WriterException, IOException {


        int width = 150;
        int height = 150;

        HttpSession session = request.getSession(false);    //이것도 현재 사용자가 로그인했던 적이 있는 사용자인지 확인하는 구문이다.

        if (session == null) {
            return "menu/home";
        }

        Member member = (Member) session
                .getAttribute(SessionConstants.LOGIN_MEMBER);

        if (member == null) {
            return "menu/home";
        }
        String url = "http://localhost:8080/open/" + member.getId();    //IOT에서 접속할 주소이다. 하지만 여기 페이지는 사용되지 않는다.

        String file_path = fileDir + member.getId() + "/";
        String file_name = "QRCODE.jpg";
        String full_path = file_path + file_name;
        QRUtil.makeQR(url, file_path, file_name);


        return "qr/test";
    }
}