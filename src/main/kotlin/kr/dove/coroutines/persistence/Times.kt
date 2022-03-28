package kr.dove.coroutines.persistence

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import java.time.ZonedDateTime

//  audit time fields
abstract class Times {
    @CreatedBy var created: ZonedDateTime = ZonedDateTime.now()
        private set
    @LastModifiedBy var modified: ZonedDateTime = ZonedDateTime.now()
        private set
}