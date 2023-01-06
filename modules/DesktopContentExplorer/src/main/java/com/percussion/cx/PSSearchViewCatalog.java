/******************************************************************************
 *
 * [ PSSearchViewCatalog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.cx;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a convenient class to get catalog of all search views from the
 * server, keep locally and and provide an easy lookup access to the desired
 * search or view. Searches are stored using the unique search id.
 */
@SuppressWarnings("unchecked")
public class PSSearchViewCatalog
{
   /**
    * Initializes all available searches and views by making a request to the
    * server and caches them in this object. 
    * 
    * @param proxy the remote proxy to use to connect to server, may not be
    * <code>null</code>
    * @param isFtsAvailable <code>true</code> if fts is enabled,
    * <code>false</code> otherwise.
    * 
    * @throws PSContentExplorerException if an error happens in the process of
    * loading searches and views.
    * @throws IllegalStateException if the default search is not found in the
    * available searches.
    */
   @SuppressWarnings("unused")
   public PSSearchViewCatalog(PSComponentProcessorProxy proxy, 
         boolean isFtsAvailable, PSContentExplorerApplet applet)
         throws PSContentExplorerException {
      if (proxy == null)
         throw new IllegalArgumentException("proxy must not be null");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      m_applet = applet;

      m_proxy = proxy;

      m_isFtsEnabled = isFtsAvailable;
      loadSearches();
   }

   /**
    * Loads all available searches and views by making a request to the server
    * and caches them in this object.  For each search loaded, if 
    * {@link PSSearch#useExternalSearch()} returns <code>true</code> and an
    * external search engine is not available, the search is converted to an
    * internal search.  
    * 
    * @throws PSContentExplorerException if an error happens in the process of
    *            loading searches and views.
    * @throws IllegalStateException if the default search is not found in the
    *            available searches.
    */
   public void loadSearches() 
      throws PSContentExplorerException
   {
      try
      {
         Element[] coll = m_proxy.load(PSSearch
               .getComponentType(PSSearch.class), null);

         if (coll.length < 1)
            throw new IllegalStateException(
                  "Searches or views are not available.");

         /* Remember the empty search to preserve changes the user may have
          * made.
          */
         Object emptySearch = m_searches.get(EMPTY_SEARCHID);
         m_searches.clear();
         m_searches.put(EMPTY_SEARCHID, emptySearch);
         m_rcSearch = null;

         String commId = String.valueOf(
               m_applet.getUserInfo().getCommunityId());
         int cxSearchIndex = -1;
         PSSearch defaultRcNewSearch = null;
         for (int i = 0; i < coll.length; i++)
         {
            PSSearch search = new PSSearch(coll[i]);
            if (!m_isFtsEnabled)
               search.convertToInternal();
            String searchid = getSearchId(search);
            m_searches.put(searchid, search);
            
            /* As we are going through the searches, check for and remember
             * the defaults for cx and rc so they can be used later if needed.
             */
            if (search.isAADNewSearch())
               defaultRcNewSearch = search;
            if (search.isCXNewSearch())
               cxSearchIndex = i;
            
            // Initialize the related content search if this is the
            // first one found
            if (search.isAADNewSearch(commId) && m_rcSearch == null)
            {
               m_rcSearch = search;
            }

            /*
             * Check whether the current search is default cx search, if true
             * then add a new search with a large searchid number to the catalog
             * and hold the empty search document.
             */
            if (search.isCXNewSearch(commId) && emptySearch == null)
            {
               m_emptySearchDoc = createAndAddEmptySearch(coll[i]);
            }
         }

         if (m_rcSearch == null && defaultRcNewSearch != null)
            m_rcSearch = defaultRcNewSearch;
         if (m_emptySearchDoc == null && cxSearchIndex >= 0)
            m_emptySearchDoc = createAndAddEmptySearch(coll[cxSearchIndex]);
         
         if (m_emptySearchDoc == null)
            throw new IllegalStateException(
                  "Default search for Content Exploreer is not found");
         if (getRcSearch() == null)
            throw new IllegalStateException(
                  "Default search for Active Assembly is not found");
      }
      catch (PSCmsException ex)
      {
         throw new PSContentExplorerException(
               IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException ex)
      {
         throw new PSContentExplorerException(
               IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
   }

   /**
    * Returns the search identified by the supplied search id.
    * 
    * @param searchid the search id, may not be <code>null</code> or empty.
    * 
    * @return search for the id given, may be <code>null</code> if not found.
    */
   public PSSearch getSearchById(String searchid)
   {
      if (searchid == null || searchid.trim().length() == 0)
         throw new IllegalArgumentException(
               "searchid may not be null or empty.");

      return (PSSearch) m_searches.get(searchid);
   }

   /**
    * Helper method to get the related content search.
    * 
    * @return PSSearch object corresponding to the Related Content Search. Could
    *         be <code>null</code> if there is a serious configuration
    *         problem, in which case the constructor will throw an exception.
    */
   public PSSearch getRcSearch()
   {
      return m_rcSearch;
   }
   
   /**
    * Retrieve and modify, or create a content id search. The search is stored
    * locally in the catalog and never persisted to the server.
    * @param contentid the content id, must never be <code>null</code> or empty
    * @return the content id search, never <code>null</code>
    * @throws PSCmsException if there's a problem creating the search
    */
   public PSSearch getContentIdSearch(String contentid) throws PSCmsException
   {
      if (contentid == null || contentid.trim().length() == 0)
      {
         throw new IllegalArgumentException("contentid may not be null or empty");
      }
      
      PSSearch rval = getSearchById("contentidinternalsearch");
      
      if (rval == null)
      {         
         rval = new PSSearch();
         PSKey locator = new PSKey(new String[]{"SEARCHID"}, true);
         locator.assign(new String[]{"contentidinternalsearch"});
         rval.setLocator(locator);
         PSSearchField sField = new PSSearchField(IPSConstants.PROPERTY_CONTENTID,
            IPSConstants.PROPERTY_CONTENTID, null, PSSearchField.TYPE_NUMBER,
            "Search on content id");
         sField.setFieldValue(PSSearchField.OP_EQUALS, contentid);
         rval.addField(sField);
         // Add to the catalog
         m_searches.put("contentidinternalsearch", rval);
      }
      else
      {
         Iterator iter = rval.getFields();
         while(iter.hasNext())
         {
            PSSearchField sField = (PSSearchField) iter.next();
            if (sField.getFieldName().equals(IPSConstants.PROPERTY_CONTENTID))
            {
               sField.setFieldValue(PSSearchField.OP_EQUALS, contentid);
            }
         }
      }
      
      return rval;
   }   

   /**
    * Helper method to get the search id of the given search object.
    * 
    * @param search the search whose id need to be extracted, must not be
    *           <code>null</code>.
    * 
    * @return the search id, never <code>null</code> or empty.
    */
   public static String getSearchId(PSSearch search)
   {
      if (search == null)
         throw new IllegalArgumentException("search must not be null");

      return search.getLocator()
            .getPart(search.getLocator().getDefinition()[0]);
   }

   /**
    * Helper method to get the empty search document for the new search.
    * 
    * @return the document, never <code>null</code> or empty.
    */
   public Document getEmptySearchDoc()
   {
      return m_emptySearchDoc;
   }

   /**
    * Helper method to replace the new search with the supplied search.
    * 
    * @param search with which the new search is to be replaced.  If 
    * {@link PSSearch#useExternalSearch()} returns <code>true</code> and an
    * external search engine is not available, the search is converted to an
    * internal search before being set.  
    */
   public void setEmptySearch(PSSearch search)
   {
      if (!m_isFtsEnabled)
         search.convertToInternal();
      m_searches.put(EMPTY_SEARCHID, search);
   }

   /**
    * Helper method to create an empty search document and add a search to the
    * catalog for the newly created empty search document. This search will be
    * created on the fly. We take the default search and change the searchid to
    * a large number and create another search from it and add it to the
    * searches.
    * 
    * @param searchElem the search element from which the search need to be
    *           created.
    * @return Document newly created document for the default search.
    * 
    * @throws PSContentExplorerException When it is not able to create a search
    *            from the supplied search element. This should not happen as by
    *            the time it comes here a search has already been created from
    *            this element.
    */
   private Document createAndAddEmptySearch(Element searchElem)
         throws PSContentExplorerException
   {
      if (searchElem == null)
         throw new IllegalArgumentException("search element must not be null");

      Document emptySearchDoc = PSXmlDocumentBuilder.createXmlDocument();
      try
      {
         Node emptyNode = emptySearchDoc.importNode(searchElem, true);
         emptySearchDoc.appendChild(emptyNode);
         NodeList nl = emptySearchDoc.getElementsByTagName(ELEM_SEARCHID);
         Node sid = null;
         for (int ii = 0; ii < nl.getLength(); ii++)
         {
            sid = nl.item(ii).getFirstChild();
            if (sid instanceof Text)
            {
               ((Text) sid).setData(EMPTY_SEARCHID);
            }
         }
         PSSearch emptySearch = new PSSearch(emptySearchDoc
               .getDocumentElement());
         boolean wasExternal = emptySearch.useExternalSearch();
         setEmptySearch(emptySearch);
         
         // re-serialize back to xml if we've converted it to internal
         if (wasExternal && !emptySearch.useExternalSearch())
         {
            emptySearchDoc = PSXmlDocumentBuilder.createXmlDocument();
            emptySearchDoc.appendChild(emptySearch.toXml(emptySearchDoc));
         }
      }
      catch (PSCmsException ex)
      {
         throw new PSContentExplorerException(
               IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException ex)
      {
         throw new PSContentExplorerException(
               IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
      return emptySearchDoc;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuffer rval = new StringBuffer(80);
      rval.append("PSSearchViewCatalog: ");
      Iterator iter = m_searches.values().iterator();
      while (iter.hasNext())
      {
         PSSearch search = (PSSearch) iter.next();
         rval.append(search.getLocator().getPart() + "("
               + search.getDisplayName() + ")");
         if (iter.hasNext()) rval.append(", ");
      }
      return rval.toString();
   }

   /**
    * Registry of all search view objects cataloged, setup as part of search
    * loading and never <code>null</code>, empty after that.
    */
   private Map m_searches = new HashMap();
   
   /**
    * Holds a reference to the first related content search found. Initialized
    * in {@link #loadSearches()} and never modified afterward. Might be 
    * <code>null</code> if there is an error in the configuration.
    */
   private PSSearch m_rcSearch = null;

   /**
    * The proxy to use to load searches from server, initialized in the ctor and
    * never <code>null</code> or modified after that.
    */
   private PSComponentProcessorProxy m_proxy;

   /**
    * The default new search document for content explorer searches.
    */
   private Document m_emptySearchDoc = null;

   /**
    * Set in ctor, then never modified. Indicates whether the full text 
    * search engine is active.
    */
   private boolean m_isFtsEnabled;
   
   /**
    * Constant for empty search id value. This must be a value that no other
    * search could possibly have.
    */
   public static final String EMPTY_SEARCHID = "-1";

   /**
    * Constant for empty SEARCHID element
    */
   public static final String ELEM_SEARCHID = "SEARCHID";
   
   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;
}
