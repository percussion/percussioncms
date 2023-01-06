/******************************************************************************
 *
 * [ PSItemRelationshipsManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.cx;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipInfo;
import com.percussion.cms.objectstore.PSRelationshipInfoSet;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.client.PSRelationshipProcessor;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The manager class to use to get ancestors or descendants of an item for
 * a relationship.
 */
public class PSItemRelationshipsManager
{
   /**
    * Constructs the manager with supplied parameters.
    *
    * @param proxy the remote proxy to use to execute a relationship request to
    *    the server, may not be <code>null</code>.
    * @param folderMgr Never <code>null</code>.
    * @param remCataloger the remote cataloger to use to get list of
    *    available relationships, may not be <code>null</code>
    * @param docBase applet base Url to make any server requests, may not
    *    be <code>null</code>
    */
   public PSItemRelationshipsManager(PSRelationshipProcessorProxy proxy,
         PSFolderActionManager folderMgr, PSRemoteCataloger remCataloger, 
         URL docBase, PSContentExplorerApplet applet)
   {
      if (proxy == null)
         throw new IllegalArgumentException("proxy may not be null.");
      
      if (folderMgr == null)
         throw new IllegalArgumentException("folderMgr may not be null.");
      
      if (remCataloger == null)
         throw new IllegalArgumentException("remCataloger may not be null.");

      if (docBase == null)
         throw new IllegalArgumentException("docBase may not be null.");
      
      m_proxy = proxy;
      m_folderActionMgr = folderMgr;
      m_remCataloger = remCataloger;
      m_docBase = docBase;
      m_applet = applet;
   }

   /**
    * Gets the set of available relationships by making a catalog request.
    *
    * @return the relationship set, never <code>null</code>
    *
    * @throws PSCmsException if an error happens cataloging relationships.
    */
   public PSRelationshipInfoSet getRelationships() throws PSCmsException
   {
      return m_remCataloger.getRelationshipInfoSet();
   }

   /**
    * Loads the parent/child dependencies of the object(item/folder) identified
    * by the locator of the node for a relationship. Assumes all these values
    * are specified as properties of the node. The following list describes
    * all required properties. Ignores
    * <ol>
    * <li>PSContentExplorerApplet.PROP_CONTENTID</li>
    * <li>PSContentExplorerApplet.PROP_REVISIONID</li>
    * <li>PSContentExplorerApplet.PROP_RELATIONSHIP</li>
    * <li>PSContentExplorerApplet.PROP_RS_LOOKUP_TYPE</li>
    * <li>Value for above property - ('ancestors'/'descendants')</li>
    * </ol>
    *
    * @param parentNode the node for which the dependencies need to be loaded,
    * may not be <code>null</code> and must have above mentioned properties.
    *
    * @return the list of <code>PSNode</code> dependencies, never <code>null
    * </code>, may be empty.
    * @throws IllegalArgumentException if parentNode is invalid.
    *
    * @throws PSCmsException if an error happens loading dependencies
    * @throws PSContentExplorerException if an error happens loading details of
    * dependencies
    */
   public Iterator loadDependencies(PSNode parentNode)
      throws PSCmsException, PSContentExplorerException
   {
      if (parentNode == null)
         throw new IllegalArgumentException("parentNode may not be null.");

      PSLocator key = PSActionManager.nodeToLocator(parentNode);
      PSProperties props = parentNode.getProperties();
      String relationship = props.getProperty(IPSConstants.PROPERTY_RELATIONSHIP);
      PSRelationshipInfo relInfo = PSContentExplorerApplet.getRelMap().get(relationship);

      if(relationship == null || relationship.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentNode not specified with a relationship");

      PSComponentSummaries summaries = null;

      String rsType = props.getProperty(PROP_RS_LOOKUP_TYPE);
      if(rsType == null || rsType.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentNode  not specified with a relationship lookup type");

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setName(relationship);
      filter.setCommunityFiltering(false);
      if (rsType.equals(RS_LOOKUP_DESCENDANTS))
      {
         if (relInfo.getCategory().equals(PSRelationshipConfig.CATEGORY_FOLDER))
         {
            summaries = new PSComponentSummaries();
            PSFolderProcessorProxy proxy = m_folderActionMgr.getFolderProxy();
            PSComponentSummary[] arrySummaries = proxy.getChildSummaries(key);
            for (PSComponentSummary summary : arrySummaries)
               summaries.add(summary);
         }
         else if (relInfo.getCategory()
               .equalsIgnoreCase(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
         {
            filter.setOwner(key);
            //Filtering only in case of AA dependents See BugId: RX-16352 
            summaries = getFilteredRelationshipSummaries(filter, parentNode
                  .getContentId());
         }
         else
         {
            filter.setOwner(key);
            summaries = m_proxy.getSummaries(filter, false);
         }
      }
      else
      {
         filter.setDependent(key);
         if (relInfo.getCategory().equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
         {
            PSComponentSummaries testSummaries = m_proxy.getSummaries(filter,
                  true);

            // filter dependents for current revision
            summaries = new PSComponentSummaries();
            Iterator walker = testSummaries.iterator();
            while (walker.hasNext())
            {
               PSComponentSummary summary = (PSComponentSummary) walker.next();
               filter.setOwner(summary.getCurrentLocator());
               if (!m_proxy.getSummaries(filter, true).isEmpty())
                  summaries.add(summary);
            }
         }
         else if (relInfo.getCategory().equals(PSRelationshipConfig.CATEGORY_FOLDER))
         {
            summaries = new PSComponentSummaries();
            PSFolderProcessorProxy proxy = m_folderActionMgr.getFolderProxy();
            PSComponentSummary[] arrySummaries = proxy.getParentSummaries(key);
            for (PSComponentSummary summary : arrySummaries)
               summaries.add(summary);
         }
         else
         {
            summaries = m_proxy.getSummaries(filter, true);
         }
      }
     
      List resList = new ArrayList();
      List idList = new ArrayList();
      //Set the 'relationship', 'rs_type' and the locator as properties of each
      //node
      Iterator it = summaries.iterator();
      while(it.hasNext())
      {
         PSComponentSummary dependent = (PSComponentSummary) it.next();

         PSLocator locator = dependent.getCurrentLocator();
         int id = locator.getId();
         if (dependent.isFolder())
         {
            //If we touched root, don't load the root as child.
            if (id == PSFolder.ROOT_ID)
               break;
            
            String type = 
                  m_folderActionMgr.getFolderType(dependent, parentNode);
            String iconKey = PSFolderActionManager.getFolderIconKey(type);

            PSNode node = new PSNode(dependent.getName(), dependent.getName(),
               type, "", iconKey, false,
               dependent.getPermissions().getPermissions());
            props = new PSProperties();
            props.setProperty(IPSConstants.PROPERTY_CONTENTID,
               String.valueOf(id));
            props.setProperty(IPSConstants.PROPERTY_REVISION,
               String.valueOf(locator.getRevision()));
            props.setProperty(
               IPSConstants.PROPERTY_RELATIONSHIP, relationship);
            
            props.setProperty(PROP_RS_LOOKUP_TYPE, rsType);
            node.setProperties(props);
            resList.add(node);
         }
         else
         {
            idList.add(new Integer(dependent.getCurrentLocator().getId()));
         }
      }
      
      if (!idList.isEmpty())
      {
         PSDisplayFormat format = m_applet.getActionManager().getDisplayFormatCatalog().
            getDisplayFormatById("0"); //get the default display format

         PSExecutableSearch searchEx = new PSExecutableSearch(m_docBase,
            format, idList, m_applet);
         List list = searchEx.executeSearch(parentNode);
         PSNode node = null;
         Map rowData = null;
         Iterator values = null;
         String label = null;
         for(int i=0; i<list.size(); i++)
         {
            node = (PSNode)list.get(i);
            node.setType(PSNode.TYPE_DTITEM);
            node.setProperty(
               IPSConstants.PROPERTY_RELATIONSHIP, relationship);
         
            node.setProperty(PROP_RS_LOOKUP_TYPE, rsType);
            label = node.getLabel();
            rowData = node.getRowData();
            if (rowData != null)
            {
               values = rowData.values().iterator();
               int ii=0;
               while(values.hasNext())
               {
                  if(ii==0)
                     label += " (";
                  else if(ii>0)
                     label +=  " - ";
                  label += values.next().toString();
                  ii++;
               }
               if(ii > 0)
                  label += ")";
               node.setLabel(label);
            }
         }
         resList.addAll(list);
      }

      parentNode.setChildren(resList.iterator());
      return resList.iterator();
   }
   
   /**
    * Filters the relationships based on whether the slots are still registered
    * to the content types through their allowed variants. Gets the related
    * content document and gets the sys_slotid property for each relationship,
    * and checks whether that slot id is a registered slot to the content type
    * of the item or not, if not filters that relationship if the associated
    * slot is not an inline slot.  Relationships to inline slots will never be
    * be filtered.
    * 
    * @param filter must not be <code>null</code>.
    * @param contentId must not be <code>null</code>.
    * @return Filtered component summaries may be empty but never <code>null</code>.
    * @throws PSCmsException in case of error.
    */
   private PSComponentSummaries getFilteredRelationshipSummaries(
         PSRelationshipFilter filter, String contentId) throws PSCmsException
   {
      if (filter == null)
         throw new IllegalArgumentException(
               "Relationship filter cannot be null.");
      if(StringUtils.isBlank(contentId))
         throw new IllegalArgumentException("contentId cannot be null.");
      PSComponentSummaries summaries = new PSComponentSummaries();
      List<String> regSlots = getRegisteredSlots(contentId);
      Document relDoc = getRelationshipDoc(filter);
      List<String> inlineSlots = getInlineSlots();
      NodeList nl = relDoc.getElementsByTagName("PSXRelationship");
      List<String> contentIds = new ArrayList<String>();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Element elem = (Element) nl.item(i);
         String slotId = null;
         String cid = null;
         Element plelem = (Element) elem.getElementsByTagName("PropertyList")
               .item(0);
         NodeList props = plelem.getElementsByTagName("Property");
         for (int j = 0; j < props.getLength(); j++)
         {
            Element e = (Element) props.item(j);
            String name = e.getAttribute("name");
            if (name.equalsIgnoreCase("sys_slotid"))
            {
               slotId = e.getTextContent();
               break;
            }
         }
         Element depLoc = (Element) ((Element) elem.getElementsByTagName(
               "Dependent").item(0)).getElementsByTagName("PSXLocator")
               .item(0);
         try
         {
            PSLocator loc = new PSLocator(depLoc);
            cid = "" + loc.getId();
         }
         catch (Exception e)
         {
            Object[] args = new Object[] { e.getLocalizedMessage() };
            throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR, args);
         }
         if (slotId != null && cid != null)
         {
            if (regSlots.contains(slotId) || inlineSlots.contains(slotId))
            {
               contentIds.add(cid);
            }
         }
      }
      PSComponentSummaries allsummaries = m_proxy.getSummaries(filter, false);
      Iterator siter = allsummaries.getSummaries();
      while (siter.hasNext())
      {
         PSComponentSummary sum = (PSComponentSummary) siter.next();
         String cid = "" + sum.getContentId();
         if (contentIds.contains(cid))
         {
            summaries.add(sum);
         }
      }
      return summaries;
   }

   /**
    * Makes a request to a resource and gets the slots registered to the all
    * allowable templates the content type of the supplied item.
    * 
    * @param contentId must not be <code>null</code>.
    * @return List of registered slots through templates to the supplied item.
    * @throws PSCmsException in case of error.
    */
   private List<String> getRegisteredSlots(String contentId)
      throws PSCmsException
   {
      if (StringUtils.isBlank(contentId))
         throw new IllegalArgumentException("contentId cannot be null.");
      Map<String, String> params = new HashMap<String, String>();
      params.put("sys_contentid", contentId);
      params.put("filter", "true");
      List<String> regSlots = new ArrayList<String>();
      try
      {
         Document doc = m_applet.getXMLDocument(
               "/sys_rcSupport/contentslotvariantlist.xml", params);
         NodeList nodes = doc.getElementsByTagName("slot");
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Element elem = (Element) nodes.item(i);
            String slotid = elem.getAttribute("slotid");
            if (slotid.trim().length() > 0)
               regSlots.add(slotid);
         }
      }
      catch (Exception e)
      {
         Object[] args = new Object[] { e.getLocalizedMessage() };
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR, args);
      }
      return regSlots;
   }
   
   /**
    * Makes a request to a resource and gets all inline slots.
    * 
    * @return List of inline slots.
    * @throws PSCmsException in case of error.
    */
   private List<String> getInlineSlots() throws PSCmsException
   {
      List<String> inlineSlots = new ArrayList<String>();
      
      Map<String, String> params = new HashMap<String, String>();
      params.put("slottype", "1");
      try
      {
         Document doc = m_applet.getXMLDocument(
               "/sys_Slots/slotlist.xml", params);
         NodeList nodes = doc.getElementsByTagName("slot");
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Element elem = (Element) nodes.item(i);
                
            Node slotId = PSXMLDomUtil.findFirstNamedChildNode(elem, 
               "slotid");
            if (slotId != null)
            {
               String data = PSXMLDomUtil.getElementData(slotId);
               if (StringUtils.isNotEmpty(data))
               {
                  inlineSlots.add(data);
               }
            }
         }
      }
      catch (Exception e)
      {
         Object[] args = new Object[] { e.getLocalizedMessage() };
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR, args);
      }
      
      return inlineSlots;
   }
   
   /**
    * Returns the document consisting of the relationships based on the supplied
    * filter. Assumes the filter is set with appropriate parameters.
    * 
    * @param filter must not be <code>null</code>
    * @return Document corresponding to PSXRelationshipSet dtd.
    * @throws PSCmsException in case of error.
    */
   private Document getRelationshipDoc(PSRelationshipFilter filter)
      throws PSCmsException
   {

      if (filter == null)
         throw new IllegalArgumentException(
               "Relationship filter cannot be null.");

      Map<String, String> params = new HashMap<String, String>();
      Document inputDoc = PSXmlDocumentBuilder.createXmlDocument();
      String rFilter = PSXmlDocumentBuilder.toString(filter.toXml(inputDoc));
      params.put(PSRelationshipProcessor.PARAM_METHOD, "getRelationships");
      params.put(PSRelationshipProcessor.PARAM_RELATIONSHIP_FILTER, rFilter);

      Document doc = null;

      try
      {
         doc = m_remCataloger.getRemoteRequester().getDocument(
               "sys_ceFieldsCataloger/Relationship", params);

      }
      catch (Exception e)
      {
         Object[] args = new Object[] { e.getLocalizedMessage() };
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR, args);
      }

      return doc;

   }

   /**
    * The remote proxy that is used to make remote requests to the server from
    * this applet, initialized in the ctor and never <code>null</code> or
    * modified after that.
    */
   private PSRelationshipProcessorProxy m_proxy;
   
   /**
    * Used to get folder node types. Set in ctor, then never <code>null</code>
    * or changed.
    */
   private PSFolderActionManager m_folderActionMgr = null;
   
   /**
    * The remote cataloger used to catalog available set of relationships,
    * initialized in the ctor and never <code>null</code> or modified after that.
    */
   private PSRemoteCataloger m_remCataloger;

   /**
    * The applet document base URL used to make any server requests, initialized
    * in the ctor and never <code>null</code> or modified after that.
    */
   private URL m_docBase;
   
   /**
    * A reference back to the applet initialized in the ctor and never
    * <code>null</code> after that.
    */
   private PSContentExplorerApplet m_applet;
   
   /**
    * The name of the property that defines the lookup type of the relationship,
    * whose value defines whether we are looking for 'Ancestors' or 'Descendants'
    * of a particular relationship.
    */
   public static final String PROP_RS_LOOKUP_TYPE = "rs_type";

   /**
    * The constant to lookup ancestors an item for a relationship.
    */
   public static final String RS_LOOKUP_ANCESTORS = "ancestors";

   /**
    * The constant to lookup descendants of an item for a relationship.
    */
   public static final String RS_LOOKUP_DESCENDANTS = "descendants";
   
}
