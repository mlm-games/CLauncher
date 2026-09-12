package app.clauncher.helper

import android.graphics.Color


object ColorPalette {
    val options: List<Pair<Int, String>> = listOf(
        Color.WHITE to "White",
        Color.BLACK to "Black",
        Color.parseColor("#FFF5F5F5") to "Light Gray",
        Color.parseColor("#FF9E9E9E") to "Gray",
        Color.parseColor("#FF424242") to "Dark Gray",
        Color.parseColor("#FFFF5252") to "Red",
        Color.parseColor("#FFE91E63") to "Pink",
        Color.parseColor("#FF9C27B0") to "Purple",
        Color.parseColor("#FF673AB7") to "Deep Purple",
        Color.parseColor("#FF3F51B5") to "Indigo",
        Color.parseColor("#FF2196F3") to "Blue",
        Color.parseColor("#FF03A9F4") to "Light Blue",
        Color.parseColor("#FF00BCD4") to "Cyan",
        Color.parseColor("#FF009688") to "Teal",
        Color.parseColor("#FF4CAF50") to "Green",
        Color.parseColor("#FF8BC34A") to "Light Green",
        Color.parseColor("#FFCDDC39") to "Lime",
        Color.parseColor("#FFFFEB3B") to "Yellow",
        Color.parseColor("#FFFFC107") to "Amber",
        Color.parseColor("#FFFF9800") to "Orange",
        Color.parseColor("#FFFF5722") to "Deep Orange",
        Color.parseColor("#FF795548") to "Brown"
    )
}
