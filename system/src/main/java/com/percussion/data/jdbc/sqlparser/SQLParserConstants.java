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
package com.percussion.data.jdbc.sqlparser;

public interface SQLParserConstants {

  int EOF = 0;
  int ALetter = 6;
  int ADigit = 7;
  int PathComponent = 8;
  int FilePath = 9;
  int FileSpecification = 10;
  int ABSOLUTE = 11;
  int ACTION = 12;
  int ADD = 13;
  int ALL = 14;
  int ALLOCATE = 15;
  int ALTER = 16;
  int AND = 17;
  int ANY = 18;
  int ARE = 19;
  int AS = 20;
  int ASC = 21;
  int ASSERTION = 22;
  int AT = 23;
  int AUTHORIZATION = 24;
  int AVG = 25;
  int BEGIN = 26;
  int BETWEEN = 27;
  int BIT = 28;
  int BIT_LENGTH = 29;
  int BOTH = 30;
  int BY = 31;
  int CASCADE = 32;
  int CASCADED = 33;
  int CASE = 34;
  int CAST = 35;
  int CATALOG = 36;
  int CHARACTER = 37;
  int CHARACTER_LENGTH = 38;
  int CHECK = 39;
  int CLOSE = 40;
  int COALESCE = 41;
  int COLLATE = 42;
  int COLLATION = 43;
  int COLUMN = 44;
  int COMMIT = 45;
  int CONNECT = 46;
  int CONNECTION = 47;
  int CONSTRAINT = 48;
  int CONSTRAINTS = 49;
  int CONTINUE = 50;
  int CONVERT = 51;
  int CORRESPONDING = 52;
  int COUNT = 53;
  int CREATE = 54;
  int CROSS = 55;
  int CURRENT = 56;
  int CURRENT_DATE = 57;
  int CURRENT_TIME = 58;
  int CURRENT_TIMESTAMP = 59;
  int CURRENT_USER = 60;
  int CURSOR = 61;
  int DATE = 62;
  int DAY = 63;
  int DEALLOCATE = 64;
  int DEC = 65;
  int DECIMAL = 66;
  int DECLARE = 67;
  int DIRECTORY = 68;
  int KEYWORD_DEFAULT = 69;
  int DEFERRABLE = 70;
  int DEFERRED = 71;
  int DELETE = 72;
  int DESC = 73;
  int DESCRIBE = 74;
  int DESCRIPTOR = 75;
  int DIAGNOSTICS = 76;
  int DISCONNECT = 77;
  int DISTINCT = 78;
  int DOMAIN = 79;
  int DOUBLE = 80;
  int DROP = 81;
  int ELSE = 82;
  int END = 83;
  int END_EXEC = 84;
  int ESCAPE = 85;
  int EXCEPT = 86;
  int EXCEPTION = 87;
  int EXEC = 88;
  int EXECUTE = 89;
  int EXISTS = 90;
  int EXTERNAL = 91;
  int EXTRACT = 92;
  int FALSE = 93;
  int FETCH = 94;
  int FIRST = 95;
  int FLOAT = 96;
  int FOR = 97;
  int FOREIGN = 98;
  int FOUND = 99;
  int FROM = 100;
  int FULL = 101;
  int GET = 102;
  int GLOBAL = 103;
  int GO = 104;
  int GOTO = 105;
  int GRANT = 106;
  int GROUP = 107;
  int HAVING = 108;
  int HOUR = 109;
  int IDENTITY = 110;
  int IMMEDIATE = 111;
  int IN = 112;
  int INDICATOR = 113;
  int INITIALLY = 114;
  int INNER = 115;
  int INPUT = 116;
  int INSENSITIVE = 117;
  int INSERT = 118;
  int INT = 119;
  int INTEGER = 120;
  int INTERSECT = 121;
  int INTERVAL = 122;
  int INTO = 123;
  int IS = 124;
  int ISOLATION = 125;
  int JOIN = 126;
  int KEY = 127;
  int LANGUAGE = 128;
  int LAST = 129;
  int LEADING = 130;
  int LEFT = 131;
  int LEVEL = 132;
  int LIKE = 133;
  int LOCAL = 134;
  int LOWER = 135;
  int MATCH = 136;
  int MAX = 137;
  int MIN = 138;
  int MINUTE = 139;
  int MODULE = 140;
  int MONTH = 141;
  int NAMES = 142;
  int NATIONAL = 143;
  int NATURAL = 144;
  int NCHAR = 145;
  int NEXT = 146;
  int NO = 147;
  int NOT = 148;
  int NULL = 149;
  int NULLIF = 150;
  int NUMERIC = 151;
  int OCTET_LENGTH = 152;
  int OF = 153;
  int ON = 154;
  int ONLY = 155;
  int OPEN = 156;
  int OPTION = 157;
  int OR = 158;
  int ORDER = 159;
  int OUTER = 160;
  int OUTPUT = 161;
  int OVERLAPS = 162;
  int PAD = 163;
  int PARTIAL = 164;
  int POSITION = 165;
  int PRECISION = 166;
  int PREPARE = 167;
  int PRESERVE = 168;
  int PRIMARY = 169;
  int PRIOR = 170;
  int PRIVILEGES = 171;
  int PROCEDURE = 172;
  int PUBLIC = 173;
  int READ = 174;
  int REAL = 175;
  int REFERENCES = 176;
  int RELATIVE = 177;
  int RESTRICT = 178;
  int REVOKE = 179;
  int RIGHT = 180;
  int ROLLBACK = 181;
  int ROWS = 182;
  int SCHEMA = 183;
  int SCROLL = 184;
  int SECOND = 185;
  int SECTION = 186;
  int SELECT = 187;
  int SESSION = 188;
  int SESSION_USER = 189;
  int SET = 190;
  int SIZE = 191;
  int SMALLINT = 192;
  int SOME = 193;
  int SPACE = 194;
  int SQL = 195;
  int SQLCODE = 196;
  int SQLERROR = 197;
  int SQLSTATE = 198;
  int SUBSTRING = 199;
  int SUM = 200;
  int SYSTEM_TIME = 201;
  int SYSTEM_USER = 202;
  int TABLE = 203;
  int TEMPORARY = 204;
  int THEN = 205;
  int TIME = 206;
  int TIMESTAMP = 207;
  int TIMEZONE_HOUR = 208;
  int TIMEZONE_MINUTE = 209;
  int TO = 210;
  int TRAILING = 211;
  int TRANSACTION = 212;
  int TRANSLATE = 213;
  int TRANSLATION = 214;
  int TRIM = 215;
  int TRUE = 216;
  int UNION = 217;
  int UNIQUE = 218;
  int UNKNOWN = 219;
  int UPDATE = 220;
  int UPPER = 221;
  int USAGE = 222;
  int USER = 223;
  int USING = 224;
  int VALUE = 225;
  int VALUES = 226;
  int VARCHAR = 227;
  int VARYING = 228;
  int VIEW = 229;
  int WHEN = 230;
  int WHENEVER = 231;
  int WHERE = 232;
  int WITH = 233;
  int WORK = 234;
  int WRITE = 235;
  int YEAR = 236;
  int ZONE = 237;
  int QuoteSymbol = 238;
  int NonQuoteCharacter = 239;
  int CharacterRepresentation = 240;
  int Bit = 241;
  int Letter = 242;
  int Digit = 243;
  int Hexit = 244;
  int AtSign = 245;
  int Identifier = 246;
  int DriveSpec = 247;
  int ParameterName = 248;
  int UnsignedInteger = 249;
  int SignedInteger = 250;
  int ExactNumericLiteral = 251;
  int ApproximateNumericLiteral = 252;
  int FractionalPart = 253;
  int Sign = 254;
  int HexStringLiteral = 255;
  int BitStringLiteral = 256;
  int CharacterStringLiteral = 257;
  int NotEqualsOperator = 258;
  int GreaterThanOrEqualsOperator = 259;
  int LessThanOrEqualsOperator = 260;
  int ConcatenationOperator = 261;
  int DoublePeriod = 262;
  int LessThanOperator = 263;
  int EqualsOperator = 264;
  int GreaterThanOperator = 265;
  int DoubleQuote = 266;
  int Percent = 267;
  int Ampersand = 268;
  int Quote = 269;
  int LeftParen = 270;
  int RightParen = 271;
  int Asterisk = 272;
  int PlusSign = 273;
  int Comma = 274;
  int MinusSign = 275;
  int Period = 276;
  int Solidus = 277;
  int Colon = 278;
  int Semicolon = 279;
  int QuestionMark = 280;
  int LeftBracket = 281;
  int RightBracket = 282;
  int Underscore = 283;
  int VerticalBar = 284;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "<ALetter>",
    "<ADigit>",
    "<PathComponent>",
    "<FilePath>",
    "<FileSpecification>",
    "\"ABSOLUTE\"",
    "\"ACTION\"",
    "\"ADD\"",
    "\"ALL\"",
    "\"ALLOCATE\"",
    "\"ALTER\"",
    "\"AND\"",
    "\"ANY\"",
    "\"ARE\"",
    "\"AS\"",
    "\"ASC\"",
    "\"ASSERTION\"",
    "\"AT\"",
    "\"AUTHORIZATION\"",
    "\"AVG\"",
    "\"BEGIN\"",
    "\"BETWEEN\"",
    "\"BIT\"",
    "\"BIT_LENGTH\"",
    "\"BOTH\"",
    "\"BY\"",
    "\"CASCADE\"",
    "\"CASCADED\"",
    "\"CASE\"",
    "\"CAST\"",
    "\"CATALOG\"",
    "<CHARACTER>",
    "<CHARACTER_LENGTH>",
    "\"CHECK\"",
    "\"CLOSE\"",
    "\"COALESCE\"",
    "\"COLLATE\"",
    "\"COLLATION\"",
    "\"COLUMN\"",
    "\"COMMIT\"",
    "\"CONNECT\"",
    "\"CONNECTION\"",
    "\"CONSTRAINT\"",
    "\"CONSTRAINTS\"",
    "\"CONTINUE\"",
    "\"CONVERT\"",
    "\"CORRESPONDING\"",
    "\"COUNT\"",
    "\"CREATE\"",
    "\"CROSS\"",
    "\"CURRENT\"",
    "\"CURRENT_DATE\"",
    "\"CURRENT_TIME\"",
    "\"CURRENT_TIMESTAMP\"",
    "\"CURRENT_USER\"",
    "\"CURSOR\"",
    "\"DATE\"",
    "\"DAY\"",
    "\"DEALLOCATE\"",
    "\"DEC\"",
    "\"DECIMAL\"",
    "\"DECLARE\"",
    "<DIRECTORY>",
    "\"DEFAULT\"",
    "\"DEFERRABLE\"",
    "\"DEFERRED\"",
    "\"DELETE\"",
    "\"DESC\"",
    "\"DESCRIBE\"",
    "\"DESCRIPTOR\"",
    "\"DIAGNOSTICS\"",
    "\"DISCONNECT\"",
    "\"DISTINCT\"",
    "\"DOMAIN\"",
    "\"DOUBLE\"",
    "\"DROP\"",
    "\"ELSE\"",
    "\"END\"",
    "\"END-EXEC\"",
    "\"ESCAPE\"",
    "\"EXCEPT\"",
    "\"EXCEPTION\"",
    "\"EXEC\"",
    "\"EXECUTE\"",
    "\"EXISTS\"",
    "\"EXTERNAL\"",
    "\"EXTRACT\"",
    "\"FALSE\"",
    "\"FETCH\"",
    "\"FIRST\"",
    "\"FLOAT\"",
    "\"FOR\"",
    "\"FOREIGN\"",
    "\"FOUND\"",
    "\"FROM\"",
    "\"FULL\"",
    "\"GET\"",
    "\"GLOBAL\"",
    "\"GO\"",
    "\"GOTO\"",
    "\"GRANT\"",
    "\"GROUP\"",
    "\"HAVING\"",
    "\"HOUR\"",
    "\"IDENTITY\"",
    "\"IMMEDIATE\"",
    "\"IN\"",
    "\"INDICATOR\"",
    "\"INITIALLY\"",
    "\"INNER\"",
    "\"INPUT\"",
    "\"INSENSITIVE\"",
    "\"INSERT\"",
    "\"INT\"",
    "\"INTEGER\"",
    "\"INTERSECT\"",
    "\"INTERVAL\"",
    "\"INTO\"",
    "\"IS\"",
    "\"ISOLATION\"",
    "\"JOIN\"",
    "\"KEY\"",
    "\"LANGUAGE\"",
    "\"LAST\"",
    "\"LEADING\"",
    "\"LEFT\"",
    "\"LEVEL\"",
    "\"LIKE\"",
    "\"LOCAL\"",
    "\"LOWER\"",
    "\"MATCH\"",
    "\"MAX\"",
    "\"MIN\"",
    "\"MINUTE\"",
    "\"MODULE\"",
    "\"MONTH\"",
    "\"NAMES\"",
    "\"NATIONAL\"",
    "\"NATURAL\"",
    "\"NCHAR\"",
    "\"NEXT\"",
    "\"NO\"",
    "\"NOT\"",
    "\"NULL\"",
    "\"NULLIF\"",
    "\"NUMERIC\"",
    "\"OCTET_LENGTH\"",
    "\"OF\"",
    "\"ON\"",
    "\"ONLY\"",
    "\"OPEN\"",
    "\"OPTION\"",
    "\"OR\"",
    "\"ORDER\"",
    "\"OUTER\"",
    "\"OUTPUT\"",
    "\"OVERLAPS\"",
    "\"PAD\"",
    "\"PARTIAL\"",
    "\"POSITION\"",
    "\"PRECISION\"",
    "\"PREPARE\"",
    "\"PRESERVE\"",
    "\"PRIMARY\"",
    "\"PRIOR\"",
    "\"PRIVILEGES\"",
    "\"PROCEDURE\"",
    "\"PUBLIC\"",
    "\"READ\"",
    "\"REAL\"",
    "\"REFERENCES\"",
    "\"RELATIVE\"",
    "\"RESTRICT\"",
    "\"REVOKE\"",
    "\"RIGHT\"",
    "\"ROLLBACK\"",
    "\"ROWS\"",
    "\"SCHEMA\"",
    "\"SCROLL\"",
    "\"SECOND\"",
    "\"SECTION\"",
    "\"SELECT\"",
    "\"SESSION\"",
    "\"SESSION_USER\"",
    "\"SET\"",
    "\"SIZE\"",
    "\"SMALLINT\"",
    "\"SOME\"",
    "\"SPACE\"",
    "\"SQL\"",
    "\"SQLCODE\"",
    "\"SQLERROR\"",
    "\"SQLSTATE\"",
    "\"SUBSTRING\"",
    "\"SUM\"",
    "\"SYSTEM_TIME\"",
    "\"SYSTEM_USER\"",
    "\"TABLE\"",
    "\"TEMPORARY\"",
    "\"THEN\"",
    "\"TIME\"",
    "\"TIMESTAMP\"",
    "\"TIMEZONE_HOUR\"",
    "\"TIMEZONE_MINUTE\"",
    "\"TO\"",
    "\"TRAILING\"",
    "\"TRANSACTION\"",
    "\"TRANSLATE\"",
    "\"TRANSLATION\"",
    "\"TRIM\"",
    "\"TRUE\"",
    "\"UNION\"",
    "\"UNIQUE\"",
    "\"UNKNOWN\"",
    "\"UPDATE\"",
    "\"UPPER\"",
    "\"USAGE\"",
    "\"USER\"",
    "\"USING\"",
    "\"VALUE\"",
    "\"VALUES\"",
    "\"VARCHAR\"",
    "\"VARYING\"",
    "\"VIEW\"",
    "\"WHEN\"",
    "\"WHENEVER\"",
    "\"WHERE\"",
    "\"WITH\"",
    "\"WORK\"",
    "\"WRITE\"",
    "\"YEAR\"",
    "\"ZONE\"",
    "<QuoteSymbol>",
    "<NonQuoteCharacter>",
    "<CharacterRepresentation>",
    "<Bit>",
    "<Letter>",
    "<Digit>",
    "<Hexit>",
    "\"@\"",
    "<Identifier>",
    "<DriveSpec>",
    "<ParameterName>",
    "<UnsignedInteger>",
    "<SignedInteger>",
    "<ExactNumericLiteral>",
    "<ApproximateNumericLiteral>",
    "<FractionalPart>",
    "<Sign>",
    "<HexStringLiteral>",
    "<BitStringLiteral>",
    "<CharacterStringLiteral>",
    "\"<>\"",
    "\">=\"",
    "\"<=\"",
    "\"||\"",
    "<DoublePeriod>",
    "\"<\"",
    "\"=\"",
    "\">\"",
    "\"\\\"\"",
    "\"%\"",
    "\"&\"",
    "\"\\\'\"",
    "\"(\"",
    "\")\"",
    "\"*\"",
    "\"+\"",
    "\",\"",
    "\"-\"",
    "\".\"",
    "\"/\"",
    "\":\"",
    "\";\"",
    "\"?\"",
    "\"[\"",
    "\"]\"",
    "\"_\"",
    "\"|\"",
  };

}
