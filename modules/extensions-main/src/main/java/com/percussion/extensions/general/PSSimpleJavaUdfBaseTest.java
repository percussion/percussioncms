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

package com.percussion.extensions.general;

import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;

/**
 * @author DougRand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class PSSimpleJavaUdfBaseTest
{
   public Object callUDF(PSSimpleJavaUdfExtension ext, 
      IPSRequestContext request) throws Exception
   {
      Object params[] = new Object[0];
      return ext.processUdf(params, request);
   }
   
   public Object callUDF(PSSimpleJavaUdfExtension ext, 
      IPSRequestContext request, Object p) throws Exception
   {
      Object params[] = new Object[] { p };
      return ext.processUdf(params, request);
   }
   
   public Object callUDF(PSSimpleJavaUdfExtension ext, 
      IPSRequestContext request, Object p1, Object p2) throws Exception
   {
      Object params[] = new Object[] { p1, p2 };
      return ext.processUdf(params, request);
   }   
   
   public Object callUDF(PSSimpleJavaUdfExtension ext, 
      IPSRequestContext request, Object p1, Object p2,
      Object p3) throws Exception
   {
      Object params[] = new Object[] { p1, p2, p3 };
      return ext.processUdf(params, request);
   }
   
   public Object callUDF(PSSimpleJavaUdfExtension ext, 
      IPSRequestContext request, Object p1, Object p2,
      Object p3, Object p4) throws Exception
   {
      Object params[] = new Object[] { p1, p2, p3, p4 };
      return ext.processUdf(params, request);
   }
   
   public Object callUDF(PSSimpleJavaUdfExtension ext, 
      IPSRequestContext request, Object p1, Object p2,
      Object p3, Object p4, Object p5) throws Exception
   {
      Object params[] = new Object[] { p1, p2, p3, p4, p5 };
      return ext.processUdf(params, request);
   }   
}
