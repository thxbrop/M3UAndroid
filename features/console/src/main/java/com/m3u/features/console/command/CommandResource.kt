package com.m3u.features.console.command

internal sealed class CommandResource<out T> {
    object Idle : CommandResource<Nothing>()
    data class Output(val line: String) : CommandResource<String>()
}