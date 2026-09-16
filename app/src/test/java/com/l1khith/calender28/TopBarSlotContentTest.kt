package com.l1khith.calender28

import com.l1khith.calender28.domain.model.TopBarSlotContent
import org.junit.Assert.assertEquals
import org.junit.Test

class TopBarSlotContentTest {

    @Test
    fun `default option is MATRIX28`() {
        assertEquals(TopBarSlotContent.MATRIX28, TopBarSlotContent.DEFAULT)
    }

    @Test
    fun `fromId returns corresponding enum value case-insensitively`() {
        assertEquals(TopBarSlotContent.MATRIX28, TopBarSlotContent.fromId("matrix28"))
        assertEquals(TopBarSlotContent.MATRIX28, TopBarSlotContent.fromId("MATRIX28"))
        assertEquals(TopBarSlotContent.USER_NAME, TopBarSlotContent.fromId("user_name"))
        assertEquals(TopBarSlotContent.TASK_COUNT, TopBarSlotContent.fromId("task_count"))
        assertEquals(TopBarSlotContent.PENDING_TODOS, TopBarSlotContent.fromId("pending_todos"))
    }

    @Test
    fun `fromId returns default when id is null, blank, or unrecognized`() {
        assertEquals(TopBarSlotContent.MATRIX28, TopBarSlotContent.fromId(null))
        assertEquals(TopBarSlotContent.MATRIX28, TopBarSlotContent.fromId(""))
        assertEquals(TopBarSlotContent.MATRIX28, TopBarSlotContent.fromId("unknown_value"))
    }

    @Test
    fun `display names match product specifications`() {
        assertEquals("Matrix 28", TopBarSlotContent.MATRIX28.displayName)
        assertEquals("Your name", TopBarSlotContent.USER_NAME.displayName)
        assertEquals("Today's task count", TopBarSlotContent.TASK_COUNT.displayName)
        assertEquals("Pending to-dos", TopBarSlotContent.PENDING_TODOS.displayName)
    }
}
