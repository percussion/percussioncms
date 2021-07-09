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
package com.percussion.fastforward.managednav;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPreparedStatement;

/**
 * The PSNavon represents a navigation section. This can be represented as
 * either a Navon or NavTree content item.
 * <p>
 * 
 * @author DavidBenua
 * 
 * 
 *  
 */
public class PSNavon
{
   /**
    * Construct a navon from a content item. Convenience method for
    * PSNavon(IPSRequestContext,PSComponentSummary
    * 
    * @param req the parent request context.
    * @param loc the locator for the content item.
    * @throws PSNavException
    */
   public PSNavon(IPSRequestContext req, PSLocator loc) throws PSNavException
   {
      ms_config = PSNavConfig.getInstance(req);

      PSNavComponentSummary summary = new PSNavComponentSummary(loc);
      //PSNavUtil.getItemSummary(req, loc);
      loadNavon(req, summary);
   }

   /**
    * Construct a navon from a content item.
    * 
    * @param req the parent request context
    * @param summary the content item
    * @throws PSNavException
    */
   public PSNavon(IPSRequestContext req, PSNavComponentSummary summary)
         throws PSNavException
   {
      ms_config = PSNavConfig.getInstance(req);
      loadNavon(req, summary);
   }

   /**
    * Construct a navon from a content item. Used when the PSNavComponentSummary
    * is not available.
    * 
    * @param req the parent request context.
    * @param summary the content item.
    * @throws PSNavException
    */
   public PSNavon(IPSRequestContext req, PSComponentSummary summary)
         throws PSNavException
   {
      ms_config = PSNavConfig.getInstance(req);
      PSNavComponentSummary sum1 = new PSNavComponentSummary(summary);
      loadNavon(req, sum1);
   }

   /**
    * Loads the values from the specified content item.
    * 
    * @param req the parent request context.
    * @param summary the content item to load.
    * @throws PSNavException
    */
   private void loadNavon(IPSRequestContext req, PSNavComponentSummary summary)
         throws PSNavException
   {
      try
      {
         log.debug("Creating Navon for {}", summary.getName());
         this.m_loc = summary.getCurrentLocator();
         
         //If the authtype is not preview then correct the revision to last
         // public revision.
         String authType = req.getParameter(IPSHtmlParameters.SYS_AUTHTYPE,
               PSNavUtil.PREVIEW_AUTHTYPE);
         if (!authType.equals(PSNavUtil.PREVIEW_AUTHTYPE))
         {
            String rev = PSMacroUtils.getLastPublicRevision(Integer
                  .toString(m_loc.getId()));
            m_loc.setRevision(Integer.parseInt(rev));
         }
            
         log.debug("Content id {} revision {}", String.valueOf(m_loc.getId()), String.valueOf(m_loc.getRevision()));
         this.LoadViaSQL(m_loc.getId(),req);

         //         Document ddoc = PSNavUtil.getNavonDocument(req, summary);
         //         
         //         this.label = PSNavUtil.getFieldValueFromXML(ddoc,
         //            config.getPropertyString(PSNavConfig.NAVON_TITLE_FIELD));
         //         log.debug("Label is: " + this.label);
         //         
         //         this.name = PSNavUtil.getFieldValueFromXML(ddoc,
         //            config.getPropertyString(PSNavConfig.NAVON_NAME_FIELD));
         //         log.debug("Name is: " + this.name);
         //
         //         this.imageSelector = PSNavUtil.getFieldValueFromXML(ddoc,
         //            config.getPropertyString(PSNavConfig.NAVON_SELECTOR_FIELD));
         //         log.debug("Image selector is " + this.imageSelector);
         //         
         //         this.variableSelector = PSNavUtil.getFieldValueFromXML(ddoc,
         //            config.getPropertyString(PSNavConfig.NAVON_VARIABLE_FIELD));

         log.debug("building info link...");
         this.m_infoLink = new PSNavLink();
         //Assume it is Navon
         PSContentTypeVariant variant = ms_config.getInfoVariant();
         //It can be a NavTree too, so get the correct info variant
         if (summary.getContentTypeId() == ms_config.getNavTreeType())
            variant = ms_config.getNavtreeInfoVariant();

         this.m_infoLink.createLinkToDocument(req, summary, variant);

         log.debug("building landing page...");
         try
         {
            this.m_landingPage = buildLinkFromSlot(this.m_loc, ms_config
                  .getPropertyString(PSNavConfig.NAVON_LANDING_SLOT), req,
                  null, false);
         }
         catch (PSNavException e1)
         {
            log.error("Unable to build landing page {}", e1.getMessage());
            log.debug(e1.getMessage(),e1);
            this.m_landingPage = null;
         }

         log.debug("building image list...");
         this.buildImageList(req);

         log.debug("constructor finished");
      }
      catch (PSNavException ne)
      {
         throw (PSNavException) ne.fillInStackTrace();

      }
      catch (Exception e)
      {
         throw new PSNavException(e);
      }

   }

   /**
    * Sets the inherited info from the parent navon. The relative and absolute
    * levels are incremented and the navon type is set based on the parent type.
    * 
    * @see PSNavonType#getDescendentType(PSNavonType, int)
    * @param req
    * @param parentNavon
    */
   public void setParentInfo(IPSRequestContext req, PSNavon parentNavon)
   {
      this.m_directParent = parentNavon;
      this.m_type = PSNavonType.getDescendentType(parentNavon.getType(),
            parentNavon.getRelativeLevel());
      this.m_relLevel = parentNavon.getRelativeLevel() + 1;
      this.m_absLevel = parentNavon.getAbsoluteLevel() + 1;

   }

   /**
    * Tests if another navon has the same content id. Cannot use
    * <code>equals</code> for this because the contract with <code>hash</code>
    * 
    * @param other the navon to compare against.
    * @return <code>true</code> if the navons have the same content id,
    *         <code>false</code> otherwise.
    */
   public boolean hasSameId(PSNavon other)
   {
      if (this.m_loc.getId() == other.m_loc.getId())
      {
         return true;
      }
      return false;
   }

   /**
    * Builds the list of images in the image slot.
    * 
    * @param req the parent request context.
    * @throws PSNavException
    */
   private void buildImageList(IPSRequestContext req) throws PSNavException
   {
      PSSlotType imageSlot = ms_config.getImageSlot();
      PSAaRelationshipList slotContents = PSNavUtil.getSlotContents(req,
            this.m_loc, imageSlot);
      Iterator slotIter = slotContents.iterator();
      while (slotIter.hasNext())
      {
         PSAaRelationship relation = (PSAaRelationship) slotIter.next();
         PSNavImageLink imageLink = new PSNavImageLink(req, relation);
         this.m_imageList.add(imageLink);
      }
   }

   /**
    * Creates a link from the first item in a slot. The link will either point
    * at the snippet variant, or will assemble the snippet and take the first
    * link within the snippet.
    * 
    * @param parentDoc the parent document (this Navon)
    * @param slotName the name of the slot to examine
    * @param req the callers request context
    * @param useVariant override the variant in the slot with this variant
    * @param followLink flag to determine which link to build. If
    *           <code>true</code> the first snippet will be assembled, and the
    *           first link within that snippet becomes the link URI.
    * @return the new link, or <code>null</code> if the slot is empty.
    * @throws PSNavException when the slot does not exist or an internal
    *            processing exception occurs.
    */
   private PSNavLink buildLinkFromSlot(PSLocator parentDoc, String slotName,
         IPSRequestContext req, PSContentTypeVariant useVariant,
         boolean followLink) throws PSNavException
   {
      PSNavLink resultLink = new PSNavLink();

      try
      {
         log.debug("Building Link from Slot {}", slotName);

         PSAaRelationshipList slotContents = PSNavUtil.getSlotContents(req,
               parentDoc, slotName);
         Iterator slotIter = slotContents.iterator();
         if (!slotIter.hasNext())
         {
            String errMsg = "Slot " + slotName + " is empty";
            log.debug(errMsg);
            return null;
         }
         PSAaRelationship firstRelation = (PSAaRelationship) slotIter.next();

         resultLink.buildLinkFromRelationship(req, firstRelation, useVariant,
               followLink);

      }
      catch (PSNavException navex)
      {
         throw (PSNavException) navex.fillInStackTrace();
      }
      catch (Exception ex)
      {
         throw new PSNavException(ex);
      }
      return resultLink;
   }

   /**
    * Finds the parent for this navon. <code>null</code> if this navon is at
    * the root of the tree.
    * 
    * @param req the parent request context.
    * @return the parent navon or <code>null</code>
    * @throws PSNavException
    */
   public PSNavon findParent(IPSRequestContext req) throws PSNavException
   {
      PSNavonType parentType;
      ms_config = PSNavConfig.getInstance(req);

      PSComponentSummary parentSummary = findParentSummary(req);
      if (parentSummary == null)
      { //this must be the top of the tree
         //maybe an error, but maybe not.
         String errMsg = "Unexpected tree root. Navon without parents "
               + String.valueOf(this.m_loc.getId());
         log.debug(errMsg);
         return null;
      }
      PSNavon parentNavon = new PSNavon(req, parentSummary);
      parentNavon.m_directDescendent = this;
      this.m_directParent = parentNavon;
      parentNavon.setRelativeLevel(this.m_relLevel - 1);

      if (parentSummary.getContentTypeId() == ms_config.getNavTreeType())
      {
         parentType = new PSNavonType(PSNavonType.TYPE_ROOT);
      }
      else
      {
         parentType = new PSNavonType(PSNavonType.TYPE_ANCESTOR);
      }
      parentNavon.setType(parentType);

      return parentNavon;
   }

   /**
    * Finds the parent summary. Will be <code>null<code> if the parent
    * cannot be found.
    * @param req the parent request context.
    * @return the component summary or <code>null</code>
    * @throws PSNavException
    */
   private PSComponentSummary findParentSummary(IPSRequestContext req)
         throws PSNavException
   {
      PSComponentSummary parentSummary = null;
      int parentCount = 0;

      try
      {
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         PSRelationshipFilter parentFilter = new PSRelationshipFilter();
         parentFilter.setDependent(this.m_loc);
         parentFilter
               .setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
         parentFilter.setCommunityFiltering(false);

         // get all AA parents
         PSComponentSummaries summaries = relProxy.getSummaries(parentFilter,
               true);

         Iterator parentIterator = summaries.iterator();
         while (parentIterator.hasNext())
         {
            PSComponentSummary maybeParent = (PSComponentSummary) parentIterator
                  .next();
            if (PSNavUtil.isNavType(req, maybeParent))
            {
               parentCount++;
               parentSummary = maybeParent;
            }
         }

      }
      catch (PSNavException ex1)
      {
         throw (PSNavException) ex1.fillInStackTrace();
      }
      catch (Exception ex)
      {
         throw new PSNavException(this.getClass().getName(), ex);
      }
      if (parentCount > 1)
      {
         //danger danger the tree is invalid
         String errMsg = "Invalid tree structure. Item with duplicate parents "
               + String.valueOf(this.m_loc.getId());
         log.error(errMsg);
         throw new PSNavException(errMsg);
      }
      return parentSummary;
   }

   /**
    * Gets the absolute level.
    * 
    * @return the absolute level.
    */
   public int getAbsoluteLevel()
   {
      return m_absLevel;
   }

   /**
    * Gets the relative level.
    * 
    * @return the relative level.
    */
   public int getRelativeLevel()
   {
      return m_relLevel;
   }

   /**
    * Gets the navon type.
    * 
    * @return the navon type.
    */
   public PSNavonType getType()
   {
      return m_type;
   }

   /**
    * Sets the absolute level of this Navon within the tree. Level 0 is the root
    * node, with each level below the root increasing by 1.
    * 
    * @param i the new level.
    */
   public void setAbsoluteLevel(int i)
   {
      m_absLevel = i;
   }

   /**
    * Sets the relative level of this Navon within the tree. Level 0 is the self
    * node. Levels above the self node (closer to the root) are negative, and
    * descendents of the self node are positive.
    * 
    * @param i the new level.
    */
   public void setRelativeLevel(int i)
   {
      m_relLevel = i;
   }

   /**
    * sets the type of this Navon.
    * 
    * @param type
    */
   public void setType(PSNavonType type)
   {
      this.m_type = type;
   }

   /**
    * sets the type of this Navon, using the type codes defined in PSNavonType.
    * 
    * @param type
    */
   public void setType(int type)
   {
      this.m_type = new PSNavonType(type);
   }

   /**
    * gets the locator for this Navon.
    * 
    * @return the locator for this Navon.
    */
   public PSLocator getLocator()
   {

      return this.m_loc;
   }

   /**
    * This is a unique identifier for the navon within the scope of all navons.
    * It is the system title of the navon.
    * 
    * @return the name of the Navon. Never <code>null</code> or empty.
    */
   public String getName()
   {

      return this.m_name;
   }

   /**
    * Finds the children of this Navon using the Navon SubMenu Slot.
    * 
    * @param req the callers request context. Used to obtain the appropriate
    *           proxy objects.
    * @throws PSNavException when the slot cannot be found, or an unexpected
    *            exception occurs.
    */
   public void findChildren(IPSRequestContext req) throws PSNavException
   {
      Set nodeSet = new HashSet();
      findChildren(req, nodeSet);
   }

   /**
    * Finds the children of this navon. The nodeset is used to prevent the same
    * node from being added to the tree more than once.
    * 
    * @param req the parent request context.
    * @param nodeSet the set of Integers representing all nodes in this tree.
    * @throws PSNavException
    */
   private void findChildren(IPSRequestContext req, Set nodeSet)
         throws PSNavException
   {
      Integer myID = new Integer(this.m_loc.getId());
      if (nodeSet.contains(myID))
      {
         log.error("Invalid Tree Structure. Duplicate id in tree {}", myID);
         return;
      }
      else
      {
         nodeSet.add(myID);
      }

      PSNavon childNavon;
      int descendentId = (this.m_directDescendent != null)
            ? this.m_directDescendent.getLocator().getId()
            : -1;

      try
      {
         log.debug("finding children for {}", this.m_name);

         PSSlotType menuSlot = ms_config.getMenuSlot();

         if (menuSlot == null)
         {
            log.error("Menu Slot not found ");
            throw new PSNavException(MSG_MENU_SLOT_NOT_FOUND);
         }
         log.debug("found slot");
         //        PSComponentSummaries slotItems =
         //           aaProxy.getSlotItems(this.loc, menuSlot,
         // PSNavUtil.getAuthType(req));

         PSAaRelationshipList slotItems = PSNavUtil.getSlotContents(req,
               this.m_loc, menuSlot);
         log.debug("found slot contents");
         Iterator childIterator = slotItems.iterator();
         while (childIterator.hasNext())
         {
            PSAaRelationship aaRel = (PSAaRelationship) childIterator.next();
            PSLocator childLoc = aaRel.getDependent();
            PSNavComponentSummary childSummary =
            // PSNavUtil.getItemSummary(req, childLoc);
            new PSNavComponentSummary(childLoc);
            log.debug("found child named {}", childSummary.getName());
            int childId = childSummary.getCurrentLocator().getId();
            if (descendentId != -1 && childId == descendentId)
            { // this is our direct descendent.
               log.debug("this child is a direct descendent");
               childNavon = m_directDescendent;
               childNavon.setAbsoluteLevel(this.m_absLevel + 1);
            }
            else
            {
               log.debug("adding new child");
               childNavon = new PSNavon(req, childSummary);
               childNavon.setParentInfo(req, this);
            }
            childNavon.setAbsoluteLevel(this.m_absLevel + 1);
            m_children.add(childNavon); //append to the list
            log
                  .debug("recurse to next level {} ", String.valueOf(this.m_absLevel));
            childNavon.findChildren(req, nodeSet); //recurse
         }
      }
      catch (PSNavException ex)
      {
         throw (PSNavException) ex.fillInStackTrace();
      }
      catch (Exception e)
      {
         throw new PSNavException(this.getClass().getName(), e);
      }
   }

   /**
    * Gets the image color selector. If the selector is <code>null</code> the
    * value will be inherited from the parent navon.
    * 
    * @return the image color selector.
    */
   public String getImageSelector()
   {
      return m_imageSelector;
   }

   /**
    * Gets the variable selector. If the selector is <code>null</code> the
    * value will be inherited from the parent navon.
    * 
    * @return the variable selector.
    */
   public String getVariableSelector()
   {
      return m_variableSelector;
   }

   /**
    * creates an XML element for this Navon as described in the NavTree schema.
    * See Navtree.xsd for full details.
    * <p>
    * This routine is recursive. All child Navons (and their children, and so
    * on) will be contained within the resulting <code>&lt;navon&gt;</code>
    * element.
    * 
    * @param doc the parent documentin which to create the element.
    * @return the Element, which is never <code>null</code>
    */
   public Element toXML(Document doc)
   {
      Element navon = doc.createElement(XML_ELEMENT_NAME);
      navon.setAttribute(XML_ATTR_NAME, this.m_name);
      navon.setAttribute(XML_ATTR_CONTENTID, String.valueOf(this.m_loc.getId()));
      navon.setAttribute(XML_ATTR_REVISION, String.valueOf(this.m_loc
            .getRevision()));
      navon.setAttribute(XML_ATTR_TYPE, this.m_type.toString());
      navon.setAttribute(XML_ATTR_REL_LEVEL, String.valueOf(this.m_relLevel));
      navon.setAttribute(XML_ATTR_ABS_LEVEL, String.valueOf(this.m_absLevel));

      Element labelElement = doc.createElement(XML_ELEMENT_LABEL);
      navon.appendChild(labelElement);
      Node labelNode = doc.createTextNode(this.m_label);
      labelElement.appendChild(labelNode);

      if (this.m_landingPage != null)
      {
         Element landingElem = doc.createElement(XML_ELEMENT_LANDINGPAGE);
         navon.appendChild(landingElem);
         this.m_landingPage.toXML(landingElem);
      }

      if (this.m_infoLink != null)
      {
         Element infoLinkElem = doc.createElement(XML_ELEMENT_INFOLINK);
         navon.appendChild(infoLinkElem);
         this.m_infoLink.toXML(infoLinkElem);
      }

      if (!this.m_imageList.isEmpty())
      {
         Element imagesElem = doc.createElement(XML_ELEMENT_IMAGELIST);
         navon.appendChild(imagesElem);
         Iterator images = m_imageList.iterator();
         while (images.hasNext())
         {
            PSNavImageLink il = (PSNavImageLink) images.next();
            il.toXML(imagesElem);
         }
      }

      Iterator childIterator = this.m_children.iterator();
      while (childIterator.hasNext())
      {
         PSNavon childNavon = (PSNavon) childIterator.next();
         Element childElement = childNavon.toXML(doc);
         navon.appendChild(childElement);
      }

      return navon;
   }

   /**
    * Loads the navon via direct JDBC rather than XML.
    * 
    * @param id the content id.
    * @throws PSNavException
    */
   private void LoadViaSQL(int id, IPSRequestContext req) throws PSNavException
   {
      Connection conn = PSNavSQLUtils.connect();
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String authType = req.getParameter(IPSHtmlParameters.SYS_AUTHTYPE,
            PSNavUtil.PREVIEW_AUTHTYPE);
      boolean isPreview = authType.equals(PSNavUtil.PREVIEW_AUTHTYPE)?true:false;
      try
      {
         String sql_stmt = "";
         int revision = 0;
         if(isPreview)
         {
            sql_stmt = SQL_LOAD_PREVIEW;
         }
         else
         {
            sql_stmt = SQL_LOAD_PUBLISH;
            String rev = PSMacroUtils.getLastPublicRevision(Integer.toString(id));
            revision = Integer.parseInt(rev);
         }
         
               
         stmt = PSPreparedStatement.getPreparedStatement(conn, sql_stmt);
         if(isPreview)
         {
            stmt.setInt(1, id);
            stmt.setInt(2, id);
         }
         else
         {
            stmt.setInt(1, revision);
            stmt.setInt(2, id);
            stmt.setInt(3, revision);
            stmt.setInt(4, id);
         }
         rs = stmt.executeQuery();
         boolean valid = rs.next();
         if (valid)
         {
            this.m_label = rs.getString(1);
            log.debug("Label is {} ", this.m_label);

            this.m_name = rs.getString(2);
            log.debug("Name is {}", this.m_name);

            this.m_imageSelector = rs.getString(3);
            log.debug("Image Selector is {}", this.m_imageSelector);

            this.m_variableSelector = rs.getString(4);
            log.debug("Variable Selector is {}", this.m_variableSelector);
         }
         else
         {
            String errMsg = "Unable to read Navon from table id = "
                  + String.valueOf(id);
            log.error(errMsg);
            throw new PSNavException(errMsg);
         }
      }
      catch (PSNavException psx)
      {
         throw (PSNavException) psx.fillInStackTrace();
      }
      catch (Exception ex)
      {
         log.error("SQL Error {}", ex.getMessage());
         log.debug(ex.getMessage(),ex);
         throw new PSNavException(ex);
      }
      finally
      {
         PSNavSQLUtils.closeout(conn, stmt, rs);
      }
   }

   /**
    * Pointer to the config object.
    */
   private static PSNavConfig ms_config = null;

   /**
    * the text label for this Navon.
    */
   private String m_label = null;

   /**
    * The internal name (system title) for this Navon.
    */
   private String m_name = null;

   /**
    * The absolute level. Level 0 is the root, level 1 is the level below the
    * root, and so on.
    */
   private int m_absLevel = 0;

   /**
    * The Relative level. Level 0 is the same level as the <code>self</code>
    * node. Levels above the self node (that is, closer to the root0 are
    * negative.
    */
   private int m_relLevel = 0;

   /**
    * The locator for the content item that represents this Navon.
    */
   private PSLocator m_loc = null;

   /**
    * A link to the landing page for this section.
    */
   private PSNavLink m_landingPage = null;

   /**
    * The link to the info variant for this section.
    */
   private PSNavLink m_infoLink = new PSNavLink();

   //private PSNavLink imageLink = null;
   /**
    * List of images
    */
   private List m_imageList = new ArrayList();

   /**
    * The navon type.
    * 
    * @see PSNavonType#PSNavonType()
    */
   private PSNavonType m_type = new PSNavonType();

   /**
    * The child PSNavon objects.
    */
   private List m_children = new ArrayList();

   /**
    * Get a list of navon object.
    *
    * @return An iterator of zero or more <code>PSnavon</code>
    *    objects. Never <code>null</code>, but may be empty.
    */
   public Iterator getChildren()
   {
      return m_children.iterator();
   }
   /**
    * The value of the image color selector for this nav section. Will be
    * <code>null</code> if no image color has been selected. In this case, the
    * image color of the parent section will be used.
    */
   private String m_imageSelector = null;

   /**
    * The value of the context variable selector for this nav section. Will be
    * <code>null</code> if no variable selector has been chosen. In ths case,
    * the variable selector will inherit the value from the parent nav section.
    */
   private String m_variableSelector = null;

   /**
    * Set of child navons. Used to maintain uniqueness.
    */
   private Set m_childSet = new HashSet();

   /**
    * The direct descendent navon. In an ancestor or root navon, this indicates
    * the path to the self navon. In all other navons, including descedent
    * navons and sibling navons, this will be <code>null</code>
    */
   private PSNavon m_directDescendent = null;

   /**
    * The direct parent navon.
    */
   private PSNavon m_directParent = null;

   /**
    * Writes the log.
    */
   private static final Logger log = LogManager.getLogger(PSNavon.class);

   /**
    * SQL Statement for loading the Navon data in preview mode.
    */
   private static final String SQL_LOAD_PREVIEW = "select sh.DISPLAYTITLE, cs.TITLE, "
      + "navon.NO_SELECTOR, navon.NO_VARIABLE "
      + "from CONTENTSTATUS cs, RXS_CT_NAVON navon, "
      + "RXS_CT_SHARED sh where cs.CONTENTID = sh.CONTENTID "
      + " and cs.CURRENTREVISION = sh.REVISIONID and cs.CONTENTID = navon.CONTENTID "
      + " and sh.REVISIONID = navon.REVISIONID and cs.CONTENTID = ? "
      + " UNION "
      + "select sh.DISPLAYTITLE, cs.TITLE, "
      + "navtree.NO_SELECTOR, navtree.NO_VARIABLE "
      + "from CONTENTSTATUS cs, RXS_CT_NAVTREE navtree, "
      + "RXS_CT_SHARED sh where cs.CONTENTID = sh.CONTENTID "
      + " and cs.CURRENTREVISION = sh.REVISIONID and cs.CONTENTID = navtree.CONTENTID "
      + " and sh.REVISIONID = navtree.REVISIONID and cs.CONTENTID = ? "

   ;

   /**
    * SQL Statement for loading the Navon data in publish mode.
    */
   private static final String SQL_LOAD_PUBLISH = "select sh.DISPLAYTITLE, cs.TITLE, "
      + "navon.NO_SELECTOR, navon.NO_VARIABLE "
      + "from CONTENTSTATUS cs, RXS_CT_NAVON navon, "
      + "RXS_CT_SHARED sh where cs.CONTENTID = sh.CONTENTID "
      + " and sh.REVISIONID = ? and cs.CONTENTID = navon.CONTENTID "
      + " and sh.REVISIONID = navon.REVISIONID and cs.CONTENTID = ? "
      + " UNION "
      + "select sh.DISPLAYTITLE, cs.TITLE, "
      + "navtree.NO_SELECTOR, navtree.NO_VARIABLE "
      + "from CONTENTSTATUS cs, RXS_CT_NAVTREE navtree, "
      + "RXS_CT_SHARED sh where cs.CONTENTID = sh.CONTENTID "
      + " and sh.REVISIONID = ? and cs.CONTENTID = navtree.CONTENTID "
      + " and sh.REVISIONID = navtree.REVISIONID and cs.CONTENTID = ? "

   ;

   /**
    * XML Element name for the navon. Used for serialization to XML.
    */
   public static final String XML_ELEMENT_NAME = "navon";

   /**
    * XML Attribute name for the navon name. Used for serialization to XML.
    */
   public static final String XML_ATTR_NAME = "name";

   /**
    * XML Attribute name for the content id. Used for serialization to XML.
    */
   public static final String XML_ATTR_CONTENTID = 
      IPSHtmlParameters.SYS_CONTENTID;

   /**
    * XML Attribute name for the revision id. Used for serialization to XML.
    */
   public static final String XML_ATTR_REVISION = 
      IPSHtmlParameters.SYS_REVISION;

   /**
    * XML Attribute name for the navon type. Used for serialization to XML.
    */
   public static final String XML_ATTR_TYPE = "relation";

   /**
    * XML Attribute name for the absolute level. Used for serialization to XML.
    */
   public static final String XML_ATTR_ABS_LEVEL = "absolute-level";

   /**
    * XML Attribute name for the relative level. Used for serialization to XML.
    */
   public static final String XML_ATTR_REL_LEVEL = "relative-level";

   /**
    * XML Attribute name for the navon label. Used for serialization to XML.
    */
   public static final String XML_ELEMENT_LABEL = "label";

   /**
    * XML Attribute name for the info link. Used for serialization to XML.
    */
   public static final String XML_ELEMENT_INFOLINK = "info-link";

   /**
    * XML Attribute name for the image list. Used for serialization to XML.
    */
   public static final String XML_ELEMENT_IMAGELIST = "image-list";

   /**
    * XML Attribute name for the landing page. Used for serialization to XML.
    */
   public static final String XML_ELEMENT_LANDINGPAGE = "landing-page";

   /**
    * Error message for missing menu slot.
    */
   private static final String MSG_MENU_SLOT_NOT_FOUND = 
      "Configuration error: Menu slot not found!";

}
