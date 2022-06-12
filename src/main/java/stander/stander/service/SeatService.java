package stander.stander.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import stander.stander.model.Entity.Member;
import stander.stander.model.Entity.Seat;
import stander.stander.repository.JpaSitRepository;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Transactional
public class SeatService {

    private JpaSitRepository sitRepository;

    public SeatService(JpaSitRepository sitRepository) {
        this.sitRepository = sitRepository;
    }

    public void save(Seat seat) {
        sitRepository.save(seat);
    }

    public void use(Long id, Member member){
        sitRepository.merge(id, member);
    }

    public Seat clearOne(Member member) {   //특정 좌석 퇴실 처리 하는 코드
        List<Seat> result = sitRepository.findUseSeat();
        if( result == null) return null;
        for(Seat seat : result) {
            if(seat.getMember().getId().equals(member.getId())) {
                seat.setPresent_use(false);
                seat.setCheck_out(new Date());
            }
        }
        return null;
    }
    public void clearAll() {    //모든 좌석 퇴실
        List<Seat> result = sitRepository.findAll();
        for(Seat seat : result) {
            seat.setPresent_use(false);
        }
    }

    public Seat findMember(Member member) { //특정 member가 앉고 있는 seat 찾는 함수
        Seat seat = sitRepository.findByMember(member);
        return seat;
    }

    public List<Seat> find_Usage_History(Member member) {   //사용자의 이용내역을 불러오는 함수
        List<Seat> result = sitRepository.findByMembers(member);
        if(result == null) return null;
        return result;
    }

    public Seat findById(Long id) {
        Seat seat = sitRepository.findById(id);
        return seat;
    }

    public List<Seat> findUseSeat() {
        List<Seat> result = sitRepository.findUseSeat();
        if(result == null) return null;
        else return result;
    }

    public List<Seat> findAll() {
        return sitRepository.findAll();

    }

    public Boolean check_member(Member member) {    //현재 사용자가 중복 예약 중인지 확인하는 함수
        List<Seat> result = sitRepository.findUseSeat();
        if(result == null) return false;
        for( Seat seat : result) {
            log.info("getMember/{} = {}/ {} / getPresent_use = {}",member, seat.getMember(), Objects.equals(member, seat.getMember()), seat.getPresent_use());
            log.info("username = {} / seat.username = {}", member.getUsername(), seat.getMember().getUsername());
            //db에서 객체를 저장하고 불러오는 과정에서 새롭게 객체를 만들어 아마 다른 객체라고 인식하는것 같다.
            if(seat.getMember().getId().equals(member.getId())  && seat.getPresent_use()) {     //중복 예약했을 때
                return true;
            }
        }
        return false;
    }
    public Boolean check_sit(Long id) { //좌석이 이미 예약 되어 있으면 true 반환
        List<Seat> result = sitRepository.findUseSeat();
        if(result == null) return false;
        for(Seat seat : result) {
            if(seat.getSeat_num().equals(String.valueOf(id))) {
                return true;
            }
        }
        return false;
    }

    public void use() {
    }
}
