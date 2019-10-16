package cn.living.sharecenter.utils;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;


public class HeaderUtil {

	/**
	 * 响应客户端
	 * @param future
	 * @param request
	 */
    public static void addHeaders(ChannelFuture future, Object request) {
        if (request instanceof HttpRequest) {
            HttpRequest msg = (FullHttpRequest) request;
            future.channel().writeAndFlush(msg);
        } else {
            future.channel().writeAndFlush(request);
        }
    }

}
