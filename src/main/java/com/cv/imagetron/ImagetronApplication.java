package com.cv.imagetron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.util.Arrays;

@SpringBootApplication
public class ImagetronApplication {
	@Bean
	public HttpMessageConverter<BufferedImage> bufferedImageHttpMessageConverter() {
		return new BufferedImageHttpMessageConverter();
	}
	public static void main(String[] args) {
		SpringApplication.run(ImagetronApplication.class, args);
	}

}
