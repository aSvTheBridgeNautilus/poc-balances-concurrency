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

	
	@Query(value = "select sum(b.amount) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId ")
	Double getLastCycleBalanceByAccountId(@Param("accountId") Long accountId);

	@Query(value = "select b "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId ")
	List<Balance> findAllByAccountId(@Param("accountId") Long accountId, Pageable pageable);
	
	@Query(value = "select max(b.version) "
			+ "from "
			+ "Balance b "
			+ "where "
			+ "b.accountId = :accountId "
			)
	Long getCurrentBalanceVersionByAccountId(@Param("accountId") Long accountId);

	@Query(value = "update "
			+ "Balance b "
			+ "set "
			+ "b.version = :version "
			+ "where "
			+ "b.accountId = :accountId "
			)
	Long updateMovementsVersionByAccountId(@Param("accountId") Long accountId, @Param("version") Long version);

}
