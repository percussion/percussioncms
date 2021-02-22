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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.impl;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.utils.codec.PSXmlEncoder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class holds state in thread local storage for exception(s) that occur
 * while assembling a page. It is called on exceptions or other errors and keeps
 * a list of problems in thread local storage.
 * 
 * @author dougrand
 * 
 */
public class PSTrackAssemblyError
{
   /**
    * Logger for the tracker
    */
   static Log ms_log = LogFactory.getLog(PSTrackAssemblyError.class);
   
   /**
    * Data structure to hold problem descriptions
    */
   private static class Problem
   {
      /**
       * Hold a description of the issue found, never <code>null</code>.
       */
      private String mi_description;

      /**
       * Hold the exception, may be <code>null</code>
       */
      private Throwable mi_exception;

      /**
       * Ctor
       * 
       * @param desc the description of the problem, never <code>null</code>
       *           or empty.
       * @param t the throwable associated with the problem, may be
       *           <code>null</code>.
       */
      public Problem(String desc, Throwable t) {
         if (StringUtils.isBlank(desc))
         {
            throw new IllegalArgumentException("desc may not be null or empty");
         }
         mi_description = desc;
         mi_exception = t;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder b = new StringBuilder(mi_description.length());
         b.append(mi_description);
         if (mi_exception != null)
         {
            b.append(" from exception ");
            b.append(mi_exception.getLocalizedMessage());
         }
         return b.toString();
      }

      /**
       * Return a formatted html table row for use in an output page.
       * 
       * @return the formatted html, never <code>null</code> or empty.
       */
      public String toHTMLRow()
      {
         PSXmlEncoder enc = new PSXmlEncoder();
         StringBuilder b = new StringBuilder(mi_description.length() + 80);
         b.append("<tr><td>");
         b.append(mi_description);
         b.append("</td><td>");
         if (mi_exception != null)
         {
            b.append(enc.encode(mi_exception.toString()));
         }
         b.append("</td></tr>\n");
         return b.toString();
      }
   }

   /**
    * Page header for error page.
    */
   private static final String HEADER = 
      "<html><head><title>Error in assembly</title></head><body>";

   /**
    * The problems are held in this thread local. It it initialized for each
    * assembly request's thread. The results are interrogated at the end of the
    * request to determine if any failures occurred in some internal processing
    * such as the various property interceptors - which do not have access to
    * the assembly result and which do not throw exceptions to avoid causing
    * problems for previews.
    */
   private static ThreadLocal<List<Problem>> ms_problems = 
      new ThreadLocal<>();

   /**
    * Initialize the stored list of problems to empty.
    */
   public static void init()
   {
      ms_problems.set(new ArrayList<>());
   }

   /**
    * Add a new problem, convenience method that calls
    * <code>addProblem(message,null)</code>.
    * 
    * @param message the problem message, never <code>null</code> or empty.
    */
   public static void addProblem(String message)
   {
      addProblem(message, null);
   }

   /**
    * Add a new problem. If the storage has not been initialized, then
    * do nothing. This allows code to use this that may not be involved
    * with assembly.
    * 
    * @param message the problem message, never <code>null</code> or empty.
    * @param t the throwable, may be <code>null</code>.
    */
   public static void addProblem(String message, Throwable t)
   {
      if (ms_problems.get() != null)
         ms_problems.get().add(new Problem(message, t));
   }

   /**
    * Did this request have problems
    * 
    * @return <code>true</code> if there were any problems recorded.
    */
   public static boolean hadProblems()
   {
      return ms_problems.get() != null && !ms_problems.get().isEmpty();
   }

   /**
    * Return a table formatted with the problems that occurred.
    * 
    * @return the formatted html table, never <code>null</code> or empty.
    */
   private static String toHTMLTable()
   {
      List<Problem> problems = ms_problems.get();
      if (problems == null) return "";
      
      StringBuilder b = new StringBuilder((1 + problems.size()) * 80);
      b.append("<table border='0'><tr><th>Description</th>" +
            "<th>Exception</th></tr>\n");
      for (Problem p : problems)
      {
         b.append(p.toHTMLRow());
      }
      b.append("</table>");
      return b.toString();
   }

   /**
    * If there were errors, change the item result status to failure. In
    * addition, if the item is not in preview mode, replace the item result with
    * a table of the error(s).
    * 
    * @param item the assembly item, never <code>null</code>.
    */
   public static void handleItem(IPSAssemblyItem item)
   {
      if (hadProblems())
      {
         item.setStatus(Status.FAILURE);
         int context = item.getContext();
         if (context != 0)
         {
            // Non-preview, replace the results
            StringBuilder b = new StringBuilder(10000);
            b.append(HEADER);
            b.append(toHTMLTable());
            b.append("</body></html>");
            try
            {
               item.setResultData(b.toString().getBytes("UTF8"));
            }
            catch (UnsupportedEncodingException e)
            {
               // Impossible!
               ms_log.error(e);
            }
            item.setMimeType("text/html;charset=utf8");
         }
      }

   }

}
