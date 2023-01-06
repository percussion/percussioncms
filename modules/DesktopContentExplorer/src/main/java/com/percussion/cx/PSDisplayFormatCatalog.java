/*[ PSDisplayFormatCatalog.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

 /**
  * This is a convenient class to get catalog of all display formats from the
  * server keep locally and and provide an easy lookup acces to the desired
  * display formats.
  */
public class PSDisplayFormatCatalog
{
   /**
    * Initializes all available display formats and organizes them by the
    * category of display fotmat by making a request to the server and caches
    * them in this object.
    *
    * @param proxy the remote proxy to use to connect to server, may not be
    * <code>null</code>
    * @param userInfo the info about current user session, may not be <code>null
    * </code>
    *
    * @throws PSContentExplorerException if an error happens in the process of
    * loading display formats.
    * @throws IllegalStateException if not even a single display format is found
    * for each category.
    */
   public PSDisplayFormatCatalog(PSComponentProcessorProxy proxy, PSUserInfo userInfo, PSContentExplorerApplet applet)
      throws PSContentExplorerException
   {
      if(proxy == null)
         throw new IllegalArgumentException("proxy must not be null");

      if(userInfo == null)
         throw new IllegalArgumentException("userInfo may not be null.");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      m_applet = applet;

      m_proxy = proxy;
      m_userInfo = userInfo;

      loadDisplayFormats();
   }

   /**
    * Loads all available display formats and organizes them by the
    * category of display fotmat by making a request to the server and caches
    * them in this object.
    *
    * @throws PSContentExplorerException if an error happens in the process of
    * loading display formats.
    * @throws IllegalStateException if not even a single display format is found
    * for each category.
    */
   private void loadDisplayFormats() throws PSContentExplorerException
   {
      try
      {
         Element[] coll = m_proxy.load(
            PSDisplayFormat.getComponentType(PSDisplayFormat.class), null);

         m_displayFormats.clear();
         m_folderDisplayFormats.clear();
         m_rcDisplayFormats.clear();
         m_viewAndSearchDisplayFormats.clear();

         String communityId = String.valueOf(m_userInfo.getCommunityId());
         for (int i = 0; i < coll.length; i++)
         {
            PSDisplayFormat format = new PSDisplayFormat(coll[i]);

            //If not allowed for current community, don't cache it.
            if(!format.isAllowedForCommunity(communityId))
               continue;
            m_displayFormats.add(format);

            if(format.isValidForRelatedContent())
               m_rcDisplayFormats.add(format);

            if(format.isValidForFolder())
               m_folderDisplayFormats.add(format);

            if(format.isValidForViewsAndSearches())
               m_viewAndSearchDisplayFormats.add(format);
         }
      }
      catch(PSCmsException ex)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
      catch(PSUnknownNodeTypeException ex)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }

      if(m_folderDisplayFormats.isEmpty())
      {  
         // Gracefully pop an error msg
         ErrorDialogs.showErrorMessage(null,
               m_applet.getResourceString(getClass(), 
            "No display formats found for folders"),
            m_applet.getResourceString(getClass(), 
            "Error"));
      }

      if(m_rcDisplayFormats.isEmpty())
      {
         // Gracefully pop an error msg
         ErrorDialogs.showErrorMessage(null,
               m_applet.getResourceString(getClass(), 
            "No display formats found for related content"),
            m_applet.getResourceString(getClass(), 
            "Error"));
      }
      
      if(m_viewAndSearchDisplayFormats.isEmpty())
      {
         // Gracefully pop an error msg
         ErrorDialogs.showErrorMessage(null,
               m_applet.getResourceString(getClass(), 
            "No display formats found for views and searches"),
            m_applet.getResourceString(getClass(), 
            "Error"));
      }
   }

   /**
    * Returns an iterator of all cataloged display format objects.
    *
    * @return the list of all displayformats, never <code>null</code> or empty.
    *
    * @see PSDisplayFormat
    */
   public Iterator getAll()
   {
      return m_displayFormats.iterator();
   }

   /**
    * Returns an iterator of all cataloged display format objects applicable to
    * Folders cotents.
    *
    * @return the list of displayformats, never <code>null</code> or empty.
    *
    * @see PSDisplayFormat
    */
   public Iterator getFolderDisplayFormats()
   {
      return m_folderDisplayFormats.iterator();
   }

   /**
    * Returns an iterator of all cataloged display format objects applicable to
    * Related Content Search results.
    *
    * @return the list of displayformats, never <code>null</code> or empty.
    *
    * @see PSDisplayFormat
    */
   public Iterator getRcDisplayFormats()
   {
      return m_rcDisplayFormats.iterator();
   }

   /**
    * Returns an iterator of all cataloged display format objects applicable to
    * Views and Search results.
    *
    * @return the list of displayformats, never <code>null</code> or empty.
    *
    * @see PSDisplayFormat
    */
   public Iterator getViewsSearchDisplayFormats()
   {
      return m_viewAndSearchDisplayFormats.iterator();
   }

   /**
    * Returns the display format object for a given display format id. If the
    * supplied display format is not in the cache, then it reloads the display
    * formats from the server and gets the matching display format.
    *
    * @param displayformatid the identifier of the display format, may not be
    * <code>null</code> or empty.
    *
    * @return display format object for the id given, may be <code>null</code>
    * if not found even on the server.
    *
    * @see PSDisplayFormat
    *
    * @throws PSContentExplorerException if an error happens reloading the
    * display formats.
    */
   public PSDisplayFormat getDisplayFormatById(String displayformatid)
      throws PSContentExplorerException
   {
      if(displayformatid == null || displayformatid.trim().length() == 0)
         throw new IllegalArgumentException(
            "displayformatid may not be null or empty.");

      PSDisplayFormat match = getDisplayFormatByIdFromCache(displayformatid);
      if(match == null)
      {
         loadDisplayFormats();
         match = getDisplayFormatByIdFromCache(displayformatid);
      }

      return match;
   }

   /**
    * Gets the display format identified by the supplied id from the list of
    * display formats cached.
    *
    * @param displayformatid the identifier of the display format, assumed not
    * <code>null</code> or empty.
    *
    * @return the matching display format, may be <code>null</code> if not
    * found in cache.
    */
   private PSDisplayFormat getDisplayFormatByIdFromCache(String displayformatid)
   {
      PSDisplayFormat match = null;

      Iterator iter = m_displayFormats.iterator();
      while(iter.hasNext())
      {
         PSDisplayFormat format = (PSDisplayFormat)iter.next();
         String id = getDisplayFormatId(format);
         if(id.equals(displayformatid))
         {
            match = format;
            break;
         }
      }

      return match;
   }

   /**
    * Helper method to get the display format id given the object.
    *
    * @param format display format for which the id need to be extracted, must
    * not be <code>null</code>.
    *
    * @return the display format id, never <code>null</code> or empty.
    */
   public static String getDisplayFormatId(PSDisplayFormat format)
   {
      if(format == null)
         throw new IllegalArgumentException("format must not be null");

      return String.valueOf(format.getDisplayId());

   }

   /**
    * Array list of all display format objects cataloged, updated with list of
    * available formats and never <code>null</code>, empty or modified after
    * that.
    */
   private List m_displayFormats = new ArrayList();

   /**
    * List of all display formats applicable to Folders, updated with list in
    * the ctor and never <code>null</code>, empty or modified after that.
    */
   private List m_folderDisplayFormats = new ArrayList();

   /**
    * List of all display formats applicable to related content, updated with
    * list in the ctor and never <code>null</code>, empty or modified after that.
    */
   private List m_rcDisplayFormats = new ArrayList();

   /**
    * List of all display formats applicable to searchs and views, updated with
    * list in the ctor and never <code>null</code>, empty or modified after that.
    */
   private List m_viewAndSearchDisplayFormats = new ArrayList();

   /**
    * The proxy to use to load display formats from server, initialized in the
    * ctor and never <code>null</code> or modified after that.
    */
   private PSComponentProcessorProxy m_proxy;

   /**
    * The object that holds the information about the user session, initialized
    * in the ctor and never <code>null</code> or modified after that.
    */
   private PSUserInfo m_userInfo;
   
   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;
}