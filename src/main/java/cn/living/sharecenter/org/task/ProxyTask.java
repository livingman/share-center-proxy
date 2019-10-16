package cn.living.sharecenter.org.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import cn.living.sharecenter.org.domain.FlowCounter;
import cn.living.sharecenter.org.domain.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *@Author living
 *@Description //将客户端发送过来的数据转发给请求的服务器端，并将服务器返回的数据转发给客户端
 *@Date 19:54 2019/9/20
 *@Param 
 *@return 
 **/
public class ProxyTask implements Runnable { 
  private Socket socketIn; 
  private Socket socketOut;  
 
  public ProxyTask(Socket socket) { 
    this.socketIn = socket; 
  } 
  private final static Logger LOG = LoggerFactory.getLogger("PROXY");
   
  private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
  /** 已连接到请求的服务器 */ 
  private static final String AUTHORED = "HTTP/1.1 200 Connection established\r\n\r\n"; 
  /** 本代理登陆失败(此应用暂时不涉及登陆操作) */ 
  //private static final String UNAUTHORED="HTTP/1.1 407 Unauthorized\r\n\r\n"; 
  /** 内部错误 */ 
  private static final String SERVERERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n"; 
   
  @Override 
  public void run() { 
     
    StringBuilder builder=new StringBuilder(); 
    FlowCounter flowCounter =new FlowCounter();
    try { 
      builder.append("\r\n").append("Request Time ：" + sdf.format(new Date())); 
       
      InputStream isIn = socketIn.getInputStream(); 
      OutputStream osIn = socketIn.getOutputStream(); 
 
      // 查找主机和端口 
      ProtocolCheck protocolCheck =new ProtocolCheck(socketIn);
      socketOut =  protocolCheck.checkProtocolType(isIn, osIn, null, null, false);
      //socketOut.setSoTimeout(1000);
      
      if(null !=protocolCheck.getHttpHeader()){
    	  builder.append("\r\n").append("From  Host ：" + socketIn.getInetAddress()); 
          builder.append("\r\n").append("From  Port ：" + socketIn.getPort()); 
          builder.append("\r\n").append("Proxy  Method：" + protocolCheck.getHttpHeader().getMethod()); 
          builder.append("\r\n").append("Request Host ：" + protocolCheck.getHttpHeader().getHost()); 
          builder.append("\r\n").append("Request Port ：" + protocolCheck.getHttpHeader().getPort()); 
      }
        LOG.info("socketIn:"+socketIn);
        LOG.info("socketOut:"+socketOut);
      InputStream isOut = socketOut.getInputStream(); 
      OutputStream osOut = socketOut.getOutputStream(); 
      
      switch(protocolCheck.getProtocolType()){
	  	case ProtocolCheck.PROTOCOL_TYPE_HTTP:
				if (null != protocolCheck.getHttpHeader()) {
					HttpHeader header = protocolCheck.getHttpHeader();
					if (header.getMethod().equals(HttpHeader.METHOD_CONNECT)) {
						// 将已联通信号返回给请求页面
						osIn.write(AUTHORED.getBytes());
						osIn.flush();
					} else {
						String requestUrl = header.getHeader().get(0);
				    	  String subUrl =null;
				    	  int afterFirstBlank =requestUrl.indexOf(" ")+1;//第一个空格后面的索引
				    	  subUrl =requestUrl.substring(afterFirstBlank+8);//用于截取 主机端口后面的url
				    	  header.getHeader().add("Connection: Keep-Alive");
				    	  if(requestUrl.substring(afterFirstBlank).startsWith("http")){
				    		  //重新构造请求头
				    		  header.getHeader().remove(0);
				    		  header.getHeader().add(requestUrl.substring(0, afterFirstBlank)+subUrl.substring(subUrl.indexOf("/")));
				    		  Collections.reverse(header.getHeader());
				    	  }
						// http请求需要将请求头部也转发出去
						byte[] headerData = header.toString().getBytes();
						flowCounter.setUploadBytes(flowCounter.getUploadBytes()+headerData.length);
						osOut.write(headerData);
						osOut.flush();
					}

				}
		  break;
	  }
      CountDownLatch latch = new CountDownLatch(1);
      transfer(latch, socketIn.getInputStream(), socketOut.getOutputStream(), null,FlowCounter.FLOW_UPLOAD,flowCounter);
      transfer(latch, socketOut.getInputStream(), socketIn.getOutputStream(), null,FlowCounter.FLOW_DOWNLOAD,flowCounter);
      try {
		latch.await();
	} catch (Exception e) {
	}

    } catch (Exception e) { 
    	LOG.error("proxy socket throw an exception,message:{}",e.getMessage());
      if(!socketIn.isOutputShutdown()){ 
        //如果还可以返回错误状态的话，返回内部错误 
        try { 
          socketIn.getOutputStream().write(SERVERERROR.getBytes()); 
        } catch (IOException e1) {} 
      } 
    } finally { 
		try {
			CloseUtil.close(socketIn.getInputStream());
		} catch (Exception e) {
		}
		try {
			socketIn.getOutputStream().flush();
			CloseUtil.close(socketIn.getOutputStream());
		} catch (Exception e) {
			CloseUtil.close(socketOut);
		}
		
		try {
			CloseUtil.close(socketOut.getInputStream());
		} catch (Exception e) {
		}
		try {
			socketOut.getOutputStream().flush();
			CloseUtil.close(socketOut.getOutputStream());
		} catch (Exception e) {
			CloseUtil.close(socketIn);
		}
      //纪录上下行数据量和最后结束时间并打印 
      builder.append("\r\n").append("Up  Bytes ：" + flowCounter.getUploadBytes()); 
      builder.append("\r\n").append("Down Bytes ：" + flowCounter.getDownloadBytes()); 
      builder.append("\r\n").append("Closed Time ：" + sdf.format(new Date())); 
      builder.append("\r\n"); 
      logRequestMsg(builder.toString()); 
    }   
  } 
   
  /** 
   * 避免多线程竞争把日志打串行了 
   * @param msg 
   */ 
  private synchronized void logRequestMsg(String msg){ 
    LOG.info(msg);
  } 
 
  protected final void transfer(final CountDownLatch latch, final InputStream in, final OutputStream out,
          final OutputStream cache,final int flowCostType,final FlowCounter flowCounter) {
      new Thread() {
          public void run() {
              byte[] bytes = new byte[1024];
              int n = 0;
              try {
                  while ((n = in.read(bytes)) > 0) {
                      out.write(bytes, 0, n);
                      out.flush();
                      if (null != cache) {
                          synchronized (cache) {
                              cache.write(bytes, 0, n);
                          }
                      }
                      //流量统计
                      switch(flowCostType){
                      	case FlowCounter.FLOW_UPLOAD:
                      		flowCounter.setUploadBytes(flowCounter.getUploadBytes()+n);
                    	  break;
                      	case FlowCounter.FLOW_DOWNLOAD:
                      		flowCounter.setDownloadBytes(flowCounter.getDownloadBytes()+n);
                      	  break;
                      }
                  }
              } catch (Exception e) {
              }
              if (null != latch) {
                  latch.countDown();
              }
          };
      }.start();
  }
 
}
