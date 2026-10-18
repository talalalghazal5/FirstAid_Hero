package com.example.firstaidhero.models;

/**
 * كلاس يمثل جهة اتصال طوارئ (أقارب، أطباء)
 */
public class EmergencyContact {
    private int id;
    private String name; // اسم جهة الاتصال
    private String phoneNumber; // رقم الهاتف

    public EmergencyContact(int id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
}
