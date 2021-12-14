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

// $ANTLR 2.7.6 (2005-12-22): "jsr-xpath.g" -> "XpathLexer.java"$

package com.percussion.services.contentmgr.impl.query;


public interface XpathTokenTypes
{
   int EOF = 1;

   int NULL_TREE_LOOKAHEAD = 3;

   int ELEMENT = 4;

   int SLASH = 5;

   int LSQ = 6;

   int RSQ = 7;

   int DSLASH = 8;

   int IDENTIFIER = 9;

   int LITERAL_not = 10;

   int OPEN_PAREN = 11;

   int CLOSE_PAREN = 12;

   int LITERAL_and = 13;

   int LITERAL_or = 14;

   int STAR = 15;

   int COMMA = 16;

   int OR = 17;

   int EQ = 18;

   int LT = 19;

   int GT = 20;

   int NE = 21;

   int LE = 22;

   int GE = 23;

   int LITERAL_order = 24;

   int LITERAL_by = 25;

   int LITERAL_ascending = 26;

   int LITERAL_descending = 27;

   int QUOTED_STRING = 28;

   int VARIABLE = 29;

   int PLUS = 30;

   int MINUS = 31;

   int NUMBER = 32;

   int AT = 33;

   int BANG = 34;

   int EXPONENT = 35;

   int FLOAT_SUFFIX = 36;

   int N = 37;

   int WS = 38;

   int ML_COMMENT = 39;
}
