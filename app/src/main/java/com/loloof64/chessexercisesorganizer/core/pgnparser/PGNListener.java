// Generated from /home/laurent/StudioProjects/ChessExercisesOrganizerJetpackCompose/app/src/main/java/com/loloof64/chessexercisesorganizer/core/pgnparser/PGN.g4 by ANTLR 4.9.2
package com.loloof64.chessexercisesorganizer.core.pgnparser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PGNParser}.
 */
public interface PGNListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PGNParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(PGNParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(PGNParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code recurPgnDatabase}
	 * labeled alternative in {@link PGNParser#pgn_database}.
	 * @param ctx the parse tree
	 */
	void enterRecurPgnDatabase(PGNParser.RecurPgnDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code recurPgnDatabase}
	 * labeled alternative in {@link PGNParser#pgn_database}.
	 * @param ctx the parse tree
	 */
	void exitRecurPgnDatabase(PGNParser.RecurPgnDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code emptyPgnDatabase}
	 * labeled alternative in {@link PGNParser#pgn_database}.
	 * @param ctx the parse tree
	 */
	void enterEmptyPgnDatabase(PGNParser.EmptyPgnDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code emptyPgnDatabase}
	 * labeled alternative in {@link PGNParser#pgn_database}.
	 * @param ctx the parse tree
	 */
	void exitEmptyPgnDatabase(PGNParser.EmptyPgnDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#pgn_game}.
	 * @param ctx the parse tree
	 */
	void enterPgn_game(PGNParser.Pgn_gameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#pgn_game}.
	 * @param ctx the parse tree
	 */
	void exitPgn_game(PGNParser.Pgn_gameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code recurTagSection}
	 * labeled alternative in {@link PGNParser#tag_section}.
	 * @param ctx the parse tree
	 */
	void enterRecurTagSection(PGNParser.RecurTagSectionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code recurTagSection}
	 * labeled alternative in {@link PGNParser#tag_section}.
	 * @param ctx the parse tree
	 */
	void exitRecurTagSection(PGNParser.RecurTagSectionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleTagSection}
	 * labeled alternative in {@link PGNParser#tag_section}.
	 * @param ctx the parse tree
	 */
	void enterSingleTagSection(PGNParser.SingleTagSectionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleTagSection}
	 * labeled alternative in {@link PGNParser#tag_section}.
	 * @param ctx the parse tree
	 */
	void exitSingleTagSection(PGNParser.SingleTagSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#tag_pair}.
	 * @param ctx the parse tree
	 */
	void enterTag_pair(PGNParser.Tag_pairContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#tag_pair}.
	 * @param ctx the parse tree
	 */
	void exitTag_pair(PGNParser.Tag_pairContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#tag_name}.
	 * @param ctx the parse tree
	 */
	void enterTag_name(PGNParser.Tag_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#tag_name}.
	 * @param ctx the parse tree
	 */
	void exitTag_name(PGNParser.Tag_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#tag_value}.
	 * @param ctx the parse tree
	 */
	void enterTag_value(PGNParser.Tag_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#tag_value}.
	 * @param ctx the parse tree
	 */
	void exitTag_value(PGNParser.Tag_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#movetext_section}.
	 * @param ctx the parse tree
	 */
	void enterMovetext_section(PGNParser.Movetext_sectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#movetext_section}.
	 * @param ctx the parse tree
	 */
	void exitMovetext_section(PGNParser.Movetext_sectionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elementRecurElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 */
	void enterElementRecurElementSequence(PGNParser.ElementRecurElementSequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elementRecurElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 */
	void exitElementRecurElementSequence(PGNParser.ElementRecurElementSequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variationRecurElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 */
	void enterVariationRecurElementSequence(PGNParser.VariationRecurElementSequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variationRecurElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 */
	void exitVariationRecurElementSequence(PGNParser.VariationRecurElementSequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code emptyElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 */
	void enterEmptyElementSequence(PGNParser.EmptyElementSequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code emptyElementSequence}
	 * labeled alternative in {@link PGNParser#element_sequence}.
	 * @param ctx the parse tree
	 */
	void exitEmptyElementSequence(PGNParser.EmptyElementSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(PGNParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(PGNParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#move_number_indication}.
	 * @param ctx the parse tree
	 */
	void enterMove_number_indication(PGNParser.Move_number_indicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#move_number_indication}.
	 * @param ctx the parse tree
	 */
	void exitMove_number_indication(PGNParser.Move_number_indicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#san_move}.
	 * @param ctx the parse tree
	 */
	void enterSan_move(PGNParser.San_moveContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#san_move}.
	 * @param ctx the parse tree
	 */
	void exitSan_move(PGNParser.San_moveContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#recursive_variation}.
	 * @param ctx the parse tree
	 */
	void enterRecursive_variation(PGNParser.Recursive_variationContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#recursive_variation}.
	 * @param ctx the parse tree
	 */
	void exitRecursive_variation(PGNParser.Recursive_variationContext ctx);
	/**
	 * Enter a parse tree produced by {@link PGNParser#game_termination}.
	 * @param ctx the parse tree
	 */
	void enterGame_termination(PGNParser.Game_terminationContext ctx);
	/**
	 * Exit a parse tree produced by {@link PGNParser#game_termination}.
	 * @param ctx the parse tree
	 */
	void exitGame_termination(PGNParser.Game_terminationContext ctx);
}