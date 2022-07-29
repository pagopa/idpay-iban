package it.gov.pagopa.iban.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpConfig {
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
