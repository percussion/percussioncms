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

public class SQLParserTokenManager implements SQLParserConstants
{
static private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
static private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 9:
         jjmatchedKind = 2;
         return jjMoveNfa_0(0, 0);
      case 10:
         jjmatchedKind = 3;
         return jjMoveNfa_0(0, 0);
      case 12:
         jjmatchedKind = 5;
         return jjMoveNfa_0(0, 0);
      case 13:
         jjmatchedKind = 4;
         return jjMoveNfa_0(0, 0);
      case 32:
         jjmatchedKind = 1;
         return jjMoveNfa_0(0, 0);
      case 34:
         jjmatchedKind = 266;
         return jjMoveNfa_0(0, 0);
      case 37:
         jjmatchedKind = 267;
         return jjMoveNfa_0(0, 0);
      case 38:
         jjmatchedKind = 268;
         return jjMoveNfa_0(0, 0);
      case 39:
         jjmatchedKind = 269;
         return jjMoveNfa_0(0, 0);
      case 40:
         jjmatchedKind = 270;
         return jjMoveNfa_0(0, 0);
      case 41:
         jjmatchedKind = 271;
         return jjMoveNfa_0(0, 0);
      case 42:
         jjmatchedKind = 272;
         return jjMoveNfa_0(0, 0);
      case 43:
         jjmatchedKind = 273;
         return jjMoveNfa_0(0, 0);
      case 44:
         jjmatchedKind = 274;
         return jjMoveNfa_0(0, 0);
      case 45:
         jjmatchedKind = 275;
         return jjMoveNfa_0(0, 0);
      case 46:
         jjmatchedKind = 276;
         return jjMoveNfa_0(0, 0);
      case 47:
         jjmatchedKind = 277;
         return jjMoveNfa_0(0, 0);
      case 58:
         jjmatchedKind = 278;
         return jjMoveNfa_0(0, 0);
      case 59:
         jjmatchedKind = 279;
         return jjMoveNfa_0(0, 0);
      case 60:
         jjmatchedKind = 263;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x0L, 0x14L);
      case 61:
         jjmatchedKind = 264;
         return jjMoveNfa_0(0, 0);
      case 62:
         jjmatchedKind = 265;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x0L, 0x8L);
      case 63:
         jjmatchedKind = 280;
         return jjMoveNfa_0(0, 0);
      case 64:
         jjmatchedKind = 245;
         return jjMoveNfa_0(0, 0);
      case 65:
         return jjMoveStringLiteralDfa1_0(0x3fff800L, 0x0L, 0x0L, 0x0L, 0x0L);
      case 66:
         return jjMoveStringLiteralDfa1_0(0xfc000000L, 0x0L, 0x0L, 0x0L, 0x0L);
      case 67:
         return jjMoveStringLiteralDfa1_0(0x3fffff9f00000000L, 0x0L, 0x0L, 0x0L, 0x0L);
      case 68:
         return jjMoveStringLiteralDfa1_0(0xc000000000000000L, 0x3ffefL, 0x0L, 0x0L, 0x0L);
      case 69:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x1ffc0000L, 0x0L, 0x0L, 0x0L);
      case 70:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x3fe0000000L, 0x0L, 0x0L, 0x0L);
      case 71:
         return jjMoveStringLiteralDfa1_0(0x0L, 0xfc000000000L, 0x0L, 0x0L, 0x0L);
      case 72:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x300000000000L, 0x0L, 0x0L, 0x0L);
      case 73:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x3fffc00000000000L, 0x0L, 0x0L, 0x0L);
      case 74:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x4000000000000000L, 0x0L, 0x0L, 0x0L);
      case 75:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x8000000000000000L, 0x0L, 0x0L, 0x0L);
      case 76:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0xffL, 0x0L, 0x0L);
      case 77:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x3f00L, 0x0L, 0x0L);
      case 78:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0xffc000L, 0x0L, 0x0L);
      case 79:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x7ff000000L, 0x0L, 0x0L);
      case 80:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x3ff800000000L, 0x0L, 0x0L);
      case 82:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x7fc00000000000L, 0x0L, 0x0L);
      case 83:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0xff80000000000000L, 0x7ffL, 0x0L);
      case 84:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x1fff800L, 0x0L);
      case 85:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x1fe000000L, 0x0L);
      case 86:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x3e00000000L, 0x0L);
      case 87:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0xfc000000000L, 0x0L);
      case 89:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x100000000000L, 0x0L);
      case 90:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x200000000000L, 0x0L);
      case 91:
         jjmatchedKind = 281;
         return jjMoveNfa_0(0, 0);
      case 93:
         jjmatchedKind = 282;
         return jjMoveNfa_0(0, 0);
      case 95:
         jjmatchedKind = 283;
         return jjMoveNfa_0(0, 0);
      case 97:
         return jjMoveStringLiteralDfa1_0(0x3fff800L, 0x0L, 0x0L, 0x0L, 0x0L);
      case 98:
         return jjMoveStringLiteralDfa1_0(0xfc000000L, 0x0L, 0x0L, 0x0L, 0x0L);
      case 99:
         return jjMoveStringLiteralDfa1_0(0x3fffff9f00000000L, 0x0L, 0x0L, 0x0L, 0x0L);
      case 100:
         return jjMoveStringLiteralDfa1_0(0xc000000000000000L, 0x3ffefL, 0x0L, 0x0L, 0x0L);
      case 101:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x1ffc0000L, 0x0L, 0x0L, 0x0L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x3fe0000000L, 0x0L, 0x0L, 0x0L);
      case 103:
         return jjMoveStringLiteralDfa1_0(0x0L, 0xfc000000000L, 0x0L, 0x0L, 0x0L);
      case 104:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x300000000000L, 0x0L, 0x0L, 0x0L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x3fffc00000000000L, 0x0L, 0x0L, 0x0L);
      case 106:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x4000000000000000L, 0x0L, 0x0L, 0x0L);
      case 107:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x8000000000000000L, 0x0L, 0x0L, 0x0L);
      case 108:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0xffL, 0x0L, 0x0L);
      case 109:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x3f00L, 0x0L, 0x0L);
      case 110:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0xffc000L, 0x0L, 0x0L);
      case 111:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x7ff000000L, 0x0L, 0x0L);
      case 112:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x3ff800000000L, 0x0L, 0x0L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x7fc00000000000L, 0x0L, 0x0L);
      case 115:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0xff80000000000000L, 0x7ffL, 0x0L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x1fff800L, 0x0L);
      case 117:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x1fe000000L, 0x0L);
      case 118:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x3e00000000L, 0x0L);
      case 119:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0xfc000000000L, 0x0L);
      case 121:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x100000000000L, 0x0L);
      case 122:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x200000000000L, 0x0L);
      case 124:
         jjmatchedKind = 284;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x0L, 0x0L, 0x0L, 0x20L);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
static private final int jjMoveStringLiteralDfa1_0(long active0, long active1, long active2, long active3, long active4)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 0);
   }
   switch(curChar)
   {
      case 61:
         if ((active4 & 0x8L) != 0L)
         {
            jjmatchedKind = 259;
            jjmatchedPos = 1;
         }
         else if ((active4 & 0x10L) != 0L)
         {
            jjmatchedKind = 260;
            jjmatchedPos = 1;
         }
         break;
      case 62:
         if ((active4 & 0x4L) != 0L)
         {
            jjmatchedKind = 258;
            jjmatchedPos = 1;
         }
         break;
      case 65:
         return jjMoveStringLiteralDfa2_0(active0, 0xc000001f00000000L, active1, 0x100020000000L, active2, 0x180001c303L, active3, 0x1e00000800L, active4, 0L);
      case 66:
         return jjMoveStringLiteralDfa2_0(active0, 0x800L, active1, 0L, active2, 0L, active3, 0L, active4, 0L);
      case 67:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000L, active1, 0L, active2, 0x180000001020000L, active3, 0L, active4, 0L);
      case 68:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000L, active1, 0x400000000000L, active2, 0L, active3, 0L, active4, 0L);
      case 69:
         return jjMoveStringLiteralDfa2_0(active0, 0xc000000L, active1, 0x8000004040000fefL, active2, 0x7e0fc0000004001cL, active3, 0x100000001000L, active4, 0L);
      case 70:
         if ((active2 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 153;
            jjmatchedPos = 1;
         }
         break;
      case 72:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000000000L, active1, 0L, active2, 0L, active3, 0x1c000002000L, active4, 0L);
      case 73:
         return jjMoveStringLiteralDfa2_0(active0, 0x30000000L, active1, 0x80007000L, active2, 0x8010000000000c20L, active3, 0x2200003c000L, active4, 0L);
      case 76:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000001c000L, active1, 0x8100040000L, active2, 0L, active3, 0L, active4, 0L);
      case 77:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x800000000000L, active2, 0L, active3, 0x1L, active4, 0L);
      case 78:
         if ((active1 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 112;
            jjmatchedPos = 1;
         }
         else if ((active2 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 154;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x60000L, active1, 0xffe000000180000L, active2, 0x8000000L, active3, 0xe000000L, active4, 0L);
      case 79:
         if ((active1 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 104;
            jjmatchedPos = 1;
         }
         else if ((active2 & 0x80000L) != 0L)
         {
            jjmatchedKind = 147;
            jjmatchedPos = 1;
         }
         else if ((active3 & 0x40000L) != 0L)
         {
            jjmatchedKind = 210;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x3ffe0040000000L, active1, 0x4000220e00018000L, active2, 0x600020001030c0L, active3, 0x240000000002L, active4, 0L);
      case 80:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0L, active2, 0x30000000L, active3, 0x30000004L, active4, 0L);
      case 81:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x78L, active4, 0L);
      case 82:
         if ((active2 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 158;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0xc0000000080000L, active1, 0xc1000020000L, active2, 0x1fc080000000L, active3, 0x80001f80000L, active4, 0L);
      case 83:
         if ((active0 & 0x100000L) != 0L)
         {
            jjmatchedKind = 20;
            jjmatchedPos = 1;
         }
         else if ((active1 & 0x1000000000000000L) != 0L)
         {
            jjmatchedKind = 124;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x600000L, active1, 0x2000000000200000L, active2, 0L, active3, 0x1c0000000L, active4, 0L);
      case 84:
         if ((active0 & 0x800000L) != 0L)
         {
            jjmatchedKind = 23;
            jjmatchedPos = 1;
         }
         break;
      case 85:
         return jjMoveStringLiteralDfa2_0(active0, 0x3f00000001000000L, active1, 0x2000000000L, active2, 0x200300e00000L, active3, 0x180L, active4, 0L);
      case 86:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000L, active1, 0L, active2, 0x400000000L, active3, 0L, active4, 0L);
      case 88:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x1fc00000L, active2, 0L, active3, 0L, active4, 0L);
      case 89:
         if ((active0 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 31;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x600L, active4, 0L);
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0xc000001f00000000L, active1, 0x100020000000L, active2, 0x180001c303L, active3, 0x1e00000800L, active4, 0L);
      case 98:
         return jjMoveStringLiteralDfa2_0(active0, 0x800L, active1, 0L, active2, 0L, active3, 0L, active4, 0L);
      case 99:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000L, active1, 0L, active2, 0x180000001020000L, active3, 0L, active4, 0L);
      case 100:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000L, active1, 0x400000000000L, active2, 0L, active3, 0L, active4, 0L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0xc000000L, active1, 0x8000004040000fefL, active2, 0x7e0fc0000004001cL, active3, 0x100000001000L, active4, 0L);
      case 102:
         if ((active2 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 153;
            jjmatchedPos = 1;
         }
         break;
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000000000L, active1, 0L, active2, 0L, active3, 0x1c000002000L, active4, 0L);
      case 105:
         return jjMoveStringLiteralDfa2_0(active0, 0x30000000L, active1, 0x80007000L, active2, 0x8010000000000c20L, active3, 0x2200003c000L, active4, 0L);
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000001c000L, active1, 0x8100040000L, active2, 0L, active3, 0L, active4, 0L);
      case 109:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x800000000000L, active2, 0L, active3, 0x1L, active4, 0L);
      case 110:
         if ((active1 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 112;
            jjmatchedPos = 1;
         }
         else if ((active2 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 154;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x60000L, active1, 0xffe000000180000L, active2, 0x8000000L, active3, 0xe000000L, active4, 0L);
      case 111:
         if ((active1 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 104;
            jjmatchedPos = 1;
         }
         else if ((active2 & 0x80000L) != 0L)
         {
            jjmatchedKind = 147;
            jjmatchedPos = 1;
         }
         else if ((active3 & 0x40000L) != 0L)
         {
            jjmatchedKind = 210;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x3ffe0040000000L, active1, 0x4000220e00018000L, active2, 0x600020001030c0L, active3, 0x240000000002L, active4, 0L);
      case 112:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0L, active2, 0x30000000L, active3, 0x30000004L, active4, 0L);
      case 113:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x78L, active4, 0L);
      case 114:
         if ((active2 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 158;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0xc0000000080000L, active1, 0xc1000020000L, active2, 0x1fc080000000L, active3, 0x80001f80000L, active4, 0L);
      case 115:
         if ((active0 & 0x100000L) != 0L)
         {
            jjmatchedKind = 20;
            jjmatchedPos = 1;
         }
         else if ((active1 & 0x1000000000000000L) != 0L)
         {
            jjmatchedKind = 124;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x600000L, active1, 0x2000000000200000L, active2, 0L, active3, 0x1c0000000L, active4, 0L);
      case 116:
         if ((active0 & 0x800000L) != 0L)
         {
            jjmatchedKind = 23;
            jjmatchedPos = 1;
         }
         break;
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x3f00000001000000L, active1, 0x2000000000L, active2, 0x200300e00000L, active3, 0x180L, active4, 0L);
      case 118:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000L, active1, 0L, active2, 0x400000000L, active3, 0L, active4, 0L);
      case 120:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x1fc00000L, active2, 0L, active3, 0L, active4, 0L);
      case 121:
         if ((active0 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 31;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x600L, active4, 0L);
      case 124:
         if ((active4 & 0x20L) != 0L)
         {
            jjmatchedKind = 261;
            jjmatchedPos = 1;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_0(0, 1);
}
static private final int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3, long old4, long active4)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3) | (active4 &= old4)) == 0L)
      return jjMoveNfa_0(0, 1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 1);
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000000000L, active1, 0x40000001001L, active2, 0xc00000000004L, active3, 0x100040780005L);
      case 66:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x200000000000L, active3, 0x880L);
      case 67:
         if ((active0 & 0x200000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x2L) != 0L)
         {
            jjmatchedKind = 65;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0xe0000cL, active2, 0x600000000000040L, active3, 0L);
      case 68:
         if ((active0 & 0x2000L) != 0L)
         {
            jjmatchedKind = 13;
            jjmatchedPos = 2;
         }
         else if ((active0 & 0x20000L) != 0L)
         {
            jjmatchedKind = 17;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x80000L) != 0L)
         {
            jjmatchedKind = 83;
            jjmatchedPos = 2;
         }
         else if ((active2 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 163;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x2000000100000L, active2, 0x80001000L, active3, 0x10000000L);
      case 69:
         if ((active0 & 0x80000L) != 0L)
         {
            jjmatchedKind = 19;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x40008000000000L, active1, 0x400003000000L, active2, 0x1c410000000L, active3, 0x1e080002000L);
      case 70:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0xe0L, active2, 0x1000000000008L, active3, 0L);
      case 71:
         if ((active0 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 25;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x4000000L, active1, 0L, active2, 0x10000000000000L, active3, 0L);
      case 72:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x80000000020000L, active3, 0L);
      case 73:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x4004000004000000L, active2, 0xe0000000000L, active3, 0x80106800000L);
      case 75:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x20L, active3, 0x8000000L);
      case 76:
         if ((active0 & 0x4000L) != 0L)
         {
            jjmatchedKind = 14;
            jjmatchedPos = 2;
         }
         else if ((active3 & 0x8L) != 0L)
         {
            jjmatchedKind = 195;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x1c0000008000L, active1, 0x2020000100L, active2, 0x822000008600000L, active3, 0x600000070L);
      case 77:
         if ((active3 & 0x100L) != 0L)
         {
            jjmatchedKind = 200;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x200000000000L, active1, 0x800000008000L, active2, 0x804000L, active3, 0x3d002L);
      case 78:
         if ((active2 & 0x400L) != 0L)
         {
            jjmatchedKind = 138;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0xfc00000000000L, active1, 0x8000000000000L, active2, 0x2801L, active3, 0x200000000000L);
      case 79:
         return jjMoveStringLiteralDfa3_0(active0, 0x80010000000000L, active1, 0x2000089100020000L, active2, 0x100000000000L, active3, 0L);
      case 80:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x10000000000000L, active2, 0L, active3, 0x20000000L);
      case 82:
         if ((active1 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 97;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x3f10000000000000L, active1, 0x480000000L, active2, 0x100001000000000L, active3, 0x41800000000L);
      case 83:
         return jjMoveStringLiteralDfa3_0(active0, 0xf00400800L, active1, 0x60000000046e00L, active2, 0x3004002000000002L, active3, 0x600L);
      case 84:
         if ((active0 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 28;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x4000000000L) != 0L)
         {
            jjmatchedKind = 102;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x80000000000000L) != 0L)
         {
            jjmatchedKind = 119;
            jjmatchedPos = 2;
         }
         else if ((active2 & 0x100000L) != 0L)
         {
            jjmatchedKind = 148;
            jjmatchedPos = 2;
         }
         else if ((active2 & 0x4000000000000000L) != 0L)
         {
            jjmatchedKind = 190;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x4000001069011000L, active1, 0xf00020058000000L, active2, 0x321018100L, active3, 0x20000000000L);
      case 85:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000000000000L, active1, 0x200800010000L, active2, 0L, active3, 0x1000000L);
      case 86:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x100000000000L, active2, 0x8000000000010L, active3, 0L);
      case 87:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x40000000000080L, active3, 0L);
      case 88:
         if ((active2 & 0x200L) != 0L)
         {
            jjmatchedKind = 137;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x40000L, active3, 0L);
      case 89:
         if ((active0 & 0x40000L) != 0L)
         {
            jjmatchedKind = 18;
            jjmatchedPos = 2;
         }
         else if ((active0 & 0x8000000000000000L) != 0L)
         {
            jjmatchedKind = 63;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x8000000000000000L) != 0L)
         {
            jjmatchedKind = 127;
            jjmatchedPos = 2;
         }
         break;
      case 90:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x8000000000000000L, active3, 0L);
      case 97:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000000000L, active1, 0x40000001001L, active2, 0xc00000000004L, active3, 0x100040780005L);
      case 98:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x200000000000L, active3, 0x880L);
      case 99:
         if ((active0 & 0x200000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x2L) != 0L)
         {
            jjmatchedKind = 65;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0xe0000cL, active2, 0x600000000000040L, active3, 0L);
      case 100:
         if ((active0 & 0x2000L) != 0L)
         {
            jjmatchedKind = 13;
            jjmatchedPos = 2;
         }
         else if ((active0 & 0x20000L) != 0L)
         {
            jjmatchedKind = 17;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x80000L) != 0L)
         {
            jjmatchedKind = 83;
            jjmatchedPos = 2;
         }
         else if ((active2 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 163;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x2000000100000L, active2, 0x80001000L, active3, 0x10000000L);
      case 101:
         if ((active0 & 0x80000L) != 0L)
         {
            jjmatchedKind = 19;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x40008000000000L, active1, 0x400003000000L, active2, 0x1c410000000L, active3, 0x1e080002000L);
      case 102:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0xe0L, active2, 0x1000000000008L, active3, 0L);
      case 103:
         if ((active0 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 25;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x4000000L, active1, 0L, active2, 0x10000000000000L, active3, 0L);
      case 104:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x80000000020000L, active3, 0L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x4004000004000000L, active2, 0xe0000000000L, active3, 0x80106800000L);
      case 107:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x20L, active3, 0x8000000L);
      case 108:
         if ((active0 & 0x4000L) != 0L)
         {
            jjmatchedKind = 14;
            jjmatchedPos = 2;
         }
         else if ((active3 & 0x8L) != 0L)
         {
            jjmatchedKind = 195;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x1c0000008000L, active1, 0x2020000100L, active2, 0x822000008600000L, active3, 0x600000070L);
      case 109:
         if ((active3 & 0x100L) != 0L)
         {
            jjmatchedKind = 200;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x200000000000L, active1, 0x800000008000L, active2, 0x804000L, active3, 0x3d002L);
      case 110:
         if ((active2 & 0x400L) != 0L)
         {
            jjmatchedKind = 138;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0xfc00000000000L, active1, 0x8000000000000L, active2, 0x2801L, active3, 0x200000000000L);
      case 111:
         return jjMoveStringLiteralDfa3_0(active0, 0x80010000000000L, active1, 0x2000089100020000L, active2, 0x100000000000L, active3, 0L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x10000000000000L, active2, 0L, active3, 0x20000000L);
      case 114:
         if ((active1 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 97;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x3f10000000000000L, active1, 0x480000000L, active2, 0x100001000000000L, active3, 0x41800000000L);
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0xf00400800L, active1, 0x60000000046e00L, active2, 0x3004002000000002L, active3, 0x600L);
      case 116:
         if ((active0 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 28;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x4000000000L) != 0L)
         {
            jjmatchedKind = 102;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x80000000000000L) != 0L)
         {
            jjmatchedKind = 119;
            jjmatchedPos = 2;
         }
         else if ((active2 & 0x100000L) != 0L)
         {
            jjmatchedKind = 148;
            jjmatchedPos = 2;
         }
         else if ((active2 & 0x4000000000000000L) != 0L)
         {
            jjmatchedKind = 190;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x4000001069011000L, active1, 0xf00020058000000L, active2, 0x321018100L, active3, 0x20000000000L);
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000000000000L, active1, 0x200800010000L, active2, 0L, active3, 0x1000000L);
      case 118:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x100000000000L, active2, 0x8000000000010L, active3, 0L);
      case 119:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x40000000000080L, active3, 0L);
      case 120:
         if ((active2 & 0x200L) != 0L)
         {
            jjmatchedKind = 137;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x40000L, active3, 0L);
      case 121:
         if ((active0 & 0x40000L) != 0L)
         {
            jjmatchedKind = 18;
            jjmatchedPos = 2;
         }
         else if ((active0 & 0x8000000000000000L) != 0L)
         {
            jjmatchedKind = 63;
            jjmatchedPos = 2;
         }
         else if ((active1 & 0x8000000000000000L) != 0L)
         {
            jjmatchedKind = 127;
            jjmatchedPos = 2;
         }
         break;
      case 122:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0L, active2, 0x8000000000000000L, active3, 0L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 2);
}
static private final int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 2);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 2);
   }
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x100000L, active2, 0L, active3, 0L);
      case 65:
         return jjMoveStringLiteralDfa4_0(active0, 0x40001000000000L, active1, 0x100208020L, active2, 0x2000000020040L, active3, 0x10000000L);
      case 66:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x8000010000L, active2, 0L, active3, 0L);
      case 67:
         if ((active1 & 0x200L) != 0L)
         {
            jjmatchedKind = 73;
            jjmatchedPos = 3;
         }
         else if ((active1 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 88;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x8300000000L, active1, 0x42002c00L, active2, 0x104000000100L, active3, 0x800000014L);
      case 68:
         if ((active2 & 0x400000000000L) != 0L)
         {
            jjmatchedKind = 174;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0x4L, active3, 0L);
      case 69:
         if ((active0 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 34;
            jjmatchedPos = 3;
         }
         else if ((active0 & 0x4000000000000000L) != 0L)
         {
            jjmatchedKind = 62;
            jjmatchedPos = 3;
         }
         else if ((active1 & 0x40000L) != 0L)
         {
            jjmatchedKind = 82;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x20L) != 0L)
         {
            jjmatchedKind = 133;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x8000000000000000L) != 0L)
         {
            jjmatchedKind = 191;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x2L) != 0L)
         {
            jjmatchedKind = 193;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x4000L) != 0L)
         {
            jjmatchedKind = 206;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 216;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 237;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x410000L, active1, 0x768800408c001c0L, active2, 0x881000181804090L, active3, 0x20038020L);
      case 71:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x1000L, active2, 0x1L, active3, 0x40000000L);
      case 72:
         if ((active0 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 30;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 233;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x1000000L, active1, 0L, active2, 0x10000000000000L, active3, 0L);
      case 73:
         return jjMoveStringLiteralDfa4_0(active0, 0x4001000L, active1, 0x2100000000004L, active2, 0x2020008000L, active3, 0x80000L);
      case 75:
         if ((active3 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 234;
            jjmatchedPos = 3;
         }
         break;
      case 76:
         if ((active1 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 101;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x200000L) != 0L)
         {
            jjmatchedKind = 149;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 175;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0xe0000000000L, active1, 0x2000000000000009L, active2, 0x20200000400000L, active3, 0x801L);
      case 77:
         if ((active1 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 100;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x800000L) != 0L)
         {
            jjmatchedKind = 215;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x200000000000L, active1, 0L, active2, 0x20000000000L, active3, 0L);
      case 78:
         if ((active1 & 0x4000000000000000L) != 0L)
         {
            jjmatchedKind = 126;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 156;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x2000L) != 0L)
         {
            jjmatchedKind = 205;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x4000000000L) != 0L)
         {
            jjmatchedKind = 230;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x20c00000000000L, active1, 0x440800000000L, active2, 0L, active3, 0x8108700000L);
      case 79:
         if ((active1 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 105;
            jjmatchedPos = 3;
         }
         else if ((active1 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 123;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x8800L, active1, 0L, active2, 0x308040000000000L, active3, 0x2000000L);
      case 80:
         if ((active1 & 0x20000L) != 0L)
         {
            jjmatchedKind = 81;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0x8200000000L, active3, 0x1000L);
      case 81:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x4000000L);
      case 82:
         if ((active1 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 109;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 223;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 236;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x1f10000000000000L, active1, 0x10000000L, active2, 0x400000000L, active3, 0x10000000000L);
      case 83:
         if ((active2 & 0x40000000000000L) != 0L)
         {
            jjmatchedKind = 182;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x2083010000000000L, active1, 0xa4000000L, active2, 0x3000010000000000L, active3, 0xc0L);
      case 84:
         if ((active0 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 35;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x2L) != 0L)
         {
            jjmatchedKind = 129;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x8L) != 0L)
         {
            jjmatchedKind = 131;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x40000L) != 0L)
         {
            jjmatchedKind = 146;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000000000L, active1, 0x4000000004000L, active2, 0x404001000002000L, active3, 0x80000000600L);
      case 85:
         return jjMoveStringLiteralDfa4_0(active0, 0x100000000000L, active1, 0x10080000000000L, active2, 0x11800L, active3, 0x600000000L);
      case 86:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000000000000L, active1, 0L, active2, 0x80000000000L, active3, 0L);
      case 87:
         if ((active3 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 229;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x8000000L, active1, 0L, active2, 0L, active3, 0L);
      case 89:
         if ((active2 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 155;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x1000000000L);
      case 95:
         return jjMoveStringLiteralDfa4_0(active0, 0x20000000L, active1, 0L, active2, 0L, active3, 0L);
      case 97:
         return jjMoveStringLiteralDfa4_0(active0, 0x40001000000000L, active1, 0x100208020L, active2, 0x2000000020040L, active3, 0x10000000L);
      case 98:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x8000010000L, active2, 0L, active3, 0L);
      case 99:
         if ((active1 & 0x200L) != 0L)
         {
            jjmatchedKind = 73;
            jjmatchedPos = 3;
         }
         else if ((active1 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 88;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x8300000000L, active1, 0x42002c00L, active2, 0x104000000100L, active3, 0x800000014L);
      case 100:
         if ((active2 & 0x400000000000L) != 0L)
         {
            jjmatchedKind = 174;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0x4L, active3, 0L);
      case 101:
         if ((active0 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 34;
            jjmatchedPos = 3;
         }
         else if ((active0 & 0x4000000000000000L) != 0L)
         {
            jjmatchedKind = 62;
            jjmatchedPos = 3;
         }
         else if ((active1 & 0x40000L) != 0L)
         {
            jjmatchedKind = 82;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x20L) != 0L)
         {
            jjmatchedKind = 133;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x8000000000000000L) != 0L)
         {
            jjmatchedKind = 191;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x2L) != 0L)
         {
            jjmatchedKind = 193;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x4000L) != 0L)
         {
            jjmatchedKind = 206;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 216;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 237;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x410000L, active1, 0x768800408c001c0L, active2, 0x881000181804090L, active3, 0x20038020L);
      case 103:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x1000L, active2, 0x1L, active3, 0x40000000L);
      case 104:
         if ((active0 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 30;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 233;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x1000000L, active1, 0L, active2, 0x10000000000000L, active3, 0L);
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x4001000L, active1, 0x2100000000004L, active2, 0x2020008000L, active3, 0x80000L);
      case 107:
         if ((active3 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 234;
            jjmatchedPos = 3;
         }
         break;
      case 108:
         if ((active1 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 101;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x200000L) != 0L)
         {
            jjmatchedKind = 149;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 175;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0xe0000000000L, active1, 0x2000000000000009L, active2, 0x20200000400000L, active3, 0x801L);
      case 109:
         if ((active1 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 100;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x800000L) != 0L)
         {
            jjmatchedKind = 215;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x200000000000L, active1, 0L, active2, 0x20000000000L, active3, 0L);
      case 110:
         if ((active1 & 0x4000000000000000L) != 0L)
         {
            jjmatchedKind = 126;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 156;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x2000L) != 0L)
         {
            jjmatchedKind = 205;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x4000000000L) != 0L)
         {
            jjmatchedKind = 230;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x20c00000000000L, active1, 0x440800000000L, active2, 0L, active3, 0x8108700000L);
      case 111:
         if ((active1 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 105;
            jjmatchedPos = 3;
         }
         else if ((active1 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 123;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x8800L, active1, 0L, active2, 0x308040000000000L, active3, 0x2000000L);
      case 112:
         if ((active1 & 0x20000L) != 0L)
         {
            jjmatchedKind = 81;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0x8200000000L, active3, 0x1000L);
      case 113:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x4000000L);
      case 114:
         if ((active1 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 109;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 223;
            jjmatchedPos = 3;
         }
         else if ((active3 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 236;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x1f10000000000000L, active1, 0x10000000L, active2, 0x400000000L, active3, 0x10000000000L);
      case 115:
         if ((active2 & 0x40000000000000L) != 0L)
         {
            jjmatchedKind = 182;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x2083010000000000L, active1, 0xa4000000L, active2, 0x3000010000000000L, active3, 0xc0L);
      case 116:
         if ((active0 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 35;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x2L) != 0L)
         {
            jjmatchedKind = 129;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x8L) != 0L)
         {
            jjmatchedKind = 131;
            jjmatchedPos = 3;
         }
         else if ((active2 & 0x40000L) != 0L)
         {
            jjmatchedKind = 146;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000000000L, active1, 0x4000000004000L, active2, 0x404001000002000L, active3, 0x80000000600L);
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x100000000000L, active1, 0x10080000000000L, active2, 0x11800L, active3, 0x600000000L);
      case 118:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000000000000L, active1, 0L, active2, 0x80000000000L, active3, 0L);
      case 119:
         if ((active3 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 229;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x8000000L, active1, 0L, active2, 0L, active3, 0L);
      case 121:
         if ((active2 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 155;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x1000000000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 3);
}
static private final int jjMoveStringLiteralDfa4_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 3);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 3);
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa5_0(active0, 0xc0300000000L, active1, 0x2000008010000008L, active2, 0x28000000000L, active3, 0L);
      case 66:
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0x20000000000000L, active3, 0L);
      case 67:
         return jjMoveStringLiteralDfa5_0(active0, 0x8000L, active1, 0x2000000000000L, active2, 0x800000000000000L, active3, 0L);
      case 68:
         if ((active1 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 99;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x800000000000L, active2, 0L, active3, 0L);
      case 69:
         if ((active0 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 40;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 93;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x4L) != 0L)
         {
            jjmatchedKind = 194;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x800L) != 0L)
         {
            jjmatchedKind = 203;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 222;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 225;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 232;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 235;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x1f18c20008000000L, active1, 0x100000L, active2, 0x110000000000L, active3, 0x8400000600L);
      case 71:
         if ((active3 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 224;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x100000000000000L, active2, 0L, active3, 0L);
      case 72:
         if ((active1 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 94;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x100L) != 0L)
         {
            jjmatchedKind = 136;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x2000L) != 0L)
         {
            jjmatchedKind = 141;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x800000000L);
      case 73:
         return jjMoveStringLiteralDfa5_0(active0, 0x4200000000000L, active1, 0x400040000c000L, active2, 0x3400285000400004L, active3, 0x1000000000L);
      case 75:
         if ((active0 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0x8000000000000L, active3, 0L);
      case 76:
         if ((active2 & 0x10L) != 0L)
         {
            jjmatchedKind = 132;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x40L) != 0L)
         {
            jjmatchedKind = 134;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x1020000800L, active1, 0x10001L, active2, 0x100000400001000L, active3, 0x80001L);
      case 77:
         return jjMoveStringLiteralDfa5_0(active0, 0x100000000000L, active1, 0x4L, active2, 0x80000000000000L, active3, 0L);
      case 78:
         if ((active0 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 26;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 217;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x20100000001000L, active2, 0x200000000000000L, active3, 0L);
      case 79:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000000001001000L, active1, 0x2000L, active2, 0x20008000L, active3, 0x8001010L);
      case 80:
         if ((active1 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 107;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0xe00000L, active2, 0L, active3, 0L);
      case 82:
         if ((active0 & 0x10000L) != 0L)
         {
            jjmatchedKind = 16;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 115;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x80L) != 0L)
         {
            jjmatchedKind = 135;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x20000L) != 0L)
         {
            jjmatchedKind = 145;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 159;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 160;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 170;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 221;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x400000L, active1, 0x640000008000cc0L, active2, 0x5000000810000L, active3, 0x20L);
      case 83:
         if ((active0 & 0x80000000000000L) != 0L)
         {
            jjmatchedKind = 55;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x4000L) != 0L)
         {
            jjmatchedKind = 142;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x708000L);
      case 84:
         if ((active0 & 0x20000000000000L) != 0L)
         {
            jjmatchedKind = 53;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 95;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 96;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 106;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x10000000000000L) != 0L)
         {
            jjmatchedKind = 116;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x10000000000000L) != 0L)
         {
            jjmatchedKind = 180;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x43000000000000L, active1, 0x400004000100L, active2, 0x2002001000800L, active3, 0x100000c0L);
      case 85:
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x2000020L, active2, 0x200000001L, active3, 0x4000000L);
      case 90:
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x30000L);
      case 97:
         return jjMoveStringLiteralDfa5_0(active0, 0xc0300000000L, active1, 0x2000008010000008L, active2, 0x28000000000L, active3, 0L);
      case 98:
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0x20000000000000L, active3, 0L);
      case 99:
         return jjMoveStringLiteralDfa5_0(active0, 0x8000L, active1, 0x2000000000000L, active2, 0x800000000000000L, active3, 0L);
      case 100:
         if ((active1 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 99;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x800000000000L, active2, 0L, active3, 0L);
      case 101:
         if ((active0 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 40;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 93;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x4L) != 0L)
         {
            jjmatchedKind = 194;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x800L) != 0L)
         {
            jjmatchedKind = 203;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 222;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 225;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 232;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 235;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x1f18c20008000000L, active1, 0x100000L, active2, 0x110000000000L, active3, 0x8400000600L);
      case 103:
         if ((active3 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 224;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x100000000000000L, active2, 0L, active3, 0L);
      case 104:
         if ((active1 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 94;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x100L) != 0L)
         {
            jjmatchedKind = 136;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x2000L) != 0L)
         {
            jjmatchedKind = 141;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x800000000L);
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x4200000000000L, active1, 0x400040000c000L, active2, 0x3400285000400004L, active3, 0x1000000000L);
      case 107:
         if ((active0 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0x8000000000000L, active3, 0L);
      case 108:
         if ((active2 & 0x10L) != 0L)
         {
            jjmatchedKind = 132;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x40L) != 0L)
         {
            jjmatchedKind = 134;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x1020000800L, active1, 0x10001L, active2, 0x100000400001000L, active3, 0x80001L);
      case 109:
         return jjMoveStringLiteralDfa5_0(active0, 0x100000000000L, active1, 0x4L, active2, 0x80000000000000L, active3, 0L);
      case 110:
         if ((active0 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 26;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 217;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x20100000001000L, active2, 0x200000000000000L, active3, 0L);
      case 111:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000000001001000L, active1, 0x2000L, active2, 0x20008000L, active3, 0x8001010L);
      case 112:
         if ((active1 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 107;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0xe00000L, active2, 0L, active3, 0L);
      case 114:
         if ((active0 & 0x10000L) != 0L)
         {
            jjmatchedKind = 16;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 115;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x80L) != 0L)
         {
            jjmatchedKind = 135;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x20000L) != 0L)
         {
            jjmatchedKind = 145;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 159;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 160;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 170;
            jjmatchedPos = 4;
         }
         else if ((active3 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 221;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x400000L, active1, 0x640000008000cc0L, active2, 0x5000000810000L, active3, 0x20L);
      case 115:
         if ((active0 & 0x80000000000000L) != 0L)
         {
            jjmatchedKind = 55;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x4000L) != 0L)
         {
            jjmatchedKind = 142;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x708000L);
      case 116:
         if ((active0 & 0x20000000000000L) != 0L)
         {
            jjmatchedKind = 53;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 95;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 96;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 106;
            jjmatchedPos = 4;
         }
         else if ((active1 & 0x10000000000000L) != 0L)
         {
            jjmatchedKind = 116;
            jjmatchedPos = 4;
         }
         else if ((active2 & 0x10000000000000L) != 0L)
         {
            jjmatchedKind = 180;
            jjmatchedPos = 4;
         }
         return jjMoveStringLiteralDfa5_0(active0, 0x43000000000000L, active1, 0x400004000100L, active2, 0x2002001000800L, active3, 0x100000c0L);
      case 117:
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x2000020L, active2, 0x200000001L, active3, 0x4000000L);
      case 122:
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x30000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 4);
}
static private final int jjMoveStringLiteralDfa5_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 4);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 4);
   }
   switch(curChar)
   {
      case 65:
         if ((active2 & 0x80000000000000L) != 0L)
         {
            jjmatchedKind = 183;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x8000L, active1, 0x6000000000004L, active2, 0x20001400010001L, active3, 0x800100040L);
      case 67:
         if ((active2 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 173;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0xc00000000000L, active1, 0x10000000L, active2, 0L, active3, 0L);
      case 68:
         if ((active2 & 0x200000000000000L) != 0L)
         {
            jjmatchedKind = 185;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x300000000L, active1, 0L, active2, 0x100000000000L, active3, 0x10L);
      case 69:
         if ((active0 & 0x40000000000000L) != 0L)
         {
            jjmatchedKind = 54;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x100L) != 0L)
         {
            jjmatchedKind = 72;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x10000L) != 0L)
         {
            jjmatchedKind = 80;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x200000L) != 0L)
         {
            jjmatchedKind = 85;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x800L) != 0L)
         {
            jjmatchedKind = 139;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x1000L) != 0L)
         {
            jjmatchedKind = 140;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 179;
            jjmatchedPos = 5;
         }
         else if ((active3 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 218;
            jjmatchedPos = 5;
         }
         else if ((active3 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 220;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x28000000L, active1, 0x100000000000000L, active2, 0x1000000000000L, active3, 0L);
      case 70:
         if ((active2 & 0x400000L) != 0L)
         {
            jjmatchedKind = 150;
            jjmatchedPos = 5;
         }
         break;
      case 71:
         if ((active1 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 108;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x400000000L, active2, 0L, active3, 0L);
      case 73:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0xc00000000c00L, active2, 0x6002000800000L, active3, 0x80001L);
      case 76:
         if ((active1 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 103;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x100000000000000L) != 0L)
         {
            jjmatchedKind = 184;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x20L, active2, 0x80000000000L, active3, 0x600000L);
      case 77:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x600L);
      case 78:
         if ((active0 & 0x1000L) != 0L)
         {
            jjmatchedKind = 12;
            jjmatchedPos = 5;
         }
         else if ((active0 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 44;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x8000L) != 0L)
         {
            jjmatchedKind = 79;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 157;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x1f04000000000000L, active1, 0x8006000L, active2, 0x8004L, active3, 0x1000000000L);
      case 79:
         return jjMoveStringLiteralDfa6_0(active0, 0x1000000000L, active1, 0x1001L, active2, 0x3400000000000000L, active3, 0x30000L);
      case 82:
         if ((active0 & 0x2000000000000000L) != 0L)
         {
            jjmatchedKind = 61;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0xb000001000000L, active1, 0xc8L, active2, 0x38000000000L, active3, 0x10a0L);
      case 83:
         if ((active1 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 90;
            jjmatchedPos = 5;
         }
         else if ((active3 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 226;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x10020000000000L, active1, 0x220000000000000L, active2, 0x4000000000L, active3, 0L);
      case 84:
         if ((active0 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x400000L) != 0L)
         {
            jjmatchedKind = 86;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x40000000000000L) != 0L)
         {
            jjmatchedKind = 118;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 161;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 187;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0xc0000400000L, active1, 0x2000000002800000L, active2, 0L, active3, 0x8000L);
      case 85:
         return jjMoveStringLiteralDfa6_0(active0, 0x800L, active1, 0L, active2, 0L, active3, 0L);
      case 86:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x400000000000000L, active2, 0L, active3, 0x8000000000L);
      case 87:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x8000000L);
      case 88:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x100000L, active2, 0L, active3, 0L);
      case 95:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0L, active2, 0x1000000L, active3, 0L);
      case 97:
         if ((active2 & 0x80000000000000L) != 0L)
         {
            jjmatchedKind = 183;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x8000L, active1, 0x6000000000004L, active2, 0x20001400010001L, active3, 0x800100040L);
      case 99:
         if ((active2 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 173;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0xc00000000000L, active1, 0x10000000L, active2, 0L, active3, 0L);
      case 100:
         if ((active2 & 0x200000000000000L) != 0L)
         {
            jjmatchedKind = 185;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x300000000L, active1, 0L, active2, 0x100000000000L, active3, 0x10L);
      case 101:
         if ((active0 & 0x40000000000000L) != 0L)
         {
            jjmatchedKind = 54;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x100L) != 0L)
         {
            jjmatchedKind = 72;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x10000L) != 0L)
         {
            jjmatchedKind = 80;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x200000L) != 0L)
         {
            jjmatchedKind = 85;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x800L) != 0L)
         {
            jjmatchedKind = 139;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x1000L) != 0L)
         {
            jjmatchedKind = 140;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 179;
            jjmatchedPos = 5;
         }
         else if ((active3 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 218;
            jjmatchedPos = 5;
         }
         else if ((active3 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 220;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x28000000L, active1, 0x100000000000000L, active2, 0x1000000000000L, active3, 0L);
      case 102:
         if ((active2 & 0x400000L) != 0L)
         {
            jjmatchedKind = 150;
            jjmatchedPos = 5;
         }
         break;
      case 103:
         if ((active1 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 108;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x400000000L, active2, 0L, active3, 0L);
      case 105:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0xc00000000c00L, active2, 0x6002000800000L, active3, 0x80001L);
      case 108:
         if ((active1 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 103;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x100000000000000L) != 0L)
         {
            jjmatchedKind = 184;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x20L, active2, 0x80000000000L, active3, 0x600000L);
      case 109:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x600L);
      case 110:
         if ((active0 & 0x1000L) != 0L)
         {
            jjmatchedKind = 12;
            jjmatchedPos = 5;
         }
         else if ((active0 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 44;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x8000L) != 0L)
         {
            jjmatchedKind = 79;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 157;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x1f04000000000000L, active1, 0x8006000L, active2, 0x8004L, active3, 0x1000000000L);
      case 111:
         return jjMoveStringLiteralDfa6_0(active0, 0x1000000000L, active1, 0x1001L, active2, 0x3400000000000000L, active3, 0x30000L);
      case 114:
         if ((active0 & 0x2000000000000000L) != 0L)
         {
            jjmatchedKind = 61;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0xb000001000000L, active1, 0xc8L, active2, 0x38000000000L, active3, 0x10a0L);
      case 115:
         if ((active1 & 0x4000000L) != 0L)
         {
            jjmatchedKind = 90;
            jjmatchedPos = 5;
         }
         else if ((active3 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 226;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0x10020000000000L, active1, 0x220000000000000L, active2, 0x4000000000L, active3, 0L);
      case 116:
         if ((active0 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x400000L) != 0L)
         {
            jjmatchedKind = 86;
            jjmatchedPos = 5;
         }
         else if ((active1 & 0x40000000000000L) != 0L)
         {
            jjmatchedKind = 118;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 161;
            jjmatchedPos = 5;
         }
         else if ((active2 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 187;
            jjmatchedPos = 5;
         }
         return jjMoveStringLiteralDfa6_0(active0, 0xc0000400000L, active1, 0x2000000002800000L, active2, 0L, active3, 0x8000L);
      case 117:
         return jjMoveStringLiteralDfa6_0(active0, 0x800L, active1, 0L, active2, 0L, active3, 0L);
      case 118:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x400000000000000L, active2, 0L, active3, 0x8000000000L);
      case 119:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x8000000L);
      case 120:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x100000L, active2, 0L, active3, 0L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 5);
}
static private final int jjMoveStringLiteralDfa6_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 5);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 5);
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa7_0(active0, 0x3000000000000L, active1, 0x400800008000040L, active2, 0x8000L, active3, 0x609000L);
      case 66:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x400L, active2, 0L, active3, 0L);
      case 67:
         if ((active2 & 0x800000L) != 0L)
         {
            jjmatchedKind = 151;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x20000000000L, active1, 0x4001L, active2, 0x24000000000000L, active3, 0x100000L);
      case 69:
         if ((active0 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 32;
            jjmatchedPos = 6;
         }
         else if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 42;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x8L) != 0L)
         {
            jjmatchedKind = 67;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 89;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 167;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x10L) != 0L)
         {
            jjmatchedKind = 196;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x200000000L, active1, 0x200000000100080L, active2, 0x80000000000L, active3, 0x8000000000L);
      case 71:
         if ((active0 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 36;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x4L) != 0L)
         {
            jjmatchedKind = 130;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 228;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0L, active2, 0x1L, active3, 0L);
      case 73:
         return jjMoveStringLiteralDfa7_0(active0, 0x80001400000L, active1, 0x2020000000800000L, active2, 0x4000000000L, active3, 0x80L);
      case 76:
         if ((active1 & 0x4L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x10000L) != 0L)
         {
            jjmatchedKind = 144;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 164;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x4000000000000L, active2, 0x1000000L, active3, 0L);
      case 78:
         if ((active0 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 27;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 98;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x400000000000000L) != 0L)
         {
            jjmatchedKind = 186;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x1000000000000000L) != 0L)
         {
            jjmatchedKind = 188;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 219;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x20000000L, active1, 0x2000L, active2, 0x2001000000000000L, active3, 0xb0001L);
      case 79:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0L, active2, 0x2000000000L, active3, 0x20L);
      case 80:
         return jjMoveStringLiteralDfa7_0(active0, 0x10000000000000L, active1, 0x800L, active2, 0x400000000L, active3, 0L);
      case 82:
         if ((active1 & 0x100000000000000L) != 0L)
         {
            jjmatchedKind = 120;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 227;
            jjmatchedPos = 6;
         }
         break;
      case 83:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x1000L, active2, 0L, active3, 0L);
      case 84:
         if ((active0 & 0x400000000000L) != 0L)
         {
            jjmatchedKind = 46;
            jjmatchedPos = 6;
         }
         else if ((active0 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 6;
         }
         else if ((active0 & 0x100000000000000L) != 0L)
         {
            jjmatchedKind = 56;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x20L) != 0L)
         {
            jjmatchedKind = 69;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 92;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x1e00800000008800L, active1, 0x2400000000000L, active2, 0L, active3, 0x40L);
      case 85:
         return jjMoveStringLiteralDfa7_0(active0, 0x4000000000000L, active1, 0L, active2, 0x100000000000L, active3, 0L);
      case 86:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0L, active2, 0x2010000000000L, active3, 0L);
      case 89:
         if ((active2 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 169;
            jjmatchedPos = 6;
         }
         break;
      case 95:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x600L);
      case 97:
         return jjMoveStringLiteralDfa7_0(active0, 0x3000000000000L, active1, 0x400800008000040L, active2, 0x8000L, active3, 0x609000L);
      case 98:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x400L, active2, 0L, active3, 0L);
      case 99:
         if ((active2 & 0x800000L) != 0L)
         {
            jjmatchedKind = 151;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x20000000000L, active1, 0x4001L, active2, 0x24000000000000L, active3, 0x100000L);
      case 101:
         if ((active0 & 0x100000000L) != 0L)
         {
            jjmatchedKind = 32;
            jjmatchedPos = 6;
         }
         else if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 42;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x8L) != 0L)
         {
            jjmatchedKind = 67;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 89;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 167;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x10L) != 0L)
         {
            jjmatchedKind = 196;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x200000000L, active1, 0x200000000100080L, active2, 0x80000000000L, active3, 0x8000000000L);
      case 103:
         if ((active0 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 36;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x4L) != 0L)
         {
            jjmatchedKind = 130;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 228;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0L, active2, 0x1L, active3, 0L);
      case 105:
         return jjMoveStringLiteralDfa7_0(active0, 0x80001400000L, active1, 0x2020000000800000L, active2, 0x4000000000L, active3, 0x80L);
      case 108:
         if ((active1 & 0x4L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x10000L) != 0L)
         {
            jjmatchedKind = 144;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 164;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x4000000000000L, active2, 0x1000000L, active3, 0L);
      case 110:
         if ((active0 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 27;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 98;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x400000000000000L) != 0L)
         {
            jjmatchedKind = 186;
            jjmatchedPos = 6;
         }
         else if ((active2 & 0x1000000000000000L) != 0L)
         {
            jjmatchedKind = 188;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 219;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x20000000L, active1, 0x2000L, active2, 0x2001000000000000L, active3, 0xb0001L);
      case 111:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0L, active2, 0x2000000000L, active3, 0x20L);
      case 112:
         return jjMoveStringLiteralDfa7_0(active0, 0x10000000000000L, active1, 0x800L, active2, 0x400000000L, active3, 0L);
      case 114:
         if ((active1 & 0x100000000000000L) != 0L)
         {
            jjmatchedKind = 120;
            jjmatchedPos = 6;
         }
         else if ((active3 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 227;
            jjmatchedPos = 6;
         }
         break;
      case 115:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x1000L, active2, 0L, active3, 0L);
      case 116:
         if ((active0 & 0x400000000000L) != 0L)
         {
            jjmatchedKind = 46;
            jjmatchedPos = 6;
         }
         else if ((active0 & 0x8000000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 6;
         }
         else if ((active0 & 0x100000000000000L) != 0L)
         {
            jjmatchedKind = 56;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x20L) != 0L)
         {
            jjmatchedKind = 69;
            jjmatchedPos = 6;
         }
         else if ((active1 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 92;
            jjmatchedPos = 6;
         }
         return jjMoveStringLiteralDfa7_0(active0, 0x1e00800000008800L, active1, 0x2400000000000L, active2, 0L, active3, 0x40L);
      case 117:
         return jjMoveStringLiteralDfa7_0(active0, 0x4000000000000L, active1, 0L, active2, 0x100000000000L, active3, 0L);
      case 118:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0L, active2, 0x2010000000000L, active3, 0L);
      case 121:
         if ((active2 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 169;
            jjmatchedPos = 6;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_0(0, 6);
}
static private final int jjMoveStringLiteralDfa7_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 6);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 6);
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x1L, active2, 0L, active3, 0L);
      case 66:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x40L, active2, 0L, active3, 0L);
      case 67:
         if ((active1 & 0x100000L) != 0L)
         {
            jjmatchedKind = 84;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x200000000000000L, active2, 0x1000000000000L, active3, 0L);
      case 68:
         if ((active0 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 7;
         }
         else if ((active1 & 0x80L) != 0L)
         {
            jjmatchedKind = 71;
            jjmatchedPos = 7;
         }
         break;
      case 69:
         if ((active0 & 0x800L) != 0L)
         {
            jjmatchedKind = 11;
            jjmatchedPos = 7;
         }
         else if ((active0 & 0x8000L) != 0L)
         {
            jjmatchedKind = 15;
            jjmatchedPos = 7;
         }
         else if ((active0 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 41;
            jjmatchedPos = 7;
         }
         else if ((active0 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 50;
            jjmatchedPos = 7;
         }
         else if ((active1 & 0x400L) != 0L)
         {
            jjmatchedKind = 74;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x1L) != 0L)
         {
            jjmatchedKind = 128;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 168;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 177;
            jjmatchedPos = 7;
         }
         else if ((active3 & 0x40L) != 0L)
         {
            jjmatchedKind = 198;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x2000L, active2, 0x1000000L, active3, 0x30000L);
      case 71:
         if ((active3 & 0x80000L) != 0L)
         {
            jjmatchedKind = 211;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0x20000000L, active1, 0L, active2, 0x80000000000L, active3, 0L);
      case 73:
         return jjMoveStringLiteralDfa8_0(active0, 0x3800000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 75:
         if ((active2 & 0x20000000000000L) != 0L)
         {
            jjmatchedKind = 181;
            jjmatchedPos = 7;
         }
         break;
      case 76:
         if ((active1 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 91;
            jjmatchedPos = 7;
         }
         else if ((active1 & 0x400000000000000L) != 0L)
         {
            jjmatchedKind = 122;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x8000L) != 0L)
         {
            jjmatchedKind = 143;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x4000000000000L, active2, 0L, active3, 0L);
      case 77:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x8000L);
      case 78:
         if ((active2 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 165;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x80L);
      case 79:
         return jjMoveStringLiteralDfa8_0(active0, 0x10080000400000L, active1, 0x2002000000800000L, active2, 0x4000000000L, active3, 0L);
      case 82:
         if ((active3 & 0x20L) != 0L)
         {
            jjmatchedKind = 197;
            jjmatchedPos = 7;
         }
         else if ((active3 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 231;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0x100000000000L, active3, 0x1000L);
      case 83:
         if ((active2 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 162;
            jjmatchedPos = 7;
         }
         break;
      case 84:
         if ((active1 & 0x4000L) != 0L)
         {
            jjmatchedKind = 78;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 178;
            jjmatchedPos = 7;
         }
         else if ((active3 & 0x1L) != 0L)
         {
            jjmatchedKind = 192;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x20800000001800L, active2, 0L, active3, 0x700200L);
      case 85:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x400L);
      case 89:
         if ((active1 & 0x400000000000L) != 0L)
         {
            jjmatchedKind = 110;
            jjmatchedPos = 7;
         }
         break;
      case 90:
         return jjMoveStringLiteralDfa8_0(active0, 0x1000000L, active1, 0L, active2, 0L, active3, 0L);
      case 95:
         return jjMoveStringLiteralDfa8_0(active0, 0x1e00000000000000L, active1, 0L, active2, 0x2000000000000000L, active3, 0L);
      case 97:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x1L, active2, 0L, active3, 0L);
      case 98:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x40L, active2, 0L, active3, 0L);
      case 99:
         if ((active1 & 0x100000L) != 0L)
         {
            jjmatchedKind = 84;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x200000000000000L, active2, 0x1000000000000L, active3, 0L);
      case 100:
         if ((active0 & 0x200000000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 7;
         }
         else if ((active1 & 0x80L) != 0L)
         {
            jjmatchedKind = 71;
            jjmatchedPos = 7;
         }
         break;
      case 101:
         if ((active0 & 0x800L) != 0L)
         {
            jjmatchedKind = 11;
            jjmatchedPos = 7;
         }
         else if ((active0 & 0x8000L) != 0L)
         {
            jjmatchedKind = 15;
            jjmatchedPos = 7;
         }
         else if ((active0 & 0x20000000000L) != 0L)
         {
            jjmatchedKind = 41;
            jjmatchedPos = 7;
         }
         else if ((active0 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 50;
            jjmatchedPos = 7;
         }
         else if ((active1 & 0x400L) != 0L)
         {
            jjmatchedKind = 74;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x1L) != 0L)
         {
            jjmatchedKind = 128;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 168;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 177;
            jjmatchedPos = 7;
         }
         else if ((active3 & 0x40L) != 0L)
         {
            jjmatchedKind = 198;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x2000L, active2, 0x1000000L, active3, 0x30000L);
      case 103:
         if ((active3 & 0x80000L) != 0L)
         {
            jjmatchedKind = 211;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0x20000000L, active1, 0L, active2, 0x80000000000L, active3, 0L);
      case 105:
         return jjMoveStringLiteralDfa8_0(active0, 0x3800000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 107:
         if ((active2 & 0x20000000000000L) != 0L)
         {
            jjmatchedKind = 181;
            jjmatchedPos = 7;
         }
         break;
      case 108:
         if ((active1 & 0x8000000L) != 0L)
         {
            jjmatchedKind = 91;
            jjmatchedPos = 7;
         }
         else if ((active1 & 0x400000000000000L) != 0L)
         {
            jjmatchedKind = 122;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x8000L) != 0L)
         {
            jjmatchedKind = 143;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x4000000000000L, active2, 0L, active3, 0L);
      case 109:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x8000L);
      case 110:
         if ((active2 & 0x2000000000L) != 0L)
         {
            jjmatchedKind = 165;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x80L);
      case 111:
         return jjMoveStringLiteralDfa8_0(active0, 0x10080000400000L, active1, 0x2002000000800000L, active2, 0x4000000000L, active3, 0L);
      case 114:
         if ((active3 & 0x20L) != 0L)
         {
            jjmatchedKind = 197;
            jjmatchedPos = 7;
         }
         else if ((active3 & 0x8000000000L) != 0L)
         {
            jjmatchedKind = 231;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0x100000000000L, active3, 0x1000L);
      case 115:
         if ((active2 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 162;
            jjmatchedPos = 7;
         }
         break;
      case 116:
         if ((active1 & 0x4000L) != 0L)
         {
            jjmatchedKind = 78;
            jjmatchedPos = 7;
         }
         else if ((active2 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 178;
            jjmatchedPos = 7;
         }
         else if ((active3 & 0x1L) != 0L)
         {
            jjmatchedKind = 192;
            jjmatchedPos = 7;
         }
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0x20800000001800L, active2, 0L, active3, 0x700200L);
      case 117:
         return jjMoveStringLiteralDfa8_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x400L);
      case 121:
         if ((active1 & 0x400000000000L) != 0L)
         {
            jjmatchedKind = 110;
            jjmatchedPos = 7;
         }
         break;
      case 122:
         return jjMoveStringLiteralDfa8_0(active0, 0x1000000L, active1, 0L, active2, 0L, active3, 0L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 7);
}
static private final int jjMoveStringLiteralDfa8_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 7);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 7);
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa9_0(active0, 0x1000000L, active1, 0L, active2, 0L, active3, 0L);
      case 67:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0x2000L, active2, 0L, active3, 0L);
      case 68:
         return jjMoveStringLiteralDfa9_0(active0, 0x200000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 69:
         if ((active1 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 111;
            jjmatchedPos = 8;
         }
         else if ((active2 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 172;
            jjmatchedPos = 8;
         }
         else if ((active3 & 0x200000L) != 0L)
         {
            jjmatchedKind = 213;
            jjmatchedPos = 8;
         }
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0L, active2, 0x1080000000000L, active3, 0L);
      case 71:
         if ((active3 & 0x80L) != 0L)
         {
            jjmatchedKind = 199;
            jjmatchedPos = 8;
         }
         break;
      case 73:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0x20000000001000L, active2, 0L, active3, 0x500200L);
      case 76:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0x40L, active2, 0L, active3, 0L);
      case 78:
         if ((active0 & 0x400000L) != 0L)
         {
            jjmatchedKind = 22;
            jjmatchedPos = 8;
         }
         else if ((active0 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 43;
            jjmatchedPos = 8;
         }
         else if ((active1 & 0x800000L) != 0L)
         {
            jjmatchedKind = 87;
            jjmatchedPos = 8;
         }
         else if ((active1 & 0x2000000000000000L) != 0L)
         {
            jjmatchedKind = 125;
            jjmatchedPos = 8;
         }
         else if ((active2 & 0x4000000000L) != 0L)
         {
            jjmatchedKind = 166;
            jjmatchedPos = 8;
         }
         return jjMoveStringLiteralDfa9_0(active0, 0x13000000000000L, active1, 0L, active2, 0x1000000L, active3, 0L);
      case 79:
         return jjMoveStringLiteralDfa9_0(active0, 0x800000000000L, active1, 0x800L, active2, 0L, active3, 0L);
      case 80:
         if ((active3 & 0x8000L) != 0L)
         {
            jjmatchedKind = 207;
            jjmatchedPos = 8;
         }
         break;
      case 82:
         if ((active1 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 113;
            jjmatchedPos = 8;
         }
         break;
      case 83:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x400L);
      case 84:
         if ((active1 & 0x200000000000000L) != 0L)
         {
            jjmatchedKind = 121;
            jjmatchedPos = 8;
         }
         return jjMoveStringLiteralDfa9_0(active0, 0xc00000020000000L, active1, 0x1L, active2, 0L, active3, 0L);
      case 85:
         return jjMoveStringLiteralDfa9_0(active0, 0x1000000000000000L, active1, 0L, active2, 0x2000000000000000L, active3, 0L);
      case 89:
         if ((active1 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 114;
            jjmatchedPos = 8;
         }
         else if ((active3 & 0x1000L) != 0L)
         {
            jjmatchedKind = 204;
            jjmatchedPos = 8;
         }
         break;
      case 95:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x30000L);
      case 97:
         return jjMoveStringLiteralDfa9_0(active0, 0x1000000L, active1, 0L, active2, 0L, active3, 0L);
      case 99:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0x2000L, active2, 0L, active3, 0L);
      case 100:
         return jjMoveStringLiteralDfa9_0(active0, 0x200000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 101:
         if ((active1 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 111;
            jjmatchedPos = 8;
         }
         else if ((active2 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 172;
            jjmatchedPos = 8;
         }
         else if ((active3 & 0x200000L) != 0L)
         {
            jjmatchedKind = 213;
            jjmatchedPos = 8;
         }
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0L, active2, 0x1080000000000L, active3, 0L);
      case 103:
         if ((active3 & 0x80L) != 0L)
         {
            jjmatchedKind = 199;
            jjmatchedPos = 8;
         }
         break;
      case 105:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0x20000000001000L, active2, 0L, active3, 0x500200L);
      case 108:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0x40L, active2, 0L, active3, 0L);
      case 110:
         if ((active0 & 0x400000L) != 0L)
         {
            jjmatchedKind = 22;
            jjmatchedPos = 8;
         }
         else if ((active0 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 43;
            jjmatchedPos = 8;
         }
         else if ((active1 & 0x800000L) != 0L)
         {
            jjmatchedKind = 87;
            jjmatchedPos = 8;
         }
         else if ((active1 & 0x2000000000000000L) != 0L)
         {
            jjmatchedKind = 125;
            jjmatchedPos = 8;
         }
         else if ((active2 & 0x4000000000L) != 0L)
         {
            jjmatchedKind = 166;
            jjmatchedPos = 8;
         }
         return jjMoveStringLiteralDfa9_0(active0, 0x13000000000000L, active1, 0L, active2, 0x1000000L, active3, 0L);
      case 111:
         return jjMoveStringLiteralDfa9_0(active0, 0x800000000000L, active1, 0x800L, active2, 0L, active3, 0L);
      case 112:
         if ((active3 & 0x8000L) != 0L)
         {
            jjmatchedKind = 207;
            jjmatchedPos = 8;
         }
         break;
      case 114:
         if ((active1 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 113;
            jjmatchedPos = 8;
         }
         break;
      case 115:
         return jjMoveStringLiteralDfa9_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x400L);
      case 116:
         if ((active1 & 0x200000000000000L) != 0L)
         {
            jjmatchedKind = 121;
            jjmatchedPos = 8;
         }
         return jjMoveStringLiteralDfa9_0(active0, 0xc00000020000000L, active1, 0x1L, active2, 0L, active3, 0L);
      case 117:
         return jjMoveStringLiteralDfa9_0(active0, 0x1000000000000000L, active1, 0L, active2, 0x2000000000000000L, active3, 0L);
      case 121:
         if ((active1 & 0x4000000000000L) != 0L)
         {
            jjmatchedKind = 114;
            jjmatchedPos = 8;
         }
         else if ((active3 & 0x1000L) != 0L)
         {
            jjmatchedKind = 204;
            jjmatchedPos = 8;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_0(0, 8);
}
static private final int jjMoveStringLiteralDfa9_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 8);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 8);
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa10_0(active0, 0x200000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 67:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0x1000L, active2, 0L, active3, 0L);
      case 68:
         return jjMoveStringLiteralDfa10_0(active0, 0x10000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 69:
         if ((active1 & 0x1L) != 0L)
         {
            jjmatchedKind = 64;
            jjmatchedPos = 9;
         }
         else if ((active1 & 0x40L) != 0L)
         {
            jjmatchedKind = 70;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x400L);
      case 71:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0x1000000L, active3, 0L);
      case 72:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 29;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x10000L);
      case 73:
         return jjMoveStringLiteralDfa10_0(active0, 0xc00000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 77:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x20200L);
      case 78:
         if ((active0 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 47;
            jjmatchedPos = 9;
         }
         break;
      case 79:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x500000L);
      case 82:
         if ((active1 & 0x800L) != 0L)
         {
            jjmatchedKind = 75;
            jjmatchedPos = 9;
         }
         break;
      case 83:
         if ((active2 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 171;
            jjmatchedPos = 9;
         }
         else if ((active2 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 176;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0x1000000000000000L, active1, 0L, active2, 0x2000000000000000L, active3, 0L);
      case 84:
         if ((active0 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 48;
            jjmatchedPos = 9;
         }
         else if ((active1 & 0x2000L) != 0L)
         {
            jjmatchedKind = 77;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0x2000001000000L, active1, 0L, active2, 0L, active3, 0L);
      case 86:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0x20000000000000L, active2, 0L, active3, 0L);
      case 97:
         return jjMoveStringLiteralDfa10_0(active0, 0x200000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 99:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0x1000L, active2, 0L, active3, 0L);
      case 100:
         return jjMoveStringLiteralDfa10_0(active0, 0x10000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 101:
         if ((active1 & 0x1L) != 0L)
         {
            jjmatchedKind = 64;
            jjmatchedPos = 9;
         }
         else if ((active1 & 0x40L) != 0L)
         {
            jjmatchedKind = 70;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x400L);
      case 103:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0x1000000L, active3, 0L);
      case 104:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 29;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x10000L);
      case 105:
         return jjMoveStringLiteralDfa10_0(active0, 0xc00000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 109:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x20200L);
      case 110:
         if ((active0 & 0x800000000000L) != 0L)
         {
            jjmatchedKind = 47;
            jjmatchedPos = 9;
         }
         break;
      case 111:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x500000L);
      case 114:
         if ((active1 & 0x800L) != 0L)
         {
            jjmatchedKind = 75;
            jjmatchedPos = 9;
         }
         break;
      case 115:
         if ((active2 & 0x80000000000L) != 0L)
         {
            jjmatchedKind = 171;
            jjmatchedPos = 9;
         }
         else if ((active2 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 176;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0x1000000000000000L, active1, 0L, active2, 0x2000000000000000L, active3, 0L);
      case 116:
         if ((active0 & 0x1000000000000L) != 0L)
         {
            jjmatchedKind = 48;
            jjmatchedPos = 9;
         }
         else if ((active1 & 0x2000L) != 0L)
         {
            jjmatchedKind = 77;
            jjmatchedPos = 9;
         }
         return jjMoveStringLiteralDfa10_0(active0, 0x2000001000000L, active1, 0L, active2, 0L, active3, 0L);
      case 118:
         return jjMoveStringLiteralDfa10_0(active0, 0L, active1, 0x20000000000000L, active2, 0L, active3, 0L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 9);
}
static private final int jjMoveStringLiteralDfa10_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 9);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 9);
   }
   switch(curChar)
   {
      case 69:
         if ((active1 & 0x20000000000000L) != 0L)
         {
            jjmatchedKind = 117;
            jjmatchedPos = 10;
         }
         else if ((active3 & 0x200L) != 0L)
         {
            jjmatchedKind = 201;
            jjmatchedPos = 10;
         }
         return jjMoveStringLiteralDfa11_0(active0, 0x1000000000000000L, active1, 0L, active2, 0x2000000000000000L, active3, 0L);
      case 73:
         return jjMoveStringLiteralDfa11_0(active0, 0x10000001000000L, active1, 0L, active2, 0L, active3, 0x20000L);
      case 77:
         return jjMoveStringLiteralDfa11_0(active0, 0xc00000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 78:
         if ((active3 & 0x100000L) != 0L)
         {
            jjmatchedKind = 212;
            jjmatchedPos = 10;
         }
         else if ((active3 & 0x400000L) != 0L)
         {
            jjmatchedKind = 214;
            jjmatchedPos = 10;
         }
         break;
      case 79:
         return jjMoveStringLiteralDfa11_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x10000L);
      case 82:
         if ((active3 & 0x400L) != 0L)
         {
            jjmatchedKind = 202;
            jjmatchedPos = 10;
         }
         break;
      case 83:
         if ((active0 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 49;
            jjmatchedPos = 10;
         }
         else if ((active1 & 0x1000L) != 0L)
         {
            jjmatchedKind = 76;
            jjmatchedPos = 10;
         }
         break;
      case 84:
         return jjMoveStringLiteralDfa11_0(active0, 0x200000000000000L, active1, 0L, active2, 0x1000000L, active3, 0L);
      case 101:
         if ((active1 & 0x20000000000000L) != 0L)
         {
            jjmatchedKind = 117;
            jjmatchedPos = 10;
         }
         else if ((active3 & 0x200L) != 0L)
         {
            jjmatchedKind = 201;
            jjmatchedPos = 10;
         }
         return jjMoveStringLiteralDfa11_0(active0, 0x1000000000000000L, active1, 0L, active2, 0x2000000000000000L, active3, 0L);
      case 105:
         return jjMoveStringLiteralDfa11_0(active0, 0x10000001000000L, active1, 0L, active2, 0L, active3, 0x20000L);
      case 109:
         return jjMoveStringLiteralDfa11_0(active0, 0xc00000000000000L, active1, 0L, active2, 0L, active3, 0L);
      case 110:
         if ((active3 & 0x100000L) != 0L)
         {
            jjmatchedKind = 212;
            jjmatchedPos = 10;
         }
         else if ((active3 & 0x400000L) != 0L)
         {
            jjmatchedKind = 214;
            jjmatchedPos = 10;
         }
         break;
      case 111:
         return jjMoveStringLiteralDfa11_0(active0, 0L, active1, 0L, active2, 0L, active3, 0x10000L);
      case 114:
         if ((active3 & 0x400L) != 0L)
         {
            jjmatchedKind = 202;
            jjmatchedPos = 10;
         }
         break;
      case 115:
         if ((active0 & 0x2000000000000L) != 0L)
         {
            jjmatchedKind = 49;
            jjmatchedPos = 10;
         }
         else if ((active1 & 0x1000L) != 0L)
         {
            jjmatchedKind = 76;
            jjmatchedPos = 10;
         }
         break;
      case 116:
         return jjMoveStringLiteralDfa11_0(active0, 0x200000000000000L, active1, 0L, active2, 0x1000000L, active3, 0L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 10);
}
static private final int jjMoveStringLiteralDfa11_0(long old0, long active0, long old1, long active1, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active1 &= old1) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 10);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 10);
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x200000000000000L) != 0L)
         {
            jjmatchedKind = 57;
            jjmatchedPos = 11;
         }
         else if ((active0 & 0x400000000000000L) != 0L)
         {
            jjmatchedKind = 58;
            jjmatchedPos = 11;
         }
         return jjMoveStringLiteralDfa12_0(active0, 0x800000000000000L, active2, 0L, active3, 0L);
      case 72:
         if ((active2 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 152;
            jjmatchedPos = 11;
         }
         break;
      case 78:
         return jjMoveStringLiteralDfa12_0(active0, 0x10000000000000L, active2, 0L, active3, 0x20000L);
      case 79:
         return jjMoveStringLiteralDfa12_0(active0, 0x1000000L, active2, 0L, active3, 0L);
      case 82:
         if ((active0 & 0x1000000000000000L) != 0L)
         {
            jjmatchedKind = 60;
            jjmatchedPos = 11;
         }
         else if ((active2 & 0x2000000000000000L) != 0L)
         {
            jjmatchedKind = 189;
            jjmatchedPos = 11;
         }
         break;
      case 85:
         return jjMoveStringLiteralDfa12_0(active0, 0L, active2, 0L, active3, 0x10000L);
      case 101:
         if ((active0 & 0x200000000000000L) != 0L)
         {
            jjmatchedKind = 57;
            jjmatchedPos = 11;
         }
         else if ((active0 & 0x400000000000000L) != 0L)
         {
            jjmatchedKind = 58;
            jjmatchedPos = 11;
         }
         return jjMoveStringLiteralDfa12_0(active0, 0x800000000000000L, active2, 0L, active3, 0L);
      case 104:
         if ((active2 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 152;
            jjmatchedPos = 11;
         }
         break;
      case 110:
         return jjMoveStringLiteralDfa12_0(active0, 0x10000000000000L, active2, 0L, active3, 0x20000L);
      case 111:
         return jjMoveStringLiteralDfa12_0(active0, 0x1000000L, active2, 0L, active3, 0L);
      case 114:
         if ((active0 & 0x1000000000000000L) != 0L)
         {
            jjmatchedKind = 60;
            jjmatchedPos = 11;
         }
         else if ((active2 & 0x2000000000000000L) != 0L)
         {
            jjmatchedKind = 189;
            jjmatchedPos = 11;
         }
         break;
      case 117:
         return jjMoveStringLiteralDfa12_0(active0, 0L, active2, 0L, active3, 0x10000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 11);
}
static private final int jjMoveStringLiteralDfa12_0(long old0, long active0, long old2, long active2, long old3, long active3)
{
   if (((active0 &= old0) | (active2 &= old2) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 11);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 11);
   }
   switch(curChar)
   {
      case 71:
         if ((active0 & 0x10000000000000L) != 0L)
         {
            jjmatchedKind = 52;
            jjmatchedPos = 12;
         }
         break;
      case 78:
         if ((active0 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 24;
            jjmatchedPos = 12;
         }
         break;
      case 82:
         if ((active3 & 0x10000L) != 0L)
         {
            jjmatchedKind = 208;
            jjmatchedPos = 12;
         }
         break;
      case 83:
         return jjMoveStringLiteralDfa13_0(active0, 0x800000000000000L, active3, 0L);
      case 85:
         return jjMoveStringLiteralDfa13_0(active0, 0L, active3, 0x20000L);
      case 103:
         if ((active0 & 0x10000000000000L) != 0L)
         {
            jjmatchedKind = 52;
            jjmatchedPos = 12;
         }
         break;
      case 110:
         if ((active0 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 24;
            jjmatchedPos = 12;
         }
         break;
      case 114:
         if ((active3 & 0x10000L) != 0L)
         {
            jjmatchedKind = 208;
            jjmatchedPos = 12;
         }
         break;
      case 115:
         return jjMoveStringLiteralDfa13_0(active0, 0x800000000000000L, active3, 0L);
      case 117:
         return jjMoveStringLiteralDfa13_0(active0, 0L, active3, 0x20000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 12);
}
static private final int jjMoveStringLiteralDfa13_0(long old0, long active0, long old3, long active3)
{
   if (((active0 &= old0) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 12);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 12);
   }
   switch(curChar)
   {
      case 84:
         return jjMoveStringLiteralDfa14_0(active0, 0x800000000000000L, active3, 0x20000L);
      case 116:
         return jjMoveStringLiteralDfa14_0(active0, 0x800000000000000L, active3, 0x20000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 13);
}
static private final int jjMoveStringLiteralDfa14_0(long old0, long active0, long old3, long active3)
{
   if (((active0 &= old0) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 13);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 13);
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa15_0(active0, 0x800000000000000L, active3, 0L);
      case 69:
         if ((active3 & 0x20000L) != 0L)
         {
            jjmatchedKind = 209;
            jjmatchedPos = 14;
         }
         break;
      case 97:
         return jjMoveStringLiteralDfa15_0(active0, 0x800000000000000L, active3, 0L);
      case 101:
         if ((active3 & 0x20000L) != 0L)
         {
            jjmatchedKind = 209;
            jjmatchedPos = 14;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_0(0, 14);
}
static private final int jjMoveStringLiteralDfa15_0(long old0, long active0, long old3, long active3)
{
   if (((active0 &= old0) | (active3 &= old3)) == 0L)
      return jjMoveNfa_0(0, 14);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 14);
   }
   switch(curChar)
   {
      case 77:
         return jjMoveStringLiteralDfa16_0(active0, 0x800000000000000L);
      case 109:
         return jjMoveStringLiteralDfa16_0(active0, 0x800000000000000L);
      default :
         break;
   }
   return jjMoveNfa_0(0, 15);
}
static private final int jjMoveStringLiteralDfa16_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjMoveNfa_0(0, 15);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
   return jjMoveNfa_0(0, 15);
   }
   switch(curChar)
   {
      case 80:
         if ((active0 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 59;
            jjmatchedPos = 16;
         }
         break;
      case 112:
         if ((active0 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 59;
            jjmatchedPos = 16;
         }
         break;
      default :
         break;
   }
   return jjMoveNfa_0(0, 16);
}
static private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
static private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
static private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
static private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
static private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static private final int jjMoveNfa_0(int startState, int curPos)
{
   int strKind = jjmatchedKind;
   int strPos = jjmatchedPos;
   int seenUpto;
   input_stream.backup(seenUpto = curPos + 1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { throw new Error("Internal Error"); }
   curPos = 0;
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 101;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 251)
                        kind = 251;
                     jjCheckNAddStates(0, 4);
                  }
                  else if (curChar == 46)
                     jjCheckNAddStates(5, 7);
                  else if (curChar == 39)
                     jjCheckNAddTwoStates(37, 40);
                  else if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 22;
                  else if (curChar == 34)
                     jjCheckNAddStates(8, 11);
                  break;
               case 1:
                  if (curChar == 34 && kind > 10)
                     kind = 10;
                  break;
               case 3:
                  if (curChar == 58)
                     jjCheckNAddStates(12, 14);
                  break;
               case 4:
                  if ((0x3ff400000000000L & l) != 0L)
                     jjCheckNAddStates(15, 17);
                  break;
               case 5:
                  if (curChar == 47)
                     jjCheckNAddStates(18, 21);
                  break;
               case 6:
                  if (curChar == 42)
                     jjCheckNAdd(1);
                  break;
               case 7:
                  if ((0x3ff400000000000L & l) != 0L)
                     jjCheckNAddStates(22, 24);
                  break;
               case 8:
                  if (curChar == 47)
                     jjCheckNAddStates(25, 29);
                  break;
               case 9:
                  if (curChar == 58)
                     jjCheckNAddTwoStates(10, 14);
                  break;
               case 10:
                  if ((0x3ff400000000000L & l) != 0L)
                     jjCheckNAddTwoStates(10, 11);
                  break;
               case 11:
                  if (curChar == 47)
                     jjCheckNAddStates(30, 33);
                  break;
               case 12:
                  if ((0x3ff400000000000L & l) != 0L)
                     jjCheckNAddTwoStates(12, 1);
                  break;
               case 13:
                  if ((0x3ff400000000000L & l) != 0L)
                     jjCheckNAddTwoStates(13, 11);
                  break;
               case 14:
                  if (curChar == 47)
                     jjCheckNAddStates(34, 38);
                  break;
               case 15:
                  if ((0x3ff400000000000L & l) != 0L)
                     jjCheckNAddStates(39, 43);
                  break;
               case 16:
                  if (curChar == 47)
                     jjCheckNAddStates(44, 52);
                  break;
               case 18:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 246)
                     kind = 246;
                  jjstateSet[jjnewStateCnt++] = 18;
                  break;
               case 20:
                  if (curChar == 58 && kind > 247)
                     kind = 247;
                  break;
               case 21:
                  if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 22;
                  break;
               case 23:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 248)
                     kind = 248;
                  jjstateSet[jjnewStateCnt++] = 23;
                  break;
               case 25:
                  if (curChar == 39)
                     jjCheckNAddTwoStates(26, 27);
                  break;
               case 26:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(26, 27);
                  break;
               case 27:
                  if (curChar != 39)
                     break;
                  if (kind > 255)
                     kind = 255;
                  jjstateSet[jjnewStateCnt++] = 28;
                  break;
               case 28:
                  if (curChar == 39)
                     jjCheckNAddTwoStates(29, 27);
                  break;
               case 29:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(29, 27);
                  break;
               case 31:
                  if (curChar == 39)
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 32:
                  if ((0x3000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 33:
                  if (curChar != 39)
                     break;
                  if (kind > 256)
                     kind = 256;
                  jjstateSet[jjnewStateCnt++] = 34;
                  break;
               case 34:
                  if (curChar == 39)
                     jjCheckNAddTwoStates(35, 33);
                  break;
               case 35:
                  if ((0x3000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(35, 33);
                  break;
               case 36:
                  if (curChar == 39)
                     jjCheckNAddTwoStates(37, 40);
                  break;
               case 37:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(37, 38);
                  break;
               case 38:
                  if (curChar != 39)
                     break;
                  if (kind > 257)
                     kind = 257;
                  jjCheckNAdd(39);
                  break;
               case 39:
                  if (curChar == 39)
                     jjCheckNAddTwoStates(37, 38);
                  break;
               case 40:
                  if (curChar == 39)
                     jjCheckNAdd(39);
                  break;
               case 52:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 251)
                     kind = 251;
                  jjCheckNAddStates(0, 4);
                  break;
               case 53:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 251)
                     kind = 251;
                  jjCheckNAddTwoStates(53, 54);
                  break;
               case 54:
                  if (curChar == 46)
                     jjCheckNAdd(55);
                  break;
               case 55:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 251)
                     kind = 251;
                  jjCheckNAdd(55);
                  break;
               case 56:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(53, 55);
                  break;
               case 57:
                  if (curChar == 46)
                     jjCheckNAdd(58);
                  break;
               case 58:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(58, 59);
                  break;
               case 60:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(61);
                  break;
               case 61:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 252)
                     kind = 252;
                  jjCheckNAdd(61);
                  break;
               case 62:
                  if (curChar == 46)
                     jjCheckNAddStates(5, 7);
                  break;
               case 63:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 251)
                     kind = 251;
                  jjCheckNAdd(63);
                  break;
               case 64:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(64, 59);
                  break;
               case 65:
                  if (curChar == 46 && kind > 262)
                     kind = 262;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 246)
                        kind = 246;
                     jjCheckNAdd(18);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 20;
                  if ((0x800000008L & l) != 0L)
                     jjAddStates(56, 59);
                  else if ((0x1000000010L & l) != 0L)
                     jjAddStates(60, 61);
                  else if (curChar == 66)
                     jjstateSet[jjnewStateCnt++] = 31;
                  else if (curChar == 88)
                     jjstateSet[jjnewStateCnt++] = 25;
                  break;
               case 2:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjAddStates(62, 63);
                  break;
               case 4:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddStates(15, 17);
                  break;
               case 7:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddStates(22, 24);
                  break;
               case 10:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(10, 11);
                  break;
               case 12:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(12, 1);
                  break;
               case 13:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(13, 11);
                  break;
               case 15:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddStates(39, 43);
                  break;
               case 17:
               case 18:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 246)
                     kind = 246;
                  jjCheckNAdd(18);
                  break;
               case 19:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 20;
                  break;
               case 22:
               case 23:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 248)
                     kind = 248;
                  jjCheckNAdd(23);
                  break;
               case 24:
                  if (curChar == 88)
                     jjstateSet[jjnewStateCnt++] = 25;
                  break;
               case 26:
                  if ((0x7e0000007eL & l) != 0L)
                     jjCheckNAddTwoStates(26, 27);
                  break;
               case 29:
                  if ((0x7e0000007eL & l) != 0L)
                     jjCheckNAddTwoStates(29, 27);
                  break;
               case 30:
                  if (curChar == 66)
                     jjstateSet[jjnewStateCnt++] = 31;
                  break;
               case 37:
                  jjAddStates(64, 65);
                  break;
               case 41:
                  if ((0x1000000010L & l) != 0L)
                     jjAddStates(60, 61);
                  break;
               case 42:
                  if ((0x4000000040000L & l) != 0L && kind > 68)
                     kind = 68;
                  break;
               case 43:
                  if ((0x20000000200L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 42;
                  break;
               case 44:
                  if ((0x200000002000000L & l) != 0L && kind > 68)
                     kind = 68;
                  break;
               case 45:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 44;
                  break;
               case 46:
                  if ((0x800000008000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 45;
                  break;
               case 47:
                  if ((0x10000000100000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 46;
                  break;
               case 48:
                  if ((0x800000008L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 47;
                  break;
               case 49:
                  if ((0x2000000020L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 48;
                  break;
               case 50:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 49;
                  break;
               case 51:
                  if ((0x20000000200L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 50;
                  break;
               case 59:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(66, 67);
                  break;
               case 66:
                  if ((0x800000008L & l) != 0L)
                     jjAddStates(56, 59);
                  break;
               case 67:
                  if ((0x4000000040000L & l) != 0L && kind > 37)
                     kind = 37;
                  break;
               case 68:
                  if ((0x2000000020L & l) != 0L)
                     jjCheckNAdd(67);
                  break;
               case 69:
                  if ((0x10000000100000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 68;
                  break;
               case 70:
                  if ((0x800000008L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 69;
                  break;
               case 71:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 70;
                  break;
               case 72:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 71;
                  break;
               case 73:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 72;
                  break;
               case 74:
                  if ((0x10000000100L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 73;
                  break;
               case 75:
                  if ((0x200000002L & l) != 0L)
                     jjCheckNAdd(67);
                  break;
               case 76:
                  if ((0x10000000100L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 75;
                  break;
               case 77:
                  if ((0x10000000100L & l) != 0L && kind > 38)
                     kind = 38;
                  break;
               case 78:
               case 87:
                  if ((0x10000000100000L & l) != 0L)
                     jjCheckNAdd(77);
                  break;
               case 79:
                  if ((0x8000000080L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 78;
                  break;
               case 80:
                  if ((0x400000004000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 79;
                  break;
               case 81:
                  if ((0x2000000020L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 80;
                  break;
               case 82:
                  if ((0x100000001000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 81;
                  break;
               case 83:
                  if (curChar == 95)
                     jjstateSet[jjnewStateCnt++] = 82;
                  break;
               case 84:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 83;
                  break;
               case 85:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 84;
                  break;
               case 86:
                  if ((0x10000000100L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 85;
                  break;
               case 88:
                  if ((0x8000000080L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 87;
                  break;
               case 89:
                  if ((0x400000004000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 88;
                  break;
               case 90:
                  if ((0x2000000020L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 89;
                  break;
               case 91:
                  if ((0x100000001000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 90;
                  break;
               case 92:
                  if (curChar == 95)
                     jjstateSet[jjnewStateCnt++] = 91;
                  break;
               case 93:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 92;
                  break;
               case 94:
                  if ((0x2000000020L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 93;
                  break;
               case 95:
                  if ((0x10000000100000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 94;
                  break;
               case 96:
                  if ((0x800000008L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 95;
                  break;
               case 97:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 96;
                  break;
               case 98:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 97;
                  break;
               case 99:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 98;
                  break;
               case 100:
                  if ((0x10000000100L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 99;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 37:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(64, 65);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 101 - (jjnewStateCnt = startsAt)))
         break;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { break; }
   }
   if (jjmatchedPos > strPos)
      return curPos;

   int toRet = Math.max(curPos, seenUpto);

   if (curPos < toRet)
      for (i = toRet - Math.min(curPos, seenUpto); i-- > 0; )
         try { curChar = input_stream.readChar(); }
         catch(java.io.IOException e) { throw new Error("Internal Error : Please send a bug report."); }

   if (jjmatchedPos < strPos)
   {
      jjmatchedKind = strKind;
      jjmatchedPos = strPos;
   }
   else if (jjmatchedPos == strPos && jjmatchedKind > strKind)
      jjmatchedKind = strKind;

   return toRet;
}
static final int[] jjnextStates = {
   53, 54, 56, 57, 59, 63, 64, 65, 1, 2, 15, 16, 1, 4, 8, 4, 
   1, 5, 6, 1, 5, 7, 7, 1, 5, 6, 1, 5, 7, 4, 1, 12, 
   11, 13, 1, 12, 11, 13, 10, 4, 10, 1, 11, 5, 6, 1, 12, 11, 
   13, 10, 5, 7, 4, 56, 57, 59, 74, 76, 86, 100, 43, 51, 3, 9, 
   37, 38, 60, 61, 
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, "\100", null, null, null, null, null, 
null, null, null, null, null, null, null, "\74\76", "\76\75", "\74\75", "\174\174", 
null, "\74", "\75", "\76", "\42", "\45", "\46", "\47", "\50", "\51", "\52", "\53", 
"\54", "\55", "\56", "\57", "\72", "\73", "\77", "\133", "\135", "\137", "\174", };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0xfffffffffffffc01L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0x99e03fffffffffffL, 
   0x1fffffffL, 
};
static final long[] jjtoSkip = {
   0x3eL, 0x0L, 0x0L, 0x0L, 
   0x0L, 
};
static private ASCII_CharStream input_stream;
static private final int[] jjrounds = new int[101];
static private final int[] jjstateSet = new int[202];
static protected char curChar;
public SQLParserTokenManager(ASCII_CharStream stream)
{
   if (input_stream != null)
      throw new TokenMgrError("ERROR: Second call to constructor of static lexer. You must use ReInit() to initialize the static variables.", TokenMgrError.STATIC_LEXER_ERROR);
   input_stream = stream;
}
public SQLParserTokenManager(ASCII_CharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
static public void ReInit(ASCII_CharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
static private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 101; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
static public void ReInit(ASCII_CharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
static public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

static private final Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

static int curLexState = 0;
static int defaultLexState = 0;
static int jjnewStateCnt;
static int jjround;
static int jjmatchedPos;
static int jjmatchedKind;

public static final Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
