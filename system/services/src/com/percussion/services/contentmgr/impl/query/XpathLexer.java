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

// $ANTLR 2.7.6 (2005-12-22): "jsr-xpath.g" -> "XpathLexer.java"$

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

public class XpathLexer extends antlr.CharScanner
      implements
         XpathTokenTypes,
         TokenStream
{
   public XpathLexer(InputStream in)
   {
      this(new ByteBuffer(in));
   }

   public XpathLexer(Reader in)
   {
      this(new CharBuffer(in));
   }

   public XpathLexer(InputBuffer ib)
   {
      this(new LexerSharedInputState(ib));
   }

   public XpathLexer(LexerSharedInputState state)
   {
      super(state);
      caseSensitiveLiterals = false;
      setCaseSensitive(false);
      literals = new Hashtable();
      literals.put(new ANTLRHashString("element", this), new Integer(4));
      literals.put(new ANTLRHashString("or", this), new Integer(14));
      literals.put(new ANTLRHashString("descending", this), new Integer(27));
      literals.put(new ANTLRHashString("by", this), new Integer(25));
      literals.put(new ANTLRHashString("and", this), new Integer(13));
      literals.put(new ANTLRHashString("not", this), new Integer(10));
      literals.put(new ANTLRHashString("ascending", this), new Integer(26));
      literals.put(new ANTLRHashString("order", this), new Integer(24));
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
                  case '/' :
                  {
                     mSLASH(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case ')' :
                  {
                     mCLOSE_PAREN(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '=' :
                  {
                     mEQ(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case ',' :
                  {
                     mCOMMA(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '!' :
                  {
                     mBANG(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '>' :
                  {
                     mGT(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '<' :
                  {
                     mLT(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '@' :
                  {
                     mAT(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '[' :
                  {
                     mLSQ(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case ']' :
                  {
                     mRSQ(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '*' :
                  {
                     mSTAR(true);
                     theRetToken = _returnToken;
                     break;
                  }
                  case '|' :
                  {
                     mOR(true);
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
                     if ((LA(1) == '(') && (LA(2) == ':'))
                     {
                        mML_COMMENT(true);
                        theRetToken = _returnToken;
                     }
                     else if ((LA(1) == '(') && (true))
                     {
                        mOPEN_PAREN(true);
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
               _ttype = testLiteralsTable(_ttype);
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
         _loop47 : do
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
                  break _loop47;
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
         _loop50 : do
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
                  break _loop50;
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
         _loop53 : do
         {
            if ((_tokenSet_0.member(LA(1))))
            {
               matchNot('\'');
            }
            else
            {
               break _loop53;
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

   public final void mSLASH(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = SLASH;
      int _saveIndex;

      match('/');
      {
         if ((LA(1) == '/'))
         {
            match('/');
            if (inputState.guessing == 0)
            {
               _ttype = DSLASH;
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

   public final void mBANG(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = BANG;
      int _saveIndex;

      match('!');
      {
         if ((LA(1) == '='))
         {
            match('=');
            if (inputState.guessing == 0)
            {
               _ttype = NE;
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

   public final void mLT(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = LT;
      int _saveIndex;

      match('<');
      {
         if ((LA(1) == '='))
         {
            match('=');
            if (inputState.guessing == 0)
            {
               _ttype = LE;
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

   public final void mAT(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = AT;
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

   public final void mLSQ(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = LSQ;
      int _saveIndex;

      match('[');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mRSQ(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = RSQ;
      int _saveIndex;

      match(']');
      if (_createToken && _token == null && _ttype != Token.SKIP)
      {
         _token = makeToken(_ttype);
         _token.setText(new String(text.getBuffer(), _begin, text.length()
               - _begin));
      }
      _returnToken = _token;
   }

   public final void mSTAR(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = STAR;
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

   public final void mOR(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = OR;
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
                        int _cnt75 = 0;
                        _loop75 : do
                        {
                           if (((LA(1) >= '0' && LA(1) <= '9')))
                           {
                              matchRange('0', '9');
                           }
                           else
                           {
                              if (_cnt75 >= 1)
                              {
                                 break _loop75;
                              }
                              else
                              {
                                 throw new NoViableAltForCharException(
                                       (char) LA(1), getFilename(), getLine(),
                                       getColumn());
                              }
                           }

                           _cnt75++;
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
                        boolean synPredMatched84 = false;
                        if ((((LA(1) >= '0' && LA(1) <= '9'))))
                        {
                           int _m84 = mark();
                           synPredMatched84 = true;
                           inputState.guessing++;
                           try
                           {
                              {
                                 {
                                    int _cnt82 = 0;
                                    _loop82 : do
                                    {
                                       if (((LA(1) >= '0' && LA(1) <= '9')))
                                       {
                                          matchRange('0', '9');
                                       }
                                       else
                                       {
                                          if (_cnt82 >= 1)
                                          {
                                             break _loop82;
                                          }
                                          else
                                          {
                                             throw new NoViableAltForCharException(
                                                   (char) LA(1), getFilename(),
                                                   getLine(), getColumn());
                                          }
                                       }

                                       _cnt82++;
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
                              synPredMatched84 = false;
                           }
                           rewind(_m84);
                           inputState.guessing--;
                        }
                        if (synPredMatched84)
                        {
                           {
                              int _cnt86 = 0;
                              _loop86 : do
                              {
                                 if (((LA(1) >= '0' && LA(1) <= '9')))
                                 {
                                    matchRange('0', '9');
                                 }
                                 else
                                 {
                                    if (_cnt86 >= 1)
                                    {
                                       break _loop86;
                                    }
                                    else
                                    {
                                       throw new NoViableAltForCharException(
                                             (char) LA(1), getFilename(),
                                             getLine(), getColumn());
                                    }
                                 }

                                 _cnt86++;
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
                        _loop89 : do
                        {
                           if (((LA(1) >= '0' && LA(1) <= '9')))
                           {
                              matchRange('0', '9');
                           }
                           else
                           {
                              break _loop89;
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
                              _loop93 : do
                              {
                                 if (((LA(1) >= '0' && LA(1) <= '9')))
                                 {
                                    matchRange('0', '9');
                                 }
                                 else
                                 {
                                    break _loop93;
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
         int _cnt101 = 0;
         _loop101 : do
         {
            if (((LA(1) >= '0' && LA(1) <= '9')))
            {
               matchRange('0', '9');
            }
            else
            {
               if (_cnt101 >= 1)
               {
                  break _loop101;
               }
               else
               {
                  throw new NoViableAltForCharException((char) LA(1),
                        getFilename(), getLine(), getColumn());
               }
            }

            _cnt101++;
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

   protected final void mN(boolean _createToken) throws RecognitionException,
         CharStreamException, TokenStreamException
   {
      int _ttype;
      Token _token = null;
      int _begin = text.length();
      _ttype = N;
      int _saveIndex;

      matchRange('0', '9');
      {
         _loop105 : do
         {
            if (((LA(1) >= '0' && LA(1) <= '9')))
            {
               matchRange('0', '9');
            }
            else
            {
               break _loop105;
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

      match("(:");
      {
         _loop111 : do
         {
            if ((LA(1) == '\r') && (LA(2) == '\n'))
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
            else if ((_tokenSet_2.member(LA(1)))
                  && ((LA(2) >= '\u0003' && LA(2) <= '\u00ff')))
            {
               {
                  match(_tokenSet_2);
               }
            }
            else if (((LA(1) == '*')) && (LA(2) != '/'))
            {
               match('*');
            }
            else if ((LA(1) == '\n'))
            {
               match('\n');
               if (inputState.guessing == 0)
               {
                  newline();
               }
            }
            else
            {
               break _loop111;
            }

         }
         while (true);
      }
      match(":)");
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
