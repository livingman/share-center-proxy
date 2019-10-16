package cn.living.sharecenter.utils;

import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author humeng
 * @Description: 沃趣拼团包装的StringUtil
 * @date 2019年1月2日 下午3:29:53
 */
public class StringUtil extends StringUtils {

    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sbu.append((int) chars[i]);
        }
        return sbu.toString();
    }

    /**
     * @param str
     * @return
     * @Description: 星化手机号
     * @author Jason
     * @date May 22, 2018 4:40:03 PM
     */
    public static String starFlyPhone(String str) {
        if (isPhone(str)) {
            return str.substring(0, 3) + "****" + str.substring(7);
        } else {
            return null;
        }
    }

    /**
     * @param str
     * @return
     * @Description: 格式化字符串
     * @author liuyu
     * @date 2017年12月11日 上午9:55:17
     */
    public static String format(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * @param str
     * @param pattern
     * @return
     * @Description: 正则校验
     * @author liuyu
     * @date 2017年12月11日 上午9:56:11
     */
    public static boolean matche(String str, Pattern pattern) {
        return isEmpty(str) ? false : pattern.matcher(str).matches();
    }

    /**
     * @param str
     * @return
     * @Description: 是否是手机号
     * @author liuyu
     * @date 2017年12月11日 上午9:56:32
     */
    public static boolean isPhone(String str) {
        return matche(str, Pattern.compile("^0?1[3|4|5|6|7|8|9][0-9]\\d{8}$"));
    }

    /**
     * @param str
     * @return
     * @Description: 从字符串中找出数字
     * @author Jason
     * @date Jul 16, 2018 10:52:25 AM
     */
    public static String findNumber(String str) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    /**
     * @return
     * @Description: 随机不重复字符串
     * @author liuyu
     * @date 2017年12月11日 上午9:57:47
     */
    public static String random() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int itmp = r.nextInt(26) + 65;
            char ctmp = (char) itmp;
            sb.append(String.valueOf(ctmp));
        }
        return System.currentTimeMillis() + sb.toString() + r.nextInt(100);
    }

    public static String getDate(String format) {

        Date date = new Date();
        DateFormat fm = new SimpleDateFormat(format);
        return fm.format(date);

    }

    public static String getDate(Date date, String format) {

        DateFormat fm = new SimpleDateFormat(format);
        return fm.format(date);

    }

    public static List<String> resolve(String rawData, String regex, int group) {
        if (rawData == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawData);
        List<String> result = new ArrayList<String>();
        try {
            while (matcher.find()) {
                result.add(matcher.group(group));
            }

        } catch (Exception e) {

        }

        return result;

    }

    public static String resolve2(String rawData, String regex, int group) {
        if (rawData == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawData);
        String result = null;
        try {
            if (matcher.find()) {
                result = matcher.group(group);
            }

        } catch (Exception e) {

        }

        return result;

    }

    public static boolean stringisEmpty(String value) {
        if (value != null && !value.equals("")) {
            return false;
        } else {
            return true;

        }
    }


    /**
     * 获取 X 位的随机数
     * @param length
     * @return
     */
    public static String getRandomNumByLength(int length) {
        String um ="1";
        for (int i=1;i<length;i++){
            um+="0";
        }
        int ramNum= (int) ((Math.random()*9+1)*Integer.parseInt(um));
        return String.valueOf(ramNum);

    }
}
