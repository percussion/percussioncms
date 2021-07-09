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
package com.percussion.test.util.itemcreator;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.cms.objectstore.ws.PSRemoteFolderProcessor;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.util.PSRemoteRequester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This application allows the easy creation of content items for testing on
 * any Rhythmyx server using any database type. We use the web services handler to 
 * create the new content.
 */
public class PSItemCreator
{

   private static final Logger log = LogManager.getLogger(PSItemCreator.class);

   public PSItemCreator(String filepath)
      throws PSInvalidItemCreatorConfigException
   {
      loadConfig(filepath);
   }
   
   /**
    *  Executes the item creation.
    * @throws PSItemCreatorException upon any validation and execution
    * errors.
    * @throws PSRemoteException upon remote agent errors
    */
   public void execute() throws PSItemCreatorException, PSRemoteException
   {
      Iterator it = m_config.getCreateItems();
      PSItemCreatorConfig.CreateItem item = null;
      while(it.hasNext())
      {
         item = (PSItemCreatorConfig.CreateItem)it.next();
         if(item.getAmount() < 1)
            continue;         
         PSRemoteAgent agent = getRemoteAgent(item);
         validate(agent, item);
         PSClientItem cItem = agent.newItemDefault(item.getContenttype());
         // add workflow field
         addItemValue(cItem, "sys_workflowid", item.getWorkflow());
         // add other fields
         Map fields = item.getFields();
         Iterator keys = fields.keySet().iterator();
         while(keys.hasNext())
         {
            String key = (String)keys.next();
            addItemValue(cItem, key, ((List)fields.get(key)).iterator());
         }
         // Item creation loop
         List locators = new ArrayList();
         List temp = null;
        
         for(int idx = 0; idx < item.getAmount(); idx++)
         {
            if(idx == 0 || idx % 1000 == 0)
            {
               if(idx > 0)
                  locators.add(temp);
               temp = new ArrayList();
            }
            String title = item.getTitlePrefix() + " " + (idx + 1);
            addItemValue(
               cItem, "displaytitle", title);
            addItemValue(
               cItem, "sys_title", title);
            System.out.println("Now creating [" + title + "].");
            temp.add(agent.updateItem(cItem, true));
         }
         locators.add(temp);
         addItemsToFolder(item, locators);
         
         
      }
   }
   
   private void addItemsToFolder(PSItemCreatorConfig.CreateItem item, List locators)
      throws PSItemCreatorException
   {
      if(item.getFolderid().trim().length() == 0)
         return;
      PSLocator targetFolder = new PSLocator(item.getFolderid());
      PSRemoteFolderProcessor processor = getRemoteFolderProcessor(item);
      Iterator it = locators.iterator();
      while(it.hasNext())
      {
         List keys = (List)it.next();
         try
         {
            processor.addChildren(keys, targetFolder);
         }
         catch (PSCmsException e)
         {
            throw new PSItemCreatorException("Error adding items to folder", e);
         }
      }
      
      
   }
   
   /**
    * 
    * @param item
    * @param fieldname
    * @param value
    */
   private void addItemValue(PSClientItem item, String fieldname, String value)
   {
      PSItemField field = item.getFieldByName(fieldname);
      if(field != null)
      {
         field.clearValues();
         field.addValue(field.createFieldValue(value));
      }
   }
   
   /**
    * 
    * @param item
    * @param fieldname
    * @param values
    */
   private void addItemValue(PSClientItem item, String fieldname, Iterator values)
   {
      PSItemField field = item.getFieldByName(fieldname);
      if(field != null)
      {
         field.clearValues();
         while(values.hasNext())
         {
            String value = (String)values.next();
            if(value.trim().length() > 0)
               field.addValue(field.createFieldValue(value));
         }
      }
   }
   
   /**
    * Validates to be sure that the community, workflow, folder, and
    * contenttype all exist and are valid.
    * @param agent the remote agent to use, assumed not code>null</code>.
    * @param item the createItem object to be processed, assumed
    * not <code>null</code>.
    * @throws PSItemCreatorException upon any validation error.
    * @throws PSRemoteException upon remote agent errors
    */
   private void validate(
      PSRemoteAgent agent, PSItemCreatorConfig.CreateItem item)
      throws PSItemCreatorException, PSRemoteException
   {
      // Make sure that this is a valid community
      if(!entriesContainValue(agent.getCommunities(), item.getCommunity()))
         throw new PSItemCreatorException(
            "Community (" + item.getCommunity() + 
            ") is not valid for the specified Rhythmyx instance.");
      PSEntry commEntry = new PSEntry(item.getCommunity(), "");
      // Is the content type valid for this community
      if(!entriesContainValue(agent.getContentTypes(commEntry), item.getContenttype()))
         throw new PSItemCreatorException(
            "Contenttype (" + item.getContenttype() + 
            ") is not valid for the specified community (" +
            item.getCommunity() + ").");
      // Is the workflow valid for this community
      if(!entriesContainValue(agent.getWorkflows(commEntry), item.getWorkflow()))
         throw new PSItemCreatorException(
            "Workflow (" + item.getWorkflow() + 
            ") is not valid for the specified community (" +
            item.getCommunity() + ").");
      // Validate folder if specified
      if(item.getFolderid().trim().length() > 0)
      {
         PSRemoteFolderProcessor processor = getRemoteFolderProcessor(item);
         try
         {
            processor.load(
               "PSFolder", new PSLocator[] {new PSLocator(item.getFolderid())});
         }
         catch (PSCmsException e)
         {
           throw new PSItemCreatorException(
              "Folderid (" + item.getFolderid() + ") is not a valid folder id.");
         }
         
      }
     
   }
   
   /**
    * Looks through a list of <code>PSEntry</code> objects for
    * the specified value.
    * @param entries the list of entries, assumed not <code>null</code>.
    * @param value the value to look for, assumed not <code>null</code>.
    * @return true if the value was found in one of the entries
    */
   private boolean entriesContainValue(List entries, String value)
   {
      Iterator it = entries.iterator();
      while(it.hasNext())
      {
         PSEntry entry = (PSEntry)it.next();
         if(entry.getValue().equals(value))
            return true;
         
      }
      return false;
   }
   
   /**
    * Loads the configuration file from the specified filepath
    * @param filepath the filepath of the configuration file,
    * cannot be null or empty.
    * 
    */
   private void loadConfig(String filepath)
      throws PSInvalidItemCreatorConfigException
   {
      m_config = new PSItemCreatorConfig(filepath);
   }
   
   /**
    * Returns a remote agent that uses the connection info from
    * the createItem passed in.
    * @param createitem the CreateItem object, cannot be not
    * <code>null</code>.
    * @return an instance of <code>PSRemoteAgent</code>, never
    * <code>null</code>.
    */
   private PSRemoteAgent getRemoteAgent(
      PSItemCreatorConfig.CreateItem createitem)
   {
      if(createitem == null)
         throw new IllegalArgumentException("CreateItem cannot be null.");
      PSRemoteRequester requester = 
         new PSRemoteRequester(
            createitem.getHost(),
            createitem.getPort(),
            createitem.getSslport()
            );
      requester.setCredentials(createitem.getUser(), createitem.getPassword());
      return new PSRemoteAgent(requester);
   }
   
   /**
    * Returns a remote folder processor that uses the connection info from
    * the createItem passed in.
    * @param createitem the CreateItem object, cannot be not
    * <code>null</code>.
    * @return an instance of <code>PSRemoteFolderProcessor</code>, never
    * <code>null</code>.
    */
   private PSRemoteFolderProcessor getRemoteFolderProcessor(
      PSItemCreatorConfig.CreateItem createitem)
   {
      if(createitem == null)
         throw new IllegalArgumentException("CreateItem cannot be null.");
      PSRemoteRequester requester = 
         new PSRemoteRequester(
            createitem.getHost(),
            createitem.getPort(),
            createitem.getSslport()
            );
      requester.setCredentials(createitem.getUser(), createitem.getPassword());
      return new PSRemoteFolderProcessor(requester);
   }
   
   public static void main(String[] args) 
   {
    if(args.length == 1)
    {
       try
      {
         PSItemCreator creator = new PSItemCreator(args[0]);
         creator.execute();
         System.out.println("Completed successfully.");
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         System.out.println("\nFinished with errors!!");
      }      
    }
    else
    {
       // Usage message
       System.out.println("Usage:");
       System.out.println("java com.percussion.test.util.PSItemCreator" +
          "[Config File Path]");
    }
      
   }
   
   private PSItemCreatorConfig m_config;
   
}
