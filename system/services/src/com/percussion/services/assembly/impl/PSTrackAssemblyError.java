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
package com.percussion.services.assembly.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.utils.codec.PSXmlEncoder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    private static final Logger ms_log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);
   
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

            item.setResultData(b.toString().getBytes(StandardCharsets.UTF_8));

            item.setMimeType("text/html;charset=utf8");
         }
      }

   }

}
