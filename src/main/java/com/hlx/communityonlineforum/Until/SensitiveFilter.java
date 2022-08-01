package com.hlx.communityonlineforum.Until;

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
 * 过滤敏感词
 * @Component 把普通pojo实例化到spring容器中
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 前缀树
    private class TrieNode {
        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyWord;
            while((keyWord = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyword(keyWord);
            }

        }
        catch (IOException e){
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tmp = rootNode;
        for(int i = 0;i < keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tmp.getSubNode(c);
            if (subNode == null){
                // 初始化子节点
                subNode = new TrieNode();
                tmp.addSubNode(c,subNode);
            }
            // 指向子节点,进入下一轮循环
            tmp = subNode;
        }
        tmp.setKeywordEnd(true);

    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text))
            return null;
        // 指针1
        TrieNode tmp = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();
        while(begin < text.length()){
            if(position < text.length()){
                Character c = text.charAt(position);
                // 跳过符号
                if(isSymbol(c)){
                    if (tmp == rootNode){
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }
                // 检查下级节点
                tmp = tmp.getSubNode(c);
                // 以begin开头的字符串不是敏感词
                if (tmp == null){
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tmp = rootNode;
                }
                // 重新指向根节点
                else if(tmp.isKeywordEnd()){
                    // 发现敏感词,将begin~position字符串替换掉
                    sb.append(REPLACEMENT);
                    // 进入下一个位置
                    begin = ++position;
                    tmp = rootNode;
                }
                // 检查下一个字符
                else
                    position++;
            }
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tmp = rootNode;
            }
        }
        return sb.toString();
    }
}
