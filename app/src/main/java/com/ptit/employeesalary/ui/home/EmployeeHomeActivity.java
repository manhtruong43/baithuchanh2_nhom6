package com.ptit.employeesalary.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.ui.auth.LoginActivity;
import com.ptit.employeesalary.util.DateTimeUtils;
import com.ptit.employeesalary.util.ScreenNavigator;
import com.ptit.employeesalary.util.SessionManager;

/**
 * Màn hình chính của Nhân viên: chấm công, lịch sử, lương của tôi, đăng xuất.
 */
public class EmployeeHomeActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView tvWelcome;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn() || session.isAdmin() || session.getEmployeeId() < 0) {
            backToLogin();
            return;
        }
        setContentView(R.layout.activity_employee_home);
        setTitle("Trang nhân viên");

        tvWelcome = findViewById(R.id.tvWelcome);
        tvInfo = findViewById(R.id.tvInfo);

        findViewById(R.id.cardAttendance).setOnClickListener(v ->
                ScreenNavigator.open(this, ScreenNavigator.ATTENDANCE));
        findViewById(R.id.cardHistory).setOnClickListener(v ->
                ScreenNavigator.open(this, ScreenNavigator.ATTENDANCE_HISTORY));
        findViewById(R.id.cardMySalary).setOnClickListener(v ->
                ScreenNavigator.open(this, ScreenNavigator.MY_SALARY));
        findViewById(R.id.cardLogout).setOnClickListener(v -> confirmLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvWelcome == null) {
            return;
        }
        Employee employee = AppDatabase.getInstance(this).employeeDao()
                .findById(session.getEmployeeId());
        if (employee == null) {
            // Nhân viên đã bị Admin xoá
            session.logout();
            backToLogin();
            return;
        }
        tvWelcome.setText("Xin chào, " + employee.getFullname());
        tvInfo.setText("Mã NV: " + employee.getCode()
                + "\nLương theo giờ: " + DateTimeUtils.formatMoney(employee.getHourlyRate()));
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    session.logout();
                    backToLogin();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void backToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
