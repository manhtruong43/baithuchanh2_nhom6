package com.ptit.employeesalary.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ptit.employeesalary.data.entity.Employee;

import java.util.List;

@Dao
public interface EmployeeDao {

    @Insert
    long insert(Employee employee);

    @Update
    void update(Employee employee);

    @Delete
    void delete(Employee employee);

    @Query("SELECT * FROM employees ORDER BY code")
    List<Employee> getAll();

    /** Tìm theo mã, họ tên, email hoặc số điện thoại */
    @Query("SELECT * FROM employees WHERE code LIKE '%' || :keyword || '%' "
            + "OR fullname LIKE '%' || :keyword || '%' "
            + "OR email LIKE '%' || :keyword || '%' "
            + "OR phone LIKE '%' || :keyword || '%' "
            + "ORDER BY code")
    List<Employee> search(String keyword);

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    Employee findById(int id);

    @Query("SELECT * FROM employees WHERE accountId = :accountId LIMIT 1")
    Employee findByAccountId(int accountId);

    @Query("SELECT * FROM employees WHERE code = :code LIMIT 1")
    Employee findByCode(String code);

    @Query("SELECT * FROM employees WHERE email = :email LIMIT 1")
    Employee findByEmail(String email);

    @Query("SELECT COUNT(*) FROM employees")
    int count();
}
