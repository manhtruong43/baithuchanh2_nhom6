# Employee Working Hours & Salary Management

Ứng dụng Android (Java + Room) quản lý chấm công và tính lương nhân viên.

## Công nghệ
- Java 11, minSdk 24, targetSdk 34
- Room Database (SQLite) – 4 bảng: `accounts`, `employees`, `work_logs`, `salaries`
- SharedPreferences – lưu phiên đăng nhập / ghi nhớ đăng nhập
- Material Components

## Tài khoản demo (tạo sẵn khi cài app lần đầu)
| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Admin | admin | admin123 |
| Nhân viên | nv01 | 123456 |
| Nhân viên | nv02 | 123456 |

Dữ liệu mẫu có sẵn chấm công tháng trước (có ngày tăng ca, có 1 ngày thiếu check-out) và các ngày trong tuần của tháng này.

## Quy tắc tính lương
- Chỉ tính ngày chấm công hoàn chỉnh (đủ check-in + check-out).
- Giờ làm/ngày = giờ check-out − giờ check-in.
- Giờ vượt 8h/ngày là giờ tăng ca, hệ số 1.5.
- Lương = giờ thường × lương giờ + giờ tăng ca × lương giờ × 1.5.
- Bảng lương đã thanh toán bị khoá, không tính lại.

## Kiểm tra dữ liệu chấm công
- Không cho check-in 2 lần/ngày (kiểm tra trong code + unique index `employeeId, workDate`).
- Không cho check-out khi chưa check-in, đã check-out, hoặc giờ ra không sau giờ vào.
- Ngày cũ chưa check-out tự chuyển `INCOMPLETE`, không được tính lương.

## Phân công
| Thành viên | Phạm vi |
|---|---|
| TV1 | Cấu hình project, Room DB (entity, DAO, dữ liệu mẫu), SharedPreferences, đăng nhập, màn hình chính 2 vai trò |
| TV2 | Quản lý nhân viên (CRUD, tìm kiếm, lương giờ), tính lương tự động + tăng ca, quản lý bảng lương (Admin) |
| TV3 | Check-in/check-out + kiểm tra dữ liệu, tự tính giờ làm, lịch sử chấm công + tổng hợp tháng, lương của tôi |

## Cấu trúc
```
app/src/main/java/com/ptit/employeesalary/
├── data/            (TV1) AppDatabase, entity/, dao/, model/
├── util/            (TV1) AppConfig, DateTimeUtils, PasswordUtils, SessionManager, ScreenNavigator
├── logic/
│   ├── EmployeeService.java     (TV2)
│   ├── SalaryCalculator.java    (TV2)
│   └── AttendanceService.java   (TV3)
└── ui/
    ├── auth/, home/             (TV1)
    ├── employee/, salary/       (TV2)
    └── attendance/              (TV3)
```
