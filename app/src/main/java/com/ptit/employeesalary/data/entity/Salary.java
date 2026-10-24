package com.ptit.employeesalary.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Bảng Salary(id, employeeId, month, year, totalHours, overtimeHours, totalSalary, status)
 * status: PENDING (chưa trả) | PAID (đã trả) (xem AppConfig)
 * Unique (employeeId, month, year) => mỗi nhân viên 1 bảng lương / tháng.
 */
@Entity(tableName = "salaries",
        foreignKeys = @ForeignKey(entity = Employee.class,
                parentColumns = "id",
                childColumns = "employeeId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"employeeId", "month", "year"}, unique = true)})
public class Salary {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int employeeId;

    private int month;

    private int year;

    private double totalHours;

    private double overtimeHours;

    private double totalSalary;

    @NonNull
    private String status = "";

    public Salary() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public double getTotalSalary() {
        return totalSalary;
    }

    public void setTotalSalary(double totalSalary) {
        this.totalSalary = totalSalary;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }
}
