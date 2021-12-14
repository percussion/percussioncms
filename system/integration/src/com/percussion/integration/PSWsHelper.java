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
package com.percussion.integration;

import com.percussion.integration.IPSSearchFilter;
import com.percussion.integration.IPSWsHelper;
import com.percussion.integration.PSColumn;
import com.percussion.integration.PSField;
import com.percussion.integration.PSItem;
import com.percussion.integration.PSSearch;
import com.percussion.integration.PSWsHelperBase;
import com.percussion.integration.webservices.common.ResultResponseResult;
import com.percussion.integration.webservices.common.ResultResponseResultType;
import com.percussion.integration.webservices.contentdata.OpenItem;
import com.percussion.integration.webservices.contentdata.OpenItemOpenItemRequest;
import com.percussion.integration.webservices.contentdata.OpenItemResponse;
import com.percussion.integration.webservices.contentdata.wsdl.ContentDataBindingStub;
import com.percussion.integration.webservices.contentdata.wsdl.ContentDataPort;
import com.percussion.integration.webservices.contentdata.wsdl.ContentDataServicesLocator;
import com.percussion.integration.webservices.design.ContentTypeList;
import com.percussion.integration.webservices.design.ContentTypeListResponse;
import com.percussion.integration.webservices.design.ContentTypeListResponseContentType;
import com.percussion.integration.webservices.design.wsdl.DesignBindingStub;
import com.percussion.integration.webservices.design.wsdl.DesignPort;
import com.percussion.integration.webservices.design.wsdl.DesignServicesLocator;
import com.percussion.integration.webservices.header.Authentication;
import com.percussion.integration.webservices.header.AuthenticationOptionsOption;
import com.percussion.integration.webservices.miscellaneous.CallDirect;
import com.percussion.integration.webservices.miscellaneous.CallDirectCallDirectRequest;
import com.percussion.integration.webservices.miscellaneous.CallDirectCallDirectRequestParamsParam;
import com.percussion.integration.webservices.miscellaneous.CallDirectResponse;
import com.percussion.integration.webservices.miscellaneous.Login;
import com.percussion.integration.webservices.miscellaneous.LoginResponse;
import com.percussion.integration.webservices.miscellaneous.LoginResponseLoginData;
import com.percussion.integration.webservices.miscellaneous.LoginResponseLoginDataCommunitiesCommunity;
import com.percussion.integration.webservices.miscellaneous.LoginResponseLoginDataLocalesLocale;
import com.percussion.integration.webservices.miscellaneous.wsdl.MiscellaneousBindingStub;
import com.percussion.integration.webservices.miscellaneous.wsdl.MiscellaneousPort;
import com.percussion.integration.webservices.miscellaneous.wsdl.MiscellaneousServicesLocator;
import com.percussion.integration.webservices.search.ConnectorTypes;
import com.percussion.integration.webservices.search.OperatorTypes;
import com.percussion.integration.webservices.search.ResultField;
import com.percussion.integration.webservices.search.Search;
import com.percussion.integration.webservices.search.SearchConfiguration;
import com.percussion.integration.webservices.search.SearchConfigurationResponse;
import com.percussion.integration.webservices.search.SearchConfigurationResponseProperty;
import com.percussion.integration.webservices.search.SearchResponse;
import com.percussion.integration.webservices.search.SearchSearchRequest;
import com.percussion.integration.webservices.search.SearchSearchRequestSearchParams;
import com.percussion.integration.webservices.search.SearchSearchRequestSearchParamsParameterSearchField;
import com.percussion.integration.webservices.search.SearchSearchRequestSearchParamsPropertiesProperty;
import com.percussion.integration.webservices.search.wsdl.SearchBindingStub;
import com.percussion.integration.webservices.search.wsdl.SearchPort;
import com.percussion.integration.webservices.search.wsdl.SearchServicesLocator;
import com.percussion.integration.webservices.standarditem.ContentKey;
import com.percussion.integration.webservices.standarditem.Item;
import com.percussion.util.IPSHtmlParameters;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisProperties;
import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author dougrand
 *
 * The helper class provides a friendlier interface to the generated
 * client stubs for SOAP. This particular class implements those methods
 * for AXIS.
 */
public class PSWsHelper extends PSWsHelperBase implements IPSWsHelper
{
   static {
      // Setup the connection for axis to find this class
      AxisProperties.setProperty(
         "axis.socketFactory",
         "com.percussion.integration.PSAxisSocketFactory");
   }

   /**
    * Initialize the the helper class including all ports used for web service 
    * calls below. @see init for more information
    * 
    * @param context the current servlet context, may not be <code>null</code>
    * @param req the current servlet request, may not be <code>null</code>
    * @param resp the current servlet response, may not be <code>null</code>
    * 
    * @throws ServletException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   public PSWsHelper(
      ServletContext context,
      HttpServletRequest req,
      HttpServletResponse resp)
      throws
         ServletException,
         IOException,
         ParserConfigurationException,
         ServiceException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      if (resp == null)
         throw new IllegalArgumentException("resp may not be null");

      init(context, req, resp, null);
   }

   /**
    * Initialize all the needed ports and sets the endpoint.
    * 
    * @param context the current servlet context, may not be <code>null</code>
    * @param req the current servlet request, may not be <code>null</code>
    * @param resp the current servlet response, may not be <code>null</code>
    * @param targetEndpoint the url location to send the soap message, may be
    *    <code>null</code>, @see init for more information
    *    
    * @throws ServletException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   public PSWsHelper(
      ServletContext context,
      HttpServletRequest req,
      HttpServletResponse resp,
      URL targetEndpoint)
      throws
         ServletException,
         IOException,
         ParserConfigurationException,
         ServiceException
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      if (resp == null)
         throw new IllegalArgumentException("resp may not be null");

      init(context, req, resp, targetEndpoint);
   }

   /**
    * Packate protected constructor for testing purposes or for using direct 
    * username and password for the method calls, no need for a servlet context
    * or request response data.
    * 
    * @throws ServletException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   PSWsHelper()
      throws
         ServletException,
         IOException,
         ParserConfigurationException,
         ServiceException
   {
      init(null, null, null, null);
   }

   /**
    * Init all the ports and the target endpoint.
    * 
    * @param context the current servlet context, may be <code>null</code>
    * @param req the current servlet request, may be <code>null</code>
    * @param resp the current servlet response, may be <code>null</code>
    * @param targetEndpoint the url location to send the soap message, may be
    *    <code>null</code> 
    *    
    * @throws ServletException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   private void init(
      ServletContext context,
      HttpServletRequest req,
      HttpServletResponse resp,
      URL targetEndpoint)
      throws
         ServletException,
         IOException,
         ParserConfigurationException,
         ServiceException
   {
      // set up the configuration properties
      if (req != null)
      {
         m_props.setProperty(RX_PROTOCOL, req.getScheme());
         m_props.setProperty(RX_HOST, req.getServerName());
         m_props.setProperty(RX_PORT, Integer.toString(req.getServerPort()));

         initRhythmyxSession(context, req, resp);
      }
      initPorts();

      m_targetEndpoint = loadProps(targetEndpoint);

      setPortURL(m_targetEndpoint);

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);

      m_db = dbf.newDocumentBuilder();
   }

   /**
    * Workaround for BEA 7.0, we need to call the Rhythmyx servlet first to 
    * create a new Rhythmyx session to store in the current session of the 
    * current request.
    * 
    * @param context the current servlet context, may be <code>null</code>
    * @param req the current servlet request, may be <code>null</code>
    * @param resp the current servlet response, may be <code>null</code>
    * 
    * @throws ServletException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   private void initRhythmyxSession(
      ServletContext context,
      HttpServletRequest req,
      HttpServletResponse resp)
      throws ServletException, IOException, ParserConfigurationException
   {
      if (context != null && req != null && resp != null)
      {
         String rxSession = getRhythmyxSession(req);

         if (rxSession == null || rxSession.trim().length() == 0)
         {
            ServletContext remContext = context.getContext("/Rhythmyx");

            String root = remContext.getInitParameter("RxRoot");
            if (root != null && root.trim().length() > 0)
               m_props.setProperty(RX_ROOT, root);
            try
            {
               // get the dispatcher to the Rhythmyx servlet
               RequestDispatcher reqDispatcher =
                  remContext.getRequestDispatcher(
                     "/sys_psxWebServices/authenticate.xml");

               // this will set the Rhythmyx session and rolelist on the request
               reqDispatcher.include(req, resp);

               setRhythmyxData(req);
            }
            catch (Exception e)
            {
               // The above code can fail on IBM, quietly catch the exception
            }
         }
      }
   }

   /**
    * Init all the needed ports, uses the current target endpoint as set by
    * the calling method or set indirectly through the configuration properties.
    * Note that this should share a single HttpSession on the other end
    * of the connection. TODO this requires more work.
    * @throws IOException
    * @throws ParserConfigurationException
    */
   private void initPorts() throws IOException, ServiceException
   {
      DesignServicesLocator designImpl = new DesignServicesLocator();
      designImpl.setMaintainSession(true);
      m_designPort = designImpl.getDesignPort();
      ((Stub) m_designPort).setMaintainSession(true);
      
      MiscellaneousServicesLocator miscImpl =
         new MiscellaneousServicesLocator();
      miscImpl.setMaintainSession(true);
      m_miscPort = miscImpl.getMiscellaneousPort();
      ((Stub) m_miscPort).setMaintainSession(true);

      SearchServicesLocator searchImpl = new SearchServicesLocator();
      searchImpl.setMaintainSession(true);
      m_searchPort = searchImpl.getSearchPort();
      ((Stub) m_searchPort).setMaintainSession(true);

      ContentDataServicesLocator contentDataImpl = new ContentDataServicesLocator();
      contentDataImpl.setMaintainSession(true);
      m_contentDataPort = contentDataImpl.getContentDataPort();
      ((Stub) m_contentDataPort).setMaintainSession(true);
   
   }

   /**
    * Set the target endpoint for all soap requests. This will change the 
    * location where the soap envelopes will be sent. This will also save the
    * endpoint in local storage to allow retrieval at any time.
    * 
    * @param url the location endpoint of where to send the messages, must not
    *    be <code>null</code> or empty
    */
   public void setPortURL(URL url)
   {
      if (url == null && url.toExternalForm().trim().length() == 0)
         throw new IllegalArgumentException("url must not be null or empty");

      m_targetEndpoint = url;

      if (m_designPort != null)
         ((DesignBindingStub) m_designPort)._setProperty(
            DesignBindingStub.ENDPOINT_ADDRESS_PROPERTY,
            url.toString());

      if (m_miscPort != null)
         ((MiscellaneousBindingStub) m_miscPort)._setProperty(
            MiscellaneousBindingStub.ENDPOINT_ADDRESS_PROPERTY,
            url.toString());

      if (m_searchPort != null)
         ((SearchBindingStub) m_searchPort)._setProperty(
            SearchBindingStub.ENDPOINT_ADDRESS_PROPERTY,
            url.toString());

      if (m_contentDataPort != null)
         ((ContentDataBindingStub) m_contentDataPort)._setProperty(
            ContentDataBindingStub.ENDPOINT_ADDRESS_PROPERTY,
            url.toString());
   }

   /**
    * Login to Rhythmyx, used to switch the locale or community or both. All 
    * options within the specified map will be added to the header.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param options a map of options to be added to the credential header,
    *    may be <code>null</code> or an empty map
    * 
    * @return a LoginResponse object defined by the sys_Miscellaneous.xsd schema
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public LoginResponse login(HttpServletRequest req, Map options)
      throws SOAPException, RemoteException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Authentication auth = getAuthenticationHeader(req);
      return login(auth, options);
   }

   /**
    * Login to Rhythmyx, used to swicth the locale or community or both. All 
    * options within the specified map will be added to the header.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    * @param options a map of options to be added to the credential header,
    *    may be <code>null</code> or an empty map
    *    
    * @return a LoginResponse object defined by the sys_Miscellaneous.xsd schema
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public LoginResponse login(String username, String password, Map options)
      throws SOAPException, RemoteException
   {
      if (username == null || username.trim().length() == 0)
         throw new IllegalArgumentException("username may not be null or empty");
      if (password == null || password.trim().length() == 0)
         throw new IllegalArgumentException("password may not be null or empty");

      Authentication auth = getAuthenticationHeader(username, password);
      return login(auth, options);
   }

   /**
    * Private helper, @see login for more information
    * 
    * @param auth credentials for this call, assumed not <code>null</code>
    * @param options a map of options to be added to the credential header,
    *    may be <code>null</code> or an empty map
    *    
    * @return a LoginResponse object defined by the sys_Miscellaneous.xsd schema
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   private LoginResponse login(Authentication auth, Map params)
      throws SOAPException, RemoteException
   {
      if (params != null && !params.isEmpty())
      {
         AuthenticationOptionsOption[] optionList =
            new AuthenticationOptionsOption[params.size()];
         int i = 0;

         Iterator iter = params.keySet().iterator();
         while (iter.hasNext())
         {
            String key = (String) iter.next();
            String val = (String) params.get(key);
            AuthenticationOptionsOption option =
               new AuthenticationOptionsOption();
            option.setName(key);
            option.set_value(val);
            optionList[i++] = option;
         }
         auth.setOptions(optionList);
      }
      Login loginAction = new Login();

      //set the authentication soap header
      setAuthenticationHeader((MiscellaneousBindingStub) m_miscPort, auth);

      // send SOAP message
      LoginResponse loginResp = m_miscPort.login(loginAction);

      return loginResp;
   }

   /**
    * Get the Rhythmyx inbox.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param fieldList a list of strings to identify which fields
    *    to be returned with each content item found, may be <code>null</code>
    *    or empty in which case a default set of fields will be returned
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object with the result of content items, this is 
    *    defined in sys_SearchParameters.xsd schema
    *    
    * @throws SOAPException
    * @throws RemoteException
    */
   public PSSearch getInbox(
      HttpServletRequest req,
      List fieldList,
      boolean includeActionMenu)
      throws SOAPException, RemoteException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Authentication auth = getAuthenticationHeader(req);
      return getInbox(auth, fieldList, includeActionMenu);
   }

   /**
    * Get the Rhythmyx inbox.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    * @param fieldList a list of strings to identify which fields
    *    to be returned with each content item found, may be <code>null</code>
    *    or empty in which case a default set of fields will be returned
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object with the result of content items, this is 
    *    defined in sys_SearchParameters.xsd schema
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public PSSearch getInbox(
      String username,
      String password,
      List fieldList,
      boolean includeActionMenu)
      throws SOAPException, RemoteException
   {
      if (username == null || username.trim().length() == 0)
         throw new IllegalArgumentException("username may not be null or empty");
      if (password == null || password.trim().length() == 0)
         throw new IllegalArgumentException("password may not be null or empty");

      Authentication auth = getAuthenticationHeader(username, password);
      return getInbox(auth, fieldList, includeActionMenu);
   }

   /**
    * Private helper, @see getInbox for more information
    * 
    * @param auth credentials for this call, assumed not <code>null</code>
    * @param fieldList a list of strings to identify which fields
    *    to be returned with each content item found, may be <code>null</code>
    *    or empty in which case a default set of fields will be returned
    *    
    * @return a PSSearch object with the result of content items, this is 
    *    defined in sys_SearchParameters.xsd schema
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   private PSSearch getInbox(
      Authentication auth,
      List fieldList,
      boolean includeActionMenu)
      throws SOAPException, RemoteException
   {
      PSSearch retSearch = new PSSearch();

      // get the inbox list of ids as an xml string
      String data = executeCallDirect(auth, "../sys_cxViews/inbox.xml", null);

      /**
       * The data returned from the above call looks like the following:
       * 
       * <View>
       *    <Item sys_contentid="301"/>
       *    <Item sys_contentid="302"/>
       * </View>
       */
      if (data == null)
         data = "<View></View>"; // Should this be escalated?

      Document doc = toDOM(data);
      Element el = doc.getDocumentElement();

      NodeList nl = el.getElementsByTagName("Item");
      if (nl.getLength() > 0)
      {
         String ids = "";
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element itemEl = (Element) nl.item(i);
            ids += "," + itemEl.getAttribute(IPSHtmlParameters.SYS_CONTENTID);
         }

         // now that we have the list of content id's execute a search
         PSSearch search = new PSSearch();
         PSItem item = new PSItem();

         // create a query field with all the content ids found
         item.addQueryField(
            IPSHtmlParameters.SYS_CONTENTID,
            ids.substring(1),
            OperatorTypes._in,
            null);

         // add any specified fields to the results
         if (fieldList != null)
         {
            Iterator columns = fieldList.iterator();
            while (columns.hasNext())
            {
               PSColumn column = (PSColumn) columns.next();
               if (column.getFieldName().equals("__ACTION_PAGE_FIELD__")
                  == false)
                  item.addSelectField(column.getFieldName());
            }
         }
         search.addItem(item);

         // now execute the search
         retSearch = executeSearch(auth, search, null, includeActionMenu);
      }
      return retSearch;
   }

   /**
    * Call any Rhythmyx application resource and get the resultant data.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param appLocation the location of the Rhythmyx application resource, 
    *    assumed not <code>null</code> or empty
    * @param paramMap a map of parameters to be added as HTML paramters to
    *    the call, may be <code>null</code>
    *    
    * @return string representing the call to the resource (usually HTML)
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public String executeCallDirect(
      HttpServletRequest req,
      String appLocation,
      Map paramMap)
      throws SOAPException, RemoteException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Authentication auth = getAuthenticationHeader(req);
      return executeCallDirect(auth, appLocation, paramMap);
   }

   /**
    * Call any Rhythmyx application resource and get the resultant data.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    * @param appLocation the location of the Rhythmyx application resource, 
    *    assumed not <code>null</code> or empty
    * @param paramMap a map of parameters to be added as HTML paramters to
    *    the call, may be <code>null</code>
    *    
    * @return string representing the call to the resource (usually HTML)
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public String executeCallDirect(
      String username,
      String password,
      String appLocation,
      Map paramMap)
      throws SOAPException, RemoteException
   {
      if (username == null || username.trim().length() == 0)
         throw new IllegalArgumentException("username may not be null or empty");
      if (password == null || password.trim().length() == 0)
         throw new IllegalArgumentException("password may not be null or empty");

      Authentication auth = getAuthenticationHeader(username, password);
      return executeCallDirect(auth, appLocation, paramMap);
   }

   /**
    * Private helper, @see executeCallDirect for more information
    * 
    * @param auth credentials for this call, assumed not <code>null</code>
    * @param appLocation the location of the Rhythmyx application resource, 
    *    assumed not <code>null</code> or empty
    * @param paramMap a map of parameters to be added as HTML paramters to
    *    the call, may be <code>null</code>
    *    
    * @return string representing the call to the resource (usually HTML)
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   private String executeCallDirect(
      Authentication auth,
      String appLocation,
      Map paramMap)
      throws SOAPException, RemoteException
   {
      CallDirectCallDirectRequestParamsParam[] paramList = null;

      if (paramMap != null && !paramMap.isEmpty())
      {
         paramList =
            new CallDirectCallDirectRequestParamsParam[paramMap.size()];

         int i = 0;
         Iterator iter = paramMap.keySet().iterator();
         while (iter.hasNext())
         {
            String key = (String) iter.next();
            String val = (String) paramMap.get(key);
            CallDirectCallDirectRequestParamsParam param =
               new CallDirectCallDirectRequestParamsParam();
            param.setName(key);
            param.set_value(val);

            paramList[i] = param;
            i++;
         }
      }

      CallDirect CallDirectAction = new CallDirect();
      CallDirectCallDirectRequest req = new CallDirectCallDirectRequest();
      req.setAppLocation(appLocation);
      req.setParams(paramList);

      CallDirectAction.setCallDirectRequest(req);

      //set the authentication soap header
      setAuthenticationHeader((MiscellaneousBindingStub) m_miscPort, auth);

      // send SOAP message
      CallDirectResponse CallDirectResp =
         m_miscPort.callDirect(CallDirectAction);

      checkResponse(CallDirectResp.getResultResponse()[0]);

      return CallDirectResp.getXMLData();
   }

   /**
    * Get the list of Rhythmyx content types.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    *    
    * @return a content type response as defined by the sys_Design.xsd schema
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public ContentTypeListResponse getContentTypeList(HttpServletRequest req)
      throws SOAPException, RemoteException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Authentication auth = getAuthenticationHeader(req);
      return getContentTypeList(auth);
   }

   /**
    * Get the list of Rhythmyx content types.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    *    
    * @return a content type response as defined by the sys_Design.xsd schema
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public ContentTypeListResponse getContentTypeList(
      String username,
      String password)
      throws SOAPException, RemoteException
   {
      if (username == null || username.trim().length() == 0)
         throw new IllegalArgumentException("username may not be null or empty");
      if (password == null || password.trim().length() == 0)
         throw new IllegalArgumentException("password may not be null or empty");

      Authentication auth = getAuthenticationHeader(username, password);
      return getContentTypeList(auth);
   }

   /**
    * Formats the supplied content type list response into a map.
    * 
    * @param contentTypes the content type list to be formatted, not
    *    <code>null</code>
    * @param sort <code>true</code> to return the content type map sorted
    *    in alpha order, <code>false</code> otherwise
    *    
    * @return a map of content types, the map key is the content type name,
    *    as <code>String</code>, the value is the <code>ContentTypeAnonType</code>
    *    object, never <code>null</code>, may be empty
    */
   @SuppressWarnings("unchecked")
   public Map formatContentTypeList(
      ContentTypeListResponse contentTypes,
      boolean sort)
   {
      if (contentTypes == null)
         throw new IllegalArgumentException("contentTypes cannot be null");

      Map results = null;
      if (sort)
         results = new TreeMap();
      else
         results = new HashMap();

      ContentTypeListResponseContentType[] types =
         contentTypes.getContentType();
      if (types != null)
      {
         for (int i = 0; i < types.length; i++)
         {
            ContentTypeListResponseContentType type = types[i];
            results.put(type.getName(), type);
         }
      }

      return results;
   }

   /**
    * Returns the fully qualified url for each content editor type specified
    * by the type parameter.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param type the content type to get the qualified url for, may not be
    *    </code>null</code>
    *    
    * @return the string of the qualified location of the editor
    * @throws MalformedURLException if the url from this request was somehow
    * incorrect
    */
   public String formatEditorUrl(
      HttpServletRequest req,
      ContentTypeListResponseContentType type)
      throws MalformedURLException
   {
      if (req != null); // Quiet compiler warning
      String url = type.getEditorUrl();
      String formattedUrl = formContentEditorURL(req, url);
      String sessionid = getRhythmyxSession(req);
      formattedUrl = formattedUrl + "&" + IPSHtmlParameters.SYS_SESSIONID
         + "=" + sessionid;
      return formattedUrl;
   }

   /**
    * Private helper, @see getContentTypeList for more information
    * 
    * @param auth credentials for this call, assumed not <code>null</code>
    * 
    * @return a content type response as defined by the sys_Design.xsd schema
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   private ContentTypeListResponse getContentTypeList(Authentication auth)
      throws SOAPException, RemoteException
   {
      ContentTypeList ContentTypeListAction = new ContentTypeList();

      //set the authentication soap header
      setAuthenticationHeader((DesignBindingStub) m_designPort, auth);

      // send SOAP message
      ContentTypeListResponse ContentTypeListResp =
         m_designPort.contentTypeList(ContentTypeListAction);

      checkResponse(ContentTypeListResp.getResultResponse()[0]);

      return ContentTypeListResp;
   }

   /**
    * Search for content items within the Rhythmyx system.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param search contains the search information to decide what selection
    *    and criteria to build the search, may not be <code>null</code>
    * @param filter the filter interface to apply to the result set, may be
    *    <code>null</code> @not implemented yet
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object that contains the search results
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public PSSearch search(
      HttpServletRequest req,
      PSSearch search,
      IPSSearchFilter filter,
      boolean includeActionMenu)
      throws SOAPException, RemoteException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      if (search == null)
         throw new IllegalArgumentException("search may not be null");

      Authentication auth = getAuthenticationHeader(req);
      return executeSearch(auth, search, null, includeActionMenu);
   }

   /**
    * Search for content items within the Rhythmyx system.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    * @param search contains the search information to decide what selection
    *    and criteria to build the search, may not be <code>null</code>
    * @param filter the filter interface to apply to the result set, may be
    *    <code>null</code> @not implemented yet
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object that contains the search results
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   public PSSearch search(
      String username,
      String password,
      PSSearch search,
      IPSSearchFilter filter,
      boolean includeActionMenu)
      throws SOAPException, RemoteException
   {
      if (username == null || username.trim().length() == 0)
         throw new IllegalArgumentException("username may not be null or empty");
      if (password == null || password.trim().length() == 0)
         throw new IllegalArgumentException("password may not be null or empty");
      if (search == null)
         throw new IllegalArgumentException("search may not be null");

      Authentication auth = getAuthenticationHeader(username, password);
      return executeSearch(auth, search, filter, includeActionMenu);
   }

   /**
    * Private method, @see search for more information.
    * 
    * @param auth credentials for this call, assumed not <code>null</code>
    * @param searchData a search object containing the information to carry
    *    out the search request, assumed not <code>null</code>
    * @param ifilter a filter interface to filter the results after the query,
    *    may be <code>null</code> @not implemented yet
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a search object containing all the returned items, will not be
    *    <code>null</code>
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   private PSSearch executeSearch(
      Authentication auth,
      PSSearch searchData,
      IPSSearchFilter ifilter,
      boolean includeActionMenu)
      throws SOAPException, RemoteException
   {
      if (ifilter != null); // Quiet compiler warning

      // Create webservices objects
      Search searchAction = new Search();
      SearchSearchRequest searchReq = new SearchSearchRequest();
      searchAction.setSearchRequest(searchReq);
      SearchSearchRequestSearchParams searchParams =
         new SearchSearchRequestSearchParams();
      Map props = searchData.getProperties();
      Iterator iter = props.entrySet().iterator();
      SearchSearchRequestSearchParamsPropertiesProperty properties[] =
         new SearchSearchRequestSearchParamsPropertiesProperty[props
            .size()];

      int i = 0;
      while (iter.hasNext())
      {
         Entry entry = (Entry) iter.next();
         properties[i] =
            new SearchSearchRequestSearchParamsPropertiesProperty();
         properties[i].setName((String) entry.getKey());
         properties[i].set_value((String) entry.getValue());
         i++;
      }
      searchParams.setProperties(properties);

      List searchList = searchData.getQuerySearchFields();
      Iterator searchIter = searchList.iterator();
      SearchSearchRequestSearchParamsParameterSearchField[] searchFieldList =
         new SearchSearchRequestSearchParamsParameterSearchField[searchList
            .size()];

      // create the where clause for the search
      i = 0;
      while (searchIter.hasNext())
      {
         PSField field = (PSField) searchIter.next();
         if (field.getName().equals(PSField.FULL_TEXT_FIELD))
         {
            searchParams.setFullTextQuery(field.getValue());
         }
         else
         {
            SearchSearchRequestSearchParamsParameterSearchField searchField =
               new SearchSearchRequestSearchParamsParameterSearchField();
            searchField.setConnector(
               ConnectorTypes.fromString(field.getConnector()));
            searchField.setName(field.getName());
            searchField.setOperator(
               OperatorTypes.fromString(field.getOperator()));
            searchField.set_value(field.getValue());
            searchFieldList[i++] = searchField;
         }
      }

      if (i != searchList.size())
      {
         // If this is a full text search, not all field elements were used
         SearchSearchRequestSearchParamsParameterSearchField newList[] =
            new SearchSearchRequestSearchParamsParameterSearchField[i];
         System.arraycopy(searchFieldList, 0, newList, 0, i);
         searchFieldList = newList;
      }

      List resultList = searchData.getQueryResultFields();
      Iterator resultIter = resultList.iterator();
      ResultField[] resultFieldList = new ResultField[resultList.size()];

      // create the select for the search
      i = 0;
      while (resultIter.hasNext())
      {
         PSField field = (PSField) resultIter.next();
         ResultField resultField = new ResultField();
         resultField.setName(field.getName());
         resultField.set_value((String) null);
         resultFieldList[i++] = resultField;
      }

      searchParams.setParameter(searchFieldList);
      searchParams.setSearchResults(resultFieldList);

      searchReq.setSearchParams(searchParams);

      //set the authentication soap header
      setAuthenticationHeader((SearchBindingStub) m_searchPort, auth);

      // send SOAP message
      SearchResponse searchResp = m_searchPort.search(searchAction);

      return createSearch(searchResp, includeActionMenu, auth.getSessionId());
   }

   /**
    * Convert the <code>SearchResponse</code> object into a <code>PSSearch</code>
    * object, to be used as the input of potentially another search.
    * 
    * @param resp the response from a search request, assumed not <code>null</code>
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    * @param sessionid the session id from Rhythmyx, used in the action panel
    * 
    * @return a search object with completed data
    * 
    * @throws SOAPException
    * @throws RemoteException
    */
   private PSSearch createSearch(
      SearchResponse resp,
      boolean includeActionMenu, String sessionid)
      throws SOAPException, RemoteException
   {
      PSSearch search = new PSSearch();

      checkResponse(resp.getResultResponse()[0]);

      ResultField[][] results = resp.getResult();
      if (results != null)
      {
         for (int i = 0; i < results.length; i++)
         {
            PSItem item = new PSItem();

            ResultField[] fields = results[i];
            for (int j = 0; j < fields.length; j++)
            {
               ResultField field = fields[j];
               item.addResultField(field.getName(), field.get_value());
            }

            // add the action menu as the first field in the item
            // if we have already found a content id
            if (includeActionMenu)
            {
               if (item.getContentId() != -1)
               {
                  String val = getActionPageLink(item.getContentId(), 
                        sessionid);
                  item.addActionPageField(val);
               }
            }
            search.addItem(item);
         }
      }
      return search;
   }

   /**
    * Check the response and throw a soap exception if the response was
    * some kind of failure or exception.
    * @param response The response from a soap request, assumed to never
    * be <code>null</code>
    */
   private void checkResponse(ResultResponseResult response) throws SOAPException
   {
      if (response != null
         && response.getType().equals(ResultResponseResultType._failure))
      {
         throw new SOAPException(
            "SOAP Request failed: " + response.get_value());
      }
   }

   /**
    * Private method to create an authentication header. Each call uses an
    * authentication header to determine credentials.
    * 
    * @param req the original http request, assumed not <code>null</code>
    * 
    * @return Authentication object filled in with the proper data to be used
    *    as a header in all WS calls, will not be <code>null</code>
    */
   private Authentication getAuthenticationHeader(HttpServletRequest req)
   {
      Authentication auth = new Authentication();

      String rxSession = getRhythmyxSession(req);
      if (rxSession != null)
         auth.setSessionId(rxSession);

      return auth;
   }

   /**
    * Private method to create an authentication header. Each call uses an
    * authentication header to determine credentials.
    * 
    * @param username name of user, may not be <code>null</code> or empty
    * @param password password of user, may not be <code>null</code> or empty
    * 
    * @return Authentication object filled in with the proper data to be used
    *    as a header in all WS calls
    */
   Authentication getAuthenticationHeader(
      String username,
      String password)
   {
      Authentication auth = new Authentication();

      auth.setUsername(username);
      auth.setPassword(password);

      return auth;
   }

   /**
    * Get the plain Rhythmyx session from the application server session.
    *
    * @param req the original http request, must not be <code>null</code>
    * 
    * @return the Rhythmyx session if available without the path information, 
    *    <code>null</code> otherwise
    */
   protected String getRhythmyxSession(HttpServletRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      String rxSession = null;
      HttpSession session = req.getSession(false);
      if (session != null)
         rxSession = (String) session.getAttribute(RX_SESSION_ATTRIB);

      return rxSession;
   }

   /* (non-Javadoc)
    * @see com.percussion.integration.IPSWsHelper#formatContentTypeList(javax.servlet.http.HttpServletRequest, boolean)
    */
   public Map formatContentTypeList(HttpServletRequest request, boolean sort)
      throws SOAPException, RemoteException
   {
      return formatContentTypeList(getContentTypeList(request), sort);
   }

   /* (non-Javadoc)
    * @see com.percussion.integration.IPSWsHelper#formatEditorUrl(javax.servlet.http.HttpServletRequest, java.lang.String)
    */
   public String formatEditorUrl(
      HttpServletRequest req,
      Map types,
      String name) throws MalformedURLException
   {
      ContentTypeListResponseContentType type =
         (ContentTypeListResponseContentType) types.get(name);

      return formatEditorUrl(req, type);
   }

   /* (non-Javadoc)
    * @see com.percussion.integration.IPSWsHelper#getTypeDescription(java.lang.String, java.util.Map)
    */
   public String getTypeDescription(String name, Map types)
   {
      ContentTypeListResponseContentType type =
         (ContentTypeListResponseContentType) types.get(name);

      return type.get_value();
   }

   /**
    * Find the login information for the currently logged in user
    * 
    * @param request the request, must never be <code>null</code>.
    * @param options the options for login, must never be <code>null</code>.
    * @return the login information, may be <code>null</code>
    * if there's a login problem.
    * @throws RemoteException if there's a problem calling the 
    * web service
    * @throws SOAPException if there's a problem calling the web service
    */
   public LoginResponseLoginData getLoginData(
      HttpServletRequest request,
      Map options)
      throws RemoteException, SOAPException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must never be null");
      }
      if (options == null)
      {
         throw new IllegalArgumentException("options must never be null");
      }
      LoginResponse login = null;
      login = login(request, options);

      LoginResponseLoginData loginData = login.getLoginData();
      return loginData;
   }

   /**
    * Get the default community id for the user
    * @param info The login information, must never be <code>null</code>
    * @return the default community index
    */
   public int getDefaultCommunityId(LoginResponseLoginData info)
   {
      if (info == null)
      {
         throw new IllegalArgumentException("info must never be null");
      }
      return info.getDefaultCommunityId();
   }

   /**
    * Get the user's list of available communities from the 
    * login information
    * @param info the login info, must never be <code>null</code>
    * @return an array of communities, may be zero length, 
    * never <code>null</code>
    */
   public String[] getCommunityNames(LoginResponseLoginData info)
   {
      if (info == null)
      {
         throw new IllegalArgumentException("info must never be null");
      }

      LoginResponseLoginDataCommunitiesCommunity clist[] =
         info.getCommunities();
      String[] rval = new String[clist.length];
      for (int i = 0; i < clist.length; i++)
      {
         rval[i] = clist[i].get_value();
      }
      return rval;
   }

   /**
    * Get the user's list of available communities ids from the 
    * login information
    * @param info the login info, must never be <code>null</code>
    * @return an array of community ids, may be zero length, 
    * never <code>null</code>
    */
   public int[] getCommunityIds(LoginResponseLoginData info)
   {
      if (info == null)
      {
         throw new IllegalArgumentException("info must never be null");
      }

      LoginResponseLoginDataCommunitiesCommunity clist[] =
         info.getCommunities();
      int[] rval = new int[clist.length];
      for (int i = 0; i < clist.length; i++)
      {
         rval[i] = clist[i].getId().intValue();
      }
      return rval;
   }

   /**
    * Get the user's default locale from the 
    * login information
    * @param info the login info, must never be <code>null</code>
    * @return the user's default locale, never <code>null</code>
    */
   public String getDefaultLocale(LoginResponseLoginData info)
   {
      if (info == null)
      {
         throw new IllegalArgumentException("info must never be null");
      }
      return info.getDefaultLocale();
   }

   /**
    * Get the user's list of available locale names from the 
    * login information
    * @param info the login info, must never be <code>null</code>
    * @return the user's available locales, never <code>null</code>
    */
   public String[] getLocaleNames(LoginResponseLoginData info)
   {
      if (info == null)
      {
         throw new IllegalArgumentException("info must never be null");
      }

      LoginResponseLoginDataLocalesLocale llist[] = info.getLocales();
      String[] rval = new String[llist.length];
      for (int i = 0; i < llist.length; i++)
      {
         rval[i] = llist[i].get_value();
      }
      return rval;
   }

   /**
    * Get the user's list of available locale names from the 
    * login information
    * @param info the login info, must never be <code>null</code>
    * @return the user's available locales, never <code>null</code>
    */
   public String[] getLocaleCodes(LoginResponseLoginData info)
   {
      if (info == null)
      {
         throw new IllegalArgumentException("info must never be null");
      }

      LoginResponseLoginDataLocalesLocale llist[] = info.getLocales();
      String[] rval = new String[llist.length];
      for (int i = 0; i < llist.length; i++)
      {
         rval[i] = llist[i].getCode();
      }
      return rval;
   }

   /**
    * Gets an existing item from the supplied parameters.
    * 
    * @param auth credentials for this call, it may not be <code>null</code>
    * @param contentID The content id of the item. 
    * @param revision The revision of the item.
    * @param includeChildren if <code>true</code>, include children in the
    *    returned object.
    * @param includeBinary if <code>true</code>, include binary data; otherwise 
    *    do not include binary data in the returned object.
    * @param includeRelated if <code>true</code>, include the related item(s)
    *    in the returned object.
    * @param checkout if <code>true</code>, check out the item afterwards.
    * 
    * @return The specified item, never <code>null</code>.
    * 
    * @throws SOAPException if soap error occurs.
    * @throws RemoteException if communication error occurs.
    */
   public Item openItem(Authentication auth, int contentID, int revision,
         boolean includeChildren, boolean includeBinary, 
         boolean includeRelated, boolean checkout) throws SOAPException,
         RemoteException
   {
      if (auth == null)
         throw new IllegalArgumentException("auth may not be null");
      
      //set the authentication soap header
      setAuthenticationHeader((ContentDataBindingStub) m_contentDataPort, auth);
      
      ContentKey contentKey = new ContentKey();
      contentKey.setContentId(new BigInteger(String.valueOf(contentID)));
      contentKey.setRevision(new BigInteger(String.valueOf(revision)));
      
      OpenItemOpenItemRequest openItemRequest = new OpenItemOpenItemRequest();
      openItemRequest.setContentKey(contentKey);
      openItemRequest.setIncludeBinary(includeBinary);
      openItemRequest.setIncludeChildren(includeChildren);
      openItemRequest.setIncludeRelated(includeRelated);
      openItemRequest.setCheckOut(checkout);

      OpenItem parameters = new OpenItem();
      parameters.setOpenItemRequest(openItemRequest);

      // send SOAP message
      OpenItemResponse rsp = m_contentDataPort.openItem(parameters);
      
      return rsp.getItem();
   }
   
   /**
    * Sets the authentication header on the specified port to the specified
    * authentication header data.
    * 
    * @param auth
    */
   private void setAuthenticationHeader(Stub port, Authentication auth)
      throws SOAPException
   {
      SOAPEnvelope env = new SOAPEnvelope();

      SOAPHeaderElement header =
         new SOAPHeaderElement(
            "urn:www.percussion.com/webservices/header",
            "Authentication");

      SOAPElement tmp = null;

      String username = auth.getUsername();
      if (username != null && username.trim().length() > 0)
      {
         tmp = header.addChildElement("Username");
         tmp.addTextNode(username);
      }

      String password = auth.getPassword();
      if (password != null && password.trim().length() > 0)
      {
         tmp = header.addChildElement("Password");
         tmp.addTextNode(password);
      }

      String sessionId = auth.getSessionId();
      if (sessionId != null && sessionId.length() > 0)
      {
         SOAPElement sessionid = header.addChildElement("SessionId");
         sessionid.addTextNode(sessionId);
      }

      if (auth.getOptions() != null
         && auth.getOptions().length > 0)
      {
         SOAPElement options = header.addChildElement("Options");

         AuthenticationOptionsOption[] opts =
            new AuthenticationOptionsOption[auth.getOptions().length];
         opts = auth.getOptions();
         for (int i = 0; i < opts.length; i++)
         {
            SOAPElement option = options.addChildElement("Option");
            Name n = env.createName("name");
            option.addAttribute(n, opts[i].getName());
            option.addTextNode(opts[i].get_value());
         }
      }
      port.setHeader(header);
   }

   /**
    * Storage for all the ports used in WS calls. Initialized on helper 
    * construction, never <code>null</code> after that.
    */
   private DesignPort m_designPort = null;
   private MiscellaneousPort m_miscPort = null;
   private SearchPort m_searchPort = null;
   private ContentDataPort m_contentDataPort = null;

   /**
    * Holds the type of search supported on the server. Initialized once and 
    * never requeried. See {@link #isFtsEnabled(Authentication)}
    */
   private static String ms_searchType = null;

   /* (non-Javadoc)
    * @see com.percussion.integration.IPSWsHelper#isFtsEnabled()
    */
   public boolean isFtsEnabled(HttpServletRequest request)
      throws RemoteException, SOAPException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      Authentication auth = getAuthenticationHeader(request);
      return isFtsEnabled(auth);
   }

   /**
    * Make the webservices request to see if fts is enabled on the
    * server.
    * @param auth authentication information, assumed not <code>null</code>
    * @return <code>true</code> if Fts is enabled on the server.
    */
   private boolean isFtsEnabled(Authentication auth)
      throws SOAPException, RemoteException
   {
      synchronized (PSWsHelper.class)
      {
         if (ms_searchType == null)
         {
            try
            {
               setAuthenticationHeader((Stub) m_searchPort, auth);
               SearchConfiguration req = new SearchConfiguration();
               SearchConfigurationResponse rep =
                  m_searchPort.searchConfiguration(req);
               SearchConfigurationResponseProperty prop[] = rep.getProperty();

               for (int i = 0; i < prop.length; i++)
               {
                  if (prop[i].getName().equals("type"))
                  {
                     ms_searchType = prop[i].getValue();
                     break;
                  }
               }
            }
            catch (Exception se)
            {
               // Catch quietly, this will happen for older versions of
               // Rhythmyx
            }
            if (ms_searchType == null)
            {
               ms_searchType = "unknown";
            }
         }
         return ms_searchType.equals("fts");
      }
   }

   /**
    * Return an item viewer search
    * @param req http request, must never be <code>null</code>.
    * @param itemList list of items, must never be <code>null</code>.
    * @param viewUrl view url, must never be <code>null</code>.
    * @param fieldList list of fields, must never be <code>null</code>.
    * @return The search results for an item viewer
    * @throws RemoteException when an error occurs communicating with the
    * Rhythmyx server. 
    * @throws SOAPException when an error occurs in the web services 
    */
   public PSSearch getItemViewer(
      HttpServletRequest req,
      String itemList,
      String viewUrl,
      List fieldList)
      throws RemoteException, SOAPException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req may not be null");
      }
      if (itemList == null)
      {
         throw new IllegalArgumentException("itemList must never be null");
      }

      Authentication auth = getAuthenticationHeader(req);
      return getItemViewer(auth, itemList, viewUrl, fieldList);
   }

   /**
    * Internal call to get the item viewer results
    * @param auth authentication object, assumed not <code>null</code>.
    * @param itemList list of items, assumed not <code>null</code>.
    * @param viewUrl view url, must never be <code>null</code>.
    * @param fieldList list of fields, assumed not <code>null</code>.
    * @return a search result
    * @throws RemoteException when an error occurs communicating with the
    * Rhythmyx server. 
    * @throws SOAPException when an error occurs in the web services 
    */
   private PSSearch getItemViewer(
      Authentication auth,
      String itemList,
      String viewUrl,
      List fieldList)
      throws RemoteException, SOAPException
   {
      String viewIds = "";
      if (viewUrl != null && viewUrl.trim().length() > 0)
      {
         viewIds = getViewIdList(auth, viewUrl);
      }
      return getSearchFromIds(auth, itemList + viewIds, fieldList, true);
   }

   /**
    * Gets the list of content ids returned from a view. 
    * @param auth the authentication for calling the Rhythmyx server.
    * @param viewName the name of the view. The relative URL.   
    * @return the list of ids. Never <code>null</code> may be empty. 
    * @throws RemoteException when an error occurs communicating with the
    * Rhythmyx server. 
    * @throws SOAPException when an error occurs in the web services 
    */
   private String getViewIdList(Authentication auth, String viewName)
      throws RemoteException, SOAPException
   {
      // get the inbox list of ids as an xml string
      String data = executeCallDirect(auth, viewName, null);
      StringBuilder ids = new StringBuilder();

      /**
       * The data returned from the above call looks like the following:
       * 
       * <View>
       *    <Item sys_contentid="301"/>
       *    <Item sys_contentid="302"/>
       * </View>
       */
      if (data == null)
         data = "<View></View>"; // Should this be escalated?

      Document doc = toDOM(data);
      Element el = doc.getDocumentElement();

      NodeList nl = el.getElementsByTagName("Item");
      if (nl.getLength() > 0)
      {
         Element itemEl = (Element) nl.item(0);
         ids.append(itemEl.getAttribute(IPSHtmlParameters.SYS_CONTENTID));
         for (int i = 1; i < nl.getLength(); i++)
         {
            itemEl = (Element) nl.item(i);
            ids.append("," 
                  + itemEl.getAttribute(IPSHtmlParameters.SYS_CONTENTID));
         }
      }
      return ids.toString();
   }

   /**
    * Returns the search result given the ids and fields.
    * @param auth
    * @param idList
    * @param fieldList
    * @param includeActionPage
    * @return a new {@link PSSearch} object that represents the results
    * of the search. Never <code>null</code>
    * @throws RemoteException
    */
   private PSSearch getSearchFromIds(
      Authentication auth,
      String idList,
      List fieldList,
      boolean includeActionPage)
      throws RemoteException, SOAPException
   {
      PSSearch retSearch = new PSSearch();
      if (idList.trim().length() > 0)
      {
         // now that we have the list of content id's execute a search
         PSSearch search = new PSSearch();
         PSItem item = new PSItem();

         // create a query field with all the content ids found
         item.addQueryField(IPSHtmlParameters.SYS_CONTENTID, idList, 
               OperatorTypes._in, null);

         // add any specified fields to the results
         if (fieldList != null)
         {
            Iterator columns = fieldList.iterator();
            while (columns.hasNext())
            {
               PSColumn column = (PSColumn) columns.next();
               item.addSelectField(column.getFieldName());
            }
         }
         search.addItem(item);

         // now execute the search
         retSearch = executeSearch(auth, search, null, includeActionPage);
      }
      return retSearch;
   }
}
