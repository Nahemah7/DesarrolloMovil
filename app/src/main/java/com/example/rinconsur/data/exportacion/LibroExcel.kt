package com.example.rinconsur.data.exportacion

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LibroExcel {

    private val hojas = mutableListOf<Par>()

    private class Par(val nombre: String, val filas: List<List<Any?>>)

    fun hoja(nombre: String, filas: List<List<Any?>>): LibroExcel {
        hojas.add(Par(nombre, filas))
        return this
    }

    fun guardar(destino: File) {
        destino.parentFile?.mkdirs()
        ZipOutputStream(destino.outputStream().buffered()).use { zip ->
            escribir(zip, "[Content_Types].xml", tipos())
            escribir(zip, "_rels/.rels", relacionesRaiz())
            escribir(zip, "xl/workbook.xml", libro())
            escribir(zip, "xl/_rels/workbook.xml.rels", relacionesLibro())
            escribir(zip, "xl/styles.xml", estilos())
            hojas.forEachIndexed { indice, hoja ->
                escribir(zip, "xl/worksheets/sheet${indice + 1}.xml", hojaXml(hoja.filas))
            }
        }
    }

    private fun escribir(zip: ZipOutputStream, ruta: String, contenido: String) {
        zip.putNextEntry(ZipEntry(ruta))
        zip.write(contenido.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun tipos(): String {
        val hojasXml = hojas.indices.joinToString("") { i ->
            "<Override PartName=\"/xl/worksheets/sheet${i + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            hojasXml +
            "</Types>"
    }

    private fun relacionesRaiz(): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "</Relationships>"

    private fun libro(): String {
        val listado = hojas.mapIndexed { indice, hoja ->
            "<sheet name=\"${escapar(hoja.nombre)}\" sheetId=\"${indice + 1}\" r:id=\"rId${indice + 1}\"/>"
        }.joinToString("")
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"" +
            " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets>$listado</sheets></workbook>"
    }

    private fun relacionesLibro(): String {
        val relaciones = hojas.indices.joinToString("") { i ->
            "<Relationship Id=\"rId${i + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet${i + 1}.xml\"/>"
        }
        val estilosId = hojas.size + 1
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            relaciones +
            "<Relationship Id=\"rId$estilosId\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
            "</Relationships>"
    }

    private fun estilos(): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<fonts count=\"2\">" +
            "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "</fonts>" +
            "<fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills>" +
            "<borders count=\"1\"><border/></borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"2\">" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>" +
            "</cellXfs>" +
            "</styleSheet>"

    private fun hojaXml(filas: List<List<Any?>>): String {
        val cuerpo = filas.mapIndexed { indiceFila, fila ->
            val celdas = fila.mapIndexed { indiceColumna, valor ->
                val referencia = columna(indiceColumna) + (indiceFila + 1)
                when (valor) {
                    null -> ""
                    is Number -> "<c r=\"$referencia\"><v>$valor</v></c>"
                    else -> "<c r=\"$referencia\" t=\"inlineStr\"><is><t>${escapar(valor.toString())}</t></is></c>"
                }
            }.joinToString("")
            "<row r=\"${indiceFila + 1}\">$celdas</row>"
        }.joinToString("")
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<sheetData>$cuerpo</sheetData></worksheet>"
    }

    private fun columna(indice: Int): String {
        var numero = indice + 1
        val letras = StringBuilder()
        while (numero > 0) {
            val resto = (numero - 1) % 26
            letras.insert(0, ('A' + resto))
            numero = (numero - 1) / 26
        }
        return letras.toString()
    }

    private fun escapar(texto: String): String = texto
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
