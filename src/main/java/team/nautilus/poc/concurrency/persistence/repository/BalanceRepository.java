package team.nautilus.poc.concurrency.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import team.nautilus.poc.concurrency.persistence.model.Balance;


@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

	
	@Query(value = "select max(b.movement.id) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId ")
	Long getLastMovementIdByAccoundId(@Param("accountId") Long accountId);

	@Query(value = "select sum(b.amount) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId ")
	Double getCompleteBillingPeriodBalanceByAccountId(@Param("accountId") Long accountId);

	@Query(value = "select sum(b.amount) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
//			+ "and b.timestamp > :from "
			+ "and b.id > :movementId "
			)
	Double getCurrentBillingPeriodBalanceByAccountId(
			@Param("accountId") Long accountId, 
			@Param("movementId") Long movementId
//			@Param("from") Instant billingPeriodDate
			);

	@Query(value = "select sum(b.amount) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
//			+ "and b.timestamp <= :timestamp "
			+ "and b.id <= :movementId ")
	Double getBalanceUntilBillingPeriod(
			@Param("accountId") Long accountId, 
//			@Param("timestamp") Instant timestamp, 
			@Param("movementId") Long movementId);

	@Query(value = "select "
			+ "sum(b.amount), "
			+ "count(b.id) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
//			+ "and b.timestamp > :fromTimestamp "
			+ "and b.id > :fromId "
//			+ "and b.timestamp <= :toTimestamp "
			+ "and b.id <= :toId "
			+ "")
	List<Object[]> getBillingPeriodBalanceByAccountId(
			@Param("accountId") Long accountId,
//			@Param("fromTimestamp") Instant fromTimestamp, 
//			@Param("toTimestamp") Instant toTimestamp,
			@Param("fromId") Long fromId, 
			@Param("toId") Long toId);

	@Query(value = "select "
			+ "sum(b.amount), "
			+ "count(b.accountId) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.id > :movementId ")
	List<Object[]> sumAmountCountTransactionFromBillingPeriodByAccountId(
			@Param("accountId") Long accountId, 
			@Param("movementId") Long movementId);
	
	@Query(value = "select "
			+ "sum(b.amount), "
			+ "count(b.accountId) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.id > :fromId "
			+ "and b.id <= :toId "
			+ "")
	List<Object[]> sumAmountCountTransactionsBetweenMovementsByAccountId(
			@Param("accountId") Long accountId, 
			@Param("fromId") Long fromId,
			@Param("toId") Long toId);

	@Query(value = "select "
			+ "sum(b.amount) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.id > :fromId "
			+ "and b.id <= :toId "
			+ "")
	Double sumTransactionsAmountBetweenIdsByAccountId(
			@Param("accountId") Long accountId, 
			@Param("fromId") Long fromId,
			@Param("toId") Long toId
			);

	@Query(value = "select "
			+ "count(b.accountId) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.id > :movementId "
			+ "")
	Long countTransactionsFromBillingPeriodByAccountId(
			@Param("accountId") Long accountId, 
			@Param("movementId") Long movementId
			);
	
	@Query(value = "select "
			+ "count(b.accountId) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			+ "and b.id > ("
			+ "select max(p.id.movementId) "
			+ "from "
			+ "BillingPeriod p "
			+ "where "
			+ "p.id.accountId = :accountId "
			+ ") "
			+ "")
	Long countTransactionsFromCurrentBillingPeriodByAccountId(
			@Param("accountId") Long accountId);

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
//			+ "order by b.timestamp asc ")
			+ "order by b.id asc ")
	List<Balance> getInitialBalanceFromAccount(@Param("accountId") Long accountId, Pageable pageable);

	@Query(value = "select "
			+ "distinct b "
			+ "from "
			+ "Balance b "
//			+ "order by b.timestamp desc ")
			+ "order by b.id desc ")
	List<Balance> getLastMovementFromAllAccounts();

}
