package com.ptit.employeesalary.logic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Account;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.PasswordUtils;

/**
 * Nghiệp vụ quản lý nhân viên: kiểm tra dữ liệu, thêm, sửa, xoá.
 * Mỗi nhân viên đi kèm 1 tài khoản đăng nhập vai trò EMPLOYEE.
 */
public class EmployeeService {

    private final AppDatabase db;

    public EmployeeService(Context context) {
        db = AppDatabase.getInstance(context);
    }

    /**
     * Kiểm tra dữ liệu nhập.
     * @param editingId id nhân viên đang sửa, 0 nếu thêm mới
     * @param password  khi sửa có thể để trống (giữ mật khẩu cũ)
     * @return thông báo lỗi, null nếu hợp lệ
     */
    public String validate(int editingId, String code, String fullname, String email,
                           String phone, String hourlyRateText, String username, String password) {
        if (TextUtils.isEmpty(code)) {
            return "Vui lòng nhập mã nhân viên";
        }
        if (TextUtils.isEmpty(fullname)) {
            return "Vui lòng nhập họ tên";
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Email không hợp lệ";
        }
        if (TextUtils.isEmpty(phone) || !phone.matches("^0\\d{9}$")) {
            return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0";
        }
        double rate;
        try {
            rate = Double.parseDouble(hourlyRateText);
        } catch (NumberFormatException e) {
            return "Lương theo giờ phải là số";
        }
        if (rate <= 0) {
            return "Lương theo giờ phải lớn hơn 0";
        }
        if (TextUtils.isEmpty(username) || username.length() < 3 || username.contains(" ")) {
            return "Tên đăng nhập tối thiểu 3 ký tự, không chứa khoảng trắng";
        }
        boolean isAdd = editingId == 0;
        if (isAdd && TextUtils.isEmpty(password)) {
            return "Vui lòng nhập mật khẩu";
        }
        if (!TextUtils.isEmpty(password) && password.length() < 6) {
            return "Mật khẩu tối thiểu 6 ký tự";
        }

        // ----- Kiểm tra trùng -----
        Employee sameCode = db.employeeDao().findByCode(code);
        if (sameCode != null && sameCode.getId() != editingId) {
            return "Mã nhân viên đã tồn tại";
        }
        Employee sameEmail = db.employeeDao().findByEmail(email);
        if (sameEmail != null && sameEmail.getId() != editingId) {
            return "Email đã được sử dụng";
        }
        Account sameUsername = db.accountDao().findByUsername(username);
        if (sameUsername != null) {
            if (isAdd) {
                return "Tên đăng nhập đã tồn tại";
            }
            Employee current = db.employeeDao().findById(editingId);
            if (current == null || sameUsername.getId() != current.getAccountId()) {
                return "Tên đăng nhập đã tồn tại";
            }
        }
        return null;
    }

    /** Thêm nhân viên mới kèm tài khoản đăng nhập */
    public void add(String code, String fullname, String email, String phone,
                    double hourlyRate, String username, String password) {
        db.runInTransaction(() -> {
            Account account = new Account(username, PasswordUtils.hash(password), AppConfig.ROLE_EMPLOYEE);
            int accountId = (int) db.accountDao().insert(account);
            Employee employee = new Employee(code, fullname, email, phone, hourlyRate, accountId);
            db.employeeDao().insert(employee);
        });
    }

    /** Cập nhật thông tin nhân viên, đổi mật khẩu nếu password không rỗng */
    public void update(Employee employee, String username, String password) {
        db.runInTransaction(() -> {
            db.employeeDao().update(employee);
            Account account = db.accountDao().findById(employee.getAccountId());
            if (account != null) {
                account.setUsername(username);
                if (!TextUtils.isEmpty(password)) {
                    account.setPassword(PasswordUtils.hash(password));
                }
                db.accountDao().update(account);
            }
        });
    }

    /** Xoá nhân viên cùng toàn bộ chấm công, bảng lương và tài khoản */
    public void delete(Employee employee) {
        db.runInTransaction(() -> {
            db.workLogDao().deleteByEmployee(employee.getId());
            db.salaryDao().deleteByEmployee(employee.getId());
            db.employeeDao().delete(employee);
            db.accountDao().deleteById(employee.getAccountId());
        });
    }
}
