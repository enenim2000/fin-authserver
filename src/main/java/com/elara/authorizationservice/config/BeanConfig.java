package com.elara.authorizationservice.config;

import com.elara.authorizationservice.util.PasswordEncoder;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BeanConfig {

  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    return modelMapper;
  }

  @Bean
  public Gson gson() {
    return new Gson();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /*@Bean
  public CorsFilter corsFilter() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(List.of("*"));
    config.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
    config.setExposedHeaders(List.of("Access-Control-Allow-Origin"));
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }*/
}
