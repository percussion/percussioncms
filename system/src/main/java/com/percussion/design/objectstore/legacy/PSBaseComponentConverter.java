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

package com.percussion.design.objectstore.legacy;

/**
 * Base class for component converters, provides common functionality.
 */
public abstract class PSBaseComponentConverter extends PSBaseDSConverter
 implements IPSComponentConverter
{
   /**
    * Construct the converter
    * 
    * @param configCtx Supplies the configurations required for conversion, may
    * not be <code>null</code>.
    * @param repositoryInfo The repository info, used to determine if creating
    * a datasource configuration that points to the repository.  May be 
    * <code>null</code> to assume the created configurations point to the 
    * repository.
    * @param updateConfig <code>true</code> to create required configurations
    * if they are not found in the configurations supplied by the
    * <code>configCtx</code>, <code>false</code> to throw an exception if
    * the required configurations are not found.
    */
   public PSBaseComponentConverter(PSConfigurationCtx configCtx, 
      IPSRepositoryInfo repositoryInfo, boolean updateConfig)
   {
      super(configCtx, repositoryInfo, updateConfig);
   }
   
   // see IPSComponentConverter interface
   public boolean isForcedConversion()
   {
      return m_isForcedConversion;
   }

   // see IPSComponentConverter interface
   public void setForcedConversion(boolean isRequired)
   {
      m_isForcedConversion = isRequired;
   }
   
   // see IPSComponentConverter interface
   public String getConversionContext()
   {
      return m_conversionContext.get();
   }

   // see IPSComponentConverter interface
   public void setConversionContext(String ctx)
   {
      // normalize no value to null
      String val = ctx;
      if (val != null && val.trim().length() == 0)
      {
         val = null;
      }
      
      m_conversionContext.set(val);
   }   
   
   /**
    * Get the context message in the form "in <ctx> " (with a trailing space).
    * 
    * @return The message, or an empty string if no context is set.
    * 
    * @see #setConversionContext(String)
    */
   protected String getContextLogMessage()
   {
      String msg = "";
      
      String ctx = m_conversionContext.get();
      if (ctx != null)
      {
         msg = "in " + ctx + " ";
      }
      
      return msg;
   }
   
   /**
    * Stores the state of the forced conversion flag, see 
    * {@link IPSComponentConverter#isForcedConversion()} for details. 
    */   
   private boolean m_isForcedConversion = false;
   
   /**
    * Stores the current conversion context in thread local storage, never 
    * <code>null</code>.
    */
   private ThreadLocal<String> m_conversionContext = new ThreadLocal<>();
}
