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
package com.percussion.data;

import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.extension.PSExtensionException;
import com.percussion.util.PSUrlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * This is the runtime representation of the PSUrlRequest object. It is a
 * compact form that can generate a result based on the execution data of a
 * request. The URL is generated one of 2 ways: either via a UDF or from the
 * pieces in the replacement value used to create this extractor.
 */
public class PSUrlRequestExtractor extends PSDataExtractor
{
   /**
    * Creates an efficient representation of the supplied request definition
    * that can be executed at runtime.
    *
    * @param req The definition used to create this object. Never <code>null
    *    </code>.
    */
   public PSUrlRequestExtractor( PSUrlRequest req )
      throws PSExtensionException, PSNotFoundException
   {
      super( req );
      if ( null == req )
         throw new IllegalArgumentException( "Request param cannot be null." );

      PSExtensionCall call = req.getConverter();
      if ( null != call )
         m_udfExtractor = new PSUdfCallExtractor( call );
      else
      {
         Iterator params = req.getQueryParameters();
         List extractors = new ArrayList(5);
         while ( params.hasNext())
         {
            PSParam param = (PSParam) params.next();
            extractors.add( PSDataExtractorFactory.
                  createReplacementValueExtractor( param.getValue()));
         }
         if ( extractors.size() > 0 )
         {
            m_paramExtractors = new IPSDataExtractor[extractors.size()];
            extractors.toArray(m_paramExtractors);
         }
      }
   }

   /**
    * Convenience method for 2 parameter version of this method. <code>null
    * </code> is passed as the 2nd param.
    */
   public Object extract( PSExecutionData data )
      throws PSDataExtractionException
   {
      return extract( data, null );
   }

   /**
    * Calculates the value as defined by the replacement value based on the
    * supplied data. If it is null, the defValue object is returned.
    *
    * @param data The context during request processing. Never <code>null
    *    </code>.
    *
    * @param defValue If the calculated value comes up empty, this value is
    *    returned instead. May be <code>null</code>.
    *
    * @return The calculated url as a String, or the default value if the
    *    calculated url is empty.
    */
   public Object extract( PSExecutionData data, Object defValue )
      throws PSDataExtractionException
   {
      if ( null == data )
         throw new IllegalArgumentException( "data can't be null" );

      String href = null;
      PSUrlRequest req = (PSUrlRequest) m_sourceReplacementValues[0];
      if ( null != m_udfExtractor )
         href = m_udfExtractor.extract( data, defValue ).toString();
      else
      {
         HashMap paramValueMap = new HashMap();
         if ( null != m_paramExtractors )
         {
            Iterator params = req.getQueryParameters();
            for ( int i = 0; i < m_paramExtractors.length; ++i )
            {
               PSParam param = (PSParam) params.next();
               Object value = m_paramExtractors[i].extract(data);
               if (value != null)
                  paramValueMap.put( param.getName(), value.toString() );
            }
         }
         Set paramValueSet = paramValueMap.entrySet();
         href = PSUrlUtils.createUrl( req.getHref(), paramValueSet.iterator(),
               null );
      }
      return href;
   }

   /**
    * If not <code>null</code>, this extractor will be used to generate the
    * URL.
    */
   private IPSDataExtractor m_udfExtractor;

   /**
    * If the UDF extractor is<code>null</code>, then the URL is built from
    * the pieces in the replacment value. These are the extractors for the
    * query params. May be <code>null</code>.
    */
   private IPSDataExtractor [] m_paramExtractors;
}


