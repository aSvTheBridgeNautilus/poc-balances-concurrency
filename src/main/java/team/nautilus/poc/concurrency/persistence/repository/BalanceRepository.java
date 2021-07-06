package team.nautilus.poc.concurrency.persistence.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import team.nautilus.poc.concurrency.persistence.model.Balance;


@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

	
	@Query(value = "select sum(b.amount) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.timestamp >= :from ")
	Double getCurrentBillingPeriodBalanceByAccountId(@Param("accountId") Long accountId, @Param("from") Instant billingPeriodDate);

	@Query(value = "select sum(b.amount) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.timestamp >= :from "
			+ "and b.timestamp < :to "
			+ "")
	Double getBillingPeriodBalanceByAccountId(@Param("accountId") Long accountId, @Param("from") Instant from, @Param("to") Instant to);

	@Query(value = "select "
			+ "sum(b.amount), "
			+ "count(b.accountId) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.timestamp >= :from "
			+ "and b.movement.id >= :movementId "
			+ "")
	List<Object[]> getBillingPeriodBalanceTransactionsCountByAccountId(@Param("accountId") Long accountId, @Param("accountId") Long movementId, @Param("movementId") Instant from);


	@Query(value = "select b "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId ")
	List<Balance> findAllByAccountId(@Param("accountId") Long accountId, Pageable pageable);

	@Query(value = "select b "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.operationType = 2 "
			+ "order by b.timestamp asc ")
	List<Balance> getInitialBalanceFromAccount(@Param("accountId") Long accountId, Pageable pageable);

	@Query(value = "select "
			+ "distinct b "
			+ "from "
			+ "Balance b "
			+ "order by b.timestamp desc ")
	List<Balance> getLastMovementFromAllAccounts();

}
