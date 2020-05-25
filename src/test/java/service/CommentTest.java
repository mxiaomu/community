package service;

import com.maple.community.CommunityApplication;
import com.maple.community.entity.Comment;
import com.maple.community.service.CommentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CommentTest {
    @Autowired
    private CommentService commentService;

    @Test
    public void testSelectCommentsByEntity(){
        List<Comment> lists = commentService.findCommentsByEntity(1,274,0, 20);
        for (Comment comment : lists){
            System.out.println(comment.getContent());
        }
    }
}
