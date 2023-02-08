/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// $ANTLR 2.7.6 (2005-12-22): "SegmentAliasFieldGrammer.g" -> "SegmentAliasFieldLexer.java"$

package com.percussion.soln.segment.rx;
import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;
import antlr.NoViableAltForCharException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.collections.impl.BitSet;


public class SegmentAliasFieldLexer extends antlr.CharScanner implements SegmentAliasFieldParserTokenTypes, TokenStream
 {
public SegmentAliasFieldLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public SegmentAliasFieldLexer(Reader in) {
	this(new CharBuffer(in));
}
public SegmentAliasFieldLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
@SuppressWarnings("unchecked")
public SegmentAliasFieldLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
}

public Token nextToken() throws TokenStreamException {
	@SuppressWarnings("unused")
    Token theRetToken=null;
tryAgain:
	for (;;) {
		@SuppressWarnings("unused")
        Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '"':
				{
					mQUOTED(true);
					theRetToken=_returnToken;
					break;
				}
				case '\u0000':  case '\u0001':  case '\u0002':  case '\u0003':
				case '\u0004':  case '\u0005':  case '\u0006':  case '\u0007':
				case '\u0008':  case '\u000b':  case '\u000c':  case '\u000e':
				case '\u000f':  case '\u0010':  case '\u0011':  case '\u0012':
				case '\u0013':  case '\u0014':  case '\u0015':  case '\u0016':
				case '\u0017':  case '\u0018':  case '\u0019':  case '\u001a':
				case '\u001b':  case '\u001c':  case '\u001d':  case '\u001e':
				case '\u001f':  case '!':  case '#':  case '$':
				case '%':  case '&':  case '\'':  case '(':
				case ')':  case '*':  case '+':  case ',':
				case '-':  case '.':  case '/':  case '0':
				case '1':  case '2':  case '3':  case '4':
				case '5':  case '6':  case '7':  case '8':
				case '9':  case ':':  case ';':  case '<':
				case '=':  case '>':  case '?':  case '@':
				case 'A':  case 'B':  case 'C':  case 'D':
				case 'E':  case 'F':  case 'G':  case 'H':
				case 'I':  case 'J':  case 'K':  case 'L':
				case 'M':  case 'N':  case 'O':  case 'P':
				case 'Q':  case 'R':  case 'S':  case 'T':
				case 'U':  case 'V':  case 'W':  case 'X':
				case 'Y':  case 'Z':  case '[':  case '\\':
				case ']':  case '^':  case '_':  case '`':
				case 'a':  case 'b':  case 'c':  case 'd':
				case 'e':  case 'f':  case 'g':  case 'h':
				case 'i':  case 'j':  case 'k':  case 'l':
				case 'm':  case 'n':  case 'o':  case 'p':
				case 'q':  case 'r':  case 's':  case 't':
				case 'u':  case 'v':  case 'w':  case 'x':
				case 'y':  case 'z':  case '{':  case '|':
				case '}':  case '~':  case '\u007f':
				{
					mUNQUOTED(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\n':  case '\r':  case ' ':
				{
					mSEPARATOR(true);
					theRetToken=_returnToken;
					break;
				}
				default:
				{
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_ttype = testLiteralsTable(_ttype);
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mQUOTED(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUOTED;
		@SuppressWarnings("unused")
        int _saveIndex;
		
		match('"');
		{
		_loop8:
		do {
			// nongreedy exit test
			if ((LA(1)=='"') && (true)) break _loop8;
			if (((LA(1) >= '\u0000' && LA(1) <= '\u007f')) && ((LA(2) >= '\u0000' && LA(2) <= '\u007f'))) {
				matchNot(EOF_CHAR);
			}
			else {
				break _loop8;
			}
			
		} while (true);
		}
		match('"');
		
			  	// Strip the surrounding quotes
			  	String txt = getText(); 
			  	setText(txt.substring(1, txt.length() -1)); 
			
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mUNQUOTED(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = UNQUOTED;
		@SuppressWarnings("unused")
        int _saveIndex;
		
		{
		int _cnt12=0;
		_loop12:
		do {
			if ((_tokenSet_0.member(LA(1)))) {
				{
				match(_tokenSet_0);
				}
			}
			else {
				if ( _cnt12>=1 ) { break _loop12; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt12++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEPARATOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEPARATOR;
		@SuppressWarnings("unused")
        int _saveIndex;
		
		switch ( LA(1)) {
		case '\r':
		{
			match('\r');
			match('\n');
			break;
		}
		case '\n':
		{
			match('\n');
			break;
		}
		case '\t':
		{
			match('\t');
			break;
		}
		case ' ':
		{
			{
			int _cnt15=0;
			_loop15:
			do {
				if ((LA(1)==' ')) {
					match(' ');
				}
				else {
					if ( _cnt15>=1 ) { break _loop15; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt15++;
			} while (true);
			}
			_ttype = Token.SKIP;
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { -21474846209L, -1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	}
