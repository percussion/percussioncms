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
