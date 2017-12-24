package com.loloof64.android.basicchessendgamestrainer.playing_activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog.Builder
import android.view.{View, ViewGroup}
import android.widget.ImageButton
import com.github.bhlangonijr.chesslib.Piece
import com.loloof64.android.basicchessendgamestrainer.R
import java.lang.ref.WeakReference

object PromotionPieceChooserDialogFragment {
    val TitleKey = "title"
    val WhiteToPlayKey = "whiteToPlay"

    def newInstance(title: String, whiteToPlay: Boolean) : PromotionPieceChooserDialogFragment {
        val dialog = PromotionPieceChooserDialogFragment()
        val args = Bundle()
        args.putString(TitleKey, title)
        args.putBoolean(WhiteToPlayKey, whiteToPlay)
        dialog.arguments = args
        return dialog
    }

    trait Listener {
        def reactToPromotionPieceSelection(piece: Piece)
    }
}

class PromotionPieceChooserDialogFragment extends DialogFragment() {

    private var listener : Listener = null

    private var promotionChooserQueenButton : ImageButton
    private var promotionChooserRookButton : ImageButton
    private var promotionChooserBishopButton : ImageButton
    private var promotionChooserKnightButton : ImageButton

    private var queenPromotionListener: PromotionButtonOnClickListener
    private var rookPromotionListener: PromotionButtonOnClickListener
    private var bishopPromotionListener: PromotionButtonOnClickListener
    private var knightPromotionListener: PromotionButtonOnClickListener

    override def onCreateDialog(savedInstanceState: Bundle): Dialog {
        val title = arguments.getString(PromotionPieceChooserDialogFragment.TitleKey)

        val whiteToPlay = arguments.getBoolean(WhiteToPlayKey)

        val nullParent: ViewGroup = null
        val rootView = activity.layoutInflater.inflate(R.layout.promotion_chooser_dialog, nullParent)

        promotionChooserQueenButton = rootView.findViewById(R.id.promotion_chooser_queen_button).asInstanceOf[ImageButton]
        promotionChooserRookButton = rootView.findViewById(R.id.promotion_chooser_rook_button).asInstanceOf[ImageButton]
        promotionChooserBishopButton = rootView.findViewById(R.id.promotion_chooser_bishop_button).asInstanceOf[ImageButton]
        promotionChooserKnightButton = rootView.findViewById(R.id.promotion_chooser_knight_button).asInstanceOf[ImageButton]

        promotionChooserQueenButton.setImageResource(if (whiteToPlay) R.drawable.chess_ql else R.drawable.chess_qd)
        promotionChooserRookButton.setImageResource(if (whiteToPlay) R.drawable.chess_rl else R.drawable.chess_rd)
        promotionChooserBishopButton.setImageResource(if (whiteToPlay) R.drawable.chess_bl else R.drawable.chess_bd)
        promotionChooserKnightButton.setImageResource(if (whiteToPlay) R.drawable.chess_nl else R.drawable.chess_nd)

        queenPromotionListener = PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN)
        rookPromotionListener = PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_ROOK else Piece.BLACK_ROOK)
        bishopPromotionListener = PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP)
        knightPromotionListener = PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT)

        promotionChooserQueenButton.setOnClickListener(queenPromotionListener)
        promotionChooserRookButton.setOnClickListener(rookPromotionListener)
        promotionChooserBishopButton.setOnClickListener(bishopPromotionListener)
        promotionChooserKnightButton.setOnClickListener(knightPromotionListener)

        val dialog = Builder(activity).setTitle(title).setView(rootView).create()
        queenPromotionListener.setDialog(dialog)
        rookPromotionListener.setDialog(dialog)
        bishopPromotionListener.setDialog(dialog)
        knightPromotionListener.setDialog(dialog)
        return dialog
    }

    override def onAttach(context: Context) {
        super.onAttach(context)
        context match {
            case _:Listener => listener = context
            case _=> throw IllegalArgumentException("Context must use PromotionPieceChooseDialogFragment.Listener trait !")
        }
    }

}

class PromotionButtonOnClickListener(listener: PromotionPieceChooserDialogFragment.Companion.Listener,
                                     val promotionPiece: Piece) : View.OnClickListener {

    override def onClick(relatedView: View) {
        refListener.get().reactToPromotionPieceSelection(promotionPiece)
        refDialog.get().dismiss()
    }

    def setDialog(dialog: Dialog){
        refDialog = WeakReference(dialog)
    }

    private val refListener = WeakReference(listener)
    private var refDialog: WeakReference[Dialog]
}