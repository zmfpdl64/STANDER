package stander.stander.model.Form;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class LoginForm {        //로그인을 위한 폼이다. 빈칸으로 로그인했을 때 오류 메시지를 반환한다.
    @NotBlank(message = "아이디를 입력해야합니다.")
    private String username;
    @NotBlank(message = "비밀번호가 필요합니다.")
    private String password;


}
