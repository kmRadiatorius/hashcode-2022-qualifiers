package com.company;

import java.util.ArrayList;
import java.util.List;

public class Project {
    public String name;
    public int daysToComplete;
    public int score;
    public int bestBefore;
    public int startedAt;
    public List<ProjectSkill> skills = new ArrayList<>();
    public List<Contributor> contributors = new ArrayList<>();

    public int getRealScore(int day) {
        int deadline = bestBefore - day - daysToComplete;
        if (deadline < 0) {
            return score - deadline > 0 ? score - bestBefore : 0;
        }
        return score;
    }

    public boolean isComplete(int day) {
        return day - startedAt == daysToComplete;
    }
}
