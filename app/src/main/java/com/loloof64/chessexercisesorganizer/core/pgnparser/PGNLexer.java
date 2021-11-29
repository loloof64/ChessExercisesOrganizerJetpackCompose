// Generated from /home/laurent/StudioProjects/ChessExercisesOrganizerJetpackCompose/app/src/main/java/com/loloof64/chessexercisesorganizer/core/pgnparser/PGN.g4 by ANTLR 4.9.2
package com.loloof64.chessexercisesorganizer.core.pgnparser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PGNLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"WHITE_WINS", "BLACK_WINS", "DRAWN_GAME", "REST_OF_LINE_COMMENT", "BRACE_COMMENT", 
			"ESCAPE", "SPACE", "SIMPLE_SPACE", "NEWLINE", "STRING", "INTEGER", "PERIOD", 
			"ASTERISK", "LEFT_BRACKET", "RIGHT_BRACKET", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS", 
			"LEFT_ANGLE_BRACKET", "RIGHT_ANGLE_BRACKET", "NUMERIC_ANNOTATION_GLYPH", 
			"SYMBOL", "SUFFIX_ANNOTATION", "UNEXPECTED_CHAR"
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


	public PGNLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "PGN.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 5:
			return ESCAPE_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean ESCAPE_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return getCharPositionInLine() == 0;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\31\u009f\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2"+
		"\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\7"+
		"\5D\n\5\f\5\16\5G\13\5\3\5\3\5\3\6\3\6\7\6M\n\6\f\6\16\6P\13\6\3\6\3\6"+
		"\3\6\3\6\3\7\3\7\3\7\7\7Y\n\7\f\7\16\7\\\13\7\3\7\3\7\3\b\3\b\5\bb\n\b"+
		"\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\7\13n\n\13\f\13\16\13q"+
		"\13\13\3\13\3\13\3\f\6\fv\n\f\r\f\16\fw\3\r\6\r{\n\r\r\r\16\r|\3\16\3"+
		"\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3"+
		"\25\6\25\u008f\n\25\r\25\16\25\u0090\3\26\3\26\7\26\u0095\n\26\f\26\16"+
		"\26\u0098\13\26\3\27\3\27\5\27\u009c\n\27\3\30\3\30\2\2\31\3\3\5\4\7\5"+
		"\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23"+
		"%\24\'\25)\26+\27-\30/\31\3\2\n\4\2\f\f\17\17\3\2\177\177\4\2\13\13\""+
		"\"\4\2$$^^\3\2\62;\5\2\62;C\\c|\n\2%%--//\62<??C\\aac|\4\2##AA\2\u00aa"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2"+
		"\2\3\61\3\2\2\2\5\65\3\2\2\2\79\3\2\2\2\tA\3\2\2\2\13J\3\2\2\2\rU\3\2"+
		"\2\2\17a\3\2\2\2\21c\3\2\2\2\23e\3\2\2\2\25g\3\2\2\2\27u\3\2\2\2\31z\3"+
		"\2\2\2\33~\3\2\2\2\35\u0080\3\2\2\2\37\u0082\3\2\2\2!\u0084\3\2\2\2#\u0086"+
		"\3\2\2\2%\u0088\3\2\2\2\'\u008a\3\2\2\2)\u008c\3\2\2\2+\u0092\3\2\2\2"+
		"-\u0099\3\2\2\2/\u009d\3\2\2\2\61\62\7\63\2\2\62\63\7/\2\2\63\64\7\62"+
		"\2\2\64\4\3\2\2\2\65\66\7\62\2\2\66\67\7/\2\2\678\7\63\2\28\6\3\2\2\2"+
		"9:\7\63\2\2:;\7\61\2\2;<\7\64\2\2<=\7/\2\2=>\7\63\2\2>?\7\61\2\2?@\7\64"+
		"\2\2@\b\3\2\2\2AE\7=\2\2BD\n\2\2\2CB\3\2\2\2DG\3\2\2\2EC\3\2\2\2EF\3\2"+
		"\2\2FH\3\2\2\2GE\3\2\2\2HI\b\5\2\2I\n\3\2\2\2JN\7}\2\2KM\n\3\2\2LK\3\2"+
		"\2\2MP\3\2\2\2NL\3\2\2\2NO\3\2\2\2OQ\3\2\2\2PN\3\2\2\2QR\7\177\2\2RS\3"+
		"\2\2\2ST\b\6\2\2T\f\3\2\2\2UV\6\7\2\2VZ\7\'\2\2WY\n\2\2\2XW\3\2\2\2Y\\"+
		"\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2[]\3\2\2\2\\Z\3\2\2\2]^\b\7\2\2^\16\3\2\2"+
		"\2_b\5\21\t\2`b\5\23\n\2a_\3\2\2\2a`\3\2\2\2b\20\3\2\2\2cd\t\4\2\2d\22"+
		"\3\2\2\2ef\t\2\2\2f\24\3\2\2\2go\7$\2\2hi\7^\2\2in\7^\2\2jk\7^\2\2kn\7"+
		"$\2\2ln\n\5\2\2mh\3\2\2\2mj\3\2\2\2ml\3\2\2\2nq\3\2\2\2om\3\2\2\2op\3"+
		"\2\2\2pr\3\2\2\2qo\3\2\2\2rs\7$\2\2s\26\3\2\2\2tv\t\6\2\2ut\3\2\2\2vw"+
		"\3\2\2\2wu\3\2\2\2wx\3\2\2\2x\30\3\2\2\2y{\7\60\2\2zy\3\2\2\2{|\3\2\2"+
		"\2|z\3\2\2\2|}\3\2\2\2}\32\3\2\2\2~\177\7,\2\2\177\34\3\2\2\2\u0080\u0081"+
		"\7]\2\2\u0081\36\3\2\2\2\u0082\u0083\7_\2\2\u0083 \3\2\2\2\u0084\u0085"+
		"\7*\2\2\u0085\"\3\2\2\2\u0086\u0087\7+\2\2\u0087$\3\2\2\2\u0088\u0089"+
		"\7>\2\2\u0089&\3\2\2\2\u008a\u008b\7@\2\2\u008b(\3\2\2\2\u008c\u008e\7"+
		"&\2\2\u008d\u008f\t\6\2\2\u008e\u008d\3\2\2\2\u008f\u0090\3\2\2\2\u0090"+
		"\u008e\3\2\2\2\u0090\u0091\3\2\2\2\u0091*\3\2\2\2\u0092\u0096\t\7\2\2"+
		"\u0093\u0095\t\b\2\2\u0094\u0093\3\2\2\2\u0095\u0098\3\2\2\2\u0096\u0094"+
		"\3\2\2\2\u0096\u0097\3\2\2\2\u0097,\3\2\2\2\u0098\u0096\3\2\2\2\u0099"+
		"\u009b\t\t\2\2\u009a\u009c\t\t\2\2\u009b\u009a\3\2\2\2\u009b\u009c\3\2"+
		"\2\2\u009c.\3\2\2\2\u009d\u009e\13\2\2\2\u009e\60\3\2\2\2\16\2ENZamow"+
		"|\u0090\u0096\u009b\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}