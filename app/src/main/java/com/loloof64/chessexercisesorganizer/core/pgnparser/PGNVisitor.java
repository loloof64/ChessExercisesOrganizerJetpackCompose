// Generated from /home/laurent/StudioProjects/ChessExercisesOrganizerJetpackCompose/app/src/main/java/com/loloof64/chessexercisesorganizer/core/pgnparser/PGN.g4 by ANTLR 4.9.2
package com.loloof64.chessexercisesorganizer.core.pgnparser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PGNParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PGNVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PGNParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(PGNParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code recurPgnDatabase}
	 * labeled alternative in {@link PGNParser#pgn_database}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecurPgnDatabase(PGNParser.RecurPgnDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code emptyPgnDatabase}
	 * labeled alternative in {@link PGNParser#pgn_database}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyPgnDatabase(PGNParser.EmptyPgnDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#pgn_game}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPgn_game(PGNParser.Pgn_gameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code recurTagSection}
	 * labeled alternative in {@link PGNParser#tag_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecurTagSection(PGNParser.RecurTagSectionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code singleTagSection}
	 * labeled alternative in {@link PGNParser#tag_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTagSection(PGNParser.SingleTagSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#tag_pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTag_pair(PGNParser.Tag_pairContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#tag_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTag_name(PGNParser.Tag_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#tag_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTag_value(PGNParser.Tag_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#movetext_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMovetext_section(PGNParser.Movetext_sectionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code elementRecurElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementRecurElementSequence(PGNParser.ElementRecurElementSequenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code variationRecurElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariationRecurElementSequence(PGNParser.VariationRecurElementSequenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code emptyElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyElementSequence(PGNParser.EmptyElementSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(PGNParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#move_number_indication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMove_number_indication(PGNParser.Move_number_indicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#san_move}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSan_move(PGNParser.San_moveContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#recursive_variation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecursive_variation(PGNParser.Recursive_variationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PGNParser#game_termination}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGame_termination(PGNParser.Game_terminationContext ctx);
}