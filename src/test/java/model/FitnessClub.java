package model;

import java.util.List;

public class FitnessClub {
    public String clubName;
    public String location;
    public List<Trainer> trainers;

    @Override
    public String toString() {
        return clubName + " (" + location + "), тренеры: " + trainers;
    }
}