package org.isegodin.spring.rest.socket.template.app.controller;

import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.isegodin.spring.security.template.app.data.TempData;
import org.isegodin.spring.security.template.system.security.data.AuthRequestDto;
import org.isegodin.spring.security.template.system.security.data.AuthResponseDto;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author isegodin
 */
public class TempLoginTest {

    @Test
    public void test_01() {

        TestService loginService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(TestService.class, "http://localhost:8080");

        AuthResponseDto authResponseDto = loginService.login(AuthRequestDto.builder()
                .username("admin")
                .password("admin")
                .build());

//        TempData test = testText("Test2", "asd");

        TempData test2 = testText("Test2", authResponseDto.getTokenId());

        logout(authResponseDto.getTokenId());

        try {
            TempData test3 = testText("Test2", authResponseDto.getTokenId());
        } catch (Exception e) {
            //
        }

        authResponseDto = loginService.login(AuthRequestDto.builder()
                .username("admin")
                .password("admin")
                .build());

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(2));
        } catch (InterruptedException e) {
            //
        }

        testText("Test2", authResponseDto.getTokenId());


//        TempData test3 = loginService.getData("Test");


        System.out.println();
    }

    private TempData testText(String name, String tokenId) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .setReadTimeout(Duration.ofMinutes(1))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("token-id", tokenId);

        ResponseEntity<TempData> response = restTemplate.exchange(
                "http://localhost:8080/test/data?name=" + name,
                HttpMethod.GET,
                new HttpEntity<>(
                        null,
                        headers
                ),
                TempData.class
        );
        return response.getBody();
    }

    private void logout(String tokenId) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .setReadTimeout(Duration.ofMinutes(1))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("token-id", tokenId);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/logout",
                HttpMethod.POST,
                new HttpEntity<>(
                        null,
                        headers
                ),
                String.class
        );
    }

    interface TestService {

        @RequestLine("POST /login")
        @Headers("Content-Type: application/json")
        AuthResponseDto login(AuthRequestDto request);

        @RequestLine("POST /logout")
        void logout();

        @RequestLine("GET /test/data?name={name}")
        TempData getData(@Param("name") String name);

    }
}
