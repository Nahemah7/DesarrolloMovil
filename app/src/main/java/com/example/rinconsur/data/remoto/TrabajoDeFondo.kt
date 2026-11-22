package com.example.rinconsur.data.remoto

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object TrabajoDeFondo {
    val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
