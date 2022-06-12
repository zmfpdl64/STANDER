package stander.stander.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import stander.stander.model.Entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class JpaRepository implements stander.stander.repository.Repository {

    private final EntityManager em;

    public JpaRepository(EntityManager em) {
        this.em = em;
    }   //jpa 매니지먼트 를 주입시켜준다.

    public Member save(Member member) {


        if(member.getId() == null)  //기존 데이터가 존재한다면 merge 없다면 persist
            em.persist(member);
        else
            em.merge(member);
        return member;
    }

    public Member merge(Member member) {
        em.merge(member);
        return member;
    }

    @Override
    public Member findById(Long id) {
        Member member = em.find(Member.class, id);  //id값을 입력받아 찾는다.
        return member;
    }

    @Override
    public Member findByUsername(String username) {
        List<Member> result = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username).getResultList();    //username을 통해 member를 찾아낸다.
        return result.size() == 0 ? null : result.get(0);
    }

    @Override
    public Optional<Member> findByPasswd(String password) {
        List<Member> result = em.createQuery("select m from Member m where m.password = :password", Member.class)
                .setParameter("password", password).getResultList();
        return result.stream().findAny();   //패스워드를 통해 상대방을 찾아낸다.
    }
    public Member findByEmail(String email) {
        Member result = em.createQuery("select m from Member m where m.email : email", Member.class)
                .setParameter("email", email)       //이메일을 통해 상대방을 찾아낸다.
                .getSingleResult();
        return result == null ? null : result;

    }

    public List<Member> findAll(String username) {  //모든 사용자들을 찾아낸다.
        List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();

        return result;
    }
}
