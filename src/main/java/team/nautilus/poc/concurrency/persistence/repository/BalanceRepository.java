package team.nautilus.poc.concurrency.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import team.nautilus.poc.concurrency.persistence.repository.model.Balance;


@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

	
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

}
