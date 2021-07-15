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
