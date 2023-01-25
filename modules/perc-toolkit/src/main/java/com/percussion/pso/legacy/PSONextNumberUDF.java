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
package com.percussion.pso.legacy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSIdGenerator;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSRequestContext;

/**
 * Allocates the next number in a sequence. 
 *
 * @author davidbenua
 *
 */
public class PSONextNumberUDF extends PSSimpleJavaUdfExtension
      implements
         IPSUdfProcessor
{
   /**
    * Logger for this class
    */

   private static final Logger log = LogManager.getLogger(PSONextNumberUDF.class);

   /**
    * 
    */
   public PSONextNumberUDF()
   {
      super();
   }
   /**
    * Generates the next key in a sequence.  Calls the internal PSUtils 
    * method for allocating a block based on the name supplied in 
    * <code>params[0]</code>.  
    * @param params the parameter array
    * @param request the callers request context
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      String keyName = PSUtils.getParameter(params, 0);
      if(StringUtils.isBlank(keyName))
      {
         String emsg = "Key name must be supplied";
         throw new IllegalArgumentException(emsg);
      }
      
      try
      {
         return new Integer(PSIdGenerator.getNextId(keyName));
      } catch (SQLException ex)
      {
         String emsg = "Database Error Allocating ID"; 
         log.error("Database Error Allocating ID Error: {}", ex.getMessage());
         log.debug(ex.getMessage(), ex);
         throw new PSConversionException(0, emsg); 
      } 
      
     
   }
}
