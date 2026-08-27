package com.grindhouse.focus

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    var isChecked: Boolean = false
)
