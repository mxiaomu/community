package com.maple.community.dao;


import com.maple.community.CommunityApplication;
import com.maple.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    public void setMailClient(MailClient mailClient) {
        this.mailClient = mailClient;
    }

    @Test
    public void testMailSend(){
        mailClient.sendMail("maplecoding@sina.com","spring","hello world");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","mufeng");
        String content = templateEngine.process("mail/demo", context);
        mailClient.sendMail("1433187543@qq.com", "Spring-Boot-Starter-Mail", content);
    }
}
