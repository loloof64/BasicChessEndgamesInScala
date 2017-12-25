package com.loloof64.android.basicchessendgamestrainer.playing_activity

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.bhlangonijr.chesslib.{Board, Piece, Side, Square}
import com.github.bhlangonijr.chesslib.move.{Move, MoveGenerator, MoveList}
import com.loloof64.android.basicchessendgamestrainer.PlayingActivity
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.BoardUtils.buildSquare
import com.loloof64.android.basicchessendgamestrainer.R
import java.lang.Math.abs
import java.util.logging.Logger

case class PromotionInfo(val startFile: Int, val startRank: Int,
                         val endFile: Int, val endRank: Int)

case class PlayableAgainstComputerBoardComponent(context: Context, attrs: AttributeSet,
                             defStyleAttr: Int) extends BoardComponent(context, attrs, defStyleAttr) with SimpleUciObserver{

    private def moveStringToFAN(moveStr: String, move: Move): String = {
        val board = new Board()
        board.loadFromFEN(this)
        val moveList = new MoveList(board.fen)
        moveList.add(move)
        return moveList.toFANArray()(0)
    }

    def isDrawByRepetitions():Boolean = {
        val historySize = _relatedPosition.history.size
        val i = Math.min(historySize - 1, _relatedPosition.getHalfMoveCounter())
        var rept = 0
        if (historySize >= 4) {
            val lastKey = _relatedPosition.getHistory().get(historySize - 1)
            var x = 2
            while (x <= i) {
                val k = _relatedPosition.getHistory().get(historySize - x - 1)
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
            val isComputerToMove = _playerHasWhite != isWhiteToPlay()
            if (isComputerToMove){
                handler.post {
                    addMoveToList(move, moveStringToFAN(_relatedPosition.fen, move))
                    _moveToHighlightFrom = move.from
                    _moveToHighlightTo = move.to
                     updateHighlightedMove()

                    invalidate()
                    checkIfGameFinished()
                }
            }
        }
    }

    override def consumeScore(score: Int) {
        val MIN_MATE_SCORE = 1000
        val MIN_DRAW_SCORE = 10
        val MIN_KNOWN_WIN = 250

        if (_waitingForPlayerGoal){

            val stringId = if (abs(score) > MIN_MATE_SCORE) {
                if (isWhiteToPlay()) R.string.white_play_for_mate
                else R.string.black_play_for_mate
            } else if (abs(score) > MIN_KNOWN_WIN) {
                if (isWhiteToPlay()) R.string.white_play_for_win
                else R.string.black_play_for_win
            } 
            else if (abs(score) <= MIN_DRAW_SCORE) R.string.should_be_draw
            else R.string.empty_string

            context match {
                case p:PlayingActivity => p.setPlayerGoalTextId(stringId, false)
            }
            _waitingForPlayerGoal = false
        }
    }

    this(context: Context, attrs: AttributeSet) { this(context, attrs, 0) }
    this(context: Context) { this(context, null, 0) }

    private val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayableAgainstComputerBoardComponent)
    private val computed = typedArray.getInt(R.styleable
            .PlayableAgainstComputerBoardComponent_min_dimension_percentage, 100)
    typedArray.recycle()
    private val minSpacePercentage = computed
    EngineInteraction.setUciObserver(this)

    override def computeMinAvailableSpacePercentage() = minSpacePercentage

    def isWaitingForPlayerGoal() = _waitingForPlayerGoal

    // Mainly used for serialisation purpose
    private def setWaitingForPlayerGoalFlag(waiting: Boolean){
        _waitingForPlayerGoal = waiting
    }

    private def waitForPlayerGoal() {
        _waitingForPlayerGoal = true
        EngineInteraction.evaluate(_relatedPosition.fen)
    }

    override def relatedPosition(): Board = {
        return _relatedPosition
    }

    override def replacePositionWith(positionFEN: String) {
        _relatedPosition.loadFromFEN(positionFEN)
    }

    override def highlightedTargetCell(): SquareCoordinates = {
        return _highlightedTargetCell
    }

    override def highlightedStartCell(): SquareCoordinates = {
        return _highlightedStartCell
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
    private var _highlightedTargetCell:SquareCoordinates = null
    private var _highlightedStartCell:SquareCoordinates = null
    private var _pendingPromotionInfo:PromotionInfo = null

    @SuppressWarning("ClickableViewAccessibility")
    override def onTouchEvent(event: MotionEvent): Boolean = {
        val whiteTurn = _relatedPosition.sideToMove == Side.WHITE
        val notPlayerTurn = _playerHasWhite != whiteTurn
        if (notPlayerTurn || _gameFinished) return true

        _waitingForPlayerGoal = false

        val x = event.x
        val y = event.y
        val action = event.action

        val cellSize = (measuredWidth.min(measuredHeight)) / 9
        val cellX = ((x-cellSize*0.5) / cellSize).toInt()
        val cellY = ((y-cellSize*0.5) / cellSize).toInt()
        val file = if (reversed) 7-cellX else cellX
        val rank = if (reversed) cellY else 7 - cellY

        if ((0 to 7).contains(file)  && (0 to 7).contains(rank)){
            action match {
                case MotionEvent.ACTION_UP => {
                    val moveSelectionHasStarted = _highlightedTargetCell != null && _highlightedStartCell != null
                    if (moveSelectionHasStarted){
                        val startFile = _highlightedStartCell.file
                        val startRank = _highlightedStartCell.rank
                        val endFile = _highlightedTargetCell.file
                        val endRank = _highlightedTargetCell.rank

                        val legalMovesList = MoveGenerator.getInstance().generateLegalMoves(_relatedPosition)
                        val matchingMoves = legalMovesList.filter { move =>
                            val matchingMoveStartCell = move.from
                            val matchingMoveEndCell = move.to
                            val playerMoveStartCell = buildSquare(startRank, startFile)
                            val playerMoveEndCell = buildSquare(endRank, endFile)

                            (matchingMoveStartCell == playerMoveStartCell) && (matchingMoveEndCell == playerMoveEndCell)
                        }

                        val isPromotionMove = matchingMoves.isNotEmpty() && matchingMoves(0).promotion != Piece.NONE

                        if (isPromotionMove) {
                            _pendingPromotionInfo = PromotionInfo(startFile, startRank, endFile, endRank)
                            askForPromotionPiece()
                        }
                        else {
                            val sameCellSelected = (startFile == endFile) && (startRank == endRank)
                            if (matchingMoves.isEmpty()) {
                                if (!sameCellSelected) reactForIllegalMove()
                            } else {
                                updateHighlightedMove()
                                val move = matchingMoves(0)
                                addMoveToList(move, moveStringToFAN(_relatedPosition.fen, move))
                            }
                        }

                        invalidate()
                        checkIfGameFinished()
                        if (!_gameFinished) {
                            val computerToPlay = _playerHasWhite != isWhiteToPlay()
                            if (computerToPlay) makeComputerPlay()
                        }
                    }
                    _highlightedStartCell = null
                    _highlightedTargetCell = null
                }
                case MotionEvent.ACTION_DOWN => {
                    val movedPiece = _relatedPosition.getPiece(buildSquare(file = file, rank = rank))
                    val isOccupiedSquare = movedPiece != Piece.NONE
                    val isWhiteTurn = _relatedPosition.sideToMove == Side.WHITE
                    val isWhitePiece = movedPiece.pieceSide == Side.WHITE
                    val isOneOfOurPiece = (isWhiteTurn && isWhitePiece) || (!isWhiteTurn && !isWhitePiece)

                    _highlightedTargetCell = if (isOccupiedSquare && isOneOfOurPiece) SquareCoordinates(file = file, rank = rank) else null
                    _highlightedStartCell = _highlightedTargetCell
                    invalidate()
                }
                case MotionEvent.ACTION_MOVE => {
                    val moveSelectionHasStarted = _highlightedTargetCell != null && _highlightedStartCell != null
                    if (moveSelectionHasStarted) {
                        _highlightedTargetCell = SquareCoordinates(file = file, rank = rank)
                        invalidate()
                    }
                }
            }
        }

        return true
    }

    def playerHasWhite() = _playerHasWhite

    def reloadPosition(fen: String, playerHasWhite: Boolean,
                       gameFinished: Boolean, waitingForPlayerGoal: Boolean,
                       hasStartedToWriteMoves: Boolean,
                       moveToHighlightFromFile: Int, moveToHighlightFromRank: Int,
                       moveToHighlightToFile: Int, moveToHighlightToRank: Int,
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
                    (0 to 7).contains(moveToHighlightFromRank) ) buildSquare(file = moveToHighlightFromFile, rank = moveToHighlightFromRank) else null
            _moveToHighlightTo = if ((0 to 7).contains(moveToHighlightToFile) &&
                    (0 to 7).contains(moveToHighlightToRank)) buildSquare(file = moveToHighlightToFile, rank = moveToHighlightToRank) else null
            updateHighlightedMove()

            setWaitingForPlayerGoalFlag(waitingForPlayerGoal)
            invalidate()
            val weMustNotLetComputerPlay = _gameFinished || playerHasWhite == isWhiteToPlay()
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
            if (startFen.isNotEmpty()) {
                _relatedPosition.loadFromFEN(startFen)
                _blackStartedTheGame = _relatedPosition.sideToMove == Side.BLACK
            }
            _playerHasWhite = isWhiteToPlay()
            waitForPlayerGoal()
            invalidate()
        }
        catch {
            case _:IllegalArgumentException => Logger.getLogger("BasicChessEndgamesTrainer").severe(s"Position $startFen is invalid and could not be load.")
        }
    }

    def isWhiteToPlay() : Boolean = {
        return _relatedPosition.sideToMove == Side.WHITE
    }

    def makeComputerPlay(){
        val isComputerToMove = _playerHasWhite != isWhiteToPlay()
        if (isComputerToMove) {
            EngineInteraction.evaluate(_relatedPosition.fen)
        }
    }

    def checkIfGameFinished() {
        def checkIfGameFinished(finishMessageId: Int, finishTest: => Boolean) : Boolean = {
            if (finishTest){
                _gameFinished = true
                context match{
                    case p:PlayingActivity => withp{
                        setPlayerGoalTextId(finishMessageId, alertMode = true)
                        activatePositionNavigation()
                    }
                }
            }
            return _gameFinished
        }

        if (!checkIfGameFinished(R.string.checkmate){ _relatedPosition.isMated })
            if (!checkIfGameFinished(R.string.missing_material_draw){ _relatedPosition.isInsufficientMaterial })
                if (!checkIfGameFinished(R.string.position_repetitions_draw){ isDrawByRepetitions() })
                    if (!checkIfGameFinished(R.string.stalemate){ _relatedPosition.isStaleMate })
                        if (!checkIfGameFinished(R.string.fiftyMoveDraw){ _relatedPosition.moveCounter >= 100 }){}
    }

    private def addMoveToList(move: Move, moveFan: String) {
        when (context) {
            case p:PlayingActivity => {

                val isWhiteTurnBeforeMove = _relatedPosition.sideToMove == Side.WHITE

                val moveNumberBeforeMoveCommit = getMoveNumber()

                _relatedPosition.doMove(move)
                val fenAfterMove = _relatedPosition.fen

                val moveToHighlight = MoveToHighlight(
                        startFile = move.from.file.ordinal,
                        startRank = move.from.rank.ordinal,
                        endFile = move.to.file.ordinal,
                        endRank = move.to.rank.ordinal
                )

                // Registering move san into history
                if (!_startedToWriteMoves && !isWhiteTurnBeforeMove){
                    withp{
                        addPositionInMovesList(moveNumberBeforeMoveCommit.toString(), "", MoveToHighlight(-1,-1,-1,-1))
                        addPositionInMovesList("..", "",MoveToHighlight(-1,-1,-1,-1))
                        addPositionInMovesList(moveFan,
                            fen = fenAfterMove, moveToHighlight = moveToHighlight)
                    }
                }
                else {
                    withp{
                        if (isWhiteTurnBeforeMove) addPositionInMovesList(moveNumberBeforeMoveCommit.toString(), "",
                                MoveToHighlight(-1,-1,-1,-1))
                        addPositionInMovesList(
                                san = moveFan,
                                fen = fenAfterMove,
                                moveToHighlight = moveToHighlight
                        )
                    }
                }
            }
        }
        _startedToWriteMoves = true
    }

    def validatePromotionMove(givenPromotionPiece: Piece) {
        _pendingPromotionInfo match {
            case null => {}
            case _ => {

                val startSquare = buildSquare(file = _pendingPromotionInfo.startFile,
                        rank = _pendingPromotionInfo.startRank)
                val endSquare = buildSquare(file = _pendingPromotionInfo.endFile,
                        rank = _pendingPromotionInfo.endRank)
                val legalMovesList = MoveGenerator.getInstance().generateLegalMoves(_relatedPosition)
                val matchingMoves = legalMovesList.filter { currentMove =>
                    val currentMoveStartSquare = currentMove.from
                    val currentMoveEndSquare = currentMove.to
                    val matchingMovePromotionPiece = currentMove.promotion

                    currentMoveStartSquare == startSquare
                    && currentMoveEndSquare == endSquare
                    && matchingMovePromotionPiece == givenPromotionPiece
                }
                if (matchingMoves.isEmpty()) Logger.getLogger("BasicChessEndgamesTrainer").severe("Illegal move ! (When validating promotion)")
                else {
                    val move = matchingMoves(0)
                    addMoveToList(move, moveStringToFAN(_relatedPosition.fen, move))
                }
                _pendingPromotionInfo = null
                _highlightedTargetCell = null
                invalidate()
            }
        }
    }

    def gameFinished() = _gameFinished
    def hasStartedToWriteMoves() = _startedToWriteMoves

    private def getMoveNumber(): Int = {
        val c = _relatedPosition.moveCounter
        return if (_blackStartedTheGame) ((c-(c%2))/2) + 1
            else (c / 2) + 1
    }

    override def setHighlightedMove(fromFile: Int, fromRank: Int, toFile: Int, toRank: Int) {
        if (!(0 to 7).contains(fromFile)) throw IllegalArgumentException()
        if (!(0 to 7).contains(fromRank)) throw IllegalArgumentException()
        if (!(0 to 7).contains(toFile)) throw IllegalArgumentException()
        if (!(0 to 7).contains(toRank)) throw IllegalArgumentException()

        super.setHighlightedMove(fromFile, fromRank, toFile, toRank)
        _moveToHighlightFrom = buildSquare(file = fromFile, rank = fromRank)
        _moveToHighlightTo = buildSquare(file = toFile, rank = toRank)
    }

    def getMoveToHighlightFromFile():Int = _moveToHighlightFrom.file.ordinal

    def getMoveToHighlightFromRank():Int = _moveToHighlightFrom.rank.ordinal

    def getMoveToHighlightToFile():Int = _moveToHighlightTo.file.ordinal

    def getMoveToHighlightToRank():Int = _moveToHighlightTo.rank.ordinal

    private def updateHighlightedMove(){
        val from = _moveToHighlightFrom
        val to = _moveToHighlightTo
        if (from != null && to != null){
            setHighlightedMove(
                    from.file.ordinal, from.rank.ordinal,
                    to.file.ordinal, to.rank.ordinal
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
    private var _moveToHighlightFrom:Square = null
    private var _moveToHighlightTo:Square = null
    private var _blackStartedTheGame = false
}