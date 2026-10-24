package com.ptit.employeesalary.ui.salary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Salary;
import com.ptit.employeesalary.data.model.SalaryWithEmployee;
import com.ptit.employeesalary.logic.SalaryCalculator;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin: xem và quản lý bảng lương theo tháng.
 * - Mở màn hình / đổi tháng => tự động tính lương cho các bảng chưa thanh toán.
 * - Chạm 1 dòng: xem chi tiết, đánh dấu đã trả / chưa trả, tính lại, xoá.
 */
public class AdminSalaryActivity extends AppCompatActivity {

    private AppDatabase db;
    private SalaryCalculator calculator;

    private int month;
    private int year;

    private TextView tvMonth;
    private TextView tvTotalFund;
    private TextView tvPaidCount;
    private TextView tvEmpty;
    private ListView lvSalaries;
    private Button btnNextMonth;

    private final List<SalaryWithEmployee> items = new ArrayList<>();
    private SalaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_salary);
        setTitle("Quản lý bảng lương");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        calculator = new SalaryCalculator(this);
        month = DateTimeUtils.currentMonth();
        year = DateTimeUtils.currentYear();

        tvMonth = findViewById(R.id.tvMonth);
        tvTotalFund = findViewById(R.id.tvTotalFund);
        tvPaidCount = findViewById(R.id.tvPaidCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvSalaries = findViewById(R.id.lvSalaries);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        adapter = new SalaryAdapter();
        lvSalaries.setAdapter(adapter);
        lvSalaries.setOnItemClickListener((p, v, position, id) -> showDetail(items.get(position)));

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));
        findViewById(R.id.btnRecalculate).setOnClickListener(v -> {
            calculator.calculateAll(month, year);
            loadData();
            Toast.makeText(this, "Đã tính lại lương " + DateTimeUtils.monthLabel(month, year),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateAndLoad();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Đổi tháng, không cho chọn tháng tương lai */
    private void changeMonth(int delta) {
        int newMonth = month + delta;
        int newYear = year;
        if (newMonth < 1) {
            newMonth = 12;
            newYear--;
        } else if (newMonth > 12) {
            newMonth = 1;
            newYear++;
        }
        if (isFuture(newMonth, newYear)) {
            return;
        }
        month = newMonth;
        year = newYear;
        calculateAndLoad();
    }

    private boolean isFuture(int m, int y) {
        int curYear = DateTimeUtils.currentYear();
        int curMonth = DateTimeUtils.currentMonth();
        return y > curYear || (y == curYear && m > curMonth);
    }

    private void calculateAndLoad() {
        calculator.calculateAll(month, year);
        loadData();
    }

    private void loadData() {
        tvMonth.setText(DateTimeUtils.monthLabel(month, year));
        btnNextMonth.setEnabled(!isFuture(month == 12 ? 1 : month + 1, month == 12 ? year + 1 : year));

        items.clear();
        items.addAll(db.salaryDao().getByMonthWithEmployee(month, year));
        adapter.notifyDataSetChanged();

        double fund = 0;
        int paid = 0;
        for (SalaryWithEmployee s : items) {
            fund += s.salary.getTotalSalary();
            if (AppConfig.SALARY_PAID.equals(s.salary.getStatus())) {
                paid++;
            }
        }
        tvTotalFund.setText(DateTimeUtils.formatMoney(fund));
        tvPaidCount.setText(paid + " / " + items.size());

        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        lvSalaries.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showDetail(SalaryWithEmployee item) {
        Salary s = item.salary;
        double regular = s.getTotalHours() - s.getOvertimeHours();
        int unfinished = db.workLogDao().countUnfinishedByMonth(s.getEmployeeId(),
                DateTimeUtils.monthPrefix(s.getMonth(), s.getYear()));
        boolean paid = AppConfig.SALARY_PAID.equals(s.getStatus());

        String message = "Nhân viên: " + item.employeeName + " (" + item.employeeCode + ")"
                + "\nKỳ lương: " + DateTimeUtils.monthLabel(s.getMonth(), s.getYear())
                + "\nLương theo giờ: " + DateTimeUtils.formatMoney(item.hourlyRate)
                + "\n\nTổng giờ làm: " + DateTimeUtils.formatHours(s.getTotalHours())
                + "\n• Giờ thường: " + DateTimeUtils.formatHours(regular)
                + "\n• Giờ tăng ca: " + DateTimeUtils.formatHours(s.getOvertimeHours())
                + " (x" + AppConfig.OVERTIME_RATE + ")"
                + "\n\nTổng lương: " + DateTimeUtils.formatMoney(s.getTotalSalary())
                + "\nTrạng thái: " + (paid ? "Đã thanh toán" : "Chưa thanh toán");
        if (unfinished > 0) {
            message += "\n\n⚠ Có " + unfinished + " ngày chấm công chưa hoàn chỉnh"
                    + " (không được tính vào lương).";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Chi tiết bảng lương")
                .setMessage(message)
                .setNegativeButton("Đóng", null)
                .setNeutralButton("Xoá", (d, w) -> confirmDelete(s));

        if (paid) {
            builder.setPositiveButton("Huỷ thanh toán", (d, w) -> {
                s.setStatus(AppConfig.SALARY_PENDING);
                db.salaryDao().update(s);
                calculateAndLoad();
            });
        } else {
            builder.setPositiveButton("Đã thanh toán", (d, w) -> {
                s.setStatus(AppConfig.SALARY_PAID);
                db.salaryDao().update(s);
                loadData();
                Toast.makeText(this, "Đã đánh dấu thanh toán", Toast.LENGTH_SHORT).show();
            });
        }
        builder.show();
    }

    private void confirmDelete(Salary s) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá bảng lương")
                .setMessage("Xoá bảng lương này? Lần tính lương kế tiếp sẽ tạo lại từ dữ liệu chấm công.")
                .setPositiveButton("Xoá", (d, w) -> {
                    db.salaryDao().delete(s);
                    loadData();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /** Adapter 1 dòng bảng lương */
    private class SalaryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public SalaryWithEmployee getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).salary.getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(AdminSalaryActivity.this)
                        .inflate(R.layout.item_admin_salary, parent, false);
            }
            SalaryWithEmployee item = getItem(position);
            Salary s = item.salary;
            boolean paid = AppConfig.SALARY_PAID.equals(s.getStatus());

            ((TextView) view.findViewById(R.id.tvEmployee)).setText(
                    item.employeeCode + " - " + item.employeeName);
            TextView tvStatus = view.findViewById(R.id.tvStatus);
            tvStatus.setText(paid ? "ĐÃ TRẢ" : "CHƯA TRẢ");
            tvStatus.setTextColor(ContextCompat.getColor(AdminSalaryActivity.this,
                    paid ? R.color.success : R.color.warning));
            ((TextView) view.findViewById(R.id.tvHours)).setText(
                    "Tổng: " + DateTimeUtils.formatHours(s.getTotalHours())
                            + "  •  Tăng ca: " + DateTimeUtils.formatHours(s.getOvertimeHours()));
            ((TextView) view.findViewById(R.id.tvTotal)).setText(
                    DateTimeUtils.formatMoney(s.getTotalSalary()));
            return view;
        }
    }
}
