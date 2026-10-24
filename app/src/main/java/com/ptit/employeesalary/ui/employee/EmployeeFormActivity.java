package com.ptit.employeesalary.ui.employee;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Account;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.logic.EmployeeService;

import java.math.BigDecimal;

/**
 * Admin: thêm mới / cập nhật nhân viên (kèm lương theo giờ và tài khoản đăng nhập).
 * Truyền EXTRA_EMPLOYEE_ID để sửa, không truyền để thêm mới.
 */
public class EmployeeFormActivity extends AppCompatActivity {

    public static final String EXTRA_EMPLOYEE_ID = "employee_id";

    private EditText edtCode;
    private EditText edtFullname;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtHourlyRate;
    private EditText edtUsername;
    private EditText edtPassword;

    private EmployeeService employeeService;
    private Employee editingEmployee;   // null = thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_form);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        employeeService = new EmployeeService(this);
        AppDatabase db = AppDatabase.getInstance(this);

        edtCode = findViewById(R.id.edtCode);
        edtFullname = findViewById(R.id.edtFullname);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtHourlyRate = findViewById(R.id.edtHourlyRate);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        TextInputLayout tilPassword = findViewById(R.id.tilPassword);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        int employeeId = getIntent().getIntExtra(EXTRA_EMPLOYEE_ID, 0);
        if (employeeId > 0) {
            editingEmployee = db.employeeDao().findById(employeeId);
        }

        if (editingEmployee != null) {
            setTitle("Sửa nhân viên");
            edtCode.setText(editingEmployee.getCode());
            edtFullname.setText(editingEmployee.getFullname());
            edtEmail.setText(editingEmployee.getEmail());
            edtPhone.setText(editingEmployee.getPhone());
            edtHourlyRate.setText(formatRate(editingEmployee.getHourlyRate()));
            Account account = db.accountDao().findById(editingEmployee.getAccountId());
            if (account != null) {
                edtUsername.setText(account.getUsername());
            }
            tilPassword.setHint("Mật khẩu mới (để trống nếu không đổi)");
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
        } else {
            setTitle("Thêm nhân viên");
        }

        btnSave.setOnClickListener(v -> save());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void save() {
        String code = edtCode.getText().toString().trim().toUpperCase();
        String fullname = edtFullname.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String rateText = edtHourlyRate.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        int editingId = editingEmployee == null ? 0 : editingEmployee.getId();
        String error = employeeService.validate(editingId, code, fullname, email, phone,
                rateText, username, password);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            return;
        }
        double rate = Double.parseDouble(rateText);

        if (editingEmployee == null) {
            employeeService.add(code, fullname, email, phone, rate, username, password);
            Toast.makeText(this, "Đã thêm nhân viên", Toast.LENGTH_SHORT).show();
        } else {
            editingEmployee.setCode(code);
            editingEmployee.setFullname(fullname);
            editingEmployee.setEmail(email);
            editingEmployee.setPhone(phone);
            editingEmployee.setHourlyRate(rate);
            employeeService.update(editingEmployee, username, password);
            Toast.makeText(this, "Đã cập nhật nhân viên", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xoá nhân viên")
                .setMessage("Xoá " + editingEmployee.getFullname() + "?\n"
                        + "Toàn bộ chấm công, bảng lương và tài khoản cũng bị xoá.")
                .setPositiveButton("Xoá", (d, w) -> {
                    employeeService.delete(editingEmployee);
                    Toast.makeText(this, "Đã xoá nhân viên", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /** 50000.0 -> "50000" */
    private String formatRate(double rate) {
        return new BigDecimal(String.valueOf(rate)).stripTrailingZeros().toPlainString();
    }
}
