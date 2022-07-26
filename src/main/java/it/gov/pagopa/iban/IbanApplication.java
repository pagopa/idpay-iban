package it.gov.pagopa.iban;

import it.gov.pagopa.iban.checkiban.DecryptRest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(clients = DecryptRest.class)
@SpringBootApplication
public class IbanApplication {

  public static void main(String[] args) {
    SpringApplication.run(IbanApplication.class, args);
  }

}
