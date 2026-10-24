package com.ptit.employeesalary.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Account;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.ui.home.AdminHomeActivity;
import com.ptit.employeesalary.ui.home.EmployeeHomeActivity;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.PasswordUtils;
import com.ptit.employeesalary.util.SessionManager;

/**
 * Đăng nhập 2 vai trò Admin / Nhân viên.
 * Thông tin đăng nhập được lưu bằng SharedPreferences (SessionManager).
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private CheckBox chkRemember;

    private SessionManager session;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        db = AppDatabase.getInstance(this);

        // Đã ghi nhớ đăng nhập -> vào thẳng màn hình chính
        if (session.shouldAutoLogin()) {
            goToHome(session.getRole());
            return;
        }

        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        chkRemember = findViewById(R.id.chkRemember);
        Button btnLogin = findViewById(R.id.btnLogin);

        String savedUsername = session.getSavedUsername();
        if (!TextUtils.isEmpty(savedUsername)) {
            edtUsername.setText(savedUsername);
            chkRemember.setChecked(true);
            edtPassword.requestFocus();
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Vui lòng nhập tên đăng nhập");
            edtUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        Account account = db.accountDao().login(username, PasswordUtils.hash(password));
        if (account == null) {
            Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        int employeeId = -1;
        if (AppConfig.ROLE_EMPLOYEE.equals(account.getRole())) {
            Employee employee = db.employeeDao().findByAccountId(account.getId());
            if (employee == null) {
                Toast.makeText(this, "Tài khoản chưa gắn với nhân viên nào", Toast.LENGTH_SHORT).show();
                return;
            }
            employeeId = employee.getId();
        }

        session.saveSession(account.getId(), account.getUsername(), account.getRole(),
                employeeId, chkRemember.isChecked());
        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        goToHome(account.getRole());
    }

    private void goToHome(String role) {
        Intent intent;
        if (AppConfig.ROLE_ADMIN.equals(role)) {
            intent = new Intent(this, AdminHomeActivity.class);
        } else {
            intent = new Intent(this, EmployeeHomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
