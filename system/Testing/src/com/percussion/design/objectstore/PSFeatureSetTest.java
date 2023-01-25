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

package com.percussion.design.objectstore;

import com.percussion.server.PSServer;
import com.percussion.util.PSProperties;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import junit.framework.TestCase;

import org.w3c.dom.Document;


public class PSFeatureSetTest extends TestCase
{
	
	//placeholder test so junit does not complain
	public void testPlaceholderTest() throws Exception{
		assertTrue(true);
	}
	
	//obsolute test
   public void ignore_testFeatureSet() throws Exception
   {
      // load source xml file      
      String fileName = PSProperties.getConfig(PSServer.ENTRY_NAME,
      PSFeatureSet.FEATURE_SET_FILE, PSServer.getRxConfigDir()).getAbsolutePath();
            
      // build a doc with it
      InputStream in = new FileInputStream(fileName);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);

      // create the feature set object with the source xml
      PSFeatureSet set = new PSFeatureSet();
      set.fromXml(doc);
      Iterator features = set.getFeatureSet();
      Iterator versions;

      // check that we got a list with at least one feature
      assertTrue(features.hasNext());
      while (features.hasNext())
      {
         PSFeature feature = (PSFeature)features.next();
         versions = feature.getVersionList();
         assertTrue(versions.hasNext());
         PSVersion version = (PSVersion)versions.next();
         assertTrue(version.getNumber() > 0 );
         assertTrue(version.getDate() != null );
      }                     

   }
}
