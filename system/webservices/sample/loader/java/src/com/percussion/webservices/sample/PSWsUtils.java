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
package com.percussion.webservices.sample;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;

import com.percussion.webservices.content.AddFolderChildrenRequest;
import com.percussion.webservices.content.AddFolderTreeRequest;
import com.percussion.webservices.content.CheckinItemsRequest;
import com.percussion.webservices.content.ContentSOAPStub;
import com.percussion.webservices.content.CreateItemsRequest;
import com.percussion.webservices.content.FindFolderChildrenRequest;
import com.percussion.webservices.content.FolderRef;
import com.percussion.webservices.content.LoadItemsRequest;
import com.percussion.webservices.content.PSFolder;
import com.percussion.webservices.content.PSItem;
import com.percussion.webservices.content.PSItemStatus;
import com.percussion.webservices.content.PSItemSummary;
import com.percussion.webservices.content.ReleaseFromEditRequest;
import com.percussion.webservices.content.SaveItemsRequest;
import com.percussion.webservices.content.SaveItemsResponse;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;
import com.percussion.webservices.rhythmyx.ContentLocator;
import com.percussion.webservices.rhythmyx.SecurityLocator;
import com.percussion.webservices.rhythmyx.SystemLocator;
import com.percussion.webservices.security.LoginRequest;
import com.percussion.webservices.security.LoginResponse;
import com.percussion.webservices.security.LogoutRequest;
import com.percussion.webservices.security.SecuritySOAPStub;
import com.percussion.webservices.security.data.PSLogin;
import com.percussion.webservices.system.SystemSOAPStub;
import com.percussion.webservices.system.TransitionItemsRequest;

/**
 * This is a utility class to demonstrate how to use the Rhythmyx
 * webservice API.  In particular, this class demonstrates how to maintain
 * sessions for both Rhythmyx and the JBoss container across all service 
 * instances.  The Rhythmyx session is communicated through the SOAP header, but the
 * JBoss session (JSESSION) is communicated through the HTTP Cookie header.
 * Note:  The current implementation of the Axis 1.3 client can only maintain one
 * JSESSION per service stub instance.
 */
public class PSWsUtils
{
   /**
    * Creates a Content Item of the specified content type.
    * 
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param contentType the Content Type for the created item, assumed not 
    *    to be <code>null</code> or empty.
    *    
    * @return the created Content Item.  The Content Item is not yet persisted
    *    to the Repository.  Never <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   public static PSItem createItem(ContentSOAPStub binding, String contentType)
      throws Exception
   {
      CreateItemsRequest request = new CreateItemsRequest();
      request.setContentType(contentType);
      request.setCount(1);
      PSItem[] items = binding.createItems(request);
      return items[0];
   }
   
   /**
    * Saves the specified Content Item to the repository.
    * 
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param item the Content Item to be saved; assumed not to be
    *    <code>null</code>.
    * 
    * @return the ID of the saved Content Item; never <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   public static long saveItem(ContentSOAPStub binding, PSItem item)
      throws Exception
   {
      SaveItemsRequest req = new SaveItemsRequest();
      req.setPSItem(new PSItem[]{item});
      SaveItemsResponse response = binding.saveItems(req);
      
      return response.getIds()[0];
   }
   
   /**
    * Loads the specified Content Item.
    * 
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param id the ID of the Content Item to be loaded.
    * 
    * @return the specified Content Item, never <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   public static PSItem loadItem(ContentSOAPStub binding, long id)
      throws Exception
   {
      LoadItemsRequest req = new LoadItemsRequest();
      req.setId(new long[]{id});
      req.setIncludeBinary(true);
      req.setAttachBinaries(true);
      PSItem[] items = binding.loadItems(req);
      return items[0];
   }
   
   /**
    * Checkin the specified Content Item.
    * 
    * @param binding the proxy of the content service, assumed not to be 
    *    <code>null</code>.
    * @param id the ID of the Content Item to be checked in.
    * 
    * @throws Exception if an error occurs.
    */
   public static void checkinItem(ContentSOAPStub binding, long id)
      throws Exception
   {
      CheckinItemsRequest req = new CheckinItemsRequest();
      req.setId(new long[]{id});
      binding.checkinItems(req);
   }

   /**
    * Prepares the specified Content Item for Edit.
    * 
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param id the ID of the Content Item to be prepared for editing.
    *
    * @return The status of the specified Content Item, which can be used to 
    *    reverse the prepareForEdit action by calling 
    *    {@link #releaseFromEdit(ContentSOAPStub, PSItemStatus)}
    * 
    * @throws Exception if an error occurs.
    */
   public static PSItemStatus prepareForEdit(ContentSOAPStub binding, long id)
      throws Exception
   {
      return binding.prepareForEdit(new long[] {id})[0];
   }

   /**
    * Releases the specified Content Item from Edit; reverse action of
    * {@link #prepareForEdit(ContentSOAPStub, long)}.
    * 
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param status the status of the Content Item to be released for edit;
    *    assumed not to be <code>null</code>.
    *  
    * @throws Exception if an error occurs.
    */
   public static void releaseFromEdit(ContentSOAPStub binding, 
      PSItemStatus status) throws Exception
   {
      ReleaseFromEditRequest req = new ReleaseFromEditRequest();
      req.setPSItemStatus(new PSItemStatus[] {status});
      binding.releaseFromEdit(req);
   }
   
   /**
    * Performs the Workflow Transition with the specified Trigger name for
    * the specified Content Item.
    *  
    * @param binding the proxy of the system service; assumed not to be 
    *    <code>null</code>.
    * @param id  the ID of the Content Item to Transition.
    * @param trigger the Trigger name of the Workflow Transition; assumed
    *    not to be <code>null</code> or empty.
    * 
    * @throws Exception if an error occurs.
    */
   public static void transitionItem(SystemSOAPStub binding, long id, 
      String trigger) throws Exception
   {
      TransitionItemsRequest req = new TransitionItemsRequest();
      req.setId(new long[]{id});
      req.setTransition(trigger);
      binding.transitionItems(req);
   }
   
   /**
    * Finds all immediate child Content Items and child Folders of the specified 
    * Folder.
    * 
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param folderPath  the path of the Folder whose children you want to find;
    *    assumed not to be <code>null</code> or empty. Provide '/' to get all 
    *    root folders such as <code>Folders</code> and <code>Sites</code>.
    *    
    * @return the result of the search for child objects; never 
    *    <code>null</code>, but may be empty.
    * 
    * @throws Exception if an error occurs.
    */
   public static PSItemSummary[] findFolderChildren(ContentSOAPStub binding,
      String folderPath) throws Exception
   {
      FindFolderChildrenRequest req = new FindFolderChildrenRequest();
      req.setFolder(new FolderRef(null, folderPath));
      return binding.findFolderChildren(req);
   }

   /**
    * Associates the specified Content Items with the specified Folder.
    * 
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param folderPath the path of the Folder to which you want to add the 
    *    child objects;, assumed not to be <code>null</code> or empty.
    * @param childIds the IDs of the objects to be associated with the Folder 
    *    specified in the folderPath parameter; assumed not <code>null</code> or 
    *    empty.
    *    
    * @throws Exception if an error occurs.
    */
   public static void addFolderChildren(ContentSOAPStub binding, 
      String folderPath, long[] childIds) throws Exception
   {
      AddFolderChildrenRequest req = new AddFolderChildrenRequest();
      req.setChildIds(childIds);
      req.setParent(new FolderRef(null, folderPath));
      binding.addFolderChildren(req);
   }
   
   /**
    * Creates Folders for the specified Folder path.  Any Folders specified in 
    * the path that do not exist will be created; No action is taken on any 
    * existing Folders.
    *  
    * @param binding the proxy of the content service; assumed not to be 
    *    <code>null</code>.
    * @param folderPath the Folder path to be updated; assumed not to be
    *    <code>null</code> or empty.
    * 
    * @return the created folder objects, never <code>null</code>, may be empty.
    * 
    * @throws Exception if an error occurs.
    */
   public static PSFolder[] addFolderTree(ContentSOAPStub binding,
      String folderPath) throws Exception
   {
      AddFolderTreeRequest req = new AddFolderTreeRequest();
      req.setPath(folderPath);
      return binding.addFolderTree(req);
   }
   
   /**
    * Sets a new server connection with the supplied parameters.  The new
    * connection information will be used for subsequent calls to get 
    * the service proxies, {@link #getContentService(String)},
    * {@link #getSecurityService()} and {@link #getSystemService(String)}.
    *
    * @param protocol the protocol of the server connection; assumed not to be
    *    <code>null</code> or empty. Defaults to <code>http</code>.
    * @param host the host name of the server connection; assumed not to be
    *    <code>null</code> or empty. Defaults to <code>localhost</code>.
    * @param port the port of the server connection; Defaults to 9992
    */
   public static void setConnectionInfo(String protocol, String host, int port)
   {
      ms_protocol = protocol;
      ms_host = host;
      ms_port = port;
   }

   /**
    * Logs in with the specified credentials and associated parameters.
    *
    * @param binding the proxy of the security service; assumed not to be 
    *    <code>null</code>. If the login attempt is successful, this object 
    *    will be modified to maintain the Rhythmyx session.
    * @param user the login user name; assumed not to be <code>null</code> or 
    *    empty.
    * @param password the password of the login user; assumed not to be
    *    <code>null</code> or empty.
    * @param community the name of the Community into which to login the user; 
    *    may be <code>null</code> or empty, in which case the user is logged 
    *    in to the last Community they logged in to, or, if the user has never 
    *    logged in before, into the first Community in alphabetical order.
    * @param locale the name of the Locale into which to log the user; may be 
    *    <code>null</code> or empty , in which case the user is logged 
    *    in to the last Locale they logged in to, or, if the user has never 
    *    logged in before, into the first Locale in alphabetical order.
    *
    * @return the Rhythmyx session, never <code>null</code> or empty.
    *
    * @throws PSNotAuthenticatedFault if authentication of the passed 
    *   credentials fails.
    */
   public static String login(SecuritySOAPStub binding, String user,
      String password, String community, String locale)
      throws PSNotAuthenticatedFault
   {
      if (user == null || user.length() == 0)
         throw new IllegalArgumentException("user may not be null or empty.");
      if (password == null || password.length() == 0)
         throw new IllegalArgumentException("password may not be null or empty.");

      LoginRequest loginReq = new LoginRequest();
      loginReq.setUsername(user);
      loginReq.setPassword(password);
      loginReq.setCommunity(community);
      loginReq.setLocaleCode(locale);

      try
      {
         LoginResponse loginResp = binding.login(loginReq);
         PSLogin loginObj = loginResp.getPSLogin();

         String rxSession = loginObj.getSessionId();

         // Setting to maintain the returned Rhythmyx session for all
         // subsequent requests from the service object.
         setRxSessionHeader(binding, rxSession);

         return rxSession;
      }
      catch (PSNotAuthenticatedFault e)
      {
         throw e;
      }
      catch (Exception e)
      {
         // not possible
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   /**
    * Logs out the specified Rhythmyx session.
    * 
    * @param binding the security proxy, assumed not to be <code>null</code>.
    * @param rxSession the Rhythmyx session for which to log out 
    * 
    * @throws Exception if any error occurs.
    */
   public static void logout(SecuritySOAPStub binding, String rxSession) 
      throws Exception
   {
      LogoutRequest logoutReq = new LogoutRequest();
      logoutReq.setSessionId(rxSession);
      binding.logout(logoutReq);
   }
   
   /**
    * Creates a proxy of the content service. Must provide a valid Rhythmyx 
    * session from {@link #login(SecuritySOAPStub, String, String, String, 
    * String)}.
    *
    * @param rxSession the Rhythmyx session; assumed to be a valid Rhythmyx
    *    session from {@link #login(SecuritySOAPStub, String, String, String,
    *    String)}, not <code>null</code> or empty.
    *
    *
    * @return the proxy of the content service; never <code>null</code>. 
    *    This method uses the server connection information that is saved with 
    *    this class. The connection information can be overridden by
    *    {@link #setConnectionInfo(String, String, int)}.
    *
    * @see #setConnectionInfo(String, String, int)
    *
    * @throws ServiceException if failed to create the content service instance.
    */
   public static ContentSOAPStub getContentService(String rxSession)
      throws ServiceException
   {
      ContentLocator locator = new ContentLocator();
      String address = getNewAddress(locator.getcontentSOAPAddress());
      locator.setcontentSOAPEndpointAddress(address);

      ContentSOAPStub binding = (ContentSOAPStub) locator.getcontentSOAP();
      setAttachmentFormat(binding);

      // Setting to maintain one JBoss session (JSESSION) for all requests
      binding.setMaintainSession(true);

      // Setting to maintain the given Rhythmyx session for all requests
      setRxSessionHeader(binding, rxSession);

      return binding;
   }

   /**
    * Creates a system service instance; must provide a valid Rhythmyx session
    * from {@link #login(SecuritySOAPStub, String, String, String, String)}.
    *
    * @param rxSession the Rhythmyx session; assumed to be a valid Rhythmyx
    *    session from {@link #login(SecuritySOAPStub, String, String, String,
    *    String)}, not <code>null</code> or empty.
    *
    * @return the proxy of the system service; never <code>null</code>.  
    *    This method uses the server connection information that is saved with 
    *    this class. The connection information can be overridden by
    *    {@link #setConnectionInfo(String, String, int)}.
    *
    * @see #setConnectionInfo(String, String, int)
    *
    * @throws ServiceException if the method fails to create the system 
    *     service instance.
    */
   public static SystemSOAPStub getSystemService(String rxSession)
      throws ServiceException
   {
      SystemLocator locator = new SystemLocator();
      String address = getNewAddress(locator.getsystemSOAPAddress());
      locator.setsystemSOAPEndpointAddress(address);

      SystemSOAPStub binding = (SystemSOAPStub) locator.getsystemSOAP();

      // Setting to maintain one JBoss session (JSESSION) for all requests
      binding.setMaintainSession(true);

      // Setting to maintain the given Rhythmyx session for all requests
      setRxSessionHeader(binding, rxSession);

      return binding;
   }

   /**
    * Creates a proxy of the security service. It is the caller's responsibility
    * to call {@link #login(SecuritySOAPStub, String, String, String, String)}
    * with the returned object.
    *
    * @return the created proxy of the security service, never 
    *    <code>null</code>. This method uses the server connection information 
    *    that is saved with this class. However, the connection information 
    *    can be overridden by {@link #setConnectionInfo(String, String, int)}.
    *
    * @see #setConnectionInfo(String, String, int)
    *
    * @throws ServiceException if the method fails to create the new stub.
    */
   public static SecuritySOAPStub getSecurityService()
      throws ServiceException
   {
      SecurityLocator locator = new SecurityLocator();
      String address = getNewAddress(locator.getsecuritySOAPAddress());
      locator.setsecuritySOAPEndpointAddress(address);

      SecuritySOAPStub binding = (SecuritySOAPStub) locator.getsecuritySOAP();
      
      // setting for maintaining JBoss session (JSESSION)
      binding.setMaintainSession(true);

      return binding;
   }

   /**
    * Sets the MIME format of the attachment of the specified stub.
    *
    * @param binding the proxy for which to set MIME format; assumed not to be
    *   <code>null</code>.
    *
    * @throws ServiceException if the method fails to set the attachment format.
    */
   private static void setAttachmentFormat(org.apache.axis.client.Stub binding)
      throws ServiceException
   {
      Call call = binding._getCall();
      if (call == null)
         call = binding._createCall();
      call.setProperty(Call.ATTACHMENT_ENCAPSULATION_FORMAT,
         Call.ATTACHMENT_ENCAPSULATION_FORMAT_MIME);
   }

   /**
    * Creates a new address from the specified source address.
    *
    * @param srcAddress the source address; assumed not to be <code>null</code>
    *    or empty.
    *
    * @return The same address as the specified source address but with the 
    *    the connection information (protocol, host and port) of this class
    *    replacing the original ones.
    */
   private static String getNewAddress(String srcAddress)
   {
      try
      {
         URL url = new URL(srcAddress);
         return ms_protocol + "://" + ms_host + ":" + ms_port + "/"
               + url.getPath();
      }
      catch (MalformedURLException e)
      {
         // this is not possible
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   /**
    * Sets the Rhythmyx session as header to the specified proxy. Clears all
    * existing headers before the new session header is set.
    *
    * @param binding the proxy to which to add the Rhythmyx session as
    *    header; assumed not to be <code>null</code>.
    * @param rxSession The Rhythmyx session; assumed to be valid a Rhythmyx
    *    session from {@link #login(SecuritySOAPStub, String, String, String,
    *    String)}, not <code>null</code> or empty.
    */
   private static void setRxSessionHeader(Stub binding, String rxSession)
   {
      binding.clearHeaders();
      binding.setHeader("urn:www.percussion.com/6.0.0/common", "session",
         rxSession);
   }

   /**
    * The protocol of the server connection. Defaults to 'http'.
    */
   private static String ms_protocol = "http";
   
   /**
    * The host name of the server connection. Defaults to 'localhost'.
    */
   private static String ms_host = "localhost";

   /**
    * The port of the server connection. Defaults to 9992.
    */
   private static int ms_port = 9992;
}
