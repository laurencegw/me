package com.binarymonks.gonzo.core.extensions.time

import java.time.ZoneId
import java.time.ZonedDateTime


fun ZonedDateTime.normalise(): ZonedDateTime {
    return this.toInstant().atZone(ZoneId.of("UTC"))
}
