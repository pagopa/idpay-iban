package it.gov.pagopa.iban.decrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import it.gov.pagopa.iban.config.RestConnectorConfig;
import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(
    initializers = DecryptRestTest.WireMockInitializer.class,
    classes = {
        DecryptRestConnectorImpl.class,
        RestConnectorConfig.class,
        FeignAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class
    })
@TestPropertySource(
    locations = "classpath:application.yml",
    properties = "spring.application.name=pdv-ms-tokenizer-decrypt")
class DecryptRestTest {
    private static final String FISCAL_CODE = "AAAAAA00A00A000A";
    private static final String TOKEN = "TEST_TOKEN";

    @Autowired
    private DecryptRest decryptRest;


    @Autowired
    private DecryptRestConnector restConnector;

    @Test
    void getPii() {

        final DecryptedCfDTO actualResponse = restConnector.getPiiByToken(TOKEN);

        assertNotNull(actualResponse);
        assertEquals(FISCAL_CODE, actualResponse.getPii());
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
                    "rest-client.decrypt.base-url=http://%s:%d",
                    wireMockServer.getOptions().bindAddress(), wireMockServer.port()));
        }
    }

}
