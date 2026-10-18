package com.example.firstaidhero.models;

/**
 * كلاس يمثل حالة تم إسعافها مسبقاً (سجل التاريخ)
 */
public class TreatedCase {
    private int id;
    private String description; // وصف الحالة
    private String date; // تاريخ الحالة
    private String photoPath; // مسار الصورة (اختياري)
    private String procedures; // الإجراءات التي تم اتخاذها

    public TreatedCase(int id, String description, String date, String photoPath, String procedures) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.photoPath = photoPath;
        this.procedures = procedures;
    }

    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getPhotoPath() { return photoPath; }
    public String getProcedures() { return procedures; }
}
