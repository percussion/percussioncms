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
package com.percussion.testing;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSWorkflowCommandHandler;
import com.percussion.cms.objectstore.IPSComponentProcessor;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariant;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariantSet;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.managednav.PSNavConfig;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.fastforward.managednav.PSNavFolderUtils;
import com.percussion.fastforward.managednav.PSNavProxyFactory;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSContentDataHandler;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class was created to provide functions that make it easy to create 
 * folder structures and duplicate content. Typically, this is useful when
 * stress testing or performance testing is being done. 
 * <p>It was initially written for SFP use. It could use further enhancement
 * for generic use.
 * <p>When used as an exit, it performs the following steps:
 * <ol>
 *    <li>Create a folder hierarchy starting at a specified location.</li>
 *    <li>Duplicate a specified set of items a specified number of times.</li>
 *    <li>Randomly distribute those items inside the created folder struct.</li>
 *    <li>Transition a specified % of them using the DirecttoPublic 
 *        transition.</li>
 *    <li>Optionally create a matching managed navigation structure, 
 *       transitioning all items created for navigation to public.</li> 
 * </ol>
 *
 * @author paulhoward
 */
public class PSContentHelper implements IPSResultDocumentProcessor
{

   //see base class method for details
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * See class description for overview of functionality.
    * 
    * @param params
    * <table>
    *    <tr>
    *       <th>Number</th>
    *       <th>Name</th>
    *       <th>Description</th>
    *    </tr>
    *    <tr>
    *       <td>0</td>
    *       <td>rootPath</td>
    *       <td>All folders will be created under this directory. Should be of
    * the form "//xxx/yyy/zzz" (e.g. //Sites/foo, //Folders/bar).</td>
    *    </tr>
    *    <tr>
    *       <td>1</td>
    *       <td>distributionFactor</td>
    *       <td>Geometric scaling factor that determines how folders per
    * level given the total and depth of the tree. Between 0 and 1. </td>
    *    </tr>
    *    <tr>
    *       <td>2</td>
    *       <td>totalFolders</td>
    *       <td>Total number of folders to create. Must be &gt;= maxDepth.</td>
    *    </tr>
    *    <tr>
    *       <td>3</td>
    *       <td>maxDepth</td>
    *       <td>How many levels in the tree. At least 1 path in the generated
    * tree will have a depth this large. No paths will have a depth greater 
    * than this. Must be &gt;= 1.</td>
    *    </tr>
    *    <tr>
    *       <td>4</td>
    *       <td>lowId</td>
    *       <td>If you want to create dupes, include this value. An attempt is
    * made to duplicate all items with content ids between lowId and highId,
    * inclusive.</td>
    *    </tr>
    *    <tr>
    *       <td>5</td>
    *       <td>highId</td>
    *       <td>If lowId is supplied, this is the id of the last item to dupe.
    * </td>
    *    </tr>
    *    <tr>
    *       <td>6</td>
    *       <td>dupes</td>
    *       <td>How many of each item to create in the set bounded by lowId 
    * and highId.</td>
    *    </tr>
    *    <tr>
    *       <td>7</td>
    *       <td>transitionPercent</td>
    *       <td>What percentage of the created items should be transitioned 
    * using the DirecttoPublic transition? An integer between 0 and 100, 
    * inclusive. Only used if a valid lowId is supplied.</td>
    *    </tr>
    *    <tr>
    *       <td>8</td>
    *       <td>createNavigation</td>
    *       <td>A value of "y" case-insensitive will cause a managed navigation
    * structure to be created.  A navtree will be created in the root path 
    * folder if a navtree or navon is not already there, with propagation turned 
    * off.  Navons will thus be created automatically for each folder created.  
    * In each folder created a landing page is also created and added to the 
    * landing page slot of the navon.</td>
    *    </tr>
    * </table>
    * 
    * <p>Example url: http://paul:9420/Rhythmyx/SFPTest/creator.html
    *    ?rootPath=//Sites/foo&distributionFactor=.5&totalFolders=1&maxDepth=1
    *    &lowId=794&highId=794&dupes=5&transitionPercent=80&createNavigation=y
    */
   public Document processResultDocument(Object[] params, 
         IPSRequestContext request, Document resultDoc) 
      throws PSParameterMismatchException, 
         PSExtensionProcessingException
   {
      if (params.length < 4)
         throw new PSParameterMismatchException(4, params.length);
      String path = params[0].toString();
      if (path.trim().length() == 0)
      {
         throw new PSParameterMismatchException(
               "Must provide the root of the hierarchy.");
      }
      float distFactor = -1f; 
      try
      {
         distFactor = Float.parseFloat(params[1].toString()); 
      }
      catch (RuntimeException e1)
      { /* ignore */ }
      if (distFactor < 0f || distFactor > 1.0f)
      {
         throw new PSParameterMismatchException(
               "Invalid value for distributionFactor (" + params[1] 
               + "). Must be between 0 (exclusive) and 1 (inclusive).");
      }
         
      int maxDepth = -1; 
      try
      {
         maxDepth = Integer.parseInt(params[3].toString()); 
      }
      catch (RuntimeException e1)
      { /* ignore */ }
      if (maxDepth < 1)
      {
         throw new PSParameterMismatchException(
               "Invalid value for maxDepth (" + params[3] 
               + "). Must be >= 1.");
      }
         
      int count = -1; 
      try
      {
         count = Integer.parseInt(params[2].toString()); 
      }
      catch (RuntimeException e1)
      { /* ignore */ }
      if (count < maxDepth)
      {
         throw new PSParameterMismatchException(
               "Invalid value for totalFolders (" + params[2] 
               + "). Must be >= maxDepth.");
      }

      int lowId = -1;
      int highId = -1;
      int dupes = -1;
      int transitionPercent = 0;
      boolean createNavigation = false;
      if (params.length > 4 && params[4] != null 
            && params[4].toString().trim().length() > 0)
      {
         try
         {
            lowId = Integer.parseInt(params[4].toString()); 
         }
         catch (RuntimeException e1)
         { /* ignore */ }
         if (lowId < 1)
         {
            throw new PSParameterMismatchException(
                  "Invalid value for lowId (" + params[4] 
                  + "). Must be an int &gt; 0.");
         }
         
         try
         {
            highId = Integer.parseInt(params[5].toString()); 
         }
         catch (RuntimeException e1)
         { /* ignore */ }
         if (highId < lowId)
         {
            throw new PSParameterMismatchException(
                  "Invalid value for highId (" + params[5] 
                  + "). Must be an int &gt;= lowId.");
         }
         
         try
         {
            dupes = Integer.parseInt(params[6].toString()); 
         }
         catch (RuntimeException e1)
         { /* ignore */ }
         if (dupes < 1)
         {
            throw new PSParameterMismatchException(
                  "Invalid value for dupes (" + params[6] 
                  + "). Must be an int &gt; 0.");
         }
         
         try
         {
            transitionPercent = Integer.parseInt(params[7].toString()); 
         }
         catch (RuntimeException e1)
         { /* ignore */ }
         if (transitionPercent < 0 || transitionPercent > 100)
         {
            throw new PSParameterMismatchException(
                  "Invalid value for transitionPercent (" + params[7] 
                  + "). Must be an int between 0 and 100, inclusive.");
         }
         
      }

      if (params.length >= 9)
      {
         createNavigation = (params[8] != null && "y".equalsIgnoreCase(
            params[8].toString()));         
      }
         
      try
      {
         IPSRelationshipProcessor rproc = PSRelationshipProcessor.getInstance();
         IPSComponentProcessor cproc = new PSComponentProcessorProxy(
               PSProcessorProxy.PROCTYPE_SERVERLOCAL, request);
         List folders = 
            createFolderTree(count, distFactor, maxDepth, path, rproc, cproc, 
               request, createNavigation);
         System.out.println("Created " + folders.size() + " folders");

         Collection dupeIds = new ArrayList();
         if (lowId > 0)
         {
            PSLocator lowLoc = new PSLocator(lowId);
            PSLocator highLoc = new PSLocator(highId);
            dupeIds = dupeContent(lowLoc, highLoc, dupes);
            System.out.println("Created " + dupeIds.size() + " dupes");
         }
         
         /* The idea here is to map all the items to the folders, then make
          * a single call to actually add the children to the folder rather
          * than making a call for each folder/item pair. It should be much
          * more efficient.
          */
         List[] mappedItems = new List[folders.size()];
         // initialize it w/ a list for each entry to be used below
         for (int i = 0; i < mappedItems.length; i++)
         {
            mappedItems[i] = new ArrayList();
         }
         if (folders.size() > 0 && dupeIds.size() > 0)
         {
            //add items to folders randomly
            int folderCount = folders.size();
            SecureRandom rgen = new SecureRandom();
            Iterator items = dupeIds.iterator();
            while (items.hasNext())
            {
               PSLocator loc = (PSLocator) ((PSLocator) items.next()).clone();
               int index = rgen.nextInt(mappedItems.length);
               mappedItems[index].add(loc);
            }
            
            //Now  create the relationships
            System.out.println("Adding items to folders.");
            for (int i = 0; i < mappedItems.length; i++)
            {
               if (mappedItems[i].size() > 0)
               {
                  rproc.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
                        mappedItems[i], (PSLocator) folders.get(i));
               }
               if (i%10 == 0)
                  System.out.print(".");
            }
            System.out.println("");
            
            //now, transition a percentage of items to public
            System.out.println("Transitioning items");
            items = dupeIds.iterator();
            int i=0;
            while (items.hasNext())
            {
               PSLocator loc = (PSLocator) items.next();
               int random = rgen.nextInt(101);
               if (random > transitionPercent)
                  continue;
               performTransition(request, loc, DIRECT_TO_PUBLIC);
               if (i++%10 == 0)
                  System.out.print(".");
            }
            System.out.println("");
         }
         
         return resultDoc;
      }
      catch (Exception e)
      {
         throw new PSExtensionProcessingException("PSContentHelper", e);
      }
   }

   //see base class method for details
   public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
   {
   }
   
   public static void main(String[] args)
      throws Exception
   {
   }
   
   /**
    * Creates a tree of <code>count</code> folders under <code>rootPath</code>.
    * The minimum and maximum depth of this tree is guaranteed to be <code>
    * maxDepth</code>.  Folders are created using the definition found in a file
    * called "folderTemplate.xml" located in the root directory of the Rhythmyx
    * server.
    * <p>The folder generation algorithm is as follows:
    * Subtract <code>maxDepth</code> from <code>count</code>. Divide 
    * this value by maxDepth to get the number of first level folders. For 
    * each additional level, take what is left and multiply by <code>
    * distribution</code> to get the # of folders for each level. Passing a 
    * value of .5 for the distribution will use up the folders in about 6 
    * levels. Passing .33 uses them up in about 8 levels. The distribution value 
    * must be 1 or less. If all folders haven't been used by the last
    * level, they will be added to the last level. At each level the folders
    * are distributed randomly among the parents.
    * <p>It is guaranteed that there will be at least 1 path that is <code>
    * maxDepth</code> deep.
    *   
    * @param count Total number of folders to create. Must be greater than or
    * equal to <code>maxDepth</code>.
    * 
    * @param distribution A factor used to control what the tree will look 
    * like. Should be &gt; 0 and &lt;=1. 
    * 
    * @param maxDepth How 'deep' should the tree be. Must be &gt;= 1.
    * 
    * @param rootPath The full path of the parent to receive the folders. Of
    * the form //Sites/foo/bar. This folder must exist.
    * 
    * @param rproc Used to create the folder relationships
    * 
    * @param cproc Used to create the folder on the server
    * 
    * @param req The request context, used to locate and create navigation
    * items.
    * 
    * @param createNavigation <code>true</code> to create a navtree in the root
    * folder if a navtree or navon is not present, <code>false</code> otherwise.
    * If <code>true</code>, then a file named navtestprops.properties is 
    * loaded from the rxroot directory. This file contains meta data for
    * creating the nav structure.
    * 
    * @return A new list with 0 or more <code>PSLocator</code>s added, one
    * for every folder created by this method. Never <code>null</code>.
    *  
    * @throws SAXException if there is an error loading the folder template xml
    * file.
    * @throws IOException if the folder template xml file cannot be opened.
    * @throws PSCmsException If there is an error creating a landing page.
    * @throws PSUnknownNodeTypeException If there is an error loading a folder
    * component summary.
    * @throws PSNavException If there is an error locating navons.
    * @throws PSInvalidContentTypeException if the landing page content type
    * cannot be loaded.
    */
   public static List createFolderTree(int count, float distribution, 
         int maxDepth, String rootPath, IPSRelationshipProcessor rproc, 
         IPSComponentProcessor cproc, IPSRequestContext req, 
         boolean createNavigation)
      throws SAXException, IOException, PSCmsException, 
         PSUnknownNodeTypeException, PSInvalidContentTypeException, 
         PSNavException
   {
      // create the new folder object      
//      InputStream in = PSContentHelper.class.getResourceAsStream(
//            "folderTemplate.xml");
      InputStream in = new FileInputStream("folderTemplate.xml");
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      Element folderEl = doc.getDocumentElement();
      PSFolder folder = new PSFolder(folderEl);

      Properties navprops = null;
      
      PSComponentSummary parentSummary =
         rproc.getSummaryByPath(
            PSDbComponent.getComponentType(PSFolder.class),
            rootPath,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      if (null == parentSummary)
         throw new RuntimeException("Can't find folder named: " + rootPath);
      PSLocator[] parentIds = new PSLocator[1];
      PSLocator parentLocator = parentSummary.getCurrentLocator();
      parentIds[0] = parentLocator;
      PSLocator navtreeloc = null;
      if (createNavigation)
      {
         navprops = new Properties();
         in = new FileInputStream("navtestprops.properties");
         navprops.load(in);
         try {in.close();} catch (IOException e) {}
         navtreeloc = ensureNavigation(req, rproc, parentSummary, navprops);
      }
      int[] levelCounts = getFolderDistribution(count, distribution, maxDepth);
      List addedFolders = new ArrayList();
      createFolderLevel(0, levelCounts, parentIds, rproc, cproc, folder,
            addedFolders);
      if (createNavigation)
      {
         if (navtreeloc != null)
            addedFolders.add(parentSummary.getCurrentLocator());
         createLandingPages(req, rproc, cproc, addedFolders, navprops);
      }
      
      return addedFolders;
   }
   
   /**
    * Checks for a navon in each folder in the supplied 
    * <code>folderList</code>, and if found, creates a landing page in the
    * folder and relates it to the navon.
    *  
    * @param req The current request to use to create landing pages, assumed not 
    * <code>null</code>.
    * @param rproc The processor to use to add folder content, assumed not 
    * <code>null</code>.
    * @param cproc The processor to use to load folder component summaries, 
    * assumed not <code>null</code>.
    * @param folderList The list of folders to which landing pages are to be
    * added, assumed not <code>null</code>, may be empty.
    * @param navprops The properties to use to create landing pages, assumed not 
    * <code>null</code>.
    * @throws PSNavException if there are any errors. 
    */
   private static void createLandingPages(IPSRequestContext req, 
      IPSRelationshipProcessor rproc, IPSComponentProcessor cproc, 
      List folderList, Properties navprops) throws PSNavException
   {
      try
      {
         PSKey[] locators = (PSKey[])folderList.toArray(
            new PSKey[folderList.size()]);
         Element[] sumEls = cproc.load(PSDbComponent.getComponentType(
            PSComponentSummaries.class), locators);
         PSItemDefManager defMgr = PSItemDefManager.getInstance();
         PSSecurityToken tok = req.getSecurityToken();

         String cType = navprops.getProperty("ctype");
         PSItemDefinition itemDef = defMgr.getItemDef(cType, tok);

         // get slot and variant info
         PSNavConfig config = PSNavConfig.getInstance(req);
         PSRelationshipConfig aaConfig = config.getAaRelConfig();
         PSSlotType landingSlot = config.getAllSlots().getSlotTypeByName(
            config.getPropertyString(PSNavConfig.NAVON_LANDING_SLOT));
         PSSlotTypeContentTypeVariantSet cvs = landingSlot.getSlotVariants();
         int variantId = -1;
         Iterator it = cvs.iterator();
         while (it.hasNext())
         {
            PSSlotTypeContentTypeVariant xtv = 
               (PSSlotTypeContentTypeVariant) it.next();
            if (xtv.getContentTypeId() == itemDef.getTypeId())
            {
               variantId = xtv.getVariantId();
               break;
            }
         }
         if (variantId == -1)
         {
            System.out.println("Slot " + landingSlot.getSlotName() + 
               " has no variants ");
            return;
         }

         //Load all variants definitions and pick the one we need
         Element[] variantElems = cproc.load(
            PSDbComponent.getComponentType(PSContentTypeVariantSet.class), 
            new PSKey[0]);
         PSContentTypeVariantSet variants = 
            new PSContentTypeVariantSet(variantElems);
         PSContentTypeVariant slotVariant = variants.getContentVariantById(
            variantId);
         PSContentTypeVariant defaultVariant = 
            variants.getContentVariantByName(navprops.getProperty(
               "landing.defaultVariant"));
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
         
         // walk the folders 
         for (int i = 0; i < sumEls.length; i++)
         {
            // check for a navon in the folder
            PSComponentSummary sum = new PSComponentSummary(sumEls[i]);
            PSComponentSummary navon = PSNavFolderUtils.getChildNavonSummary(
               req, sum);
            if (navon == null)
               continue;
            
            // we found a navon, create an item, relate it to the navon, then
            // transition both to public
            
            // create new item with default values
            PSServerItem item = new PSServerItem(itemDef, null, tok);
            IPSFieldValue titleValue = new PSTextValue(navon.getName() + 
               "LandingPage");
            setFieldValue(item, "sys_title", titleValue);         
            
            String titleField = navprops.getProperty("ctype.displayTitle");
            setFieldValue(item, titleField, titleValue);
            
            String bodyField = navprops.getProperty("ctype.body");
            setFieldValue(item, bodyField, new PSTextValue(navon.getName() + 
               " body"));
            if (defaultVariant != null)
            {
               String variantField = navprops.getProperty("ctype.defaultVariant");
               setFieldValue(item, variantField, new PSTextValue(
                  String.valueOf(defaultVariant.getVariantId())));
            }

            item.save(tok);
            
            // add it to the folder
            List children = new ArrayList(1);
            children.add(item.getKey());
            rproc.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, children, 
               sum.getCurrentLocator());
            
            // relate it to the navon
            PSLocator childLoc = (PSLocator) item.getKey().clone();
            PSAaRelationship aaRel = new PSAaRelationship(
               navon.getCurrentLocator(), childLoc,
               landingSlot, slotVariant, aaConfig);
            
            PSAaRelationshipList aaList = new PSAaRelationshipList();
            aaList.add(aaRel);
            PSNavProxyFactory navProxy = PSNavProxyFactory.getInstance(req);
            navProxy.getAaProxy().addSlotRelationships(aaList, -1);
                        
            // now transition both to public
            performTransition(req, navon.getCurrentLocator(), DIRECT_TO_PUBLIC);
            performTransition(req, item.getKey(), DIRECT_TO_PUBLIC);            
         }
         
         System.out.println("Created " + sumEls.length + " landing page(s)");
      }
      catch (PSException e)
      {
         throw new PSNavException(e);
      }
   }

   /**
    * Checks for a nav tree in the specified folder, and if not found, creates
    * one with propagate set to <code>false</code>, then transitions it to the
    * Public state. 
    * 
    * @param req The request context, used to locate and create navigation
    * items.
    * @param rproc The processor to use to add folder content, assumed not 
    * <code>null</code>.
    * @param folderSum The summary of the folder to check, assumed not 
    * <code>null</code>.
    * @param navprops Specifies user defined values to use in navigation 
    * creation, assumed not <code>null</code>.
    * 
    * @return The locator of the navtree if one is created, otherwise
    * <code>null</code>.
    * 
    * @throws PSNavException if there are any errors.
    */
   private static PSLocator ensureNavigation(IPSRequestContext req, 
      IPSRelationshipProcessor rproc, PSComponentSummary folderSum, 
      Properties navprops) throws PSNavException
   {
      PSLocator result = null;
      // check for a navtree or navon in the folder
      PSComponentSummary navSum = PSNavFolderUtils.getChildNavonSummary(req, 
         folderSum);
      if (navSum == null)
      {
         // need to create a navtree
         System.out.println("adding new navtree to folder " + 
            folderSum.getName());         
         PSItemDefManager defMgr = PSItemDefManager.getInstance();
         PSNavConfig config = PSNavConfig.getInstance(req);
         try
         {
            // create the navtree
            int communityId = req.getSecurityToken().getCommunityId(); 
            PSItemDefinition navonDef = defMgr.getItemDef(
               config.getNavTreeType(), communityId);
            if (navonDef == null)
            {
               String errmsg = "Unable to find Itemdef for type {0} in " +
                  "community {1}. ";
               Object[] args = new Object[2];
               args[0] = config.getNavonType();
               args[1] = communityId;
               String sb = MessageFormat.format(errmsg, args);
               throw new PSNavException(sb);
            }

            PSServerItem navon = new PSServerItem(navonDef, null, 
               req.getSecurityToken()); // load default values

            IPSFieldValue titleValue = new PSTextValue(folderSum.getName());
            setFieldValue(navon, "sys_title", titleValue);
            
            String dispTitleField = config.getPropertyString(
               PSNavConfig.NAVON_TITLE_FIELD);
            setFieldValue(navon, dispTitleField, titleValue);
            
            String propField = config.getPropertyString(
               PSNavConfig.NAVON_PROP_FIELD);
            setFieldValue(navon, propField, new PSTextValue(null));
            
            String themeField = config.getPropertyString(
               PSNavConfig.NAVTREE_THEME_FIELD);
            setFieldValue(navon, themeField, new PSTextValue(
               navprops.getProperty("nav.theme")));

            String varField = config.getPropertyString(
               PSNavConfig.NAVON_VARIABLE_FIELD);
            setFieldValue(navon, varField, new PSTextValue(
               navprops.getProperty("nav.variable")));

            navon.save(req.getSecurityToken());
            
            // add it to the folder
            List children = new ArrayList(1);
            children.add(navon.getKey());
            rproc.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, children, 
               folderSum.getCurrentLocator());
            
            result = navon.getKey();
         }
         catch (PSException e)
         {
            throw new PSNavException(e);
         }         
      }
      
      return result;
   }

   /**
    * Helper method to set a field value for a content item. Nothing happens if
    * the specified field by name does not exist in the item.
    * 
    * @param item server item object, assumed not <code>null</code>.
    * @param fieldName name of the field to set, assumed not <code>null</code>
    *           or empty.
    * @param fieldValue value of the field to set, may be <code>null</code> or
    *           empty.
    */
   private static void setFieldValue(PSServerItem item, String fieldName,
         IPSFieldValue fieldValue)
   {
      PSItemField field = item.getFieldByName(fieldName);
      if (field == null)
      {
         System.out.println("Field " + fieldName + " not found ");
         return;
      }
      field.clearValues();
      field.addValue(fieldValue);
   }
   /**
    * Method used to recursively create some number of folders as children of
    * the supplied <code>parentFolderId</code>.
    * 
    * @param curDepth Used as index into the <code>levelCounts</code> param.
    * The value found at this index determines how many folders to create.
    * 
    * @param levelCounts How many folders to create at each depth of the 
    * folder tree. The size of this value is equal to the maximum depth of 
    * the tree. Assumed never <code>null</code> and 
    * <code>levelCounts</code>[<code>curDepth</code>] is valid. 
    * 
    * @param parentFolderIds The ids of the folders to which the created folders
    * will be added as children. Assumed not <code>null</code>. Which parents
    * the created folders are added to are determined by calling {@link 
    * #randomizeFolderChildren(int, int) randomizeFolderChildren}.
    * 
    * @param rproc Used to perform all relationship operations. Assumed not
    * <code>null</code>.
    * 
    * @param cproc Used to perform all component operations. Assumed not
    * <code>null</code>.
    * 
    * @param folder Used as the template for creating all folders. All 
    * properties except the name will be 'inherited' by all created folders.
    * The name will take the form "Lx_foldery", where x is the depth (1 based)
    * and y is the folder number w/in a group of folders (1 based).
    * 
    * @param addedFolders Every folder added by this method is added to the
    * supplied collection as a <code>PSLocator</code>. Assumed not <code>
    * null</code>.
    */
   private static void createFolderLevel(int curDepth, int[] levelCounts,
         PSLocator parentFolderIds[], 
         IPSRelationshipProcessor rproc, 
         IPSComponentProcessor cproc,
         PSFolder folder,
         Collection addedFolders)
      throws PSCmsException
   {
      int[] folderDist;
      if (curDepth == 0)
      {
         folderDist = new int[1];
         folderDist[0] = levelCounts[curDepth];
      }
      else
      {
         folderDist = randomizeFolderChildren(levelCounts[curDepth-1],
               levelCounts[curDepth]);
      }

      PSLocator[] parentIds = null;
      int k = 0;
      parentIds = new PSLocator[levelCounts[curDepth]];
      for (int i = 0; i < parentFolderIds.length; i++)
      {
         for (int j=1; j <= folderDist[i]; j++)
         {
            String folderName = "L" + (curDepth+1) + "-" 
                  + parentFolderIds[i].getId()+ "_folder" + j;
            folder.setName(folderName);
            PSLocator id = createFolder(parentFolderIds[i], folder, rproc, 
                  cproc); 
            parentIds[k++] = id;
            addedFolders.add(id);
         }
      }
      int nextDepth = curDepth+1;
      if (nextDepth < levelCounts.length)
      {
         createFolderLevel(nextDepth, levelCounts, parentIds,  
               rproc, cproc, folder, addedFolders);
      }
   }

   /**
    * Randomly distributes the children among the parents and returns the # of
    * children for each parent.
    * 
    * @param parentCount How many parents to receive children. Assumed &gt;= 1.
    * 
    * @param childCount How many children to distribute. Assumed &gt;= 1.
    * 
    * @return An array whose size is equal to <code>parentCount</code>. The sum
    * of all values in this array is equal to <code>childCount</code>. Each 
    * value ranges from 0 to <code>childCount</code>.
    */
   private static int[] randomizeFolderChildren(int parentCount, int childCount)
   {
      int[] results = new int[parentCount];
      SecureRandom rgen = new SecureRandom();
      for (int i=0; i < childCount; i++)
      {
         int val = rgen.nextInt(parentCount);
         results[val]++;
      }
      return results;
   }

   /**
    * Calculates how many folders should be present at each level of a 
    * hierarchy given the provided constraints. See {@link #createFolderTree(
    * int, float, int, String, IPSRelationshipProcessor, IPSComponentProcessor)
    * createFolderTree} for a description of the algorithm.
    * 
    * @param count Total # of folders to exist in hierarchy. Assumed 
    * &gt;= <code>maxDepth</code>.
    * 
    * @param distribution A geometric weighting factor that determines how the 
    * files are distributed between the levels. Assumed &lt;= 1.
    * 
    * @param maxDepth The number of levels in the hierarchy at the deepest
    * point. Assumed &gt;= 1.
    * 
    * @return An array of size <code>maxDepth</code>, where each entry in the
    * array indicates how many folders to create at that level of the 
    * hierarchy. Never <code>null</code> or length &lt; 1 if assumptions are
    * met.
    */
   private static int[] getFolderDistribution(int count, float distribution, 
         int maxDepth)
   {
      int[] folderDist = new int[maxDepth];
      /* Every level is guaranteed to have at least 1 folder, so we don't
       * include those in foldersLeft, and we add it in w/ the "+1" 2 places
       * below.
       */ 
      folderDist[0] = (count - maxDepth ) / maxDepth + 1;
      int foldersLeft = count - (folderDist[0] + (maxDepth-1));
      for (int i=1; i < maxDepth; i++)
      {
         folderDist[i] = (int) (foldersLeft * distribution) + 1;
         foldersLeft -= folderDist[i];
         if (foldersLeft < 0)
            foldersLeft = 0;
      }
      //Add remaining folders to last level (they could be randomly distributed)
      if (foldersLeft > 0)
         folderDist[maxDepth-1] += foldersLeft;
      return folderDist;
   }

   /**
    * Duplicates all items whose content id is between <code>lowId</code> and
    * <code>highId</code>, inclusive. A dupe is made of the tip revision.
    * The clones are made by asking the server to clone each item.
    * 
    * @param lowId The identifier of the item w/ the lowest content id.
    * 
    * @param highId The identifier of the item w/ the highest content id.
    * 
    * @param count How many dupes of each one to make.
    * 
    * @return A set of <code>PSLocator</code>s for every clone created by
    * this method. Never <code>null</code> or empty.
    */
   public static Collection dupeContent(PSLocator lowId, PSLocator highId, 
         int count)
      throws PSException
   {
      int low = lowId.getId();
      int high = highId.getId();
      PSContentDataHandler dh = new PSContentDataHandler();
      PSRequest req = PSRequest.getContextForRequest();
      
      Collection results = new ArrayList();
      for (int i=low; i <= high; i++)
      {
         for (int j = 0; j < count; j++)
         {
            /* These 2 statements need to be inside the loop because the 
             * contentid and revision get reset by the query. This causes a
             * name cascade like follows:
             *    Copy (1) of Itemtitle
             *    Copy (1) of Copy (1) of ItemTitle
             * and so on. For any number of significant dupes, the title
             * would quickly outgrow the available storage. By reseting in
             * the loop, we get names of the form:
             *    Copy (1) of Itemtitle
             *    Copy (2) of Itemtitle
             *    Copy (3) of Itemtitle
             */
            req.setParameter(IPSHtmlParameters.SYS_CONTENTID, 
                  Integer.toString(i));
            req.setParameter(IPSHtmlParameters.SYS_REVISION, 
                  Integer.toString(1));
            dh.newCopy(req);
            results.add(new PSLocator(
                  req.getParameter(IPSHtmlParameters.SYS_CONTENTID).toString(),
                  req.getParameter(IPSHtmlParameters.SYS_REVISION).toString()));
         }
      }
      return results;  
   }
   
   /**
    * Transitions the item identified by the supplied locator using the 
    * specified transition name.
    * 
    * @param request The request context to use, assumed not <code>null</code>.
    * @param loc The locator of the item to transition, assumed not 
    * <code>null</code>.
    * @param wfAction The workflow action to perform, which is the transition
    * name, assumed not <code>null</code> or empty.
    *  
    * @throws PSException if there are any errors.
    */
   private static void performTransition(IPSRequestContext request, 
      PSLocator loc, String wfAction) throws PSException
   {
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      Map extraParams = new HashMap();
      extraParams.put(IPSHtmlParameters.SYS_COMMAND, 
            PSWorkflowCommandHandler.COMMAND_NAME);
      extraParams.put("WFAction", wfAction);

      extraParams.put(IPSHtmlParameters.SYS_CONTENTID, 
            Integer.toString(loc.getId()));
      extraParams.put(IPSHtmlParameters.SYS_REVISION, 
            Integer.toString(loc.getRevision()));
      String url = 
            defMgr.getTypeEditorUrl(defMgr.getItemContentType(loc));
      IPSInternalRequest ireq = 
            request.getInternalRequest(url, extraParams, false);
      ireq.performUpdate();
      
   }
   
   /**
    * Saves the supplied folder to the system and adds the appropriate link
    * to the parent folder..
    * 
    * @param parentLocator The locator of the parent folder, assume not 
    *    <code>null</code>.
    * 
    * @param folder The def of the folder to create.
    * 
    * @param rproc Used to perform all relationship operations. Assumed not
    * <code>null</code>.
    * 
    * @param cproc Used to perform all component operations. Assumed not
    * <code>null</code>.
    * 
    * @return The locator of the created folder, never <code>null</code>.
    * 
    * @throws Exception if error occurs.
    */
   private static PSLocator createFolder(
      PSLocator parentLocator,
      PSFolder folder,
      IPSRelationshipProcessor rproc, 
      IPSComponentProcessor cproc)
      throws PSCmsException
   {
      PSComponentSummary[] summaries;
   
      PSSaveResults results =
            cproc.save(new IPSDbComponent[] {folder});
      folder = (PSFolder) results.getResults()[0];
      PSLocator locator = (PSLocator) folder.getLocator();
   
      List locatorList = new ArrayList();
      locatorList.add(locator);
      rproc.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
         locatorList, 
         parentLocator);
      
      return locator;
   }

   /**
    * Name of transition from Draft to Public 
    */
   private static final String DIRECT_TO_PUBLIC = "DirecttoPublic";
}
