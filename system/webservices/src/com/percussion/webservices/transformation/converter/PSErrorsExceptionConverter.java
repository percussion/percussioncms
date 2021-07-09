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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.faults.PSError;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallError;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallSuccess;
import com.percussion.webservices.faults.PSLockFault;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

/**
 * Converts objects between the classes 
 * <code>com.percussion.webservices.PSErrorsException</code> and 
 * <code>com.percussion.webservices.faults.PSErrorsFault</code>.
 */
public class PSErrorsExceptionConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSErrorsExceptionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (value instanceof PSErrorsFault)
      {
         PSErrorsFault source = (PSErrorsFault) value;

         PSErrorsException target = new PSErrorsException();
         
         PSErrorsFaultServiceCall[] calls = source.getServiceCall();
         for (PSErrorsFaultServiceCall call : calls)
         {
            if (call.getSuccess() != null)
            {
               target.addResult(new PSDesignGuid(call.getSuccess().getId()));
            }
            else
            {
               PSErrorsFaultServiceCallError error = call.getError();
               Object errorValue = error.getPSError();
               Class errorType = PSErrorException.class;
               if (errorValue == null)
               {
                  errorValue = error.getPSLockFault();
                  errorType = PSLockErrorException.class;
               }
               
               if (errorValue == null)
                  throw new ConversionException(
                     "No error value found for PSErrorsFault converter.");
                  
               Converter converter = getConverter(errorType);
               target.addError(new PSDesignGuid(error.getId()), 
                  converter.convert(errorType, errorValue));
            }
         }
         
         return target;
      }
      else
      {
         PSErrorsException source = (PSErrorsException) value;

         PSErrorsFault target = new PSErrorsFault();
         
         PSErrorsFaultServiceCall[] calls = 
            new PSErrorsFaultServiceCall[source.getIds().size()];
         target.setServiceCall(calls);
         
         int index = 0;
         Map<IPSGuid, Object> errors = source.getErrors();
         for (IPSGuid id : source.getIds())
         {
            PSErrorsFaultServiceCall call = new PSErrorsFaultServiceCall();
            Object sourceError = errors.get(id);
            if (sourceError == null)
            {
               PSErrorsFaultServiceCallSuccess success = 
                  new PSErrorsFaultServiceCallSuccess();
               success.setId((new PSDesignGuid(id)).getValue());
               
               call.setSuccess(success);
            }
            else
            {
               Converter converter = getConverter(sourceError.getClass());

               PSErrorsFaultServiceCallError error = 
                  new PSErrorsFaultServiceCallError();
               error.setId((new PSDesignGuid(id)).getValue());
               if (sourceError instanceof PSLockErrorException)
               {
                  Class resultType = PSLockFault.class;
                  Object resultValue = converter.convert(resultType, 
                     sourceError);
                  error.setPSLockFault((PSLockFault) resultValue);
               }
               else if (sourceError instanceof PSErrorException)
               {
                  Class resultType = PSError.class;
                  Object resultValue = converter.convert(resultType, 
                     sourceError);
                  error.setPSError((PSError) resultValue);
               }
               else
                  throw new ConversionException(
                     "Unsupported PSErrorsException error type.");
               
               call.setError(error);
            }
            
            calls[index++] = call;
         }
         
         return target;
      }
   }
}

