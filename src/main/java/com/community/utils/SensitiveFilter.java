package com.community.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.CharUtils;
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
 * 敏感词过滤器，使用前缀树构造敏感词，以空间换取时间的策略提升敏感词查询性能。
 */
@Component
public class SensitiveFilter {

    private static final  Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //需要被替换后的字符串
    private static final String REPLACEWORD = "***";

    private TireNode rootNode = new TireNode();

    /**
     * 初始化这个前缀树，@PostConstruct的作用就是类构造器执行之后就立即执行
     */
    @PostConstruct
    public void init(){
        try(
            //包装成高级流，提高读取效率
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream("sensitive.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
       ) {
            String word;
            try {
                while ((word = reader.readLine()) != null){
                    //然后更具这个词构造一颗前缀树
                    this.addKeyWord(word);
                }
            } catch (IOException e) {
                logger.error("读取敏感词文件错误："+e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造前缀树的核心算法
     * @param word
     */
    private void addKeyWord(String word) {
        TireNode tempNode = rootNode;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            TireNode tempNodeTireNode = tempNode.getTireNode(c);
            //无该子节点，就新建一个子节点，否则直接挂载到这个节点下面
            if (tempNodeTireNode == null){
                tempNodeTireNode = new TireNode();
                tempNode.addTireNode(c,tempNodeTireNode);
            }
            //进入下一层继续构造
            tempNode = tempNodeTireNode;

            //最后一个关键字设置为结尾
            if (i == word.length() - 1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 检索敏感词核心算法，三指针方式
     * @param text 带过滤文本
     * @return 过滤后的文本
     */
    public String filterText(String text){
        //首先判断这个敏感词是否为空
        if (StringUtils.isBlank(text)){
            return null;
        }

        //定义三个指针，一个用于前缀树的扫描，另外两个用于扫描给定的词
        TireNode tmpNode = rootNode;
        int begin = 0;  //慢指针
        int position = 0;   //快指针
        StringBuffer buffer = new StringBuffer();

        //快指针到末尾的时候，说明整个待过滤的敏感词已经扫描完毕。
        while (position < text.length()){
            //判断此字符是不是敏感词
            char c = text.charAt(position);

            //跳过无效符号
            if (isSymbol(c)){
                //当这个节点是根节点，直接把这个值加入结果,然后指针2，3向后移动
                if (tmpNode == rootNode){
                    buffer.append(c);
                    begin++;
                }
                position++;
                continue;
            }


            //检查这个字符是不是敏感词开头
            tmpNode = tmpNode.getTireNode(c);
            //不是敏感词开头，继续判断后面的字符是不是敏感词
            if (tmpNode == null){
                buffer.append(c);
                //指针后移
                position = ++begin;
                //前缀树指针要重新从根节点开始
                tmpNode = rootNode;
            } else if (tmpNode.isKeyWordEnd()){
                //此节点是一个结束的标志，标示找到了目标哦敏感词，将begin~position内容替换
                buffer.append(REPLACEWORD);
                //然后两个指针可以移动到这个快指针的后面这个位置
                begin = ++position;
            } else {
                //最后的一种情况就是是一个疑似字符，但是没有检查完，所以快指针后移，
                position++;
            }
        }
        //如果都到最后一个字符依然不是敏感词，就把最后的begin~position加入这个结果
        buffer.append(text.substring(begin));
        return buffer.toString();
    }

    /**
     * 判断这个字符是不是特殊的字符
     * c < 0x2E80 || c > 0x9FFF 为东亚字符
     * @param c
     * @return true=表示是特殊字符
     */
    private boolean isSymbol(char c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    /**
     * 前缀树节点。
     */
    private class TireNode{
        //标志此节点是否是底层节点，也就是到了这个节点此路径就是一个敏感词
        boolean isKeyWordEnd = false;
        //子节点,这里使用map的好处是，关键词同一层相同的字符可以合并，节省空间。
        Map<Character,TireNode> childNode = new HashMap<>();

        public boolean isKeyWordEnd(){
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean isKeyWordEnd){
            this.isKeyWordEnd = isKeyWordEnd;
        }

        public void addTireNode(Character character,TireNode tireNode){
            childNode.put(character,tireNode);
        }

        public TireNode getTireNode(Character character){
            return childNode.get(character);
        }
    }
}
