/*
 * NAUTILUS PROJECT
 */

package team.nautilus.poc.concurrency.persistence.model.constant;

import java.util.Arrays;

/**
 *
 * @author Antonio Salazar Valero Created on : Apr 6, 2021, 1:54:36 PM
 */

public enum OperationType {

	DEBIT(1, "Debit"),
	CREDIT(2, "Credit"),
	INITIAL(3, "Initial");

	private final Integer id;
	private final String description;

	private OperationType(Integer id, String description) {
		this.id = id;
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}
	
	public static OperationType toOperationType(String type) {
		return Arrays.stream(OperationType.values())
					 .filter(v -> v.getDescription().toUpperCase().equals(type.toUpperCase()))
					 .findFirst()
					 .orElse(null);
	}
	

}
