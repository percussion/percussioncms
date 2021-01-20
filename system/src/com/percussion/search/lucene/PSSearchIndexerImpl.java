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
package com.percussion.search.lucene;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemChildLocator;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSFieldRetriever;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSField;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.search.PSSearchEngine;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchIndexer;
import com.percussion.search.PSSearchKey;
import com.percussion.search.lucene.analyzer.PSLuceneAnalyzerFactory;
import com.percussion.search.lucene.textconverter.IPSLuceneTextConverter;
import com.percussion.search.lucene.textconverter.PSLuceneTextConverterFactory;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

public class PSSearchIndexerImpl extends PSSearchIndexer
{

   PSSearchIndexerImpl()
   {

   }

   @Override
   public void clearIndex(PSKey contentType) throws PSSearchException
   {
      //We do not clear anything part of this class.
   }

   @Override
   public void close() throws PSSearchException
   {
      close(false);
   }

   @Override
   public synchronized void commit() throws PSSearchException
   {
      Iterator<Long> iter = m_indexesNotCommitted.keySet().iterator();
      HashSet<Long> committedids = new HashSet<Long>();
      List<Long> errors = new ArrayList<Long>(); 
      while(iter.hasNext())
      {
         Long ctid = iter.next();
         try
         {
            IndexWriter iw = m_indexesNotCommitted.get(ctid);
            // Default autocommit in 2.2 version, flush does commit also
            iw.flush();
            committedids.add(ctid);
         }
         catch (CorruptIndexException e)
         {
            errors.add(ctid);
            String msg = "CorruptIndexException occurred while optimizing index " +
                  "related to content type id " + ctid;  
            ms_log.error(msg);
         }
         catch (IOException e)
         {
            String msg = "IOException occurred while optimizing index " +
            "related to content type id " + ctid;  
            ms_log.error(msg);
         }
      }
      if(!errors.isEmpty())
      {
         Object[] args = { errors.toString() };
         throw new PSSearchException(IPSLuceneErrors.INDEX_OPTIMIZATION_ERROR,
               args);
      }
      for (long id : committedids) {
         m_indexesNotCommitted.remove(id);
      }
   }

   @Override
   public synchronized void commitAll() throws PSSearchException
   {
      Iterator<Long> iter = ms_indexWriters.keySet().iterator();
      List<Long> errors = new ArrayList<Long>(); 
      while(iter.hasNext())
      {
         Long ctid = iter.next();
         try
         {
            ms_indexWriters.get(ctid).flush();
         }
         catch (CorruptIndexException e)
         {
            errors.add(ctid);
            String msg = "CorruptIndexException occurred while flushing index " +
                  "related to content type id " + ctid;  
            ms_log.error(msg);
         }
         catch (IOException e)
         {
            String msg = "IOException occurred while flushing index " +
            "related to content type id " + ctid;  
            ms_log.error(msg);
         }
      }
      if(!errors.isEmpty())
      {
         Object[] args = { errors.toString() };
         throw new PSSearchException(IPSLuceneErrors.INDEX_OPTIMIZATION_ERROR,
               args);
      }
   }
   
   @Override
   public synchronized void close(boolean optimize) throws PSSearchException
   {
      Iterator<Long> iter = ms_indexWriters.keySet().iterator();
      List<Long> errors = new ArrayList<Long>(); 
      try {
      while(iter.hasNext())
      {
         Long ctid = iter.next();
         try
         {
            IndexWriter writer = ms_indexWriters.get(ctid);
            if (optimize)
               writer.forceMerge(1);
            writer.close();
         }
         catch (CorruptIndexException e)
         {
            errors.add(ctid);
            String msg = "CorruptIndexException occurred while optimizing index " +
                  "related to content type id " + ctid;  
            ms_log.error(msg);
         }
         catch (IOException e)
         {
            String msg = "IOException occurred while optimizing index " +
            "related to content type id " + ctid;  
            ms_log.error(msg);
         } catch (AlreadyClosedException e) {
            // Ignore, we wanted to close anyway.
         }
      }
      if(!errors.isEmpty())
      {
         Object[] args = { errors.toString() };
         throw new PSSearchException(IPSLuceneErrors.INDEX_OPTIMIZATION_ERROR,
               args);
      }
      } finally {
            ms_indexWriters.clear();
      }
     
   }

   @Override
   public void delete(Collection unitIds) throws PSSearchException
   {
      for (Object id : unitIds)
      {
         PSSearchKey unitId = (PSSearchKey) id;
         PSItemChildLocator chLoc = unitId.getChildId();
         Term term = null;
         if (chLoc == null)
         {
            term = new Term(IPSHtmlParameters.SYS_CONTENTID, ""
                  + unitId.getParentLocator().getId());
         }
         else
         {
            term = new Term(PSKeyConverter.LUCENE_DOCID_FIELDNAME,
                  PSKeyConverter.convert(unitId));
         }
         Long ctypeid = new Long(unitId.getContentTypeKey().getPartAsInt());
         try
         {
            IndexWriter iw = getIndexWriter(ctypeid,null);
            iw.deleteDocuments(term); 
            m_indexesNotCommitted.put(ctypeid,iw);
         }
         catch (CorruptIndexException e)
         {
            String msg = "Failed to delete document from index with unitid : "
                  + PSKeyConverter.convert(unitId);
            throw new PSSearchException(msg, e);
         }
         catch (LockObtainFailedException e)
         {
            String msg = "Failed to index document with unitid : "
                  + PSKeyConverter.convert(unitId);
            throw new PSSearchException(msg, e);
         }
         catch (IOException e)
         {
            String msg = "Failed to delete document  from index with unitid : "
                  + PSKeyConverter.convert(unitId);
            throw new PSSearchException(msg, e);
         }

      }
   }

   @Override
   public void update(PSSearchKey unitId, Map itemFragment,
         boolean commitToIndex) throws PSSearchException
   {
      if (null == unitId)
      {
         throw new IllegalArgumentException("unitId cannot be null");
      }
      if (null == itemFragment)
      {
         throw new IllegalArgumentException("itemFragment cannot be null");
      }
      String lang = (String) itemFragment.get(IPSHtmlParameters.SYS_LANG);
      Long ctypeid = new Long(unitId.getContentTypeKey().getPartAsInt());

      try
      {
         Analyzer an = PSLuceneAnalyzerFactory.getInstance().getAnalyzer(lang);
         IndexWriter iw = getIndexWriter(ctypeid,an);

         Document doc = prepareLuceneDocument(unitId, itemFragment);
         Term term = new Term(PSKeyConverter.LUCENE_DOCID_FIELDNAME,
               PSKeyConverter.convert(unitId));

         iw.updateDocument(term, doc);
         m_indexesNotCommitted.put(ctypeid,iw);
         checkIndexWriterSize(ctypeid,iw,PSKeyConverter.convert(unitId));
      }
      catch (CorruptIndexException e)
      {
         String msg = "Failed to index document with unitid : "
               + PSKeyConverter.convert(unitId);
         throw new PSSearchException(msg, e);
      }
      catch (LockObtainFailedException e)
      {
         String msg = "Failed to index document with unitid : "
               + PSKeyConverter.convert(unitId);
         throw new PSSearchException(msg, e);
      }
      catch (IOException e)
      {
         String msg = "Failed to index document with unitid : "
               + PSKeyConverter.convert(unitId);
         throw new PSSearchException(msg, e);
      }
   }

   private void checkIndexWriterSize(Long type, IndexWriter iw, String id)
   {
      ms_log.debug("update Index type = "+type +
            " id="+id+
            " ram docs="+iw.numRamDocs()+
            " memory="+iw.ramBytesUsed());
      String prop = PSServer.getServerProps().getProperty("maxIndexWriterRamBeforeFlush");
      if (prop!=null) {
         Integer p = Integer.parseInt(prop);
         if (iw.ramBytesUsed()>=p) {
            try
            {
               // Default autocommit in 2.2 version, flush does commit also
               iw.flush();
            }
            catch (CorruptIndexException e)
            {
               String msg = "CorruptIndexException occurred while flushing index " +
                     "related to content type id " + type;  
               ms_log.error(msg);
            }
            catch (IOException e)
            {
               String msg = "IOException occurred while flushing index " +
                     "related to content type id " + type;  
                     ms_log.error(msg);
            }
         }
      }

   }

   /**
    * Gets the IndexWriter for the supplied contenttype. This is a synchronized
    * method as we can't create more than one Indexwriter for the given
    * contenttype. We have three cases here. We get a matching IndexWriter for
    * the given contenttype and lang. In this case we simply return that index
    * writer. We find a IndexWriter for the given contenttype but the lang does
    * not match then we need to close this writer and Create another writer and
    * add it to the map and return. We do not find a index writer for the given
    * contenttype.
    * 
    * @param ctypeid The content typeid for which the index writer is needed.
    * assumed not <code>null</code>.
    * @return The IndexWriter associated with the content type. Never
    * <code>null</code>.
    * @throws IOException
    * @throws LockObtainFailedException
    * @throws CorruptIndexException
    * @throws PSSearchException
    */
   private synchronized IndexWriter getIndexWriter(Long ctypeid, Analyzer an)
      throws CorruptIndexException, IOException
   {
      IndexWriter iw = ms_indexWriters.get(ctypeid);
      if (iw != null)
         return iw;

      //Pick standard analyzer if not supplied
      if(an == null){
         an = new StandardAnalyzer();
      }

      try
      {
         IndexWriterConfig config = new IndexWriterConfig(an);
         FSDirectory dir= FSDirectory.open(new File(PSSearchEngineImpl.getLuceneIndexRootPath()
                 + ctypeid.toString()).toPath());

         iw = new IndexWriter(dir,config);
      }
      catch(LockObtainFailedException e)
      {
         //Delete the lock and try again
         File f = new File(PSSearchEngineImpl.getLuceneIndexRootPath()
               + ctypeid.toString() + File.separator + "write.lock");
         if(f.exists())
         {
            f.delete();
         }
         //TODO: Refactor this
         IndexWriterConfig config = new IndexWriterConfig(an);
         FSDirectory dir= FSDirectory.open(new File(PSSearchEngineImpl.getLuceneIndexRootPath()
                 + ctypeid.toString()).toPath());

         iw = new IndexWriter(dir,config);

      }
      ms_indexWriters.put(ctypeid, iw);
      return iw;
   }

   /**
    * Prepares the lucene documents based on the itemFragment.
    * 
    * @param unitId assumed not <code>null</code>.
    * @param itemFragment assumed not <code>null</code>.
    * @return lucene documents never <code>null</code>.
    */
   private Document prepareLuceneDocument(PSSearchKey unitId, Map itemFragment)
      throws PSSearchException
   {
      Document doc = new Document();
      List<Field> lucFields = new ArrayList<Field>();
      List<String> fieldData = new ArrayList<String>();
      String errmsg = "Skipping indexing of field (name:{0},unitid:{1}).";
      String docid = PSKeyConverter.convert(unitId);

      // separate submission; remove non-searchable fields
      try
      {
         PSItemDefinition def = PSItemDefManager.getInstance().getItemDef(
               unitId.getContentTypeKey().getPartAsInt(), 
               PSItemDefManager.COMMUNITY_ANY);
         
         Set<PSField> fields = new HashSet<PSField>();
         Iterator itemFields = PSSearchUtils.getFields(unitId);
         while (itemFields.hasNext())
         {
            fields.add((PSField) itemFields.next());
         }
         fields.addAll(PSSearchUtils.getSearchableFields(def));
         
         Iterator fieldsIter = fields.iterator();
         while (fieldsIter.hasNext())
         {
            Field lucField = null;
            PSField field = (PSField) fieldsIter.next();
            String name = field.getSubmitName();
            Object data = itemFragment.get(name);
            boolean addToAllContent = field.getSearchProperties()
                  .isVisibleToGlobalQuery();
            if (name.equals(IPSHtmlParameters.SYS_CONTENTID))
            {
               //Index the content id (only indexes)
               lucField = new IntPoint(name, Integer.parseInt((String)data));
               lucFields.add(lucField);

               //Store the content id
               lucField = new StoredField(name, Integer.parseInt((String)data));
               lucFields.add(lucField);
               if(addToAllContent)
                  fieldData.add((String) data);
               continue;
            }else if(name.equals(IPSHtmlParameters.SYS_TITLE)){
               lucField = new TextField(name, (String) data,Field.Store.YES );
               lucFields.add(lucField);
                  fieldData.add((String) data);
                  continue;
            }
            else if (!field.getSearchProperties().isUserSearchable())
               continue;
            else if (field.getSearchProperties().isEnableTransformation())
            {
               if (null == data
                     && (field.isForceBinary() || field.getDataType().equals(
                           PSField.DT_BINARY)))
               {
                  data = PSSearchEngine.getInstance().getSearchAdmin()
                        .getFieldRetriever(unitId.getContentTypeKey());
               }
               InputStream is = null;
               try
               {
                  is = getBodyFieldDataAsStream(unitId, data, name);
                  if (is == null)
                     continue;
                  String mimetype = def.getFieldMimeType(name,itemFragment);
                  if (StringUtils.isBlank(mimetype))
                  {
                     String msg = errmsg + " as the mimetype is empty.";
                     Object[] args = { name, docid };
                     ms_log.warn(MessageFormat.format(msg, args));
                     continue;
                  }
                  String text = "";
                  text = getText(is, mimetype, unitId, name);
                  lucField = new TextField(name, text, Field.Store.YES);
                  lucFields.add(lucField);
                  if(addToAllContent)
                     fieldData.add(text);
                  continue;
               }
               catch (PSInvalidContentTypeException | PSCmsException | PSExtensionProcessingException e)
               {
                  Object[] args = { name, docid };
                  ms_log.error("Error processing document with fields " + lucFields.toString() 
                        + " continuing to index content with these fields. Full text search will not be available for this document" );
                  continue;
               }
            }
            else if (field.getDataType().equals(PSField.DT_DATE)
                  || field.getDataType().equals(PSField.DT_DATETIME))
            {
               String ts = null == data ? "" : data.toString();
               if (ts.indexOf(' ') >= 0)
               {
                  data = ts.substring(0, ts.indexOf(' '));
               }
               //TODO:  Review this for refactoring - should we be indexing a timestamp instead
               lucField = new StringField(name, (String) data, Field.Store.NO);
               lucFields.add(lucField);
               if(addToAllContent)
                  fieldData.add((String) data);
            }
            else if (field.getDataType().equals(PSField.DT_BOOLEAN))
            {

               lucField = new StringField(name, (String) data, Field.Store.NO);
               lucFields.add(lucField);
               if(addToAllContent)
                  fieldData.add((String) data);
            }
            else
            {
               String fieldContent = (String) data;
               if (StringUtils.isBlank(fieldContent))
                  continue;
               String text = "";
             
                      
               String mimetype = def.getFieldMimeType(name,itemFragment);
               if (StringUtils.isBlank(mimetype))
               {
                  String msg = errmsg + " as the mimetype is empty.";
                  Object[] args = { name, docid };
                  ms_log.error(MessageFormat.format(msg, args));
                  continue;
               }
            
               try
               {
                  text = getText(fieldContent, mimetype, unitId, name);
               }
               catch (PSExtensionProcessingException e)
               {
                  Object[] args = { name, docid };
                  ms_log.error(MessageFormat.format(errmsg, args),e);
                  continue;
               }
           
               
               lucField = new TextField(name, text, Field.Store.NO);
               if(addToAllContent)
                  fieldData.add(text);
            }
            lucFields.add(lucField);
         }

         String allContent = "";
         for (String string : fieldData)
         {
            if (StringUtils.isNotEmpty(string))
               allContent += string
                     + IPSLuceneConstants.ALL_CONTENT_FIELD_SEPERATOR;
         }
         // Remove trailing field separator
         if (allContent
               .endsWith(IPSLuceneConstants.ALL_CONTENT_FIELD_SEPERATOR))
            allContent = allContent.substring(0, allContent.length()
                  - IPSLuceneConstants.ALL_CONTENT_FIELD_SEPERATOR.length());
         // Add docid field
         Field docidfield = new StoredField(PSKeyConverter.LUCENE_DOCID_FIELDNAME,
               docid);
         doc.add(docidfield);
         // Add all other fields
         for (Field field : lucFields)
         {
            doc.add(field);
         }
         // Add all content data field
         Field acfield = new TextField(IPSLuceneConstants.ALL_CONTENT_FIELD_NAME,
               allContent, Field.Store.NO);
         doc.add(acfield);

      }
      catch (PSInvalidContentTypeException e)
      {
         String msg = "Failed to index unit id " + docid;
         throw new PSSearchException(msg, e);
      }

      return doc;
   }

   /**
    * Extracts the data from body field. If the supplied data is instance of
    * InputStream returns as is. If it is byte array then converts into
    * ByteArrayInputStream and returns. If it is PSFieldRetriever gets the field
    * content as byte array and then converts into ByteArrayInputStream and
    * returns.
    * 
    * @param unitId to obtain the Content Type id. Assumed not <code>null</code>.
    * @param data The data that needs to be converted to InputStream. If
    * <code>null</code> returns <code>null</code>.
    * @param fieldName The name of the field for which the data needs to be
    * extracted. Assumed a valid field.
    * @return data in the form of InputStream or <code>null</code>.
    * @throws PSCmsException see
    * {@link PSFieldRetriever#getFieldContent(PSRequest, 
    *    com.percussion.design.objectstore.PSLocator, String, int)}
    * @throws PSInvalidContentTypeException see
    * {@link PSFieldRetriever#getFieldContent(PSRequest, 
    *    com.percussion.design.objectstore.PSLocator, String, int)}
    */
   private InputStream getBodyFieldDataAsStream(PSSearchKey unitId,
         Object data, String fieldName)
      throws PSCmsException, PSInvalidContentTypeException
   {
      if (data == null)
         return null;
      InputStream is = null;
      if (data instanceof InputStream)
      {
         is = (InputStream) data;
      }
      else
      {
         byte[] body = null;
         if (data instanceof byte[])
            body = (byte[]) data;
         else if (data instanceof PSFieldRetriever)
         {
            PSFieldRetriever retriever = (PSFieldRetriever) data;
            PSRequest req = PSRequest.getContextForRequest();
            int childId = -1;
            if (null != unitId.getChildId())
            {
               childId = Integer.parseInt(unitId.getChildId().getChildRowId());
            }
            body = retriever.getFieldContent(req, unitId.getParentLocator(),
                  fieldName, childId);
         }
         else if (null == data)
         {
            body = new byte[0];
         }
         else
         {
            body = ((String) data).toString().getBytes();
         }
         if(body.length > 0)
            is = new ByteArrayInputStream(body);
      }
      return is;
   }

   /**
    * Gets the text from the supplied data, if the mimetype is plain text then
    * simply returns the supplied data. Otherwise creates a ByteArrayInputStream
    * and gets the text by calling getText(InputStream, String) method.
    * 
    * @param data The data object from which the text needs to be extracted. If
    * <code>null</code> or empty returns empty string.
    * @param mimetype Mimetype that needs to be used to extract the text.
    * @param unitId used for logging if there is any error. Assumed not
    * <code>null</code>.
    * @param fieldName used for logging if there is any error. Assumed not
    * <code>null</code>.
    * @return Extracted text. May be empty, but never <code>null</code>.
    * @throws PSExtensionProcessingException
    */
   private String getText(String data, String mimetype, PSSearchKey unitId,
         String fieldName) throws PSExtensionProcessingException
   {
      if (StringUtils.isBlank(data))
         return "";
      if (mimetype.equals(IPSLuceneConstants.MIME_TYPE_PLAIN_BY_TEXT))
      {
         return data;
      }
      ByteArrayInputStream str = new ByteArrayInputStream(data.getBytes());
      return getText(str, mimetype, unitId, fieldName);
   }

   /**
    * Extracts the text from the supplied input stream. Returns empty string if
    * failed to obtain the text converter for field.
    * 
    * @param is The input stream from which the text content needs to extracted.
    * If <code>null</code> returns empty String.
    * @param mimetype See {@link #getText(String, String, PSSearchKey, String))
    * @param unitId See {@link #getText(String, String, PSSearchKey, String))
    * @param fieldName See {@link #getText(String, String, PSSearchKey, String))
    * 
    * @return Extracted text. May be empty, but never <code>null</code>.
    * @throws PSExtensionProcessingException
    */
   private String getText(InputStream is, String mimetype, PSSearchKey unitId,
         String fieldName) throws PSExtensionProcessingException
   {
      if (is == null)
         return "";
      String text = "";
      IPSLuceneTextConverter converter = PSLuceneTextConverterFactory
            .getInstance().getLuceneTextConverter(mimetype);
      if (converter == null)
      {
         String errmsg = "Cannot obtain the text converter for field: " +
               "({0}), skipping indexing of this field for (unitid:{1}).";
         Object[] args = { fieldName, PSKeyConverter.convert(unitId) };
         ms_log.debug(MessageFormat.format(errmsg, args));
         return "";
      }
     
      try
      {
         text = StringUtils.defaultString(converter.getConvertedText(is,
               mimetype));
      }
      catch(Exception e)
      {
         ms_log.error("Unable to process a document with a mime-type of " + mimetype + " removing document from queue");
         throw new PSExtensionProcessingException(new PSException(e.getMessage()));
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
            }
         }
      }
      return text;

   }

   /**
    * @return the map of content type ids and index writers. Never
    * <code>null</code> may be empty.
    */
   protected static Map<Long, IndexWriter> getLuceneIndexWriters()
   {
      return ms_indexWriters;
   }
   
   /**
    * Static list of Lucene IndexWriter objects one for contenttype.
    */
   private static Map<Long, IndexWriter> ms_indexWriters = 
      new HashMap<Long, IndexWriter>();

   /**
    * Reference to log for this class
    */
   private final static Log ms_log = 
      LogFactory.getLog(PSSearchIndexerImpl.class);

   /**
    * Map of content type ids and index writers that need to be optimized
    */
   private Map<Long, IndexWriter> m_indexesNotCommitted = 
      new HashMap<Long, IndexWriter>();
}
