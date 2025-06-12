package io.github.bapadua.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"io.github.bapadua.jwt",           // Pacotes da aplicação principal
	"io.github.bapadua.jwt.lib"        // Pacotes da biblioteca JWT
})
public class BackendChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendChallengeApplication.class, args);
	}

}
