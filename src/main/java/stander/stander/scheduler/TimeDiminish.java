package stander.stander.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.service.MemberService;
import stander.stander.service.SeatService;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class TimeDiminish {

    @Autowired
    private SeatService seatService;
    @Autowired
    private MemberService memberService;

    @Scheduled(cron = "0/1 * * * * *")  //1초마다 실행되는 함수로 실시간으로 시간을 흐르게하는 함수이다.
    public void timeDiminish() {
        List<Seat> useSeat = seatService.findUseSeat();
        if(!Objects.isNull(useSeat)) {
            for (Seat seat : useSeat) {
                int use_time = ((int) new Date().getTime() - (int) seat.getCheck_in().getTime()) / 1000;    // 1000을 나눠줘야 초로 환산된다.
               // log.info("use_time = {}", use_time);

                if (seat.getMember().getTime() == 0) {  //만약 시간이 존재하지 않는다면 예약화면에서 제겋한다.
                    Member member = seat.getMember();
                    member.setQr(null);
                    seatService.clearOne(member);   //퇴실을 진행한다.
                    memberService.modify(member);   //변경된 member db를 수정해준다.

                } else {
                    seat.getMember().setTime(seat.getMember().getTime() - 1);   //만약 시간이 존재한다면 1씩 감소 시킨다.
                    memberService.modify(seat.getMember());
                }

            }
        }
    }

}
