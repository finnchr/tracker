package com.finnchristian.tracker.model.runkeeper;

import com.google.gson.annotations.SerializedName;

public class User {
    private String profile;
    private String nutrition;
    private String weight;
    @SerializedName("change_log")
    private String changeLog;
    @SerializedName("userID")
    private int userId;
    private String settings;
    @SerializedName("strength_training_activities")
    private String strengthTrainingActivities;
    private String records;
    @SerializedName("fitness_activities")
    private String fitnessActivities;
    private String sleep;
    private String team;
    @SerializedName("background_activities")
    private String backgroundActivities;
    @SerializedName("general_measurements")
    private String generalMeasurements;
    private String diabetes;

    public String getProfile() {
        return removeLeadingSlash(profile);
    }

    public String getNutrition() {
        return removeLeadingSlash(nutrition);
    }

    public String getWeight() {
        return removeLeadingSlash(weight);
    }

    public String getChangeLog() {
        return removeLeadingSlash(changeLog);
    }

    public int getUserId() {
        return userId;
    }

    public String getSettings() {
        return removeLeadingSlash(settings);
    }

    public String getStrengthTrainingActivities() {
        return removeLeadingSlash(strengthTrainingActivities);
    }

    public String getRecords() {
        return removeLeadingSlash(records);
    }

    public String getFitnessActivities() {
        return removeLeadingSlash(fitnessActivities);
    }

    public String getSleep() {
        return removeLeadingSlash(sleep);
    }

    public String getTeam() {
        return removeLeadingSlash(team);
    }

    public String getBackgroundActivities() {
        return removeLeadingSlash(backgroundActivities);
    }

    public String getGeneralMeasurements() {
        return removeLeadingSlash(generalMeasurements);
    }

    public String getDiabetes() {
        return removeLeadingSlash(diabetes);
    }

    private String removeLeadingSlash(final String s) {
        return (s != null && s.length() > 0 && s.startsWith("/")) ? s.substring(1) : s;
    }
}
