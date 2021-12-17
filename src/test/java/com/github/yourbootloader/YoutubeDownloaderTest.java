package com.github.yourbootloader;

import com.github.yourbootloader.config.YDConfig;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RunWith(SpringRunner.class)
@Import(YDConfig.class)
@SpringBootTest
public @interface YoutubeDownloaderTest {
}