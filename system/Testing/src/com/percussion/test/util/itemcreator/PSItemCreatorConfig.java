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
package com.percussion.test.util.itemcreator;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Object that represents the PSItemCreation configuration file.
 */
public class PSItemCreatorConfig
{
   /**
    * Ctor
    * You will need to call {@link #loadConfigFile(String)}
    * to populate this object.
    */
   public PSItemCreatorConfig()
   {}
   
   /**
    * Ctor 
    * @param filepath the path to the configuration xml file, cannot
    * be <code>null</code> or empty.
    * @throws PSInvalidItemCreatorConfigException upon any error
    */
   public PSItemCreatorConfig(final String filepath)
      throws PSInvalidItemCreatorConfigException
   {
      loadConfigFile(filepath);
   }
   
   /**
    * Loads the configuration xml file specified and then
    * passes it to {@link #fromXml(Element)} in order to populate
    * this object.
    * @param filepath the path to the configuration xml file, cannot
    * be <code>null</code> or empty.
    * @throws PSInvalidItemCreatorConfigException upon any error.
    */
   public void loadConfigFile(final String filepath)
      throws PSInvalidItemCreatorConfigException
   {
      if(filepath == null || filepath.trim().length() == 0)
         throw new IllegalArgumentException(
            "filepath cannot be null or empty.");
      File file = new File(filepath);
      if(!file.exists() || !file.isFile())
         throw new PSInvalidItemCreatorConfigException(
            "Configuration file specified does not exist.");
      InputStream is = null;
            
      try
      {
         is = new FileInputStream(file);
         Document doc = 
            PSXmlDocumentBuilder.createXmlDocument(is, false);
         fromXml(doc.getDocumentElement());
      }      
      catch (IOException e)
      {
         throw new PSInvalidItemCreatorConfigException(
            "IOException when loading config file.", e);
      }
      catch (SAXException e)
      {
         throw new PSInvalidItemCreatorConfigException(
            "SAXException when loading config file.", e);
      }
      finally
      {
         try
         {
            if(is != null)
               is.close();
         }
         catch(IOException ignore){}
      }
   }
   
   /**
    * Returns all of the create item objects from the config
    * file.
    * @return Iterator of CreateItems objects, never <code>null</code>,
    * may be empty.
    */
   public Iterator getCreateItems()
   {
      return m_createItems.iterator();
   }
   
   private void fromXml(final Element elem)
      throws PSInvalidItemCreatorConfigException
   {
      m_createItems = new ArrayList();
      NodeList nl = elem.getElementsByTagName(ELEM_CREATE_ITEMS);
      int len = nl.getLength();
      for(int i = 0; i < len; i++)
      {
         m_createItems.add(new CreateItem((Element)nl.item(i)));
      }
   }
   
   /**
    * Holds all ELEM_CREATE_ITEMS objects, initialized in
    * {@link #fromXml(Element)}. Never <code>null</code>
    * after that but may be empty.
    */
   private List m_createItems;
   
   /**
    * Object that represents a createItems element from the
    * xml configuration file.
    */
   class CreateItem
   {
      CreateItem(Element item) throws PSInvalidItemCreatorConfigException
      {
         fromXml(item);
      }
      
      private void fromXml(Element item)
         throws PSInvalidItemCreatorConfigException
      {
         NodeList nl = null;
         mi_amount =  Integer.parseInt(
            validateAttribute(item, ATTR_AMOUNT, true, true, null));
         mi_community = 
            validateAttribute(item, ATTR_COMMUNITY, true, true, null);
         mi_contenttype = 
            validateAttribute(item, ATTR_CONTENTTYPE, true, true, null);
         mi_workflow = 
            validateAttribute(item, ATTR_WORKFLOW, true, true, null);
         mi_titlePrefix = 
            validateAttribute(item, ATTR_TITLE_PREFIX, false, true, null);
         mi_folderid =
            validateAttribute(item, ATTR_FOLDER, true, false, null);
         // Get connection info
         nl = item.getElementsByTagName(ELEM_CONN_INFO);
         if(nl.getLength() == 0)
            throw new PSInvalidItemCreatorConfigException(
               "Required connectionInfo element missing.");
         Element connInfo = (Element)nl.item(0);
         mi_host = 
            validateAttribute(connInfo, ATTR_HOST, false, true, null);
         mi_port = Integer.parseInt(
            validateAttribute(connInfo, ATTR_PORT, true, false, "-1"));
         mi_sslport = Integer.parseInt(
            validateAttribute(connInfo, ATTR_SSLPORT, true, false, "-1"));
         mi_user = 
            validateAttribute(connInfo, ATTR_USER, false, true, null);
         mi_password = 
            validateAttribute(connInfo, ATTR_PASSWORD, false, true, null);          
            
         // Get Fields
         nl = item.getElementsByTagName(ELEM_FIELDS);
         if(nl.getLength() > 0)
         {
            Element fields = (Element)nl.item(0);
            nl = fields.getElementsByTagName(ELEM_FIELD);
            int len = nl.getLength();
            for(int i = 0; i < len; i++)
            {
               Element field = (Element)nl.item(i);
               String fieldname = 
                  validateAttribute(field, ATTR_NAME, false, true, null);
               // Skip any field that is in the ignore collection
               if(ms_ignore.contains(fieldname.toLowerCase()))
                  continue;
               // get values               
               NodeList vList = field.getElementsByTagName(ELEM_VALUE);
               int vLen = vList.getLength();
               List values = new ArrayList(vLen);
               for(int c = 0; c < vLen; c++)
               {
                  Element value = (Element)vList.item(c);
                  if(value.hasChildNodes())
                  {
                     Node child = value.getFirstChild();
                     if(child instanceof Text)
                        values.add(((Text)child).getData());
                     else
                        throw new PSInvalidItemCreatorConfigException(
                           "Invalid value element.");
                  }
                  else
                     values.add("");
               }
               mi_fields.put(fieldname, values);
            }
         }
      }
      
      /**
       * Validates the specified attribute from the element passed
       * in. If valid it returns the attributes value or the default
       * value if the attribute does not exist. If both the default
       * value was <code>null</code> and the attribute does not exist
       * or is not set then an empty string is returned.
       * 
       * @param elem the element to which the attribute belongs,
       *   assumed not <code>null</code>
       * @param name the name of the attribute to be validated, 
       *  assumed not <code>null</code> or empty. 
       * @param isInt flag indicating that the attribute must be
       *  an integer.
       * @param isReq flag indicating that the attribute is required.
       * @param def the default value to be used if this attribute
       * is not required and no attribute or attribute value was found.
       * May be <code>null</code>.
       * @return the attribute value or default value. Never <code>null</code>
       * , may be empty.
       * @throws PSInvalidItemCreatorConfigException on any validation error.
       */
      private String validateAttribute(
         Element elem, String name, boolean isInt, boolean isReq, String def)
         throws PSInvalidItemCreatorConfigException
      {
         String val = elem.getAttribute(name);
         if(isReq)
         {
            if(val == null || val.trim().length() == 0)
               throw new PSInvalidItemCreatorConfigException(
                  "Attribute '" + name + 
                    "' is required in element '" + elem.getNodeName() + "'.");
         }
         if(isInt && val != null && val.trim().length() > 0)
         {
            try
            {
               Integer.parseInt(val);
            }
            catch(NumberFormatException e)
            {
               throw new PSInvalidItemCreatorConfigException(
                  "Attribute '" + name + "' in element '" + 
                  elem.getNodeName() +
                  "' is not a valid integer.");
            }
         }
         if(val == null || val.trim().length() == 0)
            return def == null ? "" : def;
         return val;
         
      }
      
      /**
       * @return Returns the amount.
       */
      public int getAmount()
      {
         return mi_amount;
      }
      /**
       * @return Returns the community.
       */
      public String getCommunity()
      {
         return mi_community;
      }
      /**
       * @return Returns the contenttype.
       */
      public String getContenttype()
      {
         return mi_contenttype;
      }
      /**
       * @return Returns the folderid.
       */
      public String getFolderid()
      {
         return mi_folderid;
      }     
      /**
       * @return Returns the host.
       */
      public String getHost()
      {
         return mi_host;
      }
      /**
       * @return Returns the password.
       */
      public String getPassword()
      {
         return mi_password;
      }
      /**
       * @return Returns the port.
       */
      public int getPort()
      {
         return mi_port;
      }
      /**
       * @return Returns the sslport.
       */
      public int getSslport()
      {
         return mi_sslport;
      }
      /**
       * @return Returns the titleprefix.
       */
      public String getTitlePrefix()
      {
         return mi_titlePrefix;
      }
      /**
       * @return Returns the user.
       */
      public String getUser()
      {
         return mi_user;
      }
      /**
       * @return Returns the workflow.
       */
      public String getWorkflow()
      {
         return mi_workflow;
      }
      
      /** 
       * @return the fields
       */
      public Map getFields()
      {
         return Collections.unmodifiableMap(mi_fields);
      }
      
      private int mi_amount = -1;
      private String mi_community = "-1";
      private String mi_contenttype = "-1";
      private String mi_workflow = "-1";
      private String mi_host = "localhost";
      private int mi_port = -1;
      private int mi_sslport = -1;
      private String mi_user;
      private String mi_password;
      private String mi_folderid;      
      private String mi_titlePrefix;
      private Map mi_fields = new HashMap();     
      
   }
   /* Element constants */
   public static final String ELEM_ITEM_CREATOR_CONFIG = "itemCreatorConfig";
   public static final String ELEM_CREATE_ITEMS = "createItems";
   public static final String ELEM_CONN_INFO = "connectionInfo";   
   public static final String ELEM_FIELDS = "fields";
   public static final String ELEM_FIELD = "field";
   public static final String ELEM_VALUE = "value";
   /* Attribute constants */
   public static final String ATTR_AMOUNT = "amount";
   public static final String ATTR_COMMUNITY = "community";
   public static final String ATTR_CONTENTTYPE = "contenttype";
   public static final String ATTR_FOLDER = "folder";
   public static final String ATTR_TITLE_PREFIX = "titleprefix";
   public static final String ATTR_WORKFLOW = "workflow";
   public static final String ATTR_HOST = "host";
   public static final String ATTR_PORT = "port";
   public static final String ATTR_SSLPORT = "sslport";
   public static final String ATTR_USER = "user";
   public static final String ATTR_PASSWORD = "password";
   public static final String ATTR_ID = "id";
   public static final String ATTR_NAME = "name";
   
   private static final Collection ms_ignore = new ArrayList();
   static
   {
      ms_ignore.add("displaytitle");
      ms_ignore.add("sys_workflow");
      ms_ignore.add("sys_title");
      
   }
   
   
   
   

}
