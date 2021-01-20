using System;
using System.Xml;
using System.Web.Services.Protocols;
using System.Collections.Generic;

namespace RxFaultFactory
{
    /// <summary>
    /// This class is used to extract error message from a 
    /// <code>SoapException</code> instance.
    /// <example>
    /// try
    /// {
    ///     ...make some webservice call...
    /// }
    /// catch (SoapException e)
    /// {
    ///     IPSSoapFault fault = PSFaultFactory.GetFault(e);
    /// 
    ///     Console.WriteLine("Fault name: " + fault.GetFaultName());
    ///     Console.WriteLine("Error message: " + fault.GetMessage());
    /// }
    /// </example>
    /// </summary>
    public class PSFaultFactory
    {
        /// <summary>
        /// Creates an <code>IPSSoapFault</code> instance from the specified
        /// Soap Exception.
        /// </summary>
        /// <param name="e">
        ///     the Soap Exception; assumed not <code>null</code>.
        /// </param>
        /// <returns>
        ///     the created fault object; never <code>null</code>.
        /// </returns>
        public static IPSSoapFault GetFault(SoapException e)
        {
            // try to handle generic errors
            if (e.Message.Length > 0)
            {
                PSErrorFault fault = new PSErrorFault(e.Code.Name, e.Message, null);
                return fault;
            }

            // try to handle known errors
            else
            {
                XmlNode node = e.Detail;
                XmlNodeReader reader = new XmlNodeReader(node);
                reader.MoveToContent();

                reader.Read();
                if (reader.LocalName == "PSErrorResultsFault")
                {
                    return GetErrorResultsFault(reader);
                }
                else if (reader.LocalName == "PSErrorsFault")
                {
                    return GetErrorsFault(reader);
                }
                else
                {
                    return GetErrorFault(reader);
                }
            }
        }

        /// <summary>
        /// Creates <code>PSErrorFault</code> instance from the 
        /// specified XML Reader. The XML is expected to conform with 
        /// the following DTD:
        /// <pre>
        ///     &lt;!ELEMENT $ErrorName EMPTY>
        ///     &lt;!ATTLIST $ErrorName
        ///         code          CDATA #REQUIRED
        ///         errorMessage  CDATA #REQUIRED
        ///         stack         CDATA #REQUIRED
        ///     >
        ///     
        ///     where, $ErrorName is the element name, which can be
        ///         PSContractViolation, PSNotAuthenticatedFault, 
        ///         PSInvalidSessionFault, PSNotAuthorizedFault, ...etc
        /// </pre>
        /// </summary>
        /// <param name="reader">
        ///     The reader contains the XML representation of a 
        ///     <code>PSErrorFault</code> instance; assumed not <code>null</code>.
        /// </param>
        /// <returns>
        ///     The created fault instance.
        /// </returns>
        private static PSErrorFault GetErrorFault(XmlNodeReader reader)
        {
            string name = reader.LocalName;
            string message, stack;
            ReadMessageAndStack(reader, out message, out stack);
            PSErrorFault fault = new PSErrorFault(name, message, stack);
            return fault;
        }

        /// <summary>
        /// Creates <code>PSErrorResultsFault</code> instance from the 
        /// specified XML Reader. 
        /// The XML is expected to conform with the following DTD:
        /// <pre>
        ///     &lt;!ELEMENT PSErrorResultsFault (Service, ServiceCall+)>
        ///     &lt;!ELEMENT Service (#PCDATA)>
        ///     &lt;!ELEMENT ServiceCall (Result? Error?)>
        ///     &lt;!ELEMENT Result (Id, ANY)>
        ///     &lt;!ELEMENT Id (#PCDATA)>
        ///     &lt;!ELEMENT Error (Id, PSError?, PSLockFault?>
        ///     &lt;!ELEMENT PSError EMPTY>
        ///     &lt;!ATTLIST PSError
        ///         code          CDATA #REQUIRED
        ///         errorMessage  CDATA #REQUIRED
        ///         stack         CDATA #REQUIRED
        ///     >
        ///     &lt;!ELEMENT PSLockFault EMPTY>
        ///     &lt;!ATTLIST PSLockFault
        ///         code          CDATA #REQUIRED
        ///         errorMessage  CDATA #REQUIRED
        ///         stack         CDATA #REQUIRED
        ///         locker        CDATA #REQUIRED
        ///         remainingTime CDATA #REQUIRED
        ///     >
        /// </pre>
        /// </summary>
        /// <param name="reader">
        ///     The reader contains the XML representation of a 
        ///     <code>PSErrorResultsFault</code> instance; assumed not <code>null</code>.
        /// </param>
        /// <returns>
        ///     The created <code>PSErrorResultsFault</code> instance, which 
        ///     contains all error message from the <code>Error</code> elements.
        /// </returns>
        private static PSErrorsFault GetErrorResultsFault(XmlNodeReader reader)
        {
            PSErrorsFault fault = new PSErrorsFault(reader.LocalName);
            reader.ReadToDescendant("Service", FAULT_NAMESPACE);
            //string service = reader.ReadElementString();

            while (reader.ReadToNextSibling("ServiceCall", FAULT_NAMESPACE))
            {
                XmlReader svcReader = reader.ReadSubtree();
                svcReader.MoveToContent();
                svcReader.Read();
                if (svcReader.LocalName == "Error")
                {
                    svcReader.ReadToDescendant("Id", FAULT_NAMESPACE);
                    long id = svcReader.ReadElementContentAsLong();
                    if ((svcReader.LocalName == "PSError") || 
                        (svcReader.LocalName == "PSLockFault"))
                    {
                        string message, stack;
                        ReadMessageAndStack(svcReader, out message, out stack);
                        fault.AddError(id, message, stack);
                        // pass the current element
                        svcReader.MoveToElement();
                        svcReader.ReadOuterXml();
                        svcReader.ReadEndElement();
                    }
                }
                else if (svcReader.LocalName == "Result")
                {
                    svcReader.ReadToDescendant("Id", FAULT_NAMESPACE);
                    long id = svcReader.ReadElementContentAsLong();
                    fault.addSuccessId(id);

                    // pass the current element if needed
                    svcReader.ReadOuterXml();
                    svcReader.ReadEndElement();
                }
            }

            return fault;
        }

        /// <summary>
        /// Creates <code>PSErrorsFault</code> instance from the 
        /// specified XML Reader. 
        /// The XML is expected to conform with the following DTD:
        /// <pre>
        ///     &lt;!ELEMENT PSErrorsFault (Service, ServiceCall+)>
        ///     &lt;!ELEMENT Service (#PCDATA)>
        ///     &lt;!ELEMENT ServiceCall (Success? Error?)>
        ///     &lt;!ELEMENT Success EMPTY>
        ///     &lt;!ATTLIST Success
        ///         id          CDATA #REQUIRED
        ///     >
        ///     &lt;!ELEMENT Error (PSError?, PSLockFault?>
        ///     &lt;!ATTLIST Error
        ///         id          CDATA #REQUIRED
        ///     >
        ///     &lt;!ELEMENT PSError EMPTY>
        ///     &lt;!ATTLIST PSError
        ///         code          CDATA #REQUIRED
        ///         errorMessage  CDATA #REQUIRED
        ///         stack         CDATA #REQUIRED
        ///     >
        ///     &lt;!ELEMENT PSLockFault EMPTY>
        ///     &lt;!ATTLIST PSLockFault
        ///         code          CDATA #REQUIRED
        ///         errorMessage  CDATA #REQUIRED
        ///         stack         CDATA #REQUIRED
        ///         locker        CDATA #REQUIRED
        ///         remainingTime CDATA #REQUIRED
        ///     >
        /// </pre>
        /// </summary>
        /// <param name="reader">
        ///     The reader contains the XML representation of a 
        ///     <code>PSErrorResultsFault</code> instance; assumed not <code>null</code>.
        /// </param>
        /// <returns>
        ///     The created <code>PSErrorResultsFault</code> instance, which 
        ///     contains all error message from the <code>Error</code> elements.
        /// </returns>
        private static PSErrorsFault GetErrorsFault(XmlNodeReader reader)
        {
            PSErrorsFault fault = new PSErrorsFault(reader.LocalName);
            reader.ReadToDescendant("Service", FAULT_NAMESPACE);

            while (reader.ReadToNextSibling("ServiceCall", FAULT_NAMESPACE))
            {
                XmlReader svcReader = reader.ReadSubtree();
                svcReader.MoveToContent();
                svcReader.Read();
                if (svcReader.LocalName == "Error")
                {
                    svcReader.MoveToFirstAttribute();
                    if (svcReader.Name == "id")
                    {
                        long id = Convert.ToInt64(svcReader.Value);

                        svcReader.MoveToElement(); // move back to "Error" element
                        svcReader.MoveToContent(); // move to child element
                        svcReader.Read();
                        if ((svcReader.LocalName == "PSError") ||
                            (svcReader.LocalName == "PSLockFault"))
                        {
                            string message, stack;
                            ReadMessageAndStack(svcReader, out message, out stack);

                            fault.AddError(id, message, stack);

                            // pass the current element
                            svcReader.MoveToElement();
                            svcReader.ReadOuterXml();
                            svcReader.ReadEndElement();
                        }
                    }
                }
                else if (svcReader.LocalName == "Success")
                {
                    svcReader.MoveToFirstAttribute();
                    if (svcReader.Name == "id")
                    {
                        long id = Convert.ToInt64(svcReader.Value);
                        fault.addSuccessId(id);

                        // pass the current element
                        svcReader.MoveToElement();
                        svcReader.Read();
                    }
                }
            }

            return fault;
        }

        /// <summary>
        /// The XML namespace used in the Soap Fault node.
        /// </summary>
        private static string FAULT_NAMESPACE = "urn:www.percussion.com/6.0.0/faults";

        /// <summary>
        /// Reads the values for <code>errorMessage</code> and 
        /// <code>stack</code> attributes from the current element node.
        /// </summary>
        /// <param name="reader">
        ///     The reader contains current node; assumed not <code>null</code>.
        /// </param>
        /// <param name="message">
        ///     The returned value for <code>errorMessage</code> attribute.
        /// </param>
        /// <param name="stack">
        ///     The returned value for <code>stack</code> attribute.
        /// </param>
        private static void ReadMessageAndStack(XmlReader reader, 
            out string message, out string stack)
        {
            message = "";
            stack = "";
            bool hasAttr = reader.MoveToFirstAttribute();
            while (hasAttr)
            {
                if (reader.Name == "errorMessage")
                {
                    message = reader.Value;
                }
                else if (reader.Name == "stack")
                {
                    stack = reader.Value;
                }

                hasAttr = reader.MoveToNextAttribute();
            }
        }
    }

    /// <summary>
    /// The interface must be implemented by the classes created by the Fault Factory.
    /// </summary>
    public interface IPSSoapFault
    {
        /// <summary>
        /// Gets the error message from this instance. If this instance contains
        /// more than one error messages, than get the 1st one.
        /// </summary>
        /// <returns>
        /// the error message described above; may be <code>null</code> or empty.
        /// </returns>
        string GetMessage();

        /// <summary>
        /// Gets the fault name, which may be the user defined exception name
        /// from the server.
        /// </summary>
        /// <returns>
        /// the fault name; may be <code>null</code> or empty.
        /// </returns>
        string GetFaultName();
    }

    /// <summary>
    /// This class contains a fault name, an error message and a stack trace.
    /// </summary>
    public class PSErrorFault : IPSSoapFault
    {
        /// <summary>
        /// Constructs an instance from the specified parameters.
        /// </summary>
        /// <param name="name">
        ///     the fault name; may be <code>null</code> or empty if 
        ///     not defined.
        /// </param>
        /// <param name="message">
        ///     the error message; may be <code>null</code> or empty if 
        ///     not defined.
        /// </param>
        /// <param name="stack">
        ///     the stack trace; may be <code>null</code> or empty if 
        ///     not defined. 
        /// </param>
        public PSErrorFault(string name, string message, string stack)
        {
            m_name = name;
            m_message = message;
            m_stack = stack;
        }

        /// <summary>
        /// Implements <code>IPSSoapFault.GetMessage()</code>
        /// </summary>
        public string GetMessage()
        {
            return m_message;
        }

        /// <summary>
        /// Implements <code>IPSSoapFault.GetFaultName()</code>
        /// </summary>
        public string GetFaultName()
        {
            return m_name;
        }

        /// <summary>
        /// Gets the stack trace.
        /// </summary>
        /// <returns>
        /// the stack trace; may be <code>null</code> or empty if not defined.
        /// </returns>
        public string GetStack()
        {
            return m_stack;
        }

        /// <summary>
        /// the name of the instance. Initialized by the constructor, 
        /// may be <code>null</code> or empty if not defined.
        /// </summary>
        private string m_name;

        /// <summary>
        /// the error message of the instance. Initialized by the constructor,
        /// may be <code>null</code> or empty if not defined.
        /// </summary>
        private string m_message;

        /// <summary>
        /// the stack trace of the instance. Initialized by the constructor,
        /// may be <code>null</code> or empty if not defined.
        /// </summary>
        private string m_stack;
    }

    /// <summary>
    /// This class contains one or more than one error messages. 
    /// It also contains a list of object ids for which the server 
    /// has been successfully applied the requested operation.
    /// </summary>
    public class PSErrorsFault : IPSSoapFault
    {
        /// <summary>
        /// Constructs an instance with the specified fault name.
        /// </summary>
        /// <param name="name">
        /// the fault name; assumed not <code>null</code>.
        /// </param>
        public PSErrorsFault(string name)
        {
            m_name = name;
        }

        /// <summary>
        /// Implements <code>IPSSoapFault.GetFaultName()</code>
        /// </summary>
        public string GetFaultName()
        {
            return m_name;
        }

        /// <summary>
        /// Implements <code>IPSSoapFault.GetMessage()</code>
        /// </summary>
        /// <returns>
        /// the 1st error message if there are more than one in this instance.
        /// It may be <code>null</code> if there is no error message.
        /// </returns>
        public string GetMessage()
        {
            if (m_messageMap.Count > 0)
            {
                string[] msg = new string[m_messageMap.Count];
                m_messageMap.Values.CopyTo(msg, 0);
                return msg[0];
            }
            else
            {
                return null;
            }
        }

        /// <summary>
        /// Gets the error messages along with its related object id.
        /// </summary>
        /// <returns>
        ///     the id and error message pairs; never <code>null</code>, but
        ///     may be empty.
        /// </returns>
        public Dictionary<long, string> getErrorMessages()
        {
            return m_messageMap;
        }

        /// <summary>
        /// Gets a list of object ids for which the server has successfully
        /// applied the requested operation.
        /// </summary>
        /// <returns>
        ///     a list of object ids; never <code>null</code>, but may be empty.
        /// </returns>
        public List<long> getSuccessIds()
        {
            return m_sucessIds;
        }

        /// <summary>
        /// Adds the specified id to the success id list.
        /// </summary>
        /// <param name="id">
        ///     the object id for which the server has successfully
        ///     applied the requested operation.
        /// </param>
        public void addSuccessId(long id)
        {
            m_sucessIds.Add(id);
        }

        /// <summary>
        /// Adds an error message and a stack trace for the specified (object) id.
        /// </summary>
        /// <param name="id">
        ///     the object id.
        /// </param>
        /// <param name="message">
        ///     the error message for the specified object.
        /// </param>
        /// <param name="stack">
        ///     the stack trace when the error occurs.
        /// </param>
        public void AddError(long id, string message, string stack)
        {
            m_messageMap.Add(id, message);
            m_stackMap.Add(id, stack);
        }

        /// <summary>
        /// The fault name; initialized by the constructor, never <code>null</code> after that.
        /// </summary>
        private string m_name;

        /// <summary>
        /// It maps object id to its error message; may be empty, but never <code>null</code>.
        /// </summary>
        private Dictionary<long, string> m_messageMap = new Dictionary<long, string>();

        /// <summary>
        /// It maps object id to its stack trace when an error occurs.
        /// </summary>
        private Dictionary<long, string> m_stackMap = new Dictionary<long, string>();

        /// <summary>
        /// A list of object ids for which the server has been successfully 
        /// applied the requested operation.
        /// </summary>
        private List<long> m_sucessIds = new List<long>();
    }
}