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

package com.percussion.apibridge;

import com.google.gdata.data.DateTime;
import com.percussion.rest.*;
import com.percussion.rest.acls.*;
import com.percussion.rest.actions.*;
import com.percussion.rest.communities.*;
import com.percussion.rest.contenttypes.ContentType;
import com.percussion.rest.locationscheme.LocationScheme;
import com.percussion.rest.locationscheme.LocationSchemeParameter;
import com.percussion.rest.locationscheme.LocationSchemeParameterList;
import com.percussion.rest.preferences.UserPreference;
import com.percussion.rest.preferences.UserPreferenceList;
import com.percussion.rest.roles.Role;
import com.percussion.role.data.PSRole;
import com.percussion.server.PSPersistentProperty;
import com.percussion.server.PSPersistentPropertyManager;
import com.percussion.server.PSPersistentPropertyMeta;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.locking.data.PSObjectLockSummary;
import com.percussion.services.menus.*;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.*;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal;

import java.security.Principal;
import java.util.*;

public class ApiUtils {

    /***
     * Converts a system IPSGuid to a rest compatible Guid.
     * @param guid
     * @return
     */
    public static Guid convertGuid(IPSGuid guid){
        Guid ret = new Guid();

        if(guid!=null) {
            ret.setHostId(guid.getHostId());
            ret.setLongValue(guid.longValue());
            ret.setType(guid.getType());
            ret.setUntypedString(guid.toStringUntyped());
            ret.setUuid(guid.getUUID());
            ret.setStringValue(guid.toString());
        }
        return ret;
    }

    public static IPSGuid convertGuid(Guid guid){

       IPSGuid ret = PSGuidManagerLocator.getGuidMgr().makeGuid(guid.getStringValue());
       return ret;
    }

    /***
     * Converts a PSCommunity to a Community for return to the REST API
     * @param c A valid community
     * @return The converted community
     */
    public static Community convertPSCommunity(PSCommunity c) {
        Community ret = new Community(c.getId(),convertGuid(c.getGUID()),c.getName(),c.getDescription(),c.getLabel());

        ArrayList<CommunityRole> roles = new ArrayList<>();
        Iterator it = c.getRoleAssociations().iterator();
        while(it.hasNext()){
            CommunityRole assoc = new CommunityRole();
            IPSGuid roleGuid = (IPSGuid)it.next();
            assoc.setCommunityGuid(ret.getGuid());
            assoc.setCommunityid(ret.getGuid().getLongValue());
            assoc.setRoleId(roleGuid.longValue());
            assoc.setRoleGuid(convertGuid(roleGuid));
            roles.add(assoc);
        }
        ret.setRoleList(new CommunityRoleList(roles));

        return ret;
    }

    /***
     * Takes a list of Guids and returns a list of IPSGuids
     * @param ids
     * @return
     */
    public static List<IPSGuid> convertGuids(GuidList ids) {

        List<IPSGuid> ret = new ArrayList<>();

        for(Guid g : ids){
            PSGuid ps_g = new PSGuid();

            ps_g.setHostId(g.getHostId());
            ps_g.setType(g.getType());
            ps_g.setUUID(g.getUuid());
            ret.add(ps_g);
        }
        return ret;
    }

    /***
     * Takes a list of PSCommunity instances and returns a CommunityList
     * @param ps_communities
     * @return
     */
    public static CommunityList convertPSCommunities(List<PSCommunity> ps_communities) {

        ArrayList<Community> communities = new ArrayList<>();
        for(PSCommunity p : ps_communities){
            communities.add(convertPSCommunity(p));
        }
        return new CommunityList(communities);
    }

    /***
     * Takes a CommunityList and returns a list of PSCommunity objects
     * @param communities
     * @return
     */
    public static List<PSCommunity> convertCommunityList(CommunityList communities) {

        ArrayList<PSCommunity> ret = new ArrayList<>();
        for(Community c: communities){
            ret.add(convertCommunity(c));
        }

        return ret;
    }

    /***
     * Takes a Community and returns a PSCommunity
     * @param c
     * @return
     */
    public static PSCommunity convertCommunity(Community c) {
        PSCommunity p = new PSCommunity();

        p.setDescription(c.getDescription());
        p.setName(c.getName());
        p  = (PSCommunity)p.tuneClone(c.getId());

        if (c.getRoleList() != null) {

            for(CommunityRole cr:c.getRoleList() ) {
                p.addRoleAssociation(convertGuid(cr.getRoleGuid()));
            }
        }


        return p;
    }

    /***
     * Takes a community role list and returns a List of IPSGuids
     * @param roleList
     * @return
     */
    public static Collection<PSCommunityRoleAssociation> convertCommunityRoleList(CommunityRoleList roleList) {

        ArrayList<PSCommunityRoleAssociation> ret = new ArrayList<>();

        for(CommunityRole r: roleList){
            PSCommunityRoleAssociation p_r = new PSCommunityRoleAssociation(
                    convertGuid(r.getCommunityGuid()),convertGuid(r.getRoleGuid()));
            p_r.setRoleName(r.getRoleName());
            ret.add(p_r);
        }
        return ret;
    }

    public static PSTypeEnum convertObjectTypeEnum(ObjectTypeEnum type) {
        return PSTypeEnum.valueOf(type.name());
    }

    /***
     * Takes a list of PSCommunityVisibilities and returns a CommunityVisibilityList
     * @param ps_visibilities
     * @return
     */
    public static Collection<? extends CommunityVisibility> convertPSCommunityVisibilities(List<PSCommunityVisibility> ps_visibilities) {

        CommunityVisibilityList ret = null;
        ArrayList<CommunityVisibility> visibilities = new ArrayList<>();
        Iterator it = ps_visibilities.iterator();
        while(it.hasNext()){
            PSCommunityVisibility pv = (PSCommunityVisibility)it.next();
            visibilities.add(convertPSCommunityVisibility(pv));
        }

        return new CommunityVisibilityList(visibilities);
    }

    public static CommunityVisibility convertPSCommunityVisibility(PSCommunityVisibility pv) {

        CommunityVisibility ret = new CommunityVisibility(pv.getGUID().longValue(),convertGuid(pv.getGUID()));

        ArrayList<ObjectSummary> visObjects = new ArrayList<>();
        for(PSObjectSummary s : pv.getVisibleObjects()){
            visObjects.add(convertPSObjectSummary(s));
        }

        ret.setVisibleObjects(new ObjectSummaryList(visObjects));
        return ret;
    }


    public static ObjectSummary convertPSObjectSummary(PSObjectSummary s) {
        ObjectSummary ret = new ObjectSummary();
        ret.setDescripion(s.getDescription());
        ret.setGuid(convertGuid(s.getGUID()));
        ret.setId(s.getId());
        ret.setLabel(s.getLabel());
        ret.setName(s.getName());
        ret.setType(ObjectTypeEnum.valueOf(s.getType()));
        ret.setObjectLocked(s.isObjectLocked());
        ret.setLockSummary(convertPSObjectLockSummary(s.getLocked()));
        ret.setPermissions(convertPSUserAccessLevel(s.getPermissions()));

        return ret;
    }

    /***
     * Takes a PSUserAccessLevel and returns a UserAccessLevel
     * @param permissions
     * @return
     */
    public  static UserAccessLevel convertPSUserAccessLevel(PSUserAccessLevel permissions) {
        UserAccessLevel ret = new UserAccessLevel();

        HashSet perms = new HashSet();
        for(PSPermissions p : permissions.getPermissions()){
            perms.add(convertPSPermissions(p));
        }
        ret.setPermissions(new PermissionList(perms));
        return ret;
    }

    /***
     * Takes a PSPermissions and returns a Permissions
     * @param p
     * @return
     */
    public static Permissions convertPSPermissions(PSPermissions p) {

        return Permissions.valueOf(p.name());
    }

    /***
     * Takes a PSObjectLockSummary and returns an ObjectLockSummary
     * @param locked
     * @return
     */
    public static ObjectLockSummary convertPSObjectLockSummary(PSObjectLockSummary locked) {

        ObjectLockSummary sum = new ObjectLockSummary();
        if(locked!=null) {
            sum.setCallerAccessTime(DateTime.now().toString());
            sum.setLocker(locked.getLocker());
            sum.setRemainingTime(locked.getRemainingTime());
            sum.setSession(locked.getSession());
        }
        return sum;
    }

    public static LocationScheme copyLocationScheme(IPSLocationScheme scheme){
        LocationScheme ret = new LocationScheme();

        ret.setDescription(scheme.getDescription());
        ret.setName(scheme.getName());
        ret.setSchemeId(ApiUtils.convertGuid(scheme.getGUID()));
        ret.setContentTypeId(scheme.getContentTypeId());
        ret.setTemplateId(scheme.getTemplateId());
        ret.setContext(convertGuid(scheme.getGUID()));
        ret.setLocationSchemeGenerator(scheme.getGenerator());
        ret.setParameters(convertLocationSchemeParameters(scheme));

        return ret;
    }

    /****
     * Takes a location scheme and returns a parameter list
     * @param scheme
     * @return
     */
    public static LocationSchemeParameterList convertLocationSchemeParameters(IPSLocationScheme scheme) {

    LocationSchemeParameterList ret = null;
    if(scheme != null){

        List<String> p_params = scheme.getParameterNames();
        ArrayList<LocationSchemeParameter> params = new ArrayList<>();
        for(String s : p_params){
            LocationSchemeParameter p = new LocationSchemeParameter();
            p.setName(s);
            p.setSequence(scheme.getParameterSequence(s));
            p.setType(scheme.getParameterType(s));
            p.setValue(scheme.getParameterValue(s));
            params.add(p);
        }
        ret = new LocationSchemeParameterList(params);
    }
    return ret;
    }

    /***
     * Takes a PSRole and returns a Role.
     * @param p_role
     * @return
     */
    public static Role convertRole(PSRole p_role) {

        Role ret = null;

        if(p_role != null){
            ret = new Role();
            ret.setDescription(p_role.getDescription());
            ret.setHomePage(p_role.getHomepage());
            ret.setName(p_role.getName());
            if(p_role.getUsers()!=null)
                ret.setUsers(p_role.getUsers());
        }


        return ret;
    }


    /***
     * Takes a Role and Converts it to a PSRole
     * @param role
     * @return
     */
    public static PSRole convertRole(Role role) {
        PSRole ret = new PSRole();

        ret.setDescription(role.getDescription());
        ret.setHomepage(role.getHomePage());
        ret.setName(role.getName());
        ret.setUsers(role.getUsers());

        return ret;
    }

    /***
     * Given an ACL returns an IPSAcl
     * @param acl
     * @return
     */
    public static PSAclImpl convertAcl(Acl acl) {

        PSAclImpl p_acl = new PSAclImpl();

        p_acl.setId(acl.getId());
        p_acl.setDescription(acl.getDescription());
        p_acl.setName(acl.getName());
        p_acl.setGUID(convertGuid(acl.getGuid()));
        p_acl.setObjectId(acl.getObjectId());
        p_acl.setObjectType(acl.getObjectType());
        p_acl.setEntries(convertAclEntries(acl.getAclEntries()));

        return p_acl;
    }

    /***
     * Takes an AclEntry List and returns a collection of PSAclEntryImpls
     * @param aclEntries
     * @return
     */
    public static Collection<PSAclEntryImpl> convertAclEntries(AclEntryList aclEntries) {

        HashSet<PSAclEntryImpl> ret = new HashSet<>();

        for(AclEntry entry:aclEntries){
            PSAclEntryImpl p_entry = new PSAclEntryImpl();

            p_entry.setId(entry.getId());
            p_entry.setName(entry.getName());
            p_entry.setPrincipal(convertPrincipal(entry.getPrincipal()));
            p_entry.setType(IPSTypedPrincipal.PrincipalTypes.valueOf(entry.getType().getName()));
            for(UserAccessLevel p : entry.getPermissions()){
                p_entry.addPermission(convertPermissions(p));
            }
            p_entry.setAclId(entry.getAclId());

            ret.add(p_entry);
        }

        return ret;
    }

    public static PSAccessLevelImpl convertPermissions(UserAccessLevel p) {
        PSAccessLevelImpl p_a = new PSAccessLevelImpl();

        p_a.setPermission(PSPermissions.valueOf(p.getPermission().name()));
        p_a.setId(p.getId());
        return p_a;
    }

    /***
     * Given a rest Principal returns an rx Principal
     * @param principal
     * @return
     */
    public  static Principal convertPrincipal(com.percussion.rest.acls.Principal principal) {
        Principal ret = new Principal() {
            @Override
            public String getName() {
                return principal.getName();
            }
        };

        return ret;
    }

    public static AclList convertAcls(List<IPSAcl> loadAcls) {
        ArrayList<Acl> acls = new ArrayList<>();
        for(IPSAcl p_acl : loadAcls){
            acls.add(convertAcl((PSAclImpl)p_acl));
        }
        
        return new AclList(acls);

    }

    public  static Acl convertAcl(PSAclImpl p_acl) {
        Acl ret = null;

        if(p_acl != null) {
            ret = new Acl();
            ret.setName(p_acl.getName());
            ret.setGuid(convertGuid(p_acl.getGUID()));
            ret.setId(p_acl.getId());
            ret.setObectGuid(convertGuid(p_acl.getObjectGuid()));
            ret.setObjectId(p_acl.getObjectId());
            ret.setAclEntries(convertAclEntries(p_acl.getEntries()));
        }
        return ret;
    }

    /***
     * Takes a lost of IPSAclEntry and returns a list of ACLEntries
     * @param p_entries
     * @return
     */
    public static AclEntryList convertAclEntries(Collection<IPSAclEntry> p_entries) {

        ArrayList<AclEntry> entries = new ArrayList<>();
        for(IPSAclEntry p_e : p_entries){
            entries.add(convertAclEntry((PSAclEntryImpl)p_e));
        }
        return  new AclEntryList(entries);
    }

    public  static AclEntry convertAclEntry(PSAclEntryImpl p_e) {
        
        AclEntry ret = new AclEntry();
        
        ret.setName(p_e.getName());
        ret.setAclId(p_e.getAclId());
        ret.setId(p_e.getId());
        ret.setPermissions(convertPermissions(p_e.getPermissions()));


        ret.setType(convertPrincipalType(p_e.getTypedPrincipal()));
        
        return ret;        
    }

    public static UserAccessLevelList convertPermissions(Collection<PSAccessLevelImpl> permissions) {

        ArrayList<UserAccessLevel> access = new ArrayList<>();
        for(PSAccessLevelImpl p_a : permissions){
            UserAccessLevel u = new UserAccessLevel();
            u.setId(p_a.getId());
            u.setPermission(convertPSPermissions(p_a.getPermission()));
            access.add(u);
        }
        return new UserAccessLevelList(access);
    }

    public  static TypedPrincipal convertPrincipalType(IPSTypedPrincipal typedPrincipal) {
         TypedPrincipal ret = new TypedPrincipal();

         ret.setName(typedPrincipal.getName());

         return ret;

    }

    public static List<IPSAcl> convertAcls(AclList aclList) {
        ArrayList<IPSAcl> p_acls = new ArrayList<>();

        for(Acl a : aclList){
            PSAclImpl p_a = new PSAclImpl();
            p_a.setObjectType(a.getObjectType());
            p_a.setObjectId(a.getObjectId());
            p_a.setGUID(convertGuid(a.getGuid()));
            p_a.setName(a.getName());
            p_a.setId(a.getId());
            p_a.setEntries(convertAclEntries(a.getAclEntries()));
            p_a.setDescription(a.getDescription());
            p_acls.add(p_a);
        }
        return p_acls;
    }

    public static GuidList convertGuids(Collection<IPSGuid> p_guids) {

        ArrayList<Guid> guids = new ArrayList<>();

        for(IPSGuid p_g : p_guids){
            Guid g = new Guid();
            g.setUntypedString(p_g.toStringUntyped());
            g.setUuid(p_g.getUUID());
            g.setType(p_g.getType());
            g.setStringValue(p_g.toString());
            g.setLongValue(p_g.longValue());
            g.setHostId(p_g.getHostId());
            guids.add(g);
        }
        return new GuidList(guids);
    }

    public static UserPreference convertPSPersistentProperty(PSPersistentProperty prop) {

        UserPreference up = new UserPreference();

        up.setCategory(prop.getCategory());
        up.setContext(prop.getContext());
        up.setExtraParam(prop.getExtraParam());
        up.setName(prop.getName());
        up.setUserName(prop.getUserName());
        up.setValue(prop.getValue());

        return up;

    }

    public static PSPersistentProperty convertUserPreference(UserPreference u){
        PSPersistentProperty p = new PSPersistentProperty(u.getUserName(),
                u.getName(),u.getCategory(),u.getContext(),u.getValue());

        return p;
    }

    public static UserPreferenceList convertUserProperties(Collection userProperties) {

        Iterator properties = userProperties.iterator();
        ArrayList<UserPreference> up = new ArrayList<>();
        while(properties.hasNext()){
            PSPersistentProperty prop = (PSPersistentProperty) properties.next();
            up.add(ApiUtils.convertPSPersistentProperty(prop));
        }
        return new UserPreferenceList(up);
    }

    public static Collection<PSPersistentProperty> convertUserPreferences(UserPreferenceList prefs){
            ArrayList<PSPersistentProperty> ret = new ArrayList<>();
            for(UserPreference up : prefs){
                ret.add(ApiUtils.convertUserPreference(up));
            }
            return ret;
    }

    public static UserPreference convertUserProperty(PSPersistentProperty p) {
        UserPreference u = new UserPreference();
        u.setCategory(p.getCategory());
        u.setContext(p.getContext());
        u.setExtraParam(p.getExtraParam());
        u.setName(p.getName());
        u.setUserName(p.getUserName());
        return u;
    }

    public static IPSTypedPrincipal convertPrincipalType(TypedPrincipal owner) {
        PSTypedPrincipal ret = new PSTypedPrincipal(owner.getName(), IPSTypedPrincipal.PrincipalTypes.valueOf(owner.getType().name()));
        return ret;
    }


    public static PSPersistentPropertyMeta convertUserPreferenceToMeta(UserPreference pref) {

        return  new PSPersistentPropertyMeta(PSPersistentPropertyManager.SYS_USER,
                pref.getName(),
                pref.getCategory(), 1,true, true,
        null);
    }

    public static List<ActionMenu> convertPSActionMenuList(List<PSActionMenu> actionMenus) {
        ArrayList ret = new ArrayList<ActionMenu>();
        for(PSActionMenu pa : actionMenus){
            ret.add(convertPSActionMenu(pa));
        }

        return ret;
    }

    public static Object convertPSActionMenu(PSActionMenu pa) {
        ActionMenu ret = new ActionMenu();

        ret.setId(pa.getActionId());
        ret.setName(pa.getName());
        ret.setDescription(pa.getDescription());
        ret.setLabel(pa.getDisplayName());
        ret.setUrl(pa.getUrl());
        ret.setSortRank(pa.getSortOrder());
        ret.setMenuType(pa.getType());
        ret.setHandler(pa.getHandler());

        ArrayList<ActionMenuProperty> props = new ArrayList<>();
        for(PSActionMenuProperty pap: pa.getProperties()){
            ActionMenuProperty p = new ActionMenuProperty();
            p.setActionId(pap.getPrimaryKey().getActionId());
            p.setName(pap.getPrimaryKey().getPropertyName());
            p.setDescription(pap.getDescription());
            p.setValue(pap.getValue());
            props.add(p);
        }
        ActionMenuProperty[] prop_array = new ActionMenuProperty[props.size()];
        ret.setProperties(props.toArray(prop_array));

        ArrayList<ActionMenuParameter> params = new ArrayList<>();
        for(PSActionMenuParam psparam : pa.getParameters()){
            ActionMenuParameter p = new ActionMenuParameter();
            p.setDescription(psparam.getDescription());
            p.setName(psparam.getActionParamPK().getParamName());
            p.setValue(psparam.getParamValue());
            params.add(p);
        }
        ActionMenuParameter[] param_array = new ActionMenuParameter[params.size()];
        ret.setParameters(params.toArray(param_array));

        ArrayList<ActionMenuVisibilityContext> vis = new ArrayList<>();
        for(PSActionMenuVisibility v : pa.getVisibility()) {

            ActionMenuVisibilityContext vc = new ActionMenuVisibilityContext();

            vc.setDescription(v.getPrimaryKey().getDescription());
            vc.setValue(v.getPrimaryKey().getValue());
            vc.setUiContext(copyUIContext(v.getContext()));
            vis.add(vc);
        }

        ActionMenuVisibilityContext[] ctxes = new ActionMenuVisibilityContext[vis.size()];
        ret.setVisibilityContexts(vis.toArray(ctxes));

        return ret;
    }

    private static UIContext copyUIContext(PSUiContext context) {

        UIContext ctx = new UIContext();

        ctx.setId(context.getId());
        ctx.setDescription(context.getDescription());
        ctx.setDisplayName(context.getDisplayName());
        ctx.setName(context.getName());
        return ctx;
    }

    public static ContentType convertContentType(IPSCatalogSummary s) {
        ContentType  ret = new ContentType();

        ret.setName(s.getName());
        ret.setDescription(s.getDescription());
        ret.setLabel(s.getLabel());
        ret.setGuid(convertGuid(s.getGUID()));
        return ret;
    }
}
