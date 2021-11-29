package com.loloof64.chessexercisesorganizer.core.pgnparser

data class PGNNode(
    var moveValue: String,
    var whiteMove: Boolean,
    var moveNumber: Int,
    var nextNode: PGNNode? = null,
    val variations: MutableList<PGNNode> = mutableListOf()
)
data class PGNGame(val tags: MutableMap<String, String>, val moves: PGNNode?)
data class PGN(val games: MutableList<PGNGame> = mutableListOf())

class PGNGamesListener() : PGNBaseListener() {
    lateinit var pgn:PGN
    private var variationDepth = 0
    private var moveNumberStack = mutableListOf<Int>()
    private var whiteMoveStack = mutableListOf<Boolean>()
    private var variationRootStack = mutableListOf<PGNNode>()
    private var variationCurrentNodeStack = mutableListOf<PGNNode>()
    private var currentTags = mutableMapOf<String, String>()
    private val games = mutableListOf<PGNGame>()

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

        currentTags = mutableMapOf()
    }

    override fun exitPgn_game(ctx: PGNParser.Pgn_gameContext?) {
        if (ctx == null) return

        games.add(PGNGame(tags = currentTags, moves = if (variationRootStack.isEmpty()) null else variationRootStack[0]))
    }

    override fun enterTag_pair(ctx: PGNParser.Tag_pairContext?) {
        if (ctx == null) return

        val tagName = ctx.tag_name().text
        var tagValue = ctx.tag_value().text
        // removing quotes
        if (tagValue.length >= 3) {
            tagValue = tagValue.substring(1, tagValue.length - 1)
        }

        currentTags[tagName] = tagValue
    }

    override fun enterMovetext_section(ctx: PGNParser.Movetext_sectionContext?) {
        if (ctx == null) return

        variationDepth = 0
        moveNumberStack = mutableListOf(1)
        whiteMoveStack = mutableListOf(true)
        variationRootStack = mutableListOf()
        variationCurrentNodeStack = mutableListOf()
    }

    override fun exitMovetext_section(ctx: PGNParser.Movetext_sectionContext?) {
        super.exitMovetext_section(ctx)
    }

    override fun exitMove_number_indication(ctx: PGNParser.Move_number_indicationContext?) {
        if (ctx == null) return

        val number = Integer.parseInt(ctx.INTEGER().text)
        val threePeriods = ctx.PERIOD().text.length >= 3
        /////////////////////////////////
        println("$number => $threePeriods")
        /////////////////////////////////
        moveNumberStack[variationDepth] = number
        whiteMoveStack[variationDepth] = !threePeriods
    }

    override fun exitSan_move(ctx: PGNParser.San_moveContext?) {
        if (ctx == null) return

        val moveText = ctx.SYMBOL().text
        val node = PGNNode(
            whiteMove = whiteMoveStack[variationDepth],
            moveValue = moveText,
            moveNumber = moveNumberStack[variationDepth],
        )
        if (variationRootStack.isEmpty() || variationRootStack.size < (variationDepth + 1)) variationRootStack.add(node)
        if (variationCurrentNodeStack.isEmpty() || variationCurrentNodeStack.size < (variationDepth + 1)) {
            variationCurrentNodeStack.add(node)
        } else {
            // Build up the linked list
            variationCurrentNodeStack[variationDepth].nextNode = node
            // Point to the new node
            variationCurrentNodeStack[variationDepth] = node
        }

        whiteMoveStack[variationDepth] = !whiteMoveStack[variationDepth]
    }

    override fun enterRecursive_variation(ctx: PGNParser.Recursive_variationContext?) {
        if (ctx == null) return

        variationDepth++
        moveNumberStack.add(1)
        whiteMoveStack.add(true)
    }

    override fun exitRecursive_variation(ctx: PGNParser.Recursive_variationContext?) {
        if (ctx == null) return

        val variationToAdd = variationRootStack[variationDepth]
        variationDepth--
        variationCurrentNodeStack[variationDepth].variations.add(variationToAdd)
    }
}