package com.baltajmn.bullet.data

/** Cancelling is not failing: the user changing their mind says nothing and shows nothing. */
enum class PickResult { Done, Cancelled, Failed }

/**
 * The system file picker, for both directions. Exporting goes through the picker and not the share
 * sheet on purpose: a backup has to land somewhere the user can find it again, not in a chat.
 */
expect object FilePicker {
    /** False until the platform has wired one up, so the UI can hide the row instead of lying. */
    val available: Boolean

    /**
     * Asks where to put the backup and hands [write] a sink into that destination. [onDone] is Done
     * only once the system confirms the file is written: a cancelled picker has saved nothing.
     */
    fun exportZip(suggestedName: String, write: (sink: (ByteArray) -> Unit) -> Unit, onDone: (PickResult) -> Unit)

    /**
     * Asks for a file and calls [read] with a pull source while it is open: the source hands back up
     * to the bytes asked for, and null at the end. [onDone] says whether a file was read at all.
     */
    fun importFile(read: (source: (Int) -> ByteArray?) -> Unit, onDone: (PickResult) -> Unit)
}
