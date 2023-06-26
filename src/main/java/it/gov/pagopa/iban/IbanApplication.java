package it.gov.pagopa.iban;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "it.gov.pagopa")
public class IbanApplication {

  public static void main(String[] args) {
    SpringApplication.run(IbanApplication.class, args);
  }

}
