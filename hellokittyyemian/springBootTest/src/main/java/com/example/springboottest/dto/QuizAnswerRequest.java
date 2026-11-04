package com.example.springboottest.dto;

/**
 * 答题提交请求体
 */
public class QuizAnswerRequest {

    /** 用户 ID */
    private Long userId;

    /** 题目 ID */
    private Long questionId;

    /** 用户选择的选项：A / B / C / D */
    private String choice;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
}
