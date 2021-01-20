using System;
using System.Reflection;
using System.Collections.Generic;
using System.Text;
using RxTest.RxWebServices;

namespace RxTest
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
    public class PSTest
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
            PSTest test = new PSTest();

            test.ReadInputData();
            test.Login();

            PSContentTestCase testContent = new PSContentTestCase(test);
            PSAssemblyTestCase testAssembly = new PSAssemblyTestCase(test);
            PSSecurityTestCase testSecurity = new PSSecurityTestCase(test);
            PSSystemTestCase testSystem = new PSSystemTestCase(test, testContent);
            PSUiTestCase testUi = new PSUiTestCase(test);
           
            Object[] testClasses =  { testContent, testAssembly, testSecurity, testSystem, testUi };

            ConsoleMessage("\n\nRunning all .Net Webservices tests...");

            int passed = 0;
            int errors = 0;
            for (int inc = 0; inc < testClasses.Length; ++inc)
            {

               Type testType = testClasses[inc].GetType();
               BindingFlags bindingFlags = BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Public;
               MethodInfo[] testMethods = testType.GetMethods(bindingFlags);

               ConsoleMessage(String.Format("Found {0} Mehods for {1}", testMethods.Length, testClasses[inc].ToString()));

               foreach (MethodInfo method in testMethods)
               {
                  if (String.Compare(method.Name, 0, "test", 0, 4, true) == 0)
                  {
                     try
                     {
                        ConsoleMessage(String.Format("\n\nRunning {0} ...\n", method.Name));
                        method.Invoke(testClasses[inc], null);
                        ++passed;
                        ConsoleMessage(String.Format("\n\n{0} Passed.\n", method.Name));
                     }
                     catch (Exception e)
                     {
                        ++errors;

                        Exception ex;

                        if (e.InnerException != null)
                        {
                           ex = e.InnerException;
                        }
                        else
                        {
                           ex = e;
                        }

                        ProcessException(ex);


                        ConsoleMessage(String.Format("\n{0} Failed due to a {1} Exception.\n", method.Name, ex.TargetSite.Name));
                     }
                  }
               }
            }

            ConsoleMessage(string.Format("\n\n{0} .Net Unit Tests Run.\n", passed + errors));
            ConsoleMessage(string.Format("\n\n{0} Passed.\n{1} Failed.\n\n", passed, errors));
      
            test.Logout();
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
        public void Login()
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

            m_assService = PSWsUtils.GetAssemblyService(m_secService.CookieContainer,
                m_secService.PSAuthenticationHeaderValue);

            m_uiService = PSWsUtils.GetUiService(m_secService.CookieContainer,
                m_secService.PSAuthenticationHeaderValue);
        }

        /// <summary>
        ///     Logs out Rhythmyx for the current Rhythmyx session.
        /// </summary>
        void Logout()
        {
            PSWsUtils.Logout(m_secService, m_rxSession);
        }

        /*
         * Processes exceptions by printing the message and stack trace
         */
        public static void ProcessException(Exception e)
        {
            ConsoleMessage("\n");
            ConsoleMessage(e.Message);
            ConsoleMessage(e.StackTrace);

            if (e.InnerException != null)
            {
               ProcessException(e.InnerException);
            }
         }


        ///
        /// <summary>
        ///     Writes the specified message to the console.
        /// </summary>
        /// <param name="msg">
        ///     the console message; assumed not <code>null</code>.
        /// </param>
        ///
        public static void ConsoleMessage(String msg)
        {
            Console.WriteLine(msg);
        }

        /**
         * The Rhythmyx session, initialized by login().
         */
        public string m_rxSession;

        /**
         * The security service instance; used to perform operations defined in
         * the security services. It is initialized by login().
         */
        public securitySOAP m_secService;

        /**
         * The content service instance; used to perform operations defined in
         * the content services. It is initialized by login().
         */
        public contentSOAP m_contService;

        /**
         * The system service instance; used to perform operations defined in
         * the system service. It is initialized by login().
         */
        public systemSOAP m_sysService;

        /**
         * The Assembly service instance; used to perform operations defined in
         * the Assembly service. It is initialized by login().
         */
        public assemblySOAP m_assService;

        /**
         * The Ui service instance; used to perform operations defined in
         * the Ui service. It is initialized by login().
         */
        public uiSOAP m_uiService;

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

        /** 
         * Comunity used for tests
         */
        public String CommunityName = "Enterprise_Investments";

       /*
        * New Copy Relationship test
        */
        public String NewCopyRelationshipType = "NewCopy";
    }

    public class TestException : ApplicationException
    {
        public TestException() : base()
        {
        }

        public TestException(string message) : base(message)
        {
        }
        public TestException(string message, Exception inner) : base(message, inner)
        {
        }
    }
 }
