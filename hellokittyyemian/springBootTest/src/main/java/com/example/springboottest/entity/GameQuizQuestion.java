package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 答题题库，对应数据库表 game_quiz_question
 * 题面 / 选项全部存在表里，前端只拿得到题目，答案由后端判，避免「查看源码就知道答案」。
 */
@TableName("game_quiz_question")
public class GameQuizQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题干 */
    private String content;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    /** 正确答案：A / B / C / D */
    private String answerKey;

    /** 分类：甜点 / 穿搭 */
    private String category;

    /** 难度描述 */
    private String difficulty;

    /** 答对奖励积分 */
    private Integer points;

    /** 答案解析 */
    private String explanation;

    /** 排序号，越小越靠前 */
    private Integer sort;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getAnswerKey() { return answerKey; }
    public void setAnswerKey(String answerKey) { this.answerKey = answerKey; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
