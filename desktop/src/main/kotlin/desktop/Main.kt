package desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.max

@Serializable data class Supervisor(val id: Int, val nome: String)
@Serializable data class Funcao(val id: Int, val nome: String, val efetivoPrevisto: Int, val fator: Double = 1.0)
@Serializable data class Funcionario(val id: Int, val nome: String, val funcaoId: Int, val letra: String)
@Serializable data class Recurso(val id: Int, val tag: String, val tipo: String)
@Serializable data class Turno(val id: Int, val data: String, val janela: String, val supervisorId: Int, val status: String)
@Serializable data class Alocacao(val turnoId: Int, val funcaoId: Int, val selecionados: List<Int> = emptyList(), val outraLetraId: Int? = null)
@Serializable data class Tarefa(
    val id: Int,
    val turnoId: Int,
    val categoria: String,
    val descricao: String,
    val quantidade: Int,
    val frequencia: Int,
    val tempoBaseMin: Double,
    val modoCalculo: String,
    val tempoTotalMin: Double,
    val recursoSugestao: String? = null,
    val statusExecucao: String = "NAO_INICIADO",
    val quantidadeExecutada: Int = 0,
    val observacao: String = ""
)
@Serializable data class Ocorrencia(val id: Int, val turnoId: Int, val horario: String, val texto: String, val tag: String)

@Serializable data class DesktopData(
    val supervisors: List<Supervisor> = emptyList(),
    val funcoes: List<Funcao> = emptyList(),
    val funcionarios: List<Funcionario> = emptyList(),
    val recursos: List<Recurso> = emptyList(),
    val turnos: List<Turno> = emptyList(),
    val alocacoes: List<Alocacao> = emptyList(),
    val tarefas: List<Tarefa> = emptyList(),
    val ocorrencias: List<Ocorrencia> = emptyList(),
    val minutosTurno: Int = 720,
    val selectedTurnoId: Int? = null
)

class DesktopStore {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val file = File(System.getProperty("user.home"), "diario_turno_desktop_data.json")

    fun load(): DesktopData {
        if (!file.exists()) {
            val seed = seedData()
            save(seed)
            return seed
        }
        return runCatching { json.decodeFromString<DesktopData>(file.readText()) }.getOrElse { seedData() }
    }

    fun save(data: DesktopData) {
        file.writeText(json.encodeToString(data))
    }
}

fun seedData(): DesktopData {
    val supervisors = listOf(Supervisor(1, "Supervisor A"), Supervisor(2, "Supervisor B"))
    val funcoes = listOf(
        Funcao(1, "Controlador", 2),
        Funcao(2, "Operador Pa", 3),
        Funcao(3, "Conferente", 2)
    )
    val funcionarios = listOf(
        Funcionario(1, "Joao A", 1, "A"), Funcionario(2, "Pedro B", 1, "B"),
        Funcionario(3, "Luis A", 2, "A"), Funcionario(4, "Tiago B", 2, "B"), Funcionario(5, "Andre C", 2, "C"),
        Funcionario(6, "Paulo A", 3, "A"), Funcionario(7, "Renan B", 3, "B")
    )
    val recursos = listOf(Recurso(1, "RS-01", "Reach"), Recurso(2, "EMP-02", "Empilhadeira"))
    val turno = Turno(1, LocalDate.now().toString(), "07x19", 1, "ABERTO")
    val tarefas = listOf(
        Tarefa(1, 1, "Carregamento", "Carregar placas lote A", 8, 2, 18.0, "MULTIPLICAR", 288.0),
        Tarefa(2, 1, "Recebimento", "Receber remessa patio", 5, 1, 22.0, "MULTIPLICAR", 110.0)
    )
    return DesktopData(
        supervisors = supervisors,
        funcoes = funcoes,
        funcionarios = funcionarios,
        recursos = recursos,
        turnos = listOf(turno),
        tarefas = tarefas,
        selectedTurnoId = 1
    )
}

data class CapacidadeMetrics(val demanda: Double, val capacidade: Double, val uso: Double, val acima: Double, val causa: String)

fun calcMetrics(data: DesktopData, turnoId: Int?): CapacidadeMetrics {
    if (turnoId == null) return CapacidadeMetrics(0.0, 0.0, 0.0, 0.0, "SEM TURNO")
    val demanda = data.tarefas.filter { it.turnoId == turnoId }.sumOf { it.tempoTotalMin }
    val capacidade = data.funcoes.sumOf { f ->
        val a = data.alocacoes.find { it.turnoId == turnoId && it.funcaoId == f.id }
        val qtd = (a?.selecionados?.size ?: 0) + if (a?.outraLetraId != null) 1 else 0
        qtd * data.minutosTurno * f.fator
    }
    val uso = if (capacidade <= 0.0) 0.0 else demanda / capacidade
    val acima = max(0.0, uso - 1.0)
    val completo = data.funcoes.all { f ->
        val a = data.alocacoes.find { it.turnoId == turnoId && it.funcaoId == f.id }
        val qtd = (a?.selecionados?.size ?: 0) + if (a?.outraLetraId != null) 1 else 0
        qtd >= f.efetivoPrevisto
    }
    val causa = when {
        uso <= 1.0 -> "DENTRO DA CAPACIDADE"
        !completo -> "ACIMA POR EFETIVO INCOMPLETO"
        else -> "ACIMA POR INEFICIENCIA/DEMANDA"
    }
    return CapacidadeMetrics(demanda, capacidade, uso, acima, causa)
}

fun importExcel(path: String, turnoId: Int, current: List<Tarefa>): List<Tarefa> {
    val file = File(path)
    if (!file.exists()) return current
    val formatter = DataFormatter()
    val tasks = mutableListOf<Tarefa>()
    file.inputStream().use { inp ->
        XSSFWorkbook(inp).use { wb ->
            val sh = wb.getSheetAt(0)
            val header = sh.getRow(sh.firstRowNum) ?: return current
            val map = mutableMapOf<String, Int>()
            header.forEachIndexed { idx, cell -> map[formatter.formatCellValue(cell).trim().lowercase()] = idx }
            var nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            for (i in sh.firstRowNum + 1..sh.lastRowNum) {
                val r = sh.getRow(i) ?: continue
                fun get(vararg k: String): String {
                    val id = k.firstNotNullOfOrNull { map[it.lowercase()] } ?: return ""
                    return formatter.formatCellValue(r.getCell(id)).trim()
                }
                val desc = get("descricao")
                if (desc.isBlank()) continue
                val categoria = get("categoria").ifBlank { "Outros" }
                val qtd = get("quantidade").toIntOrNull() ?: 1
                val freq = max(1, get("frequencia").toIntOrNull() ?: 1)
                val base = get("tempobasemin", "tempo", "tempo_base_min").replace(',', '.').toDoubleOrNull() ?: 0.0
                val modoRaw = get("modo_calculo").uppercase()
                val modo = if (modoRaw == "DIVIDIR" || modoRaw == "MULTIPLICAR") modoRaw else if (freq > 1 && base <= 240.0) "MULTIPLICAR" else "DIVIDIR"
                val total = if (modo == "DIVIDIR") (base / freq) * qtd else (base * freq) * qtd
                tasks += Tarefa(nextId++, turnoId, categoria, desc, qtd, freq, base, modo, total, get("recursosugestao", "recurso_sugestao").ifBlank { null })
            }
        }
    }
    return current.filterNot { it.turnoId == turnoId } + tasks
}

fun main() = application {
    val store = DesktopStore()
    var data by remember { mutableStateOf(store.load()) }
    var tab by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }

    fun mutate(block: (DesktopData) -> DesktopData) {
        data = block(data)
        store.save(data)
    }

    Window(onCloseRequest = ::exitApplication, title = "Diario de Turno - Desktop") {
        MaterialTheme {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Text("Diario de Turno - Versao Desktop", style = MaterialTheme.typography.h5)
                if (message.isNotBlank()) Text(message, color = Color(0xFF0E5A93))
                val tabs = listOf("Turno", "Programacao", "Recursos", "Capacidade", "Historico", "Config", "Diario")
                TabRow(selectedTabIndex = tab) {
                    tabs.forEachIndexed { idx, title -> Tab(selected = tab == idx, onClick = { tab = idx }, text = { Text(title) }) }
                }
                Spacer(Modifier.height(8.dp))

                when (tab) {
                    0 -> TurnoTab(data, onNewTurno = {
                        val id = (data.turnos.maxOfOrNull { t -> t.id } ?: 0) + 1
                        mutate { it.copy(turnos = listOf(Turno(id, LocalDate.now().toString(), "07x19", it.supervisors.firstOrNull()?.id ?: 1, "ABERTO")) + it.turnos, selectedTurnoId = id) }
                    }, onSelect = { id -> mutate { it.copy(selectedTurnoId = id) } }, onFinish = {
                        val id = data.selectedTurnoId ?: return@TurnoTab
                        mutate { d -> d.copy(turnos = d.turnos.map { if (it.id == id) it.copy(status = "FINALIZADO") else it }) }
                    })
                    1 -> ProgramacaoTab(data, onDelete = { id -> mutate { it.copy(tarefas = it.tarefas.filterNot { t -> t.id == id }) } }, onImport = { path ->
                        val tid = data.selectedTurnoId ?: return@ProgramacaoTab
                        val imported = importExcel(path, tid, data.tarefas)
                        mutate { it.copy(tarefas = imported) }
                        message = "Planilha importada"
                    })
                    2 -> RecursosTab(data, onToggle = { funcaoId, funcionarioId, checked ->
                        val tid = data.selectedTurnoId ?: return@RecursosTab
                        mutate { d ->
                            val existing = d.alocacoes.find { it.turnoId == tid && it.funcaoId == funcaoId }
                            val sel = (existing?.selecionados ?: emptyList()).toMutableSet()
                            if (checked) sel += funcionarioId else sel -= funcionarioId
                            val updated = Alocacao(tid, funcaoId, sel.toList(), existing?.outraLetraId)
                            d.copy(alocacoes = d.alocacoes.filterNot { it.turnoId == tid && it.funcaoId == funcaoId } + updated)
                        }
                    })
                    3 -> CapacidadeTab(data)
                    4 -> HistoricoTab(data, onOpen = { id -> mutate { it.copy(selectedTurnoId = id) } })
                    5 -> ConfigTab(data,
                        onAddSup = { nome -> if (nome.isNotBlank()) mutate { d -> d.copy(supervisors = d.supervisors + Supervisor((d.supervisors.maxOfOrNull { it.id } ?: 0) + 1, nome)) } },
                        onDeleteSup = { id -> mutate { d -> d.copy(supervisors = d.supervisors.filterNot { it.id == id }) } },
                        onDeleteTask = { id -> mutate { d -> d.copy(tarefas = d.tarefas.filterNot { it.id == id }) } }
                    )
                    else -> DiarioTab(data, onAdd = { txt, tag ->
                        val tid = data.selectedTurnoId ?: return@DiarioTab
                        if (txt.isBlank()) return@DiarioTab
                        mutate { d ->
                            val id = (d.ocorrencias.maxOfOrNull { it.id } ?: 0) + 1
                            d.copy(ocorrencias = listOf(Ocorrencia(id, tid, LocalTime.now().withSecond(0).toString(), txt, tag.ifBlank { "GERAL" })) + d.ocorrencias)
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun TurnoTab(data: DesktopData, onNewTurno: () -> Unit, onSelect: (Int) -> Unit, onFinish: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNewTurno) { Text("Novo Turno") }
            Button(onClick = onFinish) { Text("Finalizar") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(data.turnos) { t ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("#${t.id} ${t.data} ${t.janela} - ${t.status}")
                        Button(onClick = { onSelect(t.id) }) { Text(if (data.selectedTurnoId == t.id) "Selecionado" else "Abrir") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramacaoTab(data: DesktopData, onDelete: (Int) -> Unit, onImport: (String) -> Unit) {
    var path by remember { mutableStateOf("") }
    val tarefas = data.tarefas.filter { it.turnoId == data.selectedTurnoId }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(path, { path = it }, label = { Text("Caminho da planilha .xlsx") }, modifier = Modifier.weight(1f))
            Button(onClick = { onImport(path) }) { Text("Importar") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(tarefas) { t ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("${t.categoria}: ${t.descricao}")
                            Text("Tempo: ${"%.1f".format(t.tempoTotalMin)} min")
                        }
                        Button(onClick = { onDelete(t.id) }) { Text("Excluir") }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecursosTab(data: DesktopData, onToggle: (Int, Int, Boolean) -> Unit) {
    val tid = data.selectedTurnoId
    if (tid == null) { Text("Sem turno selecionado"); return }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(data.funcoes) { f ->
            val a = data.alocacoes.find { it.turnoId == tid && it.funcaoId == f.id }
            val selected = a?.selecionados ?: emptyList()
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp)) {
                    Text("${f.nome} (${selected.size}/${f.efetivoPrevisto})")
                    data.funcionarios.filter { it.funcaoId == f.id }.forEach { fn ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selected.contains(fn.id), onCheckedChange = { onToggle(f.id, fn.id, it) })
                            Text("${fn.nome} (${fn.letra})")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapacidadeTab(data: DesktopData) {
    val m = calcMetrics(data, data.selectedTurnoId)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Demanda: ${"%.1f".format(m.demanda)} min")
                Text("Capacidade: ${"%.1f".format(m.capacidade)} min")
                Text("Uso: ${"%.1f".format(m.uso * 100)}%")
                Text(if (m.uso > 1) "${"%.1f".format(m.acima * 100)}% acima" else "Dentro da capacidade", color = if (m.uso > 1) Color(0xFF9D3D3D) else Color(0xFF2A6FA0))
                Text(m.causa)
            }
        }
        Text("Top tarefas")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(data.tarefas.filter { it.turnoId == data.selectedTurnoId }.sortedByDescending { it.tempoTotalMin }.take(5)) { t ->
                Card(Modifier.fillMaxWidth()) { Text("${t.descricao} - ${"%.1f".format(t.tempoTotalMin)} min", modifier = Modifier.padding(10.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HistoricoTab(data: DesktopData, onOpen: (Int) -> Unit) {
    var status by remember { mutableStateOf("TODOS") }
    var query by remember { mutableStateOf("") }
    val rows = data.turnos.filter {
        (status == "TODOS" || it.status == status) && (query.isBlank() || it.data.contains(query) || it.janela.contains(query))
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("Buscar") })
            var expanded by remember { mutableStateOf(false) }
            Button(onClick = { expanded = true }) { Text("Status: $status") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("TODOS", "ABERTO", "FINALIZADO").forEach {
                    DropdownMenuItem(onClick = { status = it; expanded = false }) { Text(it) }
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(rows) { t ->
                val prog = data.tarefas.filter { it.turnoId == t.id }.sumOf { it.quantidade }
                val exe = data.tarefas.filter { it.turnoId == t.id }.sumOf { it.quantidadeExecutada }
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("#${t.id} ${t.data} ${t.janela}")
                            Text("Status: ${t.status} | Atend.: ${if (prog > 0) (exe * 100 / prog) else 0}%")
                        }
                        Button(onClick = { onOpen(t.id) }) { Text("Abrir") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigTab(data: DesktopData, onAddSup: (String) -> Unit, onDeleteSup: (Int) -> Unit, onDeleteTask: (Int) -> Unit) {
    var nome by remember { mutableStateOf("") }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Novo Supervisor")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(nome, { nome = it }, label = { Text("Nome") })
                        Button(onClick = { onAddSup(nome); nome = "" }) { Text("Salvar") }
                    }
                }
            }
        }
        item { Text("Supervisores") }
        items(data.supervisors) { s ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(s.nome)
                    Button(onClick = { onDeleteSup(s.id) }) { Text("Excluir") }
                }
            }
        }
        item { Text("Tarefas") }
        items(data.tarefas.filter { it.turnoId == data.selectedTurnoId }) { t ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t.descricao)
                    Button(onClick = { onDeleteTask(t.id) }) { Text("Excluir") }
                }
            }
        }
    }
}

@Composable
private fun DiarioTab(data: DesktopData, onAdd: (String, String) -> Unit) {
    var txt by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("GERAL") }
    val tid = data.selectedTurnoId
    if (tid == null) { Text("Sem turno selecionado"); return }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(txt, { txt = it }, label = { Text("Ocorrencia") }, modifier = Modifier.weight(1f))
            OutlinedTextField(tag, { tag = it }, label = { Text("Tag") })
            Button(onClick = { onAdd(txt, tag); txt = "" }) { Text("Adicionar") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(data.ocorrencias.filter { it.turnoId == tid }) { o ->
                Card(Modifier.fillMaxWidth()) {
                    Text("${o.horario} [${o.tag}] ${o.texto}", modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}
