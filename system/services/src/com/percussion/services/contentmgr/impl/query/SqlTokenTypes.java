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

// $ANTLR 2.7.6 (2005-12-22): "jsr-sql.g" -> "SqlLexer.java"$

package com.percussion.services.contentmgr.impl.query;


public interface SqlTokenTypes
{
   int EOF = 1;

   int NULL_TREE_LOOKAHEAD = 3;

   int LITERAL_select = 4;

   int LITERAL_from = 5;

   int LITERAL_where = 6;

   int COMMA = 7;

   int ASTERISK = 8;

   int IDENTIFIER = 9;

   int LITERAL_or = 10;

   int LITERAL_and = 11;

   int OPEN_PAREN = 12;

   int CLOSE_PAREN = 13;

   int LITERAL_is = 14;

   int LITERAL_not = 15;

   int LITERAL_null = 16;

   int QUOTED_STRING = 17;

   int VARIABLE = 18;

   int PLUS = 19;

   int MINUS = 20;

   int NUMBER = 21;

   int EQ = 22;

   int LT = 23;

   int GT = 24;

   int NOT_EQ = 25;

   int LE = 26;

   int GE = 27;

   int LITERAL_like = 28;

   int LITERAL_order = 29;

   int LITERAL_by = 30;

   int LITERAL_asc = 31;

   int LITERAL_desc = 32;

   int SEMI = 33;

   int AT_SIGN = 34;

   int DIVIDE = 35;

   int VERTBAR = 36;

   int EXPONENT = 37;

   int FLOAT_SUFFIX = 38;

   int DOUBLE_QUOTE = 39;

   int WS = 40;

   int ML_COMMENT = 41;
}
