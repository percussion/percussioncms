/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.server.webservices.crosssite;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;

/**
 * This class represents one AA dependent item that is participating in move or
 * remove from folder action. Mainly acts as a storage of various attributes of
 * the item at various stages.
 */
public class PSAaFolderDependent implements Serializable
{

   private static final long serialVersionUID = 1L;

   /**
    * Ctor taking the locator for a potential AA relationship dependent and
    * source folder locator.
    * 
    * @param item locator for the dependent item, must not be <code>null</code>.
    * @param srcFolder locator for the source folder of the item, must not be
    * <code>null</code>
    */
   public PSAaFolderDependent(PSLocator item, PSLocator srcFolder)
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      if (srcFolder == null)
      {
         throw new IllegalArgumentException("srcFolder must not be null");
      }
      m_item = item;
      m_srcFolder = srcFolder;
   }

   /**
    * Accessor for the cross site/folder AA relationships as set by
    * {@link #setAaRelationships(PSRelationshipSet)}.
    * 
    * @return relationship set, never <code>null</code> may be empty.
    */
   public PSRelationshipSet getAaRelationships()
   {
      return m_aaRelationships;
   }

   /**
    * Set AA relationships in which this item is dependent.
    * 
    * @param aaRelationships aa relationships, must not be <code>null</code>,
    * may be empty.
    */
   public void setAaRelationships(PSRelationshipSet aaRelationships)
   {
      if (aaRelationships == null)
      {
         throw new IllegalArgumentException("aaRelationships must not be null");
      }
      m_aaRelationships = aaRelationships;
   }

   /**
    * Accessor of the dependent item supplied during construction
    * {@link #PSAaFolderDependent(PSLocator, PSLocator)}.
    * 
    * @return never <code>null</code>.
    */
   public PSLocator getItem()
   {
      return m_item;
   }

   /**
    * Access method for the source folder as supplied in the ctor.
    * 
    * @return the source folder as supplied in the ctor.
    */
   public PSLocator getSrcFolder()
   {
      return m_srcFolder;
   }

   /**
    * Flag to indicate this item is a grand child of the root folder (not the
    * source folder in this class). Default is <code>false</code>.
    * 
    * @return <code>true</code> if it was set to <code>true</code> (using
    * {@link #setGrandChild(boolean)}.
    */
   public boolean isGrandChild()
   {
      return m_isGrandChild;
   }

   /**
    * Set the grand child flag.
    * 
    * @param isGrandChild <code>true</code> to indicate it is a grand child of
    * the starting root, <code>false</code> otherwise.
    */
   public void setGrandChild(boolean isGrandChild)
   {
      m_isGrandChild = isGrandChild;
   }

   /**
    * Get an array of siteids as set by {@link #setSites(Integer[])}.
    * 
    * @return array of siteids, never <code>null</code> may be empty.
    */
   public Integer[] getSites()
   {
      return m_sites;
   }

   /**
    * Helper method to find if this item is the last one on the site with
    * supplied siteid. Note that the evaluation is purely based on the item
    * sites previously set using {@link #setSites(Integer[])}.
    * 
    * @return <code>true</code> if this dependent item exists on the supplied
    * site only once.
    */
   public boolean isLastOnSite(Integer siteid)
   {
      if (siteid == null)
      {
         throw new IllegalArgumentException("siteid must not be null");
      }
      int count = 0;
      for (int i = 0; i < m_sites.length; i++)
      {
         if (siteid.equals(m_sites[i]))
            count++;
      }
      return (count == 1);
   }

   /**
    * Set the array of siteids to mean this item exists in the sites with
    * supplied ids. The entries can be duplicate to indicate the item exists
    * under the same site multiple times via probably different folder paths.
    * 
    * @param sites array of site ids, must not be <code>null</code> may be
    * empty.
    */
   public void setSites(Integer[] sites)
   {
      if (sites == null)
      {
         throw new IllegalArgumentException("sites must not be null");
      }
      m_sites = sites;
   }

   /**
    * Create a DOM element of this object which can be a child of the supplied
    * document and return.
    * 
    * @param parentDoc parent DOM document, must not be <code>null</code>
    * @return DOM element that is an XML representation this object which can
    * readily be appended to the supplied XML document, never <code>null</code>.
    */
   public Element toXml(Document parentDoc)
   {
      Element elem = parentDoc.createElement("depItem");
      elem.setAttribute("actionSuccess", m_actionSuccess ? "yes" : "no");
      elem.setAttribute("contentId", "" + m_item.getId());
      elem.setAttribute("revision", "" + m_item.getRevision());
      elem.setAttribute("isFolderGrandChild", m_isGrandChild ? "yes" : "no");
      Element folder = parentDoc.createElement("parentFolder");
      folder.setAttribute("folderid", "" + m_srcFolder.getId());
      elem.appendChild(folder);
      Element itemSites = parentDoc.createElement("itemSites");
      elem.appendChild(itemSites);
      for (int i = 0; i < m_sites.length; i++)
      {
         Element child = parentDoc.createElement("site");
         child.setAttribute("siteid", m_sites[i].toString());
         itemSites.appendChild(child);
      }
      elem.appendChild(m_aaRelationships.toXml(parentDoc));
      return elem;
   }

   /**
    * Is the action success?
    * 
    * @return returns the flag as set by {@link #setActionSuccess(boolean)}.
    * Default is <code>true</code>.
    */
   public boolean isActionSuccess()
   {
      return m_actionSuccess;
   }

   /**
    * Set the action success flag.
    * 
    * @param actionSuccess
    */
   public void setActionSuccess(boolean actionSuccess)
   {
      m_actionSuccess = actionSuccess;
   }

   /**
    * AA dependent item.
    * 
    * @see #getItem()
    */
   PSLocator m_item = null;

   /**
    * Source folder locator for the item, initialize in the ctor, never
    * <code>null</code> after that.
    */
   private PSLocator m_srcFolder = null;

   /**
    * @see #getAaRelationships()
    */
   private PSRelationshipSet m_aaRelationships = new PSRelationshipSet();

   /**
    * @see #getSites()
    * @see #setSites(Integer[])
    */
   private Integer[] m_sites = new Integer[0];

   /**
    * Grand child flag, default is <code>false</code>.
    * 
    * @see #isGrandChild()
    * @see #setGrandChild(boolean)
    */
   private boolean m_isGrandChild = false;

   /**
    * @see #isActionSuccess()
    * @see #setActionSuccess(boolean)
    */
   private boolean m_actionSuccess = true;
}
