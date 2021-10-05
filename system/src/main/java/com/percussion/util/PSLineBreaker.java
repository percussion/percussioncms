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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;


/**
 * This class is used to break a string up into lines with a length not
 * more than a specified width, breaking the line at the first embedded new line
 * or a whitespace or dash that is found closest to the end of the line and is
 * within a given threshold.
 */
public class PSLineBreaker
{

   private static final Logger log = LogManager.getLogger(PSLineBreaker.class);

   /**
    * Instantiates a <code>PSLineBreaker</code>, then recombines the lines
    * into a single string, adding newlines after each line (except the last).
    * See {@link #PSLineBreaker(String,int,int) ctor} for details on params.
    *
    * @param newLine Added whereever a break is found and a line follows. If
    *    <code>null</code> or empty, "\r\n" is used.
    *
    * @return The supplied string, with newlines added at appropriate break
    *    points. Never <code>null</code>.
    */
   public static String wrapString(String line, int maxLen, int threshold,
         String newLine)
   {
      PSLineBreaker breaker = new PSLineBreaker(line, maxLen, threshold, newLine);

      if (null == newLine || newLine.length() == 0)
         newLine = "\r\n";

      /* The 2nd part of the size below is the estimated # of lines times the
         number of chars in the newline. The 10 is padding so we allocate more
         space than needed.*/
      StringBuilder buf = new StringBuilder(
            line.length() + (line.length() / maxLen + 10) * newLine.length());

      while ( breaker.hasNext())
      {
         buf.append(breaker.next());
         if (breaker.hasNext())
            buf.append(newLine);
      }

      return buf.toString();
   }

   /**
    * See {@link #PSLineBreaker(String,int,int,String) ctor} for details on params.
   */
   public PSLineBreaker(String line, int maxLen, int threshold)
   {
      this(line, maxLen, threshold, null);
   }

   /**
    * Constructor for this class that takes an additional newLine string
    *
    * @param line the line to break.  May not be <code>null</code>.
    *
    * @param maxLen the max length of each line to return.  Must be
    * greater than <code>zero</code>.
    *
    * @param threshold number of chars counting from the end of line to check
    *   for a whitespace or dash. If none is found within a given threshold and
    *   a line doesn't already contain new lines, then the line will break
    *   precisely at the specified maxLen. The embedded newLine overrides a
    *   threshold restriction causing the line to break at the embedded new line
    *   which is found to be closest to the beginning of the line.
    *   In case if multiple new lines are found next to one another it results
    *   in one empty string collected for the each new line found.
    *   If NO new lines were found, then it looks for a dash or space.
    *   If both dash and space are found within the threshold then it picks the
    *   one, which index is closest to the end of the line.
    *   Threshold value MUST be greater than <code>zero</code> and less than maxLen.
    *   Usage examples / tests can be found in the main method of this class.
    *
    * @param newLine new line string to take into account when breaking lines,
    * for backward compatibility if <code>null</code> or <code>empty</code>
    * is passed then assumes newLine = "\r\n"
   */
   public PSLineBreaker(String line, int maxLen, int threshold, String newLine)
   {
      // validate inputs
      if (line == null)
         throw new IllegalArgumentException("Line may not be null");
      if (maxLen <= 0)
         throw new IllegalArgumentException("maxLen must be greater than zero");
      if (threshold <= 0 || threshold >= maxLen)
         throw new IllegalArgumentException("threshold must be > 0, < maxLen");

      if (null == newLine || newLine.length() == 0)
         newLine = "\r\n";

      final int newLineLen = newLine.length();
      int lineLen = line.length();

      while(line != null && (lineLen = line.length()) > 0)
      {
         int newLineInd = -1;
         int end = 0;

         // first see if line fits
         if (lineLen <= maxLen)
         {
            end = lineLen;
         }
         else
         {
            // search from right to left
            end = maxLen;

            int spaceInd = -1;
            int dashInd =  -1;

            /* try to find a new line that is closest to the start of the line;
             If a new line is not found then look for a space or dash that is
             closest to the end of the line and is within a given threshold.
            */

            int tmpNewLineInd = end - 1;
            do
            {
               tmpNewLineInd = line.lastIndexOf(newLine, tmpNewLineInd);
               if (tmpNewLineInd >= 0)
                  newLineInd = tmpNewLineInd; //remember new line index
            }
            while(tmpNewLineInd >=0 && (tmpNewLineInd -= newLineLen) >= 0);

            if (newLineInd >= 0)
            {
               // new line was found
               end = newLineInd;
            }
            else
            {
               spaceInd = line.lastIndexOf(' ', end - 1);
               dashInd = line.lastIndexOf('-', end - 1);

               if (spaceInd >= 0 && dashInd < 0 && (end - spaceInd) <= threshold)
               {
                  //space found within the threshold
                  end = spaceInd + 1;
               }
               else if (spaceInd < 0 && dashInd >= 0 && (end - dashInd) <= threshold)
               {
                  //dash found within the threshold
                  end = dashInd + 1;
               }
               else if (spaceInd >= 0 && dashInd >= 0)
               {
                  //both space and dash are found, pick one that is closest to the end
                  int maxInd = spaceInd > dashInd ? spaceInd : dashInd;

                  // see if the one closest to the end is within the threshold
                  if ((end - maxInd) <= threshold)
                     end = maxInd + 1;
               }
            }
         }

         String substr = line.substring(0, end);
         int substrLen = substr.length();

         // add line to the list
         m_lines.add(substr);

         // calculate the maximum line length
         m_length = (m_length > substrLen) ? m_length : substrLen;

         int nextBeginIndex = 0;

         if (newLineInd >= 0)
            nextBeginIndex = substrLen + newLineLen; //skip new line
         else
            nextBeginIndex = substrLen;

         if (nextBeginIndex >= lineLen)
            break; //make sure we never go out of bounds

         // get next substring to wrap
         line = line.substring(nextBeginIndex);
      }

      m_size = m_lines.size();
   }

   /**
    * Checks to see if more lines will be returned.
    * @return <code>true</code> if there are more lines, <code>false</code>
    * if no more lines are available.
    */
   public boolean hasNext()
   {
      return (m_size > m_index);
   }

   /**
    * Returns the next line.
    * @return the next line.  Use <code>hasNext</code> to determine if there
    * are more lines.
    * @throws IndexOutOfBoundsException if no more lines are available.
    */
   public String next()
   {
      return (String)m_lines.get(m_index++);
   }

   /**
    * Returns the length of the longest line.
    * @return the length
    */
   public int maxLength()
   {
      return m_length;
   }
   
   /**
    * Return the split lines as an array of strings 
    * @return String array after breaking, never <code>null</code> or empty.
    */
   public String[] getLines()
   {
      String[] result = new String[m_lines.size()];
      m_lines.toArray(result);
      return result;
   }

   /*
    * the list of lines
    */
   private ArrayList m_lines = new ArrayList();

   /*
    * the current index
    */
   private int m_index = 0;

   /*
    * the total number of lines
    */
   private int m_size = 0;

   /*
    * the length of the longest line
    */
   private int m_length = 0;

   public static void main(String[] args)
   {
      //test this class
      String test0 = "The quick\n brown fox jumped over the lazy dog";
      test0 = PSLineBreaker.wrapString(test0, 13, 12, "\n");
      log.info(test0);

      String test1 = "The quick-brown fox jumped over the lazy dog";
      test1 = PSLineBreaker.wrapString(test1, 13, 12, "\n");
      log.info(test1);

      String test2 = "The quick -brown fox jumped over the lazy dog";
      test2 = PSLineBreaker.wrapString(test2, 13, 12, "\n");
      log.info(test2);

      String test3 = "The\n quick\n\n\n -brown fox jumped over the lazy dog";
      PSLineBreaker breaker = new PSLineBreaker(test3, 15, 5, "\n");
      while (breaker.hasNext())
         log.info("\"" + breaker.next() + "\"");
     log.info(" ");

      String test4 = "The quick\n-brown fox jumped over the lazy dog";
      test4 = PSLineBreaker.wrapString(test4, 13, 12, "\n");
      log.info(test4);

      String test5 = "The quick\n -brown fox jumped over the lazy dog";
      test5 = PSLineBreaker.wrapString(test5, 13, 12, "\n");
      log.info(test5);

      String test = "The quick brown fox jumped over the lazy dog";
      breaker = new PSLineBreaker(test, 15, 5, "\n");
      log.info("Breaking \"{}\" with width: {} threshold: {}",test,15,5);
      log.info("longest line: {}", breaker.maxLength());
      while (breaker.hasNext())
         log.info("\"{}\"", breaker.next());
      log.info(" ");

      test = "Peter piper picked a peck of pickeld peppers";
      breaker = new PSLineBreaker(test, 15, 5, "\n");
      log.info("Breaking \"{}\" with width: {} threshold: {}", test, 15, 5);
      log.info("longest line: {}", breaker.maxLength());
      while (breaker.hasNext())
         log.info("\"{}\"", breaker.next());
      log.info(" ");


      test = "The quick ";
      breaker = new PSLineBreaker(test, 15, 5, "\n");
      log.info("Breaking \"{}\" with width: {} threshold: {}",test, 15, 5);
      log.info("longest line: {}", breaker.maxLength());
      while (breaker.hasNext())
         log.info("\"{}\"", breaker.next());
      log.info(" ");

      test = "Thequickbrownfoxjumpedoverthelazydog";
      breaker = new PSLineBreaker(test, 15, 5, "\n");
      log.info("Breaking \"{}\" with width: {} threshold: {}",test, 15, 5);
      log.info("longest line: {}", breaker.maxLength());
      while (breaker.hasNext())
         log.info("\"{}\"", breaker.next());
      log.info(" ");

      test = "Thequickbrownfo";
      breaker = new PSLineBreaker(test, 15, 5, "\n");
      log.info("Breaking \"{}\" with width: {} threshold: {}",test, 15, 5);
      log.info("longest line: {}", breaker.maxLength());
      while (breaker.hasNext())
         log.info("\"{}\"", breaker.next());
      log.info(" ");

      //note: this is a negative case that is suppose to cause IllegarArgumentsException
      test = "The quick brown fox jumped over the lazy dog";
      breaker = new PSLineBreaker(test, 15, 20, "\n");
      log.info("Breaking \"{}\" with width: {} threshold: {}",test, 15, 20);
      log.info("longest line: {}", breaker.maxLength());
      while (breaker.hasNext())
         log.info("\"{}\"", breaker.next());
      log.info(" ");
   }
}

