package com.seuapp.diarioturnoplacas.data.importer

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.seuapp.diarioturnoplacas.data.db.TarefaProgramadaEntity
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import kotlin.math.max

data class ImportResult(val imported: Int, val errors: List<String>)

data class ImportedFuncionario(
    val nome: String,
    val funcao: String,
    val letra: String
)

data class ImportedRecurso(
    val tag: String,
    val categoria: String,
    val admFlag: Boolean
)

data class RecursosImportData(
    val supervisors: List<String>,
    val funcoesPrevisto: Map<String, Int>,
    val funcionarios: List<ImportedFuncionario>,
    val recursos: List<ImportedRecurso>
)

class ExcelImporter(
    private val contentResolver: ContentResolver,
    private val context: Context
) {
    private val formatter = DataFormatter()

    fun importTarefas(turnoId: Int, uri: Uri): Pair<List<TarefaProgramadaEntity>, ImportResult> {
        val stream = contentResolver.openInputStream(uri)
            ?: return emptyList<TarefaProgramadaEntity>() to ImportResult(0, listOf("Nao foi possivel abrir o arquivo"))

        stream.use {
            return parseTarefasWorkbook(turnoId, it.readBytes())
        }
    }

    fun importTarefasFromAssets(turnoId: Int, assetPath: String): List<TarefaProgramadaEntity> {
        return runCatching {
            context.assets.open(assetPath).use { stream ->
                parseTarefasWorkbook(turnoId, stream.readBytes()).first
            }
        }.getOrElse { emptyList() }
    }

    fun importRecursosFromAssets(assetPath: String): RecursosImportData? {
        return runCatching {
            context.assets.open(assetPath).use { stream ->
                parseRecursosWorkbook(stream.readBytes())
            }
        }.getOrNull()
    }

    private fun parseTarefasWorkbook(turnoId: Int, bytes: ByteArray): Pair<List<TarefaProgramadaEntity>, ImportResult> {
        val errors = mutableListOf<String>()
        val tarefasByDescricao = linkedMapOf<String, TarefaProgramadaEntity>()

        XSSFWorkbook(bytes.inputStream()).use { workbook ->
            workbook.forEach { sheet ->
                if (!sheet.sheetName.contains("tarefa", ignoreCase = true)) return@forEach

                val headerRowIndex = findHeaderRow(sheet, "lista de tarefas") ?: return@forEach
                val headerRow = sheet.getRow(headerRowIndex) ?: return@forEach
                val headers = headersMap(headerRow)

                for (rowIndex in (headerRowIndex + 1)..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    try {
                        val descricao = get(row, headers, "lista de tarefas", "tarefa", "descricao")
                        if (descricao.isBlank()) continue

                        val equipamento = get(row, headers, "equipamentos/veiculos", "equipamentos", "equipamento")
                        val frequencia = max(1, parseLeadingInt(get(row, headers, "frequencia (media/turno)", "frequencia"), 1))
                        val quantidade = max(1, parseLeadingInt(get(row, headers, "quantidade (placas, vagoes, barrotes, etc.)", "quantidade"), 1))
                        val funcao = get(row, headers, "funcao", "função").ifBlank { "Fluxo de placas" }
                        val tempoBase = parseLeadingDouble(get(row, headers, "tempo (min)", "tempo", "tempo base (min)"), 20.0)
                        val pessoas = max(1, parseLeadingInt(get(row, headers, "quantidade de pessoas por tarefa", "qtd pessoas"), 1))

                        val categoria = funcao
                        val tempoTotal = tempoBase * frequencia * quantidade

                        tarefasByDescricao[descricao] = TarefaProgramadaEntity(
                            turnoId = turnoId,
                            categoria = categoria,
                            descricao = descricao,
                            quantidade = quantidade,
                            frequencia = frequencia,
                            tempoBaseMin = tempoBase,
                            modoCalculo = "MULTIPLICAR",
                            tempoTotalMin = tempoTotal,
                            recursoSugestao = equipamento.ifBlank { null },
                            observacaoTurno = "Pessoas sugeridas: $pessoas"
                        )
                    } catch (e: Exception) {
                        errors += "${sheet.sheetName} linha ${rowIndex + 1}: ${e.message ?: "erro"}"
                    }
                }
            }
        }

        val tarefas = tarefasByDescricao.values.toList()
        return tarefas to ImportResult(tarefas.size, errors)
    }

    private fun parseRecursosWorkbook(bytes: ByteArray): RecursosImportData {
        val supervisors = mutableSetOf<String>()
        val funcionarios = mutableListOf<ImportedFuncionario>()
        val recursoDetalhe = mutableListOf<ImportedRecurso>()
        val quantidadeCategoriaTurno = linkedMapOf<String, Int>()

        XSSFWorkbook(bytes.inputStream()).use { workbook ->
            val efetivoSheet = workbook.getSheet("EFETIVO")
            if (efetivoSheet != null) {
                val header = headersMap(efetivoSheet.getRow(0))
                for (r in 1..efetivoSheet.lastRowNum) {
                    val row = efetivoSheet.getRow(r) ?: continue
                    val letra = get(row, header, "letra").uppercase()
                    val horario = get(row, header, "horario").uppercase()
                    val funcao = get(row, header, "funcao", "função")
                    val nome = get(row, header, "nome")
                    if (funcao.isBlank() || nome.isBlank()) continue

                    if (funcao.contains("SUPERVISOR", ignoreCase = true) && !nome.contains("/")) {
                        supervisors += nome
                    }

                    if (letra in setOf("A", "B", "C", "D") && horario.contains("TURNO")) {
                        funcionarios += ImportedFuncionario(
                            nome = nome,
                            funcao = funcao,
                            letra = letra
                        )
                    }
                }
            }

            val equipamentosSheet = workbook.getSheet("EQUIPAMENTOS")
            if (equipamentosSheet != null) {
                // tabela 1: detalhamento por tag
                val header = headersMap(equipamentosSheet.getRow(0))
                var row = 1
                while (row <= equipamentosSheet.lastRowNum) {
                    val line = equipamentosSheet.getRow(row) ?: break
                    val tag = get(line, header, "equipamento")
                    val categoria = get(line, header, "categoria")
                    if (tag.isBlank() && categoria.isBlank()) {
                        row++
                        break
                    }
                    if (tag.isNotBlank() && categoria.isNotBlank()) {
                        val adm = isAdmCategoria(categoria)
                        recursoDetalhe += ImportedRecurso(tag = tag, categoria = categoria, admFlag = adm)
                    }
                    row++
                }

                // tabela 2: quantidade por categoria e horario
                val secondHeaderRow = findHeaderRow(equipamentosSheet, "categoria")
                if (secondHeaderRow != null) {
                    val secondHeader = headersMap(equipamentosSheet.getRow(secondHeaderRow))
                    for (r in (secondHeaderRow + 1)..equipamentosSheet.lastRowNum) {
                        val line = equipamentosSheet.getRow(r) ?: continue
                        val categoria = get(line, secondHeader, "categoria")
                        val qtd = parseLeadingInt(get(line, secondHeader, "quantidade"), 0)
                        val horario = get(line, secondHeader, "horario").uppercase()
                        if (categoria.isBlank() || qtd <= 0) continue
                        if (horario.contains("TURNO") || isAdmCategoria(categoria)) {
                            quantidadeCategoriaTurno[categoria] = qtd
                        }
                    }
                }
            }
        }

        val funcoesPrevisto = funcionarios
            .groupBy { it.funcao }
            .mapValues { (_, list) ->
                list.groupBy { it.letra }.values.maxOfOrNull { sameLetter -> sameLetter.size } ?: 1
            }

        val recursos = expandRecursosPorQuantidade(recursoDetalhe, quantidadeCategoriaTurno)

        return RecursosImportData(
            supervisors = supervisors.toList().sorted(),
            funcoesPrevisto = funcoesPrevisto,
            funcionarios = funcionarios,
            recursos = recursos
        )
    }

    private fun expandRecursosPorQuantidade(
        detalhe: List<ImportedRecurso>,
        quantidadePorCategoria: Map<String, Int>
    ): List<ImportedRecurso> {
        if (quantidadePorCategoria.isEmpty()) return detalhe.distinctBy { it.tag }

        val grouped = detalhe.groupBy { it.categoria }
        val output = mutableListOf<ImportedRecurso>()

        quantidadePorCategoria.forEach { (categoria, qtd) ->
            val base = grouped[categoria].orEmpty()
            if (base.isNotEmpty()) {
                output += base.take(qtd)
                if (base.size < qtd) {
                    for (i in (base.size + 1)..qtd) {
                        output += ImportedRecurso(
                            tag = "${categoria.take(3).uppercase()}-${i.toString().padStart(2, '0')}",
                            categoria = categoria,
                            admFlag = isAdmCategoria(categoria)
                        )
                    }
                }
            } else {
                for (i in 1..qtd) {
                    output += ImportedRecurso(
                        tag = "${categoria.take(3).uppercase()}-${i.toString().padStart(2, '0')}",
                        categoria = categoria,
                        admFlag = isAdmCategoria(categoria)
                    )
                }
            }
        }

        return output.distinctBy { it.tag }
    }

    private fun headersMap(row: Row?): Map<String, Int> {
        if (row == null) return emptyMap()
        val headers = mutableMapOf<String, Int>()
        for (i in row.firstCellNum until row.lastCellNum) {
            if (i < 0) continue
            val key = formatter.formatCellValue(row.getCell(i)).normalizeHeader()
            if (key.isNotBlank()) headers[key] = i.toInt()
        }
        return headers
    }

    private fun findHeaderRow(sheet: org.apache.poi.ss.usermodel.Sheet, marker: String): Int? {
        val markerNorm = marker.normalizeHeader()
        for (r in sheet.firstRowNum..minOf(sheet.lastRowNum, sheet.firstRowNum + 30)) {
            val row = sheet.getRow(r) ?: continue
            val rowText = buildString {
                for (c in row.firstCellNum until row.lastCellNum) {
                    if (c < 0) continue
                    append(formatter.formatCellValue(row.getCell(c)).normalizeHeader())
                    append('|')
                }
            }
            if (rowText.contains(markerNorm)) return r
        }
        return null
    }

    private fun get(row: Row, headers: Map<String, Int>, vararg keys: String): String {
        val idx = keys.firstNotNullOfOrNull { headers[it.normalizeHeader()] } ?: return ""
        return formatter.formatCellValue(row.getCell(idx)).trim()
    }

    private fun parseLeadingInt(raw: String, fallback: Int): Int {
        val cleaned = raw.trim().replace(',', '.')
        val n = Regex("""\d+""").find(cleaned)?.value?.toIntOrNull()
        return n ?: fallback
    }

    private fun parseLeadingDouble(raw: String, fallback: Double): Double {
        val cleaned = raw.trim().replace(',', '.')
        val n = Regex("""\d+(\.\d+)?""").find(cleaned)?.value?.toDoubleOrNull()
        return n ?: fallback
    }

    private fun isAdmCategoria(categoria: String): Boolean {
        val up = categoria.uppercase()
        return up.contains("MOTONIVELADORA") || up.contains("ROLO COMPACTADOR")
    }

    private fun String.normalizeHeader(): String {
        return lowercase()
            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("é", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")
            .replace("/", "")
            .replace("-", " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
