package com.ptit.employeesalary.logic;

import android.content.Context;

import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.data.entity.Salary;
import com.ptit.employeesalary.data.entity.WorkLog;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.DateTimeUtils;

import java.util.List;

/**
 * Tính lương tháng tự động.
 *
 * Quy tắc:
 *  - Chỉ tính các ngày chấm công COMPLETED (đủ check-in, check-out).
 *  - Mỗi ngày: giờ vượt quá STANDARD_HOURS_PER_DAY (8h) là giờ tăng ca.
 *  - Lương = giờ thường x lương giờ + giờ tăng ca x lương giờ x OVERTIME_RATE (1.5).
 *  - Bảng lương đã PAID thì không tính lại (khoá sổ).
 */
public class SalaryCalculator {

    private final AppDatabase db;

    public SalaryCalculator(Context context) {
        db = AppDatabase.getInstance(context);
    }

    /** Giờ tăng ca của 1 ngày */
    public static double overtimeOfDay(double hoursInDay) {
        return Math.max(0, hoursInDay - AppConfig.STANDARD_HOURS_PER_DAY);
    }

    /** Công thức tính tổng lương */
    public static double computeSalary(double totalHours, double overtimeHours, double hourlyRate) {
        double regularHours = totalHours - overtimeHours;
        return regularHours * hourlyRate + overtimeHours * hourlyRate * AppConfig.OVERTIME_RATE;
    }

    /**
     * Tính (hoặc tính lại) lương 1 nhân viên trong tháng và lưu vào bảng salaries.
     * @return bản ghi lương sau khi lưu
     */
    public Salary calculateForEmployee(Employee employee, int month, int year) {
        Salary existing = db.salaryDao().findByEmployeeAndMonth(employee.getId(), month, year);
        if (existing != null && AppConfig.SALARY_PAID.equals(existing.getStatus())) {
            return existing;   // đã thanh toán -> giữ nguyên
        }

        String prefix = DateTimeUtils.monthPrefix(month, year);
        List<WorkLog> logs = db.workLogDao().getCompletedByMonth(employee.getId(), prefix);

        double totalHours = 0;
        double overtimeHours = 0;
        for (WorkLog log : logs) {
            totalHours += log.getTotalHours();
            overtimeHours += overtimeOfDay(log.getTotalHours());
        }
        totalHours = DateTimeUtils.round2(totalHours);
        overtimeHours = DateTimeUtils.round2(overtimeHours);
        double totalSalary = Math.round(computeSalary(totalHours, overtimeHours, employee.getHourlyRate()));

        Salary salary = existing != null ? existing : new Salary();
        salary.setEmployeeId(employee.getId());
        salary.setMonth(month);
        salary.setYear(year);
        salary.setTotalHours(totalHours);
        salary.setOvertimeHours(overtimeHours);
        salary.setTotalSalary(totalSalary);
        salary.setStatus(AppConfig.SALARY_PENDING);

        if (existing != null) {
            db.salaryDao().update(salary);
        } else {
            long id = db.salaryDao().insert(salary);
            salary.setId((int) id);
        }
        return salary;
    }

    /** Tính lương cho toàn bộ nhân viên trong tháng */
    public void calculateAll(int month, int year) {
        db.workLogDao().markIncompleteBefore(DateTimeUtils.today());
        db.runInTransaction(() -> {
            for (Employee e : db.employeeDao().getAll()) {
                calculateForEmployee(e, month, year);
            }
        });
    }
}
