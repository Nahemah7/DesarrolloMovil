package com.example.rinconsur.data.remoto

class SincronizadorCierres(
    private val pendientes: CierresPendientes,
    private val tokens: TokenPreferencias,
    private val direccion: DireccionServidor
) {

    suspend fun enviarPendientes(): Int {
        val token = tokens.leer() ?: return 0
        val cola = pendientes.leer()
        if (cola.isEmpty()) return 0
        val remoto = RepositorioSesionesRemoto(direccion.leer(), token)
        val usuario = tokens.idUsuario()
        var enviados = 0
        for ((idRemoto, guardado) in cola) {
            val partes = guardado.split("|")
            val hora = partes.getOrNull(0).orEmpty()
            val quien = partes.getOrNull(1)?.toLongOrNull()
            if (quien != null && usuario > 0 && quien != usuario) continue
            val cerrada = remoto.cerrar(idRemoto, hora)
            if (cerrada) {
                pendientes.olvidar(idRemoto)
                enviados++
            }
        }
        return enviados
    }
}
