package cn.living.sharecenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class ShareCenterProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareCenterProxyApplication.class, args);
	}

}
