/*
 * 
 */

package team.nautilus.poc.concurrency.persistence.model.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import javax.persistence.AttributeConverter;

/**
 *
 * @author Antonio Salazar Valero Created on : Mar 23, 2021, 10:05:11 PM
 */

public class Instant2TimestampConverter implements AttributeConverter<Instant, Timestamp> {

	@Override
	public Timestamp convertToDatabaseColumn(Instant attribute) {
		
		if (attribute == null) {
			return null;
		}
		LocalDateTime ldt = LocalDateTime.ofInstant(attribute, ZoneId.of("UTC"));
		return Timestamp.valueOf(ldt);
	}

	@Override
	public Instant convertToEntityAttribute(Timestamp dbData) {
		if (dbData == null) {
			return null;
		}
		
		return dbData.toLocalDateTime().toInstant(ZoneOffset.UTC);
	}

}
