package com.loloof64.android.basicchessendgamestrainer.playing_activity

import android.content.Context
import android.graphics.{Canvas, Paint, Rect, Typeface}
import android.support.graphics.drawable.VectorDrawableCompat
import android.util.AttributeSet
import android.view.View
import com.github.bhlangonijr.chesslib.{Board, Piece, Side}
import com.loloof64.android.basicchessendgamestrainer.R
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.BoardUtils.buildSquare

object IntUtils {
    def min(a: Int, b: Int) = if (a < b) a else b
    def max(a: Int, b: Int) = if (a > b) a else b
}

case class SquareCoordinates(val file: Int, val rank: Int)

abstract class BoardComponent(context: Context, attrs: AttributeSet, defStyleAttr: Int) extends View(context, attrs, defStyleAttr) {

    @SuppressWarnings("DEPRECATION")
    def getColor(colorResId: Int): Int = resources.getColor(colorResId)

    def this(context: Context, attrs: AttributeSet) { this(context, attrs, 0) }
    def this(context: Context) { this(context, null, 0) }

    private val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BoardComponent)
    private val computed = typedArray.getInt(R.styleable.BoardComponent_min_dimension_percentage, 100)
    typedArray.recycle()
    private val minAvailableSpacePercentage = computed

    def computeMinAvailableSpacePercentage():Int = minAvailableSpacePercentage

    protected abstract def relatedPosition() : Board
    protected abstract def replacePositionWith(positionFEN : String)

    protected var reversed = false
    private val rectPaint = new Paint()
    private val fontPaint = new Paint()

    abstract def highlightedStartCell() : SquareCoordinates
    abstract def highlightedTargetCell() : SquareCoordinates

    def reverse() {
        reversed = !reversed
        invalidate()
    }

    def areBlackDown():Boolean = reversed

    def setBlackDown(yes: Boolean) {
        reversed = yes
    }

    override def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int){
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val minSpacePercentage = computeMinAvailableSpacePercentage()

        val widthAdjusted = (widthSize * minSpacePercentage / 100).max(suggestedMinimumWidth)
        val heightAdjusted = (heightSize * minSpacePercentage / 100).max(suggestedMinimumHeight)

        val desiredWidth = widthAdjusted - (widthAdjusted % 9)
        val desiredHeight = heightAdjusted - (heightAdjusted % 9)

        val computedSize = desiredWidth.min(desiredHeight)
        setMeasuredDimension(computedSize, computedSize)
    }

    private def drawBackground(canvas: Canvas) {
        rectPaint.color = getColor(R.color.chess_board_background_color)
        canvas.drawRect(0.toFloat, 0.toFloat, measuredWidth.toFloat, measuredHeight.toFloat, rectPaint)
    }

    private def drawCells(canvas: Canvas, cellSize: Int) {

        for (row <- 0 until 8) {
            for (col <- 0 until 8) {
                val color = if ((row + col) % 2 == 0) R.color.chess_board_white_cells_color else R.color.chess_board_black_cells_color
                val x = (cellSize / 2 + col * cellSize).toFloat
                val y = (cellSize / 2 + row * cellSize).toFloat
                rectPaint.color = getColor(color)
                canvas.drawRect(x, y, x + cellSize, y + cellSize, rectPaint)
            }
        }

    }

    private def drawLetters(canvas: Canvas, cellSize: Int) {
        val fileCoords = if (reversed) "HGFEDCBA" else "ABCDEFGH"
        val rankCoords = if (reversed) "12345678" else "87654321"

        for ((file, letter) <- fileCoords.zipWithIndex) {
            val y1 = (cellSize * 0.4).toFloat
            val y2 = (cellSize * 8.9).toFloat
            val x = (cellSize * 0.9 + file * cellSize).toFloat

            fontPaint.color = getColor(R.color.chess_board_font_color)

            canvas.drawText(s"$letter", x, y1, fontPaint)
            canvas.drawText(s"$letter", x, y2, fontPaint)
        }

        for ((rank, digit) <- rankCoords.zipWithIndex) {
            val x1 = (cellSize * 0.15).toFloat
            val x2 = (cellSize * 8.65).toFloat
            val y = (cellSize * 1.2 + rank * cellSize).toFloat

            fontPaint.color = getColor(R.color.chess_board_font_color)

            canvas.drawText(s"$digit", x1, y, fontPaint)
            canvas.drawText(s"$digit", x2, y, fontPaint)
        }
    }

    private def drawPieces(canvas: Canvas, cellSize: Int) {
        for (cellRank <- (0 until 8)) {
            for (cellFile <- (0 until 8)) {
                val piece = relatedPosition().getPiece(buildSquare(cellRank, cellFile))
                if (piece != Piece.NONE) {
                    val imageRes = piece match {
                        case Piece.WHITE_PAWN => R.drawable.chess_pl
                        case Piece.BLACK_PAWN => R.drawable.chess_pd
                        case Piece.WHITE_KNIGHT => R.drawable.chess_nl
                        case Piece.BLACK_KNIGHT => R.drawable.chess_nd
                        case Piece.WHITE_BISHOP => R.drawable.chess_bl
                        case Piece.BLACK_BISHOP => R.drawable.chess_bd
                        case Piece.WHITE_ROOK => R.drawable.chess_rl
                        case Piece.BLACK_ROOK => R.drawable.chess_rd
                        case Piece.WHITE_QUEEN => R.drawable.chess_ql
                        case Piece.BLACK_QUEEN => R.drawable.chess_qd
                        case Piece.WHITE_KING => R.drawable.chess_kl
                        case Piece.BLACK_KING => R.drawable.chess_kd
                        case _ => throw IllegalArgumentException(s"Unrecognized piece fen $piece !")
                    }
                    val x = (cellSize * (0.5 + (if (reversed) 7 - cellFile else cellFile))).toInt
                    val y = (cellSize * (0.5 + (if (reversed) cellRank else 7 - cellRank))).toInt

                    val drawable = VectorDrawableCompat.create(context.resources, imageRes, null)
                    drawable.bounds = Rect(x, y, x + cellSize, y + cellSize)
                    drawable.draw(canvas)
                }
            }
        }
    }

    private def drawPlayerTurn(canvas: Canvas, cellSize: Int) {
        val color = if (relatedPosition().sideToMove == Side.WHITE) R.color.chess_board_white_player_turn_color else R.color.chess_board_black_player_turn_color
        val location = (8.5 * cellSize).toFloat
        val locationEnd = (location + cellSize * 0.5).toFloat
        rectPaint.color = getColor(color)
        canvas.drawRect(location, location, locationEnd, locationEnd, rectPaint)
    }

    private def drawHighlightedCells(canvas: Canvas, cellSize: Int) {
        val startCellToHighlight = highlightedStartCell()
        if (startCellToHighlight != null){
            val fileIndex = startCellToHighlight.file
            val rankIndex = startCellToHighlight.rank

            val x = (cellSize * (0.5 + (if (reversed) 7 - fileIndex else fileIndex))).toFloat
            val y = (cellSize * (0.5 + (if (reversed) rankIndex else 7 - rankIndex))).toFloat
            rectPaint.color = getColor(R.color.chess_board_move_start_cell_highlighting)
            canvas.drawRect(x, y, x + cellSize, y + cellSize, rectPaint)
        }

        val targetCellToHighlight = highlightedTargetCell()
        if (targetCellToHighlight != null) {
            val fileIndex = targetCellToHighlight.file
            val rankIndex = targetCellToHighlight.rank

            val x = (cellSize * (0.5 + (if (reversed) 7 - fileIndex else fileIndex))).toFloat
            val y = (cellSize * (0.5 + (if (reversed) rankIndex else 7 - rankIndex))).toFloat
            rectPaint.color = getColor(R.color.chess_board_move_current_cell_highlighting)
            canvas.drawRect(x, y, x + cellSize, y + cellSize, rectPaint)
        }
    }

    private def drawCurrentTargetCellGuidingAxis(canvas: Canvas, cellSize: Int){
        val targetCellToHighlight = highlightedTargetCell()
        if (targetCellToHighlight != null) {
            val fileIndex = targetCellToHighlight.file
            val rankIndex = targetCellToHighlight.rank

            val x = (cellSize * (1 + (if (reversed) 7 - fileIndex else fileIndex))).toFloat
            val y = (cellSize * (1 + (if (reversed) rankIndex else 7 - rankIndex))).toFloat
            rectPaint.color = getColor(R.color.chess_board_move_current_cell_highlighting)
            rectPaint.strokeWidth = cellSize * 0.1f

            canvas.drawLine(0f, y, width.toFloat, y, rectPaint)
            canvas.drawLine(x, 0f, x, height.toFloat, rectPaint)
        }
    }

    private def drawHighlightedMove(canvas: Canvas, cellSize: Int){
        val from = _highlightedMoveFrom
        val to = _highlightedMoveTo
        if (from == null || to == null) return
        if (! ((0 to 7) contains from.file) ) return
        if (! ((0 to 7) contains from.rank) ) return
        if (! ((0 to 7) contains to.file) ) return
        if (! ((0 to 7) contains to.rank) ) return

        val paint = new Paint()

        val fromPointX = (cellSize * (if (reversed) (8 - from.file) else (from.file+1))).toFloat
        val fromPointY = (cellSize * (if (reversed) (from.rank+1) else (8 - from.rank))).toFloat
        val toPointX = (cellSize * (if (reversed) (8 - to.file) else (to.file+1))).toFloat
        val toPointY = (cellSize * (if (reversed) (to.rank+1) else (8 - to.rank))).toFloat

        val angleDegrees = Math.toDegrees(Math.atan2(toPointY.toDouble - fromPointY.toDouble,
                toPointX.toDouble - fromPointX.toDouble)).toFloat

        val distance = Math.sqrt(Math.pow((toPointX - fromPointX).toDouble, 2.0) +
                Math.pow((toPointY - fromPointY).toDouble, 2.0)).toFloat

        val arrowLength = distance * 0.15f

        paint.color = getColor(R.color.chess_board_highlighted_move_arrow_color)
        paint.strokeWidth = cellSize * 0.1f

        canvas.save()
        canvas.translate(fromPointX, fromPointY)
        canvas.rotate(angleDegrees)
        canvas.drawLine(0f, 0f, distance, 0f, paint)
        canvas.translate(distance, 0f)
        canvas.rotate(180f)
        canvas.save()
        canvas.drawLine(0f, 0f, arrowLength, arrowLength, paint)
        canvas.restore()
        canvas.drawLine(0f, 0f, arrowLength, -arrowLength, paint)
        canvas.restore()
    }


    override def onDraw(canvas: Canvas) {
        val cellSize = measuredWidth.min(measuredHeight) / 9
        fontPaint.textSize = (cellSize * 0.4).toFloat
        fontPaint.typeface = Typeface.defaultFromStyle(Typeface.BOLD)

        drawBackground(canvas)
        drawCells(canvas, cellSize)
        drawLetters(canvas, cellSize)
        if (highlightedTargetCell() != null) drawHighlightedCells(canvas, cellSize)
        drawPieces(canvas, cellSize)
        drawPlayerTurn(canvas, cellSize)
        drawHighlightedMove(canvas, cellSize)
        drawCurrentTargetCellGuidingAxis(canvas, cellSize)
    }

    def setHighlightedMove(fromFile: Int, fromRank: Int,
                           toFile: Int, toRank: Int){
        _highlightedMoveFrom = new SquareCoordinates(file =  if ((0 to 7).contains(fromFile)) fromFile else -1,
                rank = if ((0 to 7).contains(fromRank)) fromRank else -1)
        _highlightedMoveTo = new SquareCoordinates(file = if ((0 to 7).contains(toFile)) toFile else -1,
                rank = if ((0 to 7).contains(toRank)) toRank else -1)
        invalidate()
    }

    def clearHighlightedMove(){
        _highlightedMoveFrom = null
        _highlightedMoveTo = null
        invalidate()
    }

    def toFEN(): String = relatedPosition().fen

    def setFromFen(boardFen: String) {
        replacePositionWith(boardFen)
        invalidate()
    }

    private var _highlightedMoveFrom:SquareCoordinates = null
    private var _highlightedMoveTo:SquareCoordinates = null


}