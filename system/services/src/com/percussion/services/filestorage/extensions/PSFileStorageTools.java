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
package com.percussion.services.filestorage.extensions;

import static org.apache.commons.lang.StringUtils.endsWith;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.filestorage.IPSFileMeta;
import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.PSFileStorageServiceLocator;
import com.percussion.services.filestorage.data.PSFakeBlob;
import com.percussion.utils.jsr170.PSValueFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Iterator;

import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tools for String manipulation. Just basics for now
 * 
 * @author DavidBenua
 * 
 */
public class PSFileStorageTools extends PSJexlUtilBase
      implements
         IPSJexlExpression
{

   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSFileStorageTools.class);

   /**
    * Default constructor.
    */
   public PSFileStorageTools()
   {
   }

    @IPSJexlMethod(description = "Looks up the binary to stream based upon a digest hash lookup, " +
          "this returns the passed in 'prop' value if hash is empty or does not exist", params =
    {@IPSJexlParam(name = "prop", description = "the property to use if the file is not stored"),
            @IPSJexlParam(name = "hash", description = "the lookup hash for the file")})
    public Object getFileFromHash(Object prop, String hash) throws PSAssemblyException
    {
        if (hash==null || StringUtils.isBlank(hash)) 
                return prop;
        Object ret = null;
        IPSFileStorageService fss = PSFileStorageServiceLocator.getFileStorageService();
        try(InputStream is = fss.getStream(hash)) {
            if (is != null) {
                Blob blob = new PSFakeBlob(is);
                try {
                    ret = PSValueFactory.createValue(blob);
                } catch (ValueFormatException e) {
                    log.error("Error getting hashed binary stream:", e);
                    throw new PSAssemblyException(IPSAssemblyErrors.HASHED_BINARY_ERROR, "PSFileStorageTools",
                            e.getLocalizedMessage());
                }
            } else {
                ret = prop;
            }
            return ret;
        } catch (IOException e) {
            log.error("Error getting hashed binary stream:", e);
            throw new PSAssemblyException(IPSAssemblyErrors.HASHED_BINARY_ERROR, "PSFileStorageTools",
                    e.getLocalizedMessage());
        }
    }

    @IPSJexlMethod(description = "Looks up the binary to stream based upon a digest hash lookup", params =
    {@IPSJexlParam(name = "hash", description = "the lookup hash for the file")})
    public Object getFileFromHash(String hash) throws PSAssemblyException
    {
        if (hash==null || StringUtils.isBlank(hash)) 
            throw new PSAssemblyException(IPSAssemblyErrors.HASHED_BINARY_NO_HASH, new Object[] {});
        Object ret = null;
        IPSFileStorageService fss = PSFileStorageServiceLocator.getFileStorageService();
        try(InputStream is = fss.getStream(hash)) {
            if (is != null) {
                Blob blob = new PSFakeBlob(is);
                try {
                    ret = PSValueFactory.createValue(blob);
                } catch (ValueFormatException e) {
                    log.error("Error getting hashed binary stream:", e);
                    throw new PSAssemblyException(IPSAssemblyErrors.HASHED_BINARY_ERROR, "PSFileStorageTools",
                            e.getLocalizedMessage());
                }
            } else {
                throw new PSAssemblyException(IPSAssemblyErrors.HASHED_BINARY_NOT_FOUND, new Object[]{hash});
            }
            return ret;
        } catch (IOException e) {
            log.error("Error getting hashed binary stream:", e);
            throw new PSAssemblyException(IPSAssemblyErrors.HASHED_BINARY_ERROR, "PSFileStorageTools",
                    e.getLocalizedMessage());
        }
    }
   
   @IPSJexlMethod(description = "Gets metadata properties for a file based upon a digest hash", params =
   {@IPSJexlParam(name = "hash", description = "the digest hash hex encoded")})
   public IPSFileMeta getMeta(String hash)
   {
      IPSFileStorageService fss = PSFileStorageServiceLocator
            .getFileStorageService();
      return fss.getMeta(hash);

   }

   @IPSJexlMethod(description = "Gets textual represntation of a hashed document", params =
   {@IPSJexlParam(name = "hash", description = "the digest hash hex encoded")})
   public String getText(String hash)
   {
      IPSFileStorageService fss = PSFileStorageServiceLocator
            .getFileStorageService();
      return fss.getText(hash);
   }
   
   
   /**
    * Determines if the content field is a hash content field.
    * A hash content field uses the IPSFileStorageService for 
    * efficient file storage.
    * @param item
    * @param contentType
    * @return true if it as hash field which needs special processing.
    */
   public static boolean isHashField(Number contentTypeId, String fieldName)
   {
      notNull(contentTypeId);
      notEmpty(fieldName);
      if ( ! endsWith(fieldName, 
            IPSHashFileInfoExtension.HASH_PARAM_SUFFIX) ) return false;
      PSItemDefinition def = getItemDefinition(contentTypeId);
      IPSExtensionManager em = PSServer.getExtensionManager(null);
      PSPipe pipe = def.getContentEditor().getPipe();
      if (pipe != null && pipe.getInputDataExtensions() != null) {
         Iterator<?> it = pipe.getInputDataExtensions().iterator();
         while (it.hasNext()) {
            PSExtensionCall pc = (PSExtensionCall) it.next();
            PSExtensionRef ref = pc.getExtensionRef();
            IPSExtension ext;
            try
            {
               ext = em.prepareExtension(ref, null);
            }
            catch (Exception e)
            {
               log.error("Failure in loading extensions for content type: " 
                     + contentTypeId , e);
               continue;
            }
            if (ext instanceof IPSHashFileInfoExtension) {
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Gets the content type definition for the item.
    * @param item not null and content type id must be set.
    * @return not null.
    * @throws RuntimeException if the content type is not found.
    */
   private static PSItemDefinition getItemDefinition(Number contentTypeId) 
   {
      try
      {
         return PSItemDefManager.getInstance().getItemDef(
               contentTypeId.longValue(),
               PSItemDefManager.COMMUNITY_ANY);
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new RuntimeException(e);
      }
   }
   
}
