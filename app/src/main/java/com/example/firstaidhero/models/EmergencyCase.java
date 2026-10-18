package com.example.firstaidhero.models;

/**
 * كلاس يمثل الحالة الإسعافية (مثل: حروق، نزيف، إلخ)
 */
public class EmergencyCase {
    private int id;
    private String title; // عنوان الحالة
    private String dangerLevel; // مستوى الخطورة (حرِج، متوسط، طفيف)
    private String symptoms; // الأعراض

    public EmergencyCase(int id, String title, String dangerLevel, String symptoms) {
        this.id = id;
        this.title = title;
        this.dangerLevel = dangerLevel;
        this.symptoms = symptoms;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDangerLevel() { return dangerLevel; }
    public String getSymptoms() { return symptoms; }
}
