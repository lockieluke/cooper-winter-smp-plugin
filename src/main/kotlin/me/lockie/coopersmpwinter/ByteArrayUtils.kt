package me.lockie.coopersmpwinter

import kotlin.math.min

fun ByteArray.chunked(chunkSize: Int): kotlin.collections.ArrayList<ByteArray> {
    val result = ArrayList<ByteArray>()
    if (chunkSize <= 0) {
        result.add(this)
    } else {
        for (chunk in this.indices step chunkSize) {
            result.add(this.copyOfRange(chunk, min(chunk + chunkSize, this.size)))
        }
    }
    return result
}