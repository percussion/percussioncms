using System;
using System.Collections.Generic;
using System.Text;
using System.Web.Services.Protocols;
using System.Xml;
using Loader.RxWebServices;
using RxFaultFactory;

namespace Loader
{
    /// <summary>
    /// This class loads all Content Items defined in a specified data file
    /// (LoaderData.xml) into a specified Rhythmyx Folder. The location of the 
    /// data file and the target Folder are specified in the properties file of the 
    /// loader, Loader.xml.  If the specified target Folder does not exist, it will 
    /// be created. If the Folder contains any Content Items whose sys_title field 
    /// has the same value (case-insensitive) as the sys_title field of a Content 
    /// Item defined in the data file, the existing Content Item will be updated 
    /// with the data from the data file.  If the target Folder does not contain any
    /// Content Items whose sys_title field matches the sys_title field of a Content
    /// Item defined in the data file, a new Content Item will be created.
    /// 
    /// The properties file (Loader.xml) also specifies the Content Type used for the
    /// uploaded Content Items.  The same Content Type is used to upload all Content
    /// Items for a particular run of the loader.
    ///  
    /// The loader returns limited terminal window output while it processes each 
    /// Copntent Item.
    /// </summary>
    class PSLoader
    {
        /// <summary>
        ///     This method is the starting point of the loader program. The parameter
        ///     file, <code>Loader.xml</code>, is assumed to be in the current folder
        ///     of the loader executable program.
        /// </summary>
        /// <param name="args">
        ///     the arguments of the program, which are not used.
        /// </param>
        static void Main(string[] args)
        {
            PSLoader loader = new PSLoader();

            try
            {
                loader.ReadInputData();
                loader.Login();
                loader.CreateTargetFolder();
                loader.UploadItems();
                loader.UploadDataFile();
                loader.Logout();
            }
            catch (SoapException e)
            {
                IPSSoapFault fault = PSFaultFactory.GetFault(e);

                loader.ConsoleMessage("Caught Exception: " + fault.GetFaultName());
                loader.ConsoleMessage("Error Message: " + fault.GetMessage());

                if (fault.GetFaultName() == "PSErrorsFault" ||
                    fault.GetFaultName() == "PSErrorResultsFault")
                {
                    PSErrorsFault errorsFault = (PSErrorsFault)fault;
                    int errorCount = errorsFault.getErrorMessages().Count;
                    int successCount = errorsFault.getSuccessIds().Count;
                    loader.ConsoleMessage("There are " + errorCount + " error messages.");
                    loader.ConsoleMessage("There are " + successCount + " successful ids.");
                }
            }
        }

        /// <summary>
        ///     Updates Content Items read from LoaderData.xml into the target Folder.
        ///     Assumes the target folder already exists in Rhythmyx.
        /// </summary>
        void UploadItems()
        {
            // get the items in the target folder
            PSItemSummary[] curItems = PSWsUtils.FindFolderChildren(m_contService,
                m_props[TARGET_FOLDER]);
      
            foreach (Dictionary<string, string> itemFields in m_itemData)
            {
                PSItemSummary summary = GetItem(curItems, itemFields["sys_title"]);
         
                if (summary != null) // update the existing item
                {
                    UpdateItem(summary.id, itemFields);
                }
                else
                {
                    CreateItem(itemFields);
                }
            }
      
            ConsoleMessage("Finished uploading items into target Folder, " 
                + m_props[TARGET_FOLDER]);
        }

        /// <summary>
        ///     Creates a Content Item of Content Type specified in the properties 
        ///     file (Loader.xml) using the field values defined for the Content Item in 
        ///     the data file (LoaderData.xml).  The created Content Item is added to the
        ///     target Folder specified in the properties file.
        /// </summary>
        /// <param name="fields">
        ///     the set of field values for the created Content Item; assumed not 
        ///     to be <code>null</code> or empty.
        /// </param>
        private void CreateItem(Dictionary<string, string> fields)
        {
            PSItem item = PSWsUtils.CreateItem(m_contService, m_props[CONTENT_TYPE]);
      
            SetItemFields(item, fields);
      
            long id = PSWsUtils.SaveItem(m_contService, item);
            PSWsUtils.CheckinItem(m_contService, id);
            PSWsUtils.TransitionItem(m_sysService, id, "DirecttoPublic");
      
            // Attach the Content Item to the Target folder
            String path = m_props[TARGET_FOLDER];
            PSWsUtils.AddFolderChildren(m_contService, path, new long[]{id});
              
            ConsoleMessage("Created item: " + fields["sys_title"]);
        }

        /// <summary>
        ///     Updates the specified Content Item with the specified field values.
        /// </summary>
        /// <param name="id">
        ///     the ID of the Content Item to update.
        /// </param>
        /// <param name="fields">
        ///     the new values of the specified fields.
        /// </param>
        private void UpdateItem(long id, Dictionary<string, string> fields)
        {
            PSItemStatus status = PSWsUtils.PrepareForEdit(m_contService, id);
      
            PSItem item = PSWsUtils.LoadItem(m_contService, id);
            SetItemFields(item, fields);
            PSWsUtils.SaveItem(m_contService, item);
      
            PSWsUtils.ReleaseFromEdit(m_contService, status);
      
            ConsoleMessage("Updated item: " + fields["sys_title"]);
        }


        /// <summary>
        ///     Sets the fields of the specified Content Item to the specified values.
        /// </summary>
        /// <param name="item">
        ///     the Content Items whose field values are to be updated.
        /// </param>
        /// <param name="fields">
        ///     the set of field values to be updated to the specified 
        ///     Content Item; assumed not to be <code>null</code> or empty.
        /// </param>
        private void SetItemFields(PSItem item, Dictionary<string, string> fields)
        {
           foreach (PSField srcField in item.Fields)
           {
               string nameValue;
               if (fields.TryGetValue(srcField.name, out nameValue))
              {
                 PSFieldValue value = new PSFieldValue();
                 value.RawData = nameValue;
                 srcField.PSFieldValue = new PSFieldValue[] { value };
              }
           }
        }

        /// <summary>
        ///     Determines whether the set of supplied Content Item summaries contains 
        ///     an item with the specified value in the sys_title field.
        /// </summary>
        /// <param name="curItems">
        ///     the set of Content Item summaries to evaluate; assumed 
        ///     not to be  <code>null</code>
        /// </param>
        /// <param name="sysTitle">
        ///     the value of sys_title for which to evaluate; assumed 
        ///     not to be <code>null</code> or empty.
        /// </param>
        /// <returns>
        ///     <code>true</code> if the set of Content Item summaries contains
        ///     a Content Item with the specified value in the sys_title field; 
        ///     <code>false</code> otherwise.
        /// </returns>
        PSItemSummary GetItem(PSItemSummary[] curItems, String sysTitle)
        {
            foreach (PSItemSummary item in curItems)
            {
                if (item.name.ToLower() == sysTitle.ToLower())
                return item;
            }
            return null;

        }

        /// <summary>
        /// Read the input data from the properties file (Loader.xml) and the data 
        /// file (DataFile.xml).
        /// </summary>
        void ReadInputData()
        {
            ConsoleMessage("Read loader parameters...");
            m_props = PSFileUtils.getLoaderProperties();

            ConsoleMessage("Read loader data file, " + m_props[DATA_FILE]
                + " ...");
            m_itemData = PSFileUtils.loadDataFile(m_props[DATA_FILE]);
        }

        /// <summary>
        /// Logs in to the Rhythmyx server using the server connection and
        /// authentication data specified in the loader properties file (Loader.xml)
        /// </summary>
        void Login()
        {
            PSWsUtils.SetConnectionInfo(m_props[PROTOCOL], m_props[HOST], 
                Int16.Parse(m_props[PORT]));

            m_secService = PSWsUtils.GetSecurityService();
            m_rxSession = PSWsUtils.Login(m_secService, m_props[USER_NAME],
                  m_props[PASSWORD], m_props[COMMUNITY], null);

            m_contService = PSWsUtils.GetContentService(m_secService.CookieContainer, 
                m_secService.PSAuthenticationHeaderValue);
            m_sysService = PSWsUtils.GetSystemService(m_secService.CookieContainer, 
                m_secService.PSAuthenticationHeaderValue);
        }

        /// <summary>
        ///     Logs out Rhythmyx for the current Rhythmyx session.
        /// </summary>
        void Logout()
        {
            PSWsUtils.Logout(m_secService, m_rxSession);
        }

        /// <summary>
        ///     Creates any target Folder specified in the properties file (Loader.xml) 
        ///     that does not already exist.
        /// </summary>
        void CreateTargetFolder()
        {
           PSFolder[] folders = PSWsUtils.AddFolderTree(m_contService, 
               m_props[TARGET_FOLDER]);
           
           ConsoleMessage("AddFolderTree operation returned " + folders.Length
                 + " folders");
        }

        ///
        /// <summary>
        ///     Writes the specified message to the console.
        /// </summary>
        /// <param name="msg">
        ///     the console message; assumed not <code>null</code>. 
        /// </param>
        ///
        void ConsoleMessage(String msg)
        {
            Console.WriteLine(msg);
        }

        /// <summary>
        /// Uploads the data file into the target Folder. The data file name
        /// (LoaderData.xml) is specified in the loader properties file (Loader.xml). 
        /// Assumes the target folder already exists in Rhythmyx.
        /// </summary>
        void UploadDataFile()
        {
           string filePath = m_props[DATA_FILE];
           string fileName = getFilename(filePath);

           string targetFolder = m_props[TARGET_FOLDER];
           PSItemSummary[] curItems = PSWsUtils.FindFolderChildren(m_contService,
                 targetFolder);

           PSItemSummary summary = GetItem(curItems, fileName);
          
           if (summary != null) // update the existing item of File Content Type
           {
              UpdateFileItem(summary.id, filePath);
           }
           else
           {
              CreateFileItem(filePath, targetFolder);
           }

           ConsoleMessage("Finished uploading the Data File (" + fileName
              + ") into target Folder, " + targetFolder);
        }

        /// <summary>
        ///     Gets the file name from the specified file path.
        /// </summary>
        /// <param name="filePath">
        ///     the specified file path.
        /// </param>
        /// <returns>
        ///     the file name of the specified file path.
        /// </returns>
        private string getFilename(string filePath)
        {
            int index = filePath.LastIndexOf('\\');
            if (index != -1)
                return filePath.Substring(index + 1);
            else
                return filePath;
        }

        /// <summary>
        ///     Creates a Content Item of File Content Type for the specified file.
        ///     The created Content Item is added to the target Folder.
        /// </summary>
        /// <param name="filePath">
        ///     the file path of the specified file; assumed not to be 
        ///     <code>null</code> or empty.
        /// </param>
        /// <param name="targetFolder">
        ///     the virtual path of the target folder in Rhythmyx; assumed not 
        ///     to be <code>null</code> or empty.
        /// </param>
        void CreateFileItem(string filePath, string targetFolder)
        {
           PSItem item = PSWsUtils.CreateItem(m_contService, "rffFile");

           SetFileItemFields(item, filePath);
              
           long id = PSWsUtils.SaveItem(m_contService, item);
           PSWsUtils.CheckinItem(m_contService, id);
           PSWsUtils.TransitionItem(m_sysService, id, "DirecttoPublic");
             
           // Attach the Content Item to the Target folder
           PSWsUtils.AddFolderChildren(m_contService, targetFolder, new long[] { id });
        }

        /// <summary>
        ///     Updates a Content Item of File Content Type for the specified file.
        /// </summary>
        /// <param name="id">
        ///     the id of the specified Content Item.
        /// </param>
        /// <param name="filePath">
        ///     the file path of the specified file; assumed not to be 
        ///     <code>null</code> or empty.
        /// </param>
        void UpdateFileItem(long id, string filePath) 
        {
           PSItemStatus status = PSWsUtils.PrepareForEdit(m_contService, id);
          
           PSItem item = PSWsUtils.LoadItem(m_contService, id);
          
           // read the orignal binary data before updating
           byte[] content = RetrieveBinaryData(item);
           string text = PSFileUtils.convertBytesToString(content);
           ConsoleMessage("\nThe original content data: \n" + text);
          
           // set the new data
           SetFileItemFields(item, filePath);
           PSWsUtils.SaveItem(m_contService, item);
          
           PSWsUtils.ReleaseFromEdit(m_contService, status);
        }

        /// <summary>
        ///     Retrieves the binary value from the specified Content Item.
        /// </summary>
        /// <param name="item">
        ///     the Content Item from which to retrieve the binary value;
	    ///     assumed it is a Content Item of File Content Type.
        /// </param>
        /// 
        /// <returns>
        ///     the binary value, never <code>null</code>.
        /// </returns>
        byte[] RetrieveBinaryData(PSItem item)
        {
           // Get the value of the binary field, "item"
           PSFieldValue value = null;
           foreach (PSField field in item.Fields)
           {
               if (field.name == "item_file_attachment")
              {
                 PSFieldValue[] values = field.PSFieldValue;
                 value = values[0];
                 break;
              }
           }        

           // Assuming found the "item" field and it is not empty.
           // Retrieves the binary data from the "item" field.
           byte[] binaryData = Convert.FromBase64String(value.RawData);
           return binaryData;
        }

        /// <summary>
        ///     Sets the specified file to the specified File Content Item.
        /// </summary>
        /// <param name="item">
        ///     the File Content Item; assumed not <code>null</code>.
        /// </param>
        /// <param name="filePath">
        ///     the file name of the specified file; assumed not 
        ///     <code>null</code> or empty.
        /// </param>
        void SetFileItemFields(PSItem item, string filePath)
        {
           string fileName = getFilename(filePath);

           foreach (PSField field in item.Fields)
           {
               String name = field.name;
              PSFieldValue newValue = new PSFieldValue();
              if (name == "sys_title" || name == "displaytitle")
              {
                  newValue.RawData = fileName;
                 field.PSFieldValue = new PSFieldValue[] { newValue };
              }
              else if (name == "item_file_attachment")
              {
                 byte[] binaryData = PSFileUtils.ReadBinaryFile(filePath);
                 newValue.RawData = Convert.ToBase64String(binaryData);
                 field.PSFieldValue = new PSFieldValue[] { newValue };
              }
              else if (name == "item_file_attachment_type")
              {
                 newValue.RawData = "text/xml";
                 field.PSFieldValue = new PSFieldValue[] {newValue};
              }
              else if (name == "item_file_attachment_ext")
              {
                 newValue.RawData = ".xml";
                 field.PSFieldValue = new PSFieldValue[] { newValue };
              }
              else if (name == "item_file_attachment_filename")
              {
                  newValue.RawData = fileName;
                  field.PSFieldValue = new PSFieldValue[] { newValue };
              }
              else if (name == "item_file_attachment_size")
              {
                  long size = PSFileUtils.GetFileSize(filePath);
                  newValue.RawData = Convert.ToString(size);
                  field.PSFieldValue = new PSFieldValue[] { newValue };
              }
              else if (name == "sys_workflowid")
              {
                 newValue.RawData = "5";
                 field.PSFieldValue = new PSFieldValue[] { newValue };
              }
           }
        }
   

        /**
         * The Rhythmyx session, initialized by login()}. 
         */
        string m_rxSession;

        /**
         * The security service instance; used to perform operations defined in
         * the security services. It is initialized by login().
         */
        securitySOAP m_secService;

        /**
         * The content service instance; used to perform operations defined in
         * the content services. It is initialized by login().
         */
        contentSOAP m_contService;

        /**
         * The system service instance; used to perform operations defined in
         * the system service. It is initialized by login().
         */
        systemSOAP m_sysService;


        /**
         * The loader properties, read from the file 'Loader.xml'.
         */
        private Dictionary<string, string> m_props;

        /**
         * The Content Item data to be uploaded; read from the file 'DataFile.xml' 
         */
        private List<Dictionary<string, string>> m_itemData;


        /**
         * The property name of the protocol of the server connection.
         */
        public static String PROTOCOL = "Protocol";

        /**
         * The property name of the host of the server connection.
         */
        public static String HOST = "Host";

        /**
         * The property name of the port of the server connection.
         */
        public static String PORT = "Port";

        /**
         * The property name of the name of the login user.
         */
        public static String USER_NAME = "Username";

        /**
         * The property name of the password of the login user.
         */
        public static String PASSWORD = "Password";

        /**
         * The property name of the name of the login Community.
         */
        public static String COMMUNITY = "Community";

        /**
         * The property name of the name of the Content Type of the Content Items to
         * be uploaded. 
         */
        public static String CONTENT_TYPE = "ContentType";

        /**
         * The property name of the target Folder path in Rhythmyx.
         */

        public static String TARGET_FOLDER = "TargetFolder";
        /**
         * The property name of the name of the data file.
         */
        public static String DATA_FILE = "DataFile";
    }
}
