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

    def newInstance(title: String, whiteToPlay: Boolean) : PromotionPieceChooserDialogFragment = {
        val dialog = new PromotionPieceChooserDialogFragment()
        val args = new Bundle()
        args.putString(TitleKey, title)
        args.putBoolean(WhiteToPlayKey, whiteToPlay)
        dialog.setArguments(args)
        dialog
    }

    trait Listener {
        def reactToPromotionPieceSelection(piece: Piece)
    }
}

class PromotionPieceChooserDialogFragment extends DialogFragment() {

    import PromotionPieceChooserDialogFragment._

    private var listener : Listener = _

    private var promotionChooserQueenButton : ImageButton = _
    private var promotionChooserRookButton : ImageButton = _
    private var promotionChooserBishopButton : ImageButton = _
    private var promotionChooserKnightButton : ImageButton = _

    private var queenPromotionListener: PromotionButtonOnClickListener = _
    private var rookPromotionListener: PromotionButtonOnClickListener = _
    private var bishopPromotionListener: PromotionButtonOnClickListener = _
    private var knightPromotionListener: PromotionButtonOnClickListener = _

    override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
        val title = getArguments.getString(PromotionPieceChooserDialogFragment.TitleKey)

        val whiteToPlay = getArguments.getBoolean(WhiteToPlayKey)

        val nullParent: ViewGroup = null
        val rootView = getActivity.getLayoutInflater.inflate(R.layout.promotion_chooser_dialog, nullParent)

        promotionChooserQueenButton = rootView.findViewById(R.id.promotion_chooser_queen_button).asInstanceOf[ImageButton]
        promotionChooserRookButton = rootView.findViewById(R.id.promotion_chooser_rook_button).asInstanceOf[ImageButton]
        promotionChooserBishopButton = rootView.findViewById(R.id.promotion_chooser_bishop_button).asInstanceOf[ImageButton]
        promotionChooserKnightButton = rootView.findViewById(R.id.promotion_chooser_knight_button).asInstanceOf[ImageButton]

        promotionChooserQueenButton.setImageResource(if (whiteToPlay) R.drawable.chess_ql else R.drawable.chess_qd)
        promotionChooserRookButton.setImageResource(if (whiteToPlay) R.drawable.chess_rl else R.drawable.chess_rd)
        promotionChooserBishopButton.setImageResource(if (whiteToPlay) R.drawable.chess_bl else R.drawable.chess_bd)
        promotionChooserKnightButton.setImageResource(if (whiteToPlay) R.drawable.chess_nl else R.drawable.chess_nd)

        queenPromotionListener = new PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN)
        rookPromotionListener = new PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_ROOK else Piece.BLACK_ROOK)
        bishopPromotionListener = new PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP)
        knightPromotionListener = new PromotionButtonOnClickListener(listener.asInstanceOf[Listener],
                if (whiteToPlay) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT)

        promotionChooserQueenButton.setOnClickListener(queenPromotionListener)
        promotionChooserRookButton.setOnClickListener(rookPromotionListener)
        promotionChooserBishopButton.setOnClickListener(bishopPromotionListener)
        promotionChooserKnightButton.setOnClickListener(knightPromotionListener)

        val dialog = new Builder(getActivity).setTitle(title).setView(rootView).create()
        queenPromotionListener.setDialog(dialog)
        rookPromotionListener.setDialog(dialog)
        bishopPromotionListener.setDialog(dialog)
        knightPromotionListener.setDialog(dialog)
        dialog
    }

    override def onAttach(context: Context) {
        super.onAttach(context)
        context match {
            case l:Listener => listener = l
            case _=> throw new IllegalArgumentException("Context must use PromotionPieceChooseDialogFragment.Listener trait !")
        }
    }

}

class PromotionButtonOnClickListener(listener: PromotionPieceChooserDialogFragment.Listener,
                                     val promotionPiece: Piece) extends View.OnClickListener {

    override def onClick(relatedView: View) {
        refListener.get().reactToPromotionPieceSelection(promotionPiece)
        refDialog.get().dismiss()
    }

    def setDialog(dialog: Dialog){
        refDialog = new WeakReference(dialog)
    }

    private val refListener = new WeakReference(listener)
    private var refDialog: WeakReference[Dialog] = _
}