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
