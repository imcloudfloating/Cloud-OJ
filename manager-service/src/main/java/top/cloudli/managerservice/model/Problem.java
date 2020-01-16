package top.cloudli.managerservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Problem {
    private int problemId;
    private String title;
    private String description;
    private String input;
    private String output;
    private String sampleInput;
    private String sampleOutput;
    private String category;
    private long timeout;
    private int score;
    private boolean enable;
    private String createAt;
}
