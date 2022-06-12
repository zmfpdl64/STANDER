package stander.stander.model.Form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Getter @Setter
public class MemberForm {               //회원가입 폼이다. 규격을 설정해서 로그인 했을 때의 데이터가 부합하면 회원가입이 되고 안되면 다시 회원가입을 한다.

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        private String name;
        @NotBlank(message = "아이디는 필수 입력 값입니다.")
        private String username;
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
        private String password;
        private String gender;
        private String age;
        @Pattern(regexp = "^(?:\\w+\\.?)*\\w+@(?:\\w+\\.)+\\w+$", message = "이메일 형식이 올바르지 않습니다.")
        private String email;

}
