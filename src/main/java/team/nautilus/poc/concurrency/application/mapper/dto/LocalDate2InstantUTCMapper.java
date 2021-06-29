package team.nautilus.poc.concurrency.application.mapper.dto;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import team.nautilus.poc.concurrency.application.mapper.GenericMapper;

@Component
public interface LocalDate2InstantUTCMapper extends GenericMapper<Instant, LocalDate> {

}
