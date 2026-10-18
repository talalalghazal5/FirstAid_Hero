package com.example.firstaidhero.models;

/**
 * كلاس يمثل خطوة إسعافية تابعة لحالة معينة
 */
public class FirstAidStep {
    private int stepId;
    private int caseId; // مفتاح أجنبي يشير إلى الحالة
    private int stepNumber; // رقم الترتيب (1، 2، 3...)
    private String instructionText; // نص الإجراء الإسعافي

    public FirstAidStep(int stepId, int caseId, int stepNumber, String instructionText) {
        this.stepId = stepId;
        this.caseId = caseId;
        this.stepNumber = stepNumber;
        this.instructionText = instructionText;
    }

    // Getters
    public int getStepId() { return stepId; }
    public int getCaseId() { return caseId; }
    public int getStepNumber() { return stepNumber; }
    public String getInstructionText() { return instructionText; }
}
