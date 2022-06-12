package stander.stander.model.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)    //db 값을 담아 조작할 수 있는 Model Member 클래스이다.
    private Long id;
    private String name;    // 본명
    private String username;    //아이디
    private String password;    //비밀번호

    @Email
    private String email;   //이메일
    private String gender;  //성별
    private Long age;       //나이

    @OneToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;      //좌석 외래키 맵핑

    private String qr;      //열 수 있는 url 발행
    private int time;       
    private Date check_in;  //예약한 시간
}
