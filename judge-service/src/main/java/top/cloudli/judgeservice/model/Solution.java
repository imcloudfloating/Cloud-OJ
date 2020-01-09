package top.cloudli.judgeservice.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Solution implements Serializable {
    private int solutionId;
    private int problemId;
    private String userId;
    private int language;
    private int state;
    private int result;
    private double passRate;

    public Solution(String userId, int problemId, int language) {
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
    }
}