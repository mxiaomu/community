package com.maple.community.dao;

import com.maple.community.CommunityApplication;
import com.maple.community.entity.Message;
import com.maple.community.service.MessageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MessageService messageService;

    @Test
    public void testSelectConversations(){
        List<Message> messageList = messageMapper.selectConversations(111,0,20);
        for(Message message:messageList){
            System.out.println(message);
        }
    }
    @Test
    public void testSelectConversationCount(){
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
    }

    @Test
    public void testSelectLetters(){
        List<Message> messageList = messageMapper.selectLetters("111_112",0,20);
        for(Message message : messageList){
            System.out.println(message);
        }

    }

    @Test
    public void testSelectLetterCount(){
        int count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);
    }

    @Test
    public void selectLetterUnreadCount(){
        int count = messageMapper.selectLetterUnreadCount(111,"111_112");
        System.out.println(count);
    }

    @Test
    public void insertMessage(){
        Message message = new Message();
        message.setContent("hello");
        message.setCreateTime(new Date());
        message.setFromId(111);
        message.setToId(110);
        message.setStatus(0);
        message.setConversationId("110_111");
        messageService.addMessage(message);
    }


}
