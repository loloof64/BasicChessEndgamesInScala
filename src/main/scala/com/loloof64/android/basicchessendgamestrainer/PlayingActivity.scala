package com.loloof64.android.basicchessendgamestrainer

import android.content.{Context, Intent, DialogInterface}
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.{AlertDialog, AppCompatActivity}
import android.support.v7.widget.{GridLayoutManager, RecyclerView}
import android.util.TypedValue
import android.view.{Menu, MenuItem, View}
import android.widget.Toast
import com.github.bhlangonijr.chesslib.Piece
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.PositionGenerator
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.Exercises.availableGenerators
import com.loloof64.android.basicchessendgamestrainer.playing_activity._
import java.lang.ref.WeakReference
import java.util._
import com.github.ghik.silencer.silent

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

class PlayingActivity extends AppCompatActivity() with PromotionPieceChooserDialogFragment.Listener {

    import PlayingActivity._

    implicit val context = this
    lazy val vh: TypedViewHolder.activity_playing = TypedViewHolder.setContentView(this, TR.layout.activity_playing)

    @silent
    private def findColor(colorResId: Int): Int = getResources().getColor(colorResId)

    override def reactToPromotionPieceSelection(piece: Piece) {
        vh.playingBoard.validatePromotionMove(piece)
        vh.playingBoard.checkIfGameFinished()
        if (!vh.playingBoard.gameFinished()) vh.playingBoard.makeComputerPlay()
    }

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playing)

        val gridLayoutColumns = if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) 6 else 6
        val gridLayoutManager = new GridLayoutManager(this, gridLayoutColumns)
        vh.moves_list_view.setLayoutManager(gridLayoutManager)
        vh.moves_list_view.setAdapter(listAdapter)
        val spaceDp = 5.0f
        val space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spaceDp, getResources().getDisplayMetrics())
        vh.moves_list_view.addItemDecoration(new SpaceLeftAndRightItemDecorator(space.toInt))

        vh.playing_board_history_back.setOnClickListener(new View.OnClickListener{ v => listAdapter.goBackInHistory() })
        vh.playing_board_history_forward.setOnClickListener(new View.OnClickListener{ v => listAdapter.goForwardInHistory() })

        vh.fab_restart_exercise.setOnClickListener (new View.OnClickListener{ v => restartLastExercise() })
        vh.fab_reverse_board.setOnClickListener (new View.OnClickListener{ v => reverseBoard() })
        vh.fab_new_exercise.setOnClickListener (new View.OnClickListener{ v => newExercise() })

        EngineInteraction.initStockfishProcessIfNotDoneYet()

        generatorIndex = getIntent().getExtras().getInt(generatorIndexKey)
        val generatedPosition = new PositionGenerator(availableGenerators(generatorIndex).constraints).generatePosition(random.nextBoolean())
        if (generatedPosition.isEmpty) {
            Toast.makeText(this, R.string.position_generation_error, Toast.LENGTH_LONG).show()
        }
        else {
            newGame(generatedPosition)            
        }
    }

    override def onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(currentPositionkey, vh.playingBoard.toFEN())
        outState.putBoolean(playerHasWhiteKey, vh.playingBoard.playerHasWhite())
        outState.putBoolean(gameFinishedKey, vh.playingBoard.gameFinished())
        outState.putString(lastExerciseKey, lastExercise)
        outState.putInt(playerGoalIDKey, playerGoalTextId)
        outState.putBoolean(playerGoalInAlertModeKey, playerGoalInAlertMode)
        outState.putBoolean(waitingForPlayerGoalKey, vh.playingBoard.isWaitingForPlayerGoal())
        outState.putStringArray(adapterSanItemsKey, listAdapter.items.map { _.san }.toArray)
        outState.putStringArray(adapterFenItemsKey, listAdapter.items.map { _.relatedFen }.toArray)
        outState.putBoolean(startedToWriteMovesKey, vh.playingBoard.hasStartedToWriteMoves())
        outState.putInt(moveToHighlightFromFileKey, vh.playingBoard.getMoveToHighlightFromFile())
        outState.putInt(moveToHighlightFromRankKey, vh.playingBoard.getMoveToHighlightFromRank())
        outState.putInt(moveToHighlightToFileKey, vh.playingBoard.getMoveToHighlightToFile())
        outState.putInt(moveToHighlightToRankKey, vh.playingBoard.getMoveToHighlightToRank())
        outState.putBoolean(switchingPositionAllowedKey, listAdapter.switchingPosition)
        outState.putIntArray(registedHighlitedMovesStartFilesKey,
                listAdapter.items.map { _.moveToHighlight.startFile }.toArray)
        outState.putIntArray(registedHighlitedMovesStartRanksKey,
                listAdapter.items.map { _.moveToHighlight.startRank }.toArray)
        outState.putIntArray(registedHighlitedMovesEndFilesKey,
                listAdapter.items.map { _.moveToHighlight.endFile }.toArray)
        outState.putIntArray(registedHighlitedMovesEndRanksKey,
                listAdapter.items.map { _.moveToHighlight.endRank }.toArray)
        outState.putInt(selectedNavigationItemKey, listAdapter.selectedNavigationItem)
        outState.putBoolean(blacksAreDownKey, vh.playingBoard.areBlackDown())
    }

    override def onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            vh.playingBoard.reloadPosition(fen = savedInstanceState.getString(currentPositionkey),
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
            val highlights = (highlightStart zip highlightEnd).map { case (start, end) => MoveToHighlight(
                    start._1, start._2, end._1, end._2) }
            val adapterItems = (sanItems zip fenItems) zip highlights

            listAdapter.items = adapterItems.map { case (a, b) =>
                RowInput(a._1, a._2, b)
            }.toArray
            listAdapter.switchingPosition = savedInstanceState.getBoolean(switchingPositionAllowedKey)
            listAdapter.selectedNavigationItem = savedInstanceState.getInt(selectedNavigationItemKey)
        }
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        getMenuInflater().inflate(R.menu.menu_playing, menu)
        return true
    }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        return item.getItemId() match {
            case R.id.action_help => {
                val intent = new Intent(this, classOf[HelpActivity])
                startActivity(intent)
                return true
            }
            case _ => super.onOptionsItemSelected(item)
        }
    }

    def askForPromotionPiece() {
        val title = getString(R.string.promotion_chooser_title)
        val dialog = PromotionPieceChooserDialogFragment.newInstance(title, vh.playingBoard.isWhiteToPlay())
        dialog.show(getSupportFragmentManager(), "promotionPieceChooser")
    }

    def reactForIllegalMove() {
        Toast.makeText(this, R.string.illegal_move, Toast.LENGTH_SHORT).show()
    }

    private def reverseBoard() {
        vh.playingBoard.reverse()
    }

    /**
     * If playerHasWhite is given null, it will be set to the turn of the given fen
     */
    private def newGame(fen: String = standardFEN){
        disallowPositionNavigation()
        setPlayerGoalTextId(R.string.empty_string, alertMode = false)
        listAdapter.clear()
        lastExercise = fen
        vh.playingBoard.new_game(fen)
    }

    private def restartLastExercise(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.restarting_exercise_alert_title)
                .setMessage(R.string.restarting_exercise_alert_message)
                .setPositiveButton(R.string.yes, ( new DialogInterface.OnClickListener { _ =>
                    val exercise = lastExercise
                    if (exercise != null) newGame(exercise)
                }))
                .setNegativeButton(R.string.no, null)
                .show()
    }

    private def newExercise(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.new_exercise_alert_title)
                .setMessage(R.string.new_exercise_alert_message)
                .setPositiveButton(R.string.yes, ( new DialogInterface.OnClickListener { _ =>
                    val generatedPosition = new PositionGenerator(availableGenerators(generatorIndex).constraints).generatePosition(random.nextBoolean())
                    newGame(generatedPosition)
                }))
                .setNegativeButton(R.string.no, null)
                .show()
    }

    def addPositionInMovesList(san: String, fen: String, moveToHighlight: MoveToHighlight) {
        listAdapter.addPosition(san, fen, moveToHighlight)
        vh.moves_list_view.post (new Runnable {() =>
            vh.moves_list_view.smoothScrollToPosition(listAdapter.getItemCount())
        })
    }

    def setPlayerGoalTextId(textID: Int, alertMode: Boolean){
        playerGoalTextId = textID
        playerGoalInAlertMode = alertMode
        vh.label_player_goal.setText(getResources().getString(textID))
        if (alertMode) vh.label_player_goal.setTextColor(findColor(R.color.player_goal_label_alert_color))
        else vh.label_player_goal.setTextColor(findColor(R.color.player_goal_label_standard_color))
    }

    def activatePositionNavigation(){
        listAdapter.switchingPosition = true
        vh.playing_board_history_back.setVisibility(View.VISIBLE)
        vh.playing_board_history_forward.setVisibility(View.VISIBLE)
    }

    def disallowPositionNavigation(){
        listAdapter.switchingPosition = false
        vh.playing_board_history_back.setVisibility(View.INVISIBLE)
        vh.playing_board_history_forward.setVisibility(View.INVISIBLE)
    }

    override def onBackPressed() {
        val superOnBackPressed = () => super.onBackPressed()
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.quit_exercise_confirmation_title)
                .setMessage(R.string.quit_exercise_confirmation_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener { _ =>
                    superOnBackPressed()
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
    private var random = new Random()
    private var playerGoalTextId: Int = -1
    private var playerGoalInAlertMode = false
    private val listAdapter = new MovesListAdapter(new WeakReference(this), new ItemClickListener() {
        override def onClick(weakRefContext: WeakReference[Context], position: Int,
                             positionFen: String, moveToHighlight: MoveToHighlight) {
            if (weakRefContext.get() != null){
                weakRefContext.get() match {
                    case p:PlayingActivity => {
                        p.vh.playingBoard.setFromFen(positionFen)
                        if (moveToHighlight != null) {
                            p.vh.playingBoard.setHighlightedMove(moveToHighlight.startFile,
                                    moveToHighlight.startRank,
                                    moveToHighlight.endFile,
                                    moveToHighlight.endRank)
                            p.vh.moves_list_view.smoothScrollToPosition(position)
                        }
                        else {
                            p.vh.playingBoard.clearHighlightedMove()
                        }
                    }
                }
            }
        }
    })
}
