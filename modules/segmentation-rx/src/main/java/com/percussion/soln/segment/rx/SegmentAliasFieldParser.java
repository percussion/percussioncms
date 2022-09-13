// $ANTLR 2.7.6 (2005-12-22): "SegmentAliasFieldGrammer.g" -> "SegmentAliasFieldParser.java"$

package com.percussion.soln.segment.rx;
import java.util.List;
import java.util.Vector;

import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.impl.BitSet;

public class SegmentAliasFieldParser extends antlr.LLkParser       implements SegmentAliasFieldParserTokenTypes
 {

	private List<String> aliases = new Vector<String>(); 
	public List<String> getAliases() { return this.aliases; }

protected SegmentAliasFieldParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public SegmentAliasFieldParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected SegmentAliasFieldParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public SegmentAliasFieldParser(TokenStream lexer) {
  this(lexer,1);
}

public SegmentAliasFieldParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final void startRule() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			aliasList();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	public final void aliasList() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			aliasRule();
			{
			_loop4:
			do {
				if ((LA(1)==QUOTED||LA(1)==UNQUOTED)) {
					aliasRule();
				}
				else {
					break _loop4;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	public final void aliasRule() throws RecognitionException, TokenStreamException {
		
		Token  a = null;
		Token  b = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case QUOTED:
			{
				a = LT(1);
				match(QUOTED);
				aliases.add(a.getText());
				break;
			}
			case UNQUOTED:
			{
				b = LT(1);
				match(UNQUOTED);
				aliases.add(b.getText());
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"QUOTED",
		"UNQUOTED",
		"SEPARATOR"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 50L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	}
