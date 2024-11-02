package org.nema;

import org.nema.controller.AppUserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
// We use direct @Import instead of @ComponentScan to speed up cold starts
// @ComponentScan(basePackages = "org.nema.controller")
@Import({ AppUserController.class })
public class YonniMaApplication {

    public static void main(String[] args) {
        SpringApplication.run(YonniMaApplication.class, args);
    }
}