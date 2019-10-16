package cn.living.sharecenter.org.task;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class CloseUtil {
    /**
     * IO操作中共同的关闭方法
     * 
     */
    public static final void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

	 /**
    * IO操作中共同的关闭方法
    * 
    */
   public static final void closeIo(Socket closeable) {
       if (null != closeable) {
           try {
               closeable.close();
           } catch (IOException e) {
           }
       }
   }
}
