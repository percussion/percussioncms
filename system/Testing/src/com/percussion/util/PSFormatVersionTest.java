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

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;

import org.w3c.dom.Document;

import junit.framework.TestCase;


/**
 * The PSFormatVersionTest is the unit test for the PSFormatVersion class which
 * handles build version string formatting operations.
 */
public class PSFormatVersionTest extends TestCase
{
   public void testToXml() throws Exception
   {
      PSFormatVersion fv = new PSFormatVersion("com.percussion.util");
      assertNotNull(fv.getVersion());
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSFormatVersion fv2 = PSFormatVersion.createFromXml(fv.toXml(doc));
      
      checkEqualProperties(fv, fv2);
   }
   
   public void testCreateFromXml() throws Exception
   {
      Document withDisplayDoc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(FORMAT_VERSION_DISPLAY), false);
      
      PSFormatVersion fv = PSFormatVersion.createFromXml(
            withDisplayDoc.getDocumentElement());
      assertNotNull(fv.getBuildId());
      assertNotNull(fv.getBuildNumber());
      assertNotNull(fv.getVersion());
      assertNotNull(fv.getOptionalId());
      assertNotNull(fv.getVersionString());
      assertFalse(fv.getInterfaceVersion() == 0);
      assertFalse(fv.getMajorVersion() == 0);
      assertFalse(fv.getMicroVersion() == 0);
      assertFalse(fv.getMinorVersion() == 0);
                  
      Document noDisplayDoc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(FORMAT_VERSION_NO_DISPLAY), false);
      
      PSFormatVersion fv2 = PSFormatVersion.createFromXml(
            noDisplayDoc.getDocumentElement());
      assertNotNull(fv2.getVersion());
      
      checkEqualProperties(fv, fv2);
   }

   /**
    * Ensures that the following properties are equal for two
    * <code>PSFormatVersion</code> objects:
    * 
    * buildid, buildnumber, displayversion, optionalid, versionstring,
    * interfaceversion, majorversion, minorversion, microversion.
    *  
    * @param fv1 
    * @param fv2
    * @throws Exception
    */
   private void checkEqualProperties(PSFormatVersion fv1, PSFormatVersion fv2)
   throws Exception
   {
      assertTrue(fv1.getBuildId().equals(fv2.getBuildId()));
      assertTrue(fv1.getBuildNumber().equals(fv2.getBuildNumber()));
      assertTrue(fv1.getVersion().equals(fv2.getVersion()));
      assertTrue(fv1.getOptionalId().equals(fv2.getOptionalId()));
      assertTrue(fv1.getVersionString().equals(fv2.getVersionString()));
      assertTrue(fv1.getInterfaceVersion() == fv2.getInterfaceVersion());
      assertTrue(fv1.getMajorVersion() == fv2.getMajorVersion());
      assertTrue(fv1.getMicroVersion() == fv2.getMicroVersion());
      assertTrue(fv1.getMinorVersion() == fv2.getMinorVersion());
   }
   
   private int INTERFACE_VERSION = 9;
   private int MAJOR_VERSION = 6;
   private int MINOR_VERSION = 5;
   private int MICRO_VERSION = 1;
   private String BUILDID = "3168";
   private String BUILDNUMBER = "20070901";
   private String OPTIONALID = "";
   private String DISPLAY_VERSION = "6.5.1";
   private String VERSION_STRING = "INTERNAL";
   
   private String FORMAT_VERSION_DISPLAY = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<PSXFormatVersion buildId=\"" + BUILDID + "\"" +
      " buildNumber=\"" + BUILDNUMBER + "\"" +
      " displayVersion=\"" + DISPLAY_VERSION + "\"" +
      " interfaceVersion=\"" + INTERFACE_VERSION + "\"" +
      " majorVersion=\"" + MAJOR_VERSION + "\"" +
      " microVersion=\"" + MICRO_VERSION + "\"" +
      " minorVersion=\"" + MINOR_VERSION + "\"" +
      " optionalId=\"" + OPTIONALID + "\"" +
      " versionString=\"" + VERSION_STRING + "\"/>";
   
   private String FORMAT_VERSION_NO_DISPLAY =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<PSXFormatVersion buildId=\"" + BUILDID + "\"" +
      " buildNumber=\"" + BUILDNUMBER + "\"" +
      " interfaceVersion=\"" + INTERFACE_VERSION + "\"" +
      " majorVersion=\"" + MAJOR_VERSION + "\"" +
      " microVersion=\"" + MICRO_VERSION + "\"" +
      " minorVersion=\"" + MINOR_VERSION + "\"" +
      " optionalId=\"" + OPTIONALID + "\"" +
      " versionString=\"" + VERSION_STRING + "\"/>";
}
