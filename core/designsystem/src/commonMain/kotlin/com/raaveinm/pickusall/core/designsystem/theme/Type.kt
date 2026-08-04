package com.raaveinm.pickusall.core.designsystem.theme


import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.*

@Composable
fun getAppFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.inter_18pt_regular, FontWeight.Normal),
        Font(Res.font.inter_18pt_medium, FontWeight.Medium),
        Font(Res.font.inter_18pt_semibold, FontWeight.SemiBold),
        Font(Res.font.inter_18pt_bold, FontWeight.Bold),
        Font(Res.font.inter_18pt_extrabold, FontWeight.ExtraBold)
    )
}

@Composable
fun getDisplayFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.inter_24pt_regular, FontWeight.Normal),
        Font(Res.font.inter_24pt_bold, FontWeight.Bold),
        Font(Res.font.inter_24pt_black, FontWeight.Black)
    )
}

@Composable
fun getAppTypography(): Typography {
    val bodyFont = getAppFontFamily()
    val displayFont = getDisplayFontFamily()
    val baseline = Typography()

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFont),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFont),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFont),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFont),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFont),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFont),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFont),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFont),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFont),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFont),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFont),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFont),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFont),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFont),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFont)
    )
}