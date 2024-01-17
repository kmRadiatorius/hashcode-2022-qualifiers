package com.company;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    public static List<Contributor> contributors = new ArrayList<>();
    public static Map<String, List<Contributor>> contributorsBySkill = new HashMap<>();
    public static List<Project> projects = new ArrayList<>();

    public static List<Project> projectsResult = new ArrayList<>();

    public static void main(String[] args) {
        for (char i = 'b'; i <= 'f'; i++) {
            contributors = new ArrayList<>();
            projects = new ArrayList<>();
            contributorsBySkill = new HashMap<>();
            projectsResult = new ArrayList<>();
            read(i + ".in.txt");
            solve();
            System.out.println("File " + i + " completed");
            write(i + ".out.txt");
        }
    }

    public static void solve() {
        for (var contributor : contributors) {
            for (var skill : contributor.skills.entrySet()) {
                contributorsBySkill.putIfAbsent(skill.getKey(), new ArrayList<>());
                contributorsBySkill.get(skill.getKey()).add(contributor);
            }
        }

        contributorsBySkill.forEach((index, value) -> value.sort(Comparator.comparingInt(a -> a.skills.get(index).level)));

//        contributors.forEach(a -> a.skills.forEach((skillName, skill) -> {
//                    contributorsBySkill.get(skillName).stream()
//                            .filter(contributor -> contributor.skills.get(skillName).level > skill.level)
//                            .forEach(contributor -> {
//                                skill.mentorsByTheirSkills.putIfAbsent(skillName, new ArrayList<>());
//                                skill.mentorsByTheirSkills.get(skillName).add(contributor);
//                            });
//                    skill.mentorsByTheirSkills.forEach((index, contributorList) ->
//                            contributorList.sort(Comparator.comparingInt(b -> b.skills.get(index).level)));
//                }
//        ));

        List<Project> availableProjects = new ArrayList<>(projects.stream()
                .sorted((a, b) ->  b.getRealScore(0) - a.getRealScore(0))
                .collect(Collectors.toList()));

        List<Project> ongoingProjects = new ArrayList<>();

        for (int day = 0; day <= 10000; day++) {
            final int d = day;

            var ongoingProjectsIterator = ongoingProjects.iterator();
            while (ongoingProjectsIterator.hasNext()) {
                var project = ongoingProjectsIterator.next();
                if (project.isComplete(d)) {
                    ongoingProjectsIterator.remove();
                    project.contributors.forEach(a -> a.available = true);
                }
            }

            availableProjects.removeIf(a -> a.getRealScore(d) == 0);
            if (availableProjects.isEmpty()) {
                break;
            }

            var iterator = availableProjects.iterator();
            while (iterator.hasNext()) {
                var project = iterator.next();
                List<Contributor> contributorsForProject = new ArrayList<>();

                Map<String, Contributor> potentialContributorsBySkill = new HashMap<>();
                Map<String, List<Contributor>> potentialMentorsBySkill = new HashMap<>();

                for (var requiredSkill : project.skills) {
                    var potentialMentors = contributorsBySkill.get(requiredSkill.name).stream()
                            .filter(a -> a.available)
                            .filter(a -> !contributorsForProject.contains(a))
                            .filter(a -> a.skills.get(requiredSkill.name).level >= requiredSkill.level)
                            .collect(Collectors.toList());

                    potentialMentorsBySkill.putIfAbsent(requiredSkill.name, potentialMentors);
                }

                for (var requiredSkill : project.skills) {
                    if (requiredSkill.level == 1 && !potentialMentorsBySkill.get(requiredSkill.name).isEmpty()) {
                        var contr = contributors.stream()
                                .filter(a -> a.available)
                                .filter(a -> !a.skills.containsKey(requiredSkill.name))
                                .findFirst();
                        if (contr.isEmpty()) break;
                        potentialContributorsBySkill.putIfAbsent(requiredSkill.name,  contr.get());
                    }

                    var potentialContributorsHasMentor = contributorsBySkill.get(requiredSkill.name).stream()
                            .filter(a -> a.available)
                            .filter(a -> !contributorsForProject.contains(a))
                            .filter(a -> a.skills.get(requiredSkill.name).level >= requiredSkill.level -1)
                            .findFirst();


                    if (potentialContributorsHasMentor.isPresent()) {
                        var mentros = potentialMentorsBySkill.get(requiredSkill.name);
                        if (!mentros.isEmpty()) {
                            potentialContributorsBySkill.putIfAbsent(requiredSkill.name, potentialContributorsHasMentor.get());
                            potentialContributorsBySkill.putIfAbsent(requiredSkill.name, mentros.get(0));
                            break;
                        }
                    }

                    var potentialContributors = contributorsBySkill.get(requiredSkill.name).stream()
                            .filter(a -> a.available)
                            .filter(a -> !contributorsForProject.contains(a))
                            .filter(a -> a.skills.get(requiredSkill.name).level >= requiredSkill.level)
                            .findFirst();

                    if (potentialContributors.isEmpty()) break;

                    potentialContributorsBySkill.putIfAbsent(requiredSkill.name, potentialContributors.get());
                }

                for (var requiredSkill : project.skills) {
                    var contributor = contributorsBySkill.get(requiredSkill.name).stream()
                            .filter(a -> a.available)
                            .filter(a -> !contributorsForProject.contains(a))
                            .filter(a -> a.skills.get(requiredSkill.name).level >= requiredSkill.level).findFirst();

                    if (contributor.isPresent()) {
                        contributorsForProject.add(contributor.get());
                    } else {
                        break;
                    }
                }

                if (contributorsForProject.size() == project.skills.size()) {
                    contributorsForProject.forEach(a -> a.available = false);
                    project.contributors = contributorsForProject;
                    project.startedAt = day;
                    ongoingProjects.add(project);
                    projectsResult.add(project);

                    for (int i = 0; i < project.contributors.size(); i++) {
                        ProjectSkill projectSkill = project.skills.get(i);
                        project.contributors.get(i).skills.get(projectSkill.name).levelUp(projectSkill.level);
                    }
                    iterator.remove();
                }
            }
        }
    }

    public static void read(String fileName) {
        var file = new File(fileName);
        try (var scanner = new Scanner(file)) {
            String[] line = scanner.nextLine().split(" ");

            int c = Integer.parseInt(line[0]);
            int p = Integer.parseInt(line[1]);

            for (int i = 0; i < c; i++) {
                line = scanner.nextLine().split(" ");
                String name = line[0];
                int n = Integer.parseInt(line[1]);

                Contributor contributor = new Contributor();
                contributor.name = name;
                for (int j = 0; j < n; j++) {
                    line = scanner.nextLine().split(" ");
                    String skill = line[0];
                    int level = Integer.parseInt(line[1]);
                    contributor.skills.put(skill, new Skill(skill, level));
                }
                contributors.add(contributor);
            }

            for (int i = 0; i < p; i++) {
                line = scanner.nextLine().split(" ");
                String name = line[0];
                int d = Integer.parseInt(line[1]);
                int s = Integer.parseInt(line[2]);
                int b = Integer.parseInt(line[3]);
                int r = Integer.parseInt(line[4]);

                Project project = new Project();
                project.name = name;
                project.daysToComplete = d;
                project.score = s;
                project.bestBefore = b;
                for (int j = 0; j < r; j++) {
                    line = scanner.nextLine().split(" ");
                    String x = line[0];
                    int l = Integer.parseInt(line[1]);
                    project.skills.add(new ProjectSkill(x, l, j));
                }
                projects.add(project);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void write(String fileName) {
        try (var writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(String.format("%d\n", projectsResult.size()));
            for (var project : projectsResult) {
                if (project.contributors.size() > 0) {
                    writer.write(String.format("%s\n", project.name));
                    writer.write(String.format("%s\n", project.contributors.stream().map(a -> a.name).collect(Collectors.joining(" "))));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
