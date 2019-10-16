package cn.living.sharecenter.org.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import cn.living.sharecenter.org.domain.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *@Author living
 *@Description //协议检测
 *@Date 19:56 2019/9/20
 *@Param
 *@return
 **/
public class ProtocolCheck {
	private static  Logger LOG = LoggerFactory.getLogger("PROXY");
	  private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
	  /** 已连接到请求的服务器 */ 
	  private static final String AUTHORED = "HTTP/1.1 200 Connection established\r\n\r\n"; 
	  /** 本代理登陆失败(此应用暂时不涉及登陆操作) */ 
	  //private static final String UNAUTHORED="HTTP/1.1 407 Unauthorized\r\n\r\n"; 
	  /** 内部错误 */ 
	  private static final String SERVERERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n"; 
	  /** sock代理错误*/
	  private static final String SOCKSERVERERROR = "SOCK 500 Connection FAILED\r\n\r\n"; 
	  
	  private HttpHeader httpHeader ;
	/**
	 * 代理失败
	 */
	private int protocolType =0;
	/**
	 * 来源socket
	 */
	private Socket socket = null;
	
	
	/**
	 * http协议
	 */
	public final static int PROTOCOL_TYPE_HTTP=0x01;
	
	/**
	 * scok4协议
	 */
	public final static int PROTOCOL_TYPE_SOCK4=0x04;
	/**
	 * sock5协议
	 */
	public final static int PROTOCOL_TYPE_SOCK5=0x05;
	
	public ProtocolCheck(Socket socket){
		this.socket	= socket;
	}
	/**
	 * 检测协议
	 * @param input
	 * @param output
	 * @param user
	 * @param password
	 * @param needLogin
	 * @return
	 * @throws IOException
	 */
	public Socket checkProtocolType(InputStream input,OutputStream output,String user,String password,boolean needLogin) throws IOException{
		 // 获取协议头。取代理的类型，只有 4，5。
		Socket proxySocket =null;
        byte[] tmp = new byte[1];
        int n = input.read(tmp);
        if(n == 1){//sock;
        	 byte protocol = tmp[0];
        	 switch(protocol){
        	 	case 0x04:
        	 		 proxySocket = checkForSock4(input, output);
        	 		 this.protocolType = PROTOCOL_TYPE_SOCK4;
        		 break;
        	 	case 0x05:
        	 		proxySocket = checkForSock5(input, output, user, password, needLogin);
        	 		this.protocolType = PROTOCOL_TYPE_SOCK5;
        		 break;
        		default:
        			proxySocket =SocketCeckForHttp(input, output,tmp);
        			this.protocolType = PROTOCOL_TYPE_HTTP;
        			 break;
        	 }
        }
		return proxySocket;
	}
	private Socket SocketCeckForHttp(InputStream input,OutputStream output, byte[] temp) throws NumberFormatException, UnknownHostException, IOException{
		InputStream isIn = input; 
	      OutputStream osIn = output; 
	      Socket proxySocket = null;
	      //从客户端流数据中读取头部，获得请求主机和端口 
	      HttpHeader header = HttpHeader.readHeader(isIn,new String(temp)); 
	       this.httpHeader = header;
	      //如果没解析出请求请求地址和端口，则返回错误信息 
	      if (header.getHost() == null || header.getPort() == null) { 
	        osIn.write(SERVERERROR.getBytes()); 
	        osIn.flush(); 
	        return proxySocket; 
	      } 
	      // 查找主机和端口 
	      proxySocket = new Socket(header.getHost(), Integer.parseInt(header.getPort())); 
		return proxySocket;
	}
	private Socket checkForSock4(InputStream input,OutputStream output) throws IOException{
        Socket proxySocket = null;
        int time =0;
        //延时等待输入流瞒组8个字节，否则3秒后退出
        try {
        	int av = 0;
			while((av = input.available())!=8){
				Thread.sleep(3);
				time++;
				if(time >1000){
					break;
				}
			}
		} catch (InterruptedException e) {
		}
        byte[] tmp = new byte[3];
        input.read(tmp);
        // 请求协议|VN1|CD1|DSTPORT2|DSTIP4|NULL1|
        int port = ByteBuffer.wrap(tmp, 1, 2).asShortBuffer().get() & 0xFFFF;
        String host = getHost((byte) 0x01, input);
        if(null == host){
        	return null;
        }
        input.read();
        byte[] rsv = new byte[8];// 返回一个8位的响应协议
        // |VN1|CD1|DSTPORT2|DSTIP 4|
        try {
        	 proxySocket = new Socket(host, port);
	    	 HttpHeader header =new HttpHeader();
	    	 header.setHost(host);
	    	 header.setPort(port+"");
	    	 header.setMethod("SOCK4");
	    	 this.httpHeader = header;
            rsv[1] = 90;// 代理成功
        } catch (Exception e) {
            rsv[1] = 91;// 代理失败.
            LOG.info("sock4 proxy for ip:{},port:{} fail.",
        			host,port);
        }
        output.write(rsv);
        output.flush();
        return proxySocket;
	}
	private Socket checkForSock5(InputStream in, OutputStream out,String user,String password,boolean needLogin) throws IOException {
        byte[] tmp = new byte[2];
        in.read(tmp);
        boolean isLogin = false;
        byte method = tmp[1];
        if (0x02 == tmp[0]) {
            method = 0x00;
            in.read();
        }
        if (needLogin) {
            method = 0x02;
        }
        tmp = new byte[] { 0x05, method };
        out.write(tmp);
        out.flush();
        // Socket result = null;
        Object resultTmp = null;
        if (0x02 == method) {// 处理登录.
            int b = in.read();
            String tempUser = null;
            String tempPassword = null;
            if (0x01 == b) {
                b = in.read();
                tmp = new byte[b];
                in.read(tmp);
                tempUser = new String(tmp);
                b = in.read();
                tmp = new byte[b];
                in.read(tmp);
                tempPassword = new String(tmp);
                if (null != tempUser && tempUser.trim().equals(user) && null != tempPassword && tempPassword.trim().equals(password)) {// 权限过滤
                    isLogin = true;
                    tmp = new byte[] { 0x05, 0x00 };// 登录成功
                    out.write(tmp);
                    out.flush();
                   // log("%s login success !", user);
                } else {
                  //  log("%s login faild !", user);
                }
            }
        }
        byte cmd = 0;
        if (!needLogin || isLogin) {// 验证是否需要登录
            tmp = new byte[4];
            in.read(tmp);
          //  log("proxy header >>  %s", Arrays.toString(tmp));
            cmd = tmp[1];
            String host = getHost(tmp[3], in);
            tmp = new byte[2];
            in.read(tmp);
            int port = ByteBuffer.wrap(tmp).asShortBuffer().get() & 0xFFFF;
           // log("connect %s:%s", host, port);
            ByteBuffer rsv = ByteBuffer.allocate(10);
            rsv.put((byte) 0x05);
            try {
                if (0x01 == cmd) {
                    resultTmp = new Socket(host, port);
                    rsv.put((byte) 0x00);
                } else if (0x02 == cmd) {
                    resultTmp = new ServerSocket(port);
                    rsv.put((byte) 0x00);
                } else {
                    rsv.put((byte) 0x05);
                    resultTmp = null;
                }
            } catch (Exception e) {
                rsv.put((byte) 0x05);
                resultTmp = null;
            }
            rsv.put((byte) 0x00);
            rsv.put((byte) 0x01);
            rsv.put(socket.getLocalAddress().getAddress());
            Short localPort = (short) ((socket.getLocalPort()) & 0xFFFF);
            rsv.putShort(localPort);
            tmp = rsv.array();
        } else {
            tmp = new byte[] { 0x05, 0x01 };// 登录失败
            LOG.info("socks server need login,but no login info .");
        }
        out.write(tmp);
        out.flush();
        if (null != resultTmp && 0x02 == cmd) {
            ServerSocket ss = (ServerSocket) resultTmp;
            try {
                resultTmp = ss.accept();
            } catch (Exception e) {
            } finally {
            	CloseUtil.close(ss);
            }
        }
        return (Socket) resultTmp;
    }

    /**
     * 获取目标的服务器地址
     * 
     * @createTime 2014年12月14日 下午8:32:15
     * @param type
     * @param in
     * @return
     * @throws IOException
     */
    private String getHost(byte type, InputStream in) throws IOException {
        String host = null;
        byte[] tmp = new byte[4];
        switch (type) {
        case 0x01:// IPV4协议
            tmp = new byte[4];
            in.read(tmp);
            host = InetAddress.getByAddress(tmp).getHostAddress();
            break;
        case 0x03:// 使用域名
            int l = in.read();
            tmp = new byte[l];
            in.read(tmp);
            host = new String(tmp);
            break;
        case 0x04:// 使用IPV6
            tmp = new byte[16];
            in.read(tmp);
            host = InetAddress.getByAddress(tmp).getHostAddress();
            break;
        default:
            break;
        }
        return host;
    }
	
    public int getProtocolType(){
    	return protocolType;
    }
    
    public HttpHeader getHttpHeader(){
    	return this.httpHeader;
    }
	
	
	
	
	
	
	
	
	
}
