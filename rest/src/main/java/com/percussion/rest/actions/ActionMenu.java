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

package com.percussion.rest.actions;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ActionMenu")
@Schema(description="Represents an Action Menu")
public class ActionMenu {

    @Schema(description="The id of the menu.\n" +
            "It may be -1 if the id has not been assigned.")
    private int id;
    @Schema(description="The universally unique id of the menu, never null")
    private Guid guid;
    @Schema(description="The name of the action. Never null.",required = true)
    private String name;
    @Schema(description="Display label for this action. Can be used to set the label for\n" +
            "dynamic context menu actions.")
    private String label;
    @Schema(description="The action menu description.")
    private String description;
    @Schema(description="The action url that is relative to the document base for the page hosting the menu/.")
    private String url;
    @Schema(description="Sort rank of this Menu Action in its parent's children actions.")
    private int sortRank;

    @Schema(description="The menu type, never null or empty, must be\n" +
            "a valid menu type.", allowableValues = PSAction.TYPE_MENU + ","+ PSAction.TYPE_CONTEXTMENU +","+PSAction.TYPE_MENUITEM +",DYNAMICMENU" )
    private String menuType;
    @Schema(description="Finds whether the action to be handled by client or not. An action that\n" +
            "can not be handled by client is handled by server.")
    private String handler;

    @Schema(description=" Gets children actions of this action. Should be called only if the action\n" +
            "represents a menu as indicated by {@link #isCascadedMenu()} or\n" +
            " isDynamicMenu.\n" +
            " \n" +
            " If this action represents a menu, then a valid object is returned,\n" +
            "otherwise, it may be empty, but never null<.")
    private ActionMenuList children;
    @Schema(description="A collection of action url parameters.")
    private ActionMenuParameter[] parameters;
    @Schema(description="Set the visibility contexts that is used to control when this action will\n" +
            "be visible.")
    private ActionMenuVisibilityContext[] visibilityContexts;
    @Schema(description="Gets the list of mode-uicontexts with the action")
    private ActionMenuModeUIContext[] uiContexts;
    @Schema(description="An array of the Properties defined for this menu.   <table>\n" +
            "  <th><td>Property</td><td>Description</td><td>Allowed Values</td></th>\n" +
            "  <tr><td>AcceleratorKey</td><td>Defines accelerator key for this action</td><td></td></tr>\n" +
            "  <tr><td>MnemonicKey</td><td>Defines mnemonic key for this action</td><td></td></tr>\n" +
            "  <tr><td>ShortDescription</td><td>Defines the tooltip text for this action</td><td></td></tr>\n" +
            "  <tr><td>launchesWindow</td><td>Specifies whether to launch a new window</td><td></td></tr>\n" +
            "  <tr><td>refreshHint</td><td>Specifies what needs to be refreshed after the action is performed.</td><td>parent,root,selected</td></tr>\n" +
            "  <tr><td>SupportsMultiSelect</td><td>Specifies that the attached Command supports batch processing.</td><td></td></tr>\n" +
            "  <tr><td>SmallIcon</td><td>Defines url of the icon for this action.</td><td></td></tr>\n" +
            "  <tr><td>Description</td><td>Defines the description of this menu action.</td><td></td></tr> \n" +
            "  <tr><td>target</td><td>Defines the name of the target to which to go after the action was executed.</td><td></td></tr>\n" +
            "  <tr><td>targetStyle</td><td>Defines the name of the target style.</td><td></td></tr>\n" +
            "  </table>")
    private ActionMenuProperty[] properties;

    public ActionMenu(){}

    public void setId(int id) {
        this.id = id;
    }

    public Guid getGuid() {
        return guid;
    }

    public void setGuid(Guid guid) {
        this.guid = guid;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setChildren(ActionMenuList children) {
        this.children = children;
    }

    public void setParameters(ActionMenuParameter[] parameters) {
        this.parameters = parameters;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public void setVisibilityContexts(ActionMenuVisibilityContext[] visibilityContexts) {
        this.visibilityContexts = visibilityContexts;
    }

    public ActionMenuModeUIContext[] getUiContexts() {
        return uiContexts;
    }

    public void setUiContexts(ActionMenuModeUIContext[] uiContexts) {
        this.uiContexts = uiContexts;
    }

    public void setProperties(ActionMenuProperty[] properties) {
        this.properties = properties;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSortRank() {
        return sortRank;
    }

    public void setSortRank(int sortRank) {
        this.sortRank = sortRank;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public ActionMenuList getChildren() {
        return children;
    }

    public ActionMenuParameter[] getParameters() {
        return parameters;
    }

    public ActionMenuVisibilityContext[] getVisibilityContexts() {
        return visibilityContexts;
    }

    public ActionMenuProperty[] getProperties() {
        return properties;
    }
}
