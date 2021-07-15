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
package com.percussion.services.assembly.data;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.PSInlineLinkProcessor;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.PSContentMgrOption;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSFilterServiceErrors;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableFileInputStream;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.collections.PSCopier;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.timing.PSStopwatchStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation class for assembly items and results. Allows the
 * storage of extra information that needs to be passed from the assembly
 * service to individual assemblers.
 * <p>
 * Please note, only one of the result data storage routines should be called.
 * Either call {@link #setResultData(byte[])} or 
 * {@link #setResultStream(InputStream)} but not both.
 * 
 * @author dougrand
 */
public class PSAssemblyWorkItem implements IPSAssemblyResult
{
   /**
    * Logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSAssemblyWorkItem.class);
   /**
    * Small result data is stored in memory, but data larger than this size
    * in bytes will be stored in a temp file. Stored stream data is always
    * stored to a temp file.
    */
   private static final long THRESHOLD = 65535;
   
   /**
    * Serial id needed for serialization
    */
   private static final long serialVersionUID = 1L;

   /**
    * Pattern used to detect a pseudopath. This is a path used to pass the
    * content id and revision as a path.
    */
   private static final Pattern ms_pseudopath = Pattern.compile("/\\d+#\\d+");

   /**
    * The id of the item being assembled. If not set, the 
    * {@link #normalize()} method will compute the id from either parameters 
    * (sys_contentid, sys_revision) or a path to the item.
    * <p>
    * The id should be reset only with great care.
    */
   public IPSGuid m_id;

   /**
    * The mimetype of the finished assembly. The mimetype is either calculated
    * as part of the bindings are provided as a result from the assembly plugin
    * (or some combination).
    */
   private String m_mimeType;

   /**
    * If the debug assembly plugin is being used, this will have the value
    * <code>true</code>.
    */
   public boolean m_isDebug = false;

   /**
    * If the assembly is for unpublishing, this value is set to
    * <code>false</code>
    */
   private boolean m_isPublish;

   /**
    * If the result is large, as defined by the threshold value, the data will 
    * be stored in a file rather than in memory. 
    */   
   private PSPurgableTempFile m_resultFile = null;
   
   /**
    * If something calls {@link #getResultFile()} then the file is the 
    * responsibility of the caller and this member will be <code>true</code>.
    * This keeps {@link #clearResults()} from deleting the file.
    */
   private boolean m_fileReleased = false;
   
   /**
    * The result data from assembly. This may be image data, html, pdf,
    * anything. If it is character data then the charset is specified as part of
    * the mimetype. If no charset is specified, UTF8 will be assumed for
    * character data. If the result data is large it will be stored in 
    * {@link #m_resultFile} instead.
    */
   private byte[] m_resultData = null;

   /**
    * The status of the result, success or failure
    */
   private Status m_status;

   /**
    * The path of the item being assembled. If not set, the 
    * {@link #normalize()} method will compute the path from either the
    * sys_folderid parameter or the folderId.  In fact it typically
    * won't be set in normal operation.
    */
   public String m_path;

   /**
    * The parameters from the original HTTP request. It is possible that this
    * will be empty, but it will never be <code>null</code>.
    */
   public Map<String, String[]> m_parameters = new HashMap<>();

   /**
    * The site variables are loaded into this map. This is loaded using
    * {@link com.percussion.services.sitemgr.PSSiteHelper}.
    */
   public Map<String, String> m_variables;

   /**
    * The assembly template
    */
   public IPSAssemblyTemplate m_template;

   /**
    * The purpose of this property is to track the template that was supplied 
    * when the original request for the page was made. When assembling snippets
    * within a page, this value should be cleared for each snippet. This is
    * why the clone method clears it. However, there is one case where we don't
    * want it cleared by a clone, so that is managed by the assembler. 
    * <p>
    * @see #getOriginalTemplateGuid()
    * @see #setOriginalTemplateGuid(IPSGuid)
    */
   private IPSGuid m_originalTemplateGuid;

   /**
    * The reference id. Details at
    * {@link com.percussion.services.assembly.IPSAssemblyItem#getReferenceId()}
    */
   public long m_referenceId = 0;

   /**
    * The reference id. Details at
    * {@link com.percussion.services.assembly.IPSAssemblyItem#getUnpublishRefId()}
    */
   private Long m_unpublishRefId;

   private IPSGuid m_ownerId;
   
   /**
    * The folder id, may be <code>0</code>
    */
   public int m_folderId;

   /**
    * The site id, may be <code>null</code>
    */
   public IPSGuid m_siteid = null;

   /**
    * The site id, may be <code>null</code>
    */
   public Long m_pubserverid;

   /**
    * The job id. Details at
    * {@link com.percussion.services.assembly.IPSAssemblyItem#getJobId()}
    */
   private long m_jobId = 0;

   /**
    * Holds the invocation depth. Enables the assembly service to terminate
    * possible infinite loops
    */
   private int m_depth;

   /**
    * The bindings. The bindings are preset with a number of initial values as
    * details in the documentation, e.g. $sys.item and $sys.variables. After
    * these initial values are set, the bindings on the particular template are
    * evaluated, and the results are rebound into this map as well.
    */
   public Map<String, Object> m_bindings = new HashMap<>();

   /**
    * The node being assembled is stored here. This value is not set (initially)
    * for legacy assembly, although it is calculated on the fly in that case
    * when the {@link #getNode()} method is called.
    */
   private IPSNode m_node;

   /**
    * The item filter used in assembly. This is looked up in
    * {@link #getFilter()} on the first reference.
    */
   public IPSItemFilter m_filter;

   /**
    * The nav helper stores a bunch of common managed nav information for the
    * current tree of assembly requests. The requests are cloned across slot
    * evaluation, and the nav helper is copied as part of that cloning. This
    * allows all the related assembly requests to use the same nav helper, which
    * minimizes duplicate work in looking up the navons and navtree items, as
    * well as calculation of depth, axis, etc.
    */
   private PSNavHelper m_navHelper = null;

   /**
    * When performing Active Assembly, and for some previews, the engine should
    * use the most recent version of a content item if the item is checked out
    * to the user. This variable holds the target user. This data is propagated
    * in the clone operation, and therefore will be present for all snippets as
    * well as the parent item.
    */
   public String m_userName = null;

   /**
    * On cloning, this data member is set to the item being cloned. It is used
    * by active assembly to determine the owner item in an AA relationship.
    */
   private IPSAssemblyResult m_parentItem = null;

   /**
    * The name of the delivery location to use for the content on publishing.
    */
   private String m_deliveryType;

   /**
    * The location that the content should be published to.
    */
   private String m_deliveryPath;
   
   /**
    * The context for the delivery system, used primarily to record the delivery
    * context in the status record and to do any recalculation of location
    * for paginated content.
    */
   private int m_deliveryContext;
   
   /**
    * The original assembly url, primarily used for debugging in the publogs.
    */
   private String m_assemblyUrl;
   
   /**
    * Set by the assembly system, this records how long it took to assemble
    * the item.
    */
   private int m_elapsed;

   /**
    * The page number or <code>null</code> for no page number.
    */
   private Integer m_page;

   /**
    * The reference to the parent item if this is the status for a child
    * page, <code>null</code> otherwise.
    */
   private Long m_parentPageReference;
   
   /**
    * If this item is determined to be paginated, this should be set to 
    * <code>true</code>.
    */
   private boolean m_paginated = false;

   /**
    * The temporary directory for the result data, which is typically the
    * assembled content (before deliver to the target location). 
    * 
    * Default to <code>null</code>, which indicates to use the default 
    * temporary directory path.
    */
   private File m_tempDir = null;
   
   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyItem#getId()
    */
   public IPSGuid getId()
   {
      return m_id;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getSiteId()
    */
   public IPSGuid getSiteId()
   {
      if (m_siteid == null
            && getParameterValue(IPSHtmlParameters.SYS_SITEID, null) != null)
      {
         m_siteid = new PSGuid(PSTypeEnum.SITE, getParameterValue(
               IPSHtmlParameters.SYS_SITEID, null));
      }
      return m_siteid;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyItem#getPubServerId()
    */
   public Long getPubServerId()
   {
      return m_pubserverid;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyResult#getMimeType()
    */
   public String getMimeType()
   {
      return m_mimeType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyResult#getResultData()
    */
   public byte[] getResultData()
   {
      if (m_resultData != null)
         return m_resultData;
      else if (m_resultFile != null)
      {
         long len = m_resultFile.length();
         if (len > Integer.MAX_VALUE)
         {
            throw new IllegalStateException("File contains too much data: " +
                  len + " bytes");
         }
         try(ByteArrayOutputStream bos = new ByteArrayOutputStream((int) len)){
            try(InputStream io = new FileInputStream(m_resultFile)) {
               IOUtils.copy(io, bos);
               m_resultData = bos.toByteArray();
               m_resultFile.delete();
               m_resultFile = null;
               return m_resultData;
            }
         }
         catch (IOException e)
         {
            ms_log.error("Couldn't open temp file: " + m_resultFile, e);
            throw new RuntimeException(e);
         }
      }
      else
         return null;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyResult#getResultStream()
    */
   public InputStream getResultStream()
   {
      if (m_resultData != null)
      {
         return new ByteArrayInputStream(m_resultData);
      }
      else if (m_resultFile != null)
      {
         try
         {
            return new PSPurgableFileInputStream(m_resultFile);
         }
         catch (IOException e)
         {
            ms_log.error("Couldn't open temp file: " + m_resultFile, e);
            throw new RuntimeException(e);
         }
      }
      return null;
   }

   /**
    * Free up resources.
    */
   public void clearResults()
   {
      m_resultData = null;
      if (m_resultFile != null && ! m_fileReleased)
      {
         m_resultFile.release();
         m_resultFile = null;
         m_fileReleased = true;
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyResult#getStatus()
    */
   public Status getStatus()
   {
      return m_status;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getPath()
    */
   public String getPath()
   {
      return m_path;
   }

   public IPSGuid getOwnerId()
   {
      return m_ownerId;
   }
   
   public void setOwnerId(IPSGuid id)
   {
      m_ownerId = id;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getFolderId()
    */
   public int getFolderId()
   {
      return m_folderId;
   }

   /**
    * Set a new folder id
    * 
    * @param folderId the new folder id
    */
   public void setFolderId(int folderId)
   {
      m_folderId = folderId;
   }

   /**
    * Set a new site id
    * 
    * @param siteid the new site id
    */
   public void setSiteId(IPSGuid siteid)
   {
      m_siteid = siteid;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyItem#setPubServerId(com.percussion.utils.guid.IPSGuid)
    */
   public void setPubServerId(Long pubserverid)
   {
      m_pubserverid = pubserverid;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getParameters()
    */
   public Map<String, String[]> getParameters()
   {
      return m_parameters;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getVariables()
    */
   public Map<String, String> getVariables()
   {
      return m_variables;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getTemplate()
    */
   public IPSAssemblyTemplate getTemplate()
   {
      return m_template;
   }

   /*
    * //see base class method for details
    */
   public IPSGuid getOriginalTemplateGuid()
   {
      return m_originalTemplateGuid == null ? (m_template == null ? null
            : m_template.getGUID()) : m_originalTemplateGuid;
   }

   /**
    * This method is provided so assemblers can help manage the originating
    * template id. Generally, this method does not need to be called, but there
    * are special circumstances where this class cannot properly manage this
    * value. For example, the velocity assembler clones the item before
    * assembling the global template, which would clear this value. However, it
    * should not be cleared in that case. The velocity assembler should call
    * this method after cloning to reset this value to its state before the
    * clone.
    */
   public void setOriginalTemplateGuid(IPSGuid g)
   {
      m_originalTemplateGuid = g;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getReferenceId()
    */
   public long getReferenceId()
   {
      return m_referenceId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getUnpublishRefId()
    */
   public Long getUnpublishRefId()
   {
      return m_unpublishRefId;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getJobId()
    */
   public long getJobId()
   {
      return m_jobId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getBindings()
    */
   public Map<String, Object> getBindings()
   {
      if (m_bindings != null)
         return m_bindings;
      else
         return new HashMap<>();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getBindings()
    */
   public Map<String, Object> getMetaData()
   {
      Object sys = getBindings().get("$sys");
      if (sys==null)
         return null;
      
      if (!(sys instanceof Map))
      {
         ms_log.error("$sys is not a map");
         return null;
      }
      Map sysmap = (Map) sys;
      
      
      Object metadata = sysmap.get("metadata");
      
      if (metadata==null)
         return null;
      
      if (metadata != null && metadata instanceof Map)
      {
         return (Map<String, Object>)metadata;
      } else {
         ms_log.error("$sys.metadata is not a map");
      }
    
      return null;
      
      
   }

   /**
    * Set new bindings
    * 
    * @param bindings The bindings to set, may be <code>null</code>
    */
   public void setBindings(Map<String, Object> bindings)
   {
      m_bindings = bindings;
   }

   /**
    * Set a new id for the referenced content item
    * 
    * @param id The id to set, may be <code>null</code>
    */
   public void setId(IPSGuid id)
   {
      m_id = id;
   }

   /**
    * Set a new job id
    * 
    * @param jobId The jobId to set.
    */
   public void setJobId(long jobId)
   {
      m_jobId = jobId;
   }


   /*
    * //see base class method for details
    */
   public void setMimeType(String mimeType)
   {
      m_mimeType = mimeType;
   }

   /**
    * Set the new parameters
    * 
    * @param parameters The parameters to set, if <code>null</code> then the
    *           parameters are defaulted to an empty map
    */
   public void setParameters(Map<String, String[]> parameters)
   {
      if (parameters == null)
         m_parameters = new HashMap<>();
      else
         m_parameters = parameters;
   }

   /**
    * Put single value into parameter map, replacing any current value for the
    * named parameter
    * 
    * @param name the parameter name, never <code>null</code> or empty
    * @param value the value, never <code>null</code> or empty
    */
   public void setParameterValue(String name, String value)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      if (StringUtils.isBlank(value))
      {
         throw new IllegalArgumentException("value may not be null or empty");
      }
      m_parameters.put(name, new String[]
      {value});
   }

   public void removeParameterValue(String name)
   {
      removeParameter(name);
   }

   /**
    * Remove any value for the given parameter name
    * 
    * @param name the parameter name, never <code>null</code> or empty
    */
   public void removeParameter(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_parameters.remove(name);
   }

   /**
    * Set a new path, may be <code>null</code>
    * 
    * @param path The path to set.
    */
   public void setPath(String path)
   {
      m_path = path;
   }

   /**
    * Set a reference id
    * 
    * @param referenceId The referenceId to set.
    */
   public void setReferenceId(long referenceId)
   {
      m_referenceId = referenceId;
   }

   /**
    * Set the reference ID that the unpublishing was originated from.
    * @param unpubRefId the reference ID.
    */
   public void setUnpublishRefId(Long unpubRefId)
   {
      m_unpublishRefId = unpubRefId;
   }

   /*
    * //see base class method for details
    */
   public void setStatus(Status status)
   {
      m_status = status;
   }

   /**
    * @param variables The variables to set.
    */
   public void setVariables(Map<String, String> variables)
   {
      m_variables = variables;
   }

   /**
    * @param template The template to set.
    */
   public void setTemplate(IPSAssemblyTemplate template)
   {
      m_template = template;
      if (m_originalTemplateGuid == null && template != null)
         m_originalTemplateGuid = template.getGUID();
   }

   /*
    * //see base class method for details
    */
   public void setResultData(byte[] resultData)
   {       
      clearResults();
      if (resultData != null && resultData.length > THRESHOLD)
      {
         try {
            m_resultFile = new PSPurgableTempFile("result", ".tmp", m_tempDir);
            try (FileOutputStream os = new FileOutputStream(m_resultFile)) {
               try (ByteArrayInputStream is = new ByteArrayInputStream(resultData)) {
                  IOUtils.copy(is, os);
                  return;
               }
            }
         }

         catch (IOException e)
         {
            ms_log.error("Couldn't create temp file", e);
         }   

      }
      m_resultData = resultData;
   }

   /**
    * Store result data. The result data will be stored in a temporary file
    * in the file system using a {@link PSPurgableTempFile}. 
    * <p>
    * Call either this method or {@link #setResultData(byte[])}, but not both.
    * Calling this method will clear any previously stored result data, either
    * in memory or the file system.
    * 
    * @param is the result data stream, may be null. The input stream should
    * be closed by the caller.
    * 
    * @throws IOException 
    */
   public void setResultStream(InputStream is) throws IOException
   {
      clearResults();
      if (is == null)
      {
         m_resultFile = null;
      }
      else
      {
         m_resultFile = new PSPurgableTempFile("result", ".tmp", null);
         try(OutputStream os = new FileOutputStream(m_resultFile)){
            IOUtils.copy(is, os);
         }

      }
   }
   
   /**
    * Call this to enable using the edit revision if the item is checked out to
    * the user, and otherwise to use the current revision. Note that if this is
    * set, the node returned from {@link #getNode()} may not match a node set in
    * {@link #setNode(Node)} as the code in these routines will check the
    * version and replace the node in use if it is not the current or edit
    * reivision.
    * 
    * @param userName the user name, may be <code>null</code> but not empty
    */
   public void setUserName(String userName)
   {
      if (userName != null && StringUtils.isEmpty(userName))
      {
         throw new IllegalArgumentException(
               "The user name may be null but not empty");
      }
      m_userName = userName;
   }

   /**
    * Get the user name set, see {@link #setUserName(String)} to understand why
    * this value would be set.
    * 
    * @return the user name, may be <code>null</code> but never empty.
    */
   public String getUserName()
   {
      return m_userName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getNode()
    */
   public Node getNode()
   {
      if (m_node == null)
      {
         // If we've gotten here with a null node, we're probably processing
         // a legacy item. Load the node if this method is called
         if (m_id == null)
         {
            // If we aren't normalized then that's it
            return null;
         }

         IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
         List<IPSGuid> iguids = new ArrayList<>();
         iguids.add(m_id);
         PSContentMgrConfig config = new PSContentMgrConfig();
         config.addOption(PSContentMgrOption.LAZY_LOAD_CHILDREN);
         config.addOption(PSContentMgrOption.LOAD_MINIMAL);

         try
         {
            config.setBodyAccess(new PSInlineLinkProcessor(this.getFilter(),
                  this));
            List<Node> nodes = cmgr.findItemsByGUID(iguids, config);
            m_node = (IPSNode) nodes.get(0);
         }
         catch (RepositoryException e)
         {
            return null;
         }
         catch (PSFilterException e)
         {
            return null;
         }
      }
      return m_node;
   }

   /**
    * This is called if the revision is absent or set to <code>-1</code> in
    * order to find the "right" preview version. Assembly is never invoked
    * without a revision for publishing cases, only for preview. This method
    * therefore looks for the current or edit revision of the referenced item.
    * The current revision is used unless the item is checked out to the current
    * user as specified in {@link #setUserName(String)}.
    * 
    * @param cid the content id of the item
    * @return the current or edit guid for the given content id, never
    *         <code>null</code>
    */
   private IPSGuid calculateProperId(int cid)
   {
      IPSGuid rval;
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSComponentSummary sum = cms.loadComponentSummary(cid);
      int revision;
      if (sum != null)
      {
         revision = sum.getAAViewableRevision(m_userName);
      }
      else
      {
         throw new IllegalStateException("No summary found for contentid "
               + cid);
      }
      rval = gmgr.makeGuid(new PSLocator(cid, revision));
      String contentidstr = getParameterValue(IPSHtmlParameters.SYS_CONTENTID,
            null);
      if (StringUtils.isNotBlank(contentidstr))
      {
         setParameterValue(IPSHtmlParameters.SYS_REVISION, Integer
               .toString(revision));
      }
      return rval;
   }

   /**
    * Set a new node value, will also extract the guid value. The passed node
    * must be a Rhythmyx content node if not <code>null</code>.
    * 
    * @param node node value, may be <code>null</code>
    */
   public void setNode(Node node)
   {
      m_node = (IPSNode) node;
      if (node != null)
      {
         m_id = m_node.getGuid();
      }
      else
      {
         m_id = null;
      }
   }

   /**
    * Get the item filter, which limits the results from slot content finder
    * calls.
    * 
    * @return Returns the filter, if <code>null</code> then no filtering will
    *         occur.
    * @throws PSFilterException if there was a filter specified in the
    *            parameters, but it was not found
    */
   public IPSItemFilter getFilter() throws PSFilterException
   {
      if (m_filter == null)
      {
         String authtype = getParameterValue(IPSHtmlParameters.SYS_AUTHTYPE,
               null);
         String filter = getParameterValue(IPSHtmlParameters.SYS_ITEMFILTER,
               null);
         if ((StringUtils.isBlank(authtype) && StringUtils.isBlank(filter))
               || (!StringUtils.isBlank(authtype) && !StringUtils
                     .isBlank(filter)))
         {
            throw new PSFilterException(
                  IPSFilterServiceErrors.PARAMS_AUTHTYPE_OR_FILTER);
         }

         IPSFilterService filterservice = PSFilterServiceLocator
               .getFilterService();

         try
         {
            IPSItemFilter f = null;
            if (!StringUtils.isBlank(filter))
            {
               f = filterservice.findFilterByName(filter);
            }
            else
            {
               int at = Integer.parseInt(authtype);
               f = filterservice.findFilterByAuthType(at);
            }
            m_filter = f;
         }
         catch (NumberFormatException e)
         {
            throw new IllegalArgumentException("Authtype must be a number "
                  + authtype, e);
         }
      }
      return m_filter;
   }

   /**
    * The item filter, see {@link #getFilter()}
    * 
    * @param filter The filter to set.
    */
   public void setFilter(IPSItemFilter filter)
   {
      m_filter = filter;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#hasParameter(java.lang.String)
    */
   public boolean hasParameter(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      return m_parameters.containsKey(name);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getParameterValue(java.lang.String,
    *      java.lang.String)
    */
   public String getParameterValue(String name, String defaultvalue)
   {
      String values[] = m_parameters.get(name);
      if (values == null || values.length == 0
            || StringUtils.isBlank(values[0]))
         return defaultvalue;
      else
         return values[0];
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getParameterValues(java.lang.String,
    *      java.lang.String[])
    */
   public String[] getParameterValues(String name, String defaultvalues[])
   {
      String values[] = m_parameters.get(name);
      if (values == null || values.length == 0)
         return defaultvalues;
      else
         return values;
   }

   /*
    * //see base class method for details
    */
   @SuppressWarnings("unchecked")
   public IPSAssemblyItem pageClone()
   {
      try
      {
         PSAssemblyWorkItem copy = (PSAssemblyWorkItem) super.clone();
         copy.setBindings(new HashMap<>());
         copy.setParameters(PSCopier.deepCopy(getParameters()));
         copy.m_depth = m_depth + 1;
         if (copy.m_depth > 20)
         {
            throw new RuntimeException(
                  "Possible assembly loop detected - terminating");
         }
         copy.setDeliveryPath(null); 
         copy.setParentPageReferenceId(getReferenceId());
         copy.setReferenceId(
               PSGuidHelper.generateNextLong(PSTypeEnum.PUB_REFERENCE_ID));
         copy.setTemplate(null);
         copy.setFilter(null);
         copy.setNode(null);
         copy.m_navHelper = null;
         copy.setPaginated(false);
         copy.setId(getId());
         copy.clearResults();
         return copy;
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException("Impossible problem with cloning", e);
      }     
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#clone()
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object clone()
   {
      try
      {
         PSAssemblyWorkItem copy = (PSAssemblyWorkItem) super.clone();
         copy.setBindings(PSCopier.deepCopy(getBindings()));
         copy.setParameters(PSCopier.deepCopy(getParameters()));
         copy.m_parentItem = this;
         copy.m_depth = m_depth + 1;
         copy.m_originalTemplateGuid = null;

         if (copy.m_depth > 20)
         {
            throw new RuntimeException(
                  "Possible assembly loop detected - terminating");
         }
         return copy;
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException("Impossible problem with cloning", e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSAssemblyWorkItem))
         return false;
      PSAssemblyWorkItem b = (PSAssemblyWorkItem) obj;
      EqualsBuilder eb = new EqualsBuilder();
      
      eb.append(m_bindings, b.m_bindings).append(m_isDebug, b.m_isDebug)
            .append(m_filter, b.m_filter).append(m_folderId, b.m_folderId)
            .append(m_id, b.m_id).append(m_jobId, b.m_jobId)
            .append(m_mimeType, b.m_mimeType).append(m_node,
                  b.m_node).append(m_parameters, b.m_parameters).append(m_path,
                  b.m_path).append(m_referenceId, b.m_referenceId).append(
                  m_resultData, b.m_resultData).append(m_siteid, b.m_siteid)
            .append(m_status, b.m_status).append(m_template, b.m_template)
            .append(m_variables, b.m_variables)
            .append(m_deliveryContext, b.m_deliveryContext)
            .append(m_deliveryPath, b.m_deliveryPath)
            .append(m_deliveryType, b.m_deliveryType)
            .append(m_userName, b.m_userName);

      return eb.isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_id).append(m_jobId).append(
            m_mimeType).toHashCode();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this,
            ToStringStyle.MULTI_LINE_STYLE);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyResult#toResultString()
    */
   public String toResultString() throws IllegalStateException,
         UnsupportedEncodingException
   {
      if (!m_mimeType.startsWith("text/"))
      {
         throw new IllegalStateException(
               "The result must have a mimetype of text/something");
      }
      Charset cset = PSStringUtils.getCharsetFromMimeType(getMimeType());
      return new String(getResultData(), cset.name());
   }

   /**
    * Assigns the path field by loading the path for the folder id specified in
    * the <code>IPSHtmlParameters.SYS_FOLDERID</code> parameter map and making
    * sure it is one of the folders used by the item identified by the
    * <code>contentId</code> and <code>revision</code> method parameters.
    * <p>
    * If no folder id parameter is specified or that folder's path is not one
    * used by the item, the path field is assigned a pseudo path.
    * 
    * @param contentId content id of the item being assembled
    * @param revision revision of the item being assembled
    * @throws PSCmsException propagated if a path lookup fails
    */
   private void setPathFromFolderParam(int contentId, int revision)
         throws PSCmsException
   {
      try {
         String folderidParam = getParameterValue(IPSHtmlParameters.SYS_FOLDERID,
                 null);
         if (!StringUtils.isBlank(folderidParam) && !folderidParam.equals("0")) {
            PSRequest req = PSRequest.getContextForRequest();
            m_folderId = Integer.parseInt(folderidParam);
            PSServerFolderProcessor fproc = PSServerFolderProcessor.getInstance();


            String paths[] = fproc.getItemPaths(new PSLocator(m_folderId));
            String folderpath = paths[0] + "/";
            String itempaths[] = fproc.getItemPaths(new PSLocator(contentId,
                    revision));
            for (String ipath : itempaths) {
               if (ipath.startsWith(folderpath)) {
                  m_path = ipath;
                  break;
               }
            }
         }
         if (m_path == null) {
            // assign pseudo path when either the folder wasn't right or
            // no folder was supplied
            m_path = "/" + contentId + "#" + revision;
         }
      } catch (PSNotFoundException e) {
         throw new PSCmsException(e);
      }
   }

   /**
    * Normalizes the three identify methods of an assembly work item:
    * <ol>
    * <li>id [folderId]
    * <li>path
    * <li>sys_contentid/sys_revision [sys_folderid] parameters
    * </ol>
    * Whichever method has been used to identify the item, the other methods
    * will be set to match it. (note, no folder id if the item isn't in a folder
    * though)
    * 
    * @throws PSAssemblyException if no identity has been set, an incorrect
    *            identity has been set, or multiple identities have been set
    *            that do not refer to the same item.
    * 
    */
   public void normalize() throws PSAssemblyException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      String command = getParameterValue(IPSHtmlParameters.SYS_COMMAND, "");
      boolean isAA = command.equals("editrc");
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();

      try
      {
         sws.start(getClass().getName() + "#normalize");

         String contentidParam = getParameterValue(
               IPSHtmlParameters.SYS_CONTENTID, null);
         String revisionParam = getParameterValue(
               IPSHtmlParameters.SYS_REVISION, null);
         String folderIdParam = getParameterValue(
               IPSHtmlParameters.SYS_FOLDERID, null);

         String pseudoPath = null;

         // remember and clear any pseudo path (it will be processed later if
         // no other identity has been set)
         if (m_path != null && ms_pseudopath.matcher(m_path).matches())
         {
            pseudoPath = m_path.substring(1);
            m_path = null;
         }

         if (m_id != null)
         {
            /*
             * Use the id/folderId to assign parameters and path (if necessary)
             */
            PSLocator loc = gmgr.makeLocator(m_id);
            if (StringUtils.isBlank(contentidParam))
            {
               // assign id parameters
               setParameterValue(IPSHtmlParameters.SYS_CONTENTID, Integer
                     .toString(loc.getId()));
               setParameterValue(IPSHtmlParameters.SYS_REVISION, Integer
                     .toString(loc.getRevision()));
            }
            else
            {
               // id parameters are already set, make sure they match the guid
               int contentid = Integer.parseInt(contentidParam);
               if (contentid != loc.getId())
               {
                  throw new PSAssemblyException(
                        IPSAssemblyErrors.PARAMS_ITEM_ID_MISMATCH, new Object[]
                        {m_id, contentid});
               }
            }

            setFolderId(folderIdParam);

            if (m_path == null)
            {
               setPathFromFolderParam(loc.getId(), loc.getRevision());
            }
            else
            {
               // skipping path validation b/c it is expensive
            }
         }
         else if (StringUtils.isNotBlank(contentidParam))
         {
            /*
             * Use sys_contentid/sys_revision/sys_folderid parameters to assign
             * id/folderId and path (if necessary)
             */
            int contentid = Integer.parseInt(contentidParam);
            int revision = revisionParam != null ? Integer
                  .parseInt(revisionParam) : -1;

            if (m_id == null)
            {
               // If the revision isn't specified, or this is AA, then use the
               // current or edit revision
               if (revision < 0 || isAA)
               {
                  m_id = calculateProperId(contentid);
               }
               else
               {
                  m_id = gmgr.makeGuid(new PSLocator(contentid, revision));
               }
            }
            
            setFolderId(folderIdParam);

            if (m_path == null)
            {
               setPathFromFolderParam(contentid, revision);
            }
         }
         else if (StringUtils.isNotBlank(pseudoPath))
         {
            /*
             * Use pseudoPath to assign missing identities (when id is not set
             * and no id parameters)
             */
            String parts[] = pseudoPath.split("#");
            String contentidstr = parts[0];
            String revisionstr = parts[1];

            int contentid = Integer.parseInt(contentidstr);
            int revision = revisionstr != null
                  ? Integer.parseInt(revisionstr)
                  : -1;

            // If the revision isn't specified, or this is AA, then use the
            // current or edit revision
            if (revision < 0 || isAA)
            {
               m_id = calculateProperId(contentid);
            }
            else
            {
               m_id = gmgr.makeGuid(new PSLocator(contentid, revision));
            }

            setParameterValue(IPSHtmlParameters.SYS_CONTENTID, Integer
                  .toString(contentid));
            setParameterValue(IPSHtmlParameters.SYS_REVISION, Integer
                  .toString(revision));

            setPathFromFolderParam(contentid, revision);
         }
         else if (m_path != null)
         {
            /*
             * Use path to assign missing identities (when id is not set, no id
             * parameters, and no pseudo path)
             */
            PSRequest req = PSRequest.getContextForRequest();
            PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();

            int lastslash = m_path.lastIndexOf("/");
            if (lastslash < 0)
            {
               throw new PSAssemblyException(IPSAssemblyErrors.INVALID_PATH,
                     m_path);
            }
            String partialpath = m_path.substring(0, lastslash);
            try
            {
               m_folderId = proc.getIdByPath(
                     PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
                     partialpath, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
               if (m_folderId == -1)
                  throw new PSAssemblyException(IPSAssemblyErrors.MISSING_PATH,
                        partialpath);
            }
            catch (PSCmsException ee)
            {
               throw new PSAssemblyException(IPSAssemblyErrors.INVALID_PATH,
                     partialpath);
            }
            if (m_folderId != 0)
            {
               setParameterValue(IPSHtmlParameters.SYS_FOLDERID, Integer
                     .toString(m_folderId));
            }
            String parts[] = m_path.split("#");
            int rev = 0;
            if (parts.length > 1)
            {
               m_path = parts[0];
               rev = Integer.parseInt(parts[1]);
            }
            PSComponentSummary sum = proc.getSummaryByPath(
                  PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, m_path,
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT);
            if (rev == 0 || isAA)
            {
               rev = sum.getAAViewableRevision(m_userName);
            }
            int contentid = sum.getContentId();

            if (m_id == null)
            {
               m_id = gmgr.makeGuid(new PSLocator(contentid, rev));
            }

            setParameterValue(IPSHtmlParameters.SYS_CONTENTID, Integer
                  .toString(contentid));
            setParameterValue(IPSHtmlParameters.SYS_REVISION, String
                  .valueOf(Integer.toString(rev)));

         }

         // At this point, all three identities should be assigned
         if (m_id == null)
            throw new PSAssemblyException(IPSAssemblyErrors.PARAMS_ITEM_SPEC);
         if (m_path == null)
            throw new PSAssemblyException(IPSAssemblyErrors.PARAMS_ITEM_SPEC);
         if (StringUtils.isBlank(getParameterValue(
               IPSHtmlParameters.SYS_CONTENTID, null)))
            throw new PSAssemblyException(IPSAssemblyErrors.PARAMS_ITEM_SPEC);

      }
      catch (PSCmsException e)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.ITEM_CREATION, e);
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Sets the folder ID from the specified folder ID in string format.
    * 
    * @param folderIdParam the folder ID in string format, it may be blank.
    * 
    * @throws PSAssemblyException if the internal folder ID does not agree with the specified one.
    */
   private void setFolderId(String folderIdParam) throws PSAssemblyException
   {
      if (StringUtils.isBlank(folderIdParam))
      {
         if (m_folderId > 0)
         {
            // set folder parameter from the field (if assigned)
            setParameterValue(IPSHtmlParameters.SYS_FOLDERID, Integer
                  .toString(m_folderId));
         }
      }
      else if (m_folderId > 0)
      {
         // verify folder field and parameter have the same value
         int folderId = Integer.parseInt(folderIdParam);
         if (folderId != m_folderId)
         {
            throw new PSAssemblyException(
                  IPSAssemblyErrors.PARAMS_ITEM_FOLDER_MISMATCH,
                  new Object[]
                  {m_folderId, folderId});
         }
      }
      else
      {
         // set the folder field from the sys_folderid parameter
         m_folderId = Integer.parseInt(folderIdParam);
      }
   }

   /**
    * The work item holds a navigation helper, which contains information that
    * can be used while assembling any part of an item, either the page, global
    * page or snippets. The reference is cloned to subordinate requests.
    * 
    * @return the nav helper, never <code>null</code>
    */
   public PSNavHelper getNavHelper()
   {
      if (m_navHelper == null)
      {
         m_navHelper = new PSNavHelper(this);
      }
      return m_navHelper;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#isDebug()
    */
   public boolean isDebug()
   {
      return m_isDebug;
   }

   /**
    * Set if this work item is in debug mode
    * 
    * @param isDebug <code>true</code> if it is in debug mode
    */
   public void setDebug(boolean isDebug)
   {
      m_isDebug = isDebug;
   }

   /**
    * Is the node set on this work item, does not calculate the node if not
    * present
    * 
    * @return <code>true</code> if the node is present
    */
   public boolean hasNode()
   {
      return m_node != null;
   }

   /**
    * Set the publish state. A state of <code>false</code> means that this
    * assembly item is for an unpublish.
    * 
    * @param pub the new state
    */
   public void setPublish(boolean pub)
   {
      m_isPublish = pub;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#isPublish()
    */
   public boolean isPublish()
   {
      return m_isPublish;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssemblyItem#getCloneParentItem()
    */
   public IPSAssemblyItem getCloneParentItem()
   {
      return m_parentItem;
   }

   /**
    * Get the assembly context based on the parameters
    * 
    * @return the context value, or <code>-1</code> if the parameter cannot be
    *         found.
    */
   public int getContext()
   {
      return Integer.parseInt(getParameterValue(IPSHtmlParameters.SYS_CONTEXT,
            "-1"));
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyResult#getResultLength()
    */
   public long getResultLength()
   {
      if (m_resultData != null)
      {
         return m_resultData.length;
      }
      else if (m_resultFile != null)
      {
         return m_resultFile.length();
      }
      else
      {
         return -1;
      }
   }

   public String getDeliveryType()
   {
      return m_deliveryType;
   }

   public String getDeliveryPath()
   {
      return m_deliveryPath;
   }
   

   /*
    * //see base class method for details
    */
   public int getDeliveryContext()
   {
      return m_deliveryContext;
   }
   
   /**
    * Get the delivery context ID of the item.
    * @return the GUID of the context ID, never <code>null</code>.
    */
   public IPSGuid getDeliveryContextId()
   {
      return new PSGuid(PSTypeEnum.CONTEXT, m_deliveryContext);
   }
   
   /**
    * Set the delivery context
    * 
    * @param context the context being used for the delivery location
    */
   public void setDeliveryContext(int context)
   {
      m_deliveryContext = context;
   }

   /**
    * @param deliveryType the deliveryType to set, may not be 
    * <code>null</code> or empty.
    */
   public void setDeliveryType(String deliveryType)
   {
      if (StringUtils.isBlank(deliveryType))
      {
         throw new IllegalArgumentException(
               "deliveryType may not be null or empty");
      }
      m_deliveryType = deliveryType;
   }

   /**
    * @param deliveryPath the deliveryPath to set, may be <code>null</code>
    * or empty.
    */
   public void setDeliveryPath(String deliveryPath)
   {
      m_deliveryPath = deliveryPath;
   }


   /*
    * //see base class method for details
    */
   public String getAssemblyUrl()
   {
      return m_assemblyUrl;
   }

   /**
    * Set the assembly url.
    * @param assemblyUrl the assembly url, may be <code>null</code> or empty.
    */
   public void setAssemblyUrl(String assemblyUrl)
   {
      m_assemblyUrl = assemblyUrl;
   }

   /**
    * Get the time that it took for this work item to be assembled. Set by 
    * the assembly system. Not valid before assembly.
    * @return the time in milliseconds.
    */
   public int getElapsed()
   {
      return m_elapsed;
   }

   /**
    * Set the time in milliseconds for the assembly.
    * @param elapsed the time in milliseconds.
    */
   public void setElapsed(int elapsed)
   {
      m_elapsed = elapsed;
   }

   public boolean isSuccess()
   {     
      return m_status != null && m_status.equals(Status.SUCCESS);
   }

   /**
    * Determines if the result is stored in a file.
    * 
    * @return <code>true</code> if the result is stored in a file;
    * otherwise return <code>false</code>.
    */
   public boolean isResultInFile()
   {
      return m_resultFile != null;
   }
   
   public PSPurgableTempFile getResultFile() throws IOException
   {
      if (m_resultFile == null)
      {
         OutputStream os = null;
         try
         {
            m_resultFile = new PSPurgableTempFile("delivery",
                  ".tmp", m_tempDir);
            os = new FileOutputStream(m_resultFile);
            IOUtils.write(getResultData(), os);
         }
         finally
         {
            IOUtils.closeQuietly(os);
         }
      }
      
      m_fileReleased = true;
      return m_resultFile;
   }

   public Integer getPage()
   {
      return m_page;
   }
   
   /**
    * Set the page number
    * @param page the page number, may be <code>null</code> 
    */
   public void setPage(Integer page)
   {
      m_page = page;
   }

   public Long getParentPageReferenceId()
   {
      return m_parentPageReference;
   }
  
   /**
    * Set the parent reference id
    * @param refid the parent reference id, may be <code>null</code>.
    */
   public void setParentPageReferenceId(long refid)
   {
      m_parentPageReference = refid;
   }

   /**
    * Set the paginated state
    * @param value <code>true</code> if paginated.
    */
   public void setPaginated(boolean value)
   {
      m_paginated = value;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyResult#isPaginated()
    */
   public boolean isPaginated()
   {
      return m_paginated;
   }   
   
   /*
    * //see base interface method for details
    */
   public void setTempDir(File tmpDir)
   {
      m_tempDir = tmpDir;
   }

}
