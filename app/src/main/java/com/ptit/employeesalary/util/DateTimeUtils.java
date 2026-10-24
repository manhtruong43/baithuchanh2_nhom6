package com.ptit.employeesalary.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Tiện ích xử lý ngày giờ, định dạng tiền và giờ làm.
 * Quy ước lưu DB: ngày "yyyy-MM-dd", giờ "HH:mm:ss".
 */
public final class DateTimeUtils {

    public static final String DB_DATE = "yyyy-MM-dd";
    public static final String DB_TIME = "HH:mm:ss";

    private DateTimeUtils() {
    }

    /** Ngày hôm nay dạng yyyy-MM-dd */
    public static String today() {
        return new SimpleDateFormat(DB_DATE, Locale.US).format(new Date());
    }

    /** Giờ hiện tại dạng HH:mm:ss */
    public static String nowTime() {
        return new SimpleDateFormat(DB_TIME, Locale.US).format(new Date());
    }

    public static int currentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int currentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /** Tiền tố tháng để lọc workDate, ví dụ 2026-10 */
    public static String monthPrefix(int month, int year) {
        return String.format(Locale.US, "%04d-%02d", year, month);
    }

    /**
     * Số giờ giữa 2 mốc giờ trong cùng 1 ngày (làm tròn 2 chữ số).
     * Trả về -1 nếu dữ liệu sai hoặc giờ ra không sau giờ vào.
     */
    public static double hoursBetween(String checkIn, String checkOut) {
        if (checkIn == null || checkOut == null) {
            return -1;
        }
        try {
            SimpleDateFormat f = new SimpleDateFormat(DB_TIME, Locale.US);
            long start = f.parse(checkIn).getTime();
            long end = f.parse(checkOut).getTime();
            if (end <= start) {
                return -1;
            }
            double hours = (end - start) / 3_600_000.0;
            return round2(hours);
        } catch (ParseException | NullPointerException e) {
            return -1;
        }
    }

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** yyyy-MM-dd -> dd/MM/yyyy */
    public static String toDisplayDate(String dbDate) {
        try {
            Date d = new SimpleDateFormat(DB_DATE, Locale.US).parse(dbDate);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(d);
        } catch (ParseException | NullPointerException e) {
            return dbDate;
        }
    }

    /** Thứ trong tuần tiếng Việt của ngày yyyy-MM-dd */
    public static String dayOfWeek(String dbDate) {
        try {
            Date d = new SimpleDateFormat(DB_DATE, Locale.US).parse(dbDate);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            switch (c.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    return "Thứ 2";
                case Calendar.TUESDAY:
                    return "Thứ 3";
                case Calendar.WEDNESDAY:
                    return "Thứ 4";
                case Calendar.THURSDAY:
                    return "Thứ 5";
                case Calendar.FRIDAY:
                    return "Thứ 6";
                case Calendar.SATURDAY:
                    return "Thứ 7";
                default:
                    return "Chủ nhật";
            }
        } catch (ParseException | NullPointerException e) {
            return "";
        }
    }

    /** 1234567.0 -> "1.234.567 đ" */
    public static String formatMoney(double amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(Math.round(amount)) + " đ";
    }

    /** 8.5 -> "8.50 giờ" */
    public static String formatHours(double hours) {
        return String.format(Locale.US, "%.2f giờ", hours);
    }

    /** "Tháng 10/2026" */
    public static String monthLabel(int month, int year) {
        return String.format(Locale.US, "Tháng %02d/%04d", month, year);
    }
}
