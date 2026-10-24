package com.ptit.employeesalary.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Bảng WorkLog(id, employeeId, workDate, checkInTime, checkOutTime, totalHours, status)
 * workDate: yyyy-MM-dd, checkInTime/checkOutTime: HH:mm:ss
 * status: CHECKED_IN | COMPLETED | INCOMPLETE (xem AppConfig)
 * Unique (employeeId, workDate) => mỗi nhân viên chỉ có 1 bản ghi / ngày (chống check-in trùng).
 */
@Entity(tableName = "work_logs",
        foreignKeys = @ForeignKey(entity = Employee.class,
                parentColumns = "id",
                childColumns = "employeeId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"employeeId", "workDate"}, unique = true)})
public class WorkLog {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int employeeId;

    @NonNull
    private String workDate = "";

    private String checkInTime;

    private String checkOutTime;

    private double totalHours;

    @NonNull
    private String status = "";

    public WorkLog() {
    }

    @Ignore
    public WorkLog(int employeeId, @NonNull String workDate, String checkInTime, @NonNull String status) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.status = status;
        this.totalHours = 0;
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

    @NonNull
    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(@NonNull String workDate) {
        this.workDate = workDate;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }
}
