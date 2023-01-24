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
