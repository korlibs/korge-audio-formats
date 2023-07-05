package io.nayuki.flac.common

import korlibs.io.lang.*

class SyncRandomAccessFile(val file: SyncFile, val mode: String) : Closeable {
    fun length(): Long = TODO()
    fun seek(pos: Long) {
        TODO()
    }
    fun read(buf: ByteArray, off: Int, len: Int): Int {
        TODO()
    }
    override fun close() {
        TODO()
    }
}
