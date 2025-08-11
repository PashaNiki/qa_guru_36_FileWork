package model;

public class Trainer {
    public String name;
    public String specialization;
    public int experienceYears;

    @Override
    public String toString() {
        return name + " (" + specialization + ", опыт: " + experienceYears + " лет)";
    }
}