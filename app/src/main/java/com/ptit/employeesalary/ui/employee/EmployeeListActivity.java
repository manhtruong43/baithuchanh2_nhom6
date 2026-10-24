package com.ptit.employeesalary.ui.employee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ptit.employeesalary.R;
import com.ptit.employeesalary.data.AppDatabase;
import com.ptit.employeesalary.data.entity.Employee;
import com.ptit.employeesalary.logic.EmployeeService;
import com.ptit.employeesalary.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin: xem danh sách, tìm kiếm, thêm, sửa, xoá nhân viên.
 * - Chạm vào 1 nhân viên: sửa
 * - Giữ lâu: xoá
 */
public class EmployeeListActivity extends AppCompatActivity {

    private AppDatabase db;
    private EmployeeService employeeService;

    private EditText edtSearch;
    private TextView tvResultCount;
    private TextView tvEmpty;
    private ListView lvEmployees;

    private final List<Employee> employees = new ArrayList<>();
    private EmployeeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        setTitle("Quản lý nhân viên");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        employeeService = new EmployeeService(this);

        edtSearch = findViewById(R.id.edtSearch);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvEmployees = findViewById(R.id.lvEmployees);

        adapter = new EmployeeAdapter();
        lvEmployees.setAdapter(adapter);

        lvEmployees.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, EmployeeFormActivity.class);
            intent.putExtra(EmployeeFormActivity.EXTRA_EMPLOYEE_ID, employees.get(position).getId());
            startActivity(intent);
        });
        lvEmployees.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(employees.get(position));
            return true;
        });

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, EmployeeFormActivity.class)));

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadEmployees();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEmployees();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadEmployees() {
        String keyword = edtSearch.getText().toString().trim();
        employees.clear();
        if (keyword.isEmpty()) {
            employees.addAll(db.employeeDao().getAll());
        } else {
            employees.addAll(db.employeeDao().search(keyword));
        }
        adapter.notifyDataSetChanged();

        tvResultCount.setText("Có " + employees.size() + " nhân viên");
        boolean empty = employees.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        lvEmployees.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(Employee employee) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá nhân viên")
                .setMessage("Xoá " + employee.getFullname() + " (" + employee.getCode() + ")?\n"
                        + "Toàn bộ chấm công, bảng lương và tài khoản của nhân viên này cũng bị xoá.")
                .setPositiveButton("Xoá", (d, w) -> {
                    employeeService.delete(employee);
                    Toast.makeText(this, "Đã xoá nhân viên", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /** Adapter hiển thị 1 dòng nhân viên */
    private class EmployeeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return employees.size();
        }

        @Override
        public Employee getItem(int position) {
            return employees.get(position);
        }

        @Override
        public long getItemId(int position) {
            return employees.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(EmployeeListActivity.this)
                        .inflate(R.layout.item_employee, parent, false);
            }
            Employee e = getItem(position);
            ((TextView) view.findViewById(R.id.tvName)).setText(e.getFullname());
            ((TextView) view.findViewById(R.id.tvCode)).setText(e.getCode());
            ((TextView) view.findViewById(R.id.tvContact)).setText(e.getEmail() + "  •  " + e.getPhone());
            ((TextView) view.findViewById(R.id.tvRate)).setText(
                    DateTimeUtils.formatMoney(e.getHourlyRate()) + " / giờ");
            return view;
        }
    }
}
