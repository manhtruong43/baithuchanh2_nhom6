package com.ptit.employeesalary.util;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/**
 * Mở màn hình theo tên lớp.
 * Lý do: màn hình chính (TV1) gọi tới màn hình của TV2, TV3 nhưng không import
 * trực tiếp -> nhánh của từng thành viên vẫn build được khi chưa merge code của người khác.
 */
public final class ScreenNavigator {

    private static final String BASE = "com.ptit.employeesalary.ui.";

    // TV2
    public static final String EMPLOYEE_LIST = BASE + "employee.EmployeeListActivity";
    public static final String ADMIN_SALARY = BASE + "salary.AdminSalaryActivity";

    // TV3
    public static final String ATTENDANCE = BASE + "attendance.AttendanceActivity";
    public static final String ATTENDANCE_HISTORY = BASE + "attendance.AttendanceHistoryActivity";
    public static final String MY_SALARY = BASE + "attendance.MySalaryActivity";

    private ScreenNavigator() {
    }

    public static void open(Activity from, String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            Toast.makeText(from, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(from, className);
        from.startActivity(intent);
    }
}
