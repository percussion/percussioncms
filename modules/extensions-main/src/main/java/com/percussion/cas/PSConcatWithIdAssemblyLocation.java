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
package com.percussion.cas;

import com.percussion.extension.IPSAssemblyLocation;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSHtmlParameters;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This assembly location generator concatenates all provided parameters and
 * adds the contentid at a given position.
 */
public class PSConcatWithIdAssemblyLocation implements IPSAssemblyLocation
{
   // See interface for details
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      m_def = def;
   }

   /**
    * This implementation concatenates all but the first parameters and adds 
    * the contentid at the given position. This needs at least 2 parameters
    * and handles as many parameters as provided. If the index 
    * provided is 1, a location string like this will be created:
    * e.g. params[1] + contentid + params[2] + ... + params[n]. This will
    * check that the minimum number of parameters are provided and that the
    * index supplied is in the range of the provided parameters. All 
    * parameters provided with backslashes will be transformed to forward 
    * slashes.
    *
    * @param params[0] a string or an object convertable to a string using
    *    the toString method. This string parsed as an integer must return
    *    a valid integer.
    * @param params[1..n] all parameters to concatenated together to produce 
    *    the location string.
    */
   public String createLocation(Object[] params, IPSRequestContext request)
      throws PSExtensionException
   {
      String exitName = getClass().getName();
      request.printTraceMessage("Entering " + exitName + ".createLocation");

      String location = "";
      try
      {
         // check the number of parameters provided is correct
         if (params.length < EXPECTED_NUMBER_OF_PARAMS)
         {
            Object[] args =
            { 
               "" + EXPECTED_NUMBER_OF_PARAMS,
               "" + params.length
            };
            throw new PSExtensionException(
               IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH, args);
         }

         // parameter 0 must be the index
         int index = Integer.parseInt(params[0].toString());
         if (index < 0 || index >= params.length)
         {
            Object[] args = 
            { 
               m_def.getRef().getExtensionName(), 
               "Index out of bounds: " + index
            };
            throw new PSExtensionException(
               IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
         }

         String contentid = PSHtmlParameters.get(
            IPSHtmlParameters.SYS_CONTENTID, request);
         if (contentid == null)
         {
            Object[] args =
            { 
               exitName, 
               IPSHtmlParameters.SYS_CONTENTID
            };
            throw new PSExtensionException(
               IPSExtensionErrors.MISSING_HTML_PARAMETER, args);
         }
         
         for (int i=1; i<params.length; i++)
         {
            if ((i - 1) == index)
               location += contentid;

            location += params[i].toString();
         }
         
         request.printTraceMessage("Location= " + location);
      }
      catch (Throwable e)
      {
         StringWriter writer = new StringWriter();
         PrintWriter printer = new PrintWriter(writer, true);
         e.printStackTrace(printer);
         request.printTraceMessage("Error: " + writer.toString());
      }
      finally
      {
         request.printTraceMessage("Leaving " + exitName + ".createLocation");
         return location;
      }
   }
   
   /**
    * This is the definition for this extension. You may want to use it for
    * validation purposes in the <code>createLocation</code> method.
    */
   protected IPSExtensionDef m_def = null;

   /**
    * The number of expected parameters.
    */
   private static final int EXPECTED_NUMBER_OF_PARAMS = 2;
}
