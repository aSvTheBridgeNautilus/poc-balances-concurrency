/*
 * NAUTILUS PROJECT
 */

package team.nautilus.poc.concurrency.persistence.repository.model.constant;

import java.util.Arrays;

/**
 *
 * @author Antonio Salazar Valero Created on : Apr 6, 2021, 1:54:36 PM
 */

public enum TransactionType {

	P2P("P2P"),
	TOP("TOP"),
	CASHOUT("CASHOUT");
	
	private final String description;

	private TransactionType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public static TransactionType getTransactionType(String value) {
		return Arrays.stream(TransactionType.values())
				.filter(v -> value.equals(v.getDescription()))
				.findFirst()
				.orElse(null);
	}

}
