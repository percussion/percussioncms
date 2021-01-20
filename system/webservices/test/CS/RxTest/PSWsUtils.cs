using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Web.Services.Protocols;
using RxTest.RxWebServices;


namespace RxTest
{
    /// <summary>
    /// 
    /// This is a utility class to demonstrate how to use the Rhythmyx
    /// webservice API.  In particular, this class demonstrates how to maintain
    /// sessions for both Rhythmyx and the JBoss container across all service 
    /// instances.  The Rhythmyx session is communicated through the SOAP header, but the
    /// JBoss session (JSESSION) is communicated through the HTTP Cookie header.
    /// </summary>
    class PSWsUtils
    {
        /// <summary>
        /// The protocol of the server connection. Defaults to 'http'.
        /// </summary>
        private static string ms_protocol = "http";

        /// <summary>
        /// The host name of the server connection. Defaults to 'localhost'
        /// </summary>
        private static string ms_host = "localhost";

        /// <summary>
        /// The port of the server connection. Defaults to 9992.
        /// </summary>
        private static int ms_port = 9992;

        /// <summary>
        /// Sets a new server connection with the supplied parameters.  The new
        /// connection information will be used for subsequent calls to get 
        /// the service proxyes, GetContentSerive(), GetSecurityService() and 
        /// GetSystemSerive().
        /// </summary>
        /// 
        /// <param name="protocol">
        ///     the protocol of the server connection; assumed not to be
        ///     <code>null</code> or empty. Defaults to <code>http</code>.
        /// </param>
        /// <param name="host">
        ///     the host name of the server connection; assumed not to be
        ///     <code>null</code> or empty. Defaults to <code>localhost</code>.
        /// </param>
        /// <param name="port">
        ///     the port of the server connection. Defaults to 9992
        /// </param>
        public static void SetConnectionInfo(string protocol, string host, int port)
        {
            ms_protocol = protocol;
            ms_host = host;
            ms_port = port;
        }

        /// <summary>
        ///     Creates a new address from the specified source address.
        /// </summary>
        /// <param name="srcAddress">
        ///     the source address; assumed not to be <code>null</code> or empty.
        /// </param>
        /// <returns>
        ///     The same address as the specified source address but with the 
        ///     the connection information (protocol, host and port) of this class
        ///     replacing the original ones.
        /// </returns>
        private static String GetNewAddress(String srcAddress)
        {
            int pathStart = srcAddress.IndexOf("/Rhythmyx/");
            return ms_protocol + "://" + ms_host + ":" + ms_port + 
                srcAddress.Substring(pathStart);
        }

        /// <summary>
        ///     Creates a proxy of the security service. It is the caller's 
        ///     responsibility to call login() with the returned object.
        /// </summary>
        /// <returns>
        ///     the created proxy of the security service; never 
        ///     <code>null</code>. This method uses the server connection 
        ///     information that is saved with this class. However, the 
        ///     connection information can be overriden by calling 
        ///     setConnectionInfo()
        /// </returns>
        public static securitySOAP GetSecurityService()
        {
            securitySOAP securitySvc = new securitySOAP();
            securitySvc.Url = GetNewAddress(securitySvc.Url);

            // create a cookie object to maintain JSESSION
            CookieContainer cookie = new CookieContainer();
            securitySvc.CookieContainer = cookie;

            return securitySvc;
        }

        /// <summary>
        /// Login with the specified credentials and associated parameters.
        /// </summary>
        /// 
        /// <param name="securitySvc">
        ///     the proxy of the security service; assumed not to be 
        ///     <code>null</code>. If the login attempt is successful, this object 
        ///     will be modified to maintain the Rhythmyx session.
        /// <param name="user">
        ///     the login user name; assumed not to be <code>null</code> or empty.
        /// </param>
        /// <param name="password">
        ///     the password of the login user; assumed not to be <code>null</code>
        ///     or empty
        /// </param>
        /// <param name="community">
        ///     the name of the Community into which to log the user; 
        ///     may be <code>null</code> or empty, in which case the user is logged 
        ///     in to the last Community they logged in to, or, if the user has never 
        ///     logged in before, into the first Community in alphabetical order.
        /// <param name="locale">
        ///     the name of the Locale into which to log the user; may be 
        ///     <code>null</code> or empty , in which case the user is logged 
        ///     in to the last Locale they logged in to, or, if the user has never 
        ///     logged in before, into the first Locale in alphabetical order.
        /// </param>
        /// <returns>
        ///     the Rhythmyx session, never <code>null</code> or empty.
        /// </returns>
        public static string Login(securitySOAP securitySvc, string user, 
            string password, string community, string locale)
        {
            LoginRequest loginReq = new LoginRequest();
            loginReq.Username = user;
            loginReq.Password = password;
            loginReq.Community = community;
            loginReq.LocaleCode = locale;

            // Setting the authentication header to maintain Rhythmyx session
            LoginResponse loginResp = securitySvc.Login(loginReq);
            string rxSession = loginResp.PSLogin.sessionId;
            securitySvc.PSAuthenticationHeaderValue = new PSAuthenticationHeader();
            securitySvc.PSAuthenticationHeaderValue.Session = rxSession;

            return rxSession;
        }

        /// <summary>
        ///     Logs out the specified Rhythmyx session.
        /// </summary>
        /// <param name="securitySvc">
        ///     the security proxy, assumed not to be <code>null</code>.
        /// </param>
        /// <param name="rxSession">
        ///     the Rhythmyx session for which to log out.
        /// </param>
        public static void Logout(securitySOAP securitySvc, String rxSession) 
        {
            LogoutRequest logoutReq = new LogoutRequest();
            logoutReq.SessionId = rxSession;
            securitySvc.Logout(logoutReq);
        }

        /// <summary>
        ///     Creates a proxy of the content service with the specified cookie
        ///     and authentication header values.
        /// </summary>
        /// <param name="cookie">
        ///     the cookie container for maintaining the JSESSION for all
        ///     webservice requests. Assumed to be a valid cookie container 
        ///     which was used for the Login operation.
        /// </param>
        /// <param name="authHeader">
        ///     the authentication header for maintaining the Rhythmyx session
        ///     for all webservice requests. Assumed to be a valid auth header
        ///     which contains a valid Rhythmyx session.
        /// </param>
        /// <returns>
        ///     the proxy of the content service, never <code>null</code>. 
        ///     This method uses the server connection information that is 
        ///     saved with this class. The connection information can be 
        ///     overriden by setConnectionInfo().
        /// </returns>
        public static contentSOAP GetContentService(CookieContainer cookie, 
            PSAuthenticationHeader authHeader)
        {
            contentSOAP contentSvc = new contentSOAP();
            contentSvc.Url = GetNewAddress(contentSvc.Url);

            contentSvc.CookieContainer = cookie;
            contentSvc.PSAuthenticationHeaderValue = authHeader;

            return contentSvc;
        }

        /// <summary>
        ///     Creates a proxy of the system service with the specified cookie
        ///     and authentication header values.
        /// </summary>
        /// <param name="cookie">
        ///     the cookie container for maintaining the JSESSION for all
        ///     webservice requests. Assumed to be a valid cookie container 
        ///     which was used for the Login operation.
        /// </param>
        /// <param name="authHeader">
        ///     the authentication header for maintaining the Rhythmyx session
        ///     for all webservice requests. Assumed to be a valid auth header
        ///     which contains a valid Rhythmyx session.
        /// </param>
        /// <returns>
        ///     the proxy of the system service, never <code>null</code>. 
        ///     This method uses the server connection information that is 
        ///     saved with this class. The connection information can be 
        ///     overriden by setConnectionInfo().
        /// </returns>
        /// 
        public static systemSOAP GetSystemService(CookieContainer cookie,
            PSAuthenticationHeader authHeader)
        {
            systemSOAP systemSvc = new systemSOAP();
            systemSvc.Url = GetNewAddress(systemSvc.Url);

            systemSvc.CookieContainer = cookie;
            systemSvc.PSAuthenticationHeaderValue = authHeader;

            return systemSvc;
        }

        /// <summary>
        ///     Creates a proxy of the assembly service with the specified cookie
        ///     and authentication header values.
        /// </summary>
        /// <param name="cookie">
        ///     the cookie container for maintaining the JSESSION for all
        ///     webservice requests. Assumed to be a valid cookie container 
        ///     which was used for the Login operation.
        /// </param>
        /// <param name="authHeader">
        ///     the authentication header for maintaining the Rhythmyx session
        ///     for all webservice requests. Assumed to be a valid auth header
        ///     which contains a valid Rhythmyx session.
        /// </param>
        /// <returns>
        ///     the proxy of the assembly service, never <code>null</code>. 
        ///     This method uses the server connection information that is 
        ///     saved with this class. The connection information can be 
        ///     overriden by setConnectionInfo().
        /// </returns>
        /// 
        public static assemblySOAP GetAssemblyService(CookieContainer cookie,
            PSAuthenticationHeader authHeader)
        {
            assemblySOAP assemblySvc = new assemblySOAP();
            assemblySvc.Url = GetNewAddress(assemblySvc.Url);

            assemblySvc.CookieContainer = cookie;
            assemblySvc.PSAuthenticationHeaderValue = authHeader;

            return assemblySvc;
        }

        /// <summary>
        ///     Creates a proxy of the UI service with the specified cookie
        ///     and authentication header values.
        /// </summary>
        /// <param name="cookie">
        ///     the cookie container for maintaining the JSESSION for all
        ///     webservice requests. Assumed to be a valid cookie container 
        ///     which was used for the Login operation.
        /// </param>
        /// <param name="authHeader">
        ///     the authentication header for maintaining the Rhythmyx session
        ///     for all webservice requests. Assumed to be a valid auth header
        ///     which contains a valid Rhythmyx session.
        /// </param>
        /// <returns>
        ///     the proxy of the UI service, never <code>null</code>. 
        ///     This method uses the server connection information that is 
        ///     saved with this class. The connection information can be 
        ///     overriden by setConnectionInfo().
        /// </returns>
        /// 
        public static uiSOAP GetUiService(CookieContainer cookie,
            PSAuthenticationHeader authHeader)
        {
            uiSOAP uiSvc = new uiSOAP();
            uiSvc.Url = GetNewAddress(uiSvc.Url);

            uiSvc.CookieContainer = cookie;
            uiSvc.PSAuthenticationHeaderValue = authHeader;

            return uiSvc;
        }
        /// <summary>
        /// Creates Folders for the specified Folder path.  Any Folders specified in 
        /// the path that do not exist will be created; No action is taken on any  
        /// existing Folders.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service, assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="folderPath">
        ///     the Folder path to be updated; assumed not to be
        ///     <code>null</code> or empty.
        /// </param>
        /// <returns>the created folder objects.</returns>
        /// 
        public static PSFolder[] AddFolderTree(contentSOAP contentSvc,
            string folderPath)
        {
            AddFolderTreeRequest req = new AddFolderTreeRequest();
            req.Path = folderPath;
            return contentSvc.AddFolderTree(req);
        }

        /// <summary>
        /// Creates an Content Item of the specified content type.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="contentType">
        ///     the content type for the Created Item; assumed not 
        ///     to be <code>null</code> or empty.
        /// </param>
        /// <returns>
        ///     the created Content Item.  The Content Item is not yet 
        ///     persisted to the Repository.  Never <code>null</code>.
        /// </returns>
        public static PSItem CreateItem(contentSOAP contentSvc, string contentType)
        {
            CreateItemsRequest request = new CreateItemsRequest();
            request.ContentType = contentType;
            request.Count = 1;
            PSItem[] items = contentSvc.CreateItems(request);
            return items[0];
        }

        /// <summary>
        ///     Saves the specified Content Item to the repository.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="item">
        ///     the Content Item to be saved; assumed not to be
        ///     <code>null</code>.
        /// </param>
        /// <returns>
        ///     the ID of the saved Content Item; never <code>null</code>.
        /// </returns>
        public static long SaveItem(contentSOAP contentSvc, PSItem item)
        {
            SaveItemsRequest req = new SaveItemsRequest();
            req.PSItem = new PSItem[]{item};
            SaveItemsResponse response = contentSvc.SaveItems(req);
      
            return response.Ids[0];
        }

        /// <summary>
        ///     Loads the specified Content Item.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="id">
        ///     the ID of the Content Item to be loaded.
        /// </param>
        /// <returns>
        ///     the specified Content Item, never <code>null</code>.
        /// </returns>
        public static PSItem LoadItem(contentSOAP contentSvc, long id)
        {
            LoadItemsRequest req = new LoadItemsRequest();
            req.Id = new long[] { id };
            PSItem[] items = contentSvc.LoadItems(req);
            return items[0];
        }

        /// <summary>
        ///     Checkin the specified Content Item.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="id">
        ///     the ID of the Content Item to be checked in.
        /// </param>
        public static void CheckinItem(contentSOAP contentSvc, long id)
        {
            CheckinItemsRequest req = new CheckinItemsRequest();
            req.Id = new long[] { id };
            contentSvc.CheckinItems(req);
        }

        /// <summary>
        ///     Prepares the specified Content Item for Edit.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="id">
        ///     the ID of the Content Item to be prepared for editing.
        /// </param>
        /// <returns>
        ///     the status of the specified Content Item, which can be used to 
        ///     reverse the prepareForEdit action by calling releaseFromEdit()
        /// </returns>
        public static PSItemStatus PrepareForEdit(contentSOAP contentSvc, long id)
        {
            return contentSvc.PrepareForEdit(new long[]{ id })[0];
        }

        /// <summary>
        ///     Release the specified Content Item from Edit, which is the reverse action of
        ///     prepareForEdit().
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="status">
        ///     the status of the Content Item to be released for edit;
        ///     assumed not to be <code>null</code>.
        /// </param>
        public static void ReleaseFromEdit(contentSOAP contentSvc, PSItemStatus status)
        {
            ReleaseFromEditRequest req = new ReleaseFromEditRequest();
            req.PSItemStatus = new PSItemStatus[] { status };
            contentSvc.ReleaseFromEdit(req);
        }

        /// <summary>
        ///     Performs the Workflow Transition with the specified Trigger name for
        ///     the specified Content Item.
        /// </summary>
        /// <param name="systemSvc">
        ///     the proxy of the system service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="id">
        ///     the ID of the Content Item to Transition.
        /// </param>
        /// <param name="trigger">
        ///     the Trigger name of the Workflow Transition; assumed
        ///     not to be <code>null</code> or empty.
        /// </param>
        public static void TransitionItem(systemSOAP systemSvc, long id,
            string trigger)
        {
            TransitionItemsRequest req = new TransitionItemsRequest();
            req.Id = new long[] { id };
            req.Transition = trigger;
            systemSvc.TransitionItems(req);
        }

        /// <summary>
        ///     Finds all immediate child Content Items and child Folders of the specified 
        ///     Folder.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="folderPath">
        ///     the path of the Folder whose children you want to find;
        ///     assumed not to be <code>null</code> or empty.
        /// </param>
        /// <returns>
        ///     the result of the search for child objects; never 
        ///     <code>null</code>, but may be empty.
        /// </returns>
        public static PSItemSummary[] FindFolderChildren(contentSOAP contentSvc, 
            string folderPath)
        {
            FindFolderChildrenRequest req = new FindFolderChildrenRequest();
            req.Folder = new FolderRef();
            req.Folder.Path = folderPath;
            return contentSvc.FindFolderChildren(req);
        }

        /// <summary>
        ///     Associates the specified Content Items with the specified Folder.
        /// </summary>
        /// <param name="contentSvc">
        ///     the proxy of the content service; assumed not to be 
        ///     <code>null</code>.
        /// </param>
        /// <param name="folderPath">
        ///     the path of the Folder to which you want to add the 
        ///     child objects;, assumed not to be <code>null</code> or empty.
        /// </param>
        /// <param name="childIds">
        ///     the IDs of the objects to be associated with the Folder specified 
        ///     in the folderPath paramter; assumed not <code>null</code> or empty.
        /// </param>
        public static void AddFolderChildren(contentSOAP contentSvc,
            string folderPath, long[] childIds)
        {
            AddFolderChildrenRequest req = new AddFolderChildrenRequest();
            req.ChildIds = childIds;
            req.Parent = new FolderRef();
            req.Parent.Path = folderPath;
            contentSvc.AddFolderChildren(req);
         }

        /// returns the first value from the specified field of a content item 
        /// 
        /// Throws an exception if the specified field is not found
        public static String GetFirstFieldValue(PSItem item, String fieldname)
        {
            for (int inc = 0; inc < item.Fields.Length; ++inc)
            {
                if (item.Fields[inc].name == fieldname)
                {
                    return item.Fields[inc].PSFieldValue[0].RawData;
                }
            }
            PSFileUtils.RxAssert(false, String.Format("Field {0} not found", fieldname));
            return null;
        }

       // Compares to arrays of longs and throws an exception if they are not 
       // filled with the same values
       public static void compareLongArrays(long[] first, long[] second)
       {
          PSFileUtils.RxAssert(first.Length == second.Length);

          for (int inc = 0; inc < first.Length; ++inc)
          {
             PSFileUtils.RxAssert(first[inc] == second[inc]);
          }
       }

       /**
        * Creates a content type reference for the specified content type id
        *  
        * @param id the content type id for which to create the reference object.
        * 
        * @return the reference object for the specified content type id.
        */
       static public Reference getContentTypeRef(long id)
         {
            Reference  reference = new Reference();
            long        guid = 0;

            switch(id)
            {
               case 301:
                  guid = 8589934893;
                  break;
               case 101:
                  guid = 8589934693;
                  break;
               case 312:
                  guid = 8589934904;
                  break;
               case 315:
                  guid = 8589934907;
                  break;
               default:
                  PSFileUtils.RxAssert(String.Format("Unrecognized TypeID {0}"));
                  break;
            }

            reference.id     = guid;
            reference.name   = null;

            return reference;
         }

         /**
          * Gets the long value of the GUID for a specified relationship id.
          * 
          * @param id the relationship id for which to get the GUID value.
          * 
          * @return the long value as described above.
          */
         static public long getRelationshipId(long id)
         {
            long guid = 0;

            switch (id)
            {
               case 1:
                  guid = 81604378625;
                  break;
               case long.MaxValue:
                  guid = 83751862271;
                  break;
               default:
                  PSFileUtils.RxAssert(String.Format("Unrecognized TypeID {0}"));
                  break;
            }

            return guid;
         }

         static public LoginRequest getLoginRequest(String userName, String password, String clientId, String comunity, String localeCode)
         {

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.Username = userName;
            loginRequest.Password = password;
            loginRequest.ClientId = clientId;
            loginRequest.Community = comunity;
            loginRequest.LocaleCode = localeCode;

            return (loginRequest);
         }   
   }
}
