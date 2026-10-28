package com.dkt.todo.data

data class Task(
    val id: Long,
    val title: String,
    val done: Boolean = false,
    val important: Boolean = false,
)
