// Generated from /home/laurent/StudioProjects/ChessExercisesOrganizerJetpackCompose/app/src/main/java/com/loloof64/chessexercisesorganizer/core/pgnparser/PGN.g4 by ANTLR 4.9.2
package com.loloof64.chessexercisesorganizer.core.pgnparser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PGNParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WHITE_WINS=1, BLACK_WINS=2, DRAWN_GAME=3, REST_OF_LINE_COMMENT=4, BRACE_COMMENT=5, 
		ESCAPE=6, SPACE=7, SIMPLE_SPACE=8, NEWLINE=9, STRING=10, INTEGER=11, PERIOD=12, 
		ASTERISK=13, LEFT_BRACKET=14, RIGHT_BRACKET=15, LEFT_PARENTHESIS=16, RIGHT_PARENTHESIS=17, 
		LEFT_ANGLE_BRACKET=18, RIGHT_ANGLE_BRACKET=19, NUMERIC_ANNOTATION_GLYPH=20, 
		SYMBOL=21, SUFFIX_ANNOTATION=22, UNEXPECTED_CHAR=23;
	public static final int
		RULE_parse = 0, RULE_pgn_database = 1, RULE_pgn_game = 2, RULE_tag_section = 3, 
		RULE_tag_pair = 4, RULE_tag_name = 5, RULE_tag_value = 6, RULE_movetext_section = 7, 
		RULE_element_sequence = 8, RULE_element = 9, RULE_move_number_indication = 10, 
		RULE_san_move = 11, RULE_recursive_variation = 12, RULE_game_termination = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"parse", "pgn_database", "pgn_game", "tag_section", "tag_pair", "tag_name", 
			"tag_value", "movetext_section", "element_sequence", "element", "move_number_indication", 
			"san_move", "recursive_variation", "game_termination"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'1-0'", "'0-1'", "'1/2-1/2'", null, null, null, null, null, null, 
			null, null, null, "'*'", "'['", "']'", "'('", "')'", "'<'", "'>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WHITE_WINS", "BLACK_WINS", "DRAWN_GAME", "REST_OF_LINE_COMMENT", 
			"BRACE_COMMENT", "ESCAPE", "SPACE", "SIMPLE_SPACE", "NEWLINE", "STRING", 
			"INTEGER", "PERIOD", "ASTERISK", "LEFT_BRACKET", "RIGHT_BRACKET", "LEFT_PARENTHESIS", 
			"RIGHT_PARENTHESIS", "LEFT_ANGLE_BRACKET", "RIGHT_ANGLE_BRACKET", "NUMERIC_ANNOTATION_GLYPH", 
			"SYMBOL", "SUFFIX_ANNOTATION", "UNEXPECTED_CHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "PGN.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PGNParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ParseContext extends ParserRuleContext {
		public Pgn_databaseContext pgn_database() {
			return getRuleContext(Pgn_databaseContext.class,0);
		}
		public TerminalNode EOF() { return getToken(PGNParser.EOF, 0); }
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitParse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitParse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			pgn_database();
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SPACE) {
				{
				{
				setState(29);
				match(SPACE);
				}
				}
				setState(34);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(35);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pgn_databaseContext extends ParserRuleContext {
		public Pgn_databaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pgn_database; }
	 
		public Pgn_databaseContext() { }
		public void copyFrom(Pgn_databaseContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RecurPgnDatabaseContext extends Pgn_databaseContext {
		public Pgn_gameContext pgn_game() {
			return getRuleContext(Pgn_gameContext.class,0);
		}
		public Pgn_databaseContext pgn_database() {
			return getRuleContext(Pgn_databaseContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public RecurPgnDatabaseContext(Pgn_databaseContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterRecurPgnDatabase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitRecurPgnDatabase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitRecurPgnDatabase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class EmptyPgnDatabaseContext extends Pgn_databaseContext {
		public EmptyPgnDatabaseContext(Pgn_databaseContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterEmptyPgnDatabase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitEmptyPgnDatabase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitEmptyPgnDatabase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Pgn_databaseContext pgn_database() throws RecognitionException {
		Pgn_databaseContext _localctx = new Pgn_databaseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_pgn_database);
		try {
			int _alt;
			setState(47);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LEFT_BRACKET:
				_localctx = new RecurPgnDatabaseContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(37);
				pgn_game();
				setState(41);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(38);
						match(SPACE);
						}
						} 
					}
					setState(43);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				}
				setState(44);
				pgn_database();
				}
				break;
			case EOF:
			case SPACE:
				_localctx = new EmptyPgnDatabaseContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pgn_gameContext extends ParserRuleContext {
		public Tag_sectionContext tag_section() {
			return getRuleContext(Tag_sectionContext.class,0);
		}
		public Movetext_sectionContext movetext_section() {
			return getRuleContext(Movetext_sectionContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public Pgn_gameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pgn_game; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterPgn_game(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitPgn_game(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitPgn_game(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Pgn_gameContext pgn_game() throws RecognitionException {
		Pgn_gameContext _localctx = new Pgn_gameContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_pgn_game);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			tag_section();
			setState(51); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(50);
					match(SPACE);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(53); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(55);
			movetext_section();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Tag_sectionContext extends ParserRuleContext {
		public Tag_sectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag_section; }
	 
		public Tag_sectionContext() { }
		public void copyFrom(Tag_sectionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RecurTagSectionContext extends Tag_sectionContext {
		public Tag_pairContext tag_pair() {
			return getRuleContext(Tag_pairContext.class,0);
		}
		public Tag_sectionContext tag_section() {
			return getRuleContext(Tag_sectionContext.class,0);
		}
		public TerminalNode SPACE() { return getToken(PGNParser.SPACE, 0); }
		public RecurTagSectionContext(Tag_sectionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterRecurTagSection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitRecurTagSection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitRecurTagSection(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SingleTagSectionContext extends Tag_sectionContext {
		public Tag_pairContext tag_pair() {
			return getRuleContext(Tag_pairContext.class,0);
		}
		public SingleTagSectionContext(Tag_sectionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterSingleTagSection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitSingleTagSection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitSingleTagSection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Tag_sectionContext tag_section() throws RecognitionException {
		Tag_sectionContext _localctx = new Tag_sectionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tag_section);
		int _la;
		try {
			setState(64);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				_localctx = new RecurTagSectionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(57);
				tag_pair();
				setState(59);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SPACE) {
					{
					setState(58);
					match(SPACE);
					}
				}

				setState(61);
				tag_section();
				}
				break;
			case 2:
				_localctx = new SingleTagSectionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(63);
				tag_pair();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Tag_pairContext extends ParserRuleContext {
		public TerminalNode LEFT_BRACKET() { return getToken(PGNParser.LEFT_BRACKET, 0); }
		public Tag_nameContext tag_name() {
			return getRuleContext(Tag_nameContext.class,0);
		}
		public Tag_valueContext tag_value() {
			return getRuleContext(Tag_valueContext.class,0);
		}
		public TerminalNode RIGHT_BRACKET() { return getToken(PGNParser.RIGHT_BRACKET, 0); }
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public Tag_pairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag_pair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterTag_pair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitTag_pair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitTag_pair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Tag_pairContext tag_pair() throws RecognitionException {
		Tag_pairContext _localctx = new Tag_pairContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_tag_pair);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			match(LEFT_BRACKET);
			setState(68);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SPACE) {
				{
				setState(67);
				match(SPACE);
				}
			}

			setState(70);
			tag_name();
			setState(72); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(71);
				match(SPACE);
				}
				}
				setState(74); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==SPACE );
			setState(76);
			tag_value();
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SPACE) {
				{
				setState(77);
				match(SPACE);
				}
			}

			setState(80);
			match(RIGHT_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Tag_nameContext extends ParserRuleContext {
		public TerminalNode SYMBOL() { return getToken(PGNParser.SYMBOL, 0); }
		public Tag_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterTag_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitTag_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitTag_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Tag_nameContext tag_name() throws RecognitionException {
		Tag_nameContext _localctx = new Tag_nameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_tag_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			match(SYMBOL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Tag_valueContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(PGNParser.STRING, 0); }
		public Tag_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterTag_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitTag_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitTag_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Tag_valueContext tag_value() throws RecognitionException {
		Tag_valueContext _localctx = new Tag_valueContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_tag_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Movetext_sectionContext extends ParserRuleContext {
		public Element_sequenceContext element_sequence() {
			return getRuleContext(Element_sequenceContext.class,0);
		}
		public Game_terminationContext game_termination() {
			return getRuleContext(Game_terminationContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public Movetext_sectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_movetext_section; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterMovetext_section(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitMovetext_section(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitMovetext_section(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Movetext_sectionContext movetext_section() throws RecognitionException {
		Movetext_sectionContext _localctx = new Movetext_sectionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_movetext_section);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			element_sequence();
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SPACE) {
				{
				{
				setState(87);
				match(SPACE);
				}
				}
				setState(92);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(93);
			game_termination();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Element_sequenceContext extends ParserRuleContext {
		public Element_sequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element_sequence; }
	 
		public Element_sequenceContext() { }
		public void copyFrom(Element_sequenceContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class VariationRecurElementSequenceContext extends Element_sequenceContext {
		public Recursive_variationContext recursive_variation() {
			return getRuleContext(Recursive_variationContext.class,0);
		}
		public Element_sequenceContext element_sequence() {
			return getRuleContext(Element_sequenceContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public VariationRecurElementSequenceContext(Element_sequenceContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterVariationRecurElementSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitVariationRecurElementSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitVariationRecurElementSequence(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SingleRecurVariationRecurElementSequenceContext extends Element_sequenceContext {
		public Recursive_variationContext recursive_variation() {
			return getRuleContext(Recursive_variationContext.class,0);
		}
		public SingleRecurVariationRecurElementSequenceContext(Element_sequenceContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterSingleRecurVariationRecurElementSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitSingleRecurVariationRecurElementSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitSingleRecurVariationRecurElementSequence(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class EmptyElementSequenceContext extends Element_sequenceContext {
		public EmptyElementSequenceContext(Element_sequenceContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterEmptyElementSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitEmptyElementSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitEmptyElementSequence(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SingleElementRecurElementSequenceContext extends Element_sequenceContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public SingleElementRecurElementSequenceContext(Element_sequenceContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterSingleElementRecurElementSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitSingleElementRecurElementSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitSingleElementRecurElementSequence(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ElementRecurElementSequenceContext extends Element_sequenceContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public Element_sequenceContext element_sequence() {
			return getRuleContext(Element_sequenceContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public ElementRecurElementSequenceContext(Element_sequenceContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterElementRecurElementSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitElementRecurElementSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitElementRecurElementSequence(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Element_sequenceContext element_sequence() throws RecognitionException {
		Element_sequenceContext _localctx = new Element_sequenceContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_element_sequence);
		try {
			int _alt;
			setState(116);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				_localctx = new ElementRecurElementSequenceContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(95);
				element();
				setState(99);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(96);
						match(SPACE);
						}
						} 
					}
					setState(101);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				}
				setState(102);
				element_sequence();
				}
				break;
			case 2:
				_localctx = new VariationRecurElementSequenceContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(104);
				recursive_variation();
				setState(108);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(105);
						match(SPACE);
						}
						} 
					}
					setState(110);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				setState(111);
				element_sequence();
				}
				break;
			case 3:
				_localctx = new SingleElementRecurElementSequenceContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(113);
				element();
				}
				break;
			case 4:
				_localctx = new SingleRecurVariationRecurElementSequenceContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(114);
				recursive_variation();
				}
				break;
			case 5:
				_localctx = new EmptyElementSequenceContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementContext extends ParserRuleContext {
		public Move_number_indicationContext move_number_indication() {
			return getRuleContext(Move_number_indicationContext.class,0);
		}
		public San_moveContext san_move() {
			return getRuleContext(San_moveContext.class,0);
		}
		public TerminalNode NUMERIC_ANNOTATION_GLYPH() { return getToken(PGNParser.NUMERIC_ANNOTATION_GLYPH, 0); }
		public ElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_element);
		try {
			setState(121);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
				enterOuterAlt(_localctx, 1);
				{
				setState(118);
				move_number_indication();
				}
				break;
			case SYMBOL:
				enterOuterAlt(_localctx, 2);
				{
				setState(119);
				san_move();
				}
				break;
			case NUMERIC_ANNOTATION_GLYPH:
				enterOuterAlt(_localctx, 3);
				{
				setState(120);
				match(NUMERIC_ANNOTATION_GLYPH);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Move_number_indicationContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(PGNParser.INTEGER, 0); }
		public TerminalNode PERIOD() { return getToken(PGNParser.PERIOD, 0); }
		public Move_number_indicationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_move_number_indication; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterMove_number_indication(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitMove_number_indication(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitMove_number_indication(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Move_number_indicationContext move_number_indication() throws RecognitionException {
		Move_number_indicationContext _localctx = new Move_number_indicationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_move_number_indication);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			match(INTEGER);
			setState(125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PERIOD) {
				{
				setState(124);
				match(PERIOD);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class San_moveContext extends ParserRuleContext {
		public TerminalNode SYMBOL() { return getToken(PGNParser.SYMBOL, 0); }
		public San_moveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_san_move; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterSan_move(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitSan_move(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitSan_move(this);
			else return visitor.visitChildren(this);
		}
	}

	public final San_moveContext san_move() throws RecognitionException {
		San_moveContext _localctx = new San_moveContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_san_move);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(SYMBOL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Recursive_variationContext extends ParserRuleContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(PGNParser.LEFT_PARENTHESIS, 0); }
		public Element_sequenceContext element_sequence() {
			return getRuleContext(Element_sequenceContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(PGNParser.RIGHT_PARENTHESIS, 0); }
		public List<TerminalNode> SPACE() { return getTokens(PGNParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PGNParser.SPACE, i);
		}
		public Recursive_variationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recursive_variation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterRecursive_variation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitRecursive_variation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitRecursive_variation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Recursive_variationContext recursive_variation() throws RecognitionException {
		Recursive_variationContext _localctx = new Recursive_variationContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_recursive_variation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(LEFT_PARENTHESIS);
			setState(131);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(130);
				match(SPACE);
				}
				break;
			}
			setState(133);
			element_sequence();
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SPACE) {
				{
				setState(134);
				match(SPACE);
				}
			}

			setState(137);
			match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Game_terminationContext extends ParserRuleContext {
		public TerminalNode WHITE_WINS() { return getToken(PGNParser.WHITE_WINS, 0); }
		public TerminalNode BLACK_WINS() { return getToken(PGNParser.BLACK_WINS, 0); }
		public TerminalNode DRAWN_GAME() { return getToken(PGNParser.DRAWN_GAME, 0); }
		public TerminalNode ASTERISK() { return getToken(PGNParser.ASTERISK, 0); }
		public Game_terminationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_game_termination; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).enterGame_termination(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PGNListener ) ((PGNListener)listener).exitGame_termination(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PGNVisitor ) return ((PGNVisitor<? extends T>)visitor).visitGame_termination(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Game_terminationContext game_termination() throws RecognitionException {
		Game_terminationContext _localctx = new Game_terminationContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_game_termination);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << WHITE_WINS) | (1L << BLACK_WINS) | (1L << DRAWN_GAME) | (1L << ASTERISK))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\31\u0090\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\7\2!\n\2\f\2\16\2$\13"+
		"\2\3\2\3\2\3\3\3\3\7\3*\n\3\f\3\16\3-\13\3\3\3\3\3\3\3\5\3\62\n\3\3\4"+
		"\3\4\6\4\66\n\4\r\4\16\4\67\3\4\3\4\3\5\3\5\5\5>\n\5\3\5\3\5\3\5\5\5C"+
		"\n\5\3\6\3\6\5\6G\n\6\3\6\3\6\6\6K\n\6\r\6\16\6L\3\6\3\6\5\6Q\n\6\3\6"+
		"\3\6\3\7\3\7\3\b\3\b\3\t\3\t\7\t[\n\t\f\t\16\t^\13\t\3\t\3\t\3\n\3\n\7"+
		"\nd\n\n\f\n\16\ng\13\n\3\n\3\n\3\n\3\n\7\nm\n\n\f\n\16\np\13\n\3\n\3\n"+
		"\3\n\3\n\3\n\5\nw\n\n\3\13\3\13\3\13\5\13|\n\13\3\f\3\f\5\f\u0080\n\f"+
		"\3\r\3\r\3\16\3\16\5\16\u0086\n\16\3\16\3\16\5\16\u008a\n\16\3\16\3\16"+
		"\3\17\3\17\3\17\2\2\20\2\4\6\b\n\f\16\20\22\24\26\30\32\34\2\3\4\2\3\5"+
		"\17\17\2\u0096\2\36\3\2\2\2\4\61\3\2\2\2\6\63\3\2\2\2\bB\3\2\2\2\nD\3"+
		"\2\2\2\fT\3\2\2\2\16V\3\2\2\2\20X\3\2\2\2\22v\3\2\2\2\24{\3\2\2\2\26}"+
		"\3\2\2\2\30\u0081\3\2\2\2\32\u0083\3\2\2\2\34\u008d\3\2\2\2\36\"\5\4\3"+
		"\2\37!\7\t\2\2 \37\3\2\2\2!$\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#%\3\2\2\2$"+
		"\"\3\2\2\2%&\7\2\2\3&\3\3\2\2\2\'+\5\6\4\2(*\7\t\2\2)(\3\2\2\2*-\3\2\2"+
		"\2+)\3\2\2\2+,\3\2\2\2,.\3\2\2\2-+\3\2\2\2./\5\4\3\2/\62\3\2\2\2\60\62"+
		"\3\2\2\2\61\'\3\2\2\2\61\60\3\2\2\2\62\5\3\2\2\2\63\65\5\b\5\2\64\66\7"+
		"\t\2\2\65\64\3\2\2\2\66\67\3\2\2\2\67\65\3\2\2\2\678\3\2\2\289\3\2\2\2"+
		"9:\5\20\t\2:\7\3\2\2\2;=\5\n\6\2<>\7\t\2\2=<\3\2\2\2=>\3\2\2\2>?\3\2\2"+
		"\2?@\5\b\5\2@C\3\2\2\2AC\5\n\6\2B;\3\2\2\2BA\3\2\2\2C\t\3\2\2\2DF\7\20"+
		"\2\2EG\7\t\2\2FE\3\2\2\2FG\3\2\2\2GH\3\2\2\2HJ\5\f\7\2IK\7\t\2\2JI\3\2"+
		"\2\2KL\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MN\3\2\2\2NP\5\16\b\2OQ\7\t\2\2PO\3"+
		"\2\2\2PQ\3\2\2\2QR\3\2\2\2RS\7\21\2\2S\13\3\2\2\2TU\7\27\2\2U\r\3\2\2"+
		"\2VW\7\f\2\2W\17\3\2\2\2X\\\5\22\n\2Y[\7\t\2\2ZY\3\2\2\2[^\3\2\2\2\\Z"+
		"\3\2\2\2\\]\3\2\2\2]_\3\2\2\2^\\\3\2\2\2_`\5\34\17\2`\21\3\2\2\2ae\5\24"+
		"\13\2bd\7\t\2\2cb\3\2\2\2dg\3\2\2\2ec\3\2\2\2ef\3\2\2\2fh\3\2\2\2ge\3"+
		"\2\2\2hi\5\22\n\2iw\3\2\2\2jn\5\32\16\2km\7\t\2\2lk\3\2\2\2mp\3\2\2\2"+
		"nl\3\2\2\2no\3\2\2\2oq\3\2\2\2pn\3\2\2\2qr\5\22\n\2rw\3\2\2\2sw\5\24\13"+
		"\2tw\5\32\16\2uw\3\2\2\2va\3\2\2\2vj\3\2\2\2vs\3\2\2\2vt\3\2\2\2vu\3\2"+
		"\2\2w\23\3\2\2\2x|\5\26\f\2y|\5\30\r\2z|\7\26\2\2{x\3\2\2\2{y\3\2\2\2"+
		"{z\3\2\2\2|\25\3\2\2\2}\177\7\r\2\2~\u0080\7\16\2\2\177~\3\2\2\2\177\u0080"+
		"\3\2\2\2\u0080\27\3\2\2\2\u0081\u0082\7\27\2\2\u0082\31\3\2\2\2\u0083"+
		"\u0085\7\22\2\2\u0084\u0086\7\t\2\2\u0085\u0084\3\2\2\2\u0085\u0086\3"+
		"\2\2\2\u0086\u0087\3\2\2\2\u0087\u0089\5\22\n\2\u0088\u008a\7\t\2\2\u0089"+
		"\u0088\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008b\3\2\2\2\u008b\u008c\7\23"+
		"\2\2\u008c\33\3\2\2\2\u008d\u008e\t\2\2\2\u008e\35\3\2\2\2\23\"+\61\67"+
		"=BFLP\\env{\177\u0085\u0089";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}