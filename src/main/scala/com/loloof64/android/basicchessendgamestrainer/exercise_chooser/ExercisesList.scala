package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import com.loloof64.android.basicchessendgamestrainer.R

object Exercises {

    import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.ConstraintsConstants._

    val availableGenerators = Array(
        ExerciseInfo(mustWin = true, textId = R.string.exercise_krr_k, constraints = KRRvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kq_k, constraints = KQvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kr_k, constraints =  KRvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kbb_k, constraints = KBBvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kp_k_I, constraints =  KPvK_I),
        ExerciseInfo(mustWin = false, textId = R.string.exercise_kp_k_II, constraints = KPvK_II),
        ExerciseInfo(mustWin = true, textId = R. string.exercise_kppp_kppp, constraints = KPPPvKPPP),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_knb_k, constraints = KNBvK)
    )

    private val KRRvK = new PositionConstraints(
        computerKing = {(location, playerHasWhite) =>
            (FileC to FileF).contains(location.file) && 
            (Rank3 to Rank6).contains(location.rank)
        },

        otherPiecesCount = Array(
            Rook belongingTo Player inCount 2
        )
    )

    private val KQvK = new PositionConstraints(
        computerKing = {(location, playerHasWhite) =>
                (FileC to FileF).contains(location.file) && 
                (Rank3 to Rank6).contains(location.rank)
        },

        otherPiecesCount = Array(
            Queen belongingTo Player inCount 1
        )
    )

    private val KRvK = new PositionConstraints(
        computerKing = {(location, playerHasWhite) =>
                (FileC to FileF).contains(location.file) &&
                (Rank3 to Rank6).contains(location.rank)
        },

        otherPiecesCount = Array(
            Rook belongingTo Player inCount 1
        )
    )

    private val KBBvK = new PositionConstraints(
        computerKing = {(location, playerHasWhite) =>
                (FileC to FileF).contains(location.file) &&
                (Rank3 to Rank6).contains(location.rank)
        },

        otherPiecesCount = Array(
            Bishop belongingTo Player inCount 2
        ),

        otherPiecesMutualConstraint = Map(
            (Bishop belongingTo Player) -> {(firstPieceLocation, secondPieceLocation, playerHasWhite) =>
                val firstSquareIsBlack = (firstPieceLocation.file + firstPieceLocation.rank) % 2 > 0
                val secondSquareIsBlack = (secondPieceLocation.file + secondPieceLocation.rank) % 2 > 0
                firstSquareIsBlack != secondSquareIsBlack
            }
        )
    )

    private val KPvK_I = new PositionConstraints(
        playerKing = {(location, playerHasWhite) =>
            rank == (if (playerHasWhite) Rank6 else Rank3) &&
            (FileB to FileG).contains(location.file)
        },

        computerKing = {(location, playerHasWhite) =>
            rank == (if (playerHasWhite) Rank8 else Rank1)
        },

        kingsMutualConstraint = {
            playerKingFile == computerKingFile
        },

        otherPiecesCount = Array(
            Pawn belongingTo Player inCount 1
        ),

        otherPiecesGlobalConstraint = Map (
            (Pawn belongingTo Player) -> {
                (rank == (if (playerHasWhite) Rank5 else Rank4)) &&
                (file == playerKingFile)
            }
        )
    )

    private val KPvK_II = new PositionConstraints(
        playerKing = {(location, playerHasWhite) =>
                rank == (if (playerHasWhite) Rank1 else Rank8)
        },

        computerKing = {(location, playerHasWhite) =>
                rank == (if (playerHasWhite) Rank4 else Rank5)
        },

        kingsMutualConstraint = {
            Math.abs(playerKingFile - computerKingFile) <= 1
        },

        otherPiecesCount = Array(
            add(Pawn belongingTo Computer inCount 1)
        ),

        otherPiecesGlobalConstraint = Map(
            (Pawn belongingTo Computer) -> {
                (if (playerHasWhite) (Rank3 to Rank5) else (Rank4 to Rank6)).contains(location.rank) &&
                (file == playerKingFile)
            }
        )
    )

    private val KPPPvKPPP = new PositionConstraints(
        playerKing = {
            rank == (if (playerHasWhite) Rank1 else Rank8)
        },

        computerKing = {
            rank == (if (playerHasWhite) Rank1 else Rank8)
        },

        otherPiecesCount = Array(
            Pawn belongingTo Player inCount 3,
            Pawn belongingTo Computer inCount 3
        ),

        otherPiecesGlobalConstraint = Map (
            (Pawn belongingTo Player) -> {
                rank == (if (playerHasWhite) Rank5 else Rank4)
            },
            (Pawn belongingTo Computer) -> {
                rank == (if (playerHasWhite) Rank7 else Rank2)
            }
        ),

        otherPiecesIndexedConstraint = Map (
            (Pawn belongingTo Player) -> {
                file == apparitionIndex
            },
            (Pawn belongingTo Computer) -> {
                file == apparitionIndex
            }
        )
    )

    private val KNBvK = new PositionConstraints(
        computerKing = {
            (FileC to FileF).contains(location.file) &&
            (Rank3 to Rank6).contains(location.rank)
        },

        otherPiecesCount = Array(
            Knight belongingTo Player inCount 1,
            Bishop belongingTo Player inCount 1
        )
    )
}



case class ExerciseInfo(val constraints: PositionConstraints, val textId: Int, val mustWin: Boolean)