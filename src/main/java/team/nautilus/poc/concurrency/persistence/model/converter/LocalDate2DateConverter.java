/*
 * 
 */

package team.nautilus.poc.concurrency.persistence.model.converter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.persistence.AttributeConverter;

/**
 *
 * @author Antonio Salazar Valero Created on : Mar 23, 2021, 10:05:11 PM
 */

public class LocalDate2DateConverter implements AttributeConverter<LocalDate, Date> {

	@Override
	public Date convertToDatabaseColumn(LocalDate attribute) {
		if (attribute == null) {
			return null;
		}
		return Date.valueOf(attribute);
	}

	@Override
	public LocalDate convertToEntityAttribute(Date attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toLocalDate();
	}

}
