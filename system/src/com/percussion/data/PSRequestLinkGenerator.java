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

package com.percussion.data;

import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSDocumentMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDataEncryptor;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSDataSynchronizer;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestLink;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSUpdateColumn;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import com.percussion.util.PSURLEncoder;

import java.util.ArrayList;

import javax.servlet.ServletRequest;

/**
 * The PSRequestLinkGenerator class takes the current request context, adds
 * any additional data, then fires the chained request. This is most
 * commonly used by the update handler to return an update query result set
 * upon successful updating.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSRequestLinkGenerator extends PSDataExtractor
{
   /**
    * Construct a request link generator. This creates URL links in the
    * XML document being processed based upon the expected target data set
    * and the current data.
    *
    * @param      app         the application containing the data set we
    *                           will be linking to
    *
    * @param      sourceDS      the source data set definition
    *
    * @param      link         the link definition
    *
    * @exception   PSNotFoundException   if the target data set does not exist
    */
   public PSRequestLinkGenerator(
      PSApplicationHandler app, PSDataSet sourceDS, PSRequestLink link)
      throws PSNotFoundException
   {
      this(   app, sourceDS, app.getDataSetDefinition(link.getTargetDataSet()),
            link.getXmlField(), link.getType());
   }

   /**
    * Construct a request link generator. This creates URL links in the
    * XML document being processed based upon the expected target data set
    * and the current data.
    *
    * @param      app         the application containing the data set we
    *                           will be linking to
    *
    * @param      sourceDS      the source data set definition
    *
    * @param      targetDS      the target data set definition
    *
    * @param      xmlField      the name of the corresponding XML field
    *
    * @param      type         the type of request
    *                           (update, insert, delete, query)
    *
    * @exception   PSNotFoundException   if the target data set does not exist
    */
   protected PSRequestLinkGenerator(
      PSApplicationHandler app, PSDataSet sourceDS, PSDataSet targetDS,
      String xmlField,  int type)
      throws PSNotFoundException
   {
      super((IPSReplacementValue[])null);

      if (null == sourceDS) {
         throw new IllegalArgumentException("req link source ds null " +
            app.getName() + " " + targetDS);
      }
      if (null == targetDS) {
         throw new IllegalArgumentException("req link target ds null " +
            app.getName() + " " + sourceDS);
      }

      // store the name of the XML field we'll place the link into
      m_xmlField = xmlField;

      // get the base request URL
      PSRequestor requestor = targetDS.getRequestor();
      m_requestURL = PSServer.makeRequestRoot(app.getRequestRoot());
      if (m_requestURL == null)
         m_requestURL = "";
      m_requestURL += "/" + requestor.getRequestPage();

      m_requestURL = PSURLEncoder.encodePath(m_requestURL);

      // if they're overriding the MIME type, don't set the extension
      m_useExtension = (!requestor.isDirectDataStream());

      /* get the data encryptor so we can create an HTTPS url if
       * SSL is required.
       *
       * get the data set encryptor. if none, app encryptor. if none,
       * server encryptor. if none, no ssl.
       */
      PSDataEncryptor encr = targetDS.getDataEncryptor();
      if (encr == null) {
         encr = app.getApplicationDefinition().getDataEncryptor();
      }
      m_useSSL = (encr != null) && encr.isSSLRequired();

      // for link type query/delete we can properly build the URL
      ArrayList params = new ArrayList();
      ArrayList lookupVals = new ArrayList();

      switch (type) {
         case PSRequestLink.RL_TYPE_QUERY:
            addQueryParameters(
               app, sourceDS, targetDS, type, params, lookupVals);
            break;

         case PSRequestLink.RL_TYPE_INSERT:
            // inserts use Form?RequestType=RequestValue
            addUpdateRequestType(app, type);
            break;

         case PSRequestLink.RL_TYPE_UPDATE:
         case PSRequestLink.RL_TYPE_DELETE:
            // updates/deltes use Form?RequestType=RequestValue&key=value...
            addUpdateRequestType(app, type);
            addUpdateParameters(
               app, sourceDS, targetDS, type, params, lookupVals);
            break;

         default:
            throw new IllegalArgumentException("link type unsupported " +
               app + " " + sourceDS+ " " + targetDS+ " " + getLinkTypeString(type) );
      }

      // also append the selection parameters
      addSelectionParameters(
         app, sourceDS, targetDS, type, params, lookupVals);

      // now build up our internal string arrays
      if (lookupVals.size() == 0)
         m_lookupParams = null;
      else {
         m_lookupParams = new IPSDataExtractor[lookupVals.size()];
         lookupVals.toArray(m_lookupParams);
      }

      if (params.size() == 0)
         m_parameterNames = null;
      else
      {
         m_parameterNames = new String[params.size()];

         // all the params get encoded as soon as they are added to the
         // params ArrayList (in add...Parameters() below)
         params.toArray(m_parameterNames);
      }
   }

   /**
    * Get the name of the XML field to store the link in.
    *
    * @return         the name of the XML field
    */
   public String getXmlFieldName()
   {
      return m_xmlField;
   }

   /**
    * Get the index previously set for XML field. This is used internally
    * by the PSResultSetXmlConverter to quickly access the index
    * into the XML node array.
    *
    * @return         the XML field's index
    */
   public int getXmlFieldIndex()
   {
      return m_xmlFieldIndex;
   }

   /**
    * Set the index for the XML field. This is used internally
    * by the PSResultSetXmlConverter to quickly access the index
    * into the XML node array.
    *
    * @param   index   the XML field's index
    */
   public void setXmlFieldIndex(int index)
   {
      m_xmlFieldIndex = index;
   }

   public static String getLinkTypeString(int type)
   {
      switch (type) {
         case PSRequestLink.RL_TYPE_NONE:
            return "none";

         case PSRequestLink.RL_TYPE_QUERY:
            return "query";

         case PSRequestLink.RL_TYPE_INSERT:
            return "insert";

         case PSRequestLink.RL_TYPE_UPDATE:
            return "update";

         case PSRequestLink.RL_TYPE_DELETE:
            return "delete";
      }

      return "unknown (" + type + ")";
   }


   /* ************  IPSDataExtractor Interface Implementation ************ */

   /**
    * Extract a data value using the run-time data.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @return               the associated value; <code>null</code> if a
    *                        value is not found
    */
   public Object extract(PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param   defValue      the default value to use if a value is not found
    *
    * @return               the associated value; <code>defValue</code> if a
    *                        value is not found
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws com.percussion.data.PSDataExtractionException
   {
      if (data == null)
         return defValue;

      if (data.getRequest() == null)
         return defValue;

      return generateURL(data, null);
   }


   /**
    * Extract a data value using the run-time data.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
   *
   * @param   moreParams   additional Html Parameters to be added. These
   * must URL encoded if necessary. May be <CODE>null</CODE> if no
   * params are required.
   *
   * @return               the associated value (URL string);
    */
   protected String generateURL(PSExecutionData data, String moreParams)
      throws com.percussion.data.PSDataExtractionException
   {
      PSRequest request = data.getRequest();
      StringBuffer buf = new StringBuffer(128);
      boolean hasQueryString = false;
      ServletRequest req = request.getServletRequest();
      
      if (m_useSSL) {
         /* if this request did not come through on SSL, we need to set
          * https://server:port as the base URL. we'll then tack on
          * /file?params
          */
         boolean httpsOn = req.isSecure();
         if (!httpsOn) {
            buf.append("https://");
            buf.append(req.getServerName() + ":" + req.getServerPort());
         }
      }

      String reqExtension;
      if (m_useExtension)
         reqExtension = request.getRequestPageExtension();
      else
         reqExtension = "";

      /* if the request was submitted through a hook, make that the
       * base of the URL
       */
      String baseURL = request.getHookURL();
      if ((baseURL != null) && (baseURL.length() != 0)) {
         hasQueryString = true;
         buf.append(PSURLEncoder.encodePath(baseURL));
         buf.append("?");
         buf.append(PSRequest.REQ_URL_PARAM);
         buf.append("=");
         buf.append(m_requestURL); // already URL encoded
         buf.append(PSURLEncoder.encodePath(reqExtension));
      }
      else {
         baseURL = null;   // flag used to show we do not have a query string
         buf.append(m_requestURL); // already URL encoded
         buf.append(PSURLEncoder.encodePath(reqExtension));
      }

      if (m_staticParams != null) {
         if (!hasQueryString) {
            buf.append('?');
            hasQueryString = true;
         }
         else
            buf.append('&');
         buf.append(m_staticParams); // already URL encoded
      }

      // now tack on the run-time parameters
      if (m_lookupParams != null)
      {
         for (int i = 0; i < m_lookupParams.length; i++) {
            if (!hasQueryString) {
               buf.append('?');
               hasQueryString = true;
            }
            else
               buf.append('&');

            buf.append(m_parameterNames[i]); // already URL encoded
            buf.append("=");
            String paramVal = m_lookupParams[i].extract(data, "").toString();
            buf.append(PSURLEncoder.encodeQuery(paramVal));
         }
      }

      if (moreParams != null && moreParams.length() > 0)
      {
         if (!hasQueryString) {
            buf.append('?');
            hasQueryString = true;
         }
         else
            buf.append('&');

         buf.append(moreParams);
      }

      return buf.toString();
   }


   protected void addSelectionParameters(
      PSApplicationHandler ah, PSDataSet sourceDS, PSDataSet targetDS,
      int type, java.util.List params, java.util.List extractors)
   {
      PSRequestor req = targetDS.getRequestor();

      PSCollection conds = req.getSelectionCriteria();
      int condsCount = (conds == null) ? 0 : conds.size();
      for (int j = 0; j < condsCount; j++)
      {
         PSConditional cond = (PSConditional)conds.get(j);

         IPSReplacementValue value = cond.getVariable();
         // only add HTML params to the list
         if (PSHtmlParameter.VALUE_TYPE.equals(value.getValueType()))
         {
            addParameterSource(
               (PSHtmlParameter)value, cond.getValue(),
               sourceDS, params, extractors);
         }
         else
         {
            /* must be a literal or CGI var. either case, we're not
             * interested in it. We are, however, interested in checking
             * if the value is an HTML param in which case the variable
             * should be our dynamic value.
             */
            value = cond.getValue();
            // only add HTML params to the list
            if (PSHtmlParameter.VALUE_TYPE.equals(value.getValueType()))
            {
               addParameterSource(
                  (PSHtmlParameter)value, cond.getVariable(),
                  sourceDS, params, extractors);
            }
         }
      }
   }

   protected void addQueryParameters(
      PSApplicationHandler ah, PSDataSet sourceDS, PSDataSet targetDS,
      int type, java.util.List params, java.util.List extractors)
   {
      PSPipe pipe = targetDS.getPipe();
      if (pipe instanceof PSQueryPipe)
      {
         // this is a query pipe, get the parameters/cols from it
         PSDataSelector selector = ((PSQueryPipe)pipe).getDataSelector();
         if (selector == null) {
            throw new IllegalArgumentException("query link target selector reqd " +
               ah.getName() + " " + sourceDS.getName() + " " + targetDS.getName() );
         }

         if (selector.isSelectByWhereClause())
         {
            PSCollection clauses = selector.getWhereClauses();
            if ((clauses != null) && (clauses.size() != 0))
            {
               for (int j = 0; j < clauses.size(); j++)
               {
                  PSWhereClause wClause = (PSWhereClause)clauses.get(j);
                  IPSReplacementValue value = wClause.getValue();
                  // only add HTML params to the list
                  if (PSHtmlParameter.VALUE_TYPE.equals(value.getValueType()))
                  {
                     addParameterSource(
                        (PSHtmlParameter)value, wClause.getVariable(),
                        sourceDS, params, extractors);
                  }
                  // else: must be a literal or CGI var
                  //   either case, we're not interested
               }
            }
         }
         else
         {
            IPSReplacementValue[] replValues = PSSqlParser.getReplacementValues(
               selector.getNativeStatement());
            for (int j = 0; j < replValues.length; j++)
            {
               IPSReplacementValue value = replValues[j];
               // only add HTML params to the list
               if (PSHtmlParameter.VALUE_TYPE.equals(value.getValueType()))
               {
                  addParameterSource(
                     (PSHtmlParameter)value, null,
                     sourceDS, params, extractors);
               }
               // else: must be a literal or CGI var
               //   either case, we're not interested
            }
         }
      }
      else {
         throw new IllegalArgumentException("query link target not query " +
            ah.getName() + " " + sourceDS.getName() + " " + targetDS.getName() );
      }
   }

   protected void addUpdateParameters(
      PSApplicationHandler ah, PSDataSet sourceDS, PSDataSet targetDS,
      int type, java.util.List params, java.util.List extractors)
   {
      PSPipe pipe = targetDS.getPipe();
      if (pipe instanceof PSUpdatePipe)
      {
         // this is an update pipe, get the parameters/keys from it
         PSDataSynchronizer sync = ((PSUpdatePipe)pipe).getDataSynchronizer();
         if (sync == null) {
            throw new IllegalArgumentException("update link target sync required " +
               targetDS.getName() + " " + getLinkTypeString(type) );
         }

         PSCollection updateCols = sync.getUpdateColumns();
         if ((updateCols != null) && (updateCols.size() != 0))
         {
            for (int j = 0; j < updateCols.size(); j++)
            {
               PSUpdateColumn col = (PSUpdateColumn)updateCols.get(j);
               // the update keys are what we use to generate the URL
               // for updating. Anything else we don't care about
               if (col.isKey())
               {
                  IPSDocumentMapping[] docMaps = getDocMappings(col.getColumn(), targetDS);
                  if (docMaps != null)
                  {
                     /* There can never be more than one of these in
                        the case of an update, but we'll assume
                        everything is validated well before we
                        get here, and code it generically */
                     for (int k = 0; k < docMaps.length; k++)
                     {
                        IPSDocumentMapping val = docMaps[k];
                        if (val instanceof PSHtmlParameter)
                        {
                           addParameterSource(
                              (PSHtmlParameter)val, col.getColumn(), sourceDS, params,
                              extractors);
                        } else if (val instanceof PSExtensionCall)
                        {
                           /* This situation doesn't make sense, so
                              log a warning */
                           com.percussion.log.PSLogManager.write(
                              new com.percussion.log.PSLogServerWarning(
                                 IPSDataErrors.WARN_CALL_MAPPED_KEY_COLUMN_ON_LINK,
                                 null, false, "PSRequestLinkGenerator"));
                           PSExtensionParamValue[] exitParams = ((PSExtensionCall)val).getParamValues();
                           for (int l = 0; l < exitParams.length; l++)
                           {
                              if (exitParams[l].getValue() instanceof PSHtmlParameter)
                              {
                                 addParameterSource(
                                    (PSHtmlParameter)exitParams[l].getValue(),
                                    col.getColumn(), sourceDS, params,
                                    extractors);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      else {
         throw new IllegalArgumentException("update link target not target " +
            ah.getName() + " " + sourceDS.getName() + " " + targetDS.getName() + " " +
            getLinkTypeString(type) );
      }
   }

   protected boolean addParameterSource(
      PSHtmlParameter param, IPSReplacementValue expectedSource,
      PSDataSet sourceDS,
      java.util.List params, java.util.List extractors)
   {
      IPSDataExtractor extractor = null;

      // only add HTML params to the list
      params.add(PSURLEncoder.encodeQuery(param.getValueText()));

      // let's see if we can find a matching column
      boolean addedExtractor = false;
      if ((expectedSource != null) &&
         PSBackEndColumn.VALUE_TYPE.equals(expectedSource.getValueType()))
      {
         addedExtractor = addColumnSource(
            (PSBackEndColumn)expectedSource, sourceDS, extractors);
      }
      else if (expectedSource != null)
      {   // this must be a CGI variable, literal, etc.
         // use the value directly
         extractor = PSDataExtractorFactory.createReplacementValueExtractor(
            expectedSource);
      }

      // if we couldn't find the appropriate extractor through the
      // source data set, let's use the HTML parameter directly and
      // hope they're actually bringing it over for the source request
      if (!addedExtractor) {
         if (extractor == null)
            extractor = PSDataExtractorFactory.createReplacementValueExtractor(
               param);
         extractors.add(extractor);
      }

      return true;
   }

   protected void addUpdateRequestType(PSApplicationHandler ah, int reqType)
   {
      String strParam;
      PSApplication app = ah.getApplicationDefinition();

      strParam = app.getRequestTypeHtmlParamName();
      if (strParam == null)
         return;   // no param name, nothing to do
      strParam = PSURLEncoder.encodeQuery(strParam);   // URL encode this

      String strValue;
      if (reqType == PSRequestLink.RL_TYPE_INSERT)
         strValue = app.getRequestTypeValueInsert();
      else if (reqType == PSRequestLink.RL_TYPE_UPDATE)
         strValue = app.getRequestTypeValueUpdate();
      else if (reqType == PSRequestLink.RL_TYPE_DELETE)
         strValue = app.getRequestTypeValueDelete();
      else
         return;   // not a supported type, nothing to do
      strValue = PSURLEncoder.encodeQuery(strValue);   // URL encode this

      if (m_staticParams == null)
         m_staticParams = strParam + "=" + strValue;
      else
         m_staticParams += "&" + strParam + "=" + strValue;
   }

   private static IPSReplacementValue getMatch(
      PSBackEndColumn col,
      PSDataSet ds)
   {
      IPSReplacementValue source = null;

      // if we see a column with the same name but from a different table
      // we'll treat it as a possible match. Only if an exact match is not
      // found will this be used
      PSBackEndColumn possibleMatch = null;

      // we'll look through the column mappings and data selector of
      // the source data set to determine if this is being used
      PSPipe pipe = ds.getPipe();
      if (pipe != null) {
         PSDataMapper mapper = pipe.getDataMapper();
         int mapperSize = (mapper == null) ? 0 : mapper.size();
         for (int i = 0; (source == null) && (i < mapperSize); i++) {
            PSDataMapping map = (PSDataMapping)mapper.get(i);
            PSBackEndColumn bec = getMatchingColumn(map.getBackEndMapping(), col);
            if (bec == col)
            {
                source = col;
               break;
            }
            else if (bec != null)
            {
               possibleMatch = bec;
            }

            PSCollection conditionals = map.getConditionals();
            /* Check the conditional mappings for columns now
               Bug Id: Rx-99-11-0013 */
            if (col != null) {
               for (int condi = 0; ( (condi < conditionals.size()) && (source == null) ); condi++)
               {
                  PSConditional cond = (PSConditional) conditionals.get(condi);

                  if (cond.getValue() instanceof IPSBackEndMapping)
                  {
                     bec = getMatchingColumn(
                        (IPSBackEndMapping) cond.getValue(), col);
                     if (bec == col)
                     {
                         source = col;
                        break;
                     }
                     else if (bec != null)
                     {
                        possibleMatch = bec;
                     }
                  }
                  if (cond.getVariable() instanceof IPSBackEndMapping)
                  {
                     bec = getMatchingColumn(
                        (IPSBackEndMapping) cond.getVariable(), col);
                     if (bec == col)
                     {
                         source = col;
                        break;
                     }
                     else if (bec != null)
                     {
                        possibleMatch = bec;
                     }
                  }
               }
               if (source != null)
                  break;
            }
         }

         if ((source == null) && (pipe instanceof PSQueryPipe)) {
            // this is a query pipe, so check the selector now. If it's
            // in the selector but not in the mapper, it means we need to
            // use the value of the where clause, not the back-end column
            // as it won't be available during the result set traversal
            PSDataSelector selector = ((PSQueryPipe)pipe).getDataSelector();
            if ((selector != null) && selector.isSelectByWhereClause()) {
               PSCollection clauses = selector.getWhereClauses();
               if ((clauses != null) && (clauses.size() != 0)) {
                  for (int i = 0; i < clauses.size(); i++) {
                     PSWhereClause wClause = (PSWhereClause)clauses.get(i);
                     IPSReplacementValue var = wClause.getVariable();
                     if (col.equals(var)) {
                        // switch the var to use the value instead
                        source = wClause.getValue();
                        break;
                     }
                  }
               }
            }
         }
      }

      if (source != null)
         return source;

      return possibleMatch;
   }


   /**
    * This call is used to find the back-end column we're looking for.
    *
    * If the back-end column is found, we will return the input param col.
    *
    * If not, but we found a possible match (column name is the same,
    * table is different), then we'll return that.
    *
    * If we found nothing, we'll return null.
    */
   private static PSBackEndColumn getMatchingColumn(
      IPSBackEndMapping beMap, PSBackEndColumn col)
   {
      // if we see a column with the same name but from a different table
      // we'll treat it as a possible match. Only if an exact match is not
      // found will this be used
      PSBackEndColumn possibleMatch = null;

      if (beMap instanceof PSBackEndColumn)
      {
         if (col.equals(beMap))
         {
             return col;
         }
         else if (col.getColumn().equalsIgnoreCase(
            ((PSBackEndColumn)beMap).getColumn()))
         {
            possibleMatch = (PSBackEndColumn)beMap;
         }
      }
      else if (beMap instanceof PSExtensionCall)
      {   // UDF, which may contain the col in a param
         PSExtensionParamValue[] pValues
            = ((PSExtensionCall)beMap).getParamValues();
         int paramSize = (pValues == null) ? 0 : pValues.length;
         for (int j = 0; j < paramSize; j++) {
            PSExtensionParamValue p = pValues[j];
            if (p.isBackEndColumn())
            {
               PSBackEndColumn beVal = (PSBackEndColumn)p.getValue();
               if (col.equals(beVal))
               {
                  return col;
               }
               else if (col.getColumn().equalsIgnoreCase(beVal.getColumn()))
               {
                  possibleMatch = beVal;
               }
            }
         }
      }

      return possibleMatch;
   }

   private static IPSDocumentMapping[] getDocMappings(
      PSBackEndColumn col,
      PSDataSet ds)
   {
      IPSDocumentMapping[] mappings = null;

      // if we see a column with the same name but from a different table
      // we'll treat it as a possible match. Only if an exact match is not
      // found will this be used
      PSBackEndColumn possibleMatch = null;

      // we'll look through the column mappings and data selector of
      // the source data set to determine if this is being used
      PSPipe pipe = ds.getPipe();
      if (pipe != null)
      {
         PSDataMapper mapper = pipe.getDataMapper();
         int mapperSize = (mapper == null) ? 0 : mapper.size();
         boolean found = false;
         for (int i = 0; (!found) && (i < mapperSize); i++)
         {
            PSDataMapping map = (PSDataMapping)mapper.get(i);
            IPSBackEndMapping beMap = map.getBackEndMapping();
            if (beMap instanceof PSBackEndColumn)
            {
               if (col.equals(beMap))
               {
                   mappings = new IPSDocumentMapping[1];
                  mappings[0] = map.getDocumentMapping();
                  break;
               }  // Check the case non-specific column name now
               else if (col.getColumn().equalsIgnoreCase(
                  ((PSBackEndColumn)beMap).getColumn()))
               {
                  mappings = new IPSDocumentMapping[1];
                  mappings[0] = map.getDocumentMapping();
                  break;
               }
            }
            else if (beMap instanceof PSExtensionCall)
            {
               // UDF, which may contain the col in a param
               PSExtensionParamValue[] pValues
                  = ((PSExtensionCall)beMap).getParamValues();
               int paramSize = (pValues == null) ? 0 : pValues.length;
               for (int j = 0; j < paramSize; j++)
               {
                  PSExtensionParamValue p = pValues[j];
                  if (!found && p.isBackEndColumn())
                  {
                     PSBackEndColumn beVal = (PSBackEndColumn)p.getValue();
                     if (col.equals(beVal))
                     {
                        found = true;
                     }
                     else if (col.getColumn().equalsIgnoreCase(
                        beVal.getColumn()))
                     {
                        found = true;
                     }
                  }
               }
               if (found == true)
               {
                  // In this case add the beMap as it map contain
                  // Document references like HTML params, etc.
                  mappings = new IPSDocumentMapping[2];
                  mappings[0] = (IPSDocumentMapping) beMap;
                  mappings[1] = map.getDocumentMapping();

                  break;
               }
            }
         }
      }

      return mappings;
   }

   protected boolean addColumnSource(
      PSBackEndColumn col, PSDataSet sourceDS,
      java.util.List extractors)
   {
      IPSReplacementValue source = getMatch(col, sourceDS);

      if (source != null)
      {
         extractors.add(
            PSDataExtractorFactory.createReplacementValueExtractor(source));
         return true;
      }

      return false;
   }


   private static final int   RL_TYPE_QUERY      =    1;
   private static final int   RL_TYPE_INSERT      =    2;
   private static final int   RL_TYPE_UPDATE      =    3;
   private static final int   RL_TYPE_DELETE      =    4;

   private   IPSDataExtractor[]   m_lookupParams;
   private   String               m_xmlField;
   private   int                  m_xmlFieldIndex = -1;

   /** the URL encoded request root */
   private   String               m_requestURL;

   /** the URL encoded static params that always get added to the URL */
   private   String               m_staticParams;

   /** the URL encoded parameter names to use in building the URL */
   private   String[]            m_parameterNames;
   private   boolean               m_useExtension;
   private   boolean               m_useSSL;
}

