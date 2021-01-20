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

import com.percussion.data.macro.IPSMacroExtractor;
import com.percussion.design.objectstore.PSMacro;
import com.percussion.design.objectstore.PSMacroDefinition;
import com.percussion.error.PSException;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.server.PSServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This extractor cless extracts macro values at runtime.
 */
public class PSMacroExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param source the object defining the source of this value, may be
    *    <code>null</code>.
    */
   public PSMacroExtractor(PSMacro source)
   {
      super(source);
      
      m_source = source;
      if (m_source != null)
         m_macroDefinition = PSServer.getMacros().getMacroDefinition(
            m_source.getName());
   }

   /**
    * Convenience method signature that calls 
    * {@link extract(PSExecution Data, Object)}.
    */
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param execData the execution data associated with this request.
    *    This includes all context data, result sets, etc.
    * @param defValue the default value to use if a value is not found, may
    *    be <code>null</code>.
    * @return the associated value as <code>String</code>, <code>null</code> 
    *    if a value is not found.
    * @throws PSDataExtractionException if an error condition causes the 
    *    extraction to fail. This is not thrown if the requested data does not 
    *    exist.
    */
   public Object extract(PSExecutionData data, Object defaultValue)
      throws PSDataExtractionException
   {
      Object extractedValue = defaultValue;
      
      if (m_macroDefinition != null)
      {
         String origin = "MacroExtractor";
         try
         {
            Class c = Class.forName(m_macroDefinition.getClassName());
            Constructor ctor = c.getConstructor(new Class[] { });

            IPSMacroExtractor extractor = (IPSMacroExtractor) ctor.newInstance(
               new Object[] { });
            
            extractedValue = extractor.extract(data);
            if (extractedValue == null)
               extractedValue = defaultValue;
         }
         catch (ClassNotFoundException e)
         {
            Object[] args =
            {
               m_macroDefinition.getClassName(),
               PSException.getStackTraceAsString(e)
            };

            PSLogManager.write(new PSLogServerWarning(
               IPSDataErrors.MACRO_EXTRACTOR_CLASS_NOT_FOUND, args, true,
                  origin));
         }
         catch (InstantiationException e)
         {
            Object[] args =
            {
               m_macroDefinition.getClassName(),
               PSException.getStackTraceAsString(e)
            };

            PSLogManager.write(new PSLogServerWarning(
               IPSDataErrors.MACRO_EXTRACTOR_INSTANTIATION_FAILED, args, true,
                  origin));
         }
         catch (IllegalAccessException e)
         {
            Object[] args =
            {
               m_macroDefinition.getClassName(),
               PSException.getStackTraceAsString(e)
            };

            PSLogManager.write(new PSLogServerWarning(
               IPSDataErrors.MACRO_EXTRACTOR_ILLEGAL_ACCESS, args, true,
                  origin));
         }
         catch (InvocationTargetException e)
         {
            Object[] args =
            {
               m_macroDefinition.getClassName(),
               PSException.getStackTraceAsString(e)
            };

            PSLogManager.write(new PSLogServerWarning(
               IPSDataErrors.MACRO_EXTRACTOR_INVOCATION_TARGET_ERROR, args,
                  true, origin));
         }
         catch (NoSuchMethodException e)
         {
            Object[] args =
            {
               m_macroDefinition.getClassName(),
               PSException.getStackTraceAsString(e)
            };

            PSLogManager.write(new PSLogServerWarning(
               IPSDataErrors.MACRO_EXTRACTOR_NO_SUCH_METHOD, args,
                  true, origin));
         }
      }
      
      return extractedValue;
   }

   /**
    * The source for which to extract the data, initialized in constructor,
    * may be <code>null</code>.
    */
   protected PSMacro m_source = null;
   
   /**
    * The macro definition for which to extract the data, initialized in 
    * constructor, may be <code>null</code>.
    *
    */
   private PSMacroDefinition m_macroDefinition = null;
}
