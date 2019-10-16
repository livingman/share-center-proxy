package cn.living.sharecenter.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String getDateStringByFormat(Date date, String format) throws Exception{
        if(StringUtil.isEmpty(format)){
            throw new Exception("时间格式不能为空");
        }
        if(null == date){
            throw new Exception("时间不能为空");
        }
        SimpleDateFormat simple = new SimpleDateFormat(format);
        return simple.format(date);
    }

    /**
     * 获取指定格式时间（LocalDateTime）
     *
     * @param dateTime LocalDateTime
     * @param format   时间格式
     * @return 指定格式时间字符串
     */
    public static String getDateStringByFormat(LocalDateTime dateTime, String format) throws Exception {
        if(StringUtil.isEmpty(format)){
            throw new Exception("时间格式不能为空");
        }
        if(null == dateTime){
            throw new Exception("时间不能为空");
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return df.format(dateTime);
    }

    public static Date getDateByFormat(String time, String format) throws Exception{
        if(StringUtil.isEmpty(format)){
            throw new Exception("时间格式不能为空");
        }
        if(StringUtil.isEmpty(time)){
            throw new Exception("时间不能为空");
        }
        SimpleDateFormat simple = new SimpleDateFormat(format);
        return simple.parse(time);
    }

    public static int getMinuteByNow(Date time) throws Exception{
        Date date = new Date();
        long millsecond = date.getTime()-time.getTime();
        if(millsecond < 0){
            throw new Exception("时间超前异常");
        }
        int minute = (int) millsecond / 60000;
        return minute;
    }

    /**
     * 获取系统时间后一个月的第一天
     */
    public static String getFirstDayAfterMonth() throws Exception{
        Calendar c = Calendar.getInstance();
        String time = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+2)%13 + "-1";
        return time;
    }

    /**
     * 获取系统时间每月最后一天23：59：59到当前时间的毫秒值
     * @throws Exception
     */
    public static long getMillSecondTheMonthUtilNow() throws Exception{
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Calendar nowCalendar = Calendar.getInstance();
        return calendar.getTime().getTime() - nowCalendar.getTime().getTime();
    }

    /**
     * @Description: 增加天数
     * @param :date
     *            要进行添加的日期
     * @param :days
     *            要增加的天数
     * @return 增加天数后的日期
     * @throws Exception
     */

    public static Date addDay(Date date, int days) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.DATE, days);
        return cd.getTime();
    }



}
