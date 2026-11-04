package com.example.springboottest.vo;

import java.util.List;

/**
 * 答题结果
 */
public class QuizResultVO {

    /** 是否答对 */
    private Boolean correct;

    /** 用户选的选项 */
    private String chosen;

    /** 正确答案 */
    private String answerKey;

    /** 答案解析 */
    private String explanation;

    /** 本次获得积分（答错为 0） */
    private Integer pointsAwarded;

    /** 当前积分余额 */
    private Integer points;

    /** 本次新解锁的成就 */
    private List<GameAchievementVO> newAchievements;

    private String message;

    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }

    public String getChosen() { return chosen; }
    public void setChosen(String chosen) { this.chosen = chosen; }

    public String getAnswerKey() { return answerKey; }
    public void setAnswerKey(String answerKey) { this.answerKey = answerKey; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public List<GameAchievementVO> getNewAchievements() { return newAchievements; }
    public void setNewAchievements(List<GameAchievementVO> newAchievements) { this.newAchievements = newAchievements; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
