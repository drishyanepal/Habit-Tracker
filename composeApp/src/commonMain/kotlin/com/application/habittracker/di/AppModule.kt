package com.application.habittracker.di

import com.application.habittracker.data.db.DatabaseDriverFactory
import com.application.habittracker.data.db.HabitDatabase
import com.application.habittracker.data.preferences.AppPreferences
import com.application.habittracker.data.repository.HabitRepository
import com.application.habittracker.data.repository.HabitRepositoryImpl
import com.application.habittracker.notification.NotificationScheduler
import com.application.habittracker.ui.screen.habit.HabitFormViewModel
import com.application.habittracker.ui.screen.month.MonthViewModel
import com.application.habittracker.ui.screen.today.TodayViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun appModule(context: Any? = null) = module {
    single { DatabaseDriverFactory(context) }
    single { AppPreferences(context) }
    single { NotificationScheduler(context) }
    single { HabitDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single<HabitRepository> { HabitRepositoryImpl(get()) }
    viewModel { TodayViewModel(get(), get()) }
    viewModel { MonthViewModel(get()) }
    viewModel { HabitFormViewModel(get(), get()) }
}
