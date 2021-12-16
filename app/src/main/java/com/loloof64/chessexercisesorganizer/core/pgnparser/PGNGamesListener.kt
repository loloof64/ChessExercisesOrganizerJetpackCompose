package com.loloof64.chessexercisesorganizer.core.pgnparser

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class GameTermination : Parcelable {
    WhiteWon,
    BlackWon,
    Draw,
    NotKnown,
}

@Parcelize
data class PGNNode(
    var moveValue: String,
    var whiteMove: Boolean,
    var moveNumber: Int,
    var nextNode: PGNNode? = null,
    val variations: MutableList<PGNNode> = mutableListOf(),
    var gameTermination: GameTermination? = null
) : Parcelable

@Parcelize
data class PGNGame(val tags: MutableMap<String, String>, val moves: PGNNode?) : Parcelable
data class PGN(val games: MutableList<PGNGame> = mutableListOf())

class PGNGamesListener : PGNBaseListener() {
    lateinit var pgn: PGN
    private var currentTags = mutableMapOf<String, String>()
    private val games = mutableListOf<PGNGame>()

    private var variationDepth = 0
    // Needing this in order to add all variations - which root node is head - to the different parents nodes
    private var rootNodesStack = mutableListOf<PGNNode?>()
    // Needing this in order to be up to date in each level
    private var currentNodesStack = mutableListOf<PGNNode?>()
    // Needing this in order to add variations in the correct node in each level
    private var variationsPreviousNodeStack = mutableListOf<PGNNode?>()
    // Needing this in order to keep all current move numbers in each level
    private var moveNumberStack = mutableListOf<Int>()
    // Needing this in order to keep all white turn in each level
    private var whiteTurnStack = mutableListOf<Boolean>()

    fun getGames() = games

    override fun exitRecurPgnDatabase(ctx: PGNParser.RecurPgnDatabaseContext?) {
        if (ctx == null) return

        pgn = PGN(games)
    }

    override fun exitEmptyPgnDatabase(ctx: PGNParser.EmptyPgnDatabaseContext?) {
        if (ctx == null) return

        pgn = PGN(games)
    }

    override fun enterPgn_game(ctx: PGNParser.Pgn_gameContext?) {
        if (ctx == null) return

        variationDepth = 0
        currentTags = mutableMapOf()
        rootNodesStack = mutableListOf()
        currentNodesStack = mutableListOf()
        moveNumberStack = mutableListOf()
        whiteTurnStack = mutableListOf()
        variationsPreviousNodeStack = mutableListOf()
    }

    override fun exitPgn_game(ctx: PGNParser.Pgn_gameContext?) {
        if (ctx == null) return

        games.add(
            PGNGame(
                tags = currentTags,
                moves = if (rootNodesStack.isNotEmpty() && rootNodesStack[0] != null) rootNodesStack[0] else null
            )
        )
    }

    override fun exitTag_pair(ctx: PGNParser.Tag_pairContext?) {
        if (ctx == null) return

        val tagName = ctx.tag_name().text
        var tagValue = ctx.tag_value().text
        // removing quotes
        if (tagValue.length >= 3) {
            tagValue = tagValue.substring(1, tagValue.length - 1)
        }

        currentTags[tagName] = tagValue
    }

    override fun exitMovetext_section(ctx: PGNParser.Movetext_sectionContext?) {
        if (ctx == null) return

        val gameTermination = when (ctx.game_termination().text) {
            "1-0" -> GameTermination.WhiteWon
            "0-1" -> GameTermination.BlackWon
            "1/2-1/2" -> GameTermination.Draw
            else -> GameTermination.NotKnown
        }

        if (currentNodesStack.isNotEmpty() && currentNodesStack[variationDepth] != null) {
            currentNodesStack[variationDepth]!!.gameTermination = gameTermination
        }
    }

    override fun exitMove_number_indication(ctx: PGNParser.Move_number_indicationContext?) {
        if (ctx == null) return

        val moveNumber = Integer.parseInt(ctx.INTEGER().text)
        val periods = ctx.PERIOD()?.text ?: ""
        val isWhiteTurn = periods.length < 3

        val currentLevelHasNoMoveNumberYet = moveNumberStack.size < variationDepth + 1
        if (currentLevelHasNoMoveNumberYet) {
            moveNumberStack.add(moveNumber)
        }
        else {
            moveNumberStack[variationDepth] = moveNumber
        }

        val currentLevelHasNoWhiteTurnYet = whiteTurnStack.size < variationDepth + 1
        if (currentLevelHasNoWhiteTurnYet) {
            whiteTurnStack.add(isWhiteTurn)
        }
        else {
            whiteTurnStack[variationDepth] = isWhiteTurn
        }
    }

    override fun exitSan_move(ctx: PGNParser.San_moveContext?) {
        if (ctx == null) return

        val moveSanText = ctx.SYMBOL().text
        val newNode = PGNNode(
            moveValue = moveSanText,
            whiteMove = whiteTurnStack[variationDepth],
            moveNumber =  moveNumberStack[variationDepth],
        )

        val noRootDefinedForCurrentLevel = rootNodesStack.size < variationDepth + 1
        if (noRootDefinedForCurrentLevel) {
            rootNodesStack.add(newNode)
        }
        else if (rootNodesStack[variationDepth] == null) {
            rootNodesStack[variationDepth] = newNode
        }

        val noCurrentNodeDefinedForCurrentLevel = currentNodesStack.size < variationDepth + 1
        when {
            noCurrentNodeDefinedForCurrentLevel -> {
                currentNodesStack.add(newNode)
            }
            // Current node not attributed yet for current level
            currentNodesStack[variationDepth] == null -> {
                currentNodesStack[variationDepth] = newNode
            }
            else -> {
                // Building up the linked list
                currentNodesStack[variationDepth]!!.nextNode = newNode

                // Pointing to the new node
                currentNodesStack[variationDepth] = newNode
            }
        }

        // Updating old node
        if (variationsPreviousNodeStack.isEmpty()) {
            variationsPreviousNodeStack.add(null)
        }
        else {
            // We are sure that the newNode variable has now become the old node
            variationsPreviousNodeStack[variationDepth] = newNode
        }

        // Updating white turn
        whiteTurnStack[variationDepth] = !whiteTurnStack[variationDepth]
    }

    override fun enterRecursive_variation(ctx: PGNParser.Recursive_variationContext?) {
        if (ctx == null) return

        variationDepth++
        val currentLevelHasNoPreviousMoveYet = variationsPreviousNodeStack.size < variationDepth + 1
        if (currentLevelHasNoPreviousMoveYet) {
            variationsPreviousNodeStack.add(null)
        }
        else {
            variationsPreviousNodeStack[variationDepth] = null
        }

        val noRootForCurrentLevel = rootNodesStack.size < variationDepth + 1
        if (noRootForCurrentLevel) {
            rootNodesStack.add(null)
        }
        else {
            rootNodesStack[variationDepth] = null
        }

        val noCurrentNodeForCurrentLevel = currentNodesStack.size < variationDepth + 1
        if (noCurrentNodeForCurrentLevel) {
            currentNodesStack.add(null)
        }
        else {
            currentNodesStack[variationDepth] = null
        }
    }

    override fun exitRecursive_variation(ctx: PGNParser.Recursive_variationContext?) {
        if (ctx == null) return

        // Asserting that each variation has at least one move san
        val currentVariationRoot = rootNodesStack[variationDepth]!!
        variationDepth--
        // We add variations to the previous node of current variation if possible
        if (variationsPreviousNodeStack[variationDepth] != null) {
            variationsPreviousNodeStack[variationDepth]!!.variations.add(currentVariationRoot)
        }
        // Otherwise add to the root node of the current variation, now that we're back one level
        else {
            rootNodesStack[variationDepth]?.variations?.add(currentVariationRoot)
        }
    }
}