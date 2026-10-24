package com.ptit.employeesalary.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ptit.employeesalary.data.entity.Account;

@Dao
public interface AccountDao {

    @Insert
    long insert(Account account);

    @Update
    void update(Account account);

    @Query("SELECT * FROM accounts WHERE username = :username AND password = :passwordHash LIMIT 1")
    Account login(String username, String passwordHash);

    @Query("SELECT * FROM accounts WHERE username = :username LIMIT 1")
    Account findByUsername(String username);

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    Account findById(int id);

    @Query("DELETE FROM accounts WHERE id = :id")
    void deleteById(int id);
}
