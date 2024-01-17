package com.company;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Skill {
    public String name;
    public int level;
    public Map<String, List<Contributor>> mentorsByTheirSkills;

    public Skill(String name, int level) {
        this.name=  name;
        this.level = level;
    }

    public void levelUp(int projectSkillLevel) {
        if (projectSkillLevel >= level) {
            level++;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, level);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Skill)) return false;

        Skill that = (Skill) other;
        return name.equals(that.name) && level == that.level;
    }
}
