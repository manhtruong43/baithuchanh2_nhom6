package com.ptit.employeesalary.ui.attendance;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.entity.WorkLog;
import com.ptit.employeesalary.logic.AttendanceService;
import com.ptit.employeesalary.util.AppConfig;
import com.ptit.employeesalary.util.DateTimeUtils;
import com.ptit.employeesalary.util.SessionManager;

/**
 * Nhân viên: check-in / check-out trong ngày.
 * Giờ làm được tính tự động khi check-out.
 */
public class AttendanceActivity extends AppCompatActivity {

    private AttendanceService attendanceService;
    private int employeeId;

    private TextView tvToday;
    private TextView tvClock;
    private TextView tvStatus;
    private TextView tvCheckIn;
    private TextView tvCheckOut;
    private TextView tvHours;
    private Button btnCheckIn;
    private Button btnCheckOut;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            tvClock.setText(DateTimeUtils.nowTime());
            clockHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        setTitle("Chấm công");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        attendanceService = new AttendanceService(this);
        employeeId = new SessionManager(this).getEmployeeId();

        tvToday = findViewById(R.id.tvToday);
        tvClock = findViewById(R.id.tvClock);
        tvStatus = findViewById(R.id.tvStatus);
        tvCheckIn = findViewById(R.id.tvCheckIn);
        tvCheckOut = findViewById(R.id.tvCheckOut);
        tvHours = findViewById(R.id.tvHours);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);

        btnCheckIn.setOnClickListener(v -> {
            AttendanceService.Result r = attendanceService.checkIn(employeeId);
            showResult(r);
            refresh();
        });
        btnCheckOut.setOnClickListener(v -> confirmCheckOut());
    }

    @Override
    protected void onResume() {
        super.onResume();
        attendanceService.markIncompletePastLogs();
        refresh();
        clockHandler.post(clockTick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockTick);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void confirmCheckOut() {
        WorkLog log = attendanceService.getTodayLog(employeeId);
        if (log == null || !AppConfig.WORK_CHECKED_IN.equals(log.getStatus())) {
            // Không ở trạng thái đang làm -> gọi luôn để hiện thông báo lỗi kiểm tra dữ liệu
            showResult(attendanceService.checkOut(employeeId));
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Check-out")
                .setMessage("Xác nhận check-out lúc " + DateTimeUtils.nowTime()
                        + "?\nSau khi check-out sẽ không thể chấm công lại trong hôm nay.")
                .setPositiveButton("Check-out", (d, w) -> {
                    AttendanceService.Result r = attendanceService.checkOut(employeeId);
                    showResult(r);
                    refresh();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /** Cập nhật giao diện theo trạng thái chấm công hôm nay */
    private void refresh() {
        String today = DateTimeUtils.today();
        tvToday.setText(DateTimeUtils.dayOfWeek(today) + ", " + DateTimeUtils.toDisplayDate(today));

        WorkLog log = attendanceService.getTodayLog(employeeId);
        if (log == null) {
            tvStatus.setText("Chưa check-in hôm nay");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_sub));
            tvCheckIn.setText("--:--:--");
            tvCheckOut.setText("--:--:--");
            tvHours.setText(DateTimeUtils.formatHours(0));
            setActive(btnCheckIn, true);
            setActive(btnCheckOut, false);
        } else if (AppConfig.WORK_CHECKED_IN.equals(log.getStatus())) {
            tvStatus.setText("Đang làm việc");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.warning));
            tvCheckIn.setText(log.getCheckInTime());
            tvCheckOut.setText("--:--:--");
            tvHours.setText(DateTimeUtils.formatHours(0));
            setActive(btnCheckIn, false);
            setActive(btnCheckOut, true);
        } else {
            tvStatus.setText("Đã hoàn thành chấm công hôm nay");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
            tvCheckIn.setText(log.getCheckInTime());
            tvCheckOut.setText(log.getCheckOutTime());
            tvHours.setText(DateTimeUtils.formatHours(log.getTotalHours()));
            setActive(btnCheckIn, false);
            setActive(btnCheckOut, false);
        }
    }

    /**
     * Nút không phù hợp trạng thái chỉ làm mờ (không disable)
     * để khi bấm vẫn hiện thông báo lỗi kiểm tra dữ liệu.
     */
    private void setActive(Button button, boolean active) {
        button.setAlpha(active ? 1f : 0.45f);
    }

    private void showResult(AttendanceService.Result r) {
        new AlertDialog.Builder(this)
                .setTitle(r.success ? "Thành công" : "Không hợp lệ")
                .setMessage(r.message)
                .setPositiveButton("OK", null)
                .show();
    }
}
