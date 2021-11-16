package com.loloof64.chessexercisesorganizer.core.pgnparser

class PGNGamesListener(private val parser: PGNParser) : PGNBaseListener() {
    var gamesStrings = listOf<String>()

    override fun exitRecurPgnDatabase(ctx: PGNParser.RecurPgnDatabaseContext?) {
        if (ctx == null) return

        val tokens = parser.tokenStream
        val nextGame = tokens.getText(ctx.pgn_game())

        gamesStrings = listOf(nextGame) + gamesStrings
    }
}