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

// $ANTLR 2.7.6 (2005-12-22): "jsr-sql.g" -> "SqlParser.java"$

package com.percussion.services.contentmgr.impl.query;

import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.impl.BitSet;

import com.percussion.services.contentmgr.data.PSQuery;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeConjunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeFunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeVariable;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.query.Query;

public class SqlParser extends antlr.LLkParser implements SqlTokenTypes
{

   protected SqlParser(TokenBuffer tokenBuf, int k)
   {
      super(tokenBuf, k);
      tokenNames = _tokenNames;
   }

   public SqlParser(TokenBuffer tokenBuf)
   {
      this(tokenBuf, 4);
   }

   protected SqlParser(TokenStream lexer, int k)
   {
      super(lexer, k);
      tokenNames = _tokenNames;
   }

   public SqlParser(TokenStream lexer)
   {
      this(lexer, 4);
   }

   public SqlParser(ParserSharedInputState state)
   {
      super(state, 4);
      tokenNames = _tokenNames;
   }

   public final PSQuery start_rule() throws RecognitionException,
         TokenStreamException
   {
      PSQuery query;

      List<PSQueryNodeIdentifier> sl = null;
      List<PSQueryNodeIdentifier> tl = null;
      IPSQueryNode w = null;
      List<PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder>> o = null;
      query = new PSQuery(Query.SQL);

      try
      { // for error handling
         match(LITERAL_select);
         sl = select_list();
         match(LITERAL_from);
         tl = type_reference_list();
         {
            switch (LA(1))
            {
               case LITERAL_where :
               {
                  match(LITERAL_where);
                  w = where_condition();
                  break;
               }
               case EOF :
               case LITERAL_order :
               {
                  break;
               }
               default :
               {
                  throw new NoViableAltException(LT(1), getFilename());
               }
            }
         }
         {
            switch (LA(1))
            {
               case LITERAL_order :
               {
                  o = order_clause();
                  break;
               }
               case EOF :
               {
                  break;
               }
               default :
               {
                  throw new NoViableAltException(LT(1), getFilename());
               }
            }
         }
         if (inputState.guessing == 0)
         {

            query.setProjection(sl);
            query.setTypeConstraints(tl);
            query.setWhere(w);
            query.setSortFields(o);

         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_0);
         }
         else
         {
            throw ex;
         }
      }
      return query;
   }

   public final List<PSQueryNodeIdentifier> select_list()
         throws RecognitionException, TokenStreamException
   {
      List<PSQueryNodeIdentifier> ids;

      PSQueryNodeIdentifier p = null;
      ids = new ArrayList<>();

      try
      { // for error handling
         {
            switch (LA(1))
            {
               case IDENTIFIER :
               {
                  p = property();
                  if (inputState.guessing == 0)
                  {

                     ids.add(p);

                  }
                  {
                     _loop11 : do
                     {
                        if ((LA(1) == COMMA))
                        {
                           match(COMMA);
                           p = property();
                           if (inputState.guessing == 0)
                           {

                              ids.add(p);

                           }
                        }
                        else
                        {
                           break _loop11;
                        }

                     }
                     while (true);
                  }
                  break;
               }
               case ASTERISK :
               {
                  match(ASTERISK);
                  if (inputState.guessing == 0)
                  {

                     ids.add(new PSQueryNodeIdentifier("*"));

                  }
                  break;
               }
               default :
               {
                  throw new NoViableAltException(LT(1), getFilename());
               }
            }
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_1);
         }
         else
         {
            throw ex;
         }
      }
      return ids;
   }

   public final List<PSQueryNodeIdentifier> type_reference_list()
         throws RecognitionException, TokenStreamException
   {
      List<PSQueryNodeIdentifier> ids;

      PSQueryNodeIdentifier tn = null;
      ids = new ArrayList<>();

      try
      { // for error handling
         tn = type_name();
         if (inputState.guessing == 0)
         {

            ids.add(tn);

         }
         {
            _loop14 : do
            {
               if ((LA(1) == COMMA))
               {
                  match(COMMA);
                  tn = type_name();
                  if (inputState.guessing == 0)
                  {

                     ids.add(tn);

                  }
               }
               else
               {
                  break _loop14;
               }

            }
            while (true);
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_2);
         }
         else
         {
            throw ex;
         }
      }
      return ids;
   }

   public final IPSQueryNode where_condition() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode wnode;

      wnode = null;

      try
      { // for error handling
         wnode = condition();
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_3);
         }
         else
         {
            throw ex;
         }
      }
      return wnode;
   }

   public final List<PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder>> order_clause()
         throws RecognitionException, TokenStreamException
   {
      List<PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder>> solist;

      PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder> adef = null;
      solist = new ArrayList<>();

      try
      { // for error handling
         match(LITERAL_order);
         match(LITERAL_by);
         adef = sorted_def();
         if (inputState.guessing == 0)
         {

            solist.add(adef);

         }
         {
            _loop41 : do
            {
               if ((LA(1) == COMMA))
               {
                  match(COMMA);
                  adef = sorted_def();
                  if (inputState.guessing == 0)
                  {

                     solist.add(adef);

                  }
               }
               else
               {
                  break _loop41;
               }

            }
            while (true);
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_0);
         }
         else
         {
            throw ex;
         }
      }
      return solist;
   }

   public final PSQueryNodeIdentifier property() throws RecognitionException,
         TokenStreamException
   {
      PSQueryNodeIdentifier id;

      Token ident = null;

      String n = null, p = null;
      id = null;

      try
      { // for error handling
         ident = LT(1);
         match(IDENTIFIER);
         if (inputState.guessing == 0)
         {

            id = new PSQueryNodeIdentifier(ident.getText());

         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_4);
         }
         else
         {
            throw ex;
         }
      }
      return id;
   }

   public final PSQueryNodeIdentifier type_name() throws RecognitionException,
         TokenStreamException
   {
      PSQueryNodeIdentifier tn;

      Token id = null;

      String ns = null;
      tn = null;

      try
      { // for error handling
         id = LT(1);
         match(IDENTIFIER);
         if (inputState.guessing == 0)
         {

            tn = new PSQueryNodeIdentifier(id.getText());

         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_5);
         }
         else
         {
            throw ex;
         }
      }
      return tn;
   }

   public final IPSQueryNode condition() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode lnode;

      IPSQueryNode rnode = null;
      lnode = null;

      try
      { // for error handling
         lnode = logical_term();
         {
            _loop20 : do
            {
               if ((LA(1) == LITERAL_or))
               {
                  match(LITERAL_or);
                  rnode = logical_term();
                  if (inputState.guessing == 0)
                  {

                     lnode = new PSQueryNodeConjunction(lnode, rnode,
                           IPSQueryNode.Op.OR);

                  }
               }
               else
               {
                  break _loop20;
               }

            }
            while (true);
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_6);
         }
         else
         {
            throw ex;
         }
      }
      return lnode;
   }

   public final IPSQueryNode logical_term() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode lnode;

      IPSQueryNode rnode = null;
      lnode = null;

      try
      { // for error handling
         lnode = logical_factor();
         {
            _loop23 : do
            {
               if ((LA(1) == LITERAL_and))
               {
                  match(LITERAL_and);
                  rnode = logical_factor();
                  if (inputState.guessing == 0)
                  {

                     lnode = new PSQueryNodeConjunction(lnode, rnode,
                           IPSQueryNode.Op.AND);

                  }
               }
               else
               {
                  break _loop23;
               }

            }
            while (true);
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_7);
         }
         else
         {
            throw ex;
         }
      }
      return lnode;
   }

   public final IPSQueryNode logical_factor() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode lnode;

      Token n = null;

      IPSQueryNode rnode = null;
      IPSQueryNode arg = null;
      IPSQueryNode.Op op = null;
      PSQueryNodeIdentifier id = null;
      List params = null;
      lnode = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case OPEN_PAREN :
            {
               match(OPEN_PAREN);
               lnode = condition();
               match(CLOSE_PAREN);
               break;
            }
            case LITERAL_not :
            {
               match(LITERAL_not);
               rnode = logical_factor();
               if (inputState.guessing == 0)
               {

                  lnode = new PSQueryNodeConjunction(null,
                        (IPSQueryNode) rnode, IPSQueryNode.Op.NOT);

               }
               break;
            }
            default :
               boolean synPredMatched26 = false;
               if (((LA(1) == IDENTIFIER) && (LA(2) == OPEN_PAREN)))
               {
                  int _m26 = mark();
                  synPredMatched26 = true;
                  inputState.guessing++;
                  try
                  {
                     {
                        property();
                        match(OPEN_PAREN);
                     }
                  }
                  catch (RecognitionException pe)
                  {
                     synPredMatched26 = false;
                  }
                  rewind(_m26);
                  inputState.guessing--;
               }
               if (synPredMatched26)
               {
                  id = property();
                  match(OPEN_PAREN);
                  params = parameter_list();
                  match(CLOSE_PAREN);
                  if (inputState.guessing == 0)
                  {

                     lnode = new PSQueryNodeFunction(id.getName(), params);

                  }
               }
               else
               {
                  boolean synPredMatched28 = false;
                  if (((_tokenSet_8.member(LA(1)))
                        && (LA(2) == LITERAL_is || LA(2) == NUMBER) && ((LA(3) >= LITERAL_is && LA(3) <= LITERAL_null))))
                  {
                     int _m28 = mark();
                     synPredMatched28 = true;
                     inputState.guessing++;
                     try
                     {
                        {
                           argument();
                           match(LITERAL_is);
                        }
                     }
                     catch (RecognitionException pe)
                     {
                        synPredMatched28 = false;
                     }
                     rewind(_m28);
                     inputState.guessing--;
                  }
                  if (synPredMatched28)
                  {
                     arg = argument();
                     match(LITERAL_is);
                     {
                        switch (LA(1))
                        {
                           case LITERAL_not :
                           {
                              n = LT(1);
                              match(LITERAL_not);
                              break;
                           }
                           case LITERAL_null :
                           {
                              break;
                           }
                           default :
                           {
                              throw new NoViableAltException(LT(1),
                                    getFilename());
                           }
                        }
                     }
                     match(LITERAL_null);
                     if (inputState.guessing == 0)
                     {

                        if (n == null)
                        {
                           lnode = new PSQueryNodeComparison(arg,
                                 new PSQueryNodeValue(null), IPSQueryNode.Op.EQ);
                        }
                        else
                        {
                           lnode = new PSQueryNodeComparison(arg,
                                 new PSQueryNodeValue(null), IPSQueryNode.Op.NE);
                        }

                     }
                  }
                  else if ((_tokenSet_8.member(LA(1)))
                        && ((LA(2) >= NUMBER && LA(2) <= LITERAL_like))
                        && (_tokenSet_9.member(LA(3))))
                  {
                     arg = argument();
                     op = operator();
                     {
                        rnode = argument();
                     }
                     if (inputState.guessing == 0)
                     {

                        lnode = new PSQueryNodeComparison(arg, rnode, op);

                     }
                  }
                  else
                  {
                     throw new NoViableAltException(LT(1), getFilename());
                  }
               }
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_10);
         }
         else
         {
            throw ex;
         }
      }
      return lnode;
   }

   public final List parameter_list() throws RecognitionException,
         TokenStreamException
   {
      List rval;

      Object p = null;
      rval = new ArrayList();

      try
      { // for error handling
         p = parameter();
         if (inputState.guessing == 0)
         {

            rval.add(p);

         }
         {
            _loop34 : do
            {
               if ((LA(1) == COMMA))
               {
                  match(COMMA);
                  p = parameter();
                  if (inputState.guessing == 0)
                  {

                     rval.add(p);

                  }
               }
               else
               {
                  break _loop34;
               }

            }
            while (true);
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_11);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
   }

   public final IPSQueryNode argument() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode rval = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case IDENTIFIER :
            {
               rval = property();
               break;
            }
            case QUOTED_STRING :
            case VARIABLE :
            case PLUS :
            case MINUS :
            case NUMBER :
            {
               rval = value();
               break;
            }
            default :
            {
               throw new NoViableAltException(LT(1), getFilename());
            }
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_12);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
   }

   public final IPSQueryNode.Op operator() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode.Op op;

      op = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case EQ :
            {
               match(EQ);
               if (inputState.guessing == 0)
               {
                  op = IPSQueryNode.Op.EQ;
               }
               break;
            }
            case LT :
            {
               match(LT);
               if (inputState.guessing == 0)
               {
                  op = IPSQueryNode.Op.LT;
               }
               break;
            }
            case GT :
            {
               match(GT);
               if (inputState.guessing == 0)
               {
                  op = IPSQueryNode.Op.GT;
               }
               break;
            }
            case NOT_EQ :
            {
               match(NOT_EQ);
               if (inputState.guessing == 0)
               {
                  op = IPSQueryNode.Op.NE;
               }
               break;
            }
            case LE :
            {
               match(LE);
               if (inputState.guessing == 0)
               {
                  op = IPSQueryNode.Op.LE;
               }
               break;
            }
            case GE :
            {
               match(GE);
               if (inputState.guessing == 0)
               {
                  op = IPSQueryNode.Op.GE;
               }
               break;
            }
            case LITERAL_like :
            {
               match(LITERAL_like);
               if (inputState.guessing == 0)
               {
                  op = IPSQueryNode.Op.LIKE;
               }
               break;
            }
            default :
            {
               throw new NoViableAltException(LT(1), getFilename());
            }
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_8);
         }
         else
         {
            throw ex;
         }
      }
      return op;
   }

   public final IPSQueryNode value() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode v;

      Token qs = null;
      Token var = null;
      Token m = null;
      Token n = null;

      v = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case QUOTED_STRING :
            {
               qs = LT(1);
               match(QUOTED_STRING);
               if (inputState.guessing == 0)
               {

                  String temp = qs.getText();
                  // Strip outer quotes of any kind
                  if (temp.startsWith("'") && temp.endsWith("'"))
                  {
                     temp = temp.substring(1, temp.length() - 1);
                  }
                  v = new PSQueryNodeValue(temp);

               }
               break;
            }
            case VARIABLE :
            {
               var = LT(1);
               match(VARIABLE);
               if (inputState.guessing == 0)
               {

                  v = new PSQueryNodeVariable(var.getText());

               }
               break;
            }
            case PLUS :
            case MINUS :
            case NUMBER :
            {
               {
                  switch (LA(1))
                  {
                     case PLUS :
                     {
                        match(PLUS);
                        break;
                     }
                     case MINUS :
                     {
                        m = LT(1);
                        match(MINUS);
                        break;
                     }
                     case NUMBER :
                     {
                        break;
                     }
                     default :
                     {
                        throw new NoViableAltException(LT(1), getFilename());
                     }
                  }
               }
               n = LT(1);
               match(NUMBER);
               if (inputState.guessing == 0)
               {

                  String nstr = n.getText();
                  if (m != null)
                  {
                     nstr = "-" + nstr;
                  }
                  if (nstr.indexOf('.') >= 0)
                  {
                     v = new PSQueryNodeValue(new Double(nstr));
                  }
                  else
                  {
                     v = new PSQueryNodeValue(new Long(nstr));
                  }

               }
               break;
            }
            default :
            {
               throw new NoViableAltException(LT(1), getFilename());
            }
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_13);
         }
         else
         {
            throw ex;
         }
      }
      return v;
   }

   public final Object parameter() throws RecognitionException,
         TokenStreamException
   {
      Object rval;

      rval = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case QUOTED_STRING :
            case VARIABLE :
            case PLUS :
            case MINUS :
            case NUMBER :
            {
               rval = value();
               break;
            }
            case IDENTIFIER :
            {
               rval = property();
               break;
            }
            default :
            {
               throw new NoViableAltException(LT(1), getFilename());
            }
         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_14);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
   }

   public final PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder> sorted_def()
         throws RecognitionException, TokenStreamException
   {
      PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder> rval;

      PSQueryNodeIdentifier p = null;
      boolean asc = true;
      rval = null;

      try
      { // for error handling
         p = property();
         {
            switch (LA(1))
            {
               case LITERAL_asc :
               {
                  match(LITERAL_asc);
                  break;
               }
               case LITERAL_desc :
               {
                  match(LITERAL_desc);
                  if (inputState.guessing == 0)
                  {

                     asc = false;

                  }
                  break;
               }
               case EOF :
               case COMMA :
               {
                  break;
               }
               default :
               {
                  throw new NoViableAltException(LT(1), getFilename());
               }
            }
         }
         if (inputState.guessing == 0)
         {

            rval = new PSPair<>(p, asc
                  ? PSQuery.SortOrder.ASC
                  : PSQuery.SortOrder.DSC);

         }
      }
      catch (RecognitionException ex)
      {
         if (inputState.guessing == 0)
         {
            reportError(ex);
            recover(ex, _tokenSet_15);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
   }

   public static final String[] _tokenNames =
   {"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "\"select\"", "\"from\"",
         "\"where\"", "COMMA", "ASTERISK", "IDENTIFIER", "\"or\"", "\"and\"",
         "OPEN_PAREN", "CLOSE_PAREN", "\"is\"", "\"not\"", "\"null\"",
         "QUOTED_STRING", "VARIABLE", "PLUS", "MINUS", "NUMBER", "EQ", "LT",
         "GT", "NOT_EQ", "LE", "GE", "\"like\"", "\"order\"", "\"by\"",
         "\"asc\"", "\"desc\"", "SEMI", "AT_SIGN", "DIVIDE", "VERTBAR",
         "EXPONENT", "FLOAT_SUFFIX", "DOUBLE_QUOTE", "WS", "ML_COMMENT"};

   private static final long[] mk_tokenSet_0()
   {
      long[] data =
      {2L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

   private static final long[] mk_tokenSet_1()
   {
      long[] data =
      {32L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

   private static final long[] mk_tokenSet_2()
   {
      long[] data =
      {536870978L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

   private static final long[] mk_tokenSet_3()
   {
      long[] data =
      {536870914L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());

   private static final long[] mk_tokenSet_4()
   {
      long[] data =
      {7512030370L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());

   private static final long[] mk_tokenSet_5()
   {
      long[] data =
      {536871106L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());

   private static final long[] mk_tokenSet_6()
   {
      long[] data =
      {536879106L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());

   private static final long[] mk_tokenSet_7()
   {
      long[] data =
      {536880130L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());

   private static final long[] mk_tokenSet_8()
   {
      long[] data =
      {4063744L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());

   private static final long[] mk_tokenSet_9()
   {
      long[] data =
      {536740352L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());

   private static final long[] mk_tokenSet_10()
   {
      long[] data =
      {536882178L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());

   private static final long[] mk_tokenSet_11()
   {
      long[] data =
      {8192L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());

   private static final long[] mk_tokenSet_12()
   {
      long[] data =
      {1069575170L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());

   private static final long[] mk_tokenSet_13()
   {
      long[] data =
      {1069575298L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());

   private static final long[] mk_tokenSet_14()
   {
      long[] data =
      {8320L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());

   private static final long[] mk_tokenSet_15()
   {
      long[] data =
      {130L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());

}
