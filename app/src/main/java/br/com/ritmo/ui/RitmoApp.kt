package br.com.ritmo.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ritmo.domain.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RitmoApp(
    state: StudyState, now: Long,
    onToggle: () -> Unit, onReset: () -> Unit, onSkipBreak: () -> Unit,
    onSelectSubject: (String) -> Unit, onAddSubject: (String) -> Unit,
    onDuration: (Int) -> Unit, onGoal: (Int) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var addSubject by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
    Scaffold(
        containerColor = Paper,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Meu foco") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Peach)
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Progresso") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Peach)
                )
            }
        }
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            if (tab == 0) {
                FocusScreen(
                    state, now, today, onToggle,
                    onReset = { if (state.timer.started) confirmReset = true else onReset() },
                    onSkipBreak, onSelectSubject,
                    onAdd = { addSubject = true }, onDuration
                )
            } else ProgressScreen(state, today, onGoal)
        }
    }
    if (addSubject) {
        SubjectDialog(
            names = state.subjects.map { it.name },
            onDismiss = { addSubject = false },
            onAdd = { onAddSubject(it); addSubject = false }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Recomeçar esta etapa?") },
            text = { Text("O tempo desta etapa será descartado. As sessões já concluídas continuam no histórico.") },
            confirmButton = { TextButton(onClick = { onReset(); confirmReset = false }) { Text("Recomeçar") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Continuar") } }
        )
    }
}

@Composable
private fun BrandHeader(subtitle: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Orange),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(25.dp)) {
                drawArc(Color.White, -80f, 295f, false, style = Stroke(2.6.dp.toPx(), cap = StrokeCap.Round))
                drawLine(Color.White, center, center.copy(y = size.height * .25f), 2.6.dp.toPx(), StrokeCap.Round)
                drawLine(Color.White, center, center.copy(x = size.width * .7f, y = size.height * .63f), 2.6.dp.toPx(), StrokeCap.Round)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("ritmo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Spacer(Modifier.weight(1f))
        Text("UM PASSO\nPOR VEZ", fontSize = 10.sp, lineHeight = 15.sp,
            letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = Muted, textAlign = TextAlign.End)
    }
}

@Composable
private fun FocusScreen(
    state: StudyState, now: Long, today: LocalDate, onToggle: () -> Unit,
    onReset: () -> Unit, onSkipBreak: () -> Unit, onSelectSubject: (String) -> Unit,
    onAdd: () -> Unit, onDuration: (Int) -> Unit
) {
    val todaySessions = StudyEngine.sessionsOn(state, today)
    LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 600.dp).testTag("focus-list"),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item { BrandHeader("Seu tempo, com intenção.") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Menos distração.\nMais um passo.", style = MaterialTheme.typography.headlineLarge)
                Text("Escolha uma matéria e cuide do seu foco.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item { TimerCard(state, now, onToggle, onReset, onSkipBreak) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Foco hoje", (todaySessions.sumOf { it.durationSeconds } / 60).toString() + " min", Modifier.weight(1f))
                MetricCard("Sua meta", todaySessions.size.toString() + " / " + state.dailyGoal, Modifier.weight(1f))
            }
        }
        item {
            Column {
                Text("Duração do foco", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(15, 25, 50).forEach { minutes ->
                        FilterChip(
                            selected = state.focusMinutes == minutes,
                            onClick = { onDuration(minutes) }, enabled = !state.timer.started,
                            label = { Text(minutes.toString() + " min") }
                        )
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("O que vamos estudar?", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onAdd, enabled = state.subjects.size < 24) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar matéria", tint = Orange)
                }
            }
            if (state.timer.started) {
                Text("Recomece ou conclua a etapa para trocar de matéria.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
        items(state.subjects, key = { it.id }) { subject ->
            val minutes = state.history.filter { it.subjectId == subject.id }.sumOf { it.durationSeconds } / 60
            SubjectRow(subject, subject.id == state.selectedSubjectId, minutes, !state.timer.started) { onSelectSubject(subject.id) }
        }
        item {
            Text("Foco de " + state.focusMinutes + " min · Pausa de 5 min\nA cada 4 focos, descanse por 15 min.",
                color = Muted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun TimerCard(state: StudyState, now: Long, onToggle: () -> Unit, onReset: () -> Unit, onSkipBreak: () -> Unit) {
    val timer = state.timer
    val rest = timer.phase != Phase.FOCUS
    val accent = if (rest) Green else Orange
    val remaining = timer.remainingAt(now)
    val progress by animateFloatAsState(remaining.toFloat() / timer.durationSeconds, label = "Progresso do ciclo")
    val time = String.format(Locale.ROOT, "%02d:%02d", remaining / 60, remaining % 60)
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = if (rest) Color(0xFFE9F0E5) else Peach)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(timer.phase.title.uppercase(Locale("pt", "BR")), color = accent,
                fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(14.dp))
            BoxWithConstraints(
                Modifier.widthIn(max = 270.dp).fillMaxWidth().aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                val timerFont = ((maxWidth.value - 60f) / (2.9f * LocalDensity.current.fontScale)).coerceAtMost(56f).sp
                Canvas(Modifier.fillMaxSize().padding(12.dp)) {
                    drawArc(accent.copy(alpha = .12f), -90f, 360f, false, style = Stroke(7.dp.toPx()))
                    drawArc(accent, -90f, 360f * progress, false,
                        style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(30.dp)) {
                    Text(time, fontSize = timerFont, fontWeight = FontWeight.Light, letterSpacing = (-2).sp,
                        color = Ink, maxLines = 1, modifier = Modifier.semantics {
                            contentDescription = remaining.div(60).toString() + " minutos e " + remaining.rem(60) + " segundos restantes"
                        })
                    Text(
                        if (rest) "Respire. Você merece." else state.selectedSubject.name,
                        color = Muted, style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        repeat(4) { index ->
                            val filled = if (state.completedFocus > 0 && state.completedFocus % 4 == 0 && rest) 4 else state.completedFocus % 4
                            Box(Modifier.size(7.dp).background(if (index < filled) accent else accent.copy(alpha = .2f), CircleShape))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onToggle, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent), shape = RoundedCornerShape(18.dp)
            ) {
                Text(if (timer.running) "Pausar" else if (timer.started) "Retomar" else if (rest) "Iniciar pausa" else "Começar foco",
                    fontSize = 16.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onReset) { Text("Recomeçar", color = Muted) }
                if (rest) TextButton(onClick = onSkipBreak) { Text("Pular pausa", color = Green) }
            }
            Text(
                if (timer.running) "Você pode sair e voltar: o tempo é preservado."
                else if (rest) "Bom trabalho! Inicie sua pausa quando quiser."
                else "Uma tarefa de cada vez.",
                fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = Color.White) {
        Column(Modifier.padding(18.dp)) {
            Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(5.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun SubjectRow(subject: Subject, selected: Boolean, minutes: Int, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFFECF0E5) else Color.White)
            .border(1.dp, if (selected) Green.copy(alpha = .35f) else Color.Transparent, RoundedCornerShape(20.dp))
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = subject.name + if (selected) ", matéria selecionada" else ", selecionar matéria" }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).background(if (selected) Green else Paper, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center) {
            Text(subject.name.take(1).uppercase(Locale("pt", "BR")), color = if (selected) Color.White else Muted,
                fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(subject.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(if (minutes == 0) "Seu próximo passo começa aqui" else minutes.toString() + " minutos de dedicação",
                color = Muted, fontSize = 12.sp)
        }
        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ProgressScreen(state: StudyState, today: LocalDate, onGoal: (Int) -> Unit) {
    val sessions = StudyEngine.sessionsOn(state, today)
    LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
        contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item { BrandHeader("Pequenos passos também contam.") }
        item {
            Text("Veja o seu\nprogresso.", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text("Cada sessão é um compromisso com você.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F0E5))) {
                Column(Modifier.padding(22.dp)) {
                    Text("SUA META DE HOJE", fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = Green)
                    Spacer(Modifier.height(12.dp))
                    Text(sessions.size.toString() + " de " + state.dailyGoal + " sessões", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { (sessions.size.toFloat() / state.dailyGoal).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(7.dp), color = Green, trackColor = Green.copy(alpha = .12f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(if (sessions.size >= state.dailyGoal) "Meta alcançada. Celebre essa conquista!"
                        else "No seu ritmo. O importante é continuar.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ajustar meta", color = Muted, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onGoal(state.dailyGoal - 1) }, enabled = state.dailyGoal > 1) {
                            Text("−", fontSize = 24.sp, modifier = Modifier.semantics { contentDescription = "Diminuir meta" })
                        }
                        Text(state.dailyGoal.toString(), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onGoal(state.dailyGoal + 1) }, enabled = state.dailyGoal < 12) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar meta")
                        }
                    }
                }
            }
        }
        item { WeekChart(state, today) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Foco total", (state.history.sumOf { it.durationSeconds } / 60).toString() + " min", Modifier.weight(1f))
                MetricCard("Sessões", state.history.size.toString(), Modifier.weight(1f))
            }
        }
        item { Text("Últimas sessões", style = MaterialTheme.typography.titleLarge) }
        if (state.history.isEmpty()) {
            item {
                Surface(color = Color.White, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("Seu primeiro foco está por vir.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Conclua uma sessão de foco para começar seu histórico. Pausas e sessões interrompidas não entram na contagem.",
                            color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(state.history.take(30), key = { it.id }) { session ->
                val date = Instant.ofEpochMilli(session.completedAt).atZone(ZoneId.systemDefault())
                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(session.subjectName, style = MaterialTheme.typography.titleMedium)
                        Text(date.format(DateTimeFormatter.ofPattern("dd/MM · HH:mm")), color = Muted, fontSize = 12.sp)
                    }
                    Text((session.durationSeconds / 60).toString() + " min", fontWeight = FontWeight.SemiBold, color = Green)
                }
            }
        }
        item {
            Text("Dados salvos somente neste aparelho.\nSem cadastro, sem anúncios, no seu ritmo.",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Center, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WeekChart(state: StudyState, today: LocalDate) {
    val dates = (6L downTo 0L).map { today.minusDays(it) }
    val minutes = dates.map { date -> StudyEngine.sessionsOn(state, date).sumOf { it.durationSeconds } / 60 }
    val maximum = minutes.maxOrNull()?.coerceAtLeast(25) ?: 25
    Surface(color = Color.White, shape = RoundedCornerShape(26.dp)) {
        Column(Modifier.padding(22.dp)) {
            Text("Últimos 7 dias", style = MaterialTheme.typography.titleMedium)
            Text(minutes.sum().toString() + " minutos de foco", color = Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                dates.forEachIndexed { index, date ->
                    Column(
                        Modifier.weight(1f).semantics {
                            contentDescription = date.toString() + ": " + minutes[index] + " minutos de foco"
                        }, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(minutes[index].toString(), fontSize = 10.sp, color = Muted)
                        Box(Modifier.height(92.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.fillMaxWidth().fillMaxHeight((minutes[index].toFloat() / maximum).coerceAtLeast(.035f))
                                .background(if (date == today) Orange else Green.copy(alpha = .35f), RoundedCornerShape(7.dp)))
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(date.format(DateTimeFormatter.ofPattern("EEEEE", Locale("pt", "BR"))),
                            fontSize = 11.sp, color = if (date == today) Orange else Muted, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectDialog(names: List<String>, onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val duplicate = names.any { it.equals(name.trim(), ignoreCase = true) }
    val valid = name.trim().isNotEmpty() && !duplicate
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Nova matéria") },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(40) },
                label = { Text("Nome da matéria") }, singleLine = true,
                isError = duplicate, supportingText = { Text(if (duplicate) "Esta matéria já existe." else name.length.toString() + "/40 caracteres") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (valid) onAdd(name.trim()) })
            )
        },
        confirmButton = { TextButton(onClick = { onAdd(name.trim()) }, enabled = valid) { Text("Adicionar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 1100)
@Composable
private fun FocusPreview() {
    RitmoTheme {
        RitmoApp(StudyState(), 0L, {}, {}, {}, {}, {}, {}, {})
    }
}
