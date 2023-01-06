/******************************************************************************
 *
 * [ PSExecutableSearch.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.cx;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSSFields;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.ws.PSWSExecutableSearch;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.PSCommonSearchUtils;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSEntrySet;
import com.percussion.util.PSRemoteAppletRequester;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The utility class to hold search criteria, build search request and execute
 * it.
 */
@SuppressWarnings("unchecked")
public class PSExecutableSearch extends PSWSExecutableSearch
{
   /**
    * Label for uncategorized 
    */
   private static final String _UNCATEGORIZED_ = "[Uncategorized]";
   
   /**
    * Construct an executable search with supplied search criterias.
    *
    * @param documentBaseURL the URL of the location to post the data to make
    * a search request, may not be <code>null</code>
    * @param displayFormat the display format that need to be applied to search
    * results, may not be <code>null</code>
    * @param search the search object that defines the criteria and maximum
    * results to obtain, may not be <code>null</code>
    */
   public PSExecutableSearch(
      URL documentBaseURL,
      PSDisplayFormat displayFormat,
      PSSearch search, PSContentExplorerApplet applet)
   {
      if (documentBaseURL == null || documentBaseURL.getPath().length() == 0)
         throw new IllegalArgumentException(
            "documentBaseURL must not be null or empty");

      if (displayFormat == null)
         throw new IllegalArgumentException("displayFormat must not be null");

      if (search == null)
         throw new IllegalArgumentException("search must not be null");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      m_applet = applet;

      init(documentBaseURL, null, search, displayFormat, null, null);
   }

   /**
    * Construct an executable search with supplied list of content ids as search
    * criteria. Calls {@link #PSExecutableSearch(URL, PSDisplayFormat, List,
    * Collection, PSSearch)
    * PSExecutableSearch(documentBaseURL, displayFormat, contentIdList, null,
    * null)}.
    */
   public PSExecutableSearch(URL documentBaseURL,
         PSDisplayFormat displayFormat, List contentIdList, PSContentExplorerApplet applet) {
      this(documentBaseURL, displayFormat, contentIdList, null, null, applet);
   }
      
   /**
    * Construct an executable search with supplied list of content ids as search
    * criteria.
    * 
    * @param documentBaseURL
    *           the URL of the location to post the data to make a search
    *           request, may not be <code>null</code>
    * @param displayFormat
    *           the display format that need to be applied to search results,
    *           may not be <code>null</code>
    * @param contentIdList
    *           the list of content ids to search on, may not be
    *           <code>null</code> or empty.
    * @param contentTypeIdList
    *           An optional list of contenttypeids to search, may be
    *           <code>null</code> or empty.
    * @param search the search object that defines the criteria and maximum
    *           results to obtain, may be <code>null</code>
    */
   public PSExecutableSearch(
      URL documentBaseURL,
      PSDisplayFormat displayFormat,
      List contentIdList,
      Collection contentTypeIdList,
      PSSearch search, PSContentExplorerApplet applet)
   {
      if (documentBaseURL == null || documentBaseURL.getPath().length() == 0)
         throw new IllegalArgumentException(
            "documentBaseURL must not be null or empty");

      if (displayFormat == null)
         throw new IllegalArgumentException("displayFormat must not be null");

      if (contentIdList == null || contentIdList.isEmpty())
         throw new IllegalArgumentException(
            "contentIdList must not be null or empty");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      m_applet = applet;

      init(documentBaseURL, null, search, displayFormat, 
         contentIdList, contentTypeIdList);
   }


   /**
    * Construct an executable search with supplied list of content ids as search
    * criteria and a list of result column names.
    *
    * @param documentBaseURL the URL of the location to post the data to make
    * a search request, may not be <code>null</code>
    * @param columnNames the names of the columns to include in the search
    * results as <code>String</code> objects, may not be <code>null</code>.
    * @param contentIdList the list of content ids to search on, may not be
    * <code>null</code> or empty.
    */
   public PSExecutableSearch(
      URL documentBaseURL,
      List columnNames,
      List contentIdList, PSContentExplorerApplet applet)
   {
      if (documentBaseURL == null || documentBaseURL.getPath().length() == 0)
         throw new IllegalArgumentException(
            "documentBaseURL must not be null or empty");

      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (contentIdList == null || contentIdList.isEmpty())
         throw new IllegalArgumentException(
            "contentIdList must not be null or empty");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      m_applet = applet;

      init(documentBaseURL, columnNames, null, null, contentIdList, null);
   }

   /**
    * Method that replaces the global (not node specific) dynamic variable with
    * actual vale. Currently only comunity and locale fields are supported.
    * @param value the parameter, if dynamic starts with "$$". Assumed not
    * <code>null</code> or empty.
    * @return unmodified value of the parameter supplied if it is not dynamic
    * variable. Substituted value otherwise. Never <code>null</code> or empty.
    */
   @Override
   protected String resolveDynamicValue(String value)
   {
      if (!value.startsWith("$$"))
         return value;
      String param = value.substring(2);
      if (param.equals(PROPERTY_COMMUNITY))
      {
         value =
            String.valueOf(
                  m_applet.getUserInfo().getCommunityId());
      }
      else if (param.equals(PROPERTY_LANG))
      {
         value = m_applet.getUserInfo().getLocale();
      }
      else if (param.equals(PROPERTY_USERNAME))
      {
         value = m_applet.getUserInfo().getUserName();
      }
      return value;
   }

   /**
    * Intialize this object from the provided parameters.
    *
    * @param documentBaseURL the URL of the location to post the data to make
    * a search request, assumed not <code>null</code>.
    * @param columnNames the names of the columns to include in the search
    * results as <code>String</code> objects, assumed not <code>null</code> if
    * <code>displayFormat</code> is <code>null</code>.
    * @param search the search object that defines the criteria and maximum
    * results to obtain, assumed not <code>null</code> if 
    * <code>contentIdList</code> is <code>null</code>.
    * @param displayFormat Defines the results columns and format of the 
    * results, assumed not <code>null</code> if <code>columnNames</code> is
    * <code>null</code>.
    * @param contentIdList The list of contentids to search on, assumed not
    * <code>null</code> if <code>search</code> is <code>null</code>.
    * @param contentTypeIdList An optional list of contenttypeids to search,
    * may be <code>null</code> or empty. 
    */
   private void init(
      URL documentBaseURL,
      List columnNames,
      PSSearch search,
      PSDisplayFormat displayFormat,
      List contentIdList,
      Collection contentTypeIdList)
   {
      PSRemoteAppletRequester requester =
         new PSRemoteAppletRequester(m_applet.getHttpConnection(), documentBaseURL);
      if (columnNames == null)
         columnNames = getColumnNames(displayFormat);
      m_displayFormat = displayFormat;
      setContentTypeIdList(contentTypeIdList);

      if (m_applet.isContentRestrict() && search != null
            && search.isUserSearch())
      {
         setRestrictItems(true, 
               m_applet.getUserInfo().getCommunityId());
      }
         
      super.init(requester, columnNames, search, contentIdList);
   }

   /**
    * Add the given column to the desired output columns for the search.
    * 
    * @param columnName the column name, never <code>null</code> or empty, if
    *           already present in the list, this call has no effect.
    */
   public void addColumnName(String columnName)
   {
      if (columnName == null || StringUtils.isBlank(columnName))
      {
         throw new IllegalArgumentException(
               "columnName may not be null or empty");
      }
      if (! m_columnNames.contains(columnName))
      {
         m_columnNames.add(columnName);
      }

   }
   
   /**
    * Extracts a list of column names for the supplied display format.
    * 
    * @param displayFormat Provides the column names, assumed not 
    * <code>null</code>.
    * 
    * @return The list of column names as <code>String</code> objects, never
    * <code>null</code>.
    */
   private List getColumnNames(PSDisplayFormat displayFormat)
   {
      List result = new ArrayList();
      Iterator resultFieldIter = displayFormat.getColumns();
      while (resultFieldIter.hasNext())
      {
         PSDisplayColumn dc = (PSDisplayColumn)resultFieldIter.next();
         result.add(dc.getSource());
      }

      return result;
   }

   /**
    * Convenience method that calls 
    * {@link #executeSearch(PSNode, boolean, boolean)
    * executeSearch(<code>node</code>, false, true)}.
    */
   public List executeSearch(PSNode node) 
      throws PSContentExplorerException
   {
      return executeSearch(node, false, true);
   }
   
   /**
    * Executes the search based on the criteria specified in this object (search
    * and displayformat) and sets the search results as children of the supplied
    * node. Sets the display format of the supplied node to display its children
    * (search results).
    * 
    * @param node the node that represents the search results, may not be
    *           <code>null</code>. If this node contains slot id, that will
    *           be included in search criteria. If node contains dirty child
    *           nodes, then the search will only be applied to the dirty nodes,
    *           and the results of this search will be used to refresh the
    *           previous search results.
    * 
    * @param includeFolders If <code>true</code>, folders matching the search
    *           criteria will be returned in the results, otherwise, only items.
    * @param useFolderRefs If <code>true</code>, then reference nodes will be
    *           used for folders instead of regular folder nodes
    * @return a list of search results, never <code>null</code>, may be empty
    * 
    * @throws PSContentExplorerException if an error happens executing search.
    */
   public List executeSearch(PSNode node, boolean includeFolders,
         boolean useFolderRefs) 
      throws PSContentExplorerException
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null.");

      // see if only searching on dirty child item nodes
      boolean hasDirty = node.hasDirtyChildren();
      boolean hasDirtyItems = false;
      List contentIdList = new ArrayList();
      List<Integer> dirtyIds = new ArrayList<Integer>();
      List<Integer> refreshIds = null;
      List origContentIdList = getContentIdList();
      Iterator dirtyIter;
      Map origSearchState = null;
      
      boolean expandSynonyms = false;
      try
      {
         // get the default synonym expansion setting from the search config
         expandSynonyms = 
               m_applet.getApplet().getSearchConfig().isSynonymExpansionRequired();
      }
      catch (PSCmsException cmsex)
      {
         
         throw new PSContentExplorerException(
               IPSContentExplorerErrors.SEARCH_ERROR, cmsex.toString());
      }
           
      String[][] advancedProps = new String[][]
      {  // prop name, default prop value
         { PSCommonSearchUtils.PROP_SYNONYM_EXPANSION,
            expandSynonyms ? PSCommonSearchUtils.BOOL_YES :
                    PSCommonSearchUtils.BOOL_NO}
      };
      boolean originallyCaseSensitive = false;
      boolean addedObjectFilterField = false;
      try
      {
         if (hasDirty)
         {
            // get list of dirty item node ids
            Iterator nodes = node.getDirtyChildren(!node.isAnyFolderType());
            while (nodes.hasNext())
            {
               PSNode dirty = (PSNode)nodes.next();
               String strContentId = dirty.getContentId();
               if (!StringUtils.isBlank(strContentId))
               {
                  Integer contentId;
                  try
                  {
                     contentId = new Integer(dirty.getContentId());
                  }
                  catch (NumberFormatException e)
                  {
                     // not a searchable node, skip
                     continue;
                  }
                  
                  // a dirty id of -1 means we need to search for unknown adds                  
                  if (contentId.intValue() == -1)
                  {
                     hasDirty = false;
                     dirtyIds.clear();
                     refreshIds = new ArrayList<Integer>();
                     break;
                  }
                  dirtyIds.add(contentId);
               }
            }
            
            // if we didn't find dummy id, add all dirty
            if (refreshIds == null)
            {
               refreshIds = new ArrayList<Integer>();
               refreshIds.addAll(dirtyIds);
            }
            
            hasDirtyItems = !dirtyIds.isEmpty();
            
            // now get intersection of dirty ids and original search ids
            Set idSet = null;
            if (origContentIdList != null && !origContentIdList.isEmpty())
               idSet = new HashSet(origContentIdList);
            dirtyIter = dirtyIds.iterator();
            while (dirtyIter.hasNext())
            {
               Integer ctId = (Integer)dirtyIter.next();
               // add is no original ids or if in that set
               if (idSet == null || idSet.contains(ctId))
                  contentIdList.add(ctId);
            }
         }

         boolean partialIdSearch = !contentIdList.isEmpty();
         
         // if we had dirty item ids, but none to search on, skip search and 
         // just remove them later.  This means all dirty item nodes have been
         // removed from the results.  If we had dirty nodes, but none were 
         // existing items or folder, that should be a skip as well, as nothing
         // visible in the view was changed
         boolean skipDirtySearch = (hasDirty && !hasDirtyItems) || 
            (hasDirtyItems && !partialIdSearch);
         
         Document searchResultDoc = null;
         String slotid = node.getSlotId();
         boolean isRCSearch = (slotid.length() > 0);
         
         if (!skipDirtySearch)
         {
            // if didn't find any dirty item ids, add all specified ids
            if (!hasDirtyItems && (origContentIdList != null))
               contentIdList.addAll(origContentIdList);
               
            // perform the search
            Map params = new HashMap();
            
            if (m_displayFormat != null)
               params.put(IPSConstants.PROPERTY_DISPLAYFORMATID,
                  String.valueOf(m_displayFormat.getDisplayId()));
                  
   
            String searchType;
            if (isRCSearch)
            {
               searchType = "cxRCSearch";
               params.put("slotId", "" + slotid);
            }
            else
            {
               searchType = "cxSearch";
            }  
            params.put("cxSearch", searchType);
   
            if (m_search != null && PSSearch.SEARCH_MODE_SIMPLE.equals(
                  m_search.getProperty(PSSearch.PROP_SEARCH_MODE)))
            {
               /*
                * In simple mode, we need to use the default values for the
                * properties that appear in the advanced section of the 
                * dialog, but we don't want to lose that data because it
                * needs to remain in case they go to the advanced section
                * again. We accomplish this be setting the defaults now and
                * restoring the original values before exiting this method. 
                */
               origSearchState = new HashMap();
               for (int i=0; i < advancedProps.length; i++)
               {
                  String value = m_search.getProperty(advancedProps[i][0]);
                  if (value != null && value.trim().length() > 0)
                  {
                     origSearchState.put(advancedProps[i][0], value); 
                  }
                  if (advancedProps[i][1].length() > 0)
                  {
                     m_search.setProperty(advancedProps[i][0], 
                           advancedProps[i][1]);
                  }
                  else
                     m_search.removeProperty(advancedProps[i][0], null);
               }
               originallyCaseSensitive = m_search.isCaseSensitive();
            }
            if (includeFolders && m_search != null)
               addedObjectFilterField = addObjectFilterField();
            searchResultDoc = executeSearch(params, (!contentIdList.isEmpty() ? 
               contentIdList : null));
         }
         
         PSWSSearchResponse newRows = null;
         
         // if was not a full search, fixup previous results
         if (hasDirty)
         {
            // get current view contentds
            PSWSSearchResponse curRows = null;
            Document lastSearchResults = node.getSearchResults();
            if (lastSearchResults == null)
               curRows = new PSWSSearchResponse();
            else
               curRows = new PSWSSearchResponse(lastSearchResults);
            
            // create row list with dirty id rows removed
            List newRowList = new ArrayList();
            Set<Integer> dirtySet = new HashSet<Integer>(dirtyIds);
            Iterator rows = curRows.getRows();
            while (rows.hasNext())
            {
               IPSSearchResultRow row = (IPSSearchResultRow)rows.next();
               if (!dirtySet.contains(new Integer(row.getColumnValue(
                  IPSHtmlParameters.SYS_CONTENTID))))
               {
                  newRowList.add(row);
               }
            }
            
            // replace results with old rows plus any new rows
            if (!skipDirtySearch)
            {
               newRows = new PSWSSearchResponse(searchResultDoc);
               newRowList.addAll(PSIteratorUtils.cloneList(newRows.getRows()));
               newRows.setRows(newRowList);
            }
            else
            {
               // did no search, so use old results with the dirty rows removed
               curRows.setRows(newRowList);
               newRows = curRows;
               // need a result doc to get root from
               searchResultDoc = node.getSearchResults();
            }
            
            // replace search results doc to represent current contents
            Document tmpDoc = PSXmlDocumentBuilder.createXmlDocument();
            tmpDoc.appendChild(tmpDoc.importNode(
               searchResultDoc.getDocumentElement(), false));
            newRows.appendSearchResponseResults(tmpDoc);
            searchResultDoc = tmpDoc;
         }
         else
         {
            // did a full search, so use those results
            newRows = new PSWSSearchResponse(searchResultDoc);
            node.setTruncated(newRows.isTruncated());
         }
         
         applyDisplayFormat(node, newRows, m_displayFormat, isRCSearch, 
            useFolderRefs, refreshIds);
         
         // save results for next selective refresh processing
         node.setSearchResults(searchResultDoc);
         
         if(node.getChildren() == null)
            return new ArrayList();

         return PSIteratorUtils.cloneList(node.getChildren());
      }
      catch (Exception ex)
      {
         //Any known exception the code throws should be added here.
         if (ex instanceof PSException
            || ex instanceof ParserConfigurationException
            || ex instanceof SAXException
            || ex instanceof IOException
            || ex instanceof PSContentExplorerException)
         {
            String message = ex.toString();
            if (message.contains("<SearchResponse>"))
            {
               try
               {
                  int xml = message.indexOf("<");
                  Document doc = PSXmlDocumentBuilder.createXmlDocument(
                        new StringReader(message.substring(xml)), false);
                  NodeList results = doc.getElementsByTagName("com:Result");
                  if (results.getLength() < 1)
                  {
                     message = "Couldn't find result for search problem";
                  }
                  else
                  {
                     Element el = (Element) results.item(0);
                     message = PSStringUtils.compressWhitespace(
                           el.getTextContent());
                  }
               }
               catch (Exception e)
               {
                  // Fall through
               }
            }

            throw new PSContentExplorerException(
                IPSContentExplorerErrors.SEARCH_ERROR, message);
         }
         else
         {
            throw new RuntimeException(ex.getLocalizedMessage());
         }
      }
      finally
      {
         // restore the search to its previous state
         if (origSearchState != null)
         {
            for (int i=0; i < advancedProps.length; i++)
            {
               Object prop = origSearchState.get(advancedProps[i][0]);
               String value = null == prop ? null : prop.toString();
               if (null != value)
                  m_search.setProperty(advancedProps[i][0], value);
            }
            m_search.setCaseSensitive(originallyCaseSensitive);
         }
         if (addedObjectFilterField)
            removeObjectFilterField();
      }
   }

   /**
    * One of a pair of methods used to control whether folders are returned
    * with the results. If the {@link #PROPERTY_OBJECTTYPE} field is present,
    * then this method does nothing. Otherwise, it adds that field so that
    * all objects of type id &lt 3 are returned.
    * <p>The corresponding undo method is {@link #removeObjectFilterField()}.
    * 
    * @return <code>true</code> if a field is added, <code>false</code> 
    * otherwise.
    */
   private boolean addObjectFilterField()
   {
      if (m_search == null)
         throw new IllegalStateException("m_search has not been set yet.");
      
      Iterator fields = m_search.getFields();
      boolean found = false;
      while (fields.hasNext() && !found)
      {
         PSSearchField field = (PSSearchField) fields.next();
         if (field.getFieldName().equalsIgnoreCase(PROPERTY_OBJECTTYPE))
         {
            found = true;
         }
      }
      if (!found)
      {
         PSSearchField fld = new PSSearchField(PROPERTY_OBJECTTYPE, null, null, 
               PSSearchField.TYPE_TEXT, null);
         //we want items (id=1) and folders (id=2)
         fld.setFieldValue(PSSearchField.OP_LESSTHAN, "3");
         m_search.addField(fld);
      }
      return !found;
   }

   /**
    * If the search owned by this object contains the 
    * {@link #PROPERTY_OBJECTTYPE} field, it is removed.
    * <p>See {@link #addObjectFilterField()} for more details.
    *
    */
   private void removeObjectFilterField()
   {
      PSSFields fields = m_search.getFieldContainer();
      Iterator fieldIter = fields.iterator();
      while (fieldIter.hasNext())
      {
         PSSearchField field = (PSSearchField) fieldIter.next();
         if (field.getFieldName().equalsIgnoreCase(PROPERTY_OBJECTTYPE))
         {
            fields.remove(field);
            break;
         }
      }
   }
   
   /**
    * Applies the specified display format to the supplied search results 
    * document, converting the results to <code>PSNode</code> objects.  If no
    * display format is supplied, a flat list of nodes are created, using the
    * column name as each node's display name
    * 
    * @param baseNode The node to which the returned results are set as child
    * nodes, assumed not <code>null</code>.
    * @param resp The search response, contains the search results to convert,
    * assumed not <code>null</code>.
    * @param df The display format to use to convert the results, may be 
    * <code>null</code> in which case flat results are returned, using the
    * column name as the node's display name.
    * @param isRCSearch <code>true</code> if a related content search was
    * performed, <code>false</code> if not.  Used to determine which fields
    * should be converted to properties.
    * @param useFolderRefs if this is <code>true</code> then folders in the 
    * results are turned into folder ref nodes, if <code>false</code> then
    * folders become folder nodes instead.
    * @param dirtyIds A list of ids of dirty nodes that were searched, 
    * <code>null</code> if a dirty search was not performed, if empty, that 
    * means that all rows from the previous search are being used.  
    */
   private void applyDisplayFormat(
      PSNode baseNode,
      PSWSSearchResponse resp,
      PSDisplayFormat df,
      boolean isRCSearch, boolean useFolderRefs, List<Integer> dirtyIds)
   {
      /*
       * create copy of node to hold current children - use clone to get all
       * relevant properties. replace cloned with actual child nodes so that the
       * nav tree doesn't lose the link between PSNodes and PSTreeNodes. todo:
       * implement a copy ctor to do a shallow clone instead
       */
      PSNode previous = (PSNode) baseNode.clone();
      previous.setChildren(baseNode.getChildren());
      
      // clear current children
      baseNode.setChildren(null);

      // build lists of categories, flat fields, and table meta columns, as well
      // column display name map
      List categorized = new ArrayList();
      List flat = new ArrayList();
      List tableCols = new ArrayList();
      List colDefs = null; // will be null if no display format, or no columns
      Map colNames = new HashMap();
      
      if (df != null)
      {
         Iterator cols = df.getColumns();
         while (cols.hasNext())
         {
            PSDisplayColumn col = (PSDisplayColumn) cols.next();
            String name = col.getSource();
            String dispName = col.getDisplayName();
            colNames.put(name, dispName);
            if (col.isCategorized())
               categorized.add(name);
            else
            {
               flat.add(col.getSource());
               tableCols.add(new PSEntrySet(dispName, col.getRenderType()));
            }
         }

         // add table meta
         if (!tableCols.isEmpty())
            colDefs = tableCols;
      }
      else
      {
         flat.addAll(getColumnNames());
      }

      // if not categorized, set tablemeta on base node, otherwise will add to
      // lowest level categories.
      if (!categorized.isEmpty())
      {
         baseNode.clearChildrenDisplayFormat();
      }
      else
      {
         Iterator colDefIter = colDefs != null ? colDefs.iterator() : null;
         baseNode.setChildrenDisplayFormat(colDefIter);
      }

      /*
       * Generate a property set to add to node. The set is the static set 
       * for the search type combined with the display columns.
       */ 
      Set propList = null;
      if(isRCSearch)
         propList = new HashSet(ms_cxRCPropSet);
      else
         propList = new HashSet(ms_cxPropSet);
      Iterator colIter = colNames.keySet().iterator();
      /*
       * With Rhythmyx 5.6, every display column by internal name is eleigible
       * to be submitted to server if a server action is configured to do so by
       * specifying $params. So add all display column internal names to the
       * standard set too.
       */
      while (colIter.hasNext())
      {
         String colName = (String) colIter.next();
         propList.add(colName);
      }
         
      //Get the icon map
      
      Map<String,String> iconMap = getIconPathMap(resp);
      // walk each row and create a node for it
      Iterator rows = resp.getRows();
      String folder_type = useFolderRefs ? PSNode.TYPE_FOLDER_REF : 
         PSNode.TYPE_FOLDER;
      PSFolderActionManager folderMgr = m_applet.getApplet()
         .getActionManager().getFolderActionManager();
      while (rows.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow)rows.next();
         String title = row.getColumnDisplayValue("sys_title");
         String ctypeId = row.getColumnValue(PROPERTY_CONTENTTYPEID).toString();
         Integer contentId = new Integer(row.getColumnValue(
            PROPERTY_CONTENTID));

         String nodeType = 
            (ctypeId.equals(Integer.toString(PSFolder.FOLDER_CONTENT_TYPE_ID)))
            ? folder_type : PSNode.TYPE_ITEM;
         
         // if a folder (not folder ref), then see if it's a site folder
         if (nodeType.equals(PSNode.TYPE_FOLDER))
         {
            nodeType = folderMgr.getFolderType(contentId, title, baseNode);
         }

         String perm = row.getColumnValue(IPSHtmlParameters.SYS_PERMISSIONS);
         if (StringUtils.isBlank(perm))
         {
            perm = "-1";
         }
         
         
         PSNode itemNode = null;
         // find existing node if it's clean
         if (dirtyIds != null && !dirtyIds.contains(contentId))
         {
            // don't recurse folder children
            boolean recurse = !previous.isAnyFolderType();
            itemNode = previous.findChildNode(String.valueOf(contentId), 
               nodeType, recurse);
         }
         
         if (itemNode == null)
         {
            String iconKey = iconMap.get(contentId.toString());
            itemNode = new PSNode(title, title, nodeType, null, iconKey, false, 
               Integer.parseInt(perm));
            
            // add props and cols 
            Map rowData = new HashMap();
            Iterator cols = row.getColumnNames().iterator();
            while (cols.hasNext())
            {
               String name = cols.next().toString();
               if (propList.contains(name))
               {
                  itemNode.setProperty(name, row.getColumnValue(name));
               }

               if (flat.contains(name))
               {
                  // get the display name of the column
                  String colName = (String)colNames.get(name);
                  if (colName == null)
                     colName = name;
                  rowData.put(colName, row.getColumnDisplayValue(name));
               }
            }

            if (!rowData.isEmpty())
               itemNode.setRowData(rowData);            
         }
         
         // add to proper category node or else to base node
         if (!categorized.isEmpty())
         {
            // need to build nested groups of results
            PSNode parent = baseNode;
            Iterator catIter = categorized.iterator();
            while (catIter.hasNext())
            {
               String field = (String)catIter.next();
               String catVal = row.getColumnDisplayValue(field);
               String catName = catVal;
               //String catName = (String)colNames.get(field);
               if (catName == null)
                  catName = field;

               boolean onLast = !catIter.hasNext();

               // if "missing", build an "uncategorized" category
               if (catVal == null || catVal.trim().length() == 0)
               {
                  catName = _UNCATEGORIZED_;
                  catVal = _UNCATEGORIZED_;
               }

               // get the category node, add if not already found
               Iterator children = parent.getChildren();
               PSNode catNode = null;
               if (children != null)
               {
                  while (children.hasNext() && catNode == null)
                  {
                     PSNode child = (PSNode)children.next();
                     if (child.getType().equals(PSNode.TYPE_CATEGORY)
                        && child.getName().equals(catName)
                        && child.getLabel().equals(catVal))
                     {
                        catNode = child;
                     }
                  }
               }

  
               if (catNode == null)
               {
                  // wasn't found, so create and add a node for the category
                  catNode =
                     new PSNode(
                        catName,
                        catVal,
                        PSNode.TYPE_CATEGORY,
                        null,
                        null,
                        false,
                        -1);             
                  catNode.setDisplayFormatId(
                        Integer.toString(m_displayFormat.getDisplayId()));
                  parent.addChild(catNode);

                  // set table meta columns on the category node that will 
                  // contain the items.
                  if (onLast)
                  {
                     Iterator colDefIter =
                        colDefs != null ? colDefs.iterator() : null;
                     catNode.setChildrenDisplayFormat(colDefIter);
                  }
               }

               // if not on last category, reset the parent for the next 
               // iteration, otherwise add the item
               if (!onLast)
                  parent = catNode;
               else
                  catNode.addChild(itemNode);
            }
         }
         else
         {
            // just build flat list of children
            baseNode.addChild(itemNode);
         }
      }
      
      // now sort categories ascending
      if (!categorized.isEmpty())
      {
         Comparator comp = new Comparator()
         {
            public int compare(Object o1, Object o2)
            {
               return o1.toString().compareTo(o2.toString());               
            }
         };        
         
         sortCategories(baseNode, comp);         
      }      
   }
   
   /**
    * Gets the map of content id and icon paths. Creates a list of locators with
    * current or tip revision based on the user info and calls
    * {@link PSCxUtil#getItemIcons(List)}.
    * 
    * @param resp assumed not <code>null</code>.
    * @return Map of content ids and their icon paths. May be empty but never
    *         <code>null</code>. {@see PSCxUtil#getItemIcons(List)} for more
    *         details.
    */
   private Map<String, String> getIconPathMap(PSWSSearchResponse resp)
   {
      Map<String, String> iconMap = new HashMap<String, String>();
      Iterator rowiter = resp.getRows();
      List<PSLocator> locs = new ArrayList<PSLocator>();
      String curUser = StringUtils.defaultString(m_applet
            .getUserInfo().getUserName());
      while (rowiter.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rowiter.next();
         String cid = row.getColumnValue(PROPERTY_CONTENTID);
         String rid = row.getColumnValue("sys_currentrevision");
         String chkUser = row.getColumnValue("sys_contentcheckoutusername");
         if (curUser.equalsIgnoreCase(chkUser))
         {
            rid = row.getColumnValue("sys_tiprevision");
         }
         PSLocator loc = new PSLocator(cid, rid);
         locs.add(loc);
      }
      if (!locs.isEmpty())
      {
         iconMap = PSCxUtil.getItemIcons(locs, m_applet);
      }
      return iconMap;
   }

   /**
    * Sorts the child nodes of the supplied node if they are category nodes.
    * Sort uses the supplied comparator. Assumes that if the first child node is
    * of type <code>PSNode.TYPE_CATEGORY</code>, then all children are of
    * this type and they should be sorted, and the method should recurse into
    * these children. Otherwise, they are not sorted and the method does not
    * recurse.
    * 
    * @param node The node whose children are to be sorted, assumed not
    *           <code>null</code>.
    * @param comp The comparator to use, assumed not <code>null</code>.
    */
   private void sortCategories(PSNode node, Comparator comp)
   {
      if (node.getChildren() == null)
         return;
         
      List childList = PSIteratorUtils.cloneList(node.getChildren());
      if (!childList.isEmpty() && ((PSNode)childList.get(0)).isOfType(
         PSNode.TYPE_CATEGORY))
      {
         Collections.sort(childList, comp);
         node.setChildren(childList.iterator());
                 
         // now recurse children
         Iterator children = node.getChildren();
         while (children.hasNext())
         {
            sortCategories((PSNode)children.next(), comp);
         }
      }
   }

   /**
    * Reference to the display format for the specified search, initialized on
    * the ctor, may be <code>null</code>, never modified after that.
    */
   private PSDisplayFormat m_displayFormat;
   
   /**
    * A reference back to the applet.
    */
   private PSContentExplorerApplet m_applet;
}
