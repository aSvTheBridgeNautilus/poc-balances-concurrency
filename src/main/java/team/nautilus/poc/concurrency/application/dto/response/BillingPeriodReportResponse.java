package team.nautilus.poc.concurrency.application.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingPeriodReportResponse {
	
	private Integer total;
	
	private List<BillingPeriodPOCResponse> periods;

}
