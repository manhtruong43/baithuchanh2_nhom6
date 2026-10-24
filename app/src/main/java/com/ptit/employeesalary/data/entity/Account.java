package com.ptit.employeesalary.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Bảng Account(id, username, password, role)
 * role: ADMIN hoặc EMPLOYEE (xem AppConfig).
 */
@Entity(tableName = "accounts",
        indices = {@Index(value = {"username"}, unique = true)})
public class Account {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String username = "";

    @NonNull
    private String password = "";   // lưu dạng SHA-256

    @NonNull
    private String role = "";

    public Account() {
    }

    @Ignore
    public Account(@NonNull String username, @NonNull String password, @NonNull String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @NonNull
    public String getRole() {
        return role;
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }
}
