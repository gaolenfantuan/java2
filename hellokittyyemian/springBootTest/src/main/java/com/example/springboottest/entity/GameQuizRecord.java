package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 答题记录，对应数据库表 game_quiz_record
 * 每次提交都会落一条，用于统计「累计答对题数」以及限制每日刷分。
 */
@TableName("game_quiz_record")
public class GameQuizRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    /** 用户选择的选项 */
    private String chosen;

    /** 是否答对：1 是，0 否（is_correct 带下划线不方便映射，列名直接叫 correct） */
    private Integer correct;

    /** 本次获得积分 */
    private Integer pointsAwarded;

    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getChosen() { return chosen; }
    public void setChosen(String chosen) { this.chosen = chosen; }

    public Integer getCorrect() { return correct; }
    public void setCorrect(Integer correct) { this.correct = correct; }

    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
