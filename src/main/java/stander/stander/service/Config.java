package stander.stander.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import stander.stander.repository.JpaRepository;
import stander.stander.repository.JpaSitRepository;

import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import java.io.InputStream;
import java.sql.SQLException;

@Configuration
public class Config implements WebMvcConfigurer {

    @Value("${file.dir")
    private String fileDir;

    private EntityManager em;

    public Config(EntityManager em) {
        this.em = em;
    }

    @Bean
    public JpaRepository repository() throws SQLException {  //sit, member 의존성 주입
        return new JpaRepository(em);
    }

    @Bean
    public JpaSitRepository sitrepository() throws SQLException {
        return new JpaSitRepository(em);
    }

    @Bean
    public MemberService memberService() throws SQLException {
        return new MemberService(repository());
    }

    @Bean
    public SeatService sitService() throws SQLException {
        return new SeatService(sitrepository());
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")       //로컬 image를 url로 렌더링해주는 코드이다.
                .addResourceLocations("file:D:\\images\\");
    }
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/images/**")
//                .addResourceLocations("file:///usr/");
//    }
}
