package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import ConstraintsTypes._

object ExerciseInterpreter {
  def build(positionConstraintsStr: String):PositionConstraints = {
    val interpreter = new ExerciseInterpreter()
    new PositionConstraints(
      playerKing = if (interpreter.playerKingConstraint.isDefined)
        interpreter.playerKingConstraint.get  else { (_, _) => true },
      computerKing = if (interpreter.computerKingConstraint.isDefined)
        interpreter.computerKingConstraint.get else { (_, _) => true },
      kingsMutualConstraint = if (interpreter.kingsMutualConstraint.isDefined)
        interpreter.kingsMutualConstraint.get else { (_,_,_) => true },
      otherPiecesCount = interpreter.otherPiecesCount,
      otherPieceGlobalConstraint = interpreter.otherPieceGlobalConstraint,
      otherPieceIndexedConstraint = interpreter.otherPieceIndexedConstraint,
      otherPieceMutualConstraint = interpreter.otherPieceMutualConstraint
    )
  }
}

class ExerciseInterpreter private (){

    import scala.meta._
    import ConstraintsTypes._
    import ConstraintsConstants._

    private var playerKingConstraint: Option[SingleKingConstraint] = None
    private var computerKingConstraint: Option[SingleKingConstraint] = None
    private var kingsMutualConstraint: Option[KingsMutualConstraint] = None

    private var otherPiecesCount: Array[PieceKindCount] = Array()
    private var otherPieceIndexedConstraint : Map[PieceKind, OtherPieceIndexedConstraint] = Map()
    private var otherPieceGlobalConstraint: Map[PieceKind, OtherPieceGlobalConstraint] = Map()
    private var otherPieceMutualConstraint: Map[PieceKind, OtherPieceMutualConstraint] = Map()

    def setPlayerKingConstraint(constraintStr: String): Option[SingleKingConstraint] = {
        val scalaMathAbsImport = if (constraintStr.contains("abs(")) "import scala.math.abs\n" else ""
        val completConstraintStr =
          s"""(location, playerHasWhite) => {
            |import ConstraintsConstants._
            |$scalaMathAbsImport
            |$constraintStr
            |}
          """.stripMargin
      ////////////////////////////////////
      //println(completConstraintStr)
      /////////////////////////////////////
      val resultConstraint = completConstraintStr.parse[Term].get
      //////////////////////////////////////
      // println(resultConstraint)
      //////////////////////////////////////
      if (constraintStr.isEmpty) None else Some(resultConstraint.asInstanceOf[SingleKingConstraint])
    }

}