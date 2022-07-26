package it.gov.pagopa.iban.checkiban;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {CheckIbanRestClient.class, DecryptRest.class})
@Slf4j
public class CheckIbanRestConnectorConfig {
  @Configuration
  @ConditionalOnClass(OkHttpClient.class)
  static class OkHttpClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClientFactory okHttpClientFactory(OkHttpClient.Builder builder) {
      return new ProxyOkHttpClientFactory(builder);
    }
  }

}
