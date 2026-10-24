package com.ptit.employeesalary.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Bảng Employee(id, code, fullname, email, phone, hourlyRate, accountId)
 */
@Entity(tableName = "employees",
        foreignKeys = @ForeignKey(entity = Account.class,
                parentColumns = "id",
                childColumns = "accountId",
                onDelete = ForeignKey.CASCADE),
        indices = {
                @Index(value = {"code"}, unique = true),
                @Index(value = {"accountId"}, unique = true)
        })
public class Employee {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String code = "";

    @NonNull
    private String fullname = "";

    private String email;

    private String phone;

    private double hourlyRate;   // lương theo giờ (VNĐ)

    private int accountId;

    public Employee() {
    }

    @Ignore
    public Employee(@NonNull String code, @NonNull String fullname, String email,
                    String phone, double hourlyRate, int accountId) {
        this.code = code;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.hourlyRate = hourlyRate;
        this.accountId = accountId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    public void setCode(@NonNull String code) {
        this.code = code;
    }

    @NonNull
    public String getFullname() {
        return fullname;
    }

    public void setFullname(@NonNull String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
