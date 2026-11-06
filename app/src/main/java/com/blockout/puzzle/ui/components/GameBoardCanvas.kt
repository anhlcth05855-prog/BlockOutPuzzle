package com.blockout.puzzle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockout.puzzle.engine.DragLimits
import com.blockout.puzzle.engine.GameEngine
import com.blockout.puzzle.model.Block
import com.blockout.puzzle.model.ExitDoor
import com.blockout.puzzle.model.ExitSide
import com.blockout.puzzle.model.Obstacle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var rotation: Float,
    var vRot: Float,
    val color: Color,
    var alpha: Float = 1f,
    var life: Float = 1f
)

data class EscapingBlockAnim(
    val block: Block,
    val escapeSide: ExitSide,
    val initialOffsetX: Float,
    val initialOffsetY: Float,
    var progress: Float = 0f
)

data class FloatingScore(
    val text: String,
    val x: Float,
    val y: Float,
    val color: Color,
    var offsetY: Float = 0f,
    var alpha: Float = 1f,
    var life: Float = 1f
)

@Composable
fun GameBoardCanvas(
    engine: GameEngine,
    onMoveMade: () -> Unit,
    onBlockEscaped: (Block, ExitSide) -> Unit,
    onTapEmpty: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    var activeBlockId by remember { mutableStateOf<String?>(null) }
    var currentLimits by remember { mutableStateOf<DragLimits?>(null) }

    // Direct float states for 120 FPS zero-allocation tracking during drag
    var dragPixelX by remember { mutableFloatStateOf(0f) }
    var dragPixelY by remember { mutableFloatStateOf(0f) }

    // Animated offset for smooth spring release snap
    val springOffsetX = remember { Animatable(0f) }
    val springOffsetY = remember { Animatable(0f) }

    val particles = remember { mutableStateListOf<Particle>() }
    val escapingAnims = remember { mutableStateListOf<EscapingBlockAnim>() }
    val floatingScores = remember { mutableStateListOf<FloatingScore>() }

    // High performance floating score updater
    LaunchedEffect(floatingScores.size) {
        if (floatingScores.isNotEmpty()) {
            while (floatingScores.isNotEmpty()) {
                val it = floatingScores.iterator()
                while (it.hasNext()) {
                    val s = it.next()
                    s.offsetY -= 2f
                    s.life -= 0.038f
                    s.alpha = s.life.coerceIn(0f, 1f)
                    if (s.life <= 0f) {
                        it.remove()
                    }
                }
                delay(16)
            }
        }
    }

    // High performance particle updater
    LaunchedEffect(particles.size) {
        if (particles.isNotEmpty()) {
            while (particles.isNotEmpty()) {
                val it = particles.iterator()
                while (it.hasNext()) {
                    val p = it.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.55f // Gravity
                    p.rotation += p.vRot
                    p.life -= 0.035f
                    p.alpha = p.life.coerceIn(0f, 1f)
                    if (p.life <= 0f) {
                        it.remove()
                    }
                }
                delay(16)
            }
        }
    }

    // Escaping animation loop (fast & punchy slide-out)
    LaunchedEffect(escapingAnims.size) {
        if (escapingAnims.isNotEmpty()) {
            while (escapingAnims.isNotEmpty()) {
                val it = escapingAnims.iterator()
                while (it.hasNext()) {
                    val anim = it.next()
                    anim.progress += 0.12f
                    if (anim.progress >= 1f) {
                        it.remove()
                    }
                }
                delay(16)
            }
        }
    }

    fun spawnEscapeParticles(block: Block, exitCenterX: Float, exitCenterY: Float, side: ExitSide) {
        val count = 36
        for (i in 0 until count) {
            val speed = Random.nextFloat() * 11f + 5f
            val (vx, vy) = when (side) {
                ExitSide.TOP -> (Random.nextFloat() * 12f - 6f) to (-speed)
                ExitSide.BOTTOM -> (Random.nextFloat() * 12f - 6f) to (speed)
                ExitSide.LEFT -> (-speed) to (Random.nextFloat() * 12f - 6f)
                ExitSide.RIGHT -> (speed) to (Random.nextFloat() * 12f - 6f)
            }
            particles.add(
                Particle(
                    x = exitCenterX + Random.nextFloat() * 16f - 8f,
                    y = exitCenterY + Random.nextFloat() * 16f - 8f,
                    vx = vx,
                    vy = vy,
                    size = Random.nextFloat() * 8f + 6f,
                    rotation = Random.nextFloat() * 360f,
                    vRot = Random.nextFloat() * 16f - 8f,
                    color = if (i % 3 == 0) Color.White else block.color.mainColor
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(engine) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val boardPadding = 48f
                        val boardWidth = size.width - boardPadding * 2
                        val cellSize = boardWidth / engine.gridWidth

                        val col = ((down.position.x - boardPadding) / cellSize).toInt()
                        val row = ((down.position.y - boardPadding) / cellSize).toInt()

                        val hitBlock = engine.blocks.find { !it.isEscaped && it.occupies(col, row) }

                        if (hitBlock == null) {
                            activeBlockId = null
                            currentLimits = null
                            onTapEmpty()
                            return@awaitEachGesture
                        }

                        // Set active block
                        activeBlockId = hitBlock.id
                        val limits = engine.calculateLimits(hitBlock)
                        currentLimits = limits

                        dragPixelX = 0f
                        dragPixelY = 0f
                        coroutineScope.launch {
                            springOffsetX.snapTo(0f)
                            springOffsetY.snapTo(0f)
                        }

                        val downTime = System.currentTimeMillis()
                        var totalDragDistance = 0f
                        var lastPosition = down.position
                        var prevDelta = Offset.Zero

                        while (true) {
                            val event = awaitPointerEvent()
                            val pointerChange = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (pointerChange.pressed) {
                                val dragDelta = pointerChange.position - lastPosition
                                prevDelta = dragDelta
                                lastPosition = pointerChange.position
                                totalDragDistance += dragDelta.getDistance()

                                if (hitBlock.direction.canMoveHorizontal) {
                                    val minPixel = (limits.minXOnBoard - hitBlock.x) * cellSize - (if (limits.canEscapeLeft) cellSize * 0.9f else 0f)
                                    val maxPixel = (limits.maxXOnBoard - hitBlock.x) * cellSize + (if (limits.canEscapeRight) cellSize * 0.9f else 0f)
                                    dragPixelX = (dragPixelX + dragDelta.x).coerceIn(minPixel, maxPixel)
                                }

                                if (hitBlock.direction.canMoveVertical) {
                                    val minPixel = (limits.minYOnBoard - hitBlock.y) * cellSize - (if (limits.canEscapeTop) cellSize * 0.9f else 0f)
                                    val maxPixel = (limits.maxYOnBoard - hitBlock.y) * cellSize + (if (limits.canEscapeBottom) cellSize * 0.9f else 0f)
                                    dragPixelY = (dragPixelY + dragDelta.y).coerceIn(minPixel, maxPixel)
                                }

                                pointerChange.consume()
                            } else {
                                // Pointer up / released
                                pointerChange.consume()
                                val elapsedMs = System.currentTimeMillis() - downTime
                                val isFling = elapsedMs < 300 && totalDragDistance > 20f

                                val currentOffX = dragPixelX
                                val currentOffY = dragPixelY
                                dragPixelX = 0f
                                dragPixelY = 0f

                                // 1. If tap
                                if (totalDragDistance < 12f) {
                                    val quickResult = engine.tryQuickEscape(hitBlock.id)
                                    if (quickResult != null) {
                                        onMoveMade()
                                        if (quickResult.escaped && quickResult.escapeSide != null) {
                                            onBlockEscaped(hitBlock, quickResult.escapeSide)
                                            escapingAnims.add(EscapingBlockAnim(hitBlock, quickResult.escapeSide, 0f, 0f))
                                            val exitCenterX = boardPadding + (hitBlock.x + hitBlock.width / 2f) * cellSize
                                            val exitCenterY = boardPadding + (hitBlock.y + hitBlock.height / 2f) * cellSize
                                            spawnEscapeParticles(hitBlock, exitCenterX, exitCenterY, quickResult.escapeSide)

                                            val scoreText = if (quickResult.comboMultiplier > 1) {
                                                "+${quickResult.scoreEarned} x${quickResult.comboMultiplier}!"
                                            } else {
                                                "+${quickResult.scoreEarned}"
                                            }
                                            floatingScores.add(
                                                FloatingScore(
                                                    text = scoreText,
                                                    x = exitCenterX,
                                                    y = exitCenterY,
                                                    color = if (quickResult.comboMultiplier > 1) Color(0xFFFFD54F) else Color.White
                                                )
                                            )
                                        }
                                    }
                                    activeBlockId = null
                                    currentLimits = null
                                    break
                                }

                                // 2. Drag / Fling move
                                val deltaCellsX = currentOffX / cellSize
                                val deltaCellsY = currentOffY / cellSize

                                val result = engine.applyMove(hitBlock.id, deltaCellsX, deltaCellsY, isFling = isFling)
                                if (result != null) {
                                    onMoveMade()
                                    if (result.escaped && result.escapeSide != null) {
                                        onBlockEscaped(hitBlock, result.escapeSide)
                                        escapingAnims.add(EscapingBlockAnim(hitBlock, result.escapeSide, currentOffX, currentOffY))
                                        val exitCenterX = boardPadding + (hitBlock.x + hitBlock.width / 2f) * cellSize
                                        val exitCenterY = boardPadding + (hitBlock.y + hitBlock.height / 2f) * cellSize
                                        spawnEscapeParticles(hitBlock, exitCenterX, exitCenterY, result.escapeSide)

                                        val scoreText = if (result.comboMultiplier > 1) {
                                            "+${result.scoreEarned} x${result.comboMultiplier}!"
                                        } else {
                                            "+${result.scoreEarned}"
                                        }
                                        floatingScores.add(
                                            FloatingScore(
                                                text = scoreText,
                                                x = exitCenterX,
                                                y = exitCenterY,
                                                color = if (result.comboMultiplier > 1) Color(0xFFFFD54F) else Color.White
                                            )
                                        )

                                        activeBlockId = null
                                        currentLimits = null
                                    } else {
                                        // Smooth spring snap from drag offset into the new cell
                                        val gridShiftX = (result.newX - result.oldX) * cellSize
                                        val gridShiftY = (result.newY - result.oldY) * cellSize
                                        val startSpringX = currentOffX - gridShiftX
                                        val startSpringY = currentOffY - gridShiftY

                                        coroutineScope.launch {
                                            springOffsetX.snapTo(startSpringX)
                                            springOffsetY.snapTo(startSpringY)
                                            launch {
                                                springOffsetX.animateTo(
                                                    0f,
                                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                                )
                                            }
                                            launch {
                                                springOffsetY.animateTo(
                                                    0f,
                                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                                )
                                            }
                                            activeBlockId = null
                                            currentLimits = null
                                        }
                                    }
                                } else {
                                    // Snap back smoothly to original position
                                    coroutineScope.launch {
                                        springOffsetX.snapTo(currentOffX)
                                        springOffsetY.snapTo(currentOffY)
                                        launch {
                                            springOffsetX.animateTo(
                                                0f,
                                                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                            )
                                        }
                                        launch {
                                            springOffsetY.animateTo(
                                                0f,
                                                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                            )
                                        }
                                        activeBlockId = null
                                        currentLimits = null
                                    }
                                }
                                break
                            }
                        }
                    }
                }
        ) {
            val boardPadding = 48f
            val boardWidth = size.width - boardPadding * 2
            val boardHeight = size.height - boardPadding * 2
            val cellSize = boardWidth / engine.gridWidth

            // 1. Draw outer frame & board background
            drawBoardBackground(boardPadding, boardWidth, boardHeight, engine.gridWidth, engine.gridHeight, cellSize)

            // 2. Draw Exit Doors around the perimeter
            for (door in engine.doors) {
                drawExitDoor(door, boardPadding, cellSize, engine.gridWidth, engine.gridHeight)
            }

            // 3. Draw Obstacles (rocks/locks)
            for (obs in engine.obstacles.filter { !it.isDestroyed }) {
                drawObstacle(obs, boardPadding, cellSize)
            }

            // 4. Draw static on-board Blocks (not active and not escaping)
            val escapingIds = escapingAnims.map { it.block.id }.toSet()
            for (b in engine.blocks.filter { !it.isEscaped && it.id != activeBlockId && it.id !in escapingIds }) {
                drawBlock(b, boardPadding, cellSize, 0f, 0f, isSelected = false, alpha = 1f)
            }

            // 5. Draw active dragging Block on top with direct 120fps tracking + spring
            val active = activeBlockId?.let { engine.getBlock(it) }
            if (active != null && !active.isEscaped && active.id !in escapingIds) {
                val totalOffX = dragPixelX + springOffsetX.value
                val totalOffY = dragPixelY + springOffsetY.value
                drawBlock(active, boardPadding, cellSize, totalOffX, totalOffY, isSelected = true, alpha = 1f)
            }

            // 6. Draw blocks animating their escape out of the board!
            for (anim in escapingAnims) {
                val slideDistance = anim.progress * cellSize * 2.2f
                val animX = anim.initialOffsetX + when (anim.escapeSide) {
                    ExitSide.LEFT -> -slideDistance
                    ExitSide.RIGHT -> slideDistance
                    else -> 0f
                }
                val animY = anim.initialOffsetY + when (anim.escapeSide) {
                    ExitSide.TOP -> -slideDistance
                    ExitSide.BOTTOM -> slideDistance
                    else -> 0f
                }
                val alpha = (1f - anim.progress * 0.8f).coerceIn(0f, 1f)
                drawBlock(anim.block, boardPadding, cellSize, animX, animY, isSelected = false, alpha = alpha)
            }

            // 7. Draw rotating confetti particles
            for (p in particles) {
                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    drawRoundRect(
                        color = p.color.copy(alpha = p.alpha),
                        topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 2f),
                        size = Size(p.size, p.size),
                        cornerRadius = CornerRadius(2.5f, 2.5f)
                    )
                }
            }

            // 8. Draw floating score popups (+300, +600 x2!)
            for (s in floatingScores) {
                val style = TextStyle(
                    color = s.color.copy(alpha = s.alpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                val measured = textMeasurer.measure(s.text, style)
                drawText(
                    textMeasurer = textMeasurer,
                    text = s.text,
                    topLeft = Offset(s.x - measured.size.width / 2f, s.y + s.offsetY - measured.size.height / 2f),
                    style = style
                )
            }
        }
    }
}

private fun DrawScope.drawBoardBackground(
    padding: Float,
    width: Float,
    height: Float,
    gridW: Int,
    gridH: Int,
    cellSize: Float
) {
    // 1. Dark outer tubular frame with rounded corners
    val frameThickness = 14f
    drawRoundRect(
        color = Color(0xFF1E233D),
        topLeft = Offset(padding - frameThickness, padding - frameThickness),
        size = Size(width + frameThickness * 2, height + frameThickness * 2),
        cornerRadius = CornerRadius(26f, 26f)
    )

    // Inner board base plate
    drawRoundRect(
        color = Color(0xFF333E65),
        topLeft = Offset(padding, padding),
        size = Size(width, height),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // 2. Soft slate grid tiles with clean separation (identical to iOS original)
    val tileInset = 2.5f
    for (c in 0 until gridW) {
        for (r in 0 until gridH) {
            val tileLeft = padding + c * cellSize + tileInset
            val tileTop = padding + r * cellSize + tileInset
            val tileSize = cellSize - tileInset * 2

            // Tile background
            drawRoundRect(
                color = Color(0xFF4C5882),
                topLeft = Offset(tileLeft, tileTop),
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Subtle tile inner bevel
            drawRoundRect(
                color = Color(0xFF576594),
                topLeft = Offset(tileLeft + 1f, tileTop + 1f),
                size = Size(tileSize - 2f, tileSize - 2f),
                cornerRadius = CornerRadius(7f, 7f),
                style = Stroke(width = 1f)
            )
        }
    }

    // Outer frame inner stroke
    drawRoundRect(
        color = Color(0xFF282F4E),
        topLeft = Offset(padding, padding),
        size = Size(width, height),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawExitDoor(
    door: ExitDoor,
    padding: Float,
    cellSize: Float,
    gridW: Int,
    gridH: Int
) {
    val doorColor = door.color.mainColor
    val doorDark = door.color.darkColor
    val doorLight = door.color.lightColor

    val tabDepth = 26f
    val tabInset = 6f

    when (door.side) {
        ExitSide.TOP -> {
            val left = padding + door.index * cellSize + tabInset
            val right = left + cellSize * door.widthCells - tabInset * 2
            val top = padding - tabDepth
            val bottom = padding + 4f

            val path = Path().apply {
                moveTo(left, bottom)
                lineTo(left, top + 10f)
                quadraticBezierTo(left, top, left + 10f, top)
                lineTo(right - 10f, top)
                quadraticBezierTo(right, top, right, top + 10f)
                lineTo(right, bottom)
                close()
            }

            drawPath(path, Brush.verticalGradient(listOf(doorLight, doorColor), top, bottom))
            drawPath(path, doorDark, style = Stroke(width = 3.5f))

            val arrowMidX = (left + right) / 2f
            val arrowMidY = (top + bottom) / 2f
            drawArrow(arrowMidX, arrowMidY, ExitSide.TOP, Color.White)
        }

        ExitSide.BOTTOM -> {
            val left = padding + door.index * cellSize + tabInset
            val right = left + cellSize * door.widthCells - tabInset * 2
            val top = padding + gridH * cellSize - 4f
            val bottom = top + tabDepth + 4f

            val path = Path().apply {
                moveTo(left, top)
                lineTo(left, bottom - 10f)
                quadraticBezierTo(left, bottom, left + 10f, bottom)
                lineTo(right - 10f, bottom)
                quadraticBezierTo(right, bottom, right, bottom - 10f)
                lineTo(right, top)
                close()
            }

            drawPath(path, Brush.verticalGradient(listOf(doorColor, doorDark), top, bottom))
            drawPath(path, doorDark, style = Stroke(width = 3.5f))

            val arrowMidX = (left + right) / 2f
            val arrowMidY = (top + bottom) / 2f
            drawArrow(arrowMidX, arrowMidY, ExitSide.BOTTOM, Color.White)
        }

        ExitSide.LEFT -> {
            val top = padding + door.index * cellSize + tabInset
            val bottom = top + cellSize * door.widthCells - tabInset * 2
            val left = padding - tabDepth
            val right = padding + 4f

            val path = Path().apply {
                moveTo(right, top)
                lineTo(left + 10f, top)
                quadraticBezierTo(left, top, left, top + 10f)
                lineTo(left, bottom - 10f)
                quadraticBezierTo(left, bottom, left + 10f, bottom)
                lineTo(right, bottom)
                close()
            }

            drawPath(path, Brush.horizontalGradient(listOf(doorLight, doorColor), left, right))
            drawPath(path, doorDark, style = Stroke(width = 3.5f))

            val arrowMidX = (left + right) / 2f
            val arrowMidY = (top + bottom) / 2f
            drawArrow(arrowMidX, arrowMidY, ExitSide.LEFT, Color.White)
        }

        ExitSide.RIGHT -> {
            val top = padding + door.index * cellSize + tabInset
            val bottom = top + cellSize * door.widthCells - tabInset * 2
            val left = padding + gridW * cellSize - 4f
            val right = left + tabDepth + 4f

            val path = Path().apply {
                moveTo(left, top)
                lineTo(right - 10f, top)
                quadraticBezierTo(right, top, right, top + 10f)
                lineTo(right, bottom - 10f)
                quadraticBezierTo(right, bottom, right, bottom - 10f)
                lineTo(left, bottom)
                close()
            }

            drawPath(path, Brush.horizontalGradient(listOf(doorColor, doorDark), left, right))
            drawPath(path, doorDark, style = Stroke(width = 3.5f))

            val arrowMidX = (left + right) / 2f
            val arrowMidY = (top + bottom) / 2f
            drawArrow(arrowMidX, arrowMidY, ExitSide.RIGHT, Color.White)
        }
    }
}

private fun DrawScope.drawArrow(cx: Float, cy: Float, direction: ExitSide, color: Color) {
    val size = 9f
    val path = Path()
    when (direction) {
        ExitSide.TOP -> {
            path.moveTo(cx, cy - size)
            path.lineTo(cx - size, cy + size * 0.7f)
            path.lineTo(cx + size, cy + size * 0.7f)
            path.close()
        }
        ExitSide.BOTTOM -> {
            path.moveTo(cx, cy + size)
            path.lineTo(cx - size, cy - size * 0.7f)
            path.lineTo(cx + size, cy - size * 0.7f)
            path.close()
        }
        ExitSide.LEFT -> {
            path.moveTo(cx - size, cy)
            path.lineTo(cx + size * 0.7f, cy - size)
            path.lineTo(cx + size * 0.7f, cy + size)
            path.close()
        }
        ExitSide.RIGHT -> {
            path.moveTo(cx + size, cy)
            path.lineTo(cx - size * 0.7f, cy - size)
            path.lineTo(cx - size * 0.7f, cy + size)
            path.close()
        }
    }
    drawPath(path, color)
}

private fun DrawScope.drawObstacle(obs: Obstacle, padding: Float, cellSize: Float) {
    val left = padding + obs.x * cellSize + 4f
    val top = padding + obs.y * cellSize + 4f
    val w = obs.width * cellSize - 8f
    val h = obs.height * cellSize - 8f

    // Rock base
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // Rock border
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(left + 4f, top + 4f),
        size = Size(w - 8f, h - 8f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 3f)
    )

    // Crack lines
    drawLine(
        color = Color(0xFF334155),
        start = Offset(left + w * 0.2f, top + h * 0.3f),
        end = Offset(left + w * 0.8f, top + h * 0.7f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawBlock(
    block: Block,
    padding: Float,
    cellSize: Float,
    offsetX: Float,
    offsetY: Float,
    isSelected: Boolean,
    alpha: Float
) {
    val inset = 3.5f
    val left = padding + block.x * cellSize + inset + offsetX
    val top = padding + block.y * cellSize + inset + offsetY
    val width = block.width * cellSize - inset * 2
    val height = block.height * cellSize - inset * 2
    val cornerRadius = 14f

    // 1. Soft 3D drop shadow
    drawRoundRect(
        color = Color(0x66000000).copy(alpha = 0.35f * alpha),
        topLeft = Offset(left + 2f, top + 5f),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // 2. Block base gradient (shiny plastic finish)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                block.color.lightColor.copy(alpha = alpha),
                block.color.mainColor.copy(alpha = alpha)
            ),
            startY = top,
            endY = top + height
        ),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // 3. Subtle outer bevel border
    drawRoundRect(
        color = block.color.darkColor.copy(alpha = 0.85f * alpha),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = 2.5f)
    )

    // 4. Authentic 2x2 Lego studs per grid cell
    val studsX = block.width * 2
    val studsY = block.height * 2
    val studRadius = (width / studsX) * 0.34f

    for (sx in 0 until studsX) {
        for (sy in 0 until studsY) {
            val studCenterX = left + (sx + 0.5f) * (width / studsX)
            val studCenterY = top + (sy + 0.5f) * (height / studsY)

            // Stud bottom shadow
            drawCircle(
                color = block.color.darkColor.copy(alpha = 0.6f * alpha),
                radius = studRadius,
                center = Offset(studCenterX, studCenterY + 1.8f)
            )

            // Stud body
            drawCircle(
                color = block.color.studColor.copy(alpha = alpha),
                radius = studRadius,
                center = Offset(studCenterX, studCenterY)
            )

            // Stud 3D top-left crescent highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.5f * alpha),
                radius = studRadius * 0.65f,
                center = Offset(studCenterX - 1.2f, studCenterY - 1.2f)
            )

            // Stud center cap
            drawCircle(
                color = block.color.mainColor.copy(alpha = alpha),
                radius = studRadius * 0.50f,
                center = Offset(studCenterX, studCenterY)
            )
        }
    }

    // 5. Selection highlight glow
    if (isSelected) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 3.5f)
        )
    }
}

private fun DrawScope.drawSymbol(symbol: String?, cx: Float, cy: Float, size: Float, color: Color) {
    if (symbol == null) return
    val path = Path()
    when (symbol) {
        "star" -> {
            val rOuter = size
            val rInner = size * 0.45f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) rOuter else rInner
                val angle = (i * Math.PI / 5 - Math.PI / 2).toFloat()
                val x = cx + kotlin.math.cos(angle) * r
                val y = cy + kotlin.math.sin(angle) * r
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color)
        }
        "diamond" -> {
            path.moveTo(cx, cy - size)
            path.lineTo(cx + size * 0.8f, cy)
            path.lineTo(cx, cy + size)
            path.lineTo(cx - size * 0.8f, cy)
            path.close()
            drawPath(path, color)
        }
        "square" -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - size * 0.7f, cy - size * 0.7f),
                size = Size(size * 1.4f, size * 1.4f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
        "circle" -> {
            drawCircle(color = color, radius = size * 0.75f, center = Offset(cx, cy))
        }
        "cross" -> {
            val w = size * 0.35f
            val l = size * 1.2f
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - w / 2, cy - l / 2),
                size = Size(w, l),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - l / 2, cy - w / 2),
                size = Size(l, w),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
