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
