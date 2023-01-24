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
