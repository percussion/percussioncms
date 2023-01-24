/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.services.menus;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE,
        region = "RXMENUACTION")
@Table(name = "RXMENUACTION")
public class PSActionMenu implements Serializable {

    @Column(name="ACTIONID")
    @Id
    private int actionId;

    @Column(name="NAME")
    private String name;

    @Column(name="DISPLAYNAME")
    private String displayName;

    @Column(name="DESCRIPTION")
    private String description;

    @Column(name="URL")
    private String url;

    @Column(name="SORTORDER")
    private int sortOrder;

    @Column(name="TYPE")
    private String type;

    @Column(name="HANDLER")
    private String handler;

    @Version()
    @Column(name="VERSION")
    private int version;

    @OneToMany(mappedBy = "menu",fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    private Set<PSActionMenuParam> parameters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "menu", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    private Set<PSActionMenuProperty> properties = new  LinkedHashSet<>();

    @OneToMany(mappedBy = "menu",fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    private Set<PSActionMenuVisibility> visibility = new  LinkedHashSet<>();


    /***
     * A virtual collection of the menu's children.
     */
    @JsonInclude()
    @Transient
    private List<PSActionMenu> children;

    public List<PSActionMenu> getChildren() {
        return children;
    }

    public void setChildren(List<PSActionMenu> children) {
        this.children = children;
    }


    public Set<PSActionMenuParam> getParameters() {
        return parameters;
    }

    public void setParameters(Set<PSActionMenuParam> parameters) {
        this.parameters = parameters;
    }

    public Set<PSActionMenuProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<PSActionMenuProperty> properties) {
        this.properties = properties;
    }

    public Set<PSActionMenuVisibility> getVisibility() {
        return visibility;
    }

    public void setVisibility(Set<PSActionMenuVisibility> visibility) {
        this.visibility = visibility;
    }

    public PSActionMenu(){

    }

    public PSActionMenu(String name, String il8nLabel, String typeMenuitem, String url, String handlerServer, int i) {
        this.name = name;
        this.displayName = il8nLabel;
        this.type = typeMenuitem;
        this.handler = handlerServer;
        this.sortOrder = i;
        this.url = url;
    }

    /***
     * Helper to handle adding params so that they are updated correctly
     * by the orm.
     *
     * @param param
     */
    public void addParameter(PSActionMenuParam param) {
        this.parameters.add(param);
        param.setMenu(this);
    }

    public void addProperty(PSActionMenuProperty prop){
        this.properties.add(prop);
        prop.setMenu(this);
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


    public static final String SYS_ACTIONID="sys_actionid";
    public static final String SYS_MODE= "sys_mode";
    public static final String SYS_CONTEXT = "sys_uicontext";
    public static final String SYS_ACTIONNAME="sys_action";

    /* "SELECT RXMENUACTION.ACTIONID,                   " +
            "RXMENUACTION.NAME, RXMENUACTION.URL, RXMENUACTION.URL" +
            "                   AS url1 , RXMODE.MODEID, RXUICONTEXT.UICONTEXTID, " +
            "                  RXMENUACTION.NAME AS name1 , RXMENUACTION.DISPLAYNAME,    " +
            "               RXMENUACTION.SORTORDER, RXMENUACTION.TYPE,              " +
            "     RXMENUACTION.HANDLER FROM RXMODE,              " +
            "     RXMODEUICONTEXTACTION, RXUICONTEXT, RXMENUACTION          " +
            "         WHERE RXMODE.MODEID = RXMODEUICONTEXTACTION.MODEID AND   " +
            "                RXUICONTEXT.UICONTEXTID =  RXMODEUICONTEXTACTION.UICONTEXTID " +
            "AND                   RXMENUACTION.ACTIONID = RXMODEUICONTEXTACTION.ACTIONID  " +
            "                 AND RXMODE.NAME =                   ':"PSXSingleParam/sys_mode"'  AND                   RXUICONTEXT.NAME =                   ':"PSXSingleParam/sys_uicontext"'                   AND RXMENUACTION.ACTIONID not in(select CHILDACTIONID                   from RXMENUACTIONRELATION) ORDER BY                   " +
            "RXMENUACTION.SORTORDER ASC
*/
}
