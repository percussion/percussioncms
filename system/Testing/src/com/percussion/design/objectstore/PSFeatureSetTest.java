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
