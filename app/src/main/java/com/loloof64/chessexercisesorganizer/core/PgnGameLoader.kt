package com.loloof64.chessexercisesorganizer.core

import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGamesListener
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNLexer
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.StringReader

class PgnGameLoader {
    fun load(gamesFileContent: String): List<PGNGame> {
        val charStream = CharStreams.fromReader(StringReader(gamesFileContent))
        val lexer = PGNLexer(charStream)
        val tokens = CommonTokenStream(lexer)
        val parser = PGNParser(tokens)
        val tree = parser.parse()

        val walker = ParseTreeWalker()
        val extractor = PGNGamesListener()
        walker.walk(extractor, tree)

        return extractor.getGames()
    }
}