package com.elara.authorizationservice.service;

import com.elara.authorizationservice.dto.request.NotificationRequest;
import com.elara.authorizationservice.dto.request.Sms5linxRequest;
import com.elara.authorizationservice.dto.response.Sms5linxResponse;
import com.elara.authorizationservice.dto.response.SmsCreditBalanceResponse;
import com.elara.authorizationservice.enums.ResponseCode;
import com.elara.authorizationservice.exception.AppException;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Slf4j
@Service
public class SmsService {

    @Value("${app.sms.5linx_sender}")
    String sms5linxSender;

    @Value("${app.sms.5linx_api_key}")
    String sms5linxApiKey;

    @Value("${app.sms.5linx_sms_url}")
    String sms5linxSmsUrl;

    @Value("${app.sms.5linx_bal_url}")
    String sms5linxBalUrl;

    private final Gson gson;

    private final RestTemplate restTemplate;

    public SmsService(Gson gson, RestTemplate restTemplate) {
        this.gson = gson;
        this.restTemplate = restTemplate;
    }

    public void sendMessage(NotificationRequest notification) {
        Sms5linxRequest request = new Sms5linxRequest();
        String to = notification.getRecipientPhone();
        if (to != null && to.length() == 11 && to.charAt(0) == '0') {
            to = "234" + to.substring(1);
            notification.setRecipientPhone(to);
        }
        request.setMessages(new ArrayList<>());
        request.getMessages().add(Sms5linxRequest.Data.builder()
                        .text(notification.getMessage())
                        .from(sms5linxSender)
                        .destinations(new ArrayList<>(){{
                            add(Sms5linxRequest.Data.Inner
                                    .builder().to(notification.getRecipientPhone()).build());}}).build());
        Sms5linxResponse response = callApi(request, Sms5linxResponse.class, HttpMethod.POST, sms5linxSmsUrl);
        log.info("SMS RESPONSE: {}", response);
    }

    public SmsCreditBalanceResponse getSmsCreditBalance() {
        return callApi(null, SmsCreditBalanceResponse.class, HttpMethod.GET, sms5linxBalUrl);
    }

    private <Response, Request> Response callApi(Request request, Class<Response> typeClass, HttpMethod httpMethod, String apiUrl) {
        String message = "System Malfunction";
        String code = ResponseCode.INTERNAL_SERVER_ERROR.getValue();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", sms5linxApiKey);
        HttpEntity<Request> entity = new HttpEntity<>(request, header);

        try {
            log.info("API URL: {}", apiUrl);
            log.info("REQUEST BODY: {}", new Gson().toJson(request));

            ResponseEntity<String> responseFromApi = restTemplate.exchange(apiUrl, httpMethod, entity, String.class);
            log.info("RESPONSE FROM API: {}", responseFromApi.getBody());

            return gson.fromJson(responseFromApi.getBody(), typeClass);
        } catch (HttpClientErrorException e) {
            log.info("HttpClientErrorException: {}", e.getResponseBodyAsString());
            message = "Validation error";
            code = ResponseCode.EXPECTATION_FAILED.getValue();
        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException: ", e);
            message = "Service not reachable";
            code = ResponseCode.INTERNAL_SERVER_ERROR.getValue();
        } catch (Exception e) {
            log.error("Exception: ", e);
        }

        throw new AppException(message, code);
    }

}