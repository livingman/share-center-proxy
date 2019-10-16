package cn.living.sharecenter.org.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.living.sharecenter.org.task.ProxyTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketProxy {
	private static  Logger LOG = LoggerFactory.getLogger("PROXY");
	  
    public void start(List<Integer> ports) throws Exception {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
        List<ServerSocket> serverSockets =new ArrayList<ServerSocket>(ports.size());
        for(Integer port :ports){
        	if(port == null){
        		continue;
        	}
        	if(port<=0||port>65535){
        		LOG.error("proxyPort error,proxyPort must between 1 and 65535:{},but input:{}",port);
        		continue;
        	}
        	 ServerSocket serverSocket = new ServerSocket(port);
        	 serverSockets.add(serverSocket);
        }
        if(ports.isEmpty()){
        	LOG.error("failed to start Proxy Server ");
        	return;
        }
        
        
        final ExecutorService tpe=Executors.newCachedThreadPool();  
        for(ServerSocket serverSocket:serverSockets){
        	Thread t =new Thread(new Runnable() {
				@Override
				public void run() {
					LOG.info("Proxy Server Start At {}"+sdf.format(new Date()));
					LOG.info("support SOCKS4, HTTP and HTTPS connection");
			        LOG.info("listening port:"+serverSocket.getLocalPort()+"……");  
			        while (true) {  
		                 Socket socket = null;  
		                 try {  
		                     socket = serverSocket.accept();  
		                     socket.setKeepAlive(true);  
		                     //加入任务列表，等待处理 
		                     tpe.execute(new ProxyTask(socket));
		                 } catch (Exception e) {  
		                 	LOG.error("accept socket exception:{}",e.getMessage());
		                 }  
		             }  
				}
			});
        	t.start();
        }
    }  
}
