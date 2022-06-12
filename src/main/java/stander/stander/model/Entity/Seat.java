package stander.stander.model.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter @Setter
public class Seat { //좌석 테이블을 다루는 db 테이블 클래스이다.
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String seat_num;    
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "member_id")
    private Member member;  // 멤버 클래스와 1:1 맵핑을 하기 때문에 Json형식에서 무한 콜이 일어난다  그렇기에 JsonIgnore을 통해 무한재귀가 일어날때 id값을 반환한다.

    private Boolean present_use;    // 현재 사용중인 좌석인가?

    private Date check_in;          // 입실 시간
    private Date check_out;         // 퇴실 시간

}
