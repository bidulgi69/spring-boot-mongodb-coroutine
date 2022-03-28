package kr.dove.coroutines.persistence

import org.springframework.core.convert.converter.Converter
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class ZonedDateTimeReadConverter: Converter<Date, ZonedDateTime> {
    override fun convert(source: Date): ZonedDateTime {
        return source.toInstant().atZone(ZoneOffset.UTC)
    }
}

class ZonedDateTimeWriteConverter: Converter<ZonedDateTime, Date> {
    override fun convert(source: ZonedDateTime): Date {
        return Date.from(source.toInstant())
    }
}