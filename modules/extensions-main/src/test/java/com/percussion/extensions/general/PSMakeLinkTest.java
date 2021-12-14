/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.percussion.extensions.general;

import com.percussion.server.IPSRequestContext;
import com.percussion.testing.PSMockRequestContext;
import com.percussion.testing.PSTestCompare;
import org.junit.Before;
import org.junit.Test;

public class PSMakeLinkTest
{

   private PSMakeAbsLinkSecureEx m_ext;
   private PSMakeAbsLink m_abs;
   private PSMakeAbsLinkSecure m_secure;


   public PSMakeLinkTest() {
   }

   @Before
   public void setUp() throws Exception
   {
      m_ext = new PSMakeAbsLinkSecureEx();
      m_abs = new PSMakeAbsLink();
      m_secure = new PSMakeAbsLinkSecure();
   }

   @Test
   public void testAbsExProcessUdf() throws Exception
   {
      IPSRequestContext ctx = new PSMockRequestContext();
      Object[] params = new Object[24];
      int j = 0;
      params[0] = "yes";
      params[1] = "foobar";
      params[2] = "1012";
      params[3] = "/foo";
      for(int i = 4; i < params.length; i = i + 2)
      {
         params[i] = "param" + j;
         params[i+1] = "value" + j;
         j++;
      }
      
      String result = m_ext.processUdf(params, ctx).toString();
      PSTestCompare.assertEqualURLs("http://foobar:1012/foo?param5=value5&param4=value4&param3=value3&param2=value2&param1=value1&param0=value0&param9=value9&param8=value8&param7=value7&param6=value6",result);
   }

   @Test
   public void testAbsSecureProcessUdf() throws Exception
   {
      IPSRequestContext ctx = new PSMockRequestContext();
      Object[] params = new Object[12];
      int j = 0;
      params[0] = "yes";
      params[1] = "http://foobar:1012/foo";
      for(int i = 2; i < params.length; i = i + 2)
      {
         params[i] = "param" + j;
         params[i+1] = "value" + j;
         j++;
      }
      
      String result = m_secure.processUdf(params, ctx).toString();
      PSTestCompare.assertEqualURLs("http://foobar:1012/foo?param4=value4&param3=value3&param2=value2&param1=value1&param0=value0",result);
   }   


   @Test
   public void testAbsProcessUdf() throws Exception
   {
      IPSRequestContext ctx = new PSMockRequestContext();
      Object[] params = new Object[9];
      int j = 0;
      params[0] = "http://foobar:1021/Rhythmyx/sys_testing123/foo.xml";
      for(int i = 1; i < params.length; i = i + 2)
      {
         params[i] = "param" + j;
         params[i+1] = "value" + j;
         j++;
      }
      String result = m_abs.processUdf(params, ctx).toString();
      PSTestCompare.assertEqualURLs("http://foobar:1021/Rhythmyx/sys_testing123/foo.xml?param3=value3&param2=value2&param1=value1&param0=value0",result);
      
   }
}
