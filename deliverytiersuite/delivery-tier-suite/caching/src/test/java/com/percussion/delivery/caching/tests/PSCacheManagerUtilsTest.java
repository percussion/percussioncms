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
package com.percussion.delivery.caching.tests;

import static org.junit.Assert.assertEquals;

import com.percussion.delivery.caching.PSCacheManagerUtils;
import com.percussion.delivery.caching.data.PSInvalidateRequest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author erikserating
 *
 */
public class PSCacheManagerUtilsTest
{
   @Test
   public void testSplitRequest() throws Exception
   {
       List<String> paths = new ArrayList<String>();
       PSInvalidateRequest testReq = new PSInvalidateRequest();
       testReq.setRegionName("testRegion");
       paths.add("/foo/test.html");
       testReq.setPaths(paths);
       
       List<PSInvalidateRequest> results = PSCacheManagerUtils.splitRequest(testReq, 10);
       assertEquals("Split no op", 1, results.size());
       
       paths = new ArrayList<String>();
       for(int i = 0; i < 100; i++)
       {
          paths.add("/foo/bar/test_" + i);    
       }
       testReq.setPaths(paths);
       results = PSCacheManagerUtils.splitRequest(testReq, 10);
       assertEquals("Split 100 by 10", 10, results.size());
   }
}
