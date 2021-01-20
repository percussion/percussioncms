using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace RxTest
{
    /// <summary>
    /// This is a utility class, which is used to read the data files for the sample loader.
    /// </summary>
    class PSFileUtils
    {
        ///
        /// <summary>
        ///     Loads the loader properties from 'Loader.xml', which is located at the
        ///     current directory of the loader executable program. The content of the file 
        ///     is expected to comply with the following dtd:
        ///     <pre>
        ///     &lt;!ELEMENT Loader (ConnectionInfo, LoginRequest, ContentType, DataFile, TargetFolder)>
        ///     &lt;!ELEMENT ConnectionInfo (Protocol, Host, Port)>
        ///     &lt;!ELEMENT Protocol (#PCDATA)>
        ///     &lt;!ELEMENT Host (#PCDATA)>
        ///     &lt;!ELEMENT Port (#PCDATA)>
        ///     &lt;!ELEMENT LoginRequest (Username, Password, Community)>
        ///     &lt;!ELEMENT Username (#PCDATA)>
        ///     &lt;!ELEMENT Password (#PCDATA)>
        ///     &lt;!ELEMENT Community (#PCDATA)>
        ///     &lt;!ELEMENT ContentType (#PCDATA)>
        ///     &lt;!ELEMENT DataFile (#PCDATA)>
        ///     &lt;!ELEMENT TargetFolder (#PCDATA)>
        ///     </pre>
        /// </summary>
        /// 
        /// <returns>
        ///     the properties specified from <code>Loader.xml</code> file,
        ///     never <code>null</code> or empty.
        /// </returns>
        public static Dictionary<string, string> getLoaderProperties()
        {
            XmlReaderSettings settings = new XmlReaderSettings();
            settings.IgnoreWhitespace = true;
            XmlReader reader = XmlReader.Create("..\\..\\Loader.xml", settings);
            reader.ReadToDescendant("ConnectionInfo");

            Dictionary<string, string> props = new Dictionary<string, string>();

            // Read the connection information
            XmlReader connReader = reader.ReadSubtree();
            connReader.ReadToDescendant(PSTest.PROTOCOL);
            props.Add(PSTest.PROTOCOL, connReader.ReadElementString(PSTest.PROTOCOL));
            props.Add(PSTest.HOST, connReader.ReadElementString(PSTest.HOST));
            props.Add(PSTest.PORT, connReader.ReadElementString(PSTest.PORT));

            // Read the login credentail
            reader.ReadToNextSibling("LoginRequest");
            connReader = reader.ReadSubtree();
            connReader.ReadToDescendant(PSTest.USER_NAME);
            props.Add(PSTest.USER_NAME, connReader.ReadElementString(PSTest.USER_NAME));
            props.Add(PSTest.PASSWORD, connReader.ReadElementString(PSTest.PASSWORD));
            props.Add(PSTest.COMMUNITY, connReader.ReadElementString(PSTest.COMMUNITY));

            reader.ReadToNextSibling(PSTest.CONTENT_TYPE);
            props.Add(PSTest.CONTENT_TYPE, reader.ReadString());
            reader.ReadToNextSibling(PSTest.DATA_FILE);
            props.Add(PSTest.DATA_FILE, reader.ReadString());
            reader.ReadToNextSibling(PSTest.TARGET_FOLDER);
            props.Add(PSTest.TARGET_FOLDER, reader.ReadString());

            return props;
        }

        /// <summary>
        ///     Loads the Content Item data from the file specified by the path.
        ///     The XML file is expected conform to the following dtd:
        ///     <pre>
        ///     &lt;!ELEMENT Items (Item*)>
        ///     &lt;!ELEMENT Item (Field*)>
        ///     &lt;!ELEMENT Field (#PCDATA)>
        ///     &lt;!ATTLIST Field
        ///         name CDATA #REQUIRED
        ///         >
        ///     </pre>
        /// </summary>
        /// <param name="dataFile">
        ///     the path to the data file, defined relative to the current directory
        ///     of the loader executable program.
        /// </param>
        /// <returns>
        ///     a list of Content Item data. Each element in the returned data set
        ///     is a field name/value mapping.
        /// </returns>
        public static List<Dictionary<string, string>> loadDataFile(String dataFile)
        {
            List<Dictionary<string, string>> itemFields = new List<Dictionary<string, string>>();

            XmlReaderSettings settings = new XmlReaderSettings();
            settings.IgnoreWhitespace = true;
            XmlReader reader = XmlReader.Create(dataFile, settings);
            reader.ReadToDescendant("Item");
            do
            {
                XmlReader fieldReader = reader.ReadSubtree();
                Dictionary<string, string> fields = new Dictionary<string, string>();
                fieldReader.ReadToDescendant("Field");
                while (fieldReader.IsStartElement())
                {
                    fieldReader.MoveToFirstAttribute();
                    string name = fieldReader.Value;
                    fieldReader.MoveToElement();
                    fields.Add(name, fieldReader.ReadElementString());
                    fieldReader.MoveToElement();
                }

                itemFields.Add(fields);
            } 
            while (reader.ReadToNextSibling("Item"));

            return itemFields;
         }

        public static void RxAssert(Boolean ok, string message)
        {
            if (!ok)
            {
                throw new TestException(message);
            }
        }

        public static void RxAssert(Boolean ok)
        {
            if (!ok)
            {
                throw new TestException("No Message");
            }
        }

        public static void RxAssertTrue(Boolean ok, string message)
        {
            if (ok)
            {
                throw new TestException(message);
            }
        }

       public static void RxAssert(string message)
       {
          throw new TestException(message);
       }
    }

}
