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

package com.percussion.webservices.ui.impl;

import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;

import java.util.ArrayList;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.apache.cactus.ServletTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSUiDesignWsTest extends ServletTestCase
{
   @Test
   public void testFindDisplayFormat() throws Exception
   {
      IPSSecurityWs svc = PSSecurityWsLocator.getSecurityWebservice();
      svc.login("admin1", "demo", null, null);
      
      IPSUiDesignWs designWs = PSUiWsLocator.getUiDesignWebservice();      
      PSDisplayFormat dsFmt = designWs.findDisplayFormat("Simple");
      PSDisplayFormat dsFmt2 = designWs.findDisplayFormat(dsFmt.getGUID());
      PSDisplayFormat dsFmt3 = designWs.findDisplayFormat(dsFmt.getGUID());
       
      assertTrue(dsFmt == dsFmt2);
      assertTrue(dsFmt == dsFmt3);
      
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(dsFmt.getGUID());
      PSDisplayFormat dsFmt4 = designWs.loadDisplayFormats(ids, false, false, null, null).get(0);
      assertTrue(dsFmt != dsFmt4);
   }
   
   /**
    * Required for JUnit4 tests to be run with Ant 1.6.5.
    * 
    * @return Adapter object which wraps existing class as JUnit4.
    */
   public static junit.framework.Test suite()
   {
      return new JUnit4TestAdapter(PSUiDesignWsTest.class);
   }
   
}
