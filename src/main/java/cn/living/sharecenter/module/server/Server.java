package cn.living.sharecenter.module.server;

import cn.living.sharecenter.module.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 *@Author living
 *@Description //TODO
 *@Date 16:42 2019/9/20
 *@Param 
 *@return 
 **/
public class Server {
    private final int PORT;


    public Server(int PORT) {
        this.PORT = PORT;
    }

    public void start(){
        EventLoopGroup workerStateEvent = new NioEventLoopGroup();
        EventLoopGroup bossStateEvent = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try{
            bootstrap.group(bossStateEvent, workerStateEvent)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("httpCodec", new HttpServerCodec());
                            socketChannel.pipeline().addLast("httpObject", new HttpObjectAggregator(65536));
                            socketChannel.pipeline().addLast("serverHandle",new ServerHandler());
                        }
                    });

            ChannelFuture channel = bootstrap.bind(PORT).sync();
            //关闭通道
            channel.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerStateEvent.shutdownGracefully();
            workerStateEvent.shutdownGracefully();
        }

    }

}
