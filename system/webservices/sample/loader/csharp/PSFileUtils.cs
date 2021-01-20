using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace Loader
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
            connReader.ReadToDescendant(PSLoader.PROTOCOL);
            props.Add(PSLoader.PROTOCOL, connReader.ReadElementString(PSLoader.PROTOCOL));
            props.Add(PSLoader.HOST, connReader.ReadElementString(PSLoader.HOST));
            props.Add(PSLoader.PORT, connReader.ReadElementString(PSLoader.PORT));

            // Read the login credentail
            reader.ReadToNextSibling("LoginRequest");
            connReader = reader.ReadSubtree();
            connReader.ReadToDescendant(PSLoader.USER_NAME);
            props.Add(PSLoader.USER_NAME, connReader.ReadElementString(PSLoader.USER_NAME));
            props.Add(PSLoader.PASSWORD, connReader.ReadElementString(PSLoader.PASSWORD));
            props.Add(PSLoader.COMMUNITY, connReader.ReadElementString(PSLoader.COMMUNITY));

            reader.ReadToNextSibling(PSLoader.CONTENT_TYPE);
            props.Add(PSLoader.CONTENT_TYPE, reader.ReadString());
            reader.ReadToNextSibling(PSLoader.DATA_FILE);
            props.Add(PSLoader.DATA_FILE, reader.ReadString());
            reader.ReadToNextSibling(PSLoader.TARGET_FOLDER);
            props.Add(PSLoader.TARGET_FOLDER, reader.ReadString());

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

        /// <summary>
        ///     Reads binary data from the specified file
        /// </summary>
        /// <param name="filePath">
        ///     the path of the file from which to read binary data from; 
        ///     assumed not <code>null</code> or empty.
        /// </param>
        /// <returns>
        ///     the content of the specified file.
        /// </returns>
        public static byte[] ReadBinaryFile(string filePath)
        {
            System.IO.FileStream inFile;
            byte[] binaryData;

            inFile = new System.IO.FileStream(filePath,
                                System.IO.FileMode.Open,
                                System.IO.FileAccess.Read);
            binaryData = new byte[inFile.Length];
            long bytesRead = inFile.Read(binaryData, 0,
                                (int)inFile.Length);
            inFile.Close();

            return binaryData;
        }

        /// <summary>
        ///     Gets the size of the specified file.
        /// </summary>
        /// <param name="filePath">
        ///     the path of the file from which to read binary data from; 
        ///     assumed not <code>null</code> or empty.
        /// </param>
        /// <returns>
        ///     the size of the specified file.
        /// </returns>
        public static long GetFileSize(string filePath)
        {
            System.IO.FileStream inFile;
            long fileSize;

            inFile = new System.IO.FileStream(filePath,
                                System.IO.FileMode.Open,
                                System.IO.FileAccess.Read);
            fileSize = inFile.Length;
            inFile.Close();

            return fileSize;
        }

        /// <summary>
        ///     Converts the specified byte array to a string.
        /// </summary>
        /// <param name="binaryData">
        ///     the byte array from which to convert to string; assumed to be
        ///     not <code>null</code>.
        /// </param>
        /// <returns>
        ///     the converted string.
        /// </returns>
        public static String convertBytesToString(byte[] binaryData)
        {
            char[] chContent = new char[binaryData.Length];
            for (int i = 0; i < binaryData.Length; i++)
            {
                chContent[i] = Convert.ToChar(binaryData[i]);
            }
            String text = new String(chContent);
            return text;
        }
    }
}
