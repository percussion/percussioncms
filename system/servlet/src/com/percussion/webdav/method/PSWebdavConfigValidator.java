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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.webdav.method;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldValidationRules;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSIteratorUtils;
import com.percussion.webdav.IPSWebdavConstants;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.IPSRxWebDavDTD;
import com.percussion.webdav.objectstore.PSPropertyFieldNameMapping;
import com.percussion.webdav.objectstore.PSWebdavConfigDef;
import com.percussion.webdav.objectstore.PSWebdavContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class validates a WebDav configuration file, returning a report of all
 * errors. It is used in the context of the webdav servlet.
 */
public class PSWebdavConfigValidator
{

   private static final Logger log = LogManager.getLogger(PSWebdavConfigValidator.class);

   /**
    * List of String exception msgs generated during the 
    * validation routines
    */
   List<String> m_exceptionsList = new ArrayList<>();

   /**
    * List of String warning msgs generated during the 
    * validation routines
    */
   List<String> m_warningsList = new ArrayList<>();

   /**
    * Constructs a new WebDav configuration validator
    * 
    * @param req
    *           the Servlet request, cannot be <code>null</code>.
    * 
    * @param resp
    *           the Servlet response, cannot be <code>null</code>.
    * 
    * @param requester
    *           The remote requester, which is used to communicate with the
    *           remote Rhythmyx Server. Init by ctor, never <code>null</code>.
    * 
    * @throws IOException
    *            if an I/O error occurs
    */
   public PSWebdavConfigValidator(HttpServletRequest req,
         HttpServletResponse resp, IPSRemoteRequester requester)
         throws IOException
   {
      if (req == null)
         throw new IllegalArgumentException("Servlet request cannot be null.");
      if (resp == null)
         throw new IllegalArgumentException("Servlet response cannot be null.");
      if (requester == null)
         throw new IllegalArgumentException("requester cannot be null.");
      m_req = req;
      m_resp = resp;
      m_requester = requester;
      m_out = resp.getWriter();

   }

  

   /**
    * Ensure that the Token values supplied in the config file
    * are comma separated chars.
    * 
    * @param config PSWebdavConfigDef
    */
   private void validateContentTokens(PSWebdavConfigDef config)
   {
      String tokens = config.getPublicValidTokens();
      if (!config.parseTokens(tokens))
         m_warningsList.add(getResourceString(
               "warn.contentvalidflag.badformat",
               new String[] { IPSRxWebDavDTD.ATTR_PUBLICFLAGS }));
      tokens = config.getQEValidTokens();
      if (!config.parseTokens(tokens))
         m_warningsList.add(getResourceString(
               "warn.contentvalidflag.badformat",
               new String[] { IPSRxWebDavDTD.ATTR_QEFLAGS }));
   }

   /**
    * Validates the WebDav configuration file passed in.
    *
    * @param in the input stream of the WebDav config file, it may not be
    *    <code>null</code>.
    * @param paths a list of zero or more Rhythmyx root paths of registered 
    *    servlets, which does not include its own root path. Never 
    *    <code>null</code>.
    * 
    * @throws ServletException if an error occurs
    */
   public void validate(InputStream in, Iterator paths) throws ServletException
   {
      if (in == null)
         throw new IllegalArgumentException(
               "in (config InputStream) may not be null");

      String startHtml = "<html>\n<head>\n<title>"
            + getResourceString("msg.validator.title")
            + "</title>\n</head><body>\n<center><h2>"
            + getResourceString("msg.validator.title") + "</h2></center><hr>\n";

      m_out.write(startHtml);
      try
      {
         PSRemoteAgent agent = getRemoteAgent();
         PSWebdavConfigDef config = new PSWebdavConfigDef(in, true, null);
         m_exceptionsList = config.getValidationExceptionsList();

         // Validate root folder keep going even
         // if the validation fails
         validateRoot(config.getRootPath());

         if (paths == null)
            m_exceptionsList
                  .add(getResourceString("error.empty.registeredPaths"));
         else
            checkNestedRootPaths(config.getRootPath(), paths);
         validateContentTokens(config);

         // Validate community agaist the CMS
         List validComms = agent.getCommunities();
         PSEntry commEntry = isValidCommunityID(validComms, config
               .getCommunityId(), config.getCommunityName());

         //If the community validation succeeds, validate content types,
         // mimetypes & fields
         if (commEntry != null)
         {
            List validTypeList = agent.getContentTypes(commEntry);
            List supportedTypes = PSIteratorUtils.cloneList(config
                  .getContentTypes());
            validateAllContentTypes(validTypeList, supportedTypes);
         }

         // Write out error messages
         Iterator exceptions = m_exceptionsList.iterator();
         if (m_exceptionsList.size() > 0)
            m_out.write("<h4>" + getResourceString("msg.errors.found")
                  + "</h4>");
         while (exceptions.hasNext())
         {
            Object obj = exceptions.next();
            if (obj instanceof PSWebdavException)
            {
               PSWebdavException ex = (PSWebdavException) obj;
               writeError(ex.getMessage(), ERROR);
            }
            else if (obj instanceof String)
            {
               writeError((String) obj, ERROR);
            }
         }

         // Write out warnings
         Iterator warnings = m_warningsList.iterator();
         if (m_warningsList.size() > 0)
            m_out.write("<h4>" + getResourceString("msg.warnings.found")
                  + "</h4>");
         while (warnings.hasNext())
         {
            writeError((String) warnings.next(), WARNING);
         }

         if (m_exceptionsList.size() == 0)
         {//size=\"5\">" +
            // Configuration successfully validated
            m_out.write("<p><center><h2><font color=\"green\">"
                  + getResourceString("msg.validation.success")
                  + "</font></center></h2><br>");
         }

      }
      catch (PSWebdavException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         m_out.write("<h4>" + getResourceString("msg.errors.found") + "</h4>");
         writeError(e.getMessage(), ERROR);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new ServletException(e.getMessage());
      }

      m_out.write("</body>\n</html>");
   }

   /**
    * Test to see if the root Path is exists and has the correct format.
    * 
    * @param rootPath
    *           String root for this servlet may not be <code>null</code> or empty.
    * @return <code>true</code> if rootPath exists, and is specified
    *         correctly otherwise <code>false</code>
    * @throws Exception
    *            on PSRelationShipProcessorProxy error
    */
   public boolean validateRoot(String rootPath) throws Exception
   {

      boolean isValid = true;
      if (StringUtils.isEmpty(rootPath))
      {
         return false;
      }
      
      PSRelationshipProcessorProxy proxy = new PSRelationshipProcessorProxy(
            PSRelationshipProcessorProxy.PROCTYPE_REMOTE, m_requester);
      PSComponentSummary summary = proxy.getSummaryByPath(PSWebdavMethod.FOLDER_TYPE, rootPath,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);

      if (summary == null)
      {
         m_exceptionsList.add(getResourceString("error.root.does.not.exist",
               new Object[] { rootPath }));
         isValid = false;
      }

      int pos = rootPath.lastIndexOf('/');
      if (pos <= 1)
      {
         m_exceptionsList.add(getResourceString("error.root.levels"));
         isValid = false;
      }

      return isValid;
   }

   /**
    * Finds the PSEntry community given a community id and verifies that the
    * name matches the label.
    * 
    * @param validComms
    *           List PSEntry community objects assumed not <code>null</code>
    * @param selectedID
    *           int selected community id assumed not <code>null</code>
    * @param name
    *           String selected community name may not be <code>null</code>
    * @return PSEntry community if a match is found
    *         return will be <code>null</code> if the selectedID is invalid
    */
   public PSEntry isValidCommunityID(List validComms, int selectedID, String name)
   {
      boolean isValid = false;
      Iterator it = validComms.iterator();
      PSEntry commEntry = null;
      
      while (it.hasNext())
      {
         commEntry = (PSEntry) it.next();
         if (commEntry.getValue().equals(Integer.toString(selectedID)))
         {
            isValid = true;
            // Check the community name
            if (!commEntry.getLabel().getText().equalsIgnoreCase(name))
            {
               m_warningsList.add(getResourceString("warn.bad.community.name",
                     new String[] { commEntry.getLabel().getText() }));
            }
            break;
         }
      }
      if (!isValid)
      {
         m_exceptionsList.add(getResourceString("error.bad.community.id",
               new String[] { commEntry.getValue() }));
      }
      return commEntry;
   }

   /**
    * Ensures that the ContentType passed in has a valid id.
    * 
    * @param validTypes
    *           List of PSEntry 
    * @param type
    *           PSWebdavContentType ContentType to verify. 
    * @return PSEntry ctype if the name and id of PSWebdavContentType type
    *          are valid, otherwise <code>null</code>
    */
   private PSEntry validateAContentType(List validTypes, PSWebdavContentType type)
   {
      PSEntry ctEntry = null;
      boolean isValid = false;

      // Does this content type id exist in the CMS?
      Iterator vTypes = validTypes.iterator();
      while (vTypes.hasNext())
      {
         ctEntry = (PSEntry) vTypes.next();
         if (ctEntry.getValue().equals(Long.toString(type.getId())))
         {
            isValid = true;
            // Check the type name
            if (!ctEntry.getLabel().getText().equalsIgnoreCase(type.getName()))
            {
               m_warningsList.add(getResourceString(
                     "warn.bad.contenttype.name", new String[] {
                           ctEntry.getValue(), ctEntry.getLabel().getText() }));
            }
            break;
         }
      }
      return (isValid) ? ctEntry : null;
   }

   /**
    * For each of the valid Content Types, validate the mimeTypes & fields
    * Adds errors encountered to the exception list.
    * 
    * @param validTypes
    *           List of PSWebdavContentType support by Rhythmyx not
    *           <code>null</code>.
    * @param ConfigedTypes
    *           List of PSWebdavContentType this servlet will support not
    *           <code>null</code>.
    * @throws PSRemoteException  on error
    *           
    */
   public void validateAllContentTypes(List validTypes, List ConfigedTypes)
         throws PSRemoteException
   {
      if (validTypes == null)
         throw new IllegalArgumentException("validTypes may not be null");
      if (ConfigedTypes == null)
         throw new IllegalArgumentException("ConfigedTypes may not be null");
      PSEntry ctEntry = null;
      boolean isValid = false;
      Iterator walker = ConfigedTypes.iterator();
      while (walker.hasNext())
      {
         boolean contentTypeExists = false;
         PSWebdavContentType type = (PSWebdavContentType) walker.next();

         // Does this content type id exist in the CMS?
         ctEntry = validateAContentType(validTypes, type);
         if (ctEntry == null)
         {
            m_exceptionsList.add(getResourceString("error.bad.contenttype.id",
                  new String[] { Long.toString(type.getId()) }));
         }
         else
         {
            validateFields(type, ctEntry);
         }
      } //end of outer while
      
      validateMimeTypes(ConfigedTypes);
   }

   /**
    * Validates all fields of the supplied content type.
    * 
    * @param ctType
    *           the to be validated content type.
    * @param ctEntry
    *           the entry object of the validated content type, which contains
    *           the content id and content name.
    * 
    * @throws PSRemoteException
    *            if remote error occurs.
    */
   private void validateFields(PSWebdavContentType ctType, PSEntry ctEntry)
         throws PSRemoteException
   {
      // Check for fields
      PSClientItem clientItem = getRemoteAgent().newItem(ctEntry.getValue());
      List fieldNames = PSIteratorUtils
            .cloneList(clientItem.getAllFieldNames());

      // Does contentfield exist?
      if (!fieldNames.contains(ctType.getContentField()))
      {
         m_exceptionsList.add(getResourceString("error.bad.contentfield",
               new String[] { ctType.getContentField(),
                     ctEntry.getLabel().getText() }));

      }
      // Does ownerfield exist?
      if (!fieldNames.contains(ctType.getOwnerField()))
      {
         m_exceptionsList.add(getResourceString("error.bad.ownerfield",
               new String[] { ctType.getOwnerField(),
                     ctEntry.getLabel().getText() }));

      }
      // Check field names in all properties
      Iterator props = ctType.getMappings();
      while (props.hasNext())
      {
         PSPropertyFieldNameMapping prop = (PSPropertyFieldNameMapping) props
               .next();
         if (!fieldNames.contains(prop.getFieldName()))
         {
            m_exceptionsList.add(getResourceString("error.bad.fieldname",
                  new String[] { prop.getFieldName(), prop.getPropertyName(),
                        ctEntry.getLabel().getText() }));
         }
         // "displayname" property maps to a field which is not "sys_title"
         if (prop.getPropertyName().equalsIgnoreCase(
               IPSWebdavConstants.P_DISPLAYNAME)
               && (!prop.getFieldName().equalsIgnoreCase(
                     IPSWebdavConstants.SYS_TITLE)))
         {
            m_warningsList.add(getResourceString(
                  "warn.ignore.displayname.property", new String[] {
                        prop.getFieldName(), ctEntry.getLabel().getText() }));
         }
         // "sys_title" field maps to a property which is not "displayname"
         if (prop.getFieldName().equalsIgnoreCase(IPSWebdavConstants.SYS_TITLE)
               && (!prop.getPropertyName().equalsIgnoreCase(
                     IPSWebdavConstants.P_DISPLAYNAME)))
         {
            m_exceptionsList
                  .add(getResourceString("error.mapping.on.sys_title",
                        new String[] { prop.getPropertyName(),
                              ctEntry.getLabel().getText() }));
         }

      }

      // validates field rules
      PSItemDefinition itemDef = clientItem.getItemDefinition();
      Iterator psFields = itemDef.getMappedParentFields().iterator();

      // get the excluded field names, which include the value of the fields
      // are set by WebDAV or the Backend server.
      String fieldName;
      List<String> excludeFieldNames = new ArrayList<>();
      fieldName = ctType.getFieldName(IPSWebdavConstants.P_DISPLAYNAME);
      if (fieldName != null)
         excludeFieldNames.add(fieldName);
      fieldName = ctType.getFieldName(IPSWebdavConstants.P_GETCONTENTTYPE);
      if (fieldName != null)
         excludeFieldNames.add(fieldName);
      fieldName = ctType.getFieldName(IPSWebdavConstants.P_GETCONTENTLENGTH);
      if (fieldName != null)
         excludeFieldNames.add(fieldName);

      excludeFieldNames.addAll(ms_excludeSysFieldRules);

      while (psFields.hasNext())
      {
         PSField field = (PSField) psFields.next();

         PSFieldValidationRules rules = field.getValidationRules();
         fieldName = field.getSubmitName();
         if (rules != null && !excludeFieldNames.contains(fieldName))
         {
            m_warningsList.add(getResourceString("warn.field.rules",
                  new String[] { rules.getName(), fieldName,
                        ctEntry.getLabel().getText() }));
         }
      }
   }

   /**
    * Logs a message if more than one contentType registers for a mimetype
    * 
    * @param contentTypes List of PSWebdavContentType supported by this servlet
    */
   void validateMimeTypes(List contentTypes)
   {
      PSWebdavContentType contentType = null;

      Set allMimeTypes = new HashSet();
      Iterator ctWalker = contentTypes.iterator();
      while (ctWalker.hasNext())
      {
         contentType = (PSWebdavContentType) ctWalker.next();
         if (contentType.isDefault())
            continue; // ignore the default
         
         // for each content type, put all mime types into a set to dedup
         Set mimeTypeSet = new HashSet();
         List mimeTypes = PSIteratorUtils.cloneList(contentType.getMimeTypes());
         mimeTypeSet.addAll(mimeTypes);
         
         // make sure the mime types are unique
         Iterator mtWalker = mimeTypeSet.iterator();
         while (mtWalker.hasNext())
         {
            String mimetype = (String) mtWalker.next();
            if (allMimeTypes.contains(mimetype))
            {
               m_exceptionsList.add(getResourceString("error.mimetype.overlap",
                     new String[] { mimetype }));
            }
            else
               allMimeTypes.add(mimetype);
         }
      }
   }
   
   /*
    * Checks to see if This servlets root path is contained by, or contains
    * another servlets root path. 
    * 
    * @param me String servlet root path assume not <code>null</code>
    * @param it a list of registered root paths, which does not include its
    *    own root path, assumed not <code>null</code>.
    * 
    * @returns <code>true</code> if this servlet's rootpath is not contained
    *          nor contains any other registered servlet root paths.
    *          <code>false</code> otherwise.
    */
   private boolean checkNestedRootPaths(String me, Iterator it)
   {
      boolean status = false;

      while (it.hasNext())
      {
         String apath = (String) it.next();
         if ((apath.indexOf(me) != -1) || (me.indexOf(apath) != -1))
         {
            m_exceptionsList.add(getResourceString("error.nested.root",
                  new String[] { me, apath }));
            status = true;
         }
      }
      return status;
   }

   /**
    * Gets the remote agent, which is used to perform remote operations on cms
    * item in Rhythmyx. All operations will override the community to the
    * community id in the webdav config.
    * 
    * @return the remote agent, never <code>null</code>.
    */
   private PSRemoteAgent getRemoteAgent()
   {
      PSRemoteAgent agent = new PSRemoteAgent(m_requester);
      return agent;
   }

   /**
    * Writes an error message to the responses PrintWriter
    * adding the correct font formatting.
    *
    * @param msg the message, may be <code>null</code>
    *
    * @param level the error level (ERROR or WARNING)
    */
   private void writeError(String msg, int level)
   {
      if (msg == null)
         msg = "";
      String color = "red";
      switch (level) {
      case ERROR:
         color = "red";
         break;

      case WARNING:
         color = "blue";
         break;

      default:
      }
      msg = "<li><font color=\"" + color + "\">" + msg + "</font></li>";
      m_out.write(msg);

   }

   /**
    * Returns the resource string if it exists, else returns the resource key
    * string.
    * 
    * @param key
    *           the resource string key, cannot be <code>null</code> or empty.
    * 
    * @param args
    *           array of string replacement arguments.
    * 
    * @return the string or the key if the resource string could not be found.
    */
   static String getResourceString(String key, Object[] args)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException(
               "The key string cannot be null or empty.");
      try
      {
         String msg = ms_res.getString(key);
         return MessageFormat.format(msg, args);
      }
      catch (MissingResourceException e)
      {
         return key;
      }

   }

   /**
    * Returns the resource string if it exists, else returns the resource key
    * string.
    * 
    * @param key
    *           the resource string key, cannot be <code>null</code> or empty.
    * 
    * @return the string or the key if the resource string could not be found.
    */
   static String getResourceString(String key)
   {
      return getResourceString(key, null);
   }

   /**
    * The servlet request object, initialized in ctor. Never <code>null</code>
    * after that.
    */
   private HttpServletRequest m_req;

   /**
    * The servlet response object, initialized in ctor. Never <code>null</code>
    * after that.
    */
   private HttpServletResponse m_resp;

   /**
    * The responses output writer, initialized in ctor. Never <code>null</code>
    * after that.
    */
   private PrintWriter m_out;

   /**
    * The remote requester, which is used to communicate with the remote
    * Rhythmyx Server. Init by ctor, never <code>null</code> after that.
    */
   private IPSRemoteRequester m_requester;

   // Error levels
   private static final int ERROR = 1;

   private static final int WARNING = 2;

   /**
    * Resource bundle for this class, initialized once the first time the ctor
    * is invoked. Never <code>null</code> after that.
    */
   private static ResourceBundle ms_res = ResourceBundle
         .getBundle(PSWebdavConfigValidator.class.getName() + "Resources");

   /**
    * A list of system field names that will be excluded during validating field
    * rules.
    */
   private static List ms_excludeSysFieldRules = new ArrayList();
   static
   {
      ms_excludeSysFieldRules.add("sys_title");
      ms_excludeSysFieldRules.add("sys_workflowid");
   }
}
