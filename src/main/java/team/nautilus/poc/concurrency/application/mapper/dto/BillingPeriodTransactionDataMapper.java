package team.nautilus.poc.concurrency.application.mapper.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import team.nautilus.poc.concurrency.application.dto.BillingPeriodTransactionData;
import team.nautilus.poc.concurrency.application.mapper.GenericMapper;

@Component
public interface BillingPeriodTransactionDataMapper extends GenericMapper<BillingPeriodTransactionData, List<Object[]>> {

}
