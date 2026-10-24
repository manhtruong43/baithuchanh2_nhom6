package com.ptit.employeesalary.util;

/**
 * Các hằng số dùng chung toàn app.
 */
public final class AppConfig {

    private AppConfig() {
    }

    // ----- Vai trò -----
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    // ----- Trạng thái WorkLog -----
    public static final String WORK_CHECKED_IN = "CHECKED_IN";   // đã check-in, chưa check-out
    public static final String WORK_COMPLETED = "COMPLETED";     // đủ check-in + check-out
    public static final String WORK_INCOMPLETE = "INCOMPLETE";   // qua ngày mà không check-out

    // ----- Trạng thái Salary -----
    public static final String SALARY_PENDING = "PENDING";       // chưa thanh toán
    public static final String SALARY_PAID = "PAID";             // đã thanh toán

    // ----- Quy định tính lương -----
    /** Số giờ làm chuẩn mỗi ngày, vượt quá tính là tăng ca */
    public static final double STANDARD_HOURS_PER_DAY = 8.0;
    /** Hệ số lương tăng ca (150%) */
    public static final double OVERTIME_RATE = 1.5;
}
