/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.filetracker;

import com.percussion.security.xml.PSSecureXMLUtils;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;

/**
 * Main Application class with main() method. Main window is constructed and
 * displayed.
 */
public class RXFileTracker
{
   boolean packFrame = false;

   /**
    * Construct the application center the window and then display it.
    */
   public RXFileTracker()
   {
      MainFrame frame = new MainFrame();
      //Validate frames that have preset sizes
      //Pack frames that have useful preferred size info, e.g. from their layout
      if (packFrame)
         frame.pack();
      else
         frame.validate();
      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = frame.getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;
      frame.setLocation((screenSize.width - frameSize.width) / 2,
         (screenSize.height - frameSize.height) / 2);
      frame.setVisible(true);
   }

   /**
    * Main method. Does not require any arguments currently.
    */
   public static void main(String[] args)
   {

      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e)
      {
      }
      new RXFileTracker();
   }

   /**
    * Returns DocumentBuilder object for parsing XML documents
    * @return DocumentBuilder object for parsing XML documents. Never
    * <code>null</code>.
    */
    public static DocumentBuilder getDocumentBuilder()
   {
      try
      {
         DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                 false);

         dbf.setNamespaceAware(true);
         dbf.setValidating(false);
         return dbf.newDocumentBuilder();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage());
      }
   }

}
