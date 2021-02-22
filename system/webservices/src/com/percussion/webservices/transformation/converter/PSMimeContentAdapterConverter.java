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
package com.percussion.webservices.transformation.converter;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.util.PSBase64Decoder;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSCharSetsConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts between {@link PSMimeContentAdapter} and
 * {@link com.percussion.webservices.system.PSMimeContentAdapter}.
 */
public class PSMimeContentAdapterConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}.
    * 
    * @param beanUtils
    */
   public PSMimeContentAdapterConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      
      m_specialProperties.add("attachmentId");
      m_specialProperties.add("content");
      m_specialProperties.add("description");
      m_specialProperties.add("gUID"); 
      m_specialProperties.add("href");      
   }

   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      if (isClientToServer(value))
      {
         com.percussion.webservices.system.PSMimeContentAdapter orig = 
            (com.percussion.webservices.system.PSMimeContentAdapter) value;
         
         PSMimeContentAdapter dest = (PSMimeContentAdapter) result;
         
         // handle guid
         dest.setGUID(new PSGuid(PSTypeEnum.CONFIGURATION, orig.getId()));
         
         // handle attachment id or content
         String href = orig.getHref();
         if (href != null)
         {
            dest.setAttachmentId(Long.parseLong(href));
         }

         if (!dest.isContentAttached())
         {
            String enc = orig.getTransferEncoding(); 
            if (!enc.equals(IPSMimeContentTypes.MIME_ENC_BASE64))
            {
               throw new RuntimeException("Unsupported transfer encoding: " + 
                  enc);
            }
            
            // expecting base64 encoded string, so decode it:
            String stringValue = (String) orig.getContent();
            try(ByteArrayInputStream iBuf = new ByteArrayInputStream(
                  stringValue.getBytes(dest.getCharacterEncoding()))){

               try(ByteArrayOutputStream oBuf = new ByteArrayOutputStream()) {

                  PSBase64Decoder.decode(iBuf, oBuf);

                  dest.setContent(new ByteArrayInputStream(oBuf.toByteArray()));
               }
            }
            catch (IOException e)
            {
               throw new RuntimeException("Error converting mime content: " + 
                  e.getLocalizedMessage(), e);
            }
         }
      }
      else
      {
         PSMimeContentAdapter orig = (PSMimeContentAdapter) value;
         
         com.percussion.webservices.system.PSMimeContentAdapter dest = 
            (com.percussion.webservices.system.PSMimeContentAdapter) result;

         // handle guid
         PSDesignGuid guid = new PSDesignGuid(orig.getGUID());
         dest.setId(guid.getValue());
         
         // handle href or content
         if (orig.isContentAttached())
         {
            dest.setHref(String.valueOf(orig.getAttachmentId()));
         }
         else
         {
            try(InputStream in = orig.getContent()){
               try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                  PSBase64Encoder.encode(in, out);
                  dest.setContent(out.toString(PSCharSetsConstants.rxStdEnc()));
                  dest.setCharacterEncoding(PSCharSetsConstants.rxStdEnc());
               }
            }
            catch (IOException e)
            {
               throw new RuntimeException("Error converting mime content: " + 
                  e.getLocalizedMessage(), e);
            }
         }
      }
      
      return result;
   }

}

