package com.dkt.todo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dkt.todo.R
import com.dkt.todo.data.AppAppearance
import com.dkt.todo.ui.theme.LocalAppearance
import com.dkt.todo.ui.theme.paletteFor

@Composable
fun SettingsScreen(onBack: () -> Unit, onAppearanceChange: (AppAppearance) -> Unit) {
    val selected = LocalAppearance.current
    Scaffold(topBar = {
        ScreenHeader("Configurações", "Deixe o Checklist com a sua cara", showCompanion = false,
            navigation = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            })
    }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Aparência", style = MaterialTheme.typography.titleLarge)
            Text("Escolha quem vai te acompanhar nas tarefas. O app e o widget mudam juntos.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppAppearance.entries.forEach { appearance ->
                    val palette = paletteFor(appearance)
                    val active = selected == appearance
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(if (active) 2.dp else 1.dp,
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Row(
                            Modifier.fillMaxWidth()
                                .selectable(active, role = Role.RadioButton, onClick = { onAppearanceChange(appearance) })
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(Modifier.size(80.dp).background(palette.Night, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center) {
                                Image(
                                    painterResource(if (appearance == AppAppearance.KITTEN) R.drawable.kitten_companion else R.drawable.ashen_companion),
                                    contentDescription = null, modifier = Modifier.size(72.dp),
                                )
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(appearance.label, style = MaterialTheme.typography.titleMedium)
                                Text(appearance.description, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (active) Text("Em uso", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            RadioButton(selected = active, onClick = null)
                        }
                    }
                }
            }
            Text("O tema fica salvo neste celular. Suas tarefas, lembretes e histórico continuam os mesmos.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
