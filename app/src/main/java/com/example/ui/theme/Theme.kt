package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * TEMA DO APP
 * -----------
 * Define dois esquemas de cores (claro e escuro) com acento verde.
 * O app escolhe automaticamente conforme o tema do sistema do celular.
 */

private val EsquemaEscuro = darkColorScheme(
    primary = VerdeDark,
    onPrimary = Color(0xFF06371A),
    primaryContainer = VerdeContainerDark,
    onPrimaryContainer = Color(0xFFB8F2C6),
    background = FundoDark,
    surface = FundoDark,
)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeLight,
    onPrimary = Color.White,
    primaryContainer = VerdeContainerLight,
    onPrimaryContainer = Color(0xFF06371A),
    background = FundoLight,
    surface = FundoLight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) EsquemaEscuro else EsquemaClaro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
