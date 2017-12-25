package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import com.github.ghik.silencer.silent

object ConstraintsTypes {

    case class BoardLocation(file: Int, rank: Int)

    /**
    * Constraint between both kings


    Parameters are
    <ol>
    <li>playerKingLocation: BoardLocation </li>
    <li>computerKingLocationocation </li>
    <li>playerHasWhite: Boolean</li>
    </ol>
    */
    @silent
    type KingsMutualConstraint = (BoardLocation, BoardLocation, Boolean) => Boolean

    @silent
    /**
    Parameters are
    <ol>
    <li>apparitionIndex: Int</li>
    <li>pieceLocation: BoardLocation</li>
    <li>playerHasWhite: Boolean</li>
    </ol>
    */
    type IndexedConstraint = (Int, BoardLocation, Boolean) => Boolean

    /**
    Individual king global constraint
    Parameters are
    <ol>
    <li>pieceLocation: BoardLocation</li>
    <li>playerHasWhite: Boolean</li>
    </ol>
    */
    @silent
    type SingleKingConstraint = (BoardLocation, Boolean) => Boolean

    /**
    Parameters are
    <ol>
    <li>firstPiece: BoardLocation</li>
    <li>secondPiece: BoardLocation</li>
    <li>playerHasWhite: Boolean</li>
    </ol>
    */
    @silent
    type MutualConstraint = (BoardLocation, BoardLocation, Boolean) => Boolean

    /**
    Parameters are
    <ol>
    <li>pieceLocation: BoardLocation</li>
    <li>playerKingLocation: BoardLocation</li>
    <li>computerKingLocation: BoardLocation</li>
    <li>playerHasWhite: Boolean</li>
    </ol>
    */
    @silent
    type OtherPieceGlobalConstraint = (BoardLocation, BoardLocation, BoardLocation, Boolean) => Boolean

    /**
    Parameters are
    <ol>
    <li>firstPieceLocation: BoardLocation</li>
    <li>secondPieceLocation: BoardLocation</li>
    <li>playerHasWhite: Boolean</li>
    </ol>
    */
    @silent
    type OtherPieceMutualConstraint = (BoardLocation, BoardLocation, Boolean) => Boolean

    /**
    Parameters are
    <ol>
    <li>apparitionIndex: Int</li>
    <li>pieceLocation: BoardLocation</li>
    <li>playerHasWhite: Boolean</li>
    </ol>
    */
    @silent
    type OtherPieceIndexedConstraint = (Int, BoardLocation, Boolean) => Boolean

    class PieceType private(val ordinal: Int) {
        @silent
        def belongingTo(owner: Side) = PieceKind(pieceType = this, side = owner)
    }

    object PieceType {
        val Pawn = new PieceType(0)
        val Knight = new PieceType(1)
        val Bishop = new PieceType(2)
        val Rook = new PieceType(3)
        val Queen = new PieceType(4)
        val King = new PieceType(5)
    }

    class Side private(val ordinal: Int)
    object Side {
        val Player = new Side(0)
        val Computer = new Side(1)
    }

    case class PieceKind(val pieceType: PieceType, val side: Side){
        @silent
        def inCount(instances: Int) = PieceKindCount(pieceKind = this, count = instances)
    }
    case class PieceKindCount(val pieceKind: PieceKind, val count: Int)
}

object ConstraintsConstants {
    import ConstraintsTypes.{PieceType, Side}

    val FileA = 0
    val FileB = 1
    val FileC = 2
    val FileD = 3
    val FileE = 4
    val FileF = 5
    val FileG = 6
    val FileH = 7

    val Rank1 = 0
    val Rank2 = 1
    val Rank3 = 2
    val Rank4 = 3
    val Rank5 = 4
    val Rank6 = 5
    val Rank7 = 6
    val Rank8 = 7

    val Pawn = PieceType.Pawn
    val Knight = PieceType.Knight
    val Bishop = PieceType.Bishop
    val Rook = PieceType.Rook
    val Queen = PieceType.Queen
    val King = PieceType.King

    val Player = Side.Player
    val Computer = Side.Computer
}

import ConstraintsTypes._
@silent
class PositionConstraints (
    val playerKing: SingleKingConstraint = { (_, _) => true },
    val computerKing: SingleKingConstraint = { (_, _) => true },
    /**
    Constraint between both kings
    */
    val kingsMutualConstraint: KingsMutualConstraint = { (_,_,_) => true },
    val otherPiecesCount: Array[PieceKindCount] = Array(),
    /**
    Specific constraint for each piece (kind and side).
    */
    val otherPiecesGlobalConstraint: Map[PieceKind, OtherPieceGlobalConstraint] = Map(),
    /**
    General constraint between 2 pieces of the same kind
    */
    val otherPiecesMutualConstraint: Map[PieceKind, OtherPieceMutualConstraint] = Map(),

    /**
    Constraint between 2 pieces of the same kind, but only relying on the apparition order.
    */
    val otherPiecesIndexedConstraint: Map[PieceKind, OtherPieceIndexedConstraint] = Map()
)