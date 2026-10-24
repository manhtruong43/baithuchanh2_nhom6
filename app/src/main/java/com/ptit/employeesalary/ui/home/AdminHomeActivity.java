package com.ptit.employeesalary.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.ui.auth.LoginActivity;
import com.ptit.employeesalary.util.ScreenNavigator;
import com.ptit.employeesalary.util.SessionManager;

/**
 * Màn hình chính của Admin: quản lý nhân viên, quản lý lương, đăng xuất.
 */
public class AdminHomeActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView tvEmployeeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn() || !session.isAdmin()) {
            backToLogin();
            return;
        }
        setContentView(R.layout.activity_admin_home);
        setTitle("Trang quản trị");

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvEmployeeCount = findViewById(R.id.tvEmployeeCount);
        tvWelcome.setText("Xin chào, " + session.getUsername());

        findViewById(R.id.cardEmployees).setOnClickListener(v ->
                ScreenNavigator.open(this, ScreenNavigator.EMPLOYEE_LIST));
        findViewById(R.id.cardSalary).setOnClickListener(v ->
                ScreenNavigator.open(this, ScreenNavigator.ADMIN_SALARY));
        findViewById(R.id.cardLogout).setOnClickListener(v -> confirmLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvEmployeeCount != null) {
            int count = AppDatabase.getInstance(this).employeeDao().count();
            tvEmployeeCount.setText("Tổng số nhân viên: " + count);
        }
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
