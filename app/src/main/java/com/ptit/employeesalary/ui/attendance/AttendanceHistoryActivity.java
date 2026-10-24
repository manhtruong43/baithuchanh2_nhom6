package com.ptit.employeesalary.ui.attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.entity.WorkLog;
import com.ptit.employeesalary.logic.AttendanceService;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.DateTimeUtils;
import com.ptit.employeesalary.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhân viên: xem lịch sử chấm công từng ngày và tổng hợp giờ làm theo tháng.
 */
public class AttendanceHistoryActivity extends AppCompatActivity {

    private AttendanceService attendanceService;
    private int employeeId;
    private int month;
    private int year;

    private TextView tvMonth;
    private TextView tvWorkedDays;
    private TextView tvTotalHours;
    private TextView tvOvertime;
    private TextView tvIncomplete;
    private TextView tvEmpty;
    private ListView lvLogs;
    private Button btnNextMonth;

    private final List<WorkLog> logs = new ArrayList<>();
    private WorkLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);
        setTitle("Lịch sử chấm công");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        attendanceService = new AttendanceService(this);
        employeeId = new SessionManager(this).getEmployeeId();
        month = DateTimeUtils.currentMonth();
        year = DateTimeUtils.currentYear();

        tvMonth = findViewById(R.id.tvMonth);
        tvWorkedDays = findViewById(R.id.tvWorkedDays);
        tvTotalHours = findViewById(R.id.tvTotalHours);
        tvOvertime = findViewById(R.id.tvOvertime);
        tvIncomplete = findViewById(R.id.tvIncomplete);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvLogs = findViewById(R.id.lvLogs);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        adapter = new WorkLogAdapter();
        lvLogs.setAdapter(adapter);

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

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
        loadData();
    }

    private boolean isFuture(int m, int y) {
        int curYear = DateTimeUtils.currentYear();
        int curMonth = DateTimeUtils.currentMonth();
        return y > curYear || (y == curYear && m > curMonth);
    }

    private void loadData() {
        tvMonth.setText(DateTimeUtils.monthLabel(month, year));
        btnNextMonth.setEnabled(!isFuture(month == 12 ? 1 : month + 1, month == 12 ? year + 1 : year));

        logs.clear();
        logs.addAll(attendanceService.getLogsOfMonth(employeeId, month, year));
        adapter.notifyDataSetChanged();

        AttendanceService.MonthlySummary sum = AttendanceService.summarize(logs);
        tvWorkedDays.setText(sum.workedDays + " ngày");
        tvTotalHours.setText(DateTimeUtils.formatHours(sum.totalHours));
        tvOvertime.setText(DateTimeUtils.formatHours(sum.overtimeHours));
        tvIncomplete.setText(sum.incompleteDays + " ngày");

        boolean empty = logs.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        lvLogs.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    /** Adapter 1 dòng chấm công */
    private class WorkLogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return logs.size();
        }

        @Override
        public WorkLog getItem(int position) {
            return logs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return logs.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(AttendanceHistoryActivity.this)
                        .inflate(R.layout.item_work_log, parent, false);
            }
            WorkLog log = getItem(position);

            ((TextView) view.findViewById(R.id.tvDate)).setText(
                    DateTimeUtils.dayOfWeek(log.getWorkDate()) + ", "
                            + DateTimeUtils.toDisplayDate(log.getWorkDate()));

            String in = log.getCheckInTime() == null ? "--:--:--" : log.getCheckInTime();
            String out = log.getCheckOutTime() == null ? "--:--:--" : log.getCheckOutTime();
            ((TextView) view.findViewById(R.id.tvTimes)).setText("Vào " + in + "  →  Ra " + out);

            TextView tvHours = view.findViewById(R.id.tvLogHours);
            TextView tvStatus = view.findViewById(R.id.tvLogStatus);
            String status = log.getStatus();
            if (AppConfig.WORK_COMPLETED.equals(status)) {
                double ot = Math.max(0, log.getTotalHours() - AppConfig.STANDARD_HOURS_PER_DAY);
                tvHours.setText(DateTimeUtils.formatHours(log.getTotalHours()));
                tvStatus.setText(ot > 0
                        ? "Tăng ca " + DateTimeUtils.formatHours(DateTimeUtils.round2(ot))
                        : "Hoàn thành");
                tvStatus.setTextColor(ContextCompat.getColor(AttendanceHistoryActivity.this,
                        ot > 0 ? R.color.accent : R.color.success));
            } else if (AppConfig.WORK_CHECKED_IN.equals(status)) {
                tvHours.setText("—");
                tvStatus.setText("Đang làm việc");
                tvStatus.setTextColor(ContextCompat.getColor(AttendanceHistoryActivity.this,
                        R.color.warning));
            } else {
                tvHours.setText("—");
                tvStatus.setText("Thiếu check-out");
                tvStatus.setTextColor(ContextCompat.getColor(AttendanceHistoryActivity.this,
                        R.color.danger));
            }
            return view;
        }
    }
}
