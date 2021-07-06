/*
 * 
 */

package team.nautilus.poc.concurrency.persistence.model.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

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

	public static void main(String... args) {
		Instant someInstant = Instant.now();
		Timestamp someStamp = null;
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		formatter = formatter.withZone(TimeZone.getTimeZone("UTC").toZoneId());
		
		System.out.println(someInstant);
		System.out.println(someInstant.toString());
		System.out.println(new Timestamp(someInstant.toEpochMilli()));
		System.out.println(LocalDateTime.ofInstant(someInstant, ZoneId.of("UTC")));
		System.out.println(someStamp = Timestamp.valueOf(LocalDateTime.ofInstant(someInstant, ZoneId.of("UTC"))));
		System.out.println(someStamp.toLocalDateTime().toInstant(ZoneOffset.UTC));
	}
}
