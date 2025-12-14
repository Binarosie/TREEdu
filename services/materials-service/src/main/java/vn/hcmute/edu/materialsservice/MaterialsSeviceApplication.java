package vn.hcmute.edu.materialsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.util.TimeZone;

@SpringBootApplication
@PropertySource(value = "classpath:.env", ignoreResourceNotFound = true)
public class MaterialsSeviceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(MaterialsSeviceApplication.class, args);
	}

}
