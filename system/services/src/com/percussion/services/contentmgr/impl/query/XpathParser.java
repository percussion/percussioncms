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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

// $ANTLR 2.7.6 (2005-12-22): "jsr-xpath.g" -> "XpathParser.java"$

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

public class XpathParser extends antlr.LLkParser implements XpathTokenTypes
{

   protected XpathParser(TokenBuffer tokenBuf, int k)
   {
      super(tokenBuf, k);
      tokenNames = _tokenNames;
   }

   public XpathParser(TokenBuffer tokenBuf)
   {
      this(tokenBuf, 4);
   }

   protected XpathParser(TokenStream lexer, int k)
   {
      super(lexer, k);
      tokenNames = _tokenNames;
   }

   public XpathParser(TokenStream lexer)
   {
      this(lexer, 4);
   }

   public XpathParser(ParserSharedInputState state)
   {
      super(state, 4);
      tokenNames = _tokenNames;
   }

   public final PSQuery start_rule() throws RecognitionException,
         TokenStreamException
   {
      PSQuery query;

      IPSQueryNode w = null;
      List<PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder>> o = null;
      PSPair<PSQueryNodeIdentifier, String> eldata = null;
      List<String> path = new ArrayList<>();
      String pp = null;
      query = new PSQuery(Query.XPATH);
      List al = null;

      try
      { // for error handling
         {
            _loop3 : do
            {
               if ((LA(1) == SLASH || LA(1) == DSLASH || LA(1) == IDENTIFIER))
               {
                  pp = path_element();
                  if (inputState.guessing == 0)
                  {

                     path.add(pp);

                  }
               }
               else
               {
                  break _loop3;
               }

            }
            while (true);
         }
         {
            switch (LA(1))
            {
               case ELEMENT :
               {
                  eldata = element_rule();
                  {
                     switch (LA(1))
                     {
                        case SLASH :
                        {
                           match(SLASH);
                           break;
                        }
                        case EOF :
                        case LSQ :
                        case OPEN_PAREN :
                        case LITERAL_order :
                        case AT :
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

                     query.getTypeConstraints().add(eldata.getFirst());
                     path.add(eldata.getSecond());

                  }
                  break;
               }
               case EOF :
               case LSQ :
               case OPEN_PAREN :
               case LITERAL_order :
               case AT :
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
               case LSQ :
               {
                  match(LSQ);
                  w = where_clause();
                  match(RSQ);
                  break;
               }
               case EOF :
               case OPEN_PAREN :
               case LITERAL_order :
               case AT :
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
               case OPEN_PAREN :
               case AT :
               {
                  al = attribute_list();
                  if (inputState.guessing == 0)
                  {

                     if (al != null)
                     {
                        query.getProjection().addAll(al);
                     }

                  }
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

            StringBuilder jcrpath = new StringBuilder();
            boolean first = true;
            for (String pathel : path)
            {
               if (pathel.equals("//"))
               {
                  jcrpath.append("/%/");
               }
               else if (pathel.equals("jcr:root"))
               {
                  // Ignore
               }
               else if (pathel.equals("/"))
               {
                  jcrpath.append("/");
               }
               else if (pathel.equals("**"))
               {
                  jcrpath.append("%");
               }
               else
               {
                  jcrpath.append(pathel);
               }
               first = false;
            }

            IPSQueryNode pathnode = null;
            if (jcrpath.length() > 0)
            {
               pathnode = new PSQueryNodeComparison(new PSQueryNodeIdentifier(
                     "jcr:path"), new PSQueryNodeValue(jcrpath.toString()),
                     IPSQueryNode.Op.LIKE);
            }
            if (w == null)
            {
               query.setWhere(pathnode);
            }
            else if (pathnode != null)
            {
               query.setWhere(new PSQueryNodeConjunction(pathnode, w,
                     IPSQueryNode.Op.AND));
            }
            else
            {
               query.setWhere(w);
            }

            query.setSortFields(o);
            if (query.getProjection().size() == 0)
            {
               query.getProjection().add(new PSQueryNodeIdentifier("*"));
            }

            if (query.getTypeConstraints().size() == 0)
            {
               query.getTypeConstraints().add(
                     new PSQueryNodeIdentifier("nt:base"));
            }

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

   public final String path_element() throws RecognitionException,
         TokenStreamException
   {
      String rval = null;

      Token s = null;
      Token d = null;
      Token f = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case SLASH :
            {
               s = LT(1);
               match(SLASH);
               if (inputState.guessing == 0)
               {

                  return "/";

               }
               break;
            }
            case DSLASH :
            {
               d = LT(1);
               match(DSLASH);
               if (inputState.guessing == 0)
               {

                  return "//";

               }
               break;
            }
            case IDENTIFIER :
            {
               f = LT(1);
               match(IDENTIFIER);
               if (inputState.guessing == 0)
               {

                  return f.getText();

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
            recover(ex, _tokenSet_1);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
   }

   public final PSPair<PSQueryNodeIdentifier, String> element_rule()
         throws RecognitionException, TokenStreamException
   {
      PSPair<PSQueryNodeIdentifier, String> eldata;

      Token prop = null;

      PSQueryNodeIdentifier type = null;
      eldata = new PSPair<>();

      try
      { // for error handling
         match(ELEMENT);
         match(OPEN_PAREN);
         {
            switch (LA(1))
            {
               case STAR :
               {
                  match(STAR);
                  break;
               }
               case IDENTIFIER :
               {
                  prop = LT(1);
                  match(IDENTIFIER);
                  break;
               }
               default :
               {
                  throw new NoViableAltException(LT(1), getFilename());
               }
            }
         }
         match(COMMA);
         {
            switch (LA(1))
            {
               case STAR :
               {
                  match(STAR);
                  break;
               }
               case IDENTIFIER :
               {
                  type = fqn();
                  break;
               }
               default :
               {
                  throw new NoViableAltException(LT(1), getFilename());
               }
            }
         }
         match(CLOSE_PAREN);
         if (inputState.guessing == 0)
         {

            if (prop == null)
            {
               eldata.setSecond("**");
            }
            else
            {
               eldata.setSecond(prop.getText());
            }
            if (type == null)
            {
               eldata.setFirst(new PSQueryNodeIdentifier("*"));
            }
            else
            {
               eldata.setFirst(type);
            }

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
      return eldata;
   }

   public final IPSQueryNode where_clause() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode n = null;

      try
      { // for error handling
         n = term();
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
      return n;
   }

   public final List attribute_list() throws RecognitionException,
         TokenStreamException
   {
      List rval;

      PSQueryNodeIdentifier a = null;
      rval = new ArrayList();

      try
      { // for error handling
         switch (LA(1))
         {
            case AT :
            {
               a = attribute();
               if (inputState.guessing == 0)
               {

                  rval.add(a);

               }
               break;
            }
            case OPEN_PAREN :
            {
               {
                  match(OPEN_PAREN);
                  a = attribute();
                  if (inputState.guessing == 0)
                  {

                     rval.add(a);

                  }
                  {
                     _loop31 : do
                     {
                        if ((LA(1) == OR))
                        {
                           match(OR);
                           a = attribute();
                           if (inputState.guessing == 0)
                           {

                              rval.add(a);

                           }
                        }
                        else
                        {
                           break _loop31;
                        }

                     }
                     while (true);
                  }
                  match(CLOSE_PAREN);
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
            recover(ex, _tokenSet_4);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
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
            _loop38 : do
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
                  break _loop38;
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

   public final IPSQueryNode term() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode n = null;

      IPSQueryNode right = null;

      try
      { // for error handling
         n = factor();
         {
            _loop19 : do
            {
               if ((LA(1) == LITERAL_or))
               {
                  match(LITERAL_or);
                  right = factor();
                  if (inputState.guessing == 0)
                  {

                     n = new PSQueryNodeConjunction(n, right,
                           IPSQueryNode.Op.OR);

                  }
               }
               else
               {
                  break _loop19;
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
            recover(ex, _tokenSet_5);
         }
         else
         {
            throw ex;
         }
      }
      return n;
   }

   public final IPSQueryNode full_expr() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode n = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case IDENTIFIER :
            case EQ :
            case LT :
            case GT :
            case NE :
            case LE :
            case GE :
            case AT :
            {
               n = expr();
               break;
            }
            case LITERAL_not :
            {
               match(LITERAL_not);
               n = expr();
               if (inputState.guessing == 0)
               {

                  n = new PSQueryNodeConjunction(null, n, IPSQueryNode.Op.NOT);

               }
               break;
            }
            case OPEN_PAREN :
            {
               match(OPEN_PAREN);
               n = term();
               match(CLOSE_PAREN);
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
            recover(ex, _tokenSet_6);
         }
         else
         {
            throw ex;
         }
      }
      return n;
   }

   public final IPSQueryNode expr() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode n = null;

      IPSQueryNode.Op o = null;
      IPSQueryNode v = null;
      PSQueryNodeIdentifier a = null;
      List<IPSQueryNode> params = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case IDENTIFIER :
            {
               a = fqn();
               match(OPEN_PAREN);
               params = parameter_list();
               match(CLOSE_PAREN);
               if (inputState.guessing == 0)
               {

                  n = new PSQueryNodeFunction(a.getName(), params);

               }
               break;
            }
            case EQ :
            case LT :
            case GT :
            case NE :
            case LE :
            case GE :
            case AT :
            {
               {
                  switch (LA(1))
                  {
                     case AT :
                     {
                        a = attribute();
                        {
                           switch (LA(1))
                           {
                              case EQ :
                              case LT :
                              case GT :
                              case NE :
                              case LE :
                              case GE :
                              {
                                 o = op();
                                 v = value();
                                 break;
                              }
                              case RSQ :
                              case CLOSE_PAREN :
                              case LITERAL_and :
                              case LITERAL_or :
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
                        break;
                     }
                     case EQ :
                     case LT :
                     case GT :
                     case NE :
                     case LE :
                     case GE :
                     {
                        o = op();
                        v = value();
                        a = attribute();
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

                  if (o != null)
                  {
                     n = new PSQueryNodeComparison(a, v, o);
                  }
                  else
                  {
                     n = new PSQueryNodeComparison(new PSQueryNodeValue(null),
                           a, IPSQueryNode.Op.NE);
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
            recover(ex, _tokenSet_6);
         }
         else
         {
            throw ex;
         }
      }
      return n;
   }

   public final IPSQueryNode factor() throws RecognitionException,
         TokenStreamException
   {
      IPSQueryNode n = null;

      IPSQueryNode right = null;

      try
      { // for error handling
         n = full_expr();
         {
            _loop16 : do
            {
               if ((LA(1) == LITERAL_and))
               {
                  match(LITERAL_and);
                  right = full_expr();
                  if (inputState.guessing == 0)
                  {

                     n = new PSQueryNodeConjunction(n, right,
                           IPSQueryNode.Op.AND);

                  }
               }
               else
               {
                  break _loop16;
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
      return n;
   }

   public final PSQueryNodeIdentifier fqn() throws RecognitionException,
         TokenStreamException
   {
      PSQueryNodeIdentifier id;

      Token name = null;

      id = null;

      try
      { // for error handling
         name = LT(1);
         match(IDENTIFIER);
         if (inputState.guessing == 0)
         {

            id = new PSQueryNodeIdentifier(name.getText());

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
      return id;
   }

   public final List parameter_list() throws RecognitionException,
         TokenStreamException
   {
      List rval;

      Object p = null;
      rval = new ArrayList();

      try
      { // for error handling
         p = value();
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
                  p = value();
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
            recover(ex, _tokenSet_9);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
   }

   public final PSQueryNodeIdentifier attribute() throws RecognitionException,
         TokenStreamException
   {
      PSQueryNodeIdentifier attr = null;

      try
      { // for error handling
         match(AT);
         attr = fqn();
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
      return attr;
   }

   public final IPSQueryNode.Op op() throws RecognitionException,
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
            case NE :
            {
               match(NE);
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
            recover(ex, _tokenSet_11);
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
      IPSQueryNode rval = null;

      Token qs = null;
      Token var = null;
      Token m = null;
      Token n = null;

      try
      { // for error handling
         switch (LA(1))
         {
            case AT :
            {
               rval = attribute();
               break;
            }
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
                  rval = new PSQueryNodeValue(temp);

               }
               break;
            }
            case VARIABLE :
            {
               var = LT(1);
               match(VARIABLE);
               if (inputState.guessing == 0)
               {

                  rval = new PSQueryNodeVariable(var.getText());

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
                     rval = new PSQueryNodeValue(new Double(nstr));
                  }
                  else
                  {
                     rval = new PSQueryNodeValue(new Long(nstr));
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
            recover(ex, _tokenSet_12);
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
         p = attribute();
         {
            switch (LA(1))
            {
               case LITERAL_ascending :
               {
                  match(LITERAL_ascending);
                  break;
               }
               case LITERAL_descending :
               {
                  match(LITERAL_descending);
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
            recover(ex, _tokenSet_13);
         }
         else
         {
            throw ex;
         }
      }
      return rval;
   }

   public static final String[] _tokenNames =
   {"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "\"element\"", "SLASH", "LSQ",
         "RSQ", "DSLASH", "IDENTIFIER", "\"not\"", "OPEN_PAREN", "CLOSE_PAREN",
         "\"and\"", "\"or\"", "STAR", "COMMA", "OR", "EQ", "LT", "GT", "NE",
         "LE", "GE", "\"order\"", "\"by\"", "\"ascending\"", "\"descending\"",
         "QUOTED_STRING", "VARIABLE", "PLUS", "MINUS", "NUMBER", "AT", "BANG",
         "EXPONENT", "FLOAT_SUFFIX", "N", "WS", "ML_COMMENT"};

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
      {8606714738L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

   private static final long[] mk_tokenSet_2()
   {
      long[] data =
      {8606713954L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

   private static final long[] mk_tokenSet_3()
   {
      long[] data =
      {128L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());

   private static final long[] mk_tokenSet_4()
   {
      long[] data =
      {16777218L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());

   private static final long[] mk_tokenSet_5()
   {
      long[] data =
      {4224L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());

   private static final long[] mk_tokenSet_6()
   {
      long[] data =
      {28800L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());

   private static final long[] mk_tokenSet_7()
   {
      long[] data =
      {20608L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());

   private static final long[] mk_tokenSet_8()
   {
      long[] data =
      {8824780930L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());

   private static final long[] mk_tokenSet_9()
   {
      long[] data =
      {4096L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());

   private static final long[] mk_tokenSet_10()
   {
      long[] data =
      {8824778882L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());

   private static final long[] mk_tokenSet_11()
   {
      long[] data =
      {16911433728L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());

   private static final long[] mk_tokenSet_12()
   {
      long[] data =
      {8590028928L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());

   private static final long[] mk_tokenSet_13()
   {
      long[] data =
      {65538L, 0L};
      return data;
   }

   public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());

}
