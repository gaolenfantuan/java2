package com.example.springboottest.vo;

import java.util.List;

/**
 * 题目展示对象
 * 注意：这里刻意不带 answerKey / explanation，答案只在提交后由 QuizResultVO 返回。
 */
public class QuizQuestionVO {

    private Long id;

    /** 题干 */
    private String content;

    /** 选项列表（固定 4 个，key 为 A/B/C/D） */
    private List<Option> options;

    /** 分类：甜点 / 穿搭 */
    private String category;

    private String difficulty;

    /** 答对可得积分 */
    private Integer points;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    /** 单个选项 */
    public static class Option {

        /** A / B / C / D */
        private String key;

        /** 选项文案 */
        private String text;

        public Option() {
        }

        public Option(String key, String text) {
            this.key = key;
            this.text = text;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
