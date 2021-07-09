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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
