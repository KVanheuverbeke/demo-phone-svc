package be.demo.normalizephone;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "${info.build.name}", version = "${info.build.version}", description = "${info.build.description}"
        , contact = @Contact(name = "${info.contact.name}", url = "${info.contact.url}", email = "${info.contact.email}")))
public class NormalizePhoneApplication {

    public static void main(String[] args) {
        SpringApplication.run(NormalizePhoneApplication.class, args);
    }

}