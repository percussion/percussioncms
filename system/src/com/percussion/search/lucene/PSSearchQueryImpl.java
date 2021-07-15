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
package com.percussion.search.lucene;

import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.search.IPSSearchErrors;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchQuery;
import com.percussion.search.PSSearchResult;
import com.percussion.search.lucene.analyzer.PSLuceneAnalyzerFactory;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ThreadSafe
public class PSSearchQueryImpl extends PSSearchQuery implements Closeable
{

   private static PSSearchQueryImpl instance = null;

   /**
    *
    */
   public static synchronized PSSearchQueryImpl getInstance() throws PSSearchException {
      if(instance == null){
               instance = new PSSearchQueryImpl();
      }
      return instance;
   }

   private PSSearchQueryImpl() throws PSSearchException {
      //Prevent from the reflection api.
      if (instance != null){
         throw new PSSearchException(IPSSearchErrors.USE_GET_INSTANCE);
      }
   }


   @Override
   public void close()
   {
         //Release resources
   }

   @Override
   public List performSearch(Collection ctypeIds, String globalQuery,
         Map fieldQueries, Map controlProps) throws PSSearchException
   {
      List<PSSearchResult> searchResults = new ArrayList<>();
      // Return empty results if we do not have any thing to search for.
      if (StringUtils.isBlank(globalQuery) && fieldQueries == null)
         return searchResults;

      // normalize the param names of the control props
      Map<String,String> props = normalizeMap(controlProps);
      // set to null to catch incorrect (future) uses below
      controlProps = null;

      // remove our max results prop so it doesn't get set below
      // default to no limit
      int maxResults = getIntProp(props, QUERYPROP_MAXRESULTS, -1, 1,
            Integer.MAX_VALUE);

      List<String> processedIds = new ArrayList<>();

      try(MultiReader mr = prepareMultiSearcher(ctypeIds))
      {
         if(mr==null)
         {
            String msg = "Failed to create the index searcher";
            if(!ctypeIds.isEmpty())
            {
               msg += " for the given content types " + ctypeIds;
            }
            msg+=". The content might not have been indexed yet." +
                  " Returning empty results.";
            log.info(msg);
            return searchResults;
         }

         Query qr = prepareSearchQuery(globalQuery, fieldQueries, props);

         IndexSearcher searcher = new IndexSearcher(mr);
         int count = searcher.count(qr);
      if(count>0) {
         TopDocs docs = searcher.search(qr, count);

         for (ScoreDoc sd : docs.scoreDocs) {
            Document result = searcher.doc(sd.doc);
            String cid = result.get(IPSHtmlParameters.SYS_CONTENTID);

            /*
             * We may get more than one matching doc for a content id as we
             * store one doc for parent and one for each child row. Just
             * continue if we have already processed this content id.
             * Skip the result if the cid is null, this could happen when
             * lucene internal documents that matches when user searches for 3* or 4* etc.
             * (Fix for RX-16244)
             */
            if (cid == null || processedIds.contains(cid))
               continue;
            processedIds.add(cid);
            int score = (int) (sd.score * 100);
            PSSearchResult sr = new PSSearchResult(new PSLocator(cid), score);
            searchResults.add(sr);
         }
      }
         searcher.getIndexReader().close();
      }
      catch (IOException e)
      {
         throw new PSSearchException(IPSLuceneErrors.HITS_IOEXCEPTION, e);
      }
      catch (ParseException e)
      {
         throw new PSSearchException(
               IPSLuceneErrors.SEARCH_QUERY_PARSEEXCEPTION, e);
      }

      return searchResults;
   }

   /**
    * Prepares the search query object combining global query with field
    * queries.
    * 
    * @param globalQuery assumed not <code>null</code>.
    * @param fieldQueries assumed not <code>null</code>.
    * @param props assumed not <code>null</code>.
    * @return Combined query of global and field queries.
    * @throws ParseException in case any of the query is not parsable.
    */
   private Query prepareSearchQuery(String globalQuery, Map fieldQueries,
         Map props) throws ParseException
   {
      boolean addGlobalBooleanQR = false;
      boolean addFullBooleanQR = false;
      Query qr = null;

      String langString = (String) props
            .get(PSSearchQuery.QUERYPROP_LANGUAGE);
      Analyzer an = PSLuceneAnalyzerFactory.getInstance().getAnalyzer(
            langString);

      if (StringUtils.isNotBlank(globalQuery))
      {
         // Get the system default value for synonym expansion
         boolean defaultSynExp = PSServer.getServerConfiguration()
               .getSearchConfig().isSynonymExpansionRequired();
         boolean synExpansion = getBoolProp(props,
               QUERYPROP_SYNONYM_EXPANSION, defaultSynExp);
         boolean synExpanded = false;
         if (synExpansion)
         {

         }
         // If we have not expanded the query with synonyms search on original
         // query.
         if (!synExpanded)
         {
            QueryParser qp = new QueryParser(
                  IPSLuceneConstants.ALL_CONTENT_FIELD_NAME, an);
            qr = qp.parse(globalQuery);
         }
         if (qr != null)
            addGlobalBooleanQR = true;

      }

      Iterator iter = fieldQueries.keySet().iterator();
      BooleanQuery.Builder builder = new BooleanQuery.Builder();
      while (iter.hasNext())
      {
         String fn = (String) iter.next();
         String fq = (String) fieldQueries.get(fn);
         if (StringUtils.isNotBlank(fq))
         {
            QueryParser fqp = new QueryParser(fn, an);
            //CMS-7921 : The multiple search parameters were overwritten. Only last one was considered by lucene search query.
            //The changes in this file in this commit is to fix that.
            Query query = fqp.parse(fq);
            builder.add(query, Occur.MUST);
         }
      }

      if(addGlobalBooleanQR)
         builder.add(new BooleanClause(qr, Occur.MUST));

      return builder.build();
   }

   /**
    * A convenient method to construct the multi index searcher.
    * 
    * @param ctypeIds if <code>null</code> then all content types are
    * considered.
    * @return multi searcher or <code>null</code>, if indexes are not
    * available for the supplied content types
    * @throws PSSearchException
    */
   private MultiReader prepareMultiSearcher(Collection<PSKey> ctypeIds)
      throws PSSearchException
   {
      MultiReader searcher = null;
      List<IndexReader> isList = new ArrayList<>();
      try
      {
         Collection<PSKey> cTypeKeys = ctypeIds;
         
         if (cTypeKeys == null || cTypeKeys.isEmpty())
         {
            PSItemDefManager itemMgr = PSItemDefManager.getInstance();
            long[] allCtypeIds = itemMgr.getAllContentTypeIds(PSItemDefManager.COMMUNITY_ANY);
            cTypeKeys = new ArrayList<>();
            for (long ctypeId : allCtypeIds)
            {
                PSKey cTypeKey = PSContentType.createKey((int) ctypeId);
                cTypeKeys .add(cTypeKey);
            }
         }
         
         for (PSKey ctype : cTypeKeys)
         {
            String msg = "Failed to create IndexSearcher for content " +
            "type ({0}) not including it in search. The content type might not" +
            "have been indexed yet.";
            Object[] args = {ctype.getPart()};
            try
            {
               IndexReader ir = getIndexReader(ctype.getPart());
               if(ir == null)
               {
                  log.debug("{}",MessageFormat.format(msg,args));
                  continue;
               }
               isList.add(ir);
            }
            catch (Exception e)
            {
               log.debug(MessageFormat.format(msg,args));
            }
         }

         if(!isList.isEmpty())
            searcher = new MultiReader(isList.toArray(new IndexReader[isList
                  .size()]));
      }
      catch (IOException e)
      {
         Object[] args = { ctypeIds.toString() };
         throw new PSSearchException(
               IPSLuceneErrors.INDEX_IO_EXCEPTION_SEARCHING, e, args);
      }
      return searcher;
   }

   /**
    * Gets the IndexSearcher associated with the content type id.
    * 
    * @param ctypeId Id of the content type for which the searcher is needed,
    * assumed not <code>null</code> or empty.
    * @return The index searcher associated with the content type id or
    * <code>null</code>, if no directory exists with that content type id.
    * @throws PSSearchException in case of io or index corrupted exceptions.
    */
   @SuppressFBWarnings({"PATH_TRAVERSAL_IN"})
   private IndexReader getIndexReader(String ctypeId)
           throws IOException, PSSearchException {

         if(!StringUtils.isNumeric(ctypeId))
            throw new PSSearchException(IPSSearchErrors.INVALID_INDEX_CONTENTTYPE);

         File f = new File(PSSearchEngineImpl.getLuceneIndexRootPath() + ctypeId);
         if (!f.exists() || !f.isDirectory()) {
            // Not having a index folder for a given content type is not an error
            // Log it
            log.debug(
                    "No index directory exists for the supplied content type id {}. Ignoring it in search.",
                    ctypeId);
            return null;
         }

      return  DirectoryReader.open(FSDirectory.open(f.toPath()));

   }

   /**
    * Takes all entries in the supplied map and copies them to the returned map,
    * lowercasing all keys on the way.
    * 
    * @param src <code>null</code> OK. Assumes all keys are
    * <code>String</code>. The original is unchanged.
    * 
    * @return A 'copy' of the supplied map with keys lowercased, never <code>
    * null</code>.
    */
   private Map<String,String> normalizeMap(Map<String, String> src)
   {
      Map<String,String> map = new HashMap<>();
      if (null != src)
      {
         for (String o : src.keySet()) {
            map.put(( o).toLowerCase(), src.get(o));
         }
      }
      return map;
   }

   /**
    * Looks up a property in the supplied map by the supplied key, converts it
    * to an int and returns it. If found, the entry is removed from the map.
    * 
    * @param props Assumed not <code>null</code>. Assumed keys are
    * <code>String</code> and that the keys are all normalized to lowercase.
    * 
    * @param keyName The key to use when looking up the value in the supplied
    * map. Assumed not <code>null</code> or empty. Lower-cased before use.
    * 
    * @param defaultValue Returned if a property is not found, is <code>null
    * </code>
    * or empty, isn't in the specified range or isn't a valid number.
    * 
    * @param rangeLow If the property is less than this value, use the default.
    * If equal to <code>rangeHi</code>, no validation is performed.
    * 
    * @param rangeHi If the property is greater than this value, use the
    * default. If equal to <code>rangeLow</code>, no validation is performed.
    * 
    * @return If <code>rangleLow</code> != <code>rangeHi</code>, a value
    * between the two, inclusive, or the defaultValue. If the ranges are equal,
    * any value is possible.
    */
   private int getIntProp(Map props, String keyName, int defaultValue,
         int rangeLow, int rangeHi)
   {
      int result = defaultValue;
      Object o = props.remove(keyName.toLowerCase());
      if (null != o)
      {
         if (o instanceof Integer)
            result = ((Integer) o).intValue();
         else
         {
            String val = o.toString().trim();
            if (val.length() > 0)
            {
               try
               {
                  result = Integer.parseInt(val);
               }
               catch (NumberFormatException nfe)
               { /* ignore, use default */
               }
            }
         }
         if (rangeLow != rangeHi && (result < rangeLow || result > rangeHi))
         {
            result = defaultValue;
         }
      }
      return result;
   }

   /**
    * Looks up a property in the supplied map by the supplied key, converts it
    * to a bool and returns it. If found, the entry is removed from the map. The
    * following values are considered <code>true</code>, any other value will
    * return <code>false</code>: 1, true or yes (case-insensitive).
    * 
    * @param props Assumed not <code>null</code>. Assumed keys are
    * <code>String</code> and that the keys are all normalized to lowercase.
    * 
    * @param keyName The key to use when looking up the value in the supplied
    * map. Assumed not <code>null</code> or empty.
    * 
    * @param defaultValue Returned if a property is not found or is <code>null
    * </code>
    * or empty.
    * 
    * @return The found value, if interpretable as a bool, otherwise the
    * defaultValue.
    */
   private boolean getBoolProp(Map props, String keyName, boolean defaultValue)
   {
      boolean result = defaultValue;
      Object o = props.remove(keyName.toLowerCase());
      if (null != o)
      {
         String val = o.toString().trim().toLowerCase();
         if (val.length() > 0)
         {
            // try numeric: i.e. non-zero is true, 0 is false
            try
            {
               int numberVal;
               numberVal = Integer.parseInt(val);
               if (numberVal != 0)
               {
                  result = true;
               }
               else
                  result = false;
            }
            catch (NumberFormatException nfe)
            { /* ignore, try other formats */
            }

            // try text, yes is true, no is false or 'true' or 'false'
            if (val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true"))
               result = true;
            else
               result = false;
         }
      }
      return result;
   }

   /**
    * Custom properties in the search configuration that are targeted at the
    * query handler must have this prefix to individuate them from the other
    * custom props. These props are passed thru to the RW engine w/o 
    * modification or interpretation by this code.
    * <p>This is public as a side-effect of the implementation, but should
    * not be used outside the convera pkg beyond the initial use. 
    */
   public static final String QUERYPROP_PREFIX = "rxqh_";
   
   /**
    * A property that tells whether the query needs to be expanded for synonyms
    * or not.
    */
   public static final String QUERYPROP_SYNONYM_EXPANSION = "synonym_expansion";

   /**
    * The root folder that has synonym indexes for languages.
    */
   private static final String SYNONYM_INDEX_ROOT_DIR = PSServer.getRxDir()
         + File.separator + "sys_resources/lucene/synonym_indexes/";

   /**
    * Reference to log for this class
    */
   private static final Logger log = LogManager.getLogger(PSSearchQueryImpl.class);

}
