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
package com.percussion.util;

import com.percussion.utils.tools.PSPatternMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * A file filter that allows filtering on attributes, length, last modified,
 * and name pattern matching
 */
public class PSFileFilter implements java.io.FileFilter, java.io.FilenameFilter
{
   /**
    * Bit fields for allowable attributes
    */
   public static final int IS_DIRECTORY = 1;
   public static final int IS_FILE = 2;
   public static final int IS_HIDDEN = 4;
   
   /**
    * This flag is to includes all directroies to the list of files filtered. 
    * This is mainly to allow recursing into the directories to search for 
    * required files.
    */
   public static final int IS_INCLUDE_ALL_DIRECTORIES = 8;

   /**
    * Constructs a file filter whose accept() method will always return
    * <CODE>true</CODE>
    */
   public PSFileFilter()
   {
   }

   /**
    * Constructs a file filter that will return <CODE>true</CODE> when and only
    * when, for each attribute (file, dir, hidden) that a file possesses, the
    * corresponding bit in <CODE>allowableAttributes</CODE> is turned on.
    */
   public PSFileFilter(int allowableAttributes)
   {
      setAllowableAttributes(allowableAttributes);
   }

   /**
    * Constructs a file filter that will return <CODE>true</CODE> when and only
    * when the file's length (as returned by java.io.File.length()) is >=
    * <CODE>minLength</CODE> AND <= <CODE>maxLength</CODE>.
    */
   public PSFileFilter(long minLength, long maxLength)
   {
      setMinMaxLength(minLength, maxLength);
   }

   /**
    * Constructs a file filter that will return <CODE>true</CODE> when and only
    * when the file's modification date (as returned by
    * java.io.File.lastModified()) is >= <CODE>earliestModified</CODE> AND
    * <= <CODE>latestModified</CODE>.
    */
   public PSFileFilter(Date earliestModified, Date latestModified)
   {
      setEarliestLatestModified(earliestModified, latestModified);
   }

   /**
    * Constructs a file filter that will return <CODE>true</CODE> when and only
    * when the file's name (as returned by java.io.File.getName()) matches the
    * given pattern matcher.
    */
   public PSFileFilter(PSPatternMatcher namePattern)
   {
      setNamePattern(namePattern);
   }

   /**
    * Constructs a file filter that will return <CODE>true</CODE> when and only
    * when all of the following conditions are met:
    * <UL>
    * <LI>file's length (as returned by java.io.File.length()) is >=
    * <CODE>minLength</CODE> AND <= <CODE>maxLength</CODE>
    * <LI>for each attribute (file, dir, hidden) that a file possesses, the
    * corresponding bit in <CODE>allowableAttributes</CODE> is turned on.
    * </UL>
    */
   public PSFileFilter(int allowableAttributes, long minLength, long maxLength)
   {
      setAllowableAttributes(allowableAttributes);
      setMinMaxLength(minLength, maxLength);
   }

   /**
    * Constructs a file filter that will return <CODE>true</CODE> when and only
    * when all of the following conditions are met:
    * <UL>
    * <LI>file's length (as returned by java.io.File.length()) is >=
    * <CODE>minLength</CODE> AND <= <CODE>maxLength</CODE>
    * <LI>for each attribute (file, dir, hidden) that a file possesses, the
    * corresponding bit in <CODE>allowableAttributes</CODE> is turned on.
    * </UL>
    */
   public PSFileFilter(int allowableAttributes, long minLength, long maxLength,
      PSPatternMatcher namePattern)
   {
      setAllowableAttributes(allowableAttributes);
      setMinMaxLength(minLength, maxLength);
      setNamePattern(namePattern);
   }

   /**
    * Sets the minimum and maximum length for files. For accept() to return
    * <CODE>true</CODE>, it is necessary (but not sufficient) that the file's
    * length (as returned by java.io.File.length()) is >= <CODE>minLength</CODE>
    * AND <= <CODE>maxLength</CODE>.
    */
   public void setMinMaxLength(long minLength, long maxLength)
   {
      if (minLength >= maxLength) throw new IllegalArgumentException(
         "minLength (" + minLength + ") >= maxLength (" + maxLength + ")");

      if (minLength < 0) throw new IllegalArgumentException(
         "minLength ( " + minLength + ") < 0");

      m_minLength = minLength;
      m_maxLength = maxLength;
   }

   /**
    * Sets the earliest and latest modification date for files. For accept() to
    * return <CODE>true</CODE>, it is necessary (but not sufficient) that the
    * file's modification date (as returned by java.io.File.lastModified()) is
    * >= <CODE>earliestModified</CODE> AND <= <CODE>latestModified</CODE>.
    */
   public void setEarliestLatestModified(Date earliestModified,
      Date latestModified)
   {
      if (latestModified.compareTo(earliestModified) <= 0)
         throw new IllegalArgumentException("lastModified <= earliestModified");

      m_earliestModified = earliestModified;
      m_latestModified = latestModified;
   }

   /**
    * Adds an OR'ed name pattern for files. For accept() to return <CODE>true</CODE>,
    * it is necessary (but not sufficient) that the file's name (as returned by
    * java.io.File.getName()) matches at least one of the added pattern matchers.
    */
   public void addNamePattern(PSPatternMatcher namePattern)
   {
      if (m_namePatterns == null)
         m_namePatterns = new java.util.ArrayList();
      m_namePatterns.add(namePattern);
   }

   /**
    * Sets the name pattern for files. For accept() to return <CODE>true</CODE>,
    * it is necessary (but not sufficient) that the file's name (as returned by
    * java.io.File.getName()) matches the given pattern matcher.
    */
   public void setNamePattern(PSPatternMatcher namePattern)
   {
      m_namePatterns = new java.util.ArrayList();
      m_namePatterns.add(namePattern);
   }

   /**
    * Sets the allowable attributes for files. For accept() to return
    * <CODE>true</CODE>, it is necessary (but not sufficient) that for
    * each attribute that a file possesses, the corresponding bit in
    * <CODE>allowableAttributes</CODE> is turned on.
    */
   public void setAllowableAttributes(int allowableAttributes)
   {
      m_allowableAttributes = allowableAttributes;
   }

   /**
    * java.io.FileFilter implementation
    *
    */
   public boolean accept(File pathname)
   {
      // test attributes (directory, file, hidden)
      if (pathname.isDirectory())
      {
         if (0 != (m_allowableAttributes & IS_INCLUDE_ALL_DIRECTORIES))
            return true;

         if (0 == (m_allowableAttributes & IS_DIRECTORY))
            return false;
      }
      else if (pathname.isFile())
      {
         if (0 == (m_allowableAttributes & IS_FILE))
            return false;
      }
      if (pathname.isHidden())
      {
         if (0 == (m_allowableAttributes & IS_HIDDEN))
            return false;
      }

      // test length
      long length = pathname.length();
      if (length < m_minLength)
         return false;
      if (length > m_maxLength)
         return false;

      // test last modified
      Date lastModified = new Date(pathname.lastModified());

      if (m_earliestModified != null)
         if (lastModified.compareTo(m_earliestModified) < 0)
            return false;
      if (m_latestModified != null)
         if (lastModified.compareTo(m_latestModified) > 0)
            return false;

      // test fileame patterns
      if (m_namePatterns != null)
      {
         for (int i = 0; i < m_namePatterns.size(); i++)
         {
            PSPatternMatcher namePat
               = (PSPatternMatcher)m_namePatterns.get(i);

            if (namePat.doesMatchPattern(pathname.getName()))
               return true;
         }
         return false;
      }

      return true;
   }

   /**
    * java.io.FilenameFilter implementation
    *
    */
   public boolean accept(File dir, String name)
   {
      return accept(new File(dir, name));
   }

   /**
    * Each bit represents an allowable attribute. If the bit is on, then the
    * corresponding attribute is allowed to be on (but is not REQUIRED to be
    * on).
    */
   private int m_allowableAttributes = 0xFFFFFFFF;

   /**
    * Length of file or dir must be >= m_minLength and <= m_maxLength
    */
   private long m_minLength = 0;
   private long m_maxLength = Long.MAX_VALUE;

   private Date m_earliestModified = null;
   private Date m_latestModified = null;

   /**
    * Name of file or dir must match at least one of the patterns contained
    * in <CODE>m_namePatterns</CODE>
    */
   private ArrayList m_namePatterns;
}
