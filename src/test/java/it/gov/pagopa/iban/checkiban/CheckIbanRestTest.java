package it.gov.pagopa.iban.checkiban;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import it.gov.pagopa.iban.config.RestConnectorConfig;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(
    initializers = CheckIbanRestTest.WireMockInitializer.class,
    classes = {
        CheckIbanRestConnectorImpl.class,
        RestConnectorConfig.class,
        FeignAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class
    })
@TestPropertySource(
    locations = "classpath:application.yml",
    properties = {
        "spring.application.name=pagopa-checkiban",
        "rest-client.checkiban.url=/mock/checkiban"})
class CheckIbanRestTest {

  @Autowired
  private CheckIbanRestClient client;

  @Autowired
  private CheckIbanRestConnector connector;

  private static final String FISCAL_CODE = "TRNFNC96R02H501I";
  private static final String PAY_OFF_INSTR = "IT43O0326822300052755845000";


  @Test
  void checkIban(){

    ResponseCheckIbanDTO checkIbanDTO = connector.checkIban(PAY_OFF_INSTR,FISCAL_CODE).getBody();
    assertNotNull(checkIbanDTO);

  }

  public static class WireMockInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
      wireMockServer.start();

      applicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);

      applicationContext.addApplicationListener(
          applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
              wireMockServer.stop();
            }
          });

      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
          applicationContext,
          String.format(
              "rest-client.checkiban.base-url=http://%s:%d",
              wireMockServer.getOptions().bindAddress(), wireMockServer.port()));
    }
  }
}
