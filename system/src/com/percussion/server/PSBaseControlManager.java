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
package com.percussion.server;

import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JaySeletz
 *
 */
public abstract class PSBaseControlManager
{

   /**
    * 
    */
   public PSBaseControlManager()
   {
      super();
   }

   /**
    * Gets the specified control from the specified file.
    * 
    * @param ctrlFile The control file, assumed not <code>null</code>.
    * @param name The control name, assumed not <code>null</code>.
    * 
    * @return The specified control as a {@link PSControlMeta} object.  Returns
    * <code>null</code> if the control was not found.
    */
   protected PSControlMeta getControl(File ctrlFile, String name)
   {
      PSControlMeta ctrl = null;
      
      List<PSControlMeta> ctrls = getControls(ctrlFile);
      for (PSControlMeta meta : ctrls)
      {
         if (meta.getName().equals(name))
         {
            ctrl = meta;
            break;
         }
      }
          
      return ctrl;
   }

   /**
    * Gets controls from the specified file.
    * 
    * @param ctrlFile The control file, assumed not <code>null</code>.
    * 
    * @return List of controls as {@link PSControlMeta} objects.  Never
    * <code>null</code>, may be empty.
    */
   protected List<PSControlMeta> getControls(File ctrlFile)
   {
      List<PSControlMeta> ctrls = new ArrayList<>();
      FileInputStream fin = null;
      
      try
      {
         fin = new FileInputStream(ctrlFile);
   
         Document doc = PSXmlDocumentBuilder.createXmlDocument(fin, false);

         NodeList nodes = doc.getElementsByTagName(
               PSControlMeta.XML_NODE_NAME);
         for (int i = 0; i < nodes.getLength(); i++) 
         {
            Element control = (Element) nodes.item(i);
            ctrls.add(new PSControlMeta(control));
         }
      }
      catch (Exception e)
      {
         PSConsole.printMsg(getSubSystem(), e);
      }
      finally
      {
         if (fin != null)
         {
            try
            {
               fin.close();
            }
            catch (IOException e)
            {
               
            }
         }
      }
      
      return ctrls;
   }

   /**
    * Get the subsystem to use for logging errors.
    * @return
    */
   protected abstract String getSubSystem();
   
   /**
    * Gets the set of current control files.
    * 
    * @return The set of current  control files as a list.  Never
    * <code>null</code>, may be empty.
    */
   public abstract List<File> getControlFiles();

   /**
    * Gets the custom control object corresponding to the specified name.
    * 
    * @param name The control name, may not be blank.
    * 
    * @return The {@link PSControlMeta} object with the specified name or
    * <code>null</code> if the control does not exist.
    */
   public PSControlMeta getControl(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be blank");
      }
      
      PSControlMeta ctrl = null;
   
      List<File> ctrlFiles = getControlFiles();
      for (File ctrlFile : ctrlFiles)
      {
         ctrl = getControl(ctrlFile, name);
         if (ctrl != null)
         {
            break;
         }
      }
   
      return ctrl;
   }

   /**
    * Gets all custom controls.
    * 
    * @return List of all custom controls.  Never <code>null</code>, may be
    * empty.
    */
   public List<PSControlMeta> getAllControls()
   {
      List<PSControlMeta> controls = new ArrayList<>();
      
      List<File> ctrlFiles = getControlFiles();
      for (File ctrlFile : ctrlFiles)
      {
         controls.addAll(getControls(ctrlFile));
      }
      
      return controls;
   }
}
