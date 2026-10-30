package com.baltajmn.bullet

import com.baltajmn.bullet.data.ImportProblem
import com.baltajmn.bullet.data.ListPacket
import com.baltajmn.bullet.data.PacketEntry
import com.baltajmn.bullet.data.PacketRead
import com.baltajmn.bullet.data.ReadBackup
import com.baltajmn.bullet.data.Received
import com.baltajmn.bullet.data.encodePacket
import com.baltajmn.bullet.data.packList
import com.baltajmn.bullet.data.packetFileName
import com.baltajmn.bullet.data.parseBackup
import com.baltajmn.bullet.data.readPacket
import com.baltajmn.bullet.data.receive
import com.baltajmn.bullet.data.receivePreview
import com.baltajmn.bullet.model.Bullet
import com.baltajmn.bullet.model.BulletCollection
import com.baltajmn.bullet.model.CollectionKind
import com.baltajmn.bullet.model.Entry
import com.baltajmn.bullet.model.Journal
import com.baltajmn.bullet.model.Place
import com.baltajmn.bullet.model.Settings
import com.baltajmn.bullet.model.Signifier
import com.baltajmn.bullet.model.TaskStatus
import com.baltajmn.bullet.model.TrackerRow
import com.baltajmn.bullet.model.entriesAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.YearMonth

private const val NOW = 5_000L
private val october = YearMonth(2026, 10)
private val trip = Place.InCollection("c-trip")

private fun item(id: String, order: Int, status: TaskStatus = TaskStatus.OPEN, updatedAt: Long = 100L, bullet: Bullet = Bullet.TASK) = Entry(
    id = id, bullet = bullet, text = "texto $id", status = status, place = trip, order = order, createdAt = 10L, updatedAt = updatedAt,
)

private val tripList = BulletCollection(id = "c-trip", title = "Viaje a Lisboa", createdAt = 1L, updatedAt = 50L, icon = "travel", theme = "viajes")

private fun sender() = Journal(
    collections = listOf(tripList),
    entries = listOf(
        item("e-1", 0, TaskStatus.DONE).copy(signifiers = setOf(Signifier.PRIORITY)),
        item("e-2", 1).copy(from = "e-elsewhere"),
        item("e-3", 2, bullet = Bullet.NOTE),
        Entry(id = "e-gone", text = "", place = trip, createdAt = 1L, updatedAt = 1L, gone = true),
    ),
)

/** Ids handed out in order, so a test can say which one each new thing got. */
private fun ids(): (String) -> String {
    var n = 0
    return { prefix -> "$prefix-new${n++}" }
}

/** Test 50 (#75): what goes out, and that it reads back the same. */
class ListPacketTest {

    @Test
    fun asItIsKeepsIdsStatesAndOrderButNothingElse() {
        val p = packList(sender(), "c-trip", blank = false, now = NOW, newId = ids())!!
        assertEquals("c-trip", p.collectionId)
        assertEquals("Viaje a Lisboa", p.title)
        assertEquals("travel", p.icon)
        assertEquals(listOf("e-1", "e-2", "e-3"), p.entries.map { it.id })
        assertEquals(TaskStatus.DONE, p.entries[0].status)
        assertEquals(setOf(Signifier.PRIORITY), p.entries[0].signifiers)
        assertEquals(false, p.blank)
        // Neither the theme, a skeleton nor where a task came from ever travels.
        val text = encodePacket(p)
        assertTrue("viajes" !in text && "e-elsewhere" !in text && "e-gone" !in text)
    }

    @Test
    fun blankGivesNewIdsAndEverythingOpen() {
        val p = packList(sender(), "c-trip", blank = true, now = NOW, newId = ids())!!
        assertEquals("c-new0", p.collectionId)
        assertEquals(listOf("e-new1", "e-new2", "e-new3"), p.entries.map { it.id })
        assertTrue(p.entries.all { it.status == TaskStatus.OPEN && it.createdAt == NOW && it.updatedAt == NOW })
        assertEquals(setOf(Signifier.PRIORITY), p.entries[0].signifiers)
        assertTrue(p.blank)
    }

    @Test
    fun aTrackerAlwaysGoesBlankWithItsRowsAndNoMarks() {
        val water = BulletCollection(
            id = "c-water", title = "Agua", createdAt = 1L, kind = CollectionKind.TRACKER, month = october,
            rows = listOf(TrackerRow("r-1", "Vasos", setOf(1, 2, 3)), TrackerRow("r-2", "Paseo", setOf(5))),
        )
        val p = packList(Journal(collections = listOf(water)), "c-water", blank = false, now = NOW, newId = ids())!!
        assertTrue(p.blank)
        assertEquals(CollectionKind.TRACKER, p.kind)
        assertEquals(listOf("Vasos", "Paseo"), p.rows)
        assertTrue(p.entries.isEmpty())
        assertTrue("\"days\"" !in encodePacket(p))
    }

    @Test
    fun nothingToSendIsNull() {
        val empty = Journal(collections = listOf(tripList))
        assertNull(packList(empty, "c-trip", blank = false, now = NOW, newId = ids()))
        assertNull(packList(empty, "c-nope", blank = false, now = NOW, newId = ids()))
    }

    @Test
    fun readsBackWhatItWrote() {
        val p = packList(sender(), "c-trip", blank = false, now = NOW, newId = ids())!!
        assertEquals(PacketRead.Ok(p), readPacket(encodePacket(p)))
    }

    @Test
    fun refusesWhatItCannotTrust() {
        assertEquals(PacketRead.TooNew, readPacket("""{"bobbin":2,"id":"c-1","t":"x","u":1}"""))
        assertEquals(PacketRead.Damaged, readPacket("""{"bobbin":1,"id":"","t":"x","u":1,"e":[{"i":"e-1","x":"a","c":1,"u":1}]}"""))
        assertEquals(PacketRead.Damaged, readPacket("""{"bobbin":1,"id":"c-1","t":"x","u":1,"e":[{"i":"e-1","x":"a","c":1,"u":1},{"i":"e-1","x":"b","c":1,"u":1}]}"""))
        assertEquals(PacketRead.Damaged, readPacket("""{"bobbin":1,"id":"c-1","t":"x","u":1,"e":[{"i":"e-1","b":"rocket","x":"a","c":1,"u":1}]}"""))
        assertEquals(PacketRead.Damaged, readPacket("hola"))
        assertEquals(PacketRead.Empty, readPacket("""{"bobbin":1,"id":"c-1","t":"x","u":1}"""))
    }

    @Test
    fun aNoteNeverArrivesClosedAndAnUnknownIconIsDropped() {
        val read = readPacket("""{"bobbin":1,"id":"c-1","t":"x","u":1,"n":"rocket","e":[{"i":"e-1","b":"note","s":"done","x":"a","c":1,"u":1}]}""")
        val p = assertIs<PacketRead.Ok>(read).packet
        assertEquals(TaskStatus.OPEN, p.entries.single().status)
        assertNull(p.icon)
    }

    @Test
    fun importingTheFileOpensTheListSheetAndNotTheBackupMerge() {
        val p = packList(sender(), "c-trip", blank = false, now = NOW, newId = ids())!!
        assertIs<ReadBackup.SentList>(parseBackup(encodePacket(p)))
        assertEquals(ReadBackup.Failed(ImportProblem.LIST_TOO_NEW), parseBackup("""{"bobbin":9}"""))
    }

    @Test
    fun theFileIsNamedAfterTheList() {
        assertEquals("viaje-a-lisboa.bobbin", packetFileName("Viaje a Lisboa"))
        assertEquals("lecturas-2026.bobbin", packetFileName("  Lecturas, 2026! "))
        assertEquals("bobbin-list.bobbin", packetFileName("🙂"))
    }
}

/** Test 51 (#75): receiving never deletes, and the same list back again merges. */
class ReceiveListTest {

    private fun packet(vararg entries: PacketEntry, updatedAt: Long = 50L) =
        ListPacket(collectionId = "c-trip", title = "Viaje a Lisboa", updatedAt = updatedAt, entries = entries.toList())

    private fun sent(id: String, status: TaskStatus = TaskStatus.OPEN, updatedAt: Long = 100L, text: String = "texto $id") =
        PacketEntry(id = id, status = status, text = text, createdAt = 10L, updatedAt = updatedAt)

    @Test
    fun aNewListComesInAsNewInTheIndexWithItsEntriesInOrder() {
        val mine = Journal(settings = Settings(cover = "rose"))
        val p = packet(sent("e-1"), sent("e-2"))
        assertEquals(Received.New(2), receivePreview(mine, p))
        val after = mine.receive(p, NOW, october, ids())
        val list = after.collections.single()
        assertEquals("c-trip", list.id)
        assertEquals(NOW, list.createdAt)
        assertEquals(listOf("e-1", "e-2"), after.entriesAt(trip).map { it.id })
        assertEquals("rose", after.settings.cover)
    }

    @Test
    fun theSameListBackMergesWithoutDeletingAnything() {
        val mine = Journal(
            collections = listOf(tripList.copy(archived = true)),
            entries = listOf(item("e-1", 0), item("e-2", 1), item("e-mine", 2)),
            settings = Settings(cover = "rose"),
        )
        // The other side ticked e-1 off and added e-3.
        val p = packet(sent("e-1", TaskStatus.DONE, updatedAt = 200L), sent("e-2"), sent("e-3", updatedAt = 200L), updatedAt = 40L)
        assertEquals(Received.Merge(added = 1, updated = 1, same = 1), receivePreview(mine, p))
        val after = mine.receive(p, NOW, october, ids())
        assertEquals(TaskStatus.DONE, after.entries.single { it.id == "e-1" }.status)
        // What only the receiver had stays, and so do its archive and theme.
        assertTrue(after.entries.any { it.id == "e-mine" })
        assertEquals(listOf("e-1", "e-2", "e-mine", "e-3"), after.entriesAt(trip).map { it.id })
        assertTrue(after.collections.single().archived)
        assertEquals("viajes", after.collections.single().theme)
        assertEquals("rose", after.settings.cover)
    }

    @Test
    fun aTaskDoneHereStaysDoneWhenItComesBackOpen() {
        val mine = Journal(collections = listOf(tripList), entries = listOf(item("e-1", 0, TaskStatus.DONE, updatedAt = 100L)))
        val p = packet(sent("e-1", TaskStatus.OPEN, updatedAt = 300L, text = "otro texto"))
        val after = mine.receive(p, NOW, october, ids())
        val e1 = after.entries.single { it.id == "e-1" }
        assertEquals(TaskStatus.DONE, e1.status)
        assertEquals("otro texto", e1.text)
    }

    @Test
    fun aTrackerArrivesAsThisMonthsPageWithoutMarks() {
        val p = ListPacket(collectionId = "c-water", title = "Agua", kind = CollectionKind.TRACKER, updatedAt = 1L, blank = true, rows = listOf("Vasos", "Paseo"))
        val after = Journal().receive(p, NOW, october, ids())
        val page = after.collections.single()
        assertEquals(october, page.month)
        assertEquals(listOf("Vasos", "Paseo"), page.rows.map { it.title })
        assertTrue(page.rows.all { it.days.isEmpty() })
    }
}
