//package cn.living.sharecenter;
//
//import cn.living.sharecenter.module.server.Server;
//import cn.living.sharecenter.org.socket.SocketProxy;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// *@Author living
// *@Description //代理类启动
// *@Date 16:44 2019/9/20
// *@Param
// *@return
// **/
//@Component
//public class SocketProxyRunner implements ApplicationRunner {
//    private Logger log = LoggerFactory.getLogger("PROXY");
//    @Value("#{'${proxy.port}'.split(',')}")
//    private List<Integer> ports;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        log.info("代理启动!!端口:{}"+ports);
////      socket代理  http/https/socket5
//        System.out.println("代理启动"+ports);
//        new SocketProxy().start(ports);
//    }
//
//}
//
//
