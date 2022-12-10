package com.percussion.server.command;

import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSPasswordHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class PSConsoleCommandEncryptDBPasswords extends PSConsoleCommand
{



    /**
     * The constructor for this class.
     *
     * @param      cmdArgs      the argument string to use when executing
     *                           this command
     *
     */
    public PSConsoleCommandEncryptDBPasswords(String cmdArgs)
            throws PSIllegalArgumentException
    {
        super(cmdArgs);
    }

    /**
     * Execute the command specified by this object. The results are returned
     * as an XML document of the appropriate structure for the command.
     *   <P>
     * The execution of this command results in the following XML document
     * structure:
     * <PRE><CODE>
     *      &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, resultText)&gt;
     *
     *      &lt;--
     *         the command that was executed
     *      --&gt;
     *      &lt;ELEMENT command                     (#PCDATA)&gt;
     *
     *      &lt;--
     *         the result code for the command execution
     *      --&gt;
     *      &lt;ELEMENT resultCode                  (#PCDATA)&gt;
     *
     *      &lt;--
     *         the message text associated with the result code
     *      --&gt;
     *      &lt;ELEMENT resultText                  (#PCDATA)&gt;
     * </CODE></PRE>
     *
     * @param      request                     the requestor object
     *
     * @return                                 the result document
     *
     * @exception   PSConsoleCommandException   if an error occurs during
     *                                          execution
     */
    public Document execute(PSRequest request)
            throws PSConsoleCommandException
    {
        Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
        Element root = PSXmlDocumentBuilder.createRoot(
                respDoc, "PSXConsoleCommandResults");
        PSXmlDocumentBuilder.addElement(respDoc, root, "command", ms_cmdName + " " + m_cmdArgs);
        Locale loc;
        if (request != null)
            loc = request.getPreferredLocale();
        else
            loc = Locale.getDefault();

        try {

            encryptPassword();

            PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode", "0");
            PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", "Passwords Encrypted");
        } catch (Exception e) {
            String msg;
            if (e instanceof com.percussion.error.PSException)
                msg = ((PSException)e).getLocalizedMessage(loc);
            else
                msg = e.getMessage();

            Object[] args = { (ms_cmdName + " " + m_cmdArgs), msg };
            throw new PSConsoleCommandException(
                    IPSServerErrors.RCONSOLE_EXEC_EXCEPTION, args);
        }

        return respDoc;
    }

    private void encryptPassword() throws SQLException, NamingException, PSEncryptionException {
        Connection conn = PSConnectionHelper.getDbConnection();
         PreparedStatement stmt = conn.prepareStatement("Select USERID, PASSWORD from USERLOGIN");
         ResultSet results = stmt.executeQuery();
            while (results.next()) {
                String userId = results.getString("USERID");
                String password = results.getString("PASSWORD");
                if(!StringUtils.isEmpty(password)){
                    //If pwd less than 190 means not encrypted, else if encrypted, then length will be 193
                    if(password.length()<190) {
                        String newPwd = PSPasswordHandler.getHashedPassword(password);
                        // Prepare the corresponding query to update the row
                        String updateDataValueQuery = "UPDATE USERLOGIN SET PASSWORD = ? WHERE USERID = '" + userId + "'";
                        PreparedStatement ps2 = conn.prepareStatement(updateDataValueQuery);
                        ps2.setString(1, newPwd);     // METAKEY column
                        ps2.executeUpdate();
                    }
                }
            }

    }


    /**
     * allow package members to see our command name
     */
    final static String   ms_cmdName = "encrypt passwords";
}

