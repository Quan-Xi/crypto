package com.flow.tool;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimeTool {

    public enum TimeDimension {
        day, week, month, all
    }

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE = "yyyy-MM-dd";

    public static final String DATE_YEAR_MONTH="yyyy-MM";
    public static final long ONE_DAY_MILLISECOND = 86400000L;

    public static LocalDateTime minDay(LocalDateTime day) {
        return day.with(LocalTime.MIN);
    }

    public static LocalDateTime maxDay(LocalDateTime day) {
        return day.with(LocalTime.MAX);
    }


    public static LocalDateTime minMonthTime(LocalDateTime day) {
        LocalDate today = day.toLocalDate();
        Month month = today.getMonth();
        LocalDate min = LocalDate.of(today.getYear(), month, 1);
        return min.atTime(0, 0, 0);
    }

    public static LocalDateTime minYearTime(LocalDateTime day) {
        int year = day.getYear();
        return LocalDateTime.of(year, 1, 1, 0, 0, 0);
    }

    public static LocalDateTime maxMonthTime(LocalDateTime day) {
        LocalDate today = day.toLocalDate();
        Month month = today.getMonth();
        int length = month.length(today.isLeapYear());
        LocalDate max = LocalDate.of(today.getYear(), month, length);
        return max.atTime(23, 59, 59);
    }

    public static Map<String, LocalDateTime> thisDay() {
        Map<String, LocalDateTime> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        map.put("start", minDay(now));
        map.put("end", maxDay(now));
        return map;
    }

    public static Map<String, LocalDateTime> thisWeek() {
        LocalDate today = LocalDate.now();
        DayOfWeek week = today.getDayOfWeek();
        int value = week.getValue();
        LocalDate min = today.minusDays(value - 1);
        LocalDate max = today.plusDays(7 - value);
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", min.atTime(0, 0, 0));
        map.put("end", max.atTime(23, 59, 59));
        return map;
    }

    public static Map<String, LocalDateTime> thisMonth() {
        LocalDateTime today = LocalDateTime.now();
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", minMonthTime(today));
        map.put("end", maxMonthTime(today));
        return map;
    }

    public static Map<String, LocalDateTime> all() {
        LocalDateTime today = LocalDateTime.now();
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", LocalDateTime.of(2019, 1, 1, 1, 1));
        map.put("end", maxMonthTime(today));
        return map;
    }

    public static LocalDateTime getDateTimeOfTimestamp(Long timestamp) {
        if (Objects.isNull(timestamp)) return null;
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static Map<String, String> theLast15DaysStr() {
        Map<String, LocalDate> last15Days = theLast15Days();
        Map<String, String> map = new HashMap<>();
        map.put("today", last15Days.get("today").format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        map.put("pastDays", last15Days.get("pastDays").format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return map;
    }

    public static Map<String, LocalDate> theLast15Days() {
        return theLastNDays(15);
    }

    public static Map<String, LocalDate> theLastNDays(int n) {
        LocalDate today = LocalDate.now();
        LocalDate minusDays = today.minusDays(n);
        Map<String, LocalDate> map = new HashMap<>();
        map.put("today", today);
        map.put("pastDays", minusDays);
        return map;
    }

    /**
     * 格式化时间(yyyy-MM-dd HH:mm:ss:SSS)
     * 当前时间
     */
    public static String getNowDateTimeSSSDisplayString() {
        return getDateTimeDisplayString(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss:SSS");
    }

    /**
     * 格式化时间(yyyy-MM-dd HH:mm:ss)
     * 当前时间
     */
    public static String getNowDateTimeDisplayString() {
        return getDateTimeDisplayString(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 格式化时间(yyyy-MM-dd HH:mm:ss)
     *
     * @param dateTime 日期时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeDisplayString(LocalDateTime dateTime) {
        return getDateTimeDisplayString(dateTime, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 格式化时间
     *
     * @param dateTime 时间localDateTime
     * @param dfStr    格式
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeDisplayString(LocalDateTime dateTime, String dfStr) {
        if (Objects.isNull(dateTime)) {
            return "";
        }
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern(dfStr);
        return dtf2.format(dateTime);
    }

    /**
     * 格式化时间
     *
     * @param dateTime 时间localDateTime
     * @param dfStr    格式
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getLocalDateDisplayString(LocalDate dateTime, String dfStr) {
        if (Objects.isNull(dateTime)) {
            return "";
        }
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern(dfStr);
        return dtf2.format(dateTime);
    }

    /**
     * 字符串转LocalDateTime
     */
    public static LocalDateTime strToDateTime(String str, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(str, dtf);
    }


    /**
     * 字符串转LocalDateTime
     */
    public static LocalDate strToLocalDate(String str, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(str, dtf);
    }


    /**
     * localDateTime 转成 时间戳
     */
    public static long toEpochMilli(LocalDateTime time) {
        Instant complete_instant = time.atZone(ZoneId.systemDefault()).toInstant();
        return complete_instant.toEpochMilli();
    }

    public static Map<String, LocalDateTime> thisWeekWithTime(LocalDateTime now) {
        return thisWeekWithTime(now.toLocalDate());
    }

    public static Map<String, LocalDateTime> thisWeekWithTime(LocalDate today) {
        DayOfWeek week = today.getDayOfWeek();
        int value = week.getValue();
        LocalDate min = today.minusDays(value - 1);
        LocalDate max = today.plusDays(7 - value);
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", min.atTime(0, 0, 0));
        map.put("end", max.atTime(23, 59, 59));
        return map;
    }

    public static void sleep(long sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException ignored) {
            System.out.println("thread 被中断");
        }
    }


    public static Map<String, LocalDateTime> thisMonthWithTime(LocalDateTime today) {
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", minMonthTime(today));
        map.put("end", maxMonthTime(today));
        return map;
    }

    public static Map<String, LocalDateTime> thisDayWithTime(LocalDateTime now) {
        LocalDate localDate = now.toLocalDate();
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", localDate.atTime(0, 0, 0));
        map.put("end", localDate.atTime(23, 59, 59));
        return map;
    }

    public static Map<String, LocalDateTime> thisHourWithTime(LocalDateTime now) {
        LocalDate localDate = now.toLocalDate();
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", localDate.atTime(now.getHour(), 0, 0));
        map.put("end", localDate.atTime(now.getHour(), 59, 59));
        return map;
    }


    public static LocalDate getMonthStartTime(String date) {
        String append = new StringBuffer(date).append("-01").toString();
        LocalDate localDate= strToLocalDate(append, "yyyy-MM-dd");
        return localDate;
    }


    /**
     * 获取上一个月
     *
     * @return
     */
    public static String getNMonth(int amount) {
        Calendar cal = Calendar.getInstance();
        cal.add(cal.MONTH, amount);
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
        String lastMonth = dft.format(cal.getTime());
        return lastMonth;
    }

    public static LocalDateTime rfc1123DateDime(String timeStr) {
        String zone = timeStr.substring(timeStr.lastIndexOf('+'));
        return LocalDateTime.parse(timeStr, DateTimeFormatter.RFC_1123_DATE_TIME)
                .atZone(ZoneOffset.of(zone)).withZoneSameInstant(ZoneOffset.ofHours(8)).toLocalDateTime();
    }


}
