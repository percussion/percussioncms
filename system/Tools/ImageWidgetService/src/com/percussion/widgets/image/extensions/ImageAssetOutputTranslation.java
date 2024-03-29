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

package com.percussion.widgets.image.extensions;

      import com.percussion.design.objectstore.PSLocator;
      import com.percussion.error.PSExceptionUtils;
      import com.percussion.extension.IPSExtensionDef;
      import com.percussion.extension.IPSItemOutputTransformer;
      import com.percussion.extension.PSDefaultExtension;
      import com.percussion.extension.PSExtensionException;
      import com.percussion.extension.PSExtensionParams;
      import com.percussion.extension.PSExtensionProcessingException;
      import com.percussion.extension.PSParameterMismatchException;
      import com.percussion.server.IPSRequestContext;
      import com.percussion.services.contentmgr.IPSContentMgr;
      import com.percussion.services.contentmgr.PSContentMgrLocator;
      import com.percussion.services.guidmgr.IPSGuidManager;
      import com.percussion.services.guidmgr.PSGuidManagerLocator;
      import com.percussion.tools.PSCopyStream;
      import com.percussion.utils.guid.IPSGuid;
      import com.percussion.widgets.image.data.ImageData;
      import com.percussion.widgets.image.services.ImageCacheManager;
      import com.percussion.widgets.image.services.ImageCacheManagerLocator;
      import org.apache.commons.lang.StringUtils;
      import org.apache.logging.log4j.LogManager;
      import org.apache.logging.log4j.Logger;
      import org.w3c.dom.Document;

      import javax.jcr.Node;
      import javax.jcr.PathNotFoundException;
      import javax.jcr.RepositoryException;
      import javax.jcr.ValueFormatException;
      import java.io.ByteArrayOutputStream;
      import java.io.File;
      import java.io.IOException;
      import java.io.InputStream;
      import java.util.Collections;
      import java.util.List;
      
      public class ImageAssetOutputTranslation extends PSDefaultExtension
        implements IPSItemOutputTransformer
      {
    	  private static final Logger log = LogManager.getLogger(ImageAssetOutputTranslation.class);
      
    	  private IPSGuidManager gmgr = null;
    	  private IPSContentMgr cmgr = null;
    	  private ImageCacheManager cacheManager = null;
      
        public boolean canModifyStyleSheet()
        {
        	return false;
        }

        @Override
        public void init(IPSExtensionDef def, File codeRoot)
          throws PSExtensionException
        {
        	super.init(def, codeRoot);
        	if (this.gmgr == null)
          {
        		this.gmgr = PSGuidManagerLocator.getGuidMgr();
          }
        	if (this.cacheManager == null)
          {
        		this.cacheManager = ImageCacheManagerLocator.getImageCacheManager();
          }
        
        	if (this.cmgr == null)
          {
        		this.cmgr = PSContentMgrLocator.getContentMgr();
          }
        }

        @Override
        public Document processResultDocument(Object[] params, IPSRequestContext request, Document resultDoc)
          throws PSParameterMismatchException, PSExtensionProcessingException
        {
          try
          {
        	  PSExtensionParams ep = new PSExtensionParams(params);
        	  String imageName = ep.getStringParam(0, "img", false);
        	  String thumbName = ep.getStringParam(1, "img2", false);
      
        	  Node node = findNode(request);
        	  if (node != null)
            {
        		  String imageId = loadImageData(node, imageName);
        		  if (imageId != null)
              {
        			  PSItemXMLSupport.setFieldValue(resultDoc, imageName + "_id", imageId);
              }
      
        		  String thumbId = loadImageData(node, thumbName);
        		  if (thumbId != null)
              {
        			  PSItemXMLSupport.setFieldValue(resultDoc, thumbName + "_id", thumbId);
              }
            }
          }
          catch (Exception ex) {
        	  log.error("Unexpected Exception: {}",
                      PSExceptionUtils.getMessageForLog(ex));
        	  throw new PSExtensionProcessingException(getClass().getName(), ex);
          }
      
          return resultDoc;
        }
      
        protected Node findNode(IPSRequestContext request) throws RepositoryException
        {
        	String contentId = request.getParameter("sys_contentid");
        	if (StringUtils.isBlank(contentId))
        	{
        		log.debug("no content id, must be a new item");
        		return null;
        	}
        	
        	String revision = request.getParameter("sys_revision");
        	IPSGuid guid = this.gmgr.makeGuid(new PSLocator(contentId, revision));
      
        	List nodes = this.cmgr.findItemsByGUID(Collections.singletonList(guid), null);
        	if (nodes.size() < 1)
          {
        		log.warn("Item not found for GUID " + guid);
                return null;
          }
        	return (Node)nodes.get(0);
        }
      
        protected String loadImageData(Node node, String base)
          throws ValueFormatException, PathNotFoundException, RepositoryException, IOException
        {
        	ImageData iData = new ImageData();
        	Long size = Long.valueOf(node.getProperty(base + "_size").getLong());
        	if (size.longValue() == 0L)
        	{
        		log.debug("no image for property " + base);
        		return null;
          }
        	iData.setSize(size.intValue());
        	try(InputStream is = node.getProperty(base).getStream()) {
                try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    PSCopyStream.copyStream(is, baos);
                    iData.setBinary(baos.toByteArray());
                    iData.setExt(node.getProperty(base + "_ext").getString());
                    iData.setFilename(node.getProperty(base + "_filename").getString());
                    iData.setMimeType(node.getProperty(base + "_type").getString());
                    Long height = Long.valueOf(node.getProperty(base + "_height").getLong());
                    iData.setHeight(height.intValue());
                    Long width = Long.valueOf(node.getProperty(base + "_width").getLong());
                    iData.setWidth(width.intValue());

                    String imageKey = this.cacheManager.addImage(iData);

                    return imageKey;
                }
            }
        }
      
        protected void setGmgr(IPSGuidManager gmgr)
        {
        	this.gmgr = gmgr;
        }
      
        protected void setCacheManager(ImageCacheManager cacheManager)
        {
        	this.cacheManager = cacheManager;
        }
      
        protected void setCmgr(IPSContentMgr cmgr)
        {
        	this.cmgr = cmgr;
        }
      }
