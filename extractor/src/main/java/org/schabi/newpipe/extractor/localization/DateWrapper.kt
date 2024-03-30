package org.schabi.newpipe.extractor.localization

import java.io.Serializable
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * A wrapper class that provides a field to describe if the date/time is precise or just an
 * approximation.
 */
class DateWrapper @JvmOverloads constructor(@Nonnull offsetDateTime: OffsetDateTime?,
                                            /**
                                             * @return if the date is considered is precise or just an approximation (e.g. service only
                                             * returns an approximation like 2 weeks ago instead of a precise date).
                                             */
                                            val isApproximation: Boolean = false) : Serializable {
    @Nonnull
    private val offsetDateTime: OffsetDateTime

    @Deprecated("Use {@link #DateWrapper(OffsetDateTime)} instead.")
    constructor(@Nonnull calendar: Calendar) : this(calendar, false)

    @Deprecated("Use {@link #DateWrapper(OffsetDateTime, boolean)} instead.")
    constructor(@Nonnull calendar: Calendar, isApproximation: Boolean) : this(OffsetDateTime.ofInstant(calendar.toInstant(), ZoneOffset.UTC), isApproximation)

    init {
        this.offsetDateTime = offsetDateTime!!.withOffsetSameInstant(ZoneOffset.UTC)
    }

    /**
     * @return the wrapped date/time as a [Calendar].
     */
    @Nonnull
    @Deprecated("use {@link #offsetDateTime()} instead.")
    fun date(): Calendar {
        return GregorianCalendar.from(offsetDateTime.toZonedDateTime())
    }

    /**
     * @return the wrapped date/time.
     */
    @Nonnull
    fun offsetDateTime(): OffsetDateTime {
        return offsetDateTime
    }
}
