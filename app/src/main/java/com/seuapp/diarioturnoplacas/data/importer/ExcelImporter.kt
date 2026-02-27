package com.seuapp.diarioturnoplacas.data.importer

import android.content.ContentResolver
import android.net.Uri
import com.seuapp.diarioturnoplacas.data.db.TarefaProgramadaEntity
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import kotlin.math.max

data class ImportResult(val imported: Int, val errors: List<String>)

class ExcelImporter(private val contentResolver: ContentResolver) {
    fun importTarefas(turnoId: Int, uri: Uri): Pair<List<TarefaProgramadaEntity>, ImportResult> {
        val errors = mutableListOf<String>()
        val tarefas = mutableListOf<TarefaProgramadaEntity>()
        val formatter = DataFormatter()

        contentResolver.openInputStream(uri).use { stream ->
            if (stream == null) {
                return emptyList<TarefaProgramadaEntity>() to ImportResult(0, listOf("Nao foi possivel abrir o arquivo"))
            }
            XSSFWorkbook(stream).use { workbook ->
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(sheet.firstRowNum) ?: return emptyList<TarefaProgramadaEntity>() to ImportResult(0, listOf("Cabecalho vazio"))
                val headers = mutableMapOf<String, Int>()
                headerRow.forEachIndexed { idx, cell ->
                    headers[formatter.formatCellValue(cell).trim().lowercase()] = idx
                }

                for (rowIndex in (sheet.firstRowNum + 1)..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    try {
                        val descricao = get(row, headers, formatter, "descricao")
                        if (descricao.isBlank()) continue
                        val categoria = get(row, headers, formatter, "categoria").ifBlank { "Outros" }
                        val quantidade = get(row, headers, formatter, "quantidade").toIntOrNull() ?: 1
                        val frequencia = max(1, get(row, headers, formatter, "frequencia").toIntOrNull() ?: 1)
                        val tempoBase = get(row, headers, formatter, "tempobasemin", "tempo", "tempo_base_min").replace(',', '.').toDoubleOrNull() ?: 0.0
                        val modoRaw = get(row, headers, formatter, "modo_calculo", "modocalculo").uppercase()
                        val modo = when (modoRaw) {
                            "DIVIDIR", "MULTIPLICAR" -> modoRaw
                            else -> inferMode(frequencia, tempoBase)
                        }
                        val tempoTotal = when (modo) {
                            "DIVIDIR" -> if (frequencia == 0) tempoBase else tempoBase / frequencia
                            else -> tempoBase * frequencia
                        } * quantidade
                        val recurso = get(row, headers, formatter, "recursosugestao", "recurso_sugestao").ifBlank { null }

                        tarefas += TarefaProgramadaEntity(
                            turnoId = turnoId,
                            categoria = categoria,
                            descricao = descricao,
                            quantidade = quantidade,
                            frequencia = frequencia,
                            tempoBaseMin = tempoBase,
                            modoCalculo = modo,
                            tempoTotalMin = tempoTotal,
                            recursoSugestao = recurso
                        )
                    } catch (e: Exception) {
                        errors += "Linha ${rowIndex + 1}: ${e.message ?: "erro"}"
                    }
                }
            }
        }

        return tarefas to ImportResult(tarefas.size, errors)
    }

    private fun get(row: org.apache.poi.ss.usermodel.Row, headers: Map<String, Int>, formatter: DataFormatter, vararg keys: String): String {
        val idx = keys.firstNotNullOfOrNull { headers[it.lowercase()] } ?: return ""
        return formatter.formatCellValue(row.getCell(idx)).trim()
    }

    private fun inferMode(frequencia: Int, tempoBase: Double): String {
        return if (frequencia > 1 && tempoBase <= 240.0) "MULTIPLICAR" else "DIVIDIR"
    }
}
