package com.sunsoft.aura

import com.sunsoft.aura.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `zero or negative duration formats as 0 colon 00`() {
        assertEquals("0:00", TimeUtils.formatDuration(0))
        assertEquals("0:00", TimeUtils.formatDuration(-500))
    }

    @Test
    fun `sub-minute duration formats as m colon ss`() {
        assertEquals("0:05", TimeUtils.formatDuration(5_000))
        assertEquals("0:59", TimeUtils.formatDuration(59_000))
    }

    @Test
    fun `multi-minute duration formats as m colon ss with zero padding`() {
        assertEquals("2:05", TimeUtils.formatDuration(125_000))
        assertEquals("10:00", TimeUtils.formatDuration(600_000))
    }

    @Test
    fun `durations past an hour roll over to h colon mm colon ss`() {
        assertEquals("1:00:00", TimeUtils.formatDuration(3_600_000))
        assertEquals("1:02:03", TimeUtils.formatDuration(3_723_000))
    }

    @Test
    fun `long format summarizes to hours and minutes`() {
        assertEquals("17m", TimeUtils.formatDurationLong(1_020_000))
        assertEquals("1h 17m", TimeUtils.formatDurationLong(4_620_000))
    }
}
