/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

// $ANTLR 2.7.6 (2005-12-22): "jsr-sql.g" -> "SqlLexer.java"$

package com.percussion.services.contentmgr.impl.query;

import antlr.ANTLRHashString;
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

import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

public class SqlLexer extends antlr.CharScanner
      implements
         SqlTokenTypes,
         TokenStream
{
   public SqlLexer(InputStream in)
   {
      this(new ByteBuffer(in));
   }

   public SqlLexer(Reader in)
   {
      this(new CharBuffer(in));
   }

   public SqlLexer(InputBuffer ib)
   {
      this(new LexerSharedInputState(ib));
   }

   public SqlLexer(LexerSharedInputState state)
   {
      super(state);
      caseSensitiveLiterals = false;
      setCaseSensitive(false);
      literals = new Hashtable();
      literals.put(new ANTLRHashString("select", this), new Integer(4));
      literals.put(new ANTLRHashString("null", this), new Integer(16));
      literals.put(new ANTLRHashString("like", this), new Integer(28));
      literals.put(new ANTLRHashString("asc", this), new Integer(31));
      literals.put(new ANTLRHashString("or", this), new Integer(10));
      literals.put(new ANTLRHashString("from", this), new Integer(5));
      literals.put(new ANTLRHashString("desc", this), new Integer(32));
      literals.put(new ANTLRHashString("by", this), new Integer(30));
      literals.put(new ANTLRHashString("and", this), new Integer(11));
      literals.put(new ANTLRHashString("not", this), new Integer(15));
      literals.put(new ANTLRHashString("is", this), new Integer(14));
      literals.put(new ANTLRHashString("order", this), new Integer(29));
      literals.put(new ANTLRHashString("where", this), new Integer(6));
   }

   public Token nextToken() throws TokenStreamException
   {
      Token theRetToken = null;
      tryAgain : for (;;)
      {
         Token _token = null;
         int _ttype = Token.INVALID_TYPE;
         resetText();
         try
         { // for char stream error handling
            try
            { // for lexical error handling
               switch (LA(1))
               {
                  case 'a' :
                  case 'b' :
                  case 'c' :
                  case 'd' :
                  case 'e' :
                  case 'f' :
                  case 'g' :
                  case 'h' :
                  case 'i' :
                  case 'j' :
                  case 'k' :
                  case 'l' :
                  case 'm' :
                  case 'n' :
                  case 'o' :
                  case 'p' :
                  case 'q' :
                  case 'r' :
                  case 's' :
                  case 't' :
                  case 'u' :
                  case 'v' :
                  case 'w' :
                  case 'x' :
                  case 'y' :
                  case 'z' :
                  {
                     mIDENTIFIER(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case ':' :
                  {
                     mVARIABLE(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '\'' :
                  {
                     mQUOTED_STRING(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case ';' :
                  {
                     mSEMI(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case ',' :
                  {
                     mCOMMA(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '*' :
                  {
                     mASTERISK(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '@' :
                  {
                     mAT_SIGN(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '+' :
                  {
                     mPLUS(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '-' :
                  {
                     mMINUS(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '(' :
                  {
                     mOPEN_PAREN(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case ')' :
                  {
                     mCLOSE_PAREN(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '|' :
                  {
                     mVERTBAR(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '=' :
                  {
                     mEQ(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '!' :
                  case '<' :
                  case '^' :
                  {
                     mNOT_EQ(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '>' :
                  {
                     mGT(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '.' :
                  case '0' :
                  case '1' :
                  case '2' :
                  case '3' :
                  case '4' :
                  case '5' :
                  case '6' :
                  case '7' :
                  case '8' :
                  case '9' :
                  {
                     mNUMBER(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '"' :
                  {
                     mDOUBLE_QUOTE(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '\t' :
                  case '\n' :
                  case '\r' :
                  case ' ' :
                  {
                     mWS(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  default :
                     if ((LA(1) == '/') && (LA(2) == '*'))
                     {
                        mML_COMMENT(true);
                        theRetToken = _returnToken;
                     }
                     else if ((LA(1) == '/') && (true))
                     {
                        mDIVIDE(true);
                        theRetToken = _returnToken;
                     }
                     else
                     {
                        if (LA(1) == EOF_CHAR)
                        {
                           uponEOF();
                           _returnToken = makeToken(Token.EOF_TYPE);
                        }
                        else
                        {
                           throw new NoViableAltForCharException((char) LA(1),
                                 getFilename(), getLine(), getColumn());
                        }
                     }
               }
               if (_returnToken == null)
                  continue tryAgain; // found SKIP token
               _ttype = _returnToken.getType();
               _returnToken.setType(_ttype);
               return _returnToken;
            }
            catch (RecognitionException e)
            {
               throw new TokenStreamRecognitionException(e);
            }
         }
         catch (CharStreamException cse)
         {
            if (cse instanceof CharStreamIOException)
            {
               throw new TokenStreamIOException(
                     ((CharStreamIOException) cse).io);
            }
            else
            {
               throw new TokenStreamException(cse.getMessage());
            }
         }
      }
   }

   public final void mIDENTIFIER(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = IDENTIFIER;
      int _saveIndex;

      matchRange('a', 'z');
      {
         _loop46 : do
         {
            switch (LA(1))
            {
               case 'a' :
               case 'b' :
               case 'c' :
               case 'd' :
               case 'e' :
               case 'f' :
               case 'g' :
               case 'h' :
               case 'i' :
               case 'j' :
               case 'k' :
               case 'l' :
               case 'm' :
               case 'n' :
               case 'o' :
               case 'p' :
               case 'q' :
               case 'r' :
               case 's' :
               case 't' :
               case 'u' :
               case 'v' :
               case 'w' :
               case 'x' :
               case 'y' :
               case 'z' :
               {
                  matchRange('a', 'z');
                  break;
               }
               case '0' :
               case '1' :
               case '2' :
               case '3' :
               case '4' :
               case '5' :
               case '6' :
               case '7' :
               case '8' :
               case '9' :
               {
                  matchRange('0', '9');
                  break;
               }
               case ':' :
               {
                  match(':');
                  break;
               }
               case '_' :
               {
                  match('_');
                  break;
               }
               case '$' :
               {
                  match('$');
                  break;
               }
               case '#' :
               {
                  match('#');
                  break;
               }
               default :
               {
                  break _loop46;
               }
            }
         }
         while (true);
      }
      _ttype = testLiteralsTable(_ttype);
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mVARIABLE(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = VARIABLE;
      int _saveIndex;

      match(':');
      {
         _loop49 : do
         {
            switch (LA(1))
            {
               case 'a' :
               case 'b' :
               case 'c' :
               case 'd' :
               case 'e' :
               case 'f' :
               case 'g' :
               case 'h' :
               case 'i' :
               case 'j' :
               case 'k' :
               case 'l' :
               case 'm' :
               case 'n' :
               case 'o' :
               case 'p' :
               case 'q' :
               case 'r' :
               case 's' :
               case 't' :
               case 'u' :
               case 'v' :
               case 'w' :
               case 'x' :
               case 'y' :
               case 'z' :
               {
                  matchRange('a', 'z');
                  break;
               }
               case '0' :
               case '1' :
               case '2' :
               case '3' :
               case '4' :
               case '5' :
               case '6' :
               case '7' :
               case '8' :
               case '9' :
               {
                  matchRange('0', '9');
                  break;
               }
               case '_' :
               {
                  match('_');
                  break;
               }
               case '$' :
               {
                  match('$');
                  break;
               }
               case '#' :
               {
                  match('#');
                  break;
               }
               default :
               {
                  break _loop49;
               }
            }
         }
         while (true);
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mQUOTED_STRING(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = QUOTED_STRING;
      int _saveIndex;

      match('\'');
      {
         _loop52 : do
         {
            if ((_tokenSet_0.member(LA(1))))
            {
               matchNot('\'');
            }
            else
            {
               break _loop52;
            }

         }
         while (true);
      }
      match('\'');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mSEMI(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = SEMI;
      int _saveIndex;

      match(';');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mCOMMA(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = COMMA;
      int _saveIndex;

      match(',');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mASTERISK(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = ASTERISK;
      int _saveIndex;

      match('*');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mAT_SIGN(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = AT_SIGN;
      int _saveIndex;

      match('@');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mPLUS(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = PLUS;
      int _saveIndex;

      match('+');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mMINUS(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = MINUS;
      int _saveIndex;

      match('-');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mOPEN_PAREN(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = OPEN_PAREN;
      int _saveIndex;

      match('(');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mCLOSE_PAREN(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = CLOSE_PAREN;
      int _saveIndex;

      match(')');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mDIVIDE(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = DIVIDE;
      int _saveIndex;

      match('/');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mVERTBAR(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = VERTBAR;
      int _saveIndex;

      match('|');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mEQ(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = EQ;
      int _saveIndex;

      match('=');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mNOT_EQ(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = NOT_EQ;
      int _saveIndex;

      switch (LA(1))
      {
         case '<' :
         {
            match('<');
            if (inputState.guessing == 0)
            {
               _ttype = LT;
            }
            {
               switch (LA(1))
               {
                  case '>' :
                  {
                     {
                        match('>');
                        if (inputState.guessing == 0)
                        {
                           _ttype = NOT_EQ;
                        }
                     }
                     break;
                  }
                  case '=' :
                  {
                     {
                        match('=');
                        if (inputState.guessing == 0)
                        {
                           _ttype = LE;
                        }
                     }
                     break;
                  }
                  default :
                  {
                  }
               }
            }
            break;
         }
         case '!' :
         {
            match("!=");
            break;
         }
         case '^' :
         {
            match("^=");
            break;
         }
         default :
         {
            throw new NoViableAltForCharException((char) LA(1), getFilename(),
                  getLine(), getColumn());
         }
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mGT(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = GT;
      int _saveIndex;

      match('>');
      {
         if ((LA(1) == '='))
         {
            match('=');
            if (inputState.guessing == 0)
            {
               _ttype = GE;
            }
         }
         else
         {
         }

      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mNUMBER(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = NUMBER;
      int _saveIndex;
      boolean isDecimal = false;

      switch (LA(1))
      {
         case '.' :
         {
            match('.');
            {
               if (((LA(1) >= '0' && LA(1) <= '9')))
               {
                  {
                     {
                        int _cnt74 = 0;
                        _loop74 : do
                        {
                           if (((LA(1) >= '0' && LA(1) <= '9')))
                           {
                              matchRange('0', '9');
                           }
                           else
                           {
                              if (_cnt74 >= 1)
                              {
                                 break _loop74;
                              }
                              else
                              {
                                 throw new NoViableAltForCharException(
                                       (char) LA(1), getFilename(), getLine(),
                                       getColumn());
                              }
                           }

                           _cnt74++;
                        }
                        while (true);
                     }
                     {
                        if ((LA(1) == 'e'))
                        {
                           mEXPONENT(false);
                        }
                        else
                        {
                        }

                     }
                     {
                        if ((LA(1) == 'd' || LA(1) == 'f'))
                        {
                           mFLOAT_SUFFIX(false);
                        }
                        else
                        {
                        }

                     }
                  }
               }
               else
               {
               }

            }
            break;
         }
         case '0' :
         case '1' :
         case '2' :
         case '3' :
         case '4' :
         case '5' :
         case '6' :
         case '7' :
         case '8' :
         case '9' :
         {
            {
               switch (LA(1))
               {
                  case '0' :
                  {
                     match('0');
                     {
                        boolean synPredMatched83 = false;
                        if ((((LA(1) >= '0' && LA(1) <= '9'))))
                        {
                           int _m83 = mark();
                           synPredMatched83 = true;
                           inputState.guessing++;
                           try
                           {
                              {
                                 {
                                    int _cnt81 = 0;
                                    _loop81 : do
                                    {
                                       if (((LA(1) >= '0' && LA(1) <= '9')))
                                       {
                                          matchRange('0', '9');
                                       }
                                       else
                                       {
                                          if (_cnt81 >= 1)
                                          {
                                             break _loop81;
                                          }
                                          else
                                          {
                                             throw new NoViableAltForCharException(
                                                   (char) LA(1), getFilename(),
                                                   getLine(), getColumn());
                                          }
                                       }

                                       _cnt81++;
                                    }
                                    while (true);
                                 }
                                 {
                                    switch (LA(1))
                                    {
                                       case '.' :
                                       {
                                          match('.');
                                          break;
                                       }
                                       case 'e' :
                                       {
                                          mEXPONENT(false);
                                          break;
                                       }
                                       case 'd' :
                                       case 'f' :
                                       {
                                          mFLOAT_SUFFIX(false);
                                          break;
                                       }
                                       default :
                                       {
                                          throw new NoViableAltForCharException(
                                                (char) LA(1), getFilename(),
                                                getLine(), getColumn());
                                       }
                                    }
                                 }
                              }
                           }
                           catch (RecognitionException pe)
                           {
                              synPredMatched83 = false;
                           }
                           rewind(_m83);
                           inputState.guessing--;
                        }
                        if (synPredMatched83)
                        {
                           {
                              int _cnt85 = 0;
                              _loop85 : do
                              {
                                 if (((LA(1) >= '0' && LA(1) <= '9')))
                                 {
                                    matchRange('0', '9');
                                 }
                                 else
                                 {
                                    if (_cnt85 >= 1)
                                    {
                                       break _loop85;
                                    }
                                    else
                                    {
                                       throw new NoViableAltForCharException(
                                             (char) LA(1), getFilename(),
                                             getLine(), getColumn());
                                    }
                                 }

                                 _cnt85++;
                              }
                              while (true);
                           }
                        }
                        else
                        {
                        }

                     }
                     break;
                  }
                  case '1' :
                  case '2' :
                  case '3' :
                  case '4' :
                  case '5' :
                  case '6' :
                  case '7' :
                  case '8' :
                  case '9' :
                  {
                     {
                        matchRange('1', '9');
                     }
                     {
                        _loop88 : do
                        {
                           if (((LA(1) >= '0' && LA(1) <= '9')))
                           {
                              matchRange('0', '9');
                           }
                           else
                           {
                              break _loop88;
                           }

                        }
                        while (true);
                     }
                     if (inputState.guessing == 0)
                     {
                        isDecimal = true;
                     }
                     break;
                  }
                  default :
                  {
                     throw new NoViableAltForCharException((char) LA(1),
                           getFilename(), getLine(), getColumn());
                  }
               }
            }
            {
               if (((_tokenSet_1.member(LA(1)))) && (isDecimal))
               {
                  {
                     switch (LA(1))
                     {
                        case '.' :
                        {
                           match('.');
                           {
                              _loop92 : do
                              {
                                 if (((LA(1) >= '0' && LA(1) <= '9')))
                                 {
                                    matchRange('0', '9');
                                 }
                                 else
                                 {
                                    break _loop92;
                                 }

                              }
                              while (true);
                           }
                           {
                              if ((LA(1) == 'e'))
                              {
                                 mEXPONENT(false);
                              }
                              else
                              {
                              }

                           }
                           {
                              if ((LA(1) == 'd' || LA(1) == 'f'))
                              {
                                 mFLOAT_SUFFIX(false);
                              }
                              else
                              {
                              }

                           }
                           break;
                        }
                        case 'e' :
                        {
                           mEXPONENT(false);
                           {
                              if ((LA(1) == 'd' || LA(1) == 'f'))
                              {
                                 mFLOAT_SUFFIX(false);
                              }
                              else
                              {
                              }

                           }
                           break;
                        }
                        case 'd' :
                        case 'f' :
                        {
                           mFLOAT_SUFFIX(false);
                           break;
                        }
                        default :
                        {
                           throw new NoViableAltForCharException((char) LA(1),
                                 getFilename(), getLine(), getColumn());
                        }
                     }
                  }
               }
               else
               {
               }

            }
            break;
         }
         default :
         {
            throw new NoViableAltForCharException((char) LA(1), getFilename(),
                  getLine(), getColumn());
         }
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   protected final void mEXPONENT(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = EXPONENT;
      int _saveIndex;

      {
         match('e');
      }
      {
         switch (LA(1))
         {
            case '+' :
            {
               match('+');
               break;
            }
            case '-' :
            {
               match('-');
               break;
            }
            case '0' :
            case '1' :
            case '2' :
            case '3' :
            case '4' :
            case '5' :
            case '6' :
            case '7' :
            case '8' :
            case '9' :
            {
               break;
            }
            default :
            {
               throw new NoViableAltForCharException((char) LA(1),
                     getFilename(), getLine(), getColumn());
            }
         }
      }
      {
         int _cnt100 = 0;
         _loop100 : do
         {
            if (((LA(1) >= '0' && LA(1) <= '9')))
            {
               matchRange('0', '9');
            }
            else
            {
               if (_cnt100 >= 1)
               {
                  break _loop100;
               }
               else
               {
                  throw new NoViableAltForCharException((char) LA(1),
                        getFilename(), getLine(), getColumn());
               }
            }

            _cnt100++;
         }
         while (true);
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   protected final void mFLOAT_SUFFIX(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = FLOAT_SUFFIX;
      int _saveIndex;

      switch (LA(1))
      {
         case 'f' :
         {
            match('f');
            break;
         }
         case 'd' :
         {
            match('d');
            break;
         }
         default :
         {
            throw new NoViableAltForCharException((char) LA(1), getFilename(),
                  getLine(), getColumn());
         }
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mDOUBLE_QUOTE(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = DOUBLE_QUOTE;
      int _saveIndex;

      match('"');
      if (inputState.guessing == 0)
      {
         _ttype = Token.SKIP;
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mWS(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = WS;
      int _saveIndex;

      {
         switch (LA(1))
         {
            case ' ' :
            {
               match(' ');
               break;
            }
            case '\t' :
            {
               match('\t');
               break;
            }
            case '\n' :
            {
               match('\n');
               if (inputState.guessing == 0)
               {
                  newline();
               }
               break;
            }
            default :
               if ((LA(1) == '\r') && (LA(2) == '\n'))
               {
                  match('\r');
                  match('\n');
                  if (inputState.guessing == 0)
                  {
                     newline();
                  }
               }
               else if ((LA(1) == '\r') && (true))
               {
                  match('\r');
                  if (inputState.guessing == 0)
                  {
                     newline();
                  }
               }
               else
               {
                  throw new NoViableAltForCharException((char) LA(1),
                        getFilename(), getLine(), getColumn());
               }
         }
      }
      if (inputState.guessing == 0)
      {
         _ttype = Token.SKIP;
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mML_COMMENT(boolean _createToken)
         throws RecognitionException, CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = ML_COMMENT;
      int _saveIndex;

      match("/*");
      {
         _loop108 : do
         {
            if (((LA(1) == '*') && ((LA(2) >= '\u0003' && LA(2) <= '\u00ff')))
                  && (LA(2) != '/'))
            {
               match('*');
            }
            else if ((LA(1) == '\r') && (LA(2) == '\n'))
            {
               match('\r');
               match('\n');
               if (inputState.guessing == 0)
               {
                  newline();
               }
            }
            else if ((LA(1) == '\r')
                  && ((LA(2) >= '\u0003' && LA(2) <= '\u00ff')))
            {
               match('\r');
               if (inputState.guessing == 0)
               {
                  newline();
               }
            }
            else if ((LA(1) == '\n'))
            {
               match('\n');
               if (inputState.guessing == 0)
               {
                  newline();
               }
            }
            else if ((_tokenSet_2.member(LA(1))))
            {
               {
                  match(_tokenSet_2);
               }
            }
            else
            {
               break _loop108;
            }

         }
         while (true);
      }
      match("*/");
      if (inputState.guessing == 0)
      {
         _ttype = Token.SKIP;
      }
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   private static final long[] mk_tokenSet_0()
   {
      long[] data = new long[8];
      data[0] = -549755813896L;
      for (int i = 1; i <= 3; i++)
      {
         data[i] = -1L;
      }
      return data;
   }

   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

   private static final long[] mk_tokenSet_1()
   {
      long[] data =
      {70368744177664L, 481036337152L, 0L, 0L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

   private static final long[] mk_tokenSet_2()
   {
      long[] data = new long[8];
      data[0] = -4398046520328L;
      for (int i = 1; i <= 3; i++)
      {
         data[i] = -1L;
      }
      return data;
   }

   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

}
