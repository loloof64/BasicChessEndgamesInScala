package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import com.github.bhlangonijr.chesslib._
import java.util._
import java.util.logging.Logger
import com.github.bhlangonijr.chesslib.{Side => LibSide}

class PositionGenerationLoopException extends Exception()

object BoardUtils {
    def buildSquare(rank: Int, file: Int):Square =
        Square.encode(Rank.values()(rank), File.values()(file))

}

class PositionGenerator(private val constraints : PositionConstraints) {

    import BoardUtils.buildSquare

    private val maxLoopsIterations = 250

    private case class BoardCoordinate(file: Int, rank : Int){
        if (! (0 until 8).contains(file) ) throw new IllegalArgumentException()
        if (! (0 until 8).contains(rank) ) throw new IllegalArgumentException()
    }

    private def generateCell() = BoardCoordinate(
            file = _random.nextInt(8),
            rank = _random.nextInt(8)
    )

    private def buildPositionOrNullIfCellAlreadyOccupied(startFen: String, pieceToAdd: Piece, pieceCell: Square): Board = {
        val builtPosition = new Board()
        builtPosition.loadFromFEN(startFen)

        val wantedCellOccupied = builtPosition.getPiece(pieceCell) != Piece.NONE
        if (wantedCellOccupied) return null

        builtPosition.setPiece(pieceToAdd, pieceCell)
        builtPosition
    }

    private def enemyKingInChessFor(position: Board, playerHasWhite: Boolean) : Boolean = {
        val b = new Board()
        b.loadFromFEN(position.getFEN())
        b.setSideToMove(if (playerHasWhite) LibSide.BLACK else LibSide.WHITE)
        b.isKingAttacked
    }

    def generatePosition(playerHasWhite: Boolean = true): String =  {
        _position.loadFromFEN( s"8/8/8/8/8/8/8/8 ${if (playerHasWhite) 'w' else 'b'} - - 0 1")
        _position.setHalfMoveCounter(0)
        _position.setMoveCounter(1)

        try {
            placeKings(playerHasWhite)
            placeOtherPieces(playerHasWhite)
        } catch {
            case _:PositionGenerationLoopException => return ""
        }

        Logger.getLogger("BasicChessEndgamesTrainer").info(s"Generated position is '${_position.getFEN() }'")

        _position.getFEN()
    }

    private var playerKingCell = BoardCoordinate(file = 0, rank = 0)
    private var oppositeKingCell = BoardCoordinate(file = 0, rank = 0)

    private def placeKings(playerHasWhite: Boolean){

        import ConstraintsTypes._

        var loopSuccess = false
        var iters = 0
        while (!loopSuccess && iters < maxLoopsIterations){ // setting up player king

            val kingCell = generateCell()
            val tempPosition = buildPositionOrNullIfCellAlreadyOccupied(
                    startFen = _position.getFEN(),
                    pieceToAdd = if (playerHasWhite) Piece.WHITE_KING else Piece.BLACK_KING,
                    pieceCell = buildSquare(rank = kingCell.rank, file = kingCell.file)
            )
            if (tempPosition != null) {

                if (constraints.playerKing(
                        BoardLocation(kingCell.file, kingCell.rank),
                        playerHasWhite)) {
                    _position.loadFromFEN(tempPosition.getFEN)
                    playerKingCell = kingCell
                    loopSuccess = true
                }

            }
            iters += 1
        }
        if (!loopSuccess) throw new PositionGenerationLoopException()

        loopSuccess = false
        iters = 0
        while (!loopSuccess && iters < maxLoopsIterations){  // setting up enemy king

            val kingCell = generateCell()

            val tempPosition = buildPositionOrNullIfCellAlreadyOccupied(
                    startFen = _position.getFEN(),
                    pieceToAdd = if (playerHasWhite) Piece.BLACK_KING else Piece.WHITE_KING,
                    pieceCell = buildSquare(
                            rank = kingCell.rank, file = kingCell.file
                    )
            )

            if (tempPosition != null && !enemyKingInChessFor(tempPosition, playerHasWhite)) {

                // validate position if enemy king constraint and kings mutual constraint are respected
                if (constraints.computerKing(BoardLocation(kingCell.file, kingCell.rank), playerHasWhite)
                        && constraints.kingsMutualConstraint(
                        BoardLocation(playerKingCell.file, playerKingCell.rank),
                        BoardLocation(kingCell.file, kingCell.rank),
                        playerHasWhite
                )) {
                    oppositeKingCell = kingCell
                    _position.loadFromFEN(tempPosition.getFEN)
                    loopSuccess = true
                }

            }

            iters += 1
        }
        if (!loopSuccess) throw new PositionGenerationLoopException()
    }

    private def placeOtherPieces(playerHasWhite: Boolean){
        import ConstraintsTypes._

        def pieceKindToPiece(kind: PieceKind, whitePiece: Boolean): Piece =
        kind.pieceType match {
            case PieceType.Pawn => if (whitePiece) Piece.WHITE_PAWN else Piece.BLACK_PAWN
            case PieceType.Knight => if (whitePiece) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
            case PieceType.Bishop => if (whitePiece) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
            case PieceType.Rook => if (whitePiece) Piece.WHITE_ROOK else Piece.BLACK_ROOK
            case PieceType.Queen => if (whitePiece) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
            case PieceType.King => if (whitePiece) Piece.WHITE_KING else Piece.BLACK_KING
        }

        constraints.otherPiecesCount.foreach { case PieceKindCount(kind, count) =>

            val savedCoordinates = scala.collection.mutable.ArrayBuffer[BoardCoordinate]()
            (0 until count).foreach { index =>
                var loopSuccess = false
                var loopIter = 0
                while (!loopSuccess && loopIter < maxLoopsIterations) {
                    val isAPieceOfPlayer = kind.side == Side.Player
                    val isWhitePiece = (isAPieceOfPlayer && playerHasWhite) ||
                     (!isAPieceOfPlayer && !playerHasWhite)

                    val pieceCell = generateCell()
                    val tempPosition = buildPositionOrNullIfCellAlreadyOccupied(
                            startFen = _position.getFEN(),
                            pieceToAdd = pieceKindToPiece(kind, isWhitePiece),
                            pieceCell = buildSquare(
                                    rank = pieceCell.rank, file = pieceCell.file
                            )
                    )

                    if (tempPosition != null && !enemyKingInChessFor(tempPosition, playerHasWhite)) {

                        // If for any previous piece of same kind, mutual constraint is not respected, will loop another time
                        if (savedCoordinates.forall { current => !constraints.otherPieceMutualConstraint.keySet.contains(kind) ||
                            constraints.otherPieceMutualConstraint(kind)(
                                BoardLocation(current.file, current.rank),
                                BoardLocation(pieceCell.file, pieceCell.rank),
                                playerHasWhite) }) {

                            if (!constraints.otherPieceIndexedConstraint.keySet.contains(kind) ||
                                    constraints.otherPieceIndexedConstraint(kind)(index,
                                    BoardLocation(pieceCell.file, pieceCell.rank),
                                    playerHasWhite)) {

                                if (!constraints.otherPieceGlobalConstraint.keySet.contains(kind) ||
                                    constraints.otherPieceGlobalConstraint(kind)(
                                        BoardLocation(pieceCell.file, pieceCell.rank),
                                        BoardLocation(playerKingCell.file, playerKingCell.rank),
                                        BoardLocation(oppositeKingCell.file, oppositeKingCell.rank),
                                        playerHasWhite)){
                                    loopSuccess = true
                                    _position.loadFromFEN(tempPosition.getFEN)
                                    savedCoordinates += pieceCell
                                }

                            }

                        }

                    }
                    loopIter += 1
                }
                if (!loopSuccess) throw new PositionGenerationLoopException()
            }
        }
    }

    private val _position = new Board()
    private val _random = new Random()
}

