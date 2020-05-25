package com.maple.community.util;

import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 前缀树 过滤敏感词
 */

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    // 替换符号
    private static final String REPLACEMENT = "***";
    // 根节点
    private TireNode rootNode = new TireNode();

    @PostConstruct
    public void init(){

        try(
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))
        ){
            String keyWord;
            while((keyWord = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyWord(keyWord);
            }
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    // 过滤敏感词
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        TireNode tireNode = this.rootNode;
        int begin = 0, position = 0;
        StringBuilder builder = new StringBuilder();
        while(position < text.length()){
            char word = text.charAt(position);
            // 跳过符号
            if (isSymbol(word)){
                if (tireNode == rootNode){
                    builder.append(word);
                    begin++;
                }
                position++;
                continue;
            }
            tireNode = tireNode.getSubNode(word);
            if (tireNode == null){
                builder.append(text.charAt(begin));
                position = ++begin;
                tireNode = rootNode;
            }else if(tireNode.isKeyWordEnd()){
                builder.append(REPLACEMENT);
                begin = ++position;
                tireNode = rootNode;
            }else {
                ++position;
            }
        }
        builder.append(text.substring(begin));
        return builder.toString();
    }

    public boolean isSymbol(char c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF); // 东亚字符范围
    }

    private void addKeyWord(String keyWord){
        TireNode tireNode = rootNode;
        for(int i = 0; i < keyWord.length(); i++){
            char word = keyWord.charAt(i);
            TireNode subNode = tireNode.getSubNode(word);
            if (subNode == null){
                subNode = new TireNode();
                tireNode.addSubNode(word, subNode);
            }
            tireNode = subNode;
            if (i==keyWord.length()-1){
                tireNode.setKeyWordEnd(true);
            }
        }
    }

    private static class TireNode{
        // 关键词结束标识
        private boolean isKeyWordEnd = false;

        private Map<Character, TireNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd(){
            return this.isKeyWordEnd;
        }

        public void setKeyWordEnd(Boolean isKeyWordEnd){
            this.isKeyWordEnd = isKeyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character character, TireNode tireNode){
            subNodes.put(character,tireNode);
        }

        // 返回子节点
        public TireNode getSubNode(Character character){
            return subNodes.get(character);
        }



    }
}
