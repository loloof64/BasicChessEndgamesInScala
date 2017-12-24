package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import com.loloof64.android.basicchessendgamestrainer.R

object Exercises {

    val availableGenerators = listOf(
        ExerciseInfo(mustWin = true, textId = R.string.exercise_krr_k, constraints = KRRvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kq_k, constraints = KQvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kr_k, constraints =  KRvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kbb_k, constraints = KBBvK),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_kp_k_I, constraints =  KPvK_I),
        ExerciseInfo(mustWin = false, textId = R.string.exercise_kp_k_II, constraints = KPvK_II),
        ExerciseInfo(mustWin = true, textId = R. string.exercise_kppp_kppp, constraints = KPPPvKPPP),
        ExerciseInfo(mustWin = true, textId = R.string.exercise_knb_k, constraints = KNBvK)
    )

    private val KRRvK = positionGenerator {
        computerKing {
                (FileC to FileF).contains(file)
                && (Rank3 to Rank6).contains(rank)
        }

        otherPiecesCount {
            add(Rook belongingTo Player inCount 2)
        }
    }

    private val KQvK = positionGenerator {
        computerKing {
                (FileC to FileF).contains(file)
                && (Rank3 to Rank6).contains(rank)
        }

        otherPiecesCount {
            add(Queen belongingTo Player inCount 1)
        }
    }

    private val KRvK = positionGenerator {
        computerKing {
                (FileC to FileF).contains(file)
                && (Rank3 to Rank6).contains(rank)
        }

        otherPiecesCount {
            add(Rook belongingTo Player inCount 1)
        }
    }

    private val KBBvK = positionGenerator {
        computerKing {
                (FileC to FileF).contains(file)
                && (Rank3 to Rank6).contains(rank)
        }

        otherPiecesCount {
            add(Bishop belongingTo Player inCount 2)
        }

        otherPiecesMutualConstraint {
            set(Bishop belongingTo Player) {
                val firstSquareIsBlack = (firstPieceFile + firstPieceRank) % 2 > 0
                val secondSquareIsBlack = (secondPieceFile + secondPieceRank) % 2 > 0
                firstSquareIsBlack != secondSquareIsBlack
            }
        }
    }

    private val KPvK_I = positionGenerator {
            playerKing {
                    rank == (if (playerHasWhite) Rank6 else Rank3)
                    && (FileB to FileG).contains(file)
            }

            computerKing {
                    rank == if (playerHasWhite) Rank8 else Rank1
            }

            kingsMutualConstraint {
                playerKingFile == computerKingFile
            }

            otherPiecesCount {
                add(Pawn belongingTo Player inCount 1)
            }

            otherPiecesGlobalConstraint {
                set(Pawn belongingTo Player) {
                    (rank == if (playerHasWhite) Rank5 else Rank4)
                    && (file == playerKingFile)
                }
            }
    }

    private val KPvK_II = positionGenerator {
        playerKing {
                rank == if (playerHasWhite) Rank1 else Rank8
        }

        computerKing {
                rank == if (playerHasWhite) Rank4 else Rank5
        }

        kingsMutualConstraint {
            Math.abs(playerKingFile - computerKingFile) <= 1
        }

        otherPiecesCount {
            add(Pawn belongingTo Computer inCount 1)
        }

        otherPiecesGlobalConstraint {
            set(Pawn belongingTo Computer) {
                (rank in if (playerHasWhite) (Rank3 to Rank5) else (Rank4 to Rank6))
                && (file == playerKingFile)
            }
        }
    }

    private val KPPPvKPPP = positionGenerator {
        playerKing {
                rank == (if (playerHasWhite) Rank1 else Rank8)
        }

        computerKing {
                rank == (if (playerHasWhite) Rank1 else Rank8)
        }

        otherPiecesCount {
            add(Pawn belongingTo Player inCount 3)
            add(Pawn belongingTo Computer inCount 3)
        }

        otherPiecesGlobalConstraint {
            set(Pawn belongingTo Player){
                rank == if (playerHasWhite) Rank5 else Rank4
            }
            set(Pawn belongingTo Computer){
                rank == if (playerHasWhite) Rank7 else Rank2
            }
        }

        otherPiecesIndexedConstraint {
            set(Pawn belongingTo Player){
                file == apparitionIndex
            }
            set(Pawn belongingTo Computer){
                file == apparitionIndex
            }
        }
    }

    private val KNBvK = positionGenerator {
        computerKing {
            (FileC to FileF).contains(file)
            && (Rank3 to Rank6).contains(rank)
        }

        otherPiecesCount {
            add(Knight belongingTo Player inCount 1)
            add(Bishop belongingTo Player inCount 1)
        }
    }
}



case class ExerciseInfo(val constraints: PositionConstraints, val textId: Int, val mustWin: Boolean)