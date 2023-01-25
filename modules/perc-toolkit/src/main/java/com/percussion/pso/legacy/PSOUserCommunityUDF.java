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

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.server.IPSRequestContext;

/**
 * A UDF to get the user community name. 
 * This function, which has previously been available 
 * only in JEXL, can now be accessed in an XML application. 
 *
 * @author davidbenua
 *
 */
public class PSOUserCommunityUDF extends PSSimpleJavaUdfExtension
      implements
         IPSUdfProcessor
{
   /**
    * Gets the user community from user session.  
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] arg0, IPSRequestContext arg1)
         throws PSConversionException
   {
      PSOObjectFinder finder = new PSOObjectFinder(); 
      return finder.getUserCommunity(); 
   }
}
