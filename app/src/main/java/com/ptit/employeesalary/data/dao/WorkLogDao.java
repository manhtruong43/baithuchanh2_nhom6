package com.ptit.employeesalary.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ptit.employeesalary.data.entity.WorkLog;
import com.ptit.employeesalary.util.AppConfig;

import java.util.List;

@Dao
public interface WorkLogDao {

    @Insert
    long insert(WorkLog log);

    @Update
    void update(WorkLog log);

    /** Bản ghi chấm công của 1 nhân viên trong 1 ngày */
    @Query("SELECT * FROM work_logs WHERE employeeId = :employeeId AND workDate = :workDate LIMIT 1")
    WorkLog findByDate(int employeeId, String workDate);

    /** Tất cả bản ghi trong tháng, monthPrefix dạng yyyy-MM */
    @Query("SELECT * FROM work_logs WHERE employeeId = :employeeId "
            + "AND workDate LIKE :monthPrefix || '%' ORDER BY workDate DESC")
    List<WorkLog> getByMonth(int employeeId, String monthPrefix);

    /** Chỉ các bản ghi đã hoàn chỉnh (dùng để tính lương) */
    @Query("SELECT * FROM work_logs WHERE employeeId = :employeeId "
            + "AND workDate LIKE :monthPrefix || '%' "
            + "AND status = '" + AppConfig.WORK_COMPLETED + "' ORDER BY workDate")
    List<WorkLog> getCompletedByMonth(int employeeId, String monthPrefix);

    /** Số bản ghi chưa hoàn chỉnh (chưa check-out / bị đánh dấu thiếu) trong tháng */
    @Query("SELECT COUNT(*) FROM work_logs WHERE employeeId = :employeeId "
            + "AND workDate LIKE :monthPrefix || '%' "
            + "AND status != '" + AppConfig.WORK_COMPLETED + "'")
    int countUnfinishedByMonth(int employeeId, String monthPrefix);

    /** Đánh dấu INCOMPLETE cho các ngày trước hôm nay mà vẫn chưa check-out */
    @Query("UPDATE work_logs SET status = '" + AppConfig.WORK_INCOMPLETE + "' "
            + "WHERE status = '" + AppConfig.WORK_CHECKED_IN + "' AND workDate < :today")
    int markIncompleteBefore(String today);

    @Query("DELETE FROM work_logs WHERE employeeId = :employeeId")
    void deleteByEmployee(int employeeId);
}
