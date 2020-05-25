package com.maple.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class FileMkdirTest {
    private static final String filePath = "c://work//data//wkImages";
    @Test
    public void testMkdirFile(){
        File file = new File(filePath);
        if (!file.exists()){
            file.mkdir();
        }else {
            System.out.println("目录已经存在");
        }
    }
}
