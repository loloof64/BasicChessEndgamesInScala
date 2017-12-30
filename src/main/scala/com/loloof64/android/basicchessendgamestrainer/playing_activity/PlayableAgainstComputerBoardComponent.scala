package com.loloof64.android.basicchessendgamestrainer.playing_activity

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.os.{Handler, Looper}
import com.github.bhlangonijr.chesslib.{Board, Piece, Side, Square}
import com.github.bhlangonijr.chesslib.move.{Move, MoveGenerator, MoveList}
import com.loloof64.android.basicchessendgamestrainer.PlayingActivity
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.BoardUtils.buildSquare
import com.loloof64.android.basicchessendgamestrainer.R
import java.lang.Math.abs
import java.util.logging.Logger
import com.github.ghik.silencer.silent

case class PromotionInfo(startFile: Int, startRank: Int,
                         endFile: Int, endRank: Int)

object PlayableAgainstComputerBoardComponent {
    val MIN_MATE_SCORE = 1000
}

case class PlayableAgainstComputerBoardComponent(context: Context, attrs: AttributeSet,
                             defStyleAttr: Int) extends BoardComponent(context, attrs, defStyleAttr) with SimpleUciObserver{

    private def moveStringToFAN(positionStr: String, move: Move): String = {
        val board = new Board()
        board.loadFromFEN(positionStr)
        val moveList = new MoveList(board.getFEN())
        moveList.add(move)
        moveList.toFANArray()(0)
    }

    def isDrawByRepetitions:Boolean = {
        val historySize = _relatedPosition.getHistory.size
        val i = Math.min(historySize - 1, _relatedPosition.getHalfMoveCounter)
        var rept = 0
        if (historySize >= 4) {
            val lastKey = _relatedPosition.getHistory.get(historySize - 1)
            var x = 2
            while (x <= i) {
                val k = _relatedPosition.getHistory.get(historySize - x - 1)
                if (k == lastKey) {
                    rept += 1
                    if (rept >= 2) {
                        return true
                    }
                }
                x += 2
            }
        }
        return false
    }

    override def consumeMove(move: Move) {
        if (!_waitingForPlayerGoal && !_gameFinished){
            val isComputerToMove = _playerHasWhite != isWhiteToPlay
            if (isComputerToMove){
                new Handler(Looper.getMainLooper).post(new Runnable {
                    override def run(): Unit = {
                        addMoveToList(move, moveStringToFAN(_relatedPosition.getFEN(), move))
                        _moveToHighlightFrom = move.getFrom
                        _moveToHighlightTo = move.getTo
                        updateHighlightedMove()

                        invalidate()
                        checkIfGameFinished()
                    }
                })
            }
        }
    }

    override def consumeScore(score: Int) {
        val MIN_DRAW_SCORE = 10
        val MIN_KNOWN_WIN = 250

        if (_waitingForPlayerGoal){

            val stringId = if (abs(score) > PlayableAgainstComputerBoardComponent.MIN_MATE_SCORE) {
                if (isWhiteToPlay) R.string.white_play_for_mate
                else R.string.black_play_for_mate
            } else if (abs(score) > MIN_KNOWN_WIN) {
                if (isWhiteToPlay) R.string.white_play_for_win
                else R.string.black_play_for_win
            } 
            else if (abs(score) <= MIN_DRAW_SCORE) R.string.should_be_draw
            else R.string.empty_string

            context match {
                case p:PlayingActivity => p.setPlayerGoalTextId(stringId, alertMode = false)
            }
            _waitingForPlayerGoal = false
        }
    }

    def this(context: Context, attrs: AttributeSet) { this(context, attrs, 0) }
    def this(context: Context) { this(context, null, 0) }

    private val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayableAgainstComputerBoardComponent)
    private val computed = typedArray.getInt(R.styleable
            .PlayableAgainstComputerBoardComponent_min_dimension_percentage, 100)
    typedArray.recycle()
    private val minSpacePercentage = computed
    EngineInteraction.setUciObserver(this)

    override def computeMinAvailableSpacePercentage: Int = minSpacePercentage

    def isWaitingForPlayerGoal:Boolean = _waitingForPlayerGoal

    // Mainly used for serialisation purpose
    private def setWaitingForPlayerGoalFlag(waiting: Boolean){
        _waitingForPlayerGoal = waiting
    }

    private def waitForPlayerGoal() {
        _waitingForPlayerGoal = true
        EngineInteraction.evaluate(_relatedPosition.getFEN())
    }

    override def relatedPosition: Board = {
        _relatedPosition
    }

    override def replacePositionWith(positionFEN: String) {
        _relatedPosition.loadFromFEN(positionFEN)
    }

    override def highlightedTargetCell: SquareCoordinates = {
        _highlightedTargetCell
    }

    override def highlightedStartCell: SquareCoordinates = {
        _highlightedStartCell
    }

    private def askForPromotionPiece() {
        context match {
            case p:PlayingActivity => p.askForPromotionPiece()
        }
    }

    private def reactForIllegalMove() {
        context match {
            case p:PlayingActivity => p.reactForIllegalMove()
        }
    }

    // Fields from PieceMoveInteraction interface
    private val _relatedPosition = new Board()
    private var _highlightedTargetCell:SquareCoordinates = _
    private var _highlightedStartCell:SquareCoordinates = _
    private var _pendingPromotionInfo:PromotionInfo = _

    private def moveListToArray(list: MoveList) = {
        (for (e <- 0 until list.size) yield list.get(e)).toArray
    }

    @silent
    override def onTouchEvent(event: MotionEvent): Boolean = {

        val whiteTurn = _relatedPosition.getSideToMove == Side.WHITE
        val notPlayerTurn = _playerHasWhite != whiteTurn
        if (notPlayerTurn || _gameFinished) return true

        _waitingForPlayerGoal = false

        val x = event.getX()
        val y = event.getY()
        val action = event.getAction

        val cellSize = getMeasuredWidth.min(getMeasuredHeight) / 9
        val cellX = ((x-cellSize*0.5) / cellSize).toInt
        val cellY = ((y-cellSize*0.5) / cellSize).toInt
        val file = if (reversed) 7-cellX else cellX
        val rank = if (reversed) cellY else 7 - cellY

        if ((0 to 7).contains(file)  && (0 to 7).contains(rank)){
            action match {
                case MotionEvent.ACTION_UP =>
                    val moveSelectionHasStarted = _highlightedTargetCell != null && _highlightedStartCell != null
                    if (moveSelectionHasStarted){
                        val startFile = _highlightedStartCell.file
                        val startRank = _highlightedStartCell.rank
                        val endFile = _highlightedTargetCell.file
                        val endRank = _highlightedTargetCell.rank

                        val legalMovesList = MoveGenerator.getInstance().generateLegalMoves(_relatedPosition)
                        val legalMovesArray = moveListToArray(legalMovesList)
                        val matchingMoves = legalMovesArray.filter { move =>
                            val matchingMoveStartCell = move.getFrom
                            val matchingMoveEndCell = move.getTo
                            val playerMoveStartCell = buildSquare(startRank, startFile)
                            val playerMoveEndCell = buildSquare(endRank, endFile)

                            (matchingMoveStartCell == playerMoveStartCell) && (matchingMoveEndCell == playerMoveEndCell)
                        }

                        val isPromotionMove = !matchingMoves.isEmpty && matchingMoves(0).getPromotion != Piece.NONE

                        if (isPromotionMove) {
                            _pendingPromotionInfo = PromotionInfo(startFile, startRank, endFile, endRank)
                            askForPromotionPiece()
                        }
                        else {
                            val sameCellSelected = (startFile == endFile) && (startRank == endRank)
                            if (matchingMoves.isEmpty) {
                                if (!sameCellSelected) reactForIllegalMove()
                            } else {
                                updateHighlightedMove()
                                val move = matchingMoves(0)
                                addMoveToList(move, moveStringToFAN(_relatedPosition.getFEN(), move))
                            }
                        }

                        invalidate()
                        checkIfGameFinished()
                        if (!_gameFinished) {
                            val computerToPlay = _playerHasWhite != isWhiteToPlay
                            if (computerToPlay) makeComputerPlay()
                        }
                    }
                    _highlightedStartCell = null
                    _highlightedTargetCell = null

                case MotionEvent.ACTION_DOWN =>
                    val movedPiece = _relatedPosition.getPiece(buildSquare(file = file, rank = rank))
                    val isOccupiedSquare = movedPiece != Piece.NONE
                    val isWhiteTurn = _relatedPosition.getSideToMove == Side.WHITE
                    val isWhitePiece = movedPiece.getPieceSide == Side.WHITE
                    val isOneOfOurPiece = (isWhiteTurn && isWhitePiece) || (!isWhiteTurn && !isWhitePiece)

                    _highlightedTargetCell = if (isOccupiedSquare && isOneOfOurPiece) SquareCoordinates(file = file, rank = rank) else null
                    _highlightedStartCell = _highlightedTargetCell
                    invalidate()

                case MotionEvent.ACTION_MOVE =>
                    val moveSelectionHasStarted = _highlightedTargetCell != null && _highlightedStartCell != null
                    if (moveSelectionHasStarted) {
                        _highlightedTargetCell = SquareCoordinates(file = file, rank = rank)
                        invalidate()
                    }

            }
        }

        true
    }

    def playerHasWhite: Boolean = _playerHasWhite

    def reloadPosition(fen: String, playerHasWhite: Boolean,
                       gameFinished: Boolean, waitingForPlayerGoal: Boolean,
                       hasStartedToWriteMoves: Boolean,
                       moveToHighlightFromFile: Option[Int], moveToHighlightFromRank: Option[Int],
                       moveToHighlightToFile: Option[Int], moveToHighlightToRank: Option[Int],
                       blacksAreDown: Boolean){
        try {
            _gameFinished = gameFinished
            context match{
                case p:PlayingActivity =>
                    if (_gameFinished) {
                        p.activatePositionNavigation()
                    } else {
                        p.disallowPositionNavigation()
                    }
            }
            setBlackDown(blacksAreDown)
            _relatedPosition.loadFromFEN(fen)
            _playerHasWhite = playerHasWhite
            _startedToWriteMoves = hasStartedToWriteMoves
            _moveToHighlightFrom = if ( (0 to 7).contains(moveToHighlightFromFile) &&
                    (0 to 7).contains(moveToHighlightFromRank) ) buildSquare(file = moveToHighlightFromFile.get, rank = moveToHighlightFromRank.get) else null
            _moveToHighlightTo = if ((0 to 7).contains(moveToHighlightToFile) &&
                    (0 to 7).contains(moveToHighlightToRank)) buildSquare(file = moveToHighlightToFile.get, rank = moveToHighlightToRank.get) else null
            updateHighlightedMove()

            setWaitingForPlayerGoalFlag(waitingForPlayerGoal)
            invalidate()
            val weMustNotLetComputerPlay = _gameFinished || playerHasWhite == isWhiteToPlay
            if (!weMustNotLetComputerPlay) makeComputerPlay()
        }
        catch {
            case _:IllegalArgumentException => Logger.getLogger("BasicChessEndgamesTrainer").severe(s"Position $fen is invalid and could not be load.")
        }
    }

    def new_game(startFen: String) {
        try {
            _gameFinished = false
            context match{
                case p:PlayingActivity => p.disallowPositionNavigation()
            }
            _startedToWriteMoves = false
            _moveToHighlightFrom = null
            _moveToHighlightTo = null
            updateHighlightedMove()

            EngineInteraction.startNewGame()
            if (!startFen.isEmpty) {
                _relatedPosition.loadFromFEN(startFen)
                _blackStartedTheGame = _relatedPosition.getSideToMove == Side.BLACK
            }
            _playerHasWhite = isWhiteToPlay
            waitForPlayerGoal()
            invalidate()
        }
        catch {
            case _:IllegalArgumentException => Logger.getLogger("BasicChessEndgamesTrainer").severe(s"Position $startFen is invalid and could not be load.")
        }
    }

    def isWhiteToPlay : Boolean = {
        _relatedPosition.getSideToMove == Side.WHITE
    }

    def makeComputerPlay(){
        val isComputerToMove = _playerHasWhite != isWhiteToPlay
        if (isComputerToMove) {
            EngineInteraction.evaluate(_relatedPosition.getFEN())
        }
    }

    def checkIfGameFinished() {
        def checkIfGameFinished(finishMessageId: Int, finishTest: () => Boolean) : Boolean = {
            if (finishTest()){
                _gameFinished = true
                context match{
                    case p:PlayingActivity =>
                        p.setPlayerGoalTextId(finishMessageId, alertMode = true)
                        p.activatePositionNavigation()

                }
            }
            _gameFinished
        }

        if (!checkIfGameFinished(R.string.checkmate, { () => _relatedPosition.isMated }))
            if (!checkIfGameFinished(R.string.missing_material_draw, { () => _relatedPosition.isInsufficientMaterial }))
                if (!checkIfGameFinished(R.string.position_repetitions_draw, { () => isDrawByRepetitions }))
                    if (!checkIfGameFinished(R.string.stalemate,{ () => _relatedPosition.isStaleMate }))
                        if (!checkIfGameFinished(R.string.fiftyMoveDraw,{ () => _relatedPosition.getMoveCounter >= 100 })){}
    }

    private def addMoveToList(move: Move, moveFan: String) {
        context match {
            case p:PlayingActivity =>

                val isWhiteTurnBeforeMove = _relatedPosition.getSideToMove == Side.WHITE

                val moveNumberBeforeMoveCommit = getMoveNumber

                _relatedPosition.doMove(move)
                val fenAfterMove = _relatedPosition.getFEN()

                val moveToHighlight = MoveToHighlight(
                        startFile = move.getFrom.getFile.ordinal,
                        startRank = move.getFrom.getRank.ordinal,
                        endFile = move.getTo.getFile.ordinal,
                        endRank = move.getTo.getRank.ordinal
                )

                // Registering move san into history
                if (!_startedToWriteMoves && !isWhiteTurnBeforeMove){
                        p.addPositionInMovesList(moveNumberBeforeMoveCommit.toString, "", MoveToHighlight(-1,-1,-1,-1))
                        p.addPositionInMovesList("..", "",MoveToHighlight(-1,-1,-1,-1))
                        p.addPositionInMovesList(moveFan,
                            fen = fenAfterMove, moveToHighlight = moveToHighlight)
                }
                else {
                        if (isWhiteTurnBeforeMove) {
                            p.addPositionInMovesList(moveNumberBeforeMoveCommit.toString, "",
                                    MoveToHighlight(-1,-1,-1,-1))
                            p.addPositionInMovesList(
                                    san = moveFan,
                                    fen = fenAfterMove,
                                    moveToHighlight = moveToHighlight
                            )
                        }
                }

        }
        _startedToWriteMoves = true
    }

    def validatePromotionMove(givenPromotionPiece: Piece) {
        _pendingPromotionInfo match {
            case null =>
            case _ =>

                val startSquare = buildSquare(file = _pendingPromotionInfo.startFile,
                        rank = _pendingPromotionInfo.startRank)
                val endSquare = buildSquare(file = _pendingPromotionInfo.endFile,
                        rank = _pendingPromotionInfo.endRank)
                val legalMovesList = MoveGenerator.getInstance().generateLegalMoves(_relatedPosition)
                val legalMovesArray = moveListToArray(legalMovesList)
                val matchingMoves = legalMovesArray.filter { currentMove =>
                    val currentMoveStartSquare = currentMove.getFrom
                    val currentMoveEndSquare = currentMove.getTo
                    val matchingMovePromotionPiece = currentMove.getPromotion

                    currentMoveStartSquare == startSquare && 
                        currentMoveEndSquare == endSquare && 
                        matchingMovePromotionPiece == givenPromotionPiece
                }
                if (matchingMoves.isEmpty) Logger.getLogger("BasicChessEndgamesTrainer").severe("Illegal move ! (When validating promotion)")
                else {
                    val move = matchingMoves(0)
                    addMoveToList(move, moveStringToFAN(_relatedPosition.getFEN(), move))
                }
                _pendingPromotionInfo = null
                _highlightedTargetCell = null
                invalidate()

        }
    }

    def gameFinished: Boolean = _gameFinished
    def hasStartedToWriteMoves: Boolean = _startedToWriteMoves

    private def getMoveNumber: Int = {
        val c = _relatedPosition.getMoveCounter
        if (_blackStartedTheGame) ((c-(c%2))/2) + 1
            else (c / 2) + 1
    }

    override def setHighlightedMove(fromFile: Int, fromRank: Int, toFile: Int, toRank: Int) {
        if (!(0 to 7).contains(fromFile)) throw new IllegalArgumentException()
        if (!(0 to 7).contains(fromRank)) throw new IllegalArgumentException()
        if (!(0 to 7).contains(toFile)) throw new IllegalArgumentException()
        if (!(0 to 7).contains(toRank)) throw new IllegalArgumentException()

        super.setHighlightedMove(fromFile, fromRank, toFile, toRank)
        _moveToHighlightFrom = buildSquare(file = fromFile, rank = fromRank)
        _moveToHighlightTo = buildSquare(file = toFile, rank = toRank)
    }

    def getMoveToHighlightFromFile:Option[Int] =
        if (_moveToHighlightFrom != null) Some(_moveToHighlightFrom.getFile.ordinal) else None

    def getMoveToHighlightFromRank:Option[Int] = if (_moveToHighlightFrom != null) Some(_moveToHighlightFrom.getRank.ordinal) else None

    def getMoveToHighlightToFile:Option[Int] = if (_moveToHighlightTo != null) Some(_moveToHighlightTo.getFile.ordinal) else None

    def getMoveToHighlightToRank:Option[Int] = if (_moveToHighlightTo != null) Some(_moveToHighlightTo.getRank.ordinal) else None

    private def updateHighlightedMove(){
        val from = _moveToHighlightFrom
        val to = _moveToHighlightTo
        if (from != null && to != null){
            setHighlightedMove(
                    from.getFile.ordinal, from.getRank.ordinal,
                    to.getFile.ordinal, to.getRank.ordinal
            )
        }
        else {
            clearHighlightedMove()
        }

    }

    private var _playerHasWhite = true
    private var _gameFinished = false
    private var _waitingForPlayerGoal = true
    private var _startedToWriteMoves = false
    private var _moveToHighlightFrom:Square = _
    private var _moveToHighlightTo:Square = _
    private var _blackStartedTheGame = false
}