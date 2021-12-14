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

package com.percussion.debug;


import java.util.Arrays;

/**
 * Wrapper object for doing bitwise operations on a set of 120 possible flags.  Contains a list of 4 groups of flags.  Each flag is an int used to specify a trace option.  The highest two bits of each flag are reserved as an indicator of 4 possible groups (00-11).  Thus there is support for 30 flags in each of 4 groups.
 */
public class PSTraceFlag
{
   /**
    * Replaces this objects internal flag with the provided flag for the specfied group.
    *
    * @param flag a flag providing all of the bits to be set
    * @param group Indicates which group this flag belongs to
    * @roseuid 39F6CE6F031C
    */
   public void setFlag(int flag, int group)
   {
      try
      {
         m_traceFlags[group] = flag;
      }
      catch (IndexOutOfBoundsException e)
      {
         // this exception will be much more informative
         throw new IllegalArgumentException(
            "PSTraceFlag: Invalid group specified. Groups must be between 0 and 3.");
      }
   }

   /**
    * Used to retreive this object's internal flag for the specified group
    *
    * @param group Indicates which group this flag belongs to
    * @return this objects flag as an integer
    * @roseuid 39F6CF4701E4
    */
   public int getFlag(int group)
   {
      int i = 0;
      try
      {
         i = m_traceFlags[group];
      }
      catch (IndexOutOfBoundsException e)
      {
         // this exception will be much more informative
         throw new IllegalArgumentException(
            "PSTraceFlag: Invalid group specified. Groups must be between 0 and 3.");
      }

      return i;
   }

   /**
    * Compares the provided flag with the internal flag for the specified group
    * @param flag a flag providing the option and group bits to be checked
    * @return <code>true</code> if the all of the bits of the incoming flag are also on in the internal flag for that group
    * @roseuid 39F6CF910177
    */
   public boolean checkBit(int flag)
   {

      // get group by unsigned shifting group bits all the way over to the right
      int group = flag >>> 30;

      // use mask to get just the flag and check it against the group
      return ((flag & GROUP_MASK) & m_traceFlags[group]) != 0;
   }

   /**
    * Sets bit on the internal flag using the option and group bits in the provided flag.  Will only turn on bits, will not clear any bits.
    * @param flag a flag providing the bit to be set
    * @roseuid 39F6D60F02FD
    */
   public void setBit(int flag)
   {

      // get group by unsigned shifting group bits all the way over to the right
      int group = flag >>> 30;

      // use mask to get just the flag and set it against the group
      m_traceFlags[group] |= (flag & GROUP_MASK);
   }

   /**
    * Returns a string reprepresentation of this object.  Masks the group bit
    * before returning the string.
    *
    * @param group Indicates which group this flag belongs to
    * @return a string representation of this object
    * @roseuid 39F6D6A6001F
    */
   public String toString(int group)
   {
      try
      {
         return "0x" + Integer.toHexString(m_traceFlags[group] & GROUP_MASK).toUpperCase();
      }
      catch (IndexOutOfBoundsException e)
      {
         // this exception will be much more informative
         throw new IllegalArgumentException(
            "PSTraceFlag: Invalid group specified. Groups must be between 0 and 3.");
      }
   }

   /**
    * Constructor with no arguments.  Initializes all groups to <code>zero</code>.
    * @roseuid 39F701CB03A9
    */
   public PSTraceFlag()
   {
      this(0, 0, 0, 0);
   }

   /**
    * Constructor with 1 argument.  Initializes the first groups flag, and all other
    * groups to <code>zero</code>.
    *
    * @param flag1 The flag for the first group.
    * @roseuid 39F702130232
    */
   public PSTraceFlag(int flag1)
   {
      this(flag1, 0, 0, 0);
   }

   /**
    * Constructor with 2 arguments.  Initializes the first 2 group's flags, and the
    * last two groups to <code>zero</code>.
    *
    * @param flag1 The flag for the first group.
    * @param flag2 The flag for the second group.
    * @roseuid 39F702A302EE
    */
   public PSTraceFlag(int flag1, int flag2)
   {
      this(flag1, flag2, 0, 0);
   }

   /**
    * Constructor with 3 arguments.  Initializes the first 3 group's flags, and the
    * last group to <code>zero</code>.
    * @param flag1 The flag for the first group.
    * @param flag2 The flag for the second group.
    * @param flag3 The flag for the third group.
    * @roseuid 39F7035C00DA
    */
   public PSTraceFlag(int flag1, int flag2, int flag3)
   {
      this(flag1, flag2, flag3, 0);
   }

   /**
    * Constructor with 4 arguments.  Initializes the all 4 group flags.
    * @param flag1 The flag for the first group.
    * @param flag2 The flag for the second group.
    * @param flag3 The flag for the third group.
    * @param flag4 The flag for the fourth group.
    * @roseuid 39F7038100BB
    */
   public PSTraceFlag(int flag1, int flag2, int flag3, int flag4)
   {
      m_traceFlags[0] = flag1;
      m_traceFlags[1] = flag2;
      m_traceFlags[2] = flag3;
      m_traceFlags[3] = flag4;
   }

   /**
    * Clears the bit in the group specified by the flag
    *
    * @param flag flag which specifies option bit and group bit.
    * @roseuid 3A02E5CF02BF
    */
   public void clearBit(int flag)
   {
      // get group by unsigned shifting group bits all the way over to the right
      int group = flag >>> 30;

      // use mask to get just the flag and clear it against the group if already set
      flag &= GROUP_MASK;
      if ((flag & m_traceFlags[group]) != 0)
         m_traceFlags[group] ^= flag;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTraceFlag)) return false;
      PSTraceFlag that = (PSTraceFlag) o;
      return Arrays.equals(m_traceFlags, that.m_traceFlags);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(m_traceFlags);
   }

   /*
    * convenience method to determine if any flag in any group is non-zero.
    *
    * @return <code>true</code> if any of the group flags are non-zero,
    * <code>false</code> if not.
    */
   public boolean isTraceEnabled()
   {
      boolean isEnabled = false;
      for (int i = 0; i < m_traceFlags.length; i++)
      {
         if (m_traceFlags[i] != 0)
         {
            isEnabled = true;
            break;
         }
      }

      return isEnabled;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component.
    *
    * @param flag a valid PSTraceFlag. If null, a IllegalArgumentException is
    * thrown.
    */
   public void copyFrom( PSTraceFlag flag )
   {
      if (null == flag)
         throw new IllegalArgumentException("Invalid object for copy");

      for (int i = 0; i < m_traceFlags.length; i++)
      {
         m_traceFlags[i] = flag.getFlag(i);
      }

   }

   /**
    * A list of 4 groups of flags.
    */
   private int[] m_traceFlags = {0, 0, 0, 0};

   /**
    * Flag with only the group bits set to specify this group.
    */
   public static final int GROUP1 = 0x0;

   /**
    * Flag with only the group bits set to specify this group.
    */
   public static final int GROUP2 = 0x40000000;

   /**
    * Flag with only the group bits set to specify this group.
    */
   public static final int GROUP3 = 0x80000000;

   /**
    * Flag with only the group bits set to specify this group.
    */
   public static final int GROUP4 = 0xC0000000;

   /**
    * used to mask the the group specified by a flag
    */
   private static final int GROUP_MASK = 0x3FFFFFFF;

}
