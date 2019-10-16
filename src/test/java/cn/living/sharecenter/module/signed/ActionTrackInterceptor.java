package cn.living.sharecenter.module.signed;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @Description TODO
 * @Author Living
 * @Date 2019/9/23 18:20
 * @Version 1.0
 **/
public class ActionTrackInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        String userAndPass = "sharecenter:v45h56h3g3y5";
        // 加入自定义字段
        headers.add("Proxy-Authorization", "Basic "+Base64.encodeBase64String(userAndPass.getBytes()));
        // 保证请求继续被执行
        return execution.execute(request, body);

    }
}
