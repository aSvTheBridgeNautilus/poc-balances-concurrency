package team.nautilus.poc.concurrency.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;

@Repository
public interface BillingPeriodRepository extends JpaRepository<BillingPeriod, Long> {
	
	@Query(value = "select b "
			+ "from "
			+ "BillingPeriod b "
			+ "where "
			+ "b.accountId = :accountId "
//			+ "order by b.timestamp desc, b.id ")
			+ "order by b.id desc ")
	List<BillingPeriod> getCurrentBillingPeriodByAccountId(@Param("accountId") Long accountId, Pageable pageable);

	@Query(value = "select b "
			+ "from "
			+ "BillingPeriod b "
			+ "where "
			+ "b.accountId = :accountId "
//			+ "order by b.timestamp asc, b.id ")
			+ "order by b.id asc ")
	List<BillingPeriod> getAllBillingPeriodsByAccountId(@Param("accountId") Long accountId);
	
}
