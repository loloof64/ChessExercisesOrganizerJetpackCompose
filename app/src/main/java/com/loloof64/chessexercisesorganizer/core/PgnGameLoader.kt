package com.loloof64.chessexercisesorganizer.core

import com.alonsoruibal.chess.pgn.Game
import com.alonsoruibal.chess.pgn.PgnParser
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGamesListener
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNLexer
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.StringReader


class PgnGameLoader {
    fun load(gamesFileContent: String): List<Game> {
        val charStream = CharStreams.fromReader(StringReader(gamesFileContent))
        val lexer = PGNLexer(charStream)
        val tokens = CommonTokenStream(lexer)
        val parser = PGNParser(tokens)
        val tree = parser.parse()

        val walker = ParseTreeWalker()
        val extractor = PGNGamesListener(parser)
        walker.walk(extractor, tree)

        val gamesStrings = extractor.gamesStrings
        return gamesStrings.map {
            PgnParser.parsePgn(it)
        }
    }
}