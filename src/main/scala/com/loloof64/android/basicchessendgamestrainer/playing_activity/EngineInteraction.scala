package com.loloof64.android.basicchessendgamestrainer.playing_activity

import android.os.{Build, Handler, Looper}
import com.github.bhlangonijr.chesslib.move.Move
import com.loloof64.android.basicchessendgamestrainer.MyApplication
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.BoardUtils.buildSquare
import java.io._

class ProcessCommunicator(process: Process) extends Runnable {

    override def run(){
        while(!mustStop) {
            val line = processInput.readLine()
            if (line != null) EngineInteraction.processOutput(line)
        }
    }

    def sendCommand(command: String){
        processWriter.println(command)
        processWriter.flush()
    }

    def stop(){
        mustStop = true
        processWriter.close()
        processInput.close()
    }

    private var mustStop = false
    private val processWriter = new PrintWriter(process.outputStream)
    private val processInput = new BufferedReader(InputStreamReader(process.inputStream))
}

object EngineInteraction {

    def processOutput(outputString: String) {
        def runOnUI(block : => Unit){
            new Handler(Looper.getMainLooper()).post(block)
        }

        def stringToMove(str: String): Move = {
            def fileFromChar(c: Char):Int = c.toInt() - 'a'.toInt()
            def rankFromChar(c: Char):Int = c.toInt() - '1'.toInt()

            return new Move(
                buildSquare( rankFromChar(str(1)), fileFromChar(str(0)) ),
                buildSquare( rankFromChar(str(3)), fileFromChar(str(2)) )
            )
        }

        val bestMoveLineRegex = Regex("""^bestmove ([a-h][1-8][a-h][1-8])""")
        val scoreRegex = Regex("""score (cp|mate) (\d+)""")
        if (scoreRegex.containsMatchIn(outputString)) {
            val scoreMatcher = scoreRegex.find(outputString)
            val scoreType = scoreMatcher.groups.get(1).value
            val scoreValue = scoreMatcher.groups.get(2).value
            runOnUI{
                when (scoreType){
                    "cp" -> uciObserver.consumeScore(Integer.parseInt(scoreValue))
                    "mate" -> uciObserver.consumeScore(MIN_MATE_SCORE)
                }
            }
        }
        else if (bestMoveLineRegex.containsMatchIn(outputString)) {
                val moveStr = bestMoveLineRegex.find(outputString).groups.get(1).value
                runOnUI { uciObserver.consumeMove( stringToMove(moveStr) ) }
        }
        else {
            println(s"Unrecognized uci output '$outputString'")
        }
    }

    def startNewGame(){
        sendCommandToStockfishProcess("ucinewgame")
    }

    def evaluate(positionFen: String) {
        sendCommandToStockfishProcess(s"position fen $positionFen")
        sendCommandToStockfishProcess("go")
    }

    private val copyingThread = Thread {
        val inStream = MyApplication.appContext.assets.open(stockfishName)
        val outStream = FileOutputStream(localStockfishPath)
        val buffer = ByteArray(4096)
        var read: Int
        while (true) {
            read = inStream.read(buffer)
            if (read <= 0) break
            outStream.write(buffer, 0, read)
        }
        outStream.close()
        inStream.close()

        // Giving executable right
        Runtime.getRuntime().exec("/system/bin/chmod 744 $localStockfishPath")
    }

    private var uciObserver: SimpleUciObserver
    private var processCommunicator: ProcessCommunicator

    private lazy val stockfishName = {
        @SuppressWarning("DEPRECATION")
        val suffix = Build.CPU_ABI match {
            case "armeabi-v7a" => "arm7"
            case "arm64-v8a" => "arm8"
            case "x86" => "x86"
            case "x86_64" => "x86_64"
            case _ => throw IllegalArgumentException("Unsupported cpu !")
        }
        s"Stockfish.$suffix"
    }

    private lazy val localStockfishPath = {
        s"${MyApplication.appContext.filesDir.path}/stockfish"
    }

    def setUciObserver(observer: SimpleUciObserver){
        this.uciObserver = observer
    }

    def initStockfishProcessIfNotDoneYet() : Boolean = {
        if (copyingThread.isAlive) return false
        val process = new ProcessBuilder(localStockfishPath).start()
        processCommunicator = new ProcessCommunicator(process)
        val t = new Thread(processCommunicator)
        t.isDaemon = true
        t.start()

        return true
    }

    def copyStockfishIntoInternalMemoryIfNecessary(){
        if (!File(localStockfishPath).exists()) {
            copyingThread.start()
        }
    }

    private def sendCommandToStockfishProcess(command: String){
        processCommunicator.sendCommand(command)
    }

    def closeStockfishProcess(){
        processCommunicator.stop()
    }


}

trait SimpleUciObserver {
    def consumeMove(move: Move)
    def consumeScore(score: Int)
}