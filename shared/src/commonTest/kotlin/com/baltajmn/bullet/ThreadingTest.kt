package com.baltajmn.bullet

import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.IndexItem
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.archiveCollection
import com.baltajmn.bullet.model.continueCollection
import com.baltajmn.bullet.model.indexItems
import com.baltajmn.bullet.model.threadNext
import com.baltajmn.bullet.model.threadPrev
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// 27. Threading (docs/tecnico.md 10, 12.1, #63).
class ThreadingTest {
    private val base = Journal(
        collections = listOf(
            BulletCollection("c-1", "Lecturas 2025", createdAt = 100),
            BulletCollection("c-2", "Viajes", createdAt = 200),
        ),
    )

    @Test
    fun continuingLinksANewListAndCreatesNoEntry() {
        val j = base.continueCollection("c-1", "Lecturas 2026", now = 300, newId = "c-3")!!
        val made = j.collections.single { it.id == "c-3" }
        assertEquals("c-1", made.threadFrom)
        assertEquals("Lecturas 2026", made.title)
        assertTrue(j.entries.isEmpty())
        assertEquals("c-3", j.threadNext(j.collections.first { it.id == "c-1" })?.id)
        assertEquals("c-1", j.threadPrev(made)?.id)
        // Only one continuation per list.
        assertNull(j.continueCollection("c-1", "Otra", now = 400, newId = "c-4"))
    }

    @Test
    fun archivingTheOriginKeepsTheLinkBothWays() {
        val j = base.continueCollection("c-1", "Lecturas 2026", now = 300, newId = "c-3")!!.archiveCollection("c-1", true, now = 400)
        val made = j.collections.single { it.id == "c-3" }
        assertEquals("c-1", j.threadPrev(made)?.id)
        assertEquals("c-3", j.threadNext(j.collections.first { it.id == "c-1" })?.id)
    }

    @Test
    fun theIndexKeepsAThreadTogetherWhereItStarted() {
        val j = base.continueCollection("c-1", "Lecturas 2026", now = 300, newId = "c-3")!!
        val items = j.indexItems { "" }.filterIsInstance<IndexItem.Collection>()
        assertEquals(listOf("c-1", "c-3", "c-2"), items.map { it.id })
        assertEquals(listOf(false, true, false), items.map { it.threaded })
    }

    @Test
    fun aTrackerDoesNotContinueThisWay() {
        val j = Journal(collections = listOf(BulletCollection("c-1", "Agua", createdAt = 1, kind = CollectionKind.TRACKER)))
        assertNull(j.continueCollection("c-1", "Agua 2", now = 2, newId = "c-2"))
    }
}
