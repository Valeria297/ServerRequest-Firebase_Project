package com.template

import android.content.Context
import android.widget.Toast

fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

//-Xmx2048m -Dfile.encoding=UTF-8