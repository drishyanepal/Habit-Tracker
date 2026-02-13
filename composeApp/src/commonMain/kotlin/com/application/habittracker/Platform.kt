package com.application.habittracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform