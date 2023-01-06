/******************************************************************************
 *
 * [ PSSearchViewActionManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.cx;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.error.PSException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSHtmlParamDocument;
import com.percussion.util.PSStringOperation;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that manges all search related actions. Executes and loads search
 * results for a particular search criteria defined in search nodes and saves
 * the new searches.
 */
@SuppressWarnings("unchecked")
public class PSSearchViewActionManager
{
   static Logger log = Logger.getLogger(PSSearchViewActionManager.class);
   
   /**
    * Constructs the manager with the supplied connection objects to server
    * to perform search requests. Loads the current searches/views available on
    * server. The search criteria is different for 'CX' and 'IA' modes. The
    * search mode defaults to {@link #MODE_CXSEARCH}. The search mode can be set
    * using {@link #setSearchMode(String)}.
    *
    * @param proxy the remote proxy to use to make requests to the server,
    * may not be <code>null</code>
    * @param m_remCataloger 
    * @param urlBase the base url of the applet to make url requests, may not be
    * <code>null</code>
    * @param userInfo the info about current user session, may not be <code>null
    * </code>
    */
   public PSSearchViewActionManager(
      PSComponentProcessorProxy proxy,
      PSRemoteCataloger remCataloger, URL urlBase,
      PSContentExplorerApplet applet)
      throws PSContentExplorerException
   {
      if(proxy == null)
         throw new IllegalArgumentException("proxy may not be null.");

      if(urlBase == null)
         throw new IllegalArgumentException("urlBase may not be null.");

      m_proxy = proxy;
      m_urlBase = urlBase;
      m_applet = applet;
      try
      {
         m_searchConfig = remCataloger.getSearchConfig();
         m_searchAvailable = m_searchConfig.isFtsEnabled();
      }
      catch (PSCmsException e)
      {
         log.error("Cannot get search config from server",e);
      }
      
      m_searchViewCatalog =
         new PSSearchViewCatalog(proxy, m_searchAvailable, m_applet);

      m_emptySearchDoc = m_searchViewCatalog.getEmptySearchDoc();
      if (makeDBLookupRequest(urlBase, DBLOOKUP_UPPER_URL))
      {
         if (makeDBLookupRequest(urlBase, DBLOOKUP_LOWER_URL))
            m_isDBCaseSensitive = false;
      }
   }

   public PSContentExplorerApplet getApplet()
   {
      return m_applet;
   }

   /**
    * Sets the current search mode of the manager. The set of display formats
    * available for search are different for each mode. The {@link
    * #MODE_IA_SEARCH} limits the search to a slot.
    *
    * @param mode the search mode, must be one of the <code>MODE_xxx</code>
    * values
    */
   public void setSearchMode(String mode)
   {
      if( !(MODE_CXSEARCH.equals(mode) || MODE_IASEARCH.equals(mode)) )
         throw new IllegalArgumentException("mode is not valid");

      m_searchMode = mode;
   }

   /**
    * Initialize supplied new search node to be ready to execute the new search
    * with supplied content id as the criteria of the query. Gets the search
    * represented by this node's search id and modifies its criteria to get the
    * items whose contentid exactly matches the supplied content id.
    *
    * @param searchNode the new search node to initialize, may not be <code>null
    * </code> and must have search id defined.
    * @param contentid the contentid to use as search criteria, may not be
    * <code>null</code> or empty.
    */
   public void initContentIdSearch(PSNode searchNode, String contentid) 
   {
      if(searchNode == null)
         throw new IllegalArgumentException("searchNode may not be null.");

      if(contentid == null || contentid.trim().length() == 0)
         throw new IllegalArgumentException(
         "contentid may not be null or empty.");
      
      PSSearch contentItemSearch;
      try
      {
         contentItemSearch = m_searchViewCatalog.getContentIdSearch(contentid);
         searchNode.setSearchId(m_searchViewCatalog
               .getSearchId(contentItemSearch));
         setAsInitialized(searchNode);
      }
      catch (PSCmsException e)
      {
         throw new RuntimeException("Severe error while creating adhoc search",
               e);
      }
   }

   /**
    * saves the new search to the server. Delegates to search processor
    * proxy.
    * @param    search new  search to save.
    * @param name name of the search, must not be <code>null</code> or
    * <code>empty</code>
    */
   public PSSearch saveNewSearch(PSNode searchNode, String name, int showTo)
      throws PSContentExplorerException
   {
      if(searchNode == null)
         throw new IllegalArgumentException("searchNode must not be empty");

      if(name == null || name.trim().length() < 1)
         throw new IllegalArgumentException("name must not be null or empty");

      String searchid = searchNode.getProperties().getProperty(
         IPSConstants.PROPERTY_SEARCHID);
      PSSearch search = m_searchViewCatalog.getSearchById(searchid);

      PSSearch searchNew = (PSSearch)search.clone();
      searchNew.setDisplayName(name);
      //replace all spaces with '_'
      while(name.indexOf(' ') != -1)
         name = PSStringOperation.replaceChar(name, ' ', '_');

      searchNew.setInternalName(name);
      searchNew.clearCXNewSearch();
      searchNew.clearAADNewSearch();
      searchNew.setType(PSSearch.TYPE_USERSEARCH);
      
      if(showTo==PSSearch.SHOW_TO_USER)
      {
         searchNew.setShowTo(
            showTo,
            m_applet.getUserInfo().getUserName());
      }
      else
      {
         String community = PSSearch.PROP_COMMUNITY_ALL;
         if (showTo == PSSearch.SHOW_TO_COMMUNITY)
         {
            community =
                  m_applet.getUserInfo().getCommunityId() + "";
         }
         /*
          * If the supplied community is not All communities(-1) then we need to
          * remove the sys_community property.
          */
         if(!community.equalsIgnoreCase(PSSearch.PROP_COMMUNITY_ALL))
         {
            searchNew.removeProperty(PSSearch.PROP_COMMUNITY,null);
         }
         searchNew.setShowTo(showTo, community);
      }
      return saveSearch(searchNew);
   }

   /**
    * Saves the supplied search to the server and recatalogs the searches if we
    * are adding a new search. Search state can be found from its state of
    * persistence.
    *
    * @param search the search to save, may not be <code>null</code>
    *
    * @return the saved search, never <code>null</code>.
    *
    * @throws PSContentExplorerException if an error happens saving the search.
    */
   public PSSearch saveSearch(PSSearch search)
      throws PSContentExplorerException
   {
      if(search == null)
         throw new IllegalArgumentException("search may not be null.");

      try
      {
         boolean isNew = !search.isPersisted();

         PSSaveResults results = m_proxy.save(new IPSDbComponent[]{search});
         search = (PSSearch)results.getResults()[0];

         //refresh the catalog once new search is saved
         if(isNew)
         {
            m_searchViewCatalog.loadSearches();
         }

         return search;
      }
      catch(PSException ex)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
   }

   /**
    * Removes the saved searches represented by the supplied node list from
    * the system and saved search category.
    *
    * @param nodeList list of nodes representing saved searches,  may not be
    * <code>null</code>, may be <code>empty</code> and must be of type
    * <code>TYPE_SAVED_SEARCH</code>. Nothing happens if <code>empty</code>.
    *
    * @throws PSCmsException if an error happens while processing the request.
    */
   public void delete(Iterator nodeList)
      throws PSCmsException
   {
      if(nodeList == null )
         throw new IllegalArgumentException("nodeList must not be null");

      if(nodeList.hasNext())
      {
         PSNode node = null;
         String searchid = null;
         List list = new ArrayList();
         PSSearch searchObj = null;
         while(nodeList.hasNext())
         {
            node = (PSNode)nodeList.next();
            searchid = node.getProperties().getProperty(
               IPSConstants.PROPERTY_SEARCHID);
            if(searchid == null || searchid.trim().length()<1)
               continue;
            searchObj = m_searchViewCatalog.getSearchById(searchid);
            if(searchObj != null)
               list.add(searchObj);
         }
         //Convert the list to array
         PSSearch comp[] = (PSSearch[])list.toArray(new PSSearch[list.size()]);
         m_proxy.delete(comp);
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
      return m_searchViewCatalog.getSearchById(searchid);
   }

   /**
    * Gets the display formats available for searches and views based on the
    * current search mode set with the manager.
    *
    * @return the list of display formats, never <code>null</code> or empty.
    */
   public Iterator getDisplayFormats()
   {
      Iterator dispFormats;

      PSDisplayFormatCatalog dispFormatCatalog =
         m_applet.getActionManager().getDisplayFormatCatalog();
      if(m_searchMode.equals(MODE_CXSEARCH))
         dispFormats = dispFormatCatalog.getViewsSearchDisplayFormats();
      else
         dispFormats = dispFormatCatalog.getRcDisplayFormats();

      return dispFormats;
   }

   /**
    * Gets the displayformat identified by the supplied format id from the
    * avialable list of folder formats.
    *
    * @param formatid id of the display format to get, may not be <code>null
    * </code> or empty.
    * @param getDefault supply <code>true</code> to get the first one in the
    * available list if the supplied formatid is not found.
    *
    * @return the display format, may be <code>null</code> if the getDefault is
    * <code>false</code>
    */
   public PSDisplayFormat getDisplayFormatById(String formatid,
      boolean getDefault)
   {
      if(formatid == null || formatid.trim().length() == 0)
         throw new IllegalArgumentException(
            "formatid may not be null or empty.");

      PSDisplayFormat match = null;
      Iterator iter = getDisplayFormats();
      while(iter.hasNext())
      {
         PSDisplayFormat format = (PSDisplayFormat)iter.next();
         String id = String.valueOf(format.getDisplayId());
         if(id.equals(formatid))
         {
            match = format;
            break;
         }
      }

      if(match == null && getDefault)
         match = (PSDisplayFormat)getDisplayFormats().next();

      return match;
   }

   /**
    * Loads the children of the supplied search or view node by executing the
    * search criteria defined on the node. The required properties of node are:
    * <ol>
    * <li>Any search mode - search id and display format id</li>
    * <li>IA search mode - slot id in addition to the common properties</li>
    * </ol>
    * If there are any dirty child nodes, only the dirty nodes are reloaded.
    * 
    * @param searchNode the search or view node, may not be <code>null</code>
    * and must contain required properties.
    *
    * @return the list of search results that are children of search node, never
    * <code>null</code>, may be empty. An empty iterator if the node is is a new
    * search node and not yet initialized.
    *
    * @throws PSContentExplorerException if an error happens executing search.
    */
   public Iterator loadChildren(PSNode searchNode)
      throws PSCmsException, PSContentExplorerException
   {
      if(searchNode == null || !searchNode.isSearchType())
      {
         throw new IllegalArgumentException(
            "searchNode must not be null and represent a search or view node");
      }
      
      // Call listeners for start of search
      for (Iterator iter = m_searchListenerList.iterator(); iter.hasNext();)
      {
         IPSSearchListener listener = (IPSSearchListener) iter.next();
         listener.searchInitiated(searchNode);
      }
      
      try
      {
         //If it is an empty search, no children need to be loaded.
         if(isInitialized(searchNode) && !searchNode.isOfType(PSNode.TYPE_EMPTY_SRCH))
         {
            // first check if we have a display format option
            // if not then use the one supplied by the search node
            String formatid = m_applet.getDisplayFormatIdFromOptions(searchNode);
            if (formatid != null)
               searchNode.setDisplayFormatId(formatid);
            else
               formatid = searchNode.getDisplayFormatId();
         
            String searchId = searchNode.getSearchId();
         
            if(formatid.trim().length() == 0 ||
               searchId.trim().length() == 0)
            {
               throw new IllegalArgumentException(
                  "search node is not set with display format or search ids");
            }
         
            String slotId = searchNode.getSlotId();
            if( m_searchMode.equals(MODE_IASEARCH) )
            {
               if(slotId.trim().length() == 0)
               {
                  throw new IllegalArgumentException(
                     "search node is not set with slot id to execute related " +
                     "content search");
               }
            }
         
            PSDisplayFormat format = getDisplayFormatById(formatid, true);
         
            PSExecutableSearch searchEx = null;
            PSSearch search = m_searchViewCatalog.getSearchById(searchId);
            try
            {
               if(search.isCustomSearch() || search.isCustomView())
               {
                  String sUrl = search.getUrl();
                  if(sUrl==null || sUrl.length()<1)
                  {
                     throw new IllegalArgumentException(
                        "A custom search or view must have URL specified");
                  }
                  Document resultDoc = null;
                  String result = null;
                  try
                  {
                     //For custom searches we always post the parameters as input XML
                     //document
                     if(search.isCustomSearch())
                     {
                        Map namevalues = new HashMap();
                        Iterator fields = search.getFields();
                        PSSearchField field = null;
                        Object obj = null;
                        while(fields.hasNext())
                        {
                           field = (PSSearchField)fields.next();
                           obj = field.getFieldValues();
                           //Do not include any null or empty fields
                           if(obj != null && obj.toString().trim().length() > 0)
                           {
                              namevalues.put(field.getFieldName(),
                                 field.getFieldValues());
                              namevalues.put(field.getFieldName() +
                                 PSHtmlParamDocument.OPERATOR_SUFFIX,
                                 field.getOperator());
                           }
                        }
                        // Semd max results so the app could limit results
                        if (search.getMaximumResultSize() > 0)
                        {
                           namevalues
                                 .put(
                                       IPSHtmlParameters.SYS_MAXIMUM_SEARCH_RESULTS,
                                       Integer.toString(search
                                             .getMaximumResultSize()));
                        }
                        PSHtmlParamDocument paramDoc =
                           new PSHtmlParamDocument(namevalues);
                        result = m_applet.getActionManager().postXmlData(sUrl,
                           paramDoc.getXmlString());
                     }
                     else
                     {
                        Map params = new HashMap();
                        result = 
                           m_applet.getActionManager().postData(sUrl, params);
                     }
                  }
                  catch(PSException e)
                  {
                     throw new PSContentExplorerException(
                        e.getErrorCode(), e.getErrorArguments());
                  }
                  resultDoc = PSXmlDocumentBuilder.createXmlDocument(
                     new StringReader(result), false);
                  NodeList nl = resultDoc.getElementsByTagName("Item");
                  Element item = null;
                  Set idList = new HashSet();
                  Set typeList = new HashSet();
                  String temp = null;
                  for(int i=0; nl!=null && i<nl.getLength(); i++)
                  {
                     String typeid;
                     item = (Element)nl.item(i);
                     temp = item.getAttribute("sys_contentid").trim();
                     typeid = item.getAttribute("sys_contenttypeid").trim();
                     if (typeid.length() > 0)
                     {
                        typeList.add(new Integer(typeid));
                     }
                     idList.add(new Integer(temp));
                     // Stop extracting results after max
                     if (search.getMaximumResultSize() > 0 && 
                           idList.size() >= search.getMaximumResultSize()) break;
                  }
                  if(idList.isEmpty())
                  {
                     searchNode.setChildren(PSIteratorUtils.emptyIterator());
                     return searchNode.getChildren();
                  }
                  List retIdList = new ArrayList();
                  retIdList.addAll(idList);
                  searchEx = new PSExecutableSearch(m_urlBase, format, retIdList,
                     typeList, search, m_applet);
               }
               else
               {
                  search = m_searchViewCatalog.getSearchById(searchId);
                  searchEx = new PSExecutableSearch(m_urlBase, format, search, m_applet);
               }
               String type = searchNode.getType();
               boolean includeFolders = type.equals(PSNode.TYPE_NEW_SRCH)
                     || type.equals(PSNode.TYPE_SAVE_SRCH)
                     || type.equals(PSNode.TYPE_STANDARD_SRCH);
               searchEx.executeSearch(searchNode, includeFolders, true);
            }
            catch(IOException ioe)
            {
               throw new PSContentExplorerException(
                  IPSContentExplorerErrors.GENERAL_ERROR, ioe.getLocalizedMessage());
            }
            catch(SAXException sae)
            {
               throw new PSContentExplorerException(
                  IPSContentExplorerErrors.GENERAL_ERROR, sae.getLocalizedMessage());
            }
         }
         else //If not initialized simply set empty children
         {
            searchNode.setChildren(PSIteratorUtils.emptyIterator());
         }
      }
      finally
      {
         // Call listeners for end of search
         for (Iterator iter = m_searchListenerList.iterator(); iter.hasNext();)
         {
            IPSSearchListener listener = (IPSSearchListener) iter.next();
            listener.searchCompleted(searchNode);
         }
      }

      return searchNode.getChildren();
   }

   /**
    * Checks whether the supplied search node is initialized or not to execute
    * the search. Checks the value of <code>INITIALIZED</code> to '1'.
    *
    * @return <code>true</code> if the search node represents a new search
    * and its <code>INITIALIZED</code> property set to '1' or it is a saved
    * search or view, otherwise <code>false</code>
    */
   private boolean isInitialized(PSNode searchNode)
   {
      boolean initialized = true;

      if(ms_nodeTypesInitializable.contains(searchNode.getType()))
      {
         PSProperties props = searchNode.getProperties();
         if(props == null ||
            !props.getProperty(PROPERTY_INITIALIZED, INITIALIZED_FALSE).
            equals(INITIALIZED_TRUE))
         {
            initialized = false;
         }
      }
      return initialized;
   }

   /**
    * Sets the supplied new search node as initialized.
    *
    * @param searchNode the search node, may not be <code>null</code> and must
    * be of type <code>PSNode.TYPE_NEW_SRCH</code>
    */
   public static void setAsInitialized(PSNode searchNode)
   {
      if(searchNode == null || !isNodeInitializable(searchNode))
         throw new IllegalArgumentException("searchNode may not be null and "
            + "must be a an initializable new search node");

      searchNode.setProperty(PROPERTY_INITIALIZED, INITIALIZED_TRUE);
   }

   /**
    * Easy method to find if the supplied node is initializable.
    * @param node node under test, must not be <code>null</code>
    * @see ms_nodeTypesInitializable
    */
   static public boolean isNodeInitializable(PSNode node)
   {
      if(node == null)
         throw new IllegalArgumentException("node must not be null");
      return ms_nodeTypesInitializable.contains(node.getType());
   }

   /**
    * Determines whether the database treats the text data in case-sensitve
    * manner or not.
    *
    * @return <code>true</code> if the database performs case-sensitive
    * comparison for columns containing text data, <code>false</code> otherwise.
    */
   public boolean isDBCaseSensitive()
   {
      return m_isDBCaseSensitive;
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
    * Helper method to set the new search with the current search.
    *
    * @param search current PSSearch Object
    *
    */
   public void setEmptySearch(PSSearch search)
   {
      m_searchViewCatalog.setEmptySearch(search);
   }

   /**
    * Helper method to get the related content search.
    *
    * @return PSSearch object corresponding to Related Content Search
    *
    */
   public PSSearch getRcSearch()
   {
      return m_searchViewCatalog.getRcSearch();
   }

   /**
    * Add a search listener to the list of listeners.
    * @param l a listener, must never be <code>null</code>.
    */
   public void addSearchListener(IPSSearchListener l)
   {
      if (l == null)
      {
         throw new IllegalArgumentException("l must never be null");
      }
      m_searchListenerList.add(l);
   }
   
   /**
    * Call all the {@link IPSSearchListener#searchReset()} methods.
    */
   public void resetSearchListeners()
   {
      for (Iterator i = m_searchListenerList.iterator(); i.hasNext();)
      {
         IPSSearchListener listener = (IPSSearchListener) i.next();
         listener.searchReset();
      }
   }
   
   /**
    * Remove a search listener from the list of listeners.
    * @param l a listener, must never be <code>null</code>.
    */
   public void removeSearchListener(IPSSearchListener l)
   {
      if (l == null)
      {
         throw new IllegalArgumentException("l must never be null");
      }      
      m_searchListenerList.remove(l);
   }
   
   /**
    * Makes a request to the specified URL and parses the response document.
    *
    * @param urlBase the code base for the Content Explorer applet, assumed not
    * <code>null</code>
    *
    * @param resourceName the url of the resource relative to
    * <code>urlBase</code>, assumed not <code>null</code> and non-empty
    *
    * @return <code>true</code> if the response document has an integer value
    * for <code>ATTR_CASE_SENSITVE</code> attribute, otherwise false.
    *
    * @throws PSContentExplorerException if any IO error occurs getting the
    * response document when making the request to the specified resource or
    * parsing the response document
    */
   private boolean makeDBLookupRequest(URL urlBase, String resourceName)
      throws PSContentExplorerException
   {
      boolean ret = false;
      try
      {
         URL url = new URL(urlBase, resourceName);
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument(
            url.openStream(), false);

         if ((respDoc != null) && (respDoc.getDocumentElement() != null))
         {
            String strCaseSensitive =
               respDoc.getDocumentElement().getAttribute(ATTR_CASE_SENSITVE);
            if ((strCaseSensitive != null) &&
               (strCaseSensitive.trim().length() > 0))
            {
               try
               {
                  Integer.parseInt(strCaseSensitive);
                  ret = true;
               }
               catch(NumberFormatException ex)
               {
               }
            }
         }
      }
      catch(IOException ex)
      {
         ex.printStackTrace();
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }
      catch(SAXException ex)
      {
         ex.printStackTrace();
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.GENERAL_ERROR, ex.getLocalizedMessage());
      }

      return ret;
   }

   /**
    * Processor proxy. Initialized in the constructor, never <code>null</code>
    * after that.
    */
   private PSComponentProcessorProxy m_proxy = null;

   /**
    * Catalog of search views. Initilized in the constructor. Never
    * <code>null</code> after that.
    */
   private PSSearchViewCatalog m_searchViewCatalog = null;

   /**
    * The current search mode of this manager, initialized to <code>
    * MODE_CXSEARCH</code> and can be modified through call to {@link
    * #setSearchMode(String)}. Never <code>null</code>
    */
   private String m_searchMode = MODE_CXSEARCH;
   
   /**
    * Catalog of search Configuration Initilized in the constructor. Never
    * <code>null</code> after that. 
    */
   private PSSearchConfig m_searchConfig = null;
   
   public PSSearchConfig getSearchConfig()
   {
      return m_searchConfig;
   }

   private boolean m_searchAvailable = false;
   
   public boolean isSearchAvailable()
   {
      return m_searchAvailable;
   }

   /**
    * The base url of the applethost, initialized in the ctor and never <code>
    * null</code> or modified after that.
    */
   private URL m_urlBase = null;

   /**
    * A reference to the applet, inititialized in the ctor, never <code>null
    * </code> after that, used to save and restore display options.
    */
   private PSContentExplorerApplet m_applet = null;

   /**
    * Constant for Content Explorer search mode
    */
   static public final String MODE_CXSEARCH = "cxsearch";

   /**
    * Constant for Item Assembly search mode
    */
   static public final String MODE_IASEARCH = "iasearch";

   /**
    * The property whose value indicates whether the new search is initialized
    * or not. A value of '0' indicates not yet initialized and '1' indicates as
    * initialized.
    */
   private static final String PROPERTY_INITIALIZED = "isInitialized";

   /**
    * The value to indicate that <code>PROPERTY_INITIALIZED</code> property of
    * new search node is true.
    */
   private static final String INITIALIZED_TRUE = "1";

   /**
    * The value to indicate that <code>PROPERTY_INITIALIZED</code> property of
    * new search node is false.
    */
   private static final String INITIALIZED_FALSE = "0";

   /**
    * List of node types that can be set with initialized falg. These are
    * typically search nodes that appear under search results category in the
    * Content Explorer. This falg is useful to hide the results until user
    * actually has edited (or at least has seen) the query.
    */
   static public List ms_nodeTypesInitializable = new ArrayList();

   /**
    * Stores the case sensitivity of the database. If <code>true</code> then
    * the database is case-sensitive, otherwise not.
    * Defaults to <code>true</code>. Set to <code>false</code> in the ctor if
    * the database is not case-sensitive. Never modified after that.
    */
   private static boolean m_isDBCaseSensitive = true;

   /**
    * URL for the resource relative to the Content Explorer applet codebase,
    * which performs a query similar to:
    * SELECT NEXTNR FROM NEXTNUMBER WHERE UPPER(KEYNAME) = UPPER('PSX_PROPERTIES')
    * This URL should return single row irrespective of the case-sensitivity
    * of the database.
    */
   private static final String DBLOOKUP_UPPER_URL =
      "sys_psxCms/DBLookupUpper.xml";

   /**
    * URL for the resource relative to the Content Explorer applet codebase,
    * which performs a query similar to:
    * SELECT NEXTNR FROM NEXTNUMBER WHERE KEYNAME = 'psx_properties'
    * This URL will return a row only if the database is case-insensitive.
    */
   private static final String DBLOOKUP_LOWER_URL =
      "sys_psxCms/DBLookupLower.xml";

   /**
    * attribute of the root element of the response document obtained by making
    * a request to either <code>DBLOOKUP_LOWER_URL</code> or
    * <code>DBLOOKUP_UPPER_URL</code> resource
    */
   private static final String  ATTR_CASE_SENSITVE = "caseSensitive";

   /**
    * Member variable to hold the clean search document. Initialized in ctor
    * never <code>null</code> after that.
    */
   private Document m_emptySearchDoc = null;
   
   /**
    * This holds a list of {@link IPSSearchListener} objects to call before
    * and after a search is executed. Only modified by 
    * {@link #addSearchListener(IPSSearchListener)} and
    * {@link #removeSearchListener(IPSSearchListener)}. Never
    * <code>null</code> but may be empty.
    */
   private List m_searchListenerList = new ArrayList();

   /**
    * static initializer for {@link #ms_nodeTypesInitializable}.
    */
   static
   {
      ms_nodeTypesInitializable.add(PSNode.TYPE_STANDARD_SRCH);
      ms_nodeTypesInitializable.add(PSNode.TYPE_CUSTOM_SRCH);
      ms_nodeTypesInitializable.add(PSNode.TYPE_NEW_SRCH);
      ms_nodeTypesInitializable.add(PSNode.TYPE_EMPTY_SRCH);
      //delete or comment the following line to be backward comapatible
//      ms_nodeTypesInitializable.add(PSNode.TYPE_SAVE_SRCH);
   }
}
