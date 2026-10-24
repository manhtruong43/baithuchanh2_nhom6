package com.ptit.employeesalary.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lưu thông tin tài khoản đăng nhập bằng SharedPreferences.
 * - Phiên đăng nhập hiện tại: accountId, username, role, employeeId.
 * - "Ghi nhớ đăng nhập": lần sau mở app tự vào thẳng màn hình chính
 *   và điền sẵn tên đăng nhập.
 */
public class SessionManager {

    private static final String PREF_NAME = "employee_salary_session";

    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMPLOYEE_ID = "employee_id";
    private static final String KEY_SAVED_USERNAME = "saved_username";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lưu phiên đăng nhập.
     * @param employeeId -1 nếu là Admin
     */
    public void saveSession(int accountId, String username, String role,
                            int employeeId, boolean remember) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putBoolean(KEY_REMEMBER, remember);
        editor.putInt(KEY_ACCOUNT_ID, accountId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putInt(KEY_EMPLOYEE_ID, employeeId);
        if (remember) {
            editor.putString(KEY_SAVED_USERNAME, username);
        } else {
            editor.remove(KEY_SAVED_USERNAME);
        }
        editor.apply();
    }

    /** Đang có phiên đăng nhập (trong lần mở app hiện tại) */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    /** Có tự động đăng nhập khi mở lại app không */
    public boolean shouldAutoLogin() {
        return isLoggedIn() && prefs.getBoolean(KEY_REMEMBER, false);
    }

    public int getAccountId() {
        return prefs.getInt(KEY_ACCOUNT_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public int getEmployeeId() {
        return prefs.getInt(KEY_EMPLOYEE_ID, -1);
    }

    public boolean isAdmin() {
        return AppConfig.ROLE_ADMIN.equals(getRole());
    }

    public String getSavedUsername() {
        return prefs.getString(KEY_SAVED_USERNAME, "");
    }

    /** Đăng xuất: xoá phiên nhưng vẫn giữ tên đăng nhập đã ghi nhớ */
    public void logout() {
        prefs.edit()
                .remove(KEY_LOGGED_IN)
                .remove(KEY_REMEMBER)
                .remove(KEY_ACCOUNT_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_ROLE)
                .remove(KEY_EMPLOYEE_ID)
                .apply();
    }
}
