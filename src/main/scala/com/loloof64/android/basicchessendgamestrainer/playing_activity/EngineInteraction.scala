package com.loloof64.android.basicchessendgamestrainer.playing_activity

import android.os.{Build, Handler, Looper}
import com.github.bhlangonijr.chesslib.move.Move
import com.loloof64.android.basicchessendgamestrainer.MyApplication
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.BoardUtils.buildSquare
import java.io._
import com.github.ghik.silencer.silent

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
    private val processWriter = new PrintWriter(process.getOutputStream())
    private val processInput = new BufferedReader(new InputStreamReader(process.getInputStream()))
}

object EngineInteraction {

    def processOutput(outputString: String) {
        def runOnUI(block : () => Unit){
            new Handler(Looper.getMainLooper()).post(new Runnable{ def run() { block()} })
        }

        def stringToMove(str: String): Move = {
            def fileFromChar(c: Char):Int = c.toInt - 'a'.toInt
            def rankFromChar(c: Char):Int = c.toInt - '1'.toInt

            return new Move(
                buildSquare( rankFromChar(str(1)), fileFromChar(str(0)) ),
                buildSquare( rankFromChar(str(3)), fileFromChar(str(2)) )
            )
        }

        val bestMoveLineRegex = """^bestmove ([a-h][1-8][a-h][1-8])""".r
        val scoreRegex = """score (cp|mate) (\d+)""".r

        val scoreMatch = scoreRegex.findFirstMatchIn(outputString)
        val bestMoveLineMatch = bestMoveLineRegex.findFirstMatchIn(outputString)

        if (scoreMatch.isDefined) {
            val scoreRegex(scoreType, scoreValue) = outputString
            runOnUI{
                scoreType match {
                    case "cp" => () => uciObserver.consumeScore(Integer.parseInt(scoreValue))
                    case "mate" => () => uciObserver.consumeScore(PlayableAgainstComputerBoardComponent.MIN_MATE_SCORE)
                }
            }
        }
        else if (bestMoveLineMatch.isDefined) {
                val bestMoveLineRegex(moveStr) = outputString
                runOnUI { () => uciObserver.consumeMove( stringToMove(moveStr) ) }
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

    def setUciObserver(observer: SimpleUciObserver){
        this.uciObserver = observer
    }

    private val copyingThread = new Thread {
        val inStream = MyApplication.appContext.getAssets().open(stockfishName)
        val outStream = new FileOutputStream(localStockfishPath)
        val buffer = new Array[Byte](4096)
        var read: Int = 0
        while (read >= 0) {
            read = inStream.read(buffer)
            if (read >= 0) outStream.write(buffer, 0, read)
        }
        outStream.close()
        inStream.close()

        // Giving executable right
        Runtime.getRuntime().exec("/system/bin/chmod 744 $localStockfishPath")
    }

    private var uciObserver: SimpleUciObserver = null
    private var processCommunicator: ProcessCommunicator = null

    private lazy val stockfishName = {
        @silent
        val suffix = Build.CPU_ABI match {
            case "armeabi-v7a" => "arm7"
            case "arm64-v8a" => "arm8"
            case "x86" => "x86"
            case "x86_64" => "x86_64"
            case _ => throw new IllegalArgumentException("Unsupported cpu !")
        }
        s"Stockfish.$suffix"
    }

    private lazy val localStockfishPath = {
        s"${MyApplication.appContext.getFilesDir().getPath()}/stockfish"
    }

    def initStockfishProcessIfNotDoneYet() : Boolean = {
        if (copyingThread.isAlive) return false
        val process = new ProcessBuilder(localStockfishPath).start()
        processCommunicator = new ProcessCommunicator(process)
        val t = new Thread(processCommunicator)
        t.setDaemon(true)
        t.start()

        return true
    }

    def copyStockfishIntoInternalMemoryIfNecessary(){
        if (!(new File(localStockfishPath).exists())) {
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