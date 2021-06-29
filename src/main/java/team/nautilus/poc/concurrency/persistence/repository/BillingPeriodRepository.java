package team.nautilus.poc.concurrency.persistence.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;

@Repository
public interface BillingPeriodRepository extends JpaRepository<BillingPeriod, Long> {

	@Query(value = "select max(b.billingDate) "
			+ "from "
			+ "BillingPeriod b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "order by b.billingDate desc ")
	LocalDate getCurrentBillingPeriodByAccountId(@Param("accountId") Long accountId);

	@Query(value = "select "
			+ "distinct b "
			+ "from "
			+ "BillingPeriod b "
			+ "order by b.billingDate desc ")
	List<BillingPeriod> getAllLatestBillingPeriods();

	@Query(value = "select "
			+ "distinct b "
			+ "from "
			+ "BillingPeriod b "
//			+ "where "
//			+ "b.billingDate < :from "
			+ "order by b.billingDate desc ")
	Page<BillingPeriod> getAllLatestBillingPeriods(Pageable pageable);
	
}
