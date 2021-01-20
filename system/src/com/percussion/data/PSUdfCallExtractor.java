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
package com.percussion.data;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSServer;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The PSUdfCallExtractor class is used to extract data from various
 * sources, which are then massaged through the UDF call.
 * <P>
 * Within an application, users may define UDFs. They can then
 * define many instances of the UDF, each taking its own set of parameters.
 * This class has been defined to provide the components of E2 an easy
 * access mechanism into the UDFs. The
 * {@link com.percussion.design.objectstore.PSExtensionCall PSExtensionCall} object
 * defined in the application is used to create an instance of this class.
 * It then builds the appropriate extension handler. When the UDF is executed,
 * it loads the parameters from the appropriate places (XML, CGI vars, etc.).
 * It then returns the result the UDF supplies.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUdfCallExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param udf The udf instance that will be called.
    * @param call The udf call containing param bindings.
    */
   public PSUdfCallExtractor(PSExtensionCall call)
         throws PSExtensionException, PSNotFoundException
   {
      super(getReplacementValues(call));
      m_udfProcessor =
            (IPSUdfProcessor)PSServer.getExtensionManager(null).prepareExtension(
            call.getExtensionRef(), null);
      m_udfRunner = new PSExtensionRunner(m_udfProcessor, call);
   }

/* ************  IPSDataExtractor Interface Implementation ************ */

/**
 * Extract a data value using the run-time data.
 *
 * @param   execData    the execution data associated with this request.
 *                      This includes all context data, result sets, etc.
 *
 * @return               the associated value; <code>null</code> if a
 *                        value is not found
 *
 * @exception   PSDataExtractionException
 *                        if an error condition causes the extraction to
 *                        fail. This is not thrown if the requested data
 *                        does not exist.
 */
   public Object extract(PSExecutionData data)
         throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param   defValue      the default value to use if a value is not found
    *
    * @return               the associated value; <code>defValue</code> if a
    *    value is not found
    * @exception   PSDataExtractionException
    *                        if an error condition causes the extraction to
    *                        fail. This is not thrown if the requested data
    *                        does not exist.
    */
   public Object extract(PSExecutionData data, Object defValue)
         throws PSDataExtractionException
   {
      Object value = null;
      try
      {
         value = m_udfRunner.processUdfCallExtractor(data);
      }
      catch (PSConversionException e)
      {
         if(e.getLanguageString() == null)
            throw new PSDataExtractionException(e.getErrorCode(),
                  e.getErrorArguments());
         else
            throw new PSDataExtractionException(e.getLanguageString(),
                  e.getErrorCode(), e.getErrorArguments());
      }
      return (value == null) ? defValue : value;
   }

   /**
    * Set the base from which any XML field sources used by this extractor
    * will be extracted.
    *
    * @param   base         the base field name to use
    */
   public void setXmlFieldBase(String base)
   {
   /* we must rebase all the XML fields used by this UDF
    * (part of fix for bug id TGIS-4BTW25)
    */
      for (Iterator i = m_udfRunner.getExtractors(); i.hasNext(); )
      {
         IPSDataExtractor extr = (IPSDataExtractor)(i.next());
         if (extr instanceof PSXmlFieldExtractor)
            ((PSXmlFieldExtractor)extr).setXmlFieldBase(base);
         else if (extr instanceof PSUdfCallExtractor)
            ((PSUdfCallExtractor)extr).setXmlFieldBase(base);
      }
   }

   private static IPSReplacementValue[] getReplacementValues(PSExtensionCall source)
   {
      // this is where we'll store the data extractors for the params
      ArrayList values = new ArrayList();
   /* loop through the parameter value defs and validate them */
      PSExtensionParamValue[] vals = source.getParamValues();
      for (int i = 0; i < vals.length; i++)
      {
         PSExtensionParamValue val = vals[i];
         values.add((val == null) ? null : val.getValue());
      }
      IPSReplacementValue[] valueArray = new IPSReplacementValue[values.size()];
      return (IPSReplacementValue[])(values.toArray(valueArray));
   }

   private IPSUdfProcessor m_udfProcessor;
   private PSExtensionRunner m_udfRunner;
}
