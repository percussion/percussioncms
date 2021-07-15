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
package com.percussion.webservices.sample.loader;

import com.percussion.webservices.content.ContentSOAPStub;
import com.percussion.webservices.content.PSField;
import com.percussion.webservices.content.PSFieldValue;
import com.percussion.webservices.content.PSFolder;
import com.percussion.webservices.content.PSItem;
import com.percussion.webservices.content.PSItemStatus;
import com.percussion.webservices.content.PSItemSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;
import com.percussion.webservices.sample.PSWsUtils;
import com.percussion.webservices.security.SecuritySOAPStub;
import com.percussion.webservices.system.SystemSOAPStub;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.rpc.ServiceException;

import org.apache.axis.attachments.AttachmentPart;

/**
 * This class loads all Content Items defined in a specified data file
 * (LoaderData.xml) into a specified Rhythmyx Folder. The location of the 
 * data file and the target Folder are specified in the properties file of the
 * loader, Loader.xml.  If the specified target Folder does not exist, it will 
 * be created. If the Folder contains any Content Items whose sys_title field 
 * has the same value (case-insensitive) as the sys_title field of a Content 
 * Item defined in the data file, the existing Content Item will be updated 
 * with the data from the data file.  If the target Folder does not contain any
 * Content Items whose sys_title field matches the sys_title field of a Content
 * Item defined in the data file, a new Content Item will be created.
 *
 * The properties file (Loader.xml) also specifies the Content Type used for the
 * uploaded Content Items.  The same Content Type is used to upload all Content
 * Items for a particular run of the loader.
 * 
 * The loader returns limited terminal window output while it processes each 
 * Content Item.
 */
public class PSLoader
{
   /**
    * This method is the starting point of the loader program. The properties
    * file, <code>Loader.xml</code>, is assumed to be in the same directory as
    * this class.
    *
    * @param args the arguments of the program, which are not used.
    */
   public static void main(String args[])
   {
      PSLoader loader = new PSLoader();

      try
      {
         loader.readInputData();
         loader.login();
         loader.createTargetFolder();
         loader.uploadItems();         
         loader.uploadDataFile();
         loader.logout();
      }
      catch (PSNotAuthenticatedFault e)
      {
         loader.consoleMessage("Caught PSNotAuthenticatedFault: "
               + e.getErrorMessage());
      }
      catch (PSContractViolationFault e)
      {
         loader.consoleMessage("Caught PSContractViolationFault: "
               + e.getErrorMessage());         
      }
      catch (RemoteException e)
      {
         loader.consoleMessage("Caught RemoteException: "
               + e.getMessage());         
      }      
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Updates Content Items read from LoaderData.xml into the target Folder.
    * Assumes the target folder already exists in Rhythmyx. 
    *
    * @throws Exception if an error occurs.
    */
   void uploadItems() throws Exception
   {
      // get the items in the target folder
      PSItemSummary[] curItems = PSWsUtils.findFolderChildren(m_contService,
            m_props.getProperty(TARGET_FOLDER));
      
      for (Map<String,String> itemFields : m_itemData)
      {
         PSItemSummary summary = getItem(curItems, itemFields.get("sys_title"));
         
         if (summary != null) // update the existing item
         {
            updateItem(summary.getId(), itemFields);
         }
         else
         {
            createItem(itemFields);
         }
      }
      
      consoleMessage("Finished uploading items into target Folder, " 
         + m_props.getProperty(TARGET_FOLDER));
   }

   /**
    * Creates a Content Item of Content Type specified in the properties 
    * file (Loader.xml) using the field values defined for the Content Item in 
    * the data file (LoaderData.xml).  The created Content Item is added to the
    * target Folder specified in the properties file. 
    * 
    * @param fields the set of field values for the created item; assumed not 
    *  to be <code>null</code> or empty.
    */
   private void createItem(Map<String,String> fields) throws Exception
   {
      PSItem item = PSWsUtils.createItem(m_contService, 
         (String)m_props.get(CONTENT_TYPE));
      
      setItemFields(item, fields);
      
      long id = PSWsUtils.saveItem(m_contService, item);
      PSWsUtils.checkinItem(m_contService, id);
      PSWsUtils.transitionItem(m_sysService, id, "DirecttoPublic");
      
      // Attach the Content Item to the Target folder
      String path = m_props.getProperty(TARGET_FOLDER);
      PSWsUtils.addFolderChildren(m_contService, path, new long[]{id});
      
      consoleMessage("Created item: " + fields.get("sys_title"));
   }
   
   /**
    * Updates the specified Content Item with the specified field values.
    * 
    * @param id the ID of the Content Item to update.
    * @param fields the new values of the specified fields.
    *  
    * @throws Exception if an error occurs.
    */
   private void updateItem(long id, Map<String,String> fields)
      throws Exception
   {
      PSItemStatus status = PSWsUtils.prepareForEdit(m_contService, id);
      
      PSItem item = PSWsUtils.loadItem(m_contService, id);
      setItemFields(item, fields);
      PSWsUtils.saveItem(m_contService, item);
      
      PSWsUtils.releaseFromEdit(m_contService, status);
      
      consoleMessage("Updated item: " + fields.get("sys_title"));
   }

   /**
    * Sets the fields of the specified Content Item to the specified values.
    * 
    * @param item the Content Items whose field values are to be updated.
    * @param fields the set of field values to be updated to the specified 
    *    Content Item; assumed not to be <code>null</code> or empty.
    */
   private void setItemFields(PSItem item, Map<String, String> fields)
   {
      for (PSField field : item.getFields())
      {
         if (fields.get(field.getName()) != null)
         {
            PSFieldValue value = new PSFieldValue();
            value.setRawData(fields.get(field.getName()));
            field.setPSFieldValue(new PSFieldValue[]{value});
         }
      }
   }
   
   /**
    * Determines whether the set of supplied Content Item summaries contains 
    * an item with the specified value in the sys_title field.
    * 
    * @param curItems the set of Content Item summaries to evaluate; assumed 
    *    not to be  <code>null</code>
    * @param sysTitle the value of sys_title for which to evaluate; assumed 
    *    not to be <code>null</code> or empty.
    *    
    * @return the summary of the specified item if found, <code>null</code> 
    *    otherwise.
    */
   private PSItemSummary getItem(PSItemSummary[] curItems, String sysTitle)
   {
      for (PSItemSummary item : curItems)
      {
         if (item.getName().equalsIgnoreCase(sysTitle))
            return item;
      }
      return null;
   }
   
   /**
    * Read the input data from the properties file (Loader.xml) and the data 
    * file (DataFile.xml).
    * 
    * @throws Exception if an error occurs.
    */
   void readInputData() throws Exception
   {
      consoleMessage("Read loader parameters...");
      m_props = PSFileUtils.getLoaderProperties();

      consoleMessage("Read loader data file, " + m_props.getProperty(DATA_FILE)
            + " ...");
      m_itemData = PSFileUtils.loadDataFile(m_props.getProperty(DATA_FILE));
   }

   /**
    * Logs in to the Rhythmyx server using the server connection and
    * authentication data specified in the loader properties file (Loader.xml)
    * 
    * @throws PSNotAuthenticatedFault if fails to login or any other error occrus.
    * @throws ServiceException if the method fails to create the new stub.
    */
   void login() throws PSNotAuthenticatedFault, ServiceException
   {
      PSWsUtils.setConnectionInfo(m_props.getProperty(PROTOCOL), 
            m_props.getProperty(HOST), 
            Integer.parseInt(m_props.getProperty(PORT)));
      
      m_secService = PSWsUtils.getSecurityService();
      m_rxSession = PSWsUtils.login(m_secService, 
            m_props.getProperty(USER_NAME),
            m_props.getProperty(PASSWORD),
            m_props.getProperty(COMMUNITY),
            null);      
      
      m_contService = PSWsUtils.getContentService(m_rxSession);
      m_sysService = PSWsUtils.getSystemService(m_rxSession);
   }
   
   /**
    * Logs out Rhythmyx for the current Rhythmyx session.
    *  
    * @throws Exception if any error occurs.
    */
   void logout() throws Exception
   {
      PSWsUtils.logout(m_secService, m_rxSession);
   }
   
   /**
    * Creates any target Folder specified in the properties file (Loader.xml) 
    * that does not already exist.
    * 
    * @throws Exception if an error occurs. 
    */
   void createTargetFolder() throws Exception
   {
      PSFolder[] folders = PSWsUtils.addFolderTree(m_contService, m_props
            .getProperty(TARGET_FOLDER));
      
      consoleMessage("AddFolderTree operation returned " + folders.length
            + " folders");
   }
   
   /**
    * Writes the specified message to the console.
    * 
    * @param msg the console message; assumed not <code>null</code>. 
    */
   void consoleMessage(String msg)
   {
      System.out.println(msg);
   }
   
   
   /**
    * Uploads the data file into the target Folder. The data file name
    * (LoaderData.xml) is specified in the loader properties file (Loader.xml). 
    * Assumes the target folder already exists in Rhythmyx. 
    *
    * @throws Exception if an error occurs.
    */
   void uploadDataFile() throws Exception
   {
      String filename = m_props.getProperty(DATA_FILE);
      String targetFolder = m_props.getProperty(TARGET_FOLDER);
      PSItemSummary[] curItems = PSWsUtils.findFolderChildren(m_contService,
            targetFolder);

      PSItemSummary summary = getItem(curItems, filename);
      
      if (summary != null) // update the existing item of File Content Type
      {
         updateFileItem(summary.getId(), filename);
      }
      else
      {
         createFileItem(filename);
      }
      
      consoleMessage("Finished uploading the Data File (" + filename
         + ") into target Folder, " + targetFolder);
   }
   
   /**
    * Creates a Content Item of File Content Type for the specified file.
    * The created Content Item is added to the target Folder.
    * 
    * @param filename the file name of the specified file; assumed not to be 
    *   <code>null</code> or empty.
    */
   void createFileItem(String filename) throws Exception
   {
      PSItem item = PSWsUtils.createItem(m_contService, "rffFile");
         
      setFileItemFields(item, filename);
         
      long id = PSWsUtils.saveItem(m_contService, item);
      PSWsUtils.checkinItem(m_contService, id);
      PSWsUtils.transitionItem(m_sysService, id, "DirecttoPublic");
         
      // Attach the Content Item to the Target folder
      String path = m_props.getProperty(TARGET_FOLDER);
      PSWsUtils.addFolderChildren(m_contService, path, new long[]{id});
   }

   /**
    * Updates a Content Item of File Content Type for the specified file.
    * 
    * @param id the id of the specified Content Item.
    * @param filename the file name of the specified file; assumed not to be 
    *   <code>null</code> or empty.
    */
   void updateFileItem(long id, String filename) throws Exception
   {
      PSItemStatus status = PSWsUtils.prepareForEdit(m_contService, id);
      
      PSItem item = PSWsUtils.loadItem(m_contService, id);
      
      // read the orignal binary data before update
      byte[] content = retrieveBinaryData(m_contService, item);
      consoleMessage("\nThe original content data: \n" + new String(content));
      
      // set the new data
      setFileItemFields(item, filename);
      PSWsUtils.saveItem(m_contService, item);
      
      PSWsUtils.releaseFromEdit(m_contService, status);
   }
   
   /**
    * Retrieves the binary value from the specified Content Item.
    * 
    * @param binding the stub to which to add the attachement; assumed not
    *    <code>null</code>.
    * @param item the Content Item from which to retrieve the binary value;
    *    assumed it is a Content Item of File Content Type.
    * 
    * @return the binary value, never <code>null</code>.
    */
   byte[] retrieveBinaryData(ContentSOAPStub binding, PSItem item)
      throws Exception
   {
      // Gets the attachment id from the value of the "item" field
      String attachmentId = "";
      for (PSField field : item.getFields())
      {
         if (field.getName().equals("item_file_attachment"))
         {
            PSFieldValue[] values = field.getPSFieldValue();
            attachmentId = values[0].getAttachmentId();
            break;
         }
      }
    
      // Retrieves the attachment with the attachment id
      Object[] attachments = binding.getAttachments();
      for (Object attachment : attachments)
      {
         AttachmentPart part = (AttachmentPart) attachment;
         if (part.getContentId().equals(attachmentId))
         {
            try(InputStream reader = (InputStream) part.getContent()){
               byte[] content = new byte[reader.available()];
               reader.read(content);
               return content;
            }
         }
      }
      return null;

   }

   /**
    * Sets the specified file to the specified File Content Item.
    *  
    * @param item the File Content Item; assumed not <code>null</code>.
    * @param filename the file name of the specified file; assumed not 
    *    <code>null</code> or empty.
    */
   void setFileItemFields(PSItem item, String filename) throws Exception
   {
      URL url = this.getClass().getResource(filename);
      for (PSField field : item.getFields())
      {
         String name = field.getName();
         PSFieldValue newValue = new PSFieldValue();
         if (name.equals("sys_title") || name.equals("displaytitle"))
         {
            newValue.setRawData(filename);               
            field.setPSFieldValue(new PSFieldValue[] { newValue });
         }
         else if (name.equals("item_file_attachment"))
         {
            String attachmentId = addAttachment(m_contService, url);
            newValue.setAttachmentId(attachmentId);
            field.setPSFieldValue(new PSFieldValue[] { newValue });
         }
         else if (name.equals("item_file_attachment_type"))
         {
            newValue.setRawData("text/xml");               
            field.setPSFieldValue(new PSFieldValue[] { newValue });         
         }
         else if (name.equals("item_file_attachment_ext"))
         {
            newValue.setRawData(".xml");               
            field.setPSFieldValue(new PSFieldValue[] { newValue });         
         }
         else if (name.equals("item_file_attachment_filename"))
         {
            newValue.setRawData(filename);               
            field.setPSFieldValue(new PSFieldValue[] { newValue });         
         }
         else if (name.equals("item_file_attachment_size"))
         {
            File file = new File(url.toURI());
            long fileSize = file.length();
            newValue.setRawData(String.valueOf(fileSize));               
            field.setPSFieldValue(new PSFieldValue[] { newValue });         
         }
         else if (name.equals("sys_workflowid"))
         {
            newValue.setRawData("5");
            field.setPSFieldValue(new PSFieldValue[] { newValue });                     
         }
      }
   }
   
   /**
    * Add the supplied attachment to the provided proxy and return the 
    * attachment id.
    * 
    * @param binding the stub to which to add the attachement, assumed not
    *    <code>null</code>.
    * @param attachment the file to attach, assumed not <code>null</code>.
    * @return the attachment id needed on the server to map this to the 
    *    appropriate field, never <code>null</code> or empty.
    */
   private String addAttachment(ContentSOAPStub binding, URL attachment)
   {
      DataHandler handler = new DataHandler(new URLDataSource(attachment));

      AttachmentPart part = new AttachmentPart(handler);
      binding.addAttachment(part);

      return part.getContentId();
   }

   /**
    * The Rhythmyx session, initialized by {@link #login()}. 
    */
   String m_rxSession;
   
   /**
    * The security service instance; used to perform operations defined in
    * the security services. It is initialized by {@link #login()}.
    */
   SecuritySOAPStub m_secService;
   
   /**
    * The content service instance; used to perform operations defined in
    * the content services. It is initialized by {@link #login()}.
    */
   ContentSOAPStub m_contService;
   
   /**
    * The system service instance; used to perform operations defined in
    * the system service. It is initialized by {@link #login()}.
    */
   SystemSOAPStub m_sysService;
   
   /**
    * The loader properties, read from the file 'Loader.xml'.
    */
   private Properties m_props;
   
   /**
    * The Content Item data to be uploaded; read from the file 'DataFile.xml' 
    */
   private List<Map<String, String>> m_itemData;

   /**
    * The property name of the protocol of the server connection. 
    */
   public static String PROTOCOL = "Protocol";

   /**
    * The property name of the host of the server connection. 
    */
   public static String HOST = "Host";

   /**
    * The property name of the port of the server connection.
    */
   public static String PORT = "Port";
   
   /**
    * The property name of the name of the login user.
    */
   public static String USER_NAME = "Username";
   
   /**
    * The property name of the password of the login user.
    */
   public static String PASSWORD = "Password";

   /**
    * The property name of the name of the login Community. 
    */
   public static String COMMUNITY = "Community";
   
   /**
    * The property name of the name of the Content Type of the Content Items to
    * be uploaded. 
    */
   public static String CONTENT_TYPE = "ContentType";
   
   /**
    * The property name of the target Folder path in Rhythmyx. 
    */
   
   public static String TARGET_FOLDER = "TargetFolder";
   
   /**
    * The property name of the name of the data file. 
    */
   public static String DATA_FILE = "DataFile";
}
