package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

@SuppressWarnings("UNUSED")
def positionGenerator(init: PositionConstraints.() -> Unit) = PositionConstraints().apply(init)

@SuppressWarnings("UNUSED")
class PositionConstraints {
    private var playerKingIndividualConstraintInstance: SingleKingConstraint.() -> Boolean = { true }
    private var computerKingIndividualConstraintInstance: SingleKingConstraint.() -> Boolean = { true }
    private var kingsMutualConstraintInstance: KingsMutualConstraint.() -> Boolean = { true }
    private var otherPiecesCountConstraintInstance = OtherPiecesCountConstraints()
    private var otherPiecesGlobalConstraintInstance = OtherPiecesGlobalConstraint()
    private var otherPiecesMutualConstraintInstance = OtherPiecesMutualConstraint()
    private var otherPiecesIndexedConstraintInstance = OtherPiecesIndexedConstraint()

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

    val Pawn = PieceType.pawn
    val Knight = PieceType.knight
    val Bishop = PieceType.bishop
    val Rook = PieceType.rook
    val Queen = PieceType.queen
    val King = PieceType.king

    val Player = Side.player
    val Computer = Side.computer

    def checkPlayerKingConstraint(file: Int, rank: Int, playerHasWhite: Boolean): Boolean {
        return SingleKingConstraint(file, rank, playerHasWhite).playerKingIndividualConstraintInstance()
    }

    def checkComputerKingConstraint(file: Int, rank: Int, playerHasWhite: Boolean) : Boolean {
        return SingleKingConstraint(file, rank, playerHasWhite).computerKingIndividualConstraintInstance()
    }

    def checkKingsMutualConstraint(playerKingFile: Int, playerKingRank: Int,
                                   computerKingFile: Int, computerKingRank: Int, playerHasWhite: Boolean) : Boolean {
        return KingsMutualConstraint(playerKingFile, playerKingRank, computerKingFile, computerKingRank, playerHasWhite).kingsMutualConstraintInstance()
    }

    def otherPiecesCountsConstraint : Iterator[PieceKindCount] = otherPiecesCountConstraintInstance.iterator()

    def checkOtherPieceGlobalConstraint(pieceKind: PieceKind,
                                        file: Int, rank: Int,
                                        playerHasWhite: Boolean,
                                        playerKingFile : Int, playerKingRank: Int,
                                        computerKingFile: Int, computerKingRank: Int) : Boolean {
        return otherPiecesGlobalConstraintInstance(file, rank, playerHasWhite, playerKingFile, playerKingRank, computerKingFile, computerKingRank).checkConstraint(pieceKind)
    }

    def checkOtherPieceMutualConstraint(pieceKind: PieceKind,
                                        firstPieceFile: Int, firstPieceRank: Int,
                                        secondPieceFile: Int, secondPieceRank: Int,
                                        playerHasWhite: Boolean) : Boolean {
        return otherPiecesMutualConstraintInstance(firstPieceFile, firstPieceRank, secondPieceFile, secondPieceRank, playerHasWhite).checkConstraint(pieceKind)
    }

    def checkOtherPieceIndexedConstraint(pieceKind: PieceKind,
                                         apparitionIndex: Int,
                                         file: Int, rank: Int,
                                         playerHasWhite: Boolean) : Boolean {
        return otherPiecesIndexedConstraintInstance(apparitionIndex, file, rank, playerHasWhite).checkConstraint(pieceKind)
    }

    def playerKing(constraint: SingleKingConstraint.() -> Boolean) {
        playerKingIndividualConstraintInstance = constraint
    }

    def computerKing(constraint: SingleKingConstraint.() -> Boolean) {
        computerKingIndividualConstraintInstance = constraint
    }


    def kingsMutualConstraint(constraint: KingsMutualConstraint.() -> Boolean){
        kingsMutualConstraintInstance = constraint
    }

    def otherPiecesCount(constraint: OtherPiecesCountConstraints.() -> Unit) {
        otherPiecesCountConstraintInstance.constraint()
    }

    def otherPiecesGlobalConstraint(constraint: OtherPiecesGlobalConstraint.() -> Unit) {
        otherPiecesGlobalConstraintInstance.constraint()
    }

    def otherPiecesMutualConstraint(constraint: OtherPiecesMutualConstraint.() -> Unit) {
        otherPiecesMutualConstraintInstance.constraint()
    }

    def otherPiecesIndexedConstraint(constraint: OtherPiecesIndexedConstraint.() -> Unit) {
        otherPiecesIndexedConstraintInstance.constraint()
    }
}


/**
 * Constraint between both kings
 */
@SuppressWarnings("UNUSED")
class KingsMutualConstraint(val playerKingFile: Int, val playerKingRank: Int,
                            val computerKingFile: Int, val computerKingRank: Int,
                            val playerHasWhite: Boolean)

/**
 * Constraint based on the count of several piece kinds (piece type and side : computer or player).
 */
class OtherPiecesCountConstraints {
    import scala.collection.mutable.ArrayBuffer
    private val allConstraints: ArrayBuffer[PieceKindCount] = new ArrayBuffer()

    def add(constraint: PieceKindCount)  {
        allConstraints += constraint
    }

    def iterator() = allConstraints.iterator()
}

/*
 * Constraint based on the piece coordinate, and both kings positions
 */
@SuppressWarnings("UNUSED")
class OtherPiecesGlobalConstraint {
    import scala.collection.mutable.HashMap
    private var allConstraints: HashMap[PieceKind, OtherPiecesGlobalConstraint.() -> Boolean] = new HashMap()

    private var _file:Int = 0
    def file: Int = _file

    private var _rank:Int = 0
    def rank: Int = _rank

    private var _playerHasWhite: Boolean = true
    def playerHasWhite: Boolean = _playerHasWhite

    private var _playerKingFile: Int = 0
    def playerKingFile: Int = _playerKingFile

    var playerKingRank: Int = 0
    def _playerKingRank: Int = _playerKingRank

    private var _computerKingFile: Int = 0
    def computerKingFile : Int = _computerKingFile

    private var _computerKingRank : Int = 0
    def computerKingRank = _computerKingRank

    def apply(file: Int, rank: Int,
                        playerHasWhite: Boolean,
                        playerKingFile: Int, playerKingRank: Int,
                        computerKingFile: Int, computerKingRank: Int) : OtherPiecesGlobalConstraint {
        this.file = file
        this.rank = rank
        this.playerHasWhite = playerHasWhite
        this.playerKingFile = playerKingFile
        this.playerKingRank = playerKingRank
        this.computerKingFile = computerKingFile
        this.computerKingRank = computerKingRank

        return this
    }

    def set(pieceKind: PieceKind, constraint: OtherPiecesGlobalConstraint.() -> Boolean){
        allConstraints.put(pieceKind, constraint)
    }

    def checkConstraint(pieceKind: PieceKind) : Boolean {
        // If no constraint available then condition is considered as met.
        return allConstraints[pieceKind]?.invoke(this) ?: true
    }
}

/*
 * Constraint based on 2 pieces of the same kind.
 */
@SuppressWarnings("UNUSED")
class OtherPiecesMutualConstraint {
    private var allConstraints: MutableMap<PieceKind, OtherPiecesMutualConstraint.() -> Boolean> = mutableMapOf()

    private var _firstPieceFile: Int = 0
    def firstPieceFile: Int = _firstPieceFile

    private var _firstPieceRank: Int = 0
    def firstPieceRank: Int = _firstPieceRank

    private var _secondPieceFile: Int = 0
    def secondPieceFile: Int = _secondPieceFile

    private var _secondPieceRank: Int = 0
    def secondPieceRank: Int = _secondPieceRank

    private var _playerHasWhite: Boolean = true
    def playerHasWhite : Boolean = _playerHasWhite

    def set(pieceKind: PieceKind, constraint: OtherPiecesMutualConstraint.() -> Boolean){
        allConstraints.put(pieceKind, constraint)
    }

    def apply(firstPieceFile: Int, firstPieceRank: Int,
               secondPieceFile: Int, secondPieceRank: Int,
               playerHasWhite: Boolean) : OtherPiecesMutualConstraint {
        this.firstPieceFile = firstPieceFile
        this.firstPieceRank = firstPieceRank
        this.secondPieceFile = secondPieceFile
        this.secondPieceRank = secondPieceRank
        this.playerHasWhite = playerHasWhite
        return this
    }

    def checkConstraint(pieceKind: PieceKind): Boolean {
        // If no constraint available then condition is considered as met.
        return allConstraints[pieceKind]?.invoke(this) ?: true
    }
}
/**
 * Constraint based on the piece kind, its generation index (is it the first, the second, ... ?)
 */
@SuppressWarnings("UNUSED")
class OtherPiecesIndexedConstraint {

    private var allConstraints: MutableMap<PieceKind, OtherPiecesIndexedConstraint.() -> Boolean> = mutableMapOf()

    private var _apparitionIndex: Int = 0
    def apparitionIndex:Int = _apparitionIndex

    private var _file:Int = 0
    def file: Int = _file

    private var _rank:Int = 0
    def rank: Int = _rank

    private var _playerHasWhite: Boolean = true
    def playerHasWhite: Boolean = _playerHasWhite

    def set(pieceKind: PieceKind, constraint: OtherPiecesIndexedConstraint.() -> Boolean){
        allConstraints.put(pieceKind, constraint)
    }

    def apply(apparitionIndex: Int,
                        file: Int, rank: Int,
                        playerHasWhite: Boolean) : OtherPiecesIndexedConstraint {
        this.apparitionIndex = apparitionIndex
        this.file = file
        this.rank = rank
        this.playerHasWhite = playerHasWhite
        return this
    }

    def checkConstraint(pieceKind: PieceKind): Boolean {
        // If no constraint available then condition is considered as met
        return allConstraints[pieceKind]?.invoke(this) ?: true
    }
}


@SuppressWarnings("UNUSED")
class IndexedConstraint(val apparitionIndex: Int,
                        val file: Int, val rank: Int,
                        val playerHasWhite: Boolean)

/*
 Individual king global constraint
 */
case class SingleKingConstraint(val file: Int, val rank: Int,
                                val playerHasWhite: Boolean)

@SuppressWarnings("UNUSED")
class MutualConstraint(val firstPieceFile: Int, firstPieceRank: Int,
                       val secondPieceFile: Int, val secondPieceRank: Int,
                       val playerHasWhite: Boolean)

object PieceType extends Enumeration {
    type PieceType = Value
    pawn, knight, bishop, rook, queen, king = Value

    @SuppressWarnings("UNUSED")
    def belongingTo(owner: Side) = PieceKind(pieceType = this, side = owner)

    @SuppressWarnings("UNUSED")
    def inCount(instances: Int) = PieceKindCount(pieceKind = this, count = instances)
}

object Side extends Enumeration {
    type Side = Value
    player, computer = Value
}

case class PieceKind(val pieceType: PieceType.PieceType, val side: Side.Side)
case class PieceKindCount(val pieceKind: PieceKind.PieceKind, val count: Int)