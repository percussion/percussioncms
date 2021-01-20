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

package com.percussion.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSView;
import com.percussion.design.objectstore.PSViewSet;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestTest;
import com.percussion.util.IPSHtmlParameters;


/**
 * Unit test for the PSViewEvaluator class.
 */
public class PSViewEvaluatorTest extends TestCase
{
   /**
    * Tests view evaluator construction with valid and invalid data, checking
    * for fields in views, and getNextView
    *
    * @throws Exception if the test fails or there are any errors.
    */
   public void testViewEvaluator() throws Exception
   {
      // construct data
      final HashMap<String, String> params = new HashMap<String, String>();
      Map<String, String> cgiVars =
            Collections.singletonMap(IPSHtmlParameters.SYS_VIEW, "VieW1");

      PSRequest req = PSRequestTest.makeRequest("Rhythmyx/foo.html",
         "foo.html", params, cgiVars, null, null, null);
      PSExecutionData data = new PSExecutionData(null, null, req);

      // make an empty viewset
      PSViewSet viewSet = new PSViewSet();
      PSViewEvaluator eval = new PSViewEvaluator(viewSet);
      // this should happen since our view does not exist
      try
      {
         eval.isFieldVisible("View1", data);
         fail("Test bad view name");
      }
      catch (PSDataExtractionException success) {}

      // create a view
      List<String> fieldList = new ArrayList<String>();
      fieldList.add("field1");
      fieldList.add("field2");
      PSView view1 = new PSView("view1", fieldList.iterator());
      viewSet.addView(view1);

      // this should not fail
      eval = new PSViewEvaluator(viewSet);
      assertTrue("field visible", eval.isFieldVisible("FIELD1", data));
      assertTrue("field not visible", !eval.isFieldVisible("foobar", data));
      assertTrue("getnextview: sys_All", eval.getNextView(data, 2).equals(
         IPSConstants.SYS_ALL_VIEW_NAME));

      // remove the view param
      req.removeParameter(IPSHtmlParameters.SYS_VIEW);

      // add sys_default view
      viewSet.addView(new PSView(IPSConstants.DEFAULT_VIEW_NAME,
         fieldList.iterator()));

      // should default to sys_default for current view
      eval = new PSViewEvaluator(viewSet);
      assertTrue("field visible", eval.isFieldVisible("FIELD1", data));
      assertTrue("field not visible", !eval.isFieldVisible("foobar", data));
      assertTrue("getnextview: sys_Default", eval.getNextView(data, 0).equals(
         IPSConstants.DEFAULT_VIEW_NAME));
      assertTrue("getnextview: sys_Default", eval.getNextView(data, 1).equals(
         IPSConstants.DEFAULT_VIEW_NAME));

      // NOTE: cannot test conditional views without an app handler
   }
}
