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
