package com.elara.authorizationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitoringService {

    public boolean coreBankingIsUp() {
        return false;
    }

    public boolean billsPaymentServiceIsUp() {
        return false;
    }

    public boolean nibbsServiceIsUp() {
        return false;
    }
}
