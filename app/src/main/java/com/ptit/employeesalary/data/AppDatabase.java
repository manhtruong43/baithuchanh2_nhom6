package com.ptit.employeesalary.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ptit.employeesalary.data.dao.AccountDao;
import com.ptit.employeesalary.data.dao.EmployeeDao;
import com.ptit.employeesalary.data.dao.SalaryDao;
import com.ptit.employeesalary.data.dao.WorkLogDao;
import com.ptit.employeesalary.data.entity.Account;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.data.entity.Salary;
import com.ptit.employeesalary.data.entity.WorkLog;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.DateTimeUtils;
import com.ptit.employeesalary.util.PasswordUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Room Database gồm 4 bảng: accounts, employees, work_logs, salaries.
 * Lần đầu tạo DB sẽ chèn sẵn dữ liệu mẫu để demo:
 *   admin / admin123   (Admin)
 *   nv01  / 123456     (Nhân viên Nguyễn Văn An)
 *   nv02  / 123456     (Nhân viên Trần Thị Bình)
 */
@Database(entities = {Account.class, Employee.class, WorkLog.class, Salary.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "employee_salary.db";
    private static volatile AppDatabase instance;

    public abstract AccountDao accountDao();

    public abstract EmployeeDao employeeDao();

    public abstract WorkLogDao workLogDao();

    public abstract SalaryDao salaryDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DB_NAME)
                            // App nhỏ, dữ liệu ít -> cho phép truy vấn trên main thread cho gọn
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .addCallback(new SeedCallback())
                            .build();
                }
            }
        }
        return instance;
    }

    /** Chèn dữ liệu mẫu khi DB được tạo lần đầu */
    private static class SeedCallback extends RoomDatabase.Callback {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            String adminPass = PasswordUtils.hash("admin123");
            String empPass = PasswordUtils.hash("123456");

            db.execSQL("INSERT INTO accounts(id, username, password, role) VALUES (1, 'admin', ?, ?)",
                    new Object[]{adminPass, AppConfig.ROLE_ADMIN});
            db.execSQL("INSERT INTO accounts(id, username, password, role) VALUES (2, 'nv01', ?, ?)",
                    new Object[]{empPass, AppConfig.ROLE_EMPLOYEE});
            db.execSQL("INSERT INTO accounts(id, username, password, role) VALUES (3, 'nv02', ?, ?)",
                    new Object[]{empPass, AppConfig.ROLE_EMPLOYEE});

            db.execSQL("INSERT INTO employees(id, code, fullname, email, phone, hourlyRate, accountId) "
                            + "VALUES (1, 'NV001', 'Nguyễn Văn An', 'an.nv@company.com', '0912345678', 50000, 2)");
            db.execSQL("INSERT INTO employees(id, code, fullname, email, phone, hourlyRate, accountId) "
                            + "VALUES (2, 'NV002', 'Trần Thị Bình', 'binh.tt@company.com', '0987654321', 45000, 3)");

            seedWorkLogs(db);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys = ON");
        }

        /**
         * Tháng trước: chấm công đủ các ngày trong tuần (có ngày tăng ca, 1 ngày thiếu check-out).
         * Tháng này: chấm công các ngày trong tuần trước hôm nay.
         */
        private void seedWorkLogs(SupportSQLiteDatabase db) {
            SimpleDateFormat f = new SimpleDateFormat(DateTimeUtils.DB_DATE, Locale.US);
            String today = DateTimeUtils.today();

            // ----- Tháng trước -----
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -1);
            c.set(Calendar.DAY_OF_MONTH, 1);
            int prevMonth = c.get(Calendar.MONTH);
            boolean incompleteAdded = false;
            while (c.get(Calendar.MONTH) == prevMonth) {
                int dow = c.get(Calendar.DAY_OF_WEEK);
                if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) {
                    String date = f.format(c.getTime());
                    // NV001: thứ 2, 4, 6 làm 08:00-18:30 (tăng ca 2.5h), các ngày khác 08:00-17:00
                    boolean overtimeDay = dow == Calendar.MONDAY || dow == Calendar.WEDNESDAY
                            || dow == Calendar.FRIDAY;
                    insertLog(db, 1, date, "08:00:00", overtimeDay ? "18:30:00" : "17:00:00");

                    // NV002: 08:30-16:30 mỗi ngày, có 1 ngày quên check-out
                    if (!incompleteAdded && c.get(Calendar.DAY_OF_MONTH) >= 10) {
                        db.execSQL("INSERT INTO work_logs(employeeId, workDate, checkInTime, checkOutTime, "
                                        + "totalHours, status) VALUES (2, ?, '08:30:00', NULL, 0, ?)",
                                new Object[]{date, AppConfig.WORK_INCOMPLETE});
                        incompleteAdded = true;
                    } else {
                        insertLog(db, 2, date, "08:30:00", "16:30:00");
                    }
                }
                c.add(Calendar.DAY_OF_MONTH, 1);
            }

            // ----- Tháng này (trước hôm nay) -----
            c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            int curMonth = c.get(Calendar.MONTH);
            while (c.get(Calendar.MONTH) == curMonth) {
                String date = f.format(c.getTime());
                if (date.compareTo(today) >= 0) {
                    break;
                }
                int dow = c.get(Calendar.DAY_OF_WEEK);
                if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) {
                    insertLog(db, 1, date, "08:00:00", "17:30:00");
                    insertLog(db, 2, date, "08:30:00", "16:30:00");
                }
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        private void insertLog(SupportSQLiteDatabase db, int employeeId, String date,
                               String in, String out) {
            double hours = DateTimeUtils.hoursBetween(in, out);
            db.execSQL("INSERT INTO work_logs(employeeId, workDate, checkInTime, checkOutTime, "
                            + "totalHours, status) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[]{employeeId, date, in, out, hours, AppConfig.WORK_COMPLETED});
        }
    }
}
