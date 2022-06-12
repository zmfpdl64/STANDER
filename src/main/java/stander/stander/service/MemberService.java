package stander.stander.service;

import org.springframework.transaction.annotation.Transactional;
import stander.stander.model.Form.LoginForm;
import stander.stander.model.Entity.Member;
import stander.stander.repository.JpaRepository;
import stander.stander.repository.Repository;

@Transactional
public class MemberService {

    private JpaRepository repository;


    public MemberService(JpaRepository repository) {
        this.repository = repository;
    }

    public void join(Member member) {   //회원가입 코드
//        validName(member);

        repository.save(member);
    }

    public void modify(Member member) {
        repository.merge(member);
    }       //정보 수정
    private void validName(Member member) {
        if(repository.findByUsername(member.getUsername()) != null)
        {
            throw new IllegalStateException("이름이 중복됩니다.");  //중복성 검사
        }
    }

    public Member login(LoginForm loginForm) {  //로그인 확인 코드
//        Member member = repository.findById(7L);
//        System.out.println(member.getName() + member.getPassword());
        Member member = repository.findByUsername(loginForm.getUsername());
        if( member != null) {
            if(loginForm.getUsername().equals(member.getUsername()) && loginForm.getPassword().equals(member.getPassword())) {
                System.out.println("로그인 성공했습니다.");
                return member;
            }
        }
        return null;
//        throw new IllegalArgumentException("로그인 실패했습니다.");
    }

    public Member findByEmail(String email) {
        Member member = repository.findByEmail(email);
        return member;
    }

    public Member findById(Long id) {
        Member member = repository.findById(id);
        return member;
    }

    public Member findByUsername(String username) {
        Member member = repository.findByUsername(username);
        return member;
    }




}
