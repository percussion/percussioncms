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
package com.percussion.servlets.utils;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class PSExtractComponentUrlsTest extends TestCase
{
   static final String ms_testDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> "
         + " <slotcomponents>"
         + " <component name=\"cmp_banner\" slotname=\"slt_banner\">"
         + "  <url>http://127.0.0.1:8080/Rhythmyx/sys_cmpBanner/banner.html?sys_componentname=cmp_banner&amp;sys_pagename=sys_variants&amp;workflowid=&amp;sys_componentid=2&amp;sys_sortparam=&amp;sys_sysnavcontentid=307</url> "
         + "  </component>"
         + " <component name=\"sys_nav\" slotname=\"slt_sys_nav\">"
         + "  <url>http://127.0.0.1:8080/Rhythmyx/sys_cmpSysLeftnav/leftnav.html?sys_componentname=sys_nav&amp;sys_pagename=sys_variants&amp;workflowid=&amp;sys_componentid=14&amp;sys_sortparam=&amp;sys_sysnavcontentid=307</url> "
         + "  </component>"
         + " <component name=\"cmp_userstatus\" slotname=\"slt_userstatus\">"
         + "  <url>http://127.0.0.1:8080/Rhythmyx/sys_cmpUserStatus/userstatus.html?sys_componentname=cmp_userstatus&amp;sys_pagename=sys_variants&amp;workflowid=&amp;sys_componentid=1&amp;sys_sortparam=&amp;sys_sysnavcontentid=307</url> "
         + "  </component>" + "  </slotcomponents>";
   
   public void testit() throws Exception
   {
      InputSource source = new InputSource(new StringReader(ms_testDoc));
      Document d = PSXmlDocumentBuilder.createXmlDocument(source, false);
      PSExtractComponentUrls ecu = new PSExtractComponentUrls(d);
      
      assertEquals(ecu.getComponentUrl("cmp_banner"),"http://127.0.0.1:8080/Rhythmyx/sys_cmpBanner/banner.html?sys_componentname=cmp_banner&sys_pagename=sys_variants&workflowid=&sys_componentid=2&sys_sortparam=&sys_sysnavcontentid=307");
      assertEquals(ecu.getComponentUrl("sys_nav"),"http://127.0.0.1:8080/Rhythmyx/sys_cmpSysLeftnav/leftnav.html?sys_componentname=sys_nav&sys_pagename=sys_variants&workflowid=&sys_componentid=14&sys_sortparam=&sys_sysnavcontentid=307");
      assertEquals(ecu.getComponentUrl("cmp_userstatus"),"http://127.0.0.1:8080/Rhythmyx/sys_cmpUserStatus/userstatus.html?sys_componentname=cmp_userstatus&sys_pagename=sys_variants&workflowid=&sys_componentid=1&sys_sortparam=&sys_sysnavcontentid=307");
      assertNull(ecu.getComponentUrl("foo"));
   }
}
