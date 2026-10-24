package com.ptit.employeesalary.ui.attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.data.entity.Salary;
import com.ptit.employeesalary.logic.AttendanceService;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.DateTimeUtils;
import com.ptit.employeesalary.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhân viên: xem lương tạm tính tháng hiện tại + chi tiết các bảng lương
 * (tổng giờ, giờ tăng ca, tổng lương, trạng thái thanh toán).
 */
public class MySalaryActivity extends AppCompatActivity {

    private AppDatabase db;
    private AttendanceService attendanceService;
    private Employee employee;

    private TextView tvEstimateTitle;
    private TextView tvEstimate;
    private TextView tvEstimateDetail;
    private TextView tvEmpty;
    private ListView lvSalaries;

    private final List<Salary> salaries = new ArrayList<>();
    private MySalaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_salary);
        setTitle("Lương của tôi");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        attendanceService = new AttendanceService(this);
        employee = db.employeeDao().findById(new SessionManager(this).getEmployeeId());
        if (employee == null) {
            finish();
            return;
        }

        tvEstimateTitle = findViewById(R.id.tvEstimateTitle);
        tvEstimate = findViewById(R.id.tvEstimate);
        tvEstimateDetail = findViewById(R.id.tvEstimateDetail);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvSalaries = findViewById(R.id.lvSalaries);

        adapter = new MySalaryAdapter();
        lvSalaries.setAdapter(adapter);
        lvSalaries.setOnItemClickListener((p, v, position, id) -> showDetail(salaries.get(position)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (employee == null) {
            return;
        }
        loadEstimate();
        loadSalaries();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Tạm tính lương tháng hiện tại từ dữ liệu chấm công */
    private void loadEstimate() {
        int month = DateTimeUtils.currentMonth();
        int year = DateTimeUtils.currentYear();
        AttendanceService.MonthlySummary sum = AttendanceService.summarize(
                attendanceService.getLogsOfMonth(employee.getId(), month, year));

        double rate = employee.getHourlyRate();
        double estimate = sum.regularHours() * rate
                + sum.overtimeHours * rate * AppConfig.OVERTIME_RATE;

        tvEstimateTitle.setText("Lương tạm tính " + DateTimeUtils.monthLabel(month, year).toLowerCase());
        tvEstimate.setText(DateTimeUtils.formatMoney(estimate));
        tvEstimateDetail.setText(sum.workedDays + " ngày công  •  "
                + DateTimeUtils.formatHours(sum.totalHours)
                + "  •  Tăng ca " + DateTimeUtils.formatHours(sum.overtimeHours)
                + "\nLương giờ: " + DateTimeUtils.formatMoney(rate)
                + "  •  Hệ số tăng ca: x" + AppConfig.OVERTIME_RATE);
    }

    private void loadSalaries() {
        salaries.clear();
        salaries.addAll(db.salaryDao().getByEmployee(employee.getId()));
        adapter.notifyDataSetChanged();

        boolean empty = salaries.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        lvSalaries.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showDetail(Salary s) {
        double regular = DateTimeUtils.round2(s.getTotalHours() - s.getOvertimeHours());
        boolean paid = AppConfig.SALARY_PAID.equals(s.getStatus());
        String message = "Kỳ lương: " + DateTimeUtils.monthLabel(s.getMonth(), s.getYear())
                + "\n\nTổng giờ làm: " + DateTimeUtils.formatHours(s.getTotalHours())
                + "\n• Giờ thường: " + DateTimeUtils.formatHours(regular)
                + "\n• Giờ tăng ca: " + DateTimeUtils.formatHours(s.getOvertimeHours())
                + " (x" + AppConfig.OVERTIME_RATE + ")"
                + "\n\nTổng lương: " + DateTimeUtils.formatMoney(s.getTotalSalary())
                + "\nTrạng thái: " + (paid ? "Đã thanh toán" : "Chưa thanh toán");
        new AlertDialog.Builder(this)
                .setTitle("Chi tiết lương")
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show();
    }

    /** Adapter 1 dòng bảng lương */
    private class MySalaryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return salaries.size();
        }

        @Override
        public Salary getItem(int position) {
            return salaries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return salaries.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(MySalaryActivity.this)
                        .inflate(R.layout.item_my_salary, parent, false);
            }
            Salary s = getItem(position);
            boolean paid = AppConfig.SALARY_PAID.equals(s.getStatus());

            ((TextView) view.findViewById(R.id.tvPeriod)).setText(
                    DateTimeUtils.monthLabel(s.getMonth(), s.getYear()));
            TextView tvStatus = view.findViewById(R.id.tvSalaryStatus);
            tvStatus.setText(paid ? "ĐÃ TRẢ" : "CHƯA TRẢ");
            tvStatus.setTextColor(ContextCompat.getColor(MySalaryActivity.this,
                    paid ? R.color.success : R.color.warning));
            ((TextView) view.findViewById(R.id.tvSalaryHours)).setText(
                    "Tổng: " + DateTimeUtils.formatHours(s.getTotalHours())
                            + "  •  Tăng ca: " + DateTimeUtils.formatHours(s.getOvertimeHours()));
            ((TextView) view.findViewById(R.id.tvSalaryTotal)).setText(
                    DateTimeUtils.formatMoney(s.getTotalSalary()));
            return view;
        }
    }
}
