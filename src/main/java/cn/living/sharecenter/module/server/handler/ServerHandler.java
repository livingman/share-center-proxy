package cn.living.sharecenter.module.server.handler;

import cn.living.sharecenter.utils.PropertiesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.codec.binary.Base64;

/**
 *@Author living
 *@Description //TODO
 *@Date 16:42 2019/9/20
 *@Param 
 *@return 
 **/
public class ServerHandler extends ChannelInboundHandlerAdapter {
    public final static HttpResponseStatus SUCCESS = new HttpResponseStatus(200,
            "Connection established");
    private final static Log LOG = LogFactory.getLog("PROXY");


    //保证线程安全
    private ChannelFuture cf;
    private int PORT ;
    private String HOST ;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("服务器连接成功......");
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {

        if(msg instanceof HttpObject){
            HttpRequest req = (HttpRequest) msg;
            String s = req.headers().get("Proxy-Authorization");  //校验账号密码
            if(s == null){
                //校验失败返回提示
                ctx.writeAndFlush(authValidateFailed());
                return;
            }
            try {
                //解析账号密码
                String[] split = s.split(" ");
                byte[] decode = Base64.decodeBase64(split[1]);
                String userNamePassWord = new String(decode);
                String[] split1 = userNamePassWord.split(":", 2);
                boolean flag = PropertiesUtil.getProperty(split1[0]).equals(split1[1]);
                if(!flag){
                    ctx.writeAndFlush(authValidateFailed());
                }
            } catch (Exception e) {
                LOG.info("Authenticated failed"+e.getMessage());
                ctx.writeAndFlush(authValidateFailed());
                return;
            }

        }
        //http
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String host = request.headers().get("host");
            String[] temp = host.split(":");
            int port = 80;
            if (temp.length > 1) {
                port = Integer.parseInt(temp[1]);
            } else {
                if (request.uri().indexOf("https") == 0) {
                    port = 443;
                }
            }
            this.HOST = temp[0];
            this.PORT = port;
            if ("CONNECT".equalsIgnoreCase(request.method().name())) {//HTTPS建立代理握手
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, SUCCESS);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("httpCodec");
                ctx.pipeline().remove("httpObject");
                return;
            }
            //连接至目标服务器
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new HttpProxyInitializer(ctx.channel()));

            ChannelFuture cf = bootstrap.connect(temp[0], port);
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });
        } else { //https,只转发数据，不对数据做处理，所以不需要解密密文
            //代理连接还未建立
            if (cf == null) {
                //连接至目标服务器
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(ctx.channel().eventLoop()) // 复用客户端连接线程池
                        .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                        .handler(new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {
                                        ctx.channel().writeAndFlush(msg);
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        LOG.info("https 代理服务器连接成功...");
                                    }
                                });
                            }
                        });
                cf = bootstrap.connect(HOST, PORT);
                cf.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().writeAndFlush(msg);
                        } else {
                            ctx.channel().close();
                        }
                    }
                });
            } else {
                //代理建立连接之后，直接刷回数据
                cf.channel().writeAndFlush(msg);
            }
        }

    }

    private FullHttpResponse authValidateFailed(){
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
        resp.headers().add("Proxy-Authenticate", "Basic realm=\"Text\"");
        resp.headers().setInt("Content-Length", resp.content().readableBytes());
        return resp;
    }
}
