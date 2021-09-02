/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.user.service.impl;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSUserManagementEvent;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSExceptionUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.role.service.IPSRoleService;
import com.percussion.role.service.impl.PSRoleService;
import com.percussion.security.IPSPasswordFilter;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSPasswordHandler;
import com.percussion.security.PSSecurityCatalogException;
import com.percussion.security.PSSecurityException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.system.PSAssignmentTypeHelper;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.impl.PSServerConfigUpdater;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.share.service.PSCollectionUtils;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.sitemanage.dao.IPSUserLoginDao;
import com.percussion.user.data.PSAccessLevel;
import com.percussion.user.data.PSAccessLevelRequest;
import com.percussion.user.data.PSCurrentUser;
import com.percussion.user.data.PSExternalUser;
import com.percussion.user.data.PSImportedUser;
import com.percussion.user.data.PSImportedUser.ImportStatus;
import com.percussion.user.data.PSImportedUserList;
import com.percussion.user.data.PSRoleList;
import com.percussion.user.data.PSUser;
import com.percussion.user.data.PSUserList;
import com.percussion.user.data.PSUserLogin;
import com.percussion.user.data.PSUserProviderType;
import com.percussion.user.service.IPSUserService;
import com.percussion.user.service.IPSUserService.PSDirectoryServiceStatus.ServiceStatus;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.PSSpringBeanProvider;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.service.IPSUtilityService;
import com.percussion.utils.service.impl.PSBackEndRoleManagerFacade;
import com.percussion.utils.service.impl.PSUtilityService;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.security.auth.Subject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.percussion.role.service.IPSRoleService.ADMINISTRATOR_ROLE;
import static com.percussion.role.service.IPSRoleService.DESIGNER_ROLE;
import static com.percussion.utils.request.PSRequestInfoBase.KEY_PSREQUEST;
import static com.percussion.utils.request.PSRequestInfoBase.initRequestInfo;
import static com.percussion.utils.request.PSRequestInfoBase.resetRequestInfo;
import static com.percussion.utils.request.PSRequestInfoBase.setRequestInfo;
import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.setUserName;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.containsAny;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * See the interface for documentation.
 * 
 * @author DavidBenua
 * @author adamgent
 */
@Path("/user")
@Component("userService")
@Lazy
public class PSUserService implements IPSUserService
{
    private static final Logger log = LogManager.getLogger(PSUserService.class);

    public static final String VAR_CONFIG_PATH="var" + File.separatorChar + "config";

    public static final String PWD_CONFIG_PATH=VAR_CONFIG_PATH + File.separatorChar + "generated";

    // Used to get the email on the user
    private static final String EMAIL_ATTRIBUTE_NAME = "sys_email";
    private static final String PWD_FILE = "passwords";

    private final IPSUserLoginDao userLoginDao;

    private final IPSPasswordFilter passwordFilter;

    private final IPSRoleMgr roleMgr;

    private final PSBackEndRoleManagerFacade backEndRoleMgr;
    
    private final IPSWorkflowService workflowService;
    
    private final IPSSecurityWs securityWs;
    
    private final IPSContentWs contentWs;
    
    private final IPSIdMapper idMapper;

    private List<String> accessibilityRoles = null;
    
  
    private IPSSystemProperties systemProps;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private final IPSUtilityService utilityService;
    
    public static final String PERCUSSION_ADMIN_NAME = "PercussionAdmin";
    public static final String ADMIN_NAME ="Admin";
    public static final String ADMIN1_NAME ="admin1";
    public static final String ADMIN2_NAME ="admin2";
    public static final String EDITOR_NAME="Editor";
    public static final String CONTRIBUTOR_NAME="Contributor";
    public static final String RXSERVER_NAME="rxserver";
    public static final String RXPUBLISHER_NAME="rxpublisher";


    public static final List<String> SYSTEM_USERS = asList(RXSERVER_NAME,PERCUSSION_ADMIN_NAME);
    private final PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSUserManagementEvent psUserManagementEvent;

    @Autowired
    public PSUserService(IPSUserLoginDao userLoginDao, IPSPasswordFilter passwordFilter,
            IPSBackEndRoleMgr backEndRoleMgr, IPSRoleMgr roleMgr, IPSNotificationService notificationService,
            IPSWorkflowService workflowService, IPSSecurityWs securityWs, IPSContentWs contentWs, IPSIdMapper idMapper, IPSUtilityService utilityService)
    {
        super();
        this.userLoginDao = userLoginDao;
        this.passwordFilter = passwordFilter;
        this.backEndRoleMgr = new PSBackEndRoleManagerFacade(backEndRoleMgr);
        this.roleMgr = roleMgr;
        this.workflowService = workflowService;
        this.securityWs = securityWs;
        this.contentWs = contentWs;
        this.idMapper = idMapper;
        this.utilityService = utilityService;
        setupServerStartupListener(notificationService);
    }
    
    /**
     * Registers {@link PSCreatePercussionUserNotificationListener} for server startup.
     * @param notificationService never <code>null</code>.
     */
    protected void setupServerStartupListener(IPSNotificationService notificationService) 
    {
        if ( notificationService != null ) {
            PSCreatePercussionUserNotificationListener listener = new PSCreatePercussionUserNotificationListener();
            notificationService.addListener(EventType.CORE_SERVER_INITIALIZED, listener);
        }
    }
    
    /**
     * 
     * Create the PercussionUser for SaaS PIG test
     * 
     * @author adamgent
     *
     */
    protected class PSCreatePercussionUserNotificationListener implements IPSNotificationListener, Runnable
    {

        private String errorMessage = "The server could not create the percussion user. ";
        
        /**
         * We run the user work in a separate thread to avoid dirtying the server start up thread.
         */
        @Override
        public void run()
        {
            try
            {
                PSThreadRequestUtils.initServerThreadRequest();
                if(utilityService.isSaaSEnvironment() && findUsername(PERCUSSION_ADMIN_NAME) == null) {
                    log.info("Creating Percussion User");
                    /*
                     * Here we have to setup thread-local meta data used for web
                     * services. On server start up this is not setup.
                     */
                    PSRequest req = PSRequest.getContextForRequest();
                    resetRequestInfo();
                    initRequestInfo( null);
                    setRequestInfo(KEY_PSREQUEST, req);
                    setUserName(PSSecurityProvider.INTERNAL_USER_NAME);
    
                    TimeUnit.SECONDS.sleep(30);
                    
                    createPercussionUser();
                    log.info("Finished creating Percussion User");
                }
                log.info("Replacing legacy 'demo' password for generated users...");
                updateLegacyPasswordsForUser(ADMIN_NAME);
                updateLegacyPasswordsForUser(EDITOR_NAME);
                updateLegacyPasswordsForUser(CONTRIBUTOR_NAME);
                updateLegacyPasswordsForUser(RXSERVER_NAME);
                updateLegacyPasswordsForUser(ADMIN1_NAME);
                updateLegacyPasswordsForUser(ADMIN2_NAME);
                log.info("Done generating new password for generated users.");

            } catch (InterruptedException e) {
                log.warn("Shutting down user update thread...");
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void notifyEvent(PSNotificationEvent event)
        {
            notNull(event, "event");
            isTrue(EventType.CORE_SERVER_INITIALIZED == event.getType(),
                    "Should only be registered for server startup.");
            
            try
            {
                /*
                 * This will execute our work concurrently.
                 */
                executorService.execute(this);
            }
            catch (Exception e)
            {
                throw new RuntimeException(errorMessage,e);
            }

        }

    }


    private void writeTemporaryPassword(String uid, String pwd){
        File pwdFile = new File(PSServer.getRxDir().getAbsolutePath() + File.separatorChar + PWD_CONFIG_PATH);
    try {
        if (!pwdFile.exists()) {
            pwdFile.mkdirs();
            Properties props = new Properties();
                props.put(uid, pwd);
                try (FileOutputStream outputStream = new FileOutputStream(PSServer.getRxDir().getAbsolutePath() + File.separatorChar + PWD_CONFIG_PATH + File.separatorChar + PWD_FILE)) {
                    props.store(outputStream, "File for generated temporary passwords");
                }
        }else{
            Properties props = new Properties();
            try(FileInputStream fis = new FileInputStream(PSServer.getRxDir().getAbsolutePath() + File.separatorChar + PWD_CONFIG_PATH + File.separatorChar + PWD_FILE)) {
                props.load(fis);
            }

            props.put(uid,pwd);

            try (FileOutputStream outputStream = new FileOutputStream(PSServer.getRxDir().getAbsolutePath() + File.separatorChar + PWD_CONFIG_PATH + File.separatorChar + PWD_FILE)) {
                props.store(outputStream, "File for generated temporary passwords");
            }
        }
    } catch (IOException e) {
        log.error("{}", PSExceptionUtils.getMessageForLog(e));
        log.debug(e);
    }
    }


    /***
     * generates a new password for any built-in / generated users
     * that have "demo" as their password.
     */
    private void updateLegacyPasswordsForUser(String userName){

        boolean found = false;
        PSUserLogin u=null;
        try {

            List<PSUserLogin> users = userLoginDao.findByName(userName);
            if(users != null && !users.isEmpty()) {
                 u = users.get(0);
                log.debug("Found User: {}", u.getUserid());
            }
            found = true;
        } catch (PSDataServiceException e) {
            //ignore if not found
        }

        if(found && u != null) {
            try {
                if (PSLegacyEncrypter.LEGACY_USER_PWD.equalsIgnoreCase(u.getPassword()) ||
                        PSLegacyEncrypter.LEGACY_USER_PWD_ENC.equalsIgnoreCase(u.getPassword()) ||
                        PSPasswordHandler.getHashedPassword(PSLegacyEncrypter.LEGACY_USER_PWD).equals(u.getPassword())
                ) {
                    String pw = SecureStringUtils.generateRandomPassword();
                    String cryptPW = (passwordFilter == null) ? pw : passwordFilter.encrypt(pw);
                    u.setPassword(cryptPW);
                    try {

                        userLoginDao.save(u);

                        writeTemporaryPassword(userName, pw);

                        log.info("Generating new temporary password: {} for {}", pw, userName);
                        log.info("This temporary password will be stored in: {}", PSServer.getRxDir().getAbsolutePath() + File.separatorChar + PWD_CONFIG_PATH + File.separatorChar + PWD_FILE);
                        log.info("Please change this temporary password using the Change Password feature after installation / upgrade.");
                    } catch (PSDataServiceException e) {
                        log.error("An unexpected error resetting legacy passwords: {}",
                                PSExceptionUtils.getMessageForLog(e));
                        log.debug(e);
                    }
                }
            } catch (PSEncryptionException e) {
                log.error(PSExceptionUtils.getMessageForLog(e));
            }
        }
    }

    /**
     * Create the PercussionUser.  Will generate a password and write the password
     * to system log and to the PWD_CONFIG_PATH + "password" file.
     */
    protected void createPercussionUser() {

        PSUser user = new PSUser();

        String password = SecureStringUtils.generateRandomPassword();

        user.setName(PERCUSSION_ADMIN_NAME);
        user.setPassword(password);

        user.setEmail("");
        List<String> roles = new ArrayList<>();
        roles.add(IPSRoleService.ADMINISTRATOR_ROLE); 
        
        user.setRoles(roles);
        createUser(user);
        log.info("Generating temporary password: {} for {}", password, PERCUSSION_ADMIN_NAME);
        log.info("This temporary password will be stored in: {}",PWD_CONFIG_PATH + File.separatorChar + PWD_FILE);
        log.info("Please change this temporary password using the Change Password feature after installation / upgrade.");
        writeTemporaryPassword(PERCUSSION_ADMIN_NAME,password);
    }

    @Override
    @POST
    @Path("/create")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUser create(PSUser user) throws PSDataServiceException
    {
        log.debug("creating user {}", user);
        doValidation(user, true);

        return createUser(user);
    }

    private PSUser createUser(PSUser user)
    {
        PSUserLogin login = new PSUserLogin();
        login.setUserid(user.getName());
        String cryptPW = (passwordFilter == null) ? user.getPassword() : passwordFilter.encrypt(user.getPassword());
        login.setPassword(cryptPW);
        try
        {
            login = userLoginDao.create(login);

            updateRoles(user.getName(), user.getRoles());
            backEndRoleMgr.setSubjectEmail(user.getName(), user.getEmail());
        } catch (IPSGenericDao.SaveException e) {
            log.error("Failed to create user {} because could not add roles to user: {}",user.getName() , e.getMessage());
            log.debug(e.getMessage(),e);
        }

        PSUser rvalue = user.clone();
        rvalue.setProviderType(PSUserProviderType.INTERNAL);
        rvalue.setPassword(null);
        rvalue.setEmail(user.getEmail());
        return rvalue;
    }

    @Override
    @DELETE
    @Path("/delete/{name}")
    public void delete(@PathParam("name") String name) throws PSDataServiceException
    {
        log.debug("deleting user {}", name);
        checkUser(name);
        if (PSCollectionUtils.containsIgnoringCase(SYSTEM_USERS, name))
            PSParameterValidationUtils.validateParameters("delete").rejectField("name", "Cannot delete system user",
                    name).throwIfInvalid();
        PSUserProviderType provider = fromProvider(name);
        String current = getCurrentUserName();
        if (name.equalsIgnoreCase(current))
        {
            String emsg = "Cannot delete the current user";
            log.error(emsg);
            PSParameterValidationUtils.validateParameters("delete").rejectField("name", emsg, name).throwIfInvalid();
        }
        // remove from all roles
        backEndRoleMgr.setRoles(name, Collections.<String> emptyList());
        if (provider == PSUserProviderType.INTERNAL)
        {
            userLoginDao.delete(name);
        }
        try{
            psUserManagementEvent=new PSUserManagementEvent(PSSecurityFilter.getCurrentRequest().getServletRequest(),
                    PSUserManagementEvent.UserEventActions.delete,
                    PSActionOutcome.SUCCESS);
            psAuditLogService.logUserManagementEvent(psUserManagementEvent);
        }catch (Exception e){
            //Just handling exception
        }

        PSNotificationEvent notifyEvent = new PSNotificationEvent(EventType.USER_DELETE, name);
        IPSNotificationService srv = PSNotificationServiceLocator.getNotificationService();
        srv.notifyEvent(notifyEvent);
    }

    @Override
    @GET
    @Path("/find/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUser find(@PathParam("name") String name) throws PSDataServiceException
    {
        PSUser user = new PSUser();
        user.setName(name);
        checkUser(name);
        PSUserProviderType provider = fromProvider(user.getName());
        user.setProviderType(provider);
        List<String> roles = findRoles(name);
        roles = filterOutSystemRoles(roles);
        user.setRoles(roles);
        if (provider.equals(PSUserProviderType.INTERNAL))
        {
            try
            {
                user.setEmail(getSubjectEmail(name));
            }
            catch (PSSecurityCatalogException e)
            {
                log.error("Failed to get the email for the user: {}",name);
            }
        }
        return user;
    }

    /**
     * Filter out the pre-defined system roles for the specified roles.
     * 
     * @param srcRoles the list of role names in question, assumed not
     *            <code>null</code>.
     * 
     * @return a list of role names that does not contain any of the pre-defined
     *         system roles. It may be empty, but never <code>null</code>.
     */
    private List<String> filterOutSystemRoles(Collection<String> srcRoles)
    {
        List<String> result = new ArrayList<>();
        for (String role : srcRoles)
        {
            if (!PSCollectionUtils.containsIgnoringCase(PSRoleService.SYSTEM_ROLES, role))
            {
                result.add(role);
            }
        }

        return result;
    }

    /**
     * Gets the list of roles. The list of roles is returned in alphabetical
     * order according to the current default locale.
     * 
     * @see com.percussion.user.service.IPSUserService#getRoles()
     */
    @Override
    @GET
    @Path("/roles")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSRoleList getRoles() throws PSDataServiceException
    {
        PSRoleList roles = new PSRoleList();

        List<String> rl = filterOutSystemRoles(backEndRoleMgr.getRoles());
        roles.setRoles(rl);
        return roles;
    }

    /**
     * Gets the list of users. The list of users is returned in alphabetical
     * order according to the current default locale.
     * 
     * @see com.percussion.user.service.IPSUserService#getUsers()
     */
    @Override
    @GET
    @Path("/users")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserList getUsers() throws PSDataServiceException
    {
        List<String> names = findUserNames(null);
        names.removeAll(SYSTEM_USERS);
        PSUserList result = new PSUserList();
        sort(names);
        result.setUsers(names);
        return result;
    }
    
    @Override
    @GET
    @Path("/users/names/{nameFilter}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserList getUserNames(@PathParam("nameFilter") String nameFilter) throws PSDataServiceException
    {
        if(nameFilter == null || StringUtils.isEmpty(nameFilter) || nameFilter.equalsIgnoreCase("*")){
            nameFilter = "%";
        }

        List<String> names = findUserNames(nameFilter);
        names.removeAll(SYSTEM_USERS);
        PSUserList result = new PSUserList();
        sort(names);
        result.setUsers(names);
        return result;
    }

    /**
     * Gets the list of users which are members of the specified role. The list
     * of users is returned in alphabetical order according to the current
     * default locale.
     * 
     * @see com.percussion.user.service.IPSUserService#getUsers()
     */
    @Override
    @GET
    @Path("/usersByRole/{role}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUserList getUsersByRole(@PathParam("role") String roleName) throws PSDataServiceException
    {
        PSParameterValidationUtils.rejectIfBlank("getUsersByRole", "roleName", roleName);

        PSUserList userList = getUsers();
        Iterator<String> iter = userList.getUsers().iterator();
        while (iter.hasNext())
        {
            PSUser user = find(iter.next());
            if (!user.getRoles().contains(roleName))
            {
                iter.remove();
            }
        }

        return userList;
    }

    private PSUserProviderType fromProvider(String name) throws PSDataServiceException {
        boolean internal = userLoginDao.find(name) != null;
        return internal ? PSUserProviderType.INTERNAL : PSUserProviderType.DIRECTORY;
    }

    @Override
    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUser update(PSUser user) throws PSDataServiceException
    {
        log.debug("updating user {}" , user);
        PSUserProviderType provider = fromProvider(user.getName());
        user.setProviderType(provider);
        doValidation(user, false);

        if (provider == PSUserProviderType.INTERNAL && isNotBlank(user.getPassword()))
        { // only update if there is a password supplied
            // and its an internal user.
            PSUserLogin login = new PSUserLogin();
            login.setUserid(user.getName());
            String cryptPW = (passwordFilter == null) ? user.getPassword() : passwordFilter.encrypt(user.getPassword());
            login.setPassword(cryptPW);

            userLoginDao.save(login);

        }
        updateRoles(user.getName(), user.getRoles());

        PSUser rvalue = user.clone();
        rvalue.setProviderType(provider);
        rvalue.setPassword(null);
        if (provider.equals(PSUserProviderType.INTERNAL))
        {
            backEndRoleMgr.setSubjectEmail(user.getName(), user.getEmail());
            rvalue.setEmail(user.getEmail());
        }
        try {
            psUserManagementEvent = new PSUserManagementEvent(PSSecurityFilter.getCurrentRequest().getServletRequest(),
                    PSUserManagementEvent.UserEventActions.update,
                    PSActionOutcome.SUCCESS);
            psAuditLogService.logUserManagementEvent(psUserManagementEvent);
        }catch (Exception e){
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return rvalue;
    }

    @Override
    @PUT
    @Path("/changepw")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUser changePassword(PSUser user) throws PSDataServiceException {
        log.debug("changing password for user {}" , user);

        // the result
        PSUser rvalue = null;

        PSUserProviderType provider = fromProvider(user.getName());
        user.setProviderType(provider);

        // parameter validation
        PSParameterValidationUtils.validateParameters("changePassword").rejectIfNull("user", user).throwIfInvalid();
        PSParameterValidationUtils.validateParameters("changePassword").rejectIfBlank("password", user.getPassword()).throwIfInvalid();
        PSParameterValidationUtils.validateParameters("changePassword").rejectIfBlank("name", user.getName()).throwIfInvalid();

        // can only change password of self
        String userName = user.getName();
        PSCurrentUser currentUser = getCurrentUser();
        if (currentUser == null || !userName.equalsIgnoreCase(currentUser.getName())) {
            String emsg = "Can only change the password of the current user";
            log.error(emsg);
            PSParameterValidationUtils.validateParameters("changePassword").rejectField("name", emsg, userName).throwIfInvalid();
        }

        // only update if there is a password supplied
        // and its an internal user.
        if (provider == PSUserProviderType.INTERNAL && isNotBlank(user.getPassword())) {
            PSUserLogin login = new PSUserLogin();
            login.setUserid(user.getName());
            String cryptPW = (passwordFilter == null) ? user.getPassword() : passwordFilter.encrypt(user.getPassword());
            login.setPassword(cryptPW);

            // save changes
            userLoginDao.save(login);

            rvalue = user.clone();
            rvalue.setRoles(currentUser.getRoles());
            rvalue.setProviderType(provider);
            rvalue.setPassword(null);

        }

        return rvalue;
    }

    @Override
    @GET
    @Path("/current")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSCurrentUser getCurrentUser() throws PSDataServiceException {
        String userName;
        try {
            userName = getCurrentUserName();
            if (isBlank(userName))
                throw new PSNoCurrentUserException("No current user in current request");
        } catch (Exception e) {
            throw new PSNoCurrentUserException("Error getting current user.", e);
        }
        PSUser user = find(userName);
        PSCurrentUser currUser = new PSCurrentUser(user);

        boolean isAdmin = currUser.getRoles().contains(ADMINISTRATOR_ROLE);
        currUser.setAdminUser(isAdmin);

        boolean isDesigner = currUser.getRoles().contains(DESIGNER_ROLE);
        currUser.setDesignerUser(isDesigner);

        boolean isAccessibility = containsAny(currUser.getRoles(), getAccessibilityRoles());
        currUser.setAccessibilityUser(isAccessibility);

        return currUser;
    }

    @POST
    @Path("/accessLevel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSAccessLevel getAccessLevel(PSAccessLevelRequest request)
    {
        try {
            PSParameterValidationUtils.rejectIfNull("getAccessLevel", "request", request);

            PSAssignmentTypeEnum assignmentType = PSAssignmentTypeEnum.READER;

            String type = request.getType();
            int workflowId = request.getWorkflowId() > 0 ? request.getWorkflowId() : getWorkflowId(request);

            try {
                PSWorkflow wf = null;
                if (workflowId > 0) {
                    wf = workflowService.loadWorkflow(PSGuidUtils.makeGuid(workflowId, PSTypeEnum.WORKFLOW));
                    if (wf == null)
                        log.debug("Got invalid workflow id '{}", workflowId);
                }
                if (wf == null) {
                    wf = workflowService.getDefaultWorkflow();
                }

                PSState state = wf.getInitialState();
                int communityId = (int) securityWs.loadCommunities("Default").get(0).getId();

                PSUser user = getCurrentUser();
                PSAssignmentTypeHelper helper = new PSAssignmentTypeHelper(user.getName(), user.getRoles(),
                        communityId);
                assignmentType = helper.getAssignmentType(wf, state, communityId, null);
            } catch (SQLException | PSDataServiceException throwables) {
                log.error("Error occurred determining access level of current user for type '{}', workflow id '{}'. {}",
                        type, workflowId, throwables.getMessage());
                log.debug(throwables);
                throw new WebApplicationException(throwables.getMessage());
            }

            PSAccessLevel accessLevel = new PSAccessLevel();
            accessLevel.setAccessLevel(assignmentType.name());

            return accessLevel;
        } catch (PSValidationException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * Get the workflow ID from the request, from the parent-folder or the (source) item (which may be copied from).
     * @param request the request, assumed not <code>null</code>.
     * @return the workflow ID. It may be <code>-1</code> if there is no (source) item or the parent-folder does not have the workflow ID property.
     */
	private int getWorkflowId(PSAccessLevelRequest request)
    {
        int workflowId = -1;
        IPSGuid guid = null;
        String itemId = "-1".equals(request.getItemId()) ? null : request.getItemId();
        
        if (isNotBlank(itemId))
        {           
            int contentId = ((PSLegacyGuid) idMapper.getGuid(itemId)).getContentId();
            PSComponentSummary compSum = getItemSummary(contentId);
            workflowId = compSum.getWorkflowAppId();
        }
        else
        {
            String uiPath = request.getParentFolderPath();
            if (isBlank(uiPath))
                return -1;
            
            String parentFolderPath = "/" + uiPath;
            if (uiPath.startsWith("/Assets"))
                parentFolderPath = PSAssetPathItemService.ASSET_ROOT_SUB + uiPath;
             
            guid = contentWs.getIdByPath(parentFolderPath);
            
            PSFolder parentFolder = contentWs.loadFolder(guid, false);
            String parentWorkflowId = parentFolder.getPropertyValue(IPSHtmlParameters.SYS_WORKFLOWID);
            if (isNotBlank(parentWorkflowId))
            {
                try
                {
                    workflowId = Integer.parseInt(parentWorkflowId);    
                }
                catch (NumberFormatException e)
                {
                    return -1;
                }
                
            }
        }
        return workflowId;
    }
    
    /**
     * Validates the specified user. It validates the user object according to
     * its annotation and invokes
     * {@link PSUserValidator#doValidation(PSUser, PSBeanValidationException)}
     * for additional validation.
     * 
     * @param user the user in question, not <code>null</code>.
     * @param isCreateUser if <code>true</code>, validating creating the user.
     * 
     * @throws PSBeanValidationException if failed to validate the specified
     *             user.
     */
    protected void doValidation(PSUser user, boolean isCreateUser) throws PSValidationException
    {
        log.debug("validating user {}" , user);
        user.setCreateUser(isCreateUser);
        PSUserValidator validator = new PSUserValidator(isCreateUser);

        validator.validate(user).throwIfInvalid();
    }

    /**
     * Sets to the given roles to the given user removing the old roles
     * associated to the user.
     * <p>
     * <em>This also makes sure that the default roles are always associated to the user.</em>
     * 
     * @param userName never <code>null</code> or empty.
     * @param roles never <code>null</code>.
     */
    protected void updateRoles(String userName, List<String> roles)
    {
        /*
         * We use a set to remove duplicates and add the default roles.
         */
        Set<String> updateRoles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        updateRoles.addAll(PSRoleService.DEFAULT_ROLES);
        backEndRoleMgr.setRoles(userName, updateRoles);
    }
    
    /**
     * Sets to the given roles to the given users removing the old roles
     * associated to the users.
     * <p>
     * <em>This also makes sure that the default roles are always associated to the users.</em>
     * 
     * @param userNames list of users. Never <code>null</code> or empty.
     * @param roles never <code>null</code>.
     */    
    protected void updateRoles(List<String> userNames, List<String> roles)
    {
        /*
         * We use a set to remove duplicates and add the default roles.
         */
        Set<String> updateRoles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        updateRoles.addAll(PSRoleService.DEFAULT_ROLES);
        backEndRoleMgr.setRoles(userNames, updateRoles);
    }    

    /**
     * Gets a list of roles that the specified user is a member of.
     * 
     * @param userName the user name, assumed not <code>null</code> or empty.
     * 
     * @return a list of role names, never <code>null</code> but may be empty.
     */
    protected List<String> findRoles(String userName)
    {
        return backEndRoleMgr.getRoles(userName);
    }

    /**
     * Validates that the user is already in the system.
     * 
     * @param name never <code>null</code> or empty.
     */
    protected void checkUser(String name) throws PSDataServiceException {
        PSParameterValidationUtils.rejectIfBlank("checkUser", "name", name);

        boolean found = findUsername(name) != null || userLoginDao.find(name) != null;

        if (!found)
        {
            log.error("User not found {}" ,name);
            PSValidationErrorsBuilder builder = new PSValidationErrorsBuilder(PSUser.class.getCanonicalName());
            builder.reject("no.such.user", "User not found").throwIfInvalid();
        }
    }

    /**
     * Get the names of users matching the supplied filter.
     * 
     * @param nameFilter <code>null</code> to find all users, "%" and "_" sql wildcards are supported.
     * @return Returns a list of matching subjects, never null, may be empty
     */
    private List<String> findUserNames(String nameFilter)
    {
    	
        try
        {
            List<Subject> subjects = findExistingUsers(nameFilter);
            int size = subjects == null ? 0 : subjects.size();
            List<String> userNames = new ArrayList<>(size);

            if(subjects != null){
	            for (Subject s : subjects)
	            {
	                userNames.add(getUsername(s));
	            }
	            sort(userNames);
            }else{
            	if(nameFilter == null){
            		nameFilter = "null";
            	}
            	log.warn("No users found for filter: {}" ,nameFilter);
            }
            return userNames;
        }
        catch (PSSecurityCatalogException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void sort(List<String> names)
    {
        Collator coll = Collator.getInstance();
        Collections.sort(names, coll);
    }

    /**
     * Finds existing registered users of the system. This includes external and
     * internal.
     * 
     * @param name if <code>null</code> will find all existing users, "%" and "_" sql wildcards are supported.
     * @return never <code>null</code>.
     * @throws PSSecurityCatalogException If there are any errors.
     */
    private List<Subject> findExistingUsers(String name) throws PSSecurityCatalogException
    {
        List<String> names = name == null ? null : asList(name);
        return roleMgr.findUsers(names, "Default", "backend");
    }

    /**
     * Gets the email of the provided subject
     * 
     * @param subjectName assumed not <code>null</code> or empty.
     * @return not <code>null</code> may be empty.
     * @throws PSSecurityCatalogException
     */
    @SuppressWarnings("unchecked")
    private String getSubjectEmail(String subjectName) throws PSSecurityCatalogException
    {
        String email = "";
        List<Subject> subjects = findExistingUsers(subjectName);
        if (!subjects.isEmpty())
        {
            Subject subject = subjects.get(0);
            PSSubject sub = PSJaasUtils.convertSubject(subject);
            if (sub != null)
            {
                PSAttributeList attrs = sub.getAttributes();
                PSAttribute attribute = attrs.getAttribute(EMAIL_ATTRIBUTE_NAME);
                if (attribute != null)
                {
                    List<String> attrList = attribute.getValues();
                    if (!attrList.isEmpty())
                    {
                        email =  attrList.get(0);
                    }
                }
            }
        }
        return email;
    }

    /**
     * Finds the user name.
     * 
     * @param name never <code>null</code>.
     * @return <code>null</code> if no user is found otherwise the first user
     *         name found.
     */
    private String findUsername(String name)
    {
        notNull(name);
        try
        {
            List<Subject> subjects = findExistingUsers(name);
            if (subjects != null && !subjects.isEmpty())
                return getUsername(subjects.get(0));
        }
        catch (Exception e)
        {
            throw new PSDirectoryServiceException("Error while checking for user: " + name, e);
        }
        return null;
    }

    /**
     * Gets a user name from a subject.
     * 
     * @param subject never <code>null</code>.
     * @return never <code>null</code>.
     * @throws NullPointerException if the subject does not have a proper public
     *             credential.
     */
    private String getUsername(Subject subject)
    {
        return subject.getPublicCredentials().iterator().next().toString();
    }

    private boolean isUser(Subject subject)
    {
        Set<IPSTypedPrincipal> ps = subject.getPrincipals(IPSTypedPrincipal.class);
        if (ps == null || ps.isEmpty())
            return false;
        PrincipalTypes t = ps.iterator().next().getPrincipalType();
        return t == PrincipalTypes.USER || t == PrincipalTypes.SUBJECT;
    }

    @GET
    @Path("/external/find")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSExternalUser> findUsersFromDirectoryService()
    {
        return findUsersFromDirectoryService("%");
    }

    @Override
    @GET
    @Path("/external/find/{query}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSExternalUser> findUsersFromDirectoryService(@PathParam("query") String query)
            throws PSDirectoryServiceException, PSDirectoryServiceConnectionException,
            PSDirectoryServiceDisabledException
    {
        PSUtilityService utilityService = (PSUtilityService) PSSpringBeanProvider.getBean("utilityService");

        List<Subject> subjects;
        try
        {
            subjects = roleMgr.findUsers(asList(query), PSServerConfigUpdater.DIRECTORY_SET_NAME, "directorySet", null, true);
        }
        catch (PSSecurityCatalogException e)
        {
            log.error("General directory service failure: {}" , PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            if(e.getMessage().contains("LDAP: error code 4 - Sizelimit Exceeded")){
                throw new PSDirectoryServiceException("The returned results exceeded LDAP server limit, please refine your search to get the results.");
            }
            throw new PSDirectoryServiceException(e);
        }
        catch (PSSecurityException e)
        {
            log.error("Failed to connect to Directory Server: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            throw new PSDirectoryServiceConnectionException(e);
        }
        catch (IllegalArgumentException ae)
        {
            throw new PSDirectoryServiceDisabledException("No directory service enabled:", ae);
        }
        int size = subjects == null ? 0 : subjects.size();
        List<PSExternalUser> users = new ArrayList<>(size);
        if(subjects!=null){
	        for (Subject s : subjects)
	        {
	            if (isUser(s))
	            {
	                String userName = getUsername(s);
	                users.add(new PSExternalUser(userName));
	            }
	
	        }
	        Collections.sort(users);
        }else{
        	log.warn("No users found in Directory Service matching query [{}]", query );
        }
        return users;
    }

    @Override
    @POST
    @Path("/import")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSImportedUser> importDirectoryUsers(PSImportUsers importUsers) throws PSDirectoryServiceException, PSSpringValidationException {
        PSUtilityService utilityService = (PSUtilityService) PSSpringBeanProvider.getBean("utilityService");

        try {
            PSParameterValidationUtils.rejectIfNull("importDirectoryUsers", "importUsers", importUsers);
            PSBeanValidationUtils.validate(importUsers).throwIfInvalid();
            List<PSExternalUser> users = importUsers.getExternalUsers();
            List<String> userNames = new ArrayList<>();

            for (PSExternalUser e : users) {
                userNames.add(e.getName());
                psUserManagementEvent = new PSUserManagementEvent(PSSecurityFilter.getCurrentRequest().getServletRequest(),
                        PSUserManagementEvent.UserEventActions.create,
                        PSActionOutcome.SUCCESS);
                psAuditLogService.logUserManagementEvent(psUserManagementEvent);
            }

            List<PSImportedUser> importedUsers = importUsers(userNames);

            return new PSImportedUserList(importedUsers);
        } catch (PSValidationException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * Imports a single user by setting the roles to that user. An exception
     * will generally not be thrown. Instead the return object will contain the
     * exception along with whether or not it succeeded.
     * 
     * @param name user name.
     * @return never <code>null</code>.
     * @see ImportStatus
     */
    private PSImportedUser importUser(String name)
    {

        String user = null;
        ImportStatus status = null;

        try
        {
            user = findUsername(name);
        }
        catch (Exception e)
        {
            log.error("While importing invalid  user name: {}. Error: {}", name, e.getMessage());
            log.debug(e.getMessage(),e);
            status = ImportStatus.INVALID;
        }

        if (status == null && user != null)
        {
            status = ImportStatus.DUPLICATE;
        }
        else if (status == null)
        {
            try
            {
                updateRoles(name, PSRoleService.DEFAULT_IMPORTED_USER_ROLES);
                status = ImportStatus.SUCCESS;
            }
            catch (Exception e)
            {
                log.error("Error importing user: {} {}", name, e.getMessage());
                log.debug(e);
                status = ImportStatus.ERROR;
            }
        }

        PSImportedUser u = new PSImportedUser();
        u.setName(name);
        u.setStatus(status);
        return u;
    }

    /**
     * Imports several users by setting the roles to them. An exception
     * will generally not be thrown. Instead the return object will contain the status
     * whether or not it succeeded.
     * 
     * @param names list of user names to import.
     * @return list of imported users. Never <code>null</code>.
     * @see ImportStatus
     */
    private List<PSImportedUser> importUsers(List<String> names)
    {

        String user = null;
        ImportStatus status = null;
        List<String> updateRolesUsers = new ArrayList<>();
        List<PSImportedUser> users = new ArrayList<>();

        for (String name : names)
        {
            try
            {                
                user = findUsername(name);
                
                if (status == null && user != null)
                {
                    status = ImportStatus.DUPLICATE;
                }
                else if (status == null)
                {
                    updateRolesUsers.add(name);
                }                
            }
            catch (Exception e)
            {
                log.error("While importing invalid  user name: {} Error: {}",name, e.getMessage());
                log.debug(e.getMessage(),e);
                status = ImportStatus.INVALID;
            }
            
            PSImportedUser u = new PSImportedUser();
            u.setName(name);
            u.setStatus(status);
            users.add(u);            
        }            

        try
        {
            updateRoles(names, PSRoleService.DEFAULT_IMPORTED_USER_ROLES);
            for (PSImportedUser singleUser : users)
            {
                if (singleUser.getStatus() == null)
                {
                    singleUser.setStatus(ImportStatus.SUCCESS);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error importing users", e);
            for (PSImportedUser singleUser : users)
            {
                if (singleUser.getStatus() == null)
                {
                    singleUser.setStatus(ImportStatus.ERROR);
                }
            }
        }

        return users;
    }
    
    @GET
    @Path("/external/status")
    @Override
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSDirectoryServiceStatus checkDirectoryService()
    {
        try
        {
            findUsersFromDirectoryService("a");
        }
        catch (PSDirectoryServiceException e)
        {
            return e.getStatus();
        }
        PSDirectoryServiceStatus status = new PSDirectoryServiceStatus();
        status.setStatus(ServiceStatus.ENABLED);
        return status;
    }

    /**
     * Set the system properties on this service.  This service will always use the the values provided by
     * the most recently set instance of the properties.
     * 
     * @param props the system properties
     */
    @Autowired
    public void setSystemProps(IPSSystemProperties props)
    {
        systemProps = props;
        
        // force to reset the cached data for next calls to getAccessibilityRoles()
        accessibilityRoles = null;
    }
    
    public IPSSystemProperties getSystemProps()
    {
        return systemProps;
    }

    private List<String> getAccessibilityRoles()
    {
        if (accessibilityRoles != null)
            return accessibilityRoles;
        
        accessibilityRoles = new ArrayList<>();
        String roles = systemProps.getProperty("accessibilityRoles");
        if (StringUtils.isBlank(roles))
            return accessibilityRoles;
        
        String[] array = roles.split(",");
        accessibilityRoles = new ArrayList<>(Arrays.asList(array));
        
        return accessibilityRoles;
    }
    
    protected String getCurrentUserName()
    {
        return PSWebserviceUtils.getUserName();
    }

    /**
     * Check if user has admin role.
     * 
     * @param userName
     * @return
     */
    public boolean isAdminUser(String userName)
    {
        if (userName == null)
        {
            return false;
        }
        
        return findRoles(userName).contains(IPSRoleService.ADMINISTRATOR_ROLE);
    }
    
    @Override
    public boolean isDesignUser(String userName)
    {
        if (StringUtils.isBlank(userName))
            return false;
        
        return findRoles(userName).contains(IPSRoleService.DESIGNER_ROLE);
    }

    /**
     * This is used to validate a {@link PSUser} object before updating an
     * existing user or create a new one.
     * 
     * This invocation of
     * {@link #doValidation(PSUser, PSBeanValidationException)} is indirectly
     * done by {@link PSUserService#doValidation(PSUser, boolean)}.
     */
    protected class PSUserValidator extends PSAbstractBeanValidator<PSUser>
    {
        /**
         * It is <code>true</code> if validating {@link PSUser} object for
         * creating a user.
         */
        boolean isCreateUser = false;

        PSUserValidator(boolean isCreate)
        {
            this.isCreateUser = isCreate;
        }

        @Override
        protected void doValidation(PSUser user, PSBeanValidationException e) {
           try {
               // make sure all roles exist in the system.
               List<String> allRoles = backEndRoleMgr.getRoles();

               if (PSCollectionUtils.containsIgnoringCase(SYSTEM_USERS, user.getName())) {
                   e.rejectValue("name", "user.nameRestricted",
                           "That user name is restricted for system use. Please choose a different user name");
               }
               /*
                * Lets not continue validating if it's already invalid.
                */
               if (e.hasErrors()) {
                   return;
               }

               for (String rl : user.getRoles()) {
                   if (!PSCollectionUtils.containsIgnoringCase(allRoles, rl)) {
                       String msg = "Cannot add role \"" + rl + "\" because role named \"" + rl + "\" does not exist.";
                       e.rejectValue("roles", "no.such.role", msg);
                   }
               }

               if (isCreateUser) {
                   // make sure created user not in the system
                   boolean differByCase = false;
                   String existingName = null;
                   String newName = user.getName();
                   List<PSUserLogin> users = userLoginDao.findByName(newName);
                   if (users.size() > 1) {
                       log.warn("Multiple user login entries found for name : {}", newName);
                   }

                   for (PSUserLogin usr : users) {
                       String userId = usr.getUserid();
                       if (userId.equals(newName)) {
                           existingName = userId;
                           break;
                       } else if (userId.equalsIgnoreCase(newName)) {
                           existingName = userId;
                           differByCase = true;
                           break;
                       }
                   }
                   if (existingName != null)
                       existingName = findUsername(newName);

                   if (existingName != null) {
                       String errorMsg = "Cannot create user \"" + user.getName() + "\" because a user named \""
                               + existingName + "\" already exists.";
                       if (differByCase) {
                           errorMsg += "  User names must differ by more than just case.";
                       }
                       log.debug(errorMsg);
                       e.rejectValue("name", "not.create.existing.user", errorMsg);
                   }
               } else {
                   cannotRemoveAdminRoleByYourself(user, e);
               }
           } catch (IPSGenericDao.LoadException loadException) {
               e.addSuppressed(loadException);
           }
        }

        /**
         * Make sure the user cannot remove "Admin" role from he/she own
         * profile.
         * 
         * @param user the modified user profile, assumed not <code>null</code>.
         * @param e used to collect validation errors.
         */
        private void cannotRemoveAdminRoleByYourself(PSUser user, PSBeanValidationException e)
        {
            String current = getCurrentUserName();
            if (StringUtils.isBlank(user.getName()) || !StringUtils.equalsIgnoreCase(user.getName(), current))
            {
                return;
            }

            List<String> origRoles = findRoles(user.getName());

            if (PSCollectionUtils.containsIgnoringCase(origRoles, "Admin") &&
                    (!PSCollectionUtils.containsIgnoringCase(user.getRoles(), "Admin")))
            {
                String emsg = "Cannot remove \"Admin\" role from your own profile";
                log.debug(emsg);
                e.rejectValue("roles", "cannot.remove.own.admin.role", emsg);
            }
        }
    }
    
}
