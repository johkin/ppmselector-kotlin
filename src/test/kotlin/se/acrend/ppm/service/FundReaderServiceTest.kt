package se.acrend.ppm.service

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 *
 */
internal class FundReaderServiceTest {

    @Test
    fun plusWeekDaysMonday() {

        val result = LocalDate.of(2017, 9, 25).plusWeekDays(3)

        assertThat(result, equalTo(LocalDate.of(2017, 9, 28)))
    }

    @Test
    fun plusWeekDaysFriday() {

        val result = LocalDate.of(2017, 9, 22).plusWeekDays(3)

        assertThat(result, equalTo(LocalDate.of(2017, 9, 27)))
    }

    @Test
    fun plusWeekDaysSaturday() {

        val result = LocalDate.of(2017, 9, 23).plusWeekDays(3)

        assertThat(result, equalTo(LocalDate.of(2017, 9, 28)))
    }

    @Test
    fun plusWeekDaysSunday() {

        val result = LocalDate.of(2017, 9, 24).plusWeekDays(3)

        assertThat(result, equalTo(LocalDate.of(2017, 9, 28)))
    }

}