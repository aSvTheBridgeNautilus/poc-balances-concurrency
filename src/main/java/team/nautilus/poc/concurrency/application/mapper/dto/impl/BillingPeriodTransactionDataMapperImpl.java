package team.nautilus.poc.concurrency.application.mapper.dto.impl;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import team.nautilus.poc.concurrency.application.dto.BillingPeriodTransactionData;
import team.nautilus.poc.concurrency.application.mapper.dto.BillingPeriodTransactionDataMapper;

@Component
public class BillingPeriodTransactionDataMapperImpl implements BillingPeriodTransactionDataMapper {

	@Override
	@SneakyThrows
	public List<Object[]> toEntity(BillingPeriodTransactionData dto) {
		throw new OperationNotSupportedException();
	}

	@Override
	public BillingPeriodTransactionData toDTO(List<Object[]> entity) {
		if (entity.get(0)[1] == null) {
			return null;
		}
		
		Long count = ((Number) entity.get(0)[1]).longValue();
		Double sum = count > 0 ? ((Number) entity.get(0)[0]).doubleValue() : 0;
		
		return BillingPeriodTransactionData
				.builder()
				.count(count)
				.balance(sum)
				.build();
	}

}
