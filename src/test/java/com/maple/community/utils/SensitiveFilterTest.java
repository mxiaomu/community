package com.maple.community.utils;

import com.maple.community.CommunityApplication;
import com.maple.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveFilterTest  {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testFilter(){
        String text = sensitiveFilter.filter("大家😊好不好");
        System.out.println(text);
    }
}
