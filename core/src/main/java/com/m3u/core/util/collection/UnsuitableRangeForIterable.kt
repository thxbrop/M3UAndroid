package com.m3u.core.util.collection

class UnsuitableRangeForIterable(
    iterable: Iterable<Any?>,
    range: IntRange,
    tryUntil: Boolean = false
) : Exception(
    buildString {
        append("The range($range) is not suit for your iterable($iterable).")
        if (tryUntil) append(" Try \"util\" instead of \"..\".")
    }
)