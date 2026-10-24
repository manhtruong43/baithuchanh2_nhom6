package com.ptit.employeesalary.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ptit.employeesalary.data.entity.Salary;
import com.ptit.employeesalary.data.model.SalaryWithEmployee;

import java.util.List;

@Dao
public interface SalaryDao {

    @Insert
    long insert(Salary salary);

    @Update
    void update(Salary salary);

    @Delete
    void delete(Salary salary);

    @Query("SELECT * FROM salaries WHERE employeeId = :employeeId AND month = :month AND year = :year LIMIT 1")
    Salary findByEmployeeAndMonth(int employeeId, int month, int year);

    @Query("SELECT * FROM salaries WHERE id = :id LIMIT 1")
    Salary findById(int id);

    /** Lịch sử lương của 1 nhân viên, mới nhất trước */
    @Query("SELECT * FROM salaries WHERE employeeId = :employeeId ORDER BY year DESC, month DESC")
    List<Salary> getByEmployee(int employeeId);

    /** Bảng lương cả công ty trong 1 tháng (kèm mã, tên, lương giờ) */
    @Query("SELECT s.*, e.code AS employeeCode, e.fullname AS employeeName, e.hourlyRate AS hourlyRate "
            + "FROM salaries s INNER JOIN employees e ON s.employeeId = e.id "
            + "WHERE s.month = :month AND s.year = :year ORDER BY e.code")
    List<SalaryWithEmployee> getByMonthWithEmployee(int month, int year);

    @Query("DELETE FROM salaries WHERE employeeId = :employeeId")
    void deleteByEmployee(int employeeId);
}
