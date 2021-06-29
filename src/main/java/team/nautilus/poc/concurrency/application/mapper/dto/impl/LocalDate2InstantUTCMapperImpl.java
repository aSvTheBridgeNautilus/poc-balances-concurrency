package team.nautilus.poc.concurrency.application.mapper.dto.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.naming.OperationNotSupportedException;

import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;

@Component
public class LocalDate2InstantUTCMapperImpl implements LocalDate2InstantUTCMapper {

	@Override
	@SneakyThrows
	public LocalDate toEntity(Instant dto) {
		throw new OperationNotSupportedException("Not supported");
	}

	@Override
	public Instant toDTO(LocalDate entity) {
		return entity.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
	}

}
