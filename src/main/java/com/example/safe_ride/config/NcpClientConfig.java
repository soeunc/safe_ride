package com.example.safe_ride.config;

import com.example.safe_ride.safe.service.NcpMapApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


@Slf4j
@Configuration
public class NcpClientConfig {
    // NCP Map Rest Client
    private static final String NCP_APIGW_KEY_ID = "X-NCP-APIGW-API-KEY-ID";
    private static final String NCP_APIGW_KEY = "X-NCP-APIGW-API-KEY";

    // geoLocation
    private static final String X_TIMESTAMP_HEADER = "x-ncp-apigw-timestamp";
    private static final String X_IAM_ACCESS_KEY = "x-ncp-iam-access-key";
    private static final String X_APIGW_SIGNATURE = "x-ncp-apigw-signature-v2";

    // map
    @Value("${ncp.api.client-id}")
    private String ncpMApClientId;
    @Value("${ncp.api.client-secret}")
    private String ncpMapClientSecret;

    // map
    @Bean
    public RestClient ncpMapClient() {
        return RestClient.builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .defaultHeader(NCP_APIGW_KEY_ID, ncpMApClientId)
                .defaultHeader(NCP_APIGW_KEY, ncpMapClientSecret)
                .build();
    }

    // map
    // http 요청을 보내는 프록시 객체
    // NcpMapApiService 구현체가 빈 객체로 등록이 된다.
    @Bean
    public NcpMapApiService mapApiService() {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(ncpMapClient()))
                .build()
                .createClient(NcpMapApiService.class);
    }
}
