package com.ptit.employeesalary.logic;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.WorkLog;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.DateTimeUtils;

import java.util.List;

/**
 * Nghiệp vụ chấm công + kiểm tra dữ liệu:
 *  - Không cho check-in 2 lần trong 1 ngày.
 *  - Không cho check-out khi chưa check-in, đã check-out rồi, hoặc giờ ra không sau giờ vào.
 *  - Bản ghi những ngày trước mà chưa check-out => đánh dấu INCOMPLETE, không tính lương.
 *  - Tự tính tổng giờ làm khi check-out.
 */
public class AttendanceService {

    /** Kết quả 1 thao tác chấm công */
    public static class Result {
        public final boolean success;
        public final String message;

        Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    /** Tổng hợp giờ làm trong tháng */
    public static class MonthlySummary {
        public int workedDays;        // số ngày COMPLETED
        public int incompleteDays;    // số ngày chưa hoàn chỉnh
        public double totalHours;
        public double overtimeHours;

        public double regularHours() {
            return DateTimeUtils.round2(totalHours - overtimeHours);
        }
    }

    private final AppDatabase db;

    public AttendanceService(Context context) {
        db = AppDatabase.getInstance(context);
    }

    /** Đánh dấu các ngày cũ quên check-out là INCOMPLETE */
    public void markIncompletePastLogs() {
        db.workLogDao().markIncompleteBefore(DateTimeUtils.today());
    }

    public WorkLog getTodayLog(int employeeId) {
        return db.workLogDao().findByDate(employeeId, DateTimeUtils.today());
    }

    public Result checkIn(int employeeId) {
        markIncompletePastLogs();
        String today = DateTimeUtils.today();
        WorkLog existing = db.workLogDao().findByDate(employeeId, today);
        if (existing != null) {
            return new Result(false, "Bạn đã check-in hôm nay lúc " + existing.getCheckInTime()
                    + ". Không thể check-in lại.");
        }
        String now = DateTimeUtils.nowTime();
        WorkLog log = new WorkLog(employeeId, today, now, AppConfig.WORK_CHECKED_IN);
        try {
            db.workLogDao().insert(log);
        } catch (SQLiteConstraintException e) {
            // Chặn ở tầng DB (unique employeeId + workDate) phòng khi bấm 2 lần quá nhanh
            return new Result(false, "Bạn đã check-in hôm nay rồi.");
        }
        return new Result(true, "Check-in thành công lúc " + now);
    }

    public Result checkOut(int employeeId) {
        String today = DateTimeUtils.today();
        WorkLog log = db.workLogDao().findByDate(employeeId, today);
        if (log == null) {
            return new Result(false, "Bạn chưa check-in hôm nay nên không thể check-out.");
        }
        if (AppConfig.WORK_COMPLETED.equals(log.getStatus())) {
            return new Result(false, "Bạn đã check-out hôm nay lúc " + log.getCheckOutTime() + ".");
        }
        String now = DateTimeUtils.nowTime();
        double hours = DateTimeUtils.hoursBetween(log.getCheckInTime(), now);
        if (hours <= 0) {
            return new Result(false, "Giờ check-out (" + now + ") không hợp lệ: phải sau giờ check-in ("
                    + log.getCheckInTime() + ").");
        }
        log.setCheckOutTime(now);
        log.setTotalHours(hours);
        log.setStatus(AppConfig.WORK_COMPLETED);
        db.workLogDao().update(log);
        return new Result(true, "Check-out thành công lúc " + now
                + ". Hôm nay bạn làm " + DateTimeUtils.formatHours(hours) + ".");
    }

    public List<WorkLog> getLogsOfMonth(int employeeId, int month, int year) {
        markIncompletePastLogs();
        return db.workLogDao().getByMonth(employeeId, DateTimeUtils.monthPrefix(month, year));
    }

    /** Tổng hợp giờ làm, giờ tăng ca của 1 tháng từ danh sách bản ghi */
    public static MonthlySummary summarize(List<WorkLog> logs) {
        MonthlySummary sum = new MonthlySummary();
        for (WorkLog log : logs) {
            if (AppConfig.WORK_COMPLETED.equals(log.getStatus())) {
                sum.workedDays++;
                sum.totalHours += log.getTotalHours();
                sum.overtimeHours += Math.max(0, log.getTotalHours() - AppConfig.STANDARD_HOURS_PER_DAY);
            } else if (AppConfig.WORK_INCOMPLETE.equals(log.getStatus())) {
                sum.incompleteDays++;
            }
        }
        sum.totalHours = DateTimeUtils.round2(sum.totalHours);
        sum.overtimeHours = DateTimeUtils.round2(sum.overtimeHours);
        return sum;
    }
}
