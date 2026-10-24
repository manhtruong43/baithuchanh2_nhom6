package com.ptit.employeesalary.data.model;

import androidx.room.Embedded;

import com.ptit.employeesalary.data.entity.Salary;

/**
 * Kết quả JOIN Salary + Employee để hiển thị danh sách lương cho Admin.
 */
public class SalaryWithEmployee {

    @Embedded
    public Salary salary;

    public String employeeCode;

    public String employeeName;

    public double hourlyRate;
}
