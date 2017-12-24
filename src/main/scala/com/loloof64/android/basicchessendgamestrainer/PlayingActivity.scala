package com.loloof64.android.basicchessendgamestrainer

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.github.bhlangonijr.chesslib.Piece
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.PositionGenerator
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.availableGenerators
import com.loloof64.android.basicchessendgamestrainer.playing_activity._
import kotlinx.android.synthetic.main.activity_playing._
import java.lang.ref.WeakReference
import java.util._

class SpaceLeftAndRightItemDecorator(private val space: Int) extends RecyclerView.ItemDecoration(){
    override def getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = space
        outRect.right = space
    }
}

object PlayingActivity {
    val currentPositionkey = "CurrentPosition"
    val playerHasWhiteKey = "PlayerHasWhite"
    val gameFinishedKey = "GameFinished"
    val lastExerciseKey = "LastExercise"
    val playerGoalIDKey = "PlayerGoalID"
    val playerGoalInAlertModeKey = "PlayerGoalInAlertMode"
    val waitingForPlayerGoalKey = "WaitingForPlayerGoal"
    val generatorIndexKey = "GeneratorIndex"
    val adapterSanItemsKey = "AdapterSanItems"
    val adapterFenItemsKey = "AdapterFenItems"
    val startedToWriteMovesKey = "StartedToWriteMoves"
    val moveToHighlightFromFileKey = "MoveToHighlightFromFile"
    val moveToHighlightFromRankKey = "MoveToHighlightFromRank"
    val moveToHighlightToFileKey = "MoveToHighlightToFile"
    val moveToHighlightToRankKey = "MoveToHighlightToRank"
    val switchingPositionAllowedKey = "SwitchingPositionAllowed"
    val registedHighlitedMovesStartFilesKey = "RegistedHighlitedMovesStartFiles"
    val registedHighlitedMovesStartRanksKey = "RegistedHighlitedMovesStartRanks"
    val registedHighlitedMovesEndFilesKey = "RegistedHighlitedMovesEndFiles"
    val registedHighlitedMovesEndRanksKey = "RegistedHighlitedMovesEndRanks"
    val selectedNavigationItemKey = "SelectedNavigationItem"
    val blacksAreDownKey = "BlacksAreDown"

    val standardFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
}

class PlayingActivity extends AppCompatActivity() with PromotionPieceChooserDialogFragment.Companion.Listener {

    @SuppressWarnings("DEPRECATION")
    private def findColor(colorResId: Int): Int = resources.getColor(colorResId)

    override def reactToPromotionPieceSelection(piece: Piece) {
        playingBoard.validatePromotionMove(piece)
        playingBoard.checkIfGameFinished()
        if (!playingBoard.gameFinished()) playingBoard.makeComputerPlay()
    }

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playing)

        val gridLayoutColumns = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 6 else 6
        val gridLayoutManager = GridLayoutManager(this, gridLayoutColumns)
        moves_list_view.layoutManager = gridLayoutManager
        moves_list_view.adapter = listAdapter
        val spaceDp = 5.0f
        val space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spaceDp, resources.displayMetrics)
        moves_list_view.addItemDecoration(SpaceLeftAndRightItemDecorator(space.toInt()))

        playing_board_history_back.setOnClickListener { listAdapter.goBackInHistory() }
        playing_board_history_forward.setOnClickListener { listAdapter.goForwardInHistory() }

        fab_restart_exercise.setOnClickListener { restartLastExercise() }
        fab_reverse_board.setOnClickListener { reverseBoard() }
        fab_new_exercise.setOnClickListener { newExercise() }

        EngineInteraction.initStockfishProcessIfNotDoneYet()

        generatorIndex = intent.extras.getInt(generatorIndexKey)
        val generatedPosition = PositionGenerator(availableGenerators[generatorIndex].constraints).generatePosition(random.nextBoolean())
        if (generatedPosition.isNotEmpty()) {
            newGame(generatedPosition)
        }
        else {
            Toast.makeText(this, R.string.position_generation_error, Toast.LENGTH_LONG).show()
        }
    }

    override def onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(currentPositionkey, playingBoard.toFEN())
        outState.putBoolean(playerHasWhiteKey, playingBoard.playerHasWhite())
        outState.putBoolean(gameFinishedKey, playingBoard.gameFinished())
        outState.putString(lastExerciseKey, lastExercise)
        outState.putInt(playerGoalIDKey, playerGoalTextId)
        outState.putBoolean(playerGoalInAlertModeKey, playerGoalInAlertMode)
        outState.putBoolean(waitingForPlayerGoalKey, playingBoard.isWaitingForPlayerGoal())
        outState.putStringArray(adapterSanItemsKey, listAdapter.items.map { it.san }.toTypedArray())
        outState.putStringArray(adapterFenItemsKey, listAdapter.items.map { it.relatedFen }.toTypedArray())
        outState.putBoolean(startedToWriteMovesKey, playingBoard.hasStartedToWriteMoves())
        outState.putInt(moveToHighlightFromFileKey, playingBoard.getMoveToHighlightFromFile())
        outState.putInt(moveToHighlightFromRankKey, playingBoard.getMoveToHighlightFromRank())
        outState.putInt(moveToHighlightToFileKey, playingBoard.getMoveToHighlightToFile())
        outState.putInt(moveToHighlightToRankKey, playingBoard.getMoveToHighlightToRank())
        outState.putBoolean(switchingPositionAllowedKey, listAdapter.switchingPosition)
        outState.putIntArray(registedHighlitedMovesStartFilesKey,
                listAdapter.items.map { it.moveToHighlight.startFile }.toIntArray())
        outState.putIntArray(registedHighlitedMovesStartRanksKey,
                listAdapter.items.map { it.moveToHighlight.startRank }.toIntArray())
        outState.putIntArray(registedHighlitedMovesEndFilesKey,
                listAdapter.items.map { it.moveToHighlight.endFile }.toIntArray())
        outState.putIntArray(registedHighlitedMovesEndRanksKey,
                listAdapter.items.map { it.moveToHighlight.endRank }.toIntArray())
        outState.putInt(selectedNavigationItemKey, listAdapter.selectedNavigationItem)
        outState.putBoolean(blacksAreDownKey, playingBoard.areBlackDown())
    }

    override def onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            playingBoard.reloadPosition(fen = savedInstanceState.getString(currentPositionkey),
                    playerHasWhite = savedInstanceState.getBoolean(playerHasWhiteKey),
                    gameFinished = savedInstanceState.getBoolean(gameFinishedKey),
                    waitingForPlayerGoal = savedInstanceState.getBoolean(waitingForPlayerGoalKey),
                    hasStartedToWriteMoves = savedInstanceState.getBoolean(startedToWriteMovesKey),
                    moveToHighlightFromFile = savedInstanceState.getInt(moveToHighlightFromFileKey),
                    moveToHighlightFromRank = savedInstanceState.getInt(moveToHighlightFromRankKey),
                    moveToHighlightToFile = savedInstanceState.getInt(moveToHighlightToFileKey),
                    moveToHighlightToRank = savedInstanceState.getInt(moveToHighlightToRankKey),
                    blacksAreDown = savedInstanceState.getBoolean(blacksAreDownKey)
            )
            lastExercise = savedInstanceState.getString(lastExerciseKey)
            setPlayerGoalTextId(savedInstanceState.getInt(playerGoalIDKey),
                    savedInstanceState.getBoolean(playerGoalInAlertModeKey))
            val sanItems = savedInstanceState.getStringArray(adapterSanItemsKey)
            val fenItems = savedInstanceState.getStringArray(adapterFenItemsKey)
            val highlightStartFiles = savedInstanceState.getIntArray(registedHighlitedMovesStartFilesKey)
            val highlightStartRanks = savedInstanceState.getIntArray(registedHighlitedMovesStartRanksKey)
            val highlightEndFiles = savedInstanceState.getIntArray(registedHighlitedMovesEndFilesKey)
            val highlightEndRanks = savedInstanceState.getIntArray(registedHighlitedMovesEndRanksKey)

            val highlightStart = highlightStartFiles zip highlightStartRanks
            val highlightEnd = highlightEndFiles zip highlightEndRanks
            val highlights = (highlightStart zip highlightEnd).map { (start, end) => MoveToHighlight(
                    start.first, start.second, end.first, end.second) }
            val adapterItems = (sanItems zip fenItems) zip highlights

            listAdapter.items = adapterItems.map { (a, b) =>
                RowInput(a.first, a.second, b)
            }.toTypedArray()
            listAdapter.switchingPosition = savedInstanceState.getBoolean(switchingPositionAllowedKey)
            listAdapter.selectedNavigationItem = savedInstanceState.getInt(selectedNavigationItemKey)
        }
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        menuInflater.inflate(R.menu.menu_playing, menu)
        return true
    }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        return item.itemId match {
            case R.id.action_help => {
                val intent = Intent(this, HelpActivity.getClass)
                startActivity(intent)
                return true
            }
            case _ => super.onOptionsItemSelected(item)
        }
    }

    def askForPromotionPiece() {
        val title = getString(R.string.promotion_chooser_title)
        val dialog = PromotionPieceChooserDialogFragment.newInstance(title, playingBoard.isWhiteToPlay())
        dialog.show(supportFragmentManager, "promotionPieceChooser")
    }

    def reactForIllegalMove() {
        Toast.makeText(this, R.string.illegal_move, Toast.LENGTH_SHORT).show()
    }

    private def reverseBoard() {
        playingBoard.reverse()
    }

    /**
     * If playerHasWhite is given null, it will be set to the turn of the given fen
     */
    private def newGame(fen: String = standardFEN){
        disallowPositionNavigation()
        setPlayerGoalTextId(R.string.empty_string, alertMode = false)
        listAdapter.clear()
        lastExercise = fen
        playingBoard.new_game(fen)
    }

    private def restartLastExercise(){
        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.restarting_exercise_alert_title)
                .setMessage(R.string.restarting_exercise_alert_message)
                .setPositiveButton(R.string.yes, {(_, _) =>
                    val exercise = lastExercise
                    if (exercise != null) newGame(exercise)
                })
                .setNegativeButton(R.string.no, null)
                .show()
    }

    private def newExercise(){
        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.new_exercise_alert_title)
                .setMessage(R.string.new_exercise_alert_message)
                .setPositiveButton(R.string.yes, {(_, _) =>
                    val generatedPosition = PositionGenerator(availableGenerators[generatorIndex].constraints).generatePosition(random.nextBoolean())
                    newGame(generatedPosition)
                })
                .setNegativeButton(R.string.no, null)
                .show()
    }

    def addPositionInMovesList(san: String, fen: String, moveToHighlight: MoveToHighlight) {
        listAdapter.addPosition(san, fen, moveToHighlight)
        moves_list_view.post {
            moves_list_view.smoothScrollToPosition(listAdapter.itemCount)
        }
    }

    def setPlayerGoalTextId(textID: Int, alertMode: Boolean){
        playerGoalTextId = textID
        playerGoalInAlertMode = alertMode
        label_player_goal.text = resources.getString(textID)
        if (alertMode) label_player_goal.setTextColor(findColor(R.color.player_goal_label_alert_color))
        else label_player_goal.setTextColor(findColor(R.color.player_goal_label_standard_color))
    }

    def activatePositionNavigation(){
        listAdapter.switchingPosition = true
        playing_board_history_back.visibility = View.VISIBLE
        playing_board_history_forward.visibility = View.VISIBLE
    }

    def disallowPositionNavigation(){
        listAdapter.switchingPosition = false
        playing_board_history_back.visibility = View.INVISIBLE
        playing_board_history_forward.visibility = View.INVISIBLE
    }

    override def onBackPressed() {
        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.quit_exercise_confirmation_title)
                .setMessage(R.string.quit_exercise_confirmation_message)
                .setPositiveButton(R.string.yes, {(_, _) =>
                    super.onBackPressed()
                })
                .setNegativeButton(R.string.no, null)
                .show()
    }

    override def onStart() {
        super.onStart()
        EngineInteraction.initStockfishProcessIfNotDoneYet()
    }

    override def onStop() {
        EngineInteraction.closeStockfishProcess()
        super.onStop()
    }

    private var lastExercise:String = null
    private var generatorIndex: Int = 0
    private var random = Random()
    private var playerGoalTextId: Int = -1
    private var playerGoalInAlertMode = false
    private val listAdapter = MovesListAdapter(WeakReference(this), new ItemClickListener() {
        override def onClick(weakRefContext: WeakReference[Context], position: Int,
                             positionFen: String, moveToHighlight: MoveToHighlight) {
            if (weakRefContext.get() != null){
                weakRefContext.get() match {
                    case p:PlayingActivity => {
                        p.playingBoard.setFromFen(positionFen)
                        if (p.moveToHighlight != null) {
                            p.playingBoard.setHighlightedMove(moveToHighlight.startFile,
                                    moveToHighlight.startRank,
                                    moveToHighlight.endFile,
                                    moveToHighlight.endRank)
                            p.moves_list_view.smoothScrollToPosition(position)
                        }
                        else {
                            p.playingBoard.clearHighlightedMove()
                        }
                    }
                }
            }
        }
    })
}
