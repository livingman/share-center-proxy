package cn.living.sharecenter.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @Description properties解析
 * @Author Living
 * @Date 2019/9/23 17:18
 * @Version 1.0
 **/
public class PropertiesUtil {

    private static Logger LOG = LoggerFactory.getLogger("PROXY");
    public static final Properties properties = new Properties();
    static {
        try {
            properties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            LOG.error("fail to load config.properties files,exception:{}",e.getMessage());
        }
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }
}
