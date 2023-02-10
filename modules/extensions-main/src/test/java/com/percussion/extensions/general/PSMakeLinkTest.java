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
