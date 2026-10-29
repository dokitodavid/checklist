package com.dkt.todo.data

import android.content.Context
import android.os.Looper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppearanceStoreTest {
    private lateinit var context: Context

    @Before fun clearAppearance() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun existingInstallStartsWithMage() {
        assertEquals(AppAppearance.MAGE, AppearanceStore(context).load())
    }

    @Test fun selectedThemeSurvivesReopeningAndCanSwitchBack() {
        AppearanceStore(context).save(AppAppearance.KITTEN)
        assertEquals(AppAppearance.KITTEN, AppearanceStore(context).load())
        AppearanceStore(context).save(AppAppearance.MAGE)
        assertEquals(AppAppearance.MAGE, AppearanceStore(context).load())
    }

    @Test fun unknownPreferenceFallsBackToMage() {
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
            .edit().putString("theme", "unknown").commit()
        assertEquals(AppAppearance.MAGE, AppearanceStore(context).load())
    }

    @Test fun widgetAndAppObserveTheSavedTheme() = runBlocking {
        AppearanceStore(context).save(AppAppearance.KITTEN)
        assertEquals(AppAppearance.KITTEN, AppearanceStore(context).changes().first())
    }

    @Test fun activeCollectorsReceiveThemeChanges() = runBlocking {
        val store = AppearanceStore(context)
        val observed = withTimeout(5000) {
            store.changes().onEach {
                if (it == AppAppearance.MAGE) {
                    store.save(AppAppearance.KITTEN)
                    shadowOf(Looper.getMainLooper()).idle()
                }
            }.take(2).toList()
        }
        assertEquals(listOf(AppAppearance.MAGE, AppAppearance.KITTEN), observed)
    }

    @Test fun changingThemePreservesTaskPreferences() {
        val tasks = context.getSharedPreferences("tarefas", Context.MODE_PRIVATE)
        val items = """[{"id":1,"title":"Minha tarefa","done":false}]"""
        val history = """{"2026-10-28":{"created":1,"done":0}}"""
        tasks.edit().putString("items", items).putString("activity", history).commit()
        AppearanceStore(context).save(AppAppearance.KITTEN)
        assertEquals("Minha tarefa", TaskStore(context).load().single().title)
        assertEquals(items, tasks.getString("items", null))
        assertEquals(history, tasks.getString("activity", null))
    }
}
