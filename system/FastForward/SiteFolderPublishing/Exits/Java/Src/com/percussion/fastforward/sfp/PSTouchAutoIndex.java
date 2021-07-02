/******************************************************************************
 *
 * [ PSTouchAutoIndex.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.fastforward.sfp;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.utils.PSRelationshipHelper;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.cache.PSCacheException;
import com.percussion.server.cache.PSCacheProxy;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * A Rhythmyx extension that touches the last modify date and last modifier for
 * all AutoIndex variants registered in the system. Add this extension to a
 * content list generator resource. Items will be touched regardless of their
 * state.
 */
public class PSTouchAutoIndex extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor,
         IPSRequestPreProcessor
{
   /**
    * Default constructor.
    */
   public PSTouchAutoIndex()
   {
      super();
   }

   /**
    * This extension never modifies the stylesheet.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Process the result document. This method is called when this extension is
    * a post-exit. See the class header for details.
    * 
    * @param params see the parameter table in the class header.
    * @param req the parent request context.
    * @param resultDoc the xml result document for the resource.
    * @see com.percussion.extension.IPSResultDocumentProcessor#
    *      processResultDocument(java.lang.Object[], com.percussion.server.
    *      IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext req, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      touchAutoIndex(req);
      return resultDoc;
   }

   /**
    * Process the request. This method is call when this extension is a
    * pre-exit.
    * <p>
    * If the parent request contains the <code>psfirst</code> HTML parameter
    * then this method will return without performing any actions. This allows
    * this exit to be used on content list generators whioh contain the Result
    * Pager.
    * </p>
    * 
    * @param params see the parameter table in the class header.
    * @param req the parent request context.
    * @see com.percussion.extension.IPSRequestPreProcessor#
    *      preProcessRequest(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext req)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      touchAutoIndex(req);
   }

   /**
    * Touches all autoindex pages and their parents. This is the common routine
    * used for both pre and post exit processing.
    * <p>
    * If the parent request contains the <code>psfirst</code> HTML parameter
    * then this method will return without performing any actions. This allows
    * this exit to be used on content list generators whioh contain the Result
    * Pager.
    * </p>
    * 
    * @param req the parent request
    * @throws PSExtensionProcessingException
    */
   @SuppressWarnings("unchecked")
   private void touchAutoIndex(IPSRequestContext req)
         throws PSExtensionProcessingException
   {
      String first = req.getParameter("psfirst");
      if (first != null && first.trim().length() > 0)
      {
         //pager was specified... this isn't the first request
         m_log.debug("result pager detected, items will not be flushed");
         return;
      }
      try
      {
         Set<IPSGuid> contentTypes = buildContentTypeSet(req);
         IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
         Collection<Integer> cids = pub.touchContentTypeItems(contentTypes);

         for(Integer id : cids)
         {
            PSCacheProxy.flushAssemblers(null, id, null, null);
            m_log.debug("Flushed item " + id);
         }

      }
      catch (Exception e)
      {
         m_log.error(getClass().getName(), e);
         throw new PSExtensionProcessingException(getClass().getName(), e);
      }

   }

   /**
    * Builds a set of content types. All content types that have at least one
    * automated index variant will be included. The content types are
    * <code>Integer</code> objects.
    * 
    * @param req the parent request.
    * @return a list of content types.
    * @throws PSCmsException
    * @throws PSUnknownNodeTypeException
    * @throws PSCacheException
    */
   @SuppressWarnings({"deprecation","unchecked"})
   private Set<IPSGuid> buildContentTypeSet(IPSRequestContext req)
         throws PSCmsException, PSUnknownNodeTypeException, PSCacheException
   {
      Set<IPSGuid> ctSet = new HashSet<>();

      PSRelationshipHelper helper = new PSRelationshipHelper(req);
      Iterator variants = helper.getVariantSet().iterator();
      while (variants.hasNext())
      {
         PSContentTypeVariant vart = (PSContentTypeVariant) variants.next();
         if (vart.getActiveAssemblyType() == 1)
         {
            m_log.debug("Found automated variant " + vart.getName() + " - "
                  + vart.getVariantId());
            for(Long type : vart.getContentTypes())
            {
               ctSet.add(new PSGuid(PSTypeEnum.NODEDEF,type));
            }
            PSCacheProxy.flushAssemblers(null, null, null, new Integer(vart
                  .getVariantId()));

         }
      }
      return ctSet;
   }

   /**
    * Handles log messages.
    */
   Logger m_log = LogManager.getLogger(this.getClass());

}
