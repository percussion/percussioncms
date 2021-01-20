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

package com.percussion.util;

import com.percussion.HTTPClient.NVPair;
import com.percussion.HTTPClient.PSBinaryFileData;
import com.percussion.server.PSRequest;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



/**
 * @author ErikSerating
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@Category(IntegrationTest.class)
public class PSRemoteRequesterTest
{


   public void oneTimeSetUp(PSRequest req)
   {
      //no op
   }

   public void oneTimeTearDown()
   {
      //no op
   }

   /**
    * @param arg0
    */
   public PSRemoteRequesterTest()
   {
   }

   @Test
   public void testGetDocument() throws Exception
   {
      IPSRemoteRequesterEx requester = getRemoteRequester();
      String resource = "sys_cmpCommunities/communities.xml";
      Map params = new HashMap();
      Document doc = requester.getDocument(resource, params);
      System.out.println(PSXmlDocumentBuilder.toString(doc));
   }

   @Test
   public void testGetBinary() throws Exception
   {
      IPSRemoteRequesterEx requester = getRemoteRequester();
      String resource = "rx_ceImage/image";
      Map params = new HashMap(3);
      params.put("sys_command","binary");
      params.put("sys_contentid", "302");
      params.put("sys_revision", "1");
      params.put("sys_submitname", "imgbody");
      byte[] bin = requester.getBinary(resource, params);

      File file = File.createTempFile("test","gif", null);
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(bin);
      fos.close();
   }

   @Test
   public void testSendBinary() throws Exception
   {
      IPSRemoteRequesterEx requester = getRemoteRequester();
      String resource = "rx_ceImage/image";
      Map params = new HashMap(3);
      params.put("sys_title", "TEST IMAGE");
      params.put("sys_command","modify");
      params.put("sys_contentid", "302");
      params.put("sys_revision", "1");
      params.put("sys_view", "sys_All");
      params.put("sys_pageid", "0");
      params.put("DBActionType", "UPDATE");
      params.put("sys_workflowid", "3");

      File file = File.createTempFile("test","gif", null);
      FileInputStream fis = new FileInputStream(file);
      int fileLen = (int)file.length();
      byte[] data = new byte[fileLen];
      fis.read(data, 0, fileLen);
      fis.close();

      PSBinaryFileData bfd = new PSBinaryFileData(
         data, "imgbody", file.getAbsolutePath(), null);


      requester.updateBinary(new PSBinaryFileData[] {bfd}, resource, params);



   }

   @Test
   public void testSendBinary2() throws Exception
   {
      IPSRemoteRequesterEx requester = getRemoteRequester();
      String resource = "rx_ceImage/image";
      Map params = new HashMap();
      params.put("sys_title", "TEST IMAGE");
      params.put("sys_command","modify");
      params.put("sys_contentid", "312");
      params.put("sys_revision", "1");
      params.put("sys_view", "sys_All");
      params.put("sys_pageid", "0");
      params.put("DBActionType", "UPDATE");
      params.put("sys_workflowid", "3");


      NVPair pair = new NVPair("imgbody", "c:/arrow.gif");


      //requester.sendBinary(new NVPair[] {pair}, resource, params);



   }


   /**
    * Returns a new <code>PSRemoteRequester</code> object.
    * @return the remote requester. Never <code>null</code>.
    */
   private IPSRemoteRequesterEx getRemoteRequester()
   {
      Properties connInfo = new Properties();
      connInfo.put("hostName", "localhost");
      connInfo.put("port", "9992");
      connInfo.put("loginId", "admin1");
      connInfo.put("loginPw", "demo");

      return new PSRemoteRequester(connInfo);

   }


}
