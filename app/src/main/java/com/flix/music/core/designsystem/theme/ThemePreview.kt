package com.flix.music.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun Swatch(name: String, color: Color){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(32.dp)
                .background(color, RoundedCornerShape(8.dp))
        )
        Text(text = name, color = FlixTheme.colors.textPrimary)
    }
}

@Composable
private fun ColorSwatches() {
    val color = FlixTheme.colors
    Column(
        modifier = Modifier
            .background(color.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Swatch("primary", color.primary)
        Swatch("primaryContent", color.primaryContent)
        Swatch("surface", color.surface)
        Swatch("surfaceElevated", color.surfaceElevated)
        Swatch("surfaceVariant", color.surfaceVariant)
        Swatch("textSecondary", color.textSecondary)
        Swatch("textTertiary", color.textTertiary)
        Swatch("success", color.success)
        Swatch("error", color.error)
    }
}

@Composable
private fun TypeSpecimen() {
    val t = FlixTheme.typography
    Column(
        modifier = Modifier
            .background(FlixTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Ride the Lightning", style = t.display, color = FlixTheme.colors.textPrimary)
        Text("Buenas noches", style = t.headline, color = FlixTheme.colors.textPrimary)
        Text("Fade To Black", style = t.titleLarge, color = FlixTheme.colors.textPrimary)
        Text("Escuchado recientemente", style = t.title, color = FlixTheme.colors.textPrimary)
        Text("For Whom the Bell Tolls", style = t.subtitle, color = FlixTheme.colors.textPrimary)
        Text("Busca canciones, artistas o álbumes.", style = t.body, color = FlixTheme.colors.textPrimary)
        Text("Metallica · Ride the Lightning", style = t.bodySmall, color = FlixTheme.colors.textSecondary)
        Text("Reproducir", style = t.label, color = FlixTheme.colors.primaryContent)
        Text("2:41 · 6:57", style = t.caption, color = FlixTheme.colors.textSecondary)
        Text("HOY".uppercase(), style = t.overline, color = FlixTheme.colors.textSecondary)
    }
}

@Preview(name = "Dark")
@Composable
private fun ColorsDarkPreview() {
    FlixTheme(themeMode = ThemeMode.Dark) { ColorSwatches() }
}

@Preview(name = "Light")
@Composable
private fun ColorsLightPreview() {
    FlixTheme(themeMode = ThemeMode.Light) { ColorSwatches() }
}

@Preview(name = "Tipografía", widthDp = 360)
@Composable
private fun TypePreview() {
    FlixTheme(themeMode = ThemeMode.Dark) { TypeSpecimen() }
}
