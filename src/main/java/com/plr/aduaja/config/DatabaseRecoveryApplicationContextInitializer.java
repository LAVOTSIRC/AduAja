package com.plr.aduaja.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.jspecify.annotations.NonNull;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseRecoveryApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        DatabaseStartupRecovery.prepareDatabase();
    }
}



