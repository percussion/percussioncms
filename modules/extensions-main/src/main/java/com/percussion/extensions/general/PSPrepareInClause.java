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

package com.percussion.extensions.general;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;


/**
 * This Exit is used to take in a Collection and format the value portion of an
 * "IN" clause, adding the resulting string to the reqeust as a specified HTML
 * parameter.
 * <p>
 * An optional "default" value may be provided to use in case the Collection is
 * empty.
 * By default all the objects in the collection is enclosed in single quotes.
 * However optional parameter "encloseInQuotes" can be specified as
 * <code>0</code> if the objects should not be enclosed in quotes.
 */
public class PSPrepareInClause implements IPSRequestPreProcessor
{

   // see IPSExtension interface
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      //noop
   }

   /**
    * Formats the value list portion on an "IN" clause from a Collection and
    * stores the result in an HTML parameter.  Does not include the parenthesis.
    *
    * @param params An array of Objects to use as input parameters.  The
    * parameters expected are:
    * <ol>
    * <li>The base parameter name, as a String.  This value is used as the name
    * of the parameter created, whose value is set as the resulting "IN" clause.
    * May not be <code>null</code> or empty.</li>
    * <li>The Collection to use as the values of the "IN" clause.  May not be
    * <code>null</code>, but may be empty. May also be a String. If so, the
    * value is set as the value of the base parameter.</li>
    * <li>An optional default value to use if the collection is <code>null
    * </code> or emtpy.  If this default value is not <code>null</code>, it will
    * be used only if the collection does not contain at least one value that
    * resolve to a non-empty string when <code>toString()</code> is called on
    * it.</li>
    * <li>An optional parameter (specified as "encloseInQuotes" in the
    * Workbench) to determine if the objects in the collection should be
    * enclosed in single quotes or not. Defaults to <code>1</code> (which
    * implies enclose in single qoutes).
    * </li>
    * </ol>
    *
    * @param request The request context, never <code>null</code>.
    *
    * @throws PSRequestValidationException If the request is <code>null</code>.
    *
    * @throws PSParameterMismatchException If params is <code>null</code>, or if
    * the paramters do not match the above specifications.
    *
    * @throws PSExtensionProcessorException If any other exception occurs which
    * prevents the proper handling of this request
    *
    * @see IPSRequestPreProcessor#preProcessRequest(Object[], IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws
         PSParameterMismatchException, PSExtensionProcessingException,
            PSRequestValidationException
   {
      // validate and extract params
      if ((params == null) || (params.length < EXPECTED_NUMBER_OF_PARAMS))
      {
         // must at least provide 1 ("base parameter name") parameter
         throw new PSParameterMismatchException(
            EXPECTED_NUMBER_OF_PARAMS, params == null ? 0 : params.length);
      }
      else if (null == params[0] || 0 == params[0].toString().trim().length())
      {
         throw new PSExtensionProcessingException(
            IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION,
            "Base param name may not be null or empty");
      }
      else if (request == null)
      {
         throw new PSRequestValidationException(
            IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION,
            "Request context may not be null");
      }

      String baseName = params[0].toString();
      String inString = "";

      boolean encloseInQuotes = true;
      if ((params.length > 3) && (params[3] != null))
         encloseInQuotes = !(params[3].toString().trim().equals("0"));

      if (params[1] != null && params[1].toString().trim().length() > 0)
      {
         // build the "IN" string
         StringBuilder buf = new StringBuilder();
         Collection coll;
         Object obj = params[1];
         if (obj instanceof Collection)
         {
            coll = (Collection) obj;
            Iterator values = coll.iterator();
            while (values.hasNext())
            {
               Object o = values.next();
               if (o != null)
               {
                  String value = o.toString();
                  if (value.trim().length() != 0)
                  {
                     if (buf.length() != 0)
                        buf.append(", ");

                     if (encloseInQuotes)
                        buf.append('\'');
                     buf.append(o.toString());
                     if (encloseInQuotes)
                        buf.append('\'');
                  }
               }
            }
            if (buf.length() > 0)
               inString = buf.toString();
         }
         else
         {
            inString = obj.toString().trim();
            if (encloseInQuotes)
               inString = '\'' + inString + '\'';
         }
      }

      // see if we have anything
      if (inString.trim().length() == 0 && params.length > 2 &&
         params[2] != null)
      {
         // supply the default if provided
         inString = params[2].toString();
         if (encloseInQuotes)
            inString = '\'' + inString + '\'';
      }

      request.setParameter(baseName, inString);
   }

   /**
    * The number of expected parameters.
    */
   private static final int EXPECTED_NUMBER_OF_PARAMS = 1;

}
