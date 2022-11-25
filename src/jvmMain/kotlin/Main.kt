import game.v1.*
//import game.v2.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.godaddy.android.colorpicker.ClassicColorPicker
import java.awt.Dimension
import kotlin.math.max
import kotlin.math.min

//+++++++++++++++++++++++++++++++++++++++++
// TODO: optimize next step calculation to increase speed and possibly size og board
//      Idea save a list of all Cells that are Alive and only to calculations with those
//      To do that each cells needs to be able to tell his neighbours of possible state change
//      Each Cell needs to know its position (probably as a Pair) and when Drawing check if Pair is
//      inside view range and only Draw if necessary.
//+++++++++++++++++++++++++++++++++++++++++

/*
    TODO:
        1. Create a new Window to set Settings
            a. change rules?
            b. change buttons for placing depending on rules
            c. change Size
            d save and open current grid as file
            e. change color for live cells and auto adjust color for dead cells
        2. change starting point to middle of grid
        3. optimize as described above
        4. ...
 */

@Composable
@Preview
fun App() {
    var gameState by remember { mutableStateOf(GameState.PAUSED) }
    var zoom by remember { mutableStateOf(0.5f) }
    var offset by remember { mutableStateOf(Pair(0, 0)) }
    val game by remember { mutableStateOf(Game(500, 500)) }
    var faster by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(true) } //TODO Change to false only for debugging
    var color by remember { mutableStateOf(Color.Cyan) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(text = "Controls: ", modifier = Modifier.align(Alignment.CenterVertically))
                        if (gameState == GameState.PAUSED) {
                            Button(
                                onClick = {
                                    gameState = GameState.RUNNING
                                    game.startGame()
                                },
                                colors = (ButtonDefaults.buttonColors(Color(35, 222, 91))),
                                modifier = Modifier.padding(all = 3.dp)
                            )
                            {
                                Text("Start")
                            }
                        } else {
                            Button(
                                onClick = {
                                    gameState = GameState.PAUSED
                                    game.stopGame()
                                },
                                colors = (ButtonDefaults.buttonColors(Color(189, 4, 65))),
                                modifier = Modifier.padding(all = 3.dp)
                            )
                            {
                                Text("Stop")
                            }
                        }
                        var speed by remember { mutableStateOf(300f) }
                        Button(
                            onClick = {
                                gameState = GameState.PAUSED
                                game.resetGame()
                            },
                            colors = (ButtonDefaults.buttonColors(Color.Black)),
                            modifier = Modifier.padding(all = 3.dp)
                        )
                        {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                settings = true;
                            },
                            colors = (ButtonDefaults.buttonColors(Color.White)),
                            modifier = Modifier.padding(all = 3.dp)
                        )
                        {
                            Text("Settings")
                        }
                        Column {
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                // add a trailing zero to the rounded number to keep it at consistent 2 decimals
                                val speedString = speed.toInt().toString()
                                Text("Speed (ms): ")
                                Text(text = speedString)
                            }
                            Slider(
                                value = speed,
                                onValueChange = {
                                    speed = it
                                },
                                colors = SliderDefaults.colors(
                                    activeTickColor = Color.Gray,
                                    inactiveTickColor = Color.Gray,
                                    inactiveTrackColor = Color.White,
                                    activeTrackColor = Color.White,
                                    thumbColor = Color.White,
                                ),
                                modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                                valueRange = 10f..1000f,
                                onValueChangeFinished = {
                                    game.changeSpeed(speed, gameState)
                                }
                            )
                        }
                    }
                },
            )
        },
        content = {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val scope = this
                val minCellsSize = 5
                val maxZoomFactor = 50
                var cellSize =
                    Size(minCellsSize + (zoom * maxZoomFactor), minCellsSize + (zoom * maxZoomFactor))
                val nrCellsWidth = (scope.maxWidth.value / cellSize.width).toInt()
                val nrCellsHeight = (scope.maxHeight.value / (cellSize.height)).toInt()
                Canvas(
                    Modifier.align(Alignment.Center).fillMaxSize()
                        .scrollable(orientation = Orientation.Vertical, state = rememberScrollableState { delta ->
                            offset = Pair(
                                offset.first + 0,
                                max(0, min(game.height, offset.second - delta.toInt() / 10))
                            ) //only Vertical Scroll since adding horizontal scroll seems to trigger vertical as well -> diagonal Scroll ?
                            delta
                        }).pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { tap ->
                                    // Recalculate Cell Size when necessary because on tap does not react to slider change value
                                    cellSize =
                                        Size(
                                            minCellsSize + (zoom * maxZoomFactor),
                                            minCellsSize + (zoom * maxZoomFactor)
                                        )
                                    val tapOffset =
                                        Pair((tap.x / cellSize.width).toInt(), (tap.y / cellSize.height).toInt())
                                    game.set(offset + tapOffset, !(game get (offset + tapOffset)).state)
                                    //println("CLicked at: $tap. -> / ${cellSize.width}; ${cellSize.height} -> Cell ${offset + tapOffset}")
                                    //Output for designing pre-made Cell structures
                                    println("set(offset + Pair(${(offset + tapOffset).first},${(offset + tapOffset).second}), CellState.ALIVE)")
                                }
                            )

                        }) {
                    inset {
                        //drawRect(Color.LightGray, Offset(0f, 0f), Size(scope.maxWidth.value, scope.maxHeight.value))
                        for (w in 0 until nrCellsWidth) {
                            for (h in 0 until nrCellsHeight) {
                                if ((Pair(w, h) + offset).first >= game.width || (Pair(
                                        w,
                                        h
                                    ) + offset).second >= game.height
                                ) break
                                drawRect(
                                    size = cellSize,
                                    color = if ((game get (Pair(
                                            w,
                                            h
                                        ) + offset)).state == CellState.ALIVE
                                    ) color else color.complement(),
                                    topLeft = Offset(cellSize.width * w, cellSize.height * h)
                                )
                                drawRect(
                                    size = Size(cellSize.width + 1, cellSize.height + 1),
                                    color = Color.Gray,
                                    topLeft = Offset(cellSize.width * w, cellSize.height * h),
                                    style = Stroke(2f)
                                )
                                // For Debugging print index of every cell
                                /*drawContext.canvas.nativeCanvas.apply {
                                    drawString(
                                        "${w + offset.first},${h + offset.second}",
                                        Offset(cellSize.width * w, cellSize.height * h).x,
                                        Offset(cellSize.width * w, cellSize.height * h + cellSize.height / 2).y,
                                        Font(Typeface.makeDefault()),
                                        Paint()
                                    )
                                }*/
                            }
                        }
                    }
                }
                // Zoom
                Column(modifier = Modifier.align(Alignment.BottomStart).width(((5 * scope.maxWidth.value) / 8).dp)) {
                    Text("Zoom:")
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        // add a trailing zero to the rounded number to keep it at consistent 2 decimals
                        val speedString = ((zoom * 100).toInt() / 100f).toString()
                        Text(text = if (speedString.length == 3) speedString + "0" else speedString)
                    }
                    Slider(
                        value = zoom,
                        onValueChange = {
                            zoom = it
                        },
                        modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                    )
                }
                // Movement of Grid + random placement + Structures
                Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                    val steps = if (faster) 10 else 1
                    Button(
                        onClick = {
                            game.addGlider(gameState = gameState)
                        },
                        colors = (ButtonDefaults.buttonColors(Color.Cyan)),
                        modifier = Modifier.padding(all = 0.dp).align(Alignment.End),
                        border = BorderStroke(1.dp, Color.Black)
                    )
                    {
                        Text("Glider")
                    }
                    Button(
                        onClick = {
                            game.addGliderGun(gameState = gameState)
                        },
                        colors = (ButtonDefaults.buttonColors(Color.Cyan)),
                        modifier = Modifier.padding(all = 0.dp).align(Alignment.End),
                        border = BorderStroke(1.dp, Color.Black)
                    )
                    {
                        Text("Glider Gun")
                    }
                    Button(
                        onClick = {
                            game.addPentomino(gameState = gameState)
                        },
                        colors = (ButtonDefaults.buttonColors(Color.Cyan)),
                        modifier = Modifier.padding(all = 0.dp).align(Alignment.End),
                        border = BorderStroke(1.dp, Color.Black)
                    )
                    {
                        Text("f-Pentomino")
                    }
                    Button(
                        onClick = {
                            game.random(gameState = gameState)
                        },
                        colors = (ButtonDefaults.buttonColors(Color.Cyan)),
                        modifier = Modifier.padding(all = 0.dp).align(Alignment.End),
                        border = BorderStroke(1.dp, Color.Black)
                    )
                    {
                        Text("Random")
                    }
                    Text("10 Steps:", modifier = Modifier.align(Alignment.End).padding(end = 5.dp))
                    Checkbox(
                        checked = faster,
                        onCheckedChange = {
                            faster = it
                        }, modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        "X,Y: ${offset.first}, ${offset.second}",
                        modifier = Modifier.align(Alignment.End).padding(end = 5.dp)
                    )
                    Button(
                        onClick = {
                            offset = Pair(offset.first, max(0, offset.second - steps))
                        },
                        colors = (ButtonDefaults.buttonColors(Color.Yellow)),
                        modifier = Modifier.padding(all = 0.dp).align(Alignment.CenterHorizontally),
                        border = BorderStroke(1.dp, Color.Black)
                    )
                    {
                        Text("Up")
                    }
                    Row {
                        Button(
                            onClick = {
                                offset = Pair(max(0, offset.first - steps), offset.second)
                            },
                            colors = (ButtonDefaults.buttonColors(Color.Yellow)),
                            modifier = Modifier.padding(all = 0.dp),
                            border = BorderStroke(1.dp, Color.Black)
                        )
                        {
                            Text("Left")
                        }
                        Button(
                            onClick = {
                                offset = Pair(offset.first, min(game.height, offset.second + steps))
                            },
                            colors = (ButtonDefaults.buttonColors(Color.Yellow)),
                            modifier = Modifier.padding(all = 0.dp),
                            border = BorderStroke(1.dp, Color.Black)
                        )
                        {
                            Text("Down")
                        }
                        Button(
                            onClick = {
                                offset = Pair(min(game.width, offset.first + steps), offset.second)
                            },
                            colors = (ButtonDefaults.buttonColors(Color.Yellow)),
                            modifier = Modifier.padding(all = 0.dp),
                            border = BorderStroke(1.dp, Color.Black)
                        )
                        {
                            Text("Right")
                        }
                    }
                }
                // Settings Menu
                if (settings) {
                    BoxWithConstraints(
                        modifier = Modifier.size((scope.maxWidth.value * 0.75).dp, (scope.maxHeight.value * 0.75).dp)
                            .align(
                                Alignment.Center
                            ).background(Color(0.023529f, 0.223529f, 0.439215f, 0.75f))
                    ) {

                        Row(modifier = Modifier.align(Alignment.TopStart)) {
                            //Color Picker
                            Column {
                                ClassicColorPicker(
                                    showAlphaBar = false,
                                    modifier = Modifier.size(
                                        min(
                                            (scope.maxWidth.value * 0.25),
                                            (scope.maxHeight.value * 0.25)
                                        ).dp
                                    ).padding(15.dp),
                                    color = Color.Cyan,
                                    onColorChanged = { c ->
                                        color = c.toColor()
                                    })
                                Text(
                                    "Color: ${(color.red * 255).toInt()}, ${(color.green * 255).toInt()}, ${(color.blue * 255).toInt()}",
                                    modifier = Modifier.padding(start = 15.dp),
                                    color = Color.White
                                )
                            }
                            //Rules
                            Column() {
                                var expanded by remember { mutableStateOf(false) }
                                val rules = listOf("Conway", "B", "C", "D", "E", "F")
                                var selectedIndex by remember { mutableStateOf(0) }
                                Box(
                                    modifier = Modifier.size((scope.maxWidth.value * 0.25).dp)
                                        .wrapContentSize(Alignment.TopStart).padding(15.dp)
                                ) {
                                    Text(
                                        rules[selectedIndex],
                                        modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true })
                                            .background(
                                                Color.Gray
                                            )
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.fillMaxWidth().background(
                                            Color.Gray
                                        )
                                    ) {
                                        rules.forEachIndexed { index, s ->
                                            DropdownMenuItem(onClick = {
                                                selectedIndex = index
                                                expanded = false
                                                game.rule = index
                                            }) {
                                                Text(s)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Row(modifier = Modifier.align(Alignment.BottomEnd), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = {
                                    // TODO open file menu and dave all live cell indices
                                },
                                colors = (ButtonDefaults.buttonColors(Color.White)),
                                modifier = Modifier.padding(all = 15.dp)
                            )
                            {
                                Text("Save to File")
                            }
                            Button(
                                onClick = {
                                    // TODO open file dialog and load list of live cell indices
                                    game.initialize(listOf(Pair(0,0), Pair(1,1), Pair(2,2), Pair(3,3), Pair(4,4), Pair(5,5), Pair(6,6), Pair(7,7)))
                                },
                                colors = (ButtonDefaults.buttonColors(Color.White)),
                                modifier = Modifier.padding(all = 15.dp)
                            )
                            {
                                Text("Load from File")
                            }
                            Button(
                                onClick = {
                                    settings = false
                                },
                                colors = (ButtonDefaults.buttonColors(Color.White)),
                                modifier = Modifier.padding(all = 15.dp)
                            )
                            {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    )
}

// Code to convert rgb to hsv from: https://www.geeksforgeeks.org/program-change-rgb-color-model-hsv-color-model/
@OptIn(ExperimentalGraphicsApi::class) // to be able to use the hsv initializer of Color
fun Color.complement(): Color {
    val max = red.coerceAtLeast(green.coerceAtLeast(blue)); // maximum of r, g, b
    val min = red.coerceAtMost(green.coerceAtMost(blue)); // minimum of r, g, b
    val diff = max - min; // diff of cmax and cmin.
    var h = -1f
    var s = -1f

    when (max) {
        min -> h = 0f
        red -> h = (60 * ((green - blue) / diff) + 360) % 360
        green -> h = (60 * ((blue - red) / diff) + 120) % 360
        blue -> h = (60 * ((red - green) / diff) + 240) % 360
    };

    s = if (max == 0f) 0f else (diff / max)

    val v = max
    // code for contrast color calculation from: https://gamedev.stackexchange.com/questions/38536/given-a-rgb-color-x-how-to-find-the-most-contrasting-color-y
    return Color.hsv(
        (h + 180f) % 360,
        s,
        1f - v
    )
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Reinegger's Game of Life",
        state = rememberWindowState(width = 800.dp, height = 700.dp)
    ) {
        window.minimumSize = Dimension(600, 600)
        App()
    }
}
