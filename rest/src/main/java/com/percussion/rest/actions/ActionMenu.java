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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rest.actions;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.rest.Guid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ActionMenu")
@ApiModel(description="Represents an Action Menu")
public class ActionMenu {

    @ApiModelProperty(notes="The id of the menu.\n" +
            "It may be -1 if the id has not been assigned.")
    private int id;
    @ApiModelProperty(notes="The universally unique id of the menu, never null")
    private Guid guid;
    @ApiModelProperty(notes="The name of the action. Never null.",required = true)
    private String name;
    @ApiModelProperty(notes="Display label for this action. Can be used to set the label for\n" +
            "dynamic context menu actions.")
    private String label;
    @ApiModelProperty(notes="The action menu description.")
    private String description;
    @ApiModelProperty(notes="The action url that is relative to the document base for the page hosting the menu/.")
    private String url;
    @ApiModelProperty(notes="Sort rank of this Menu Action in its parent's children actions.")
    private int sortRank;

    @ApiModelProperty(notes="The menu type, never null or empty, must be\n" +
            "a valid menu type.", allowableValues = PSAction.TYPE_MENU + ","+ PSAction.TYPE_CONTEXTMENU +","+PSAction.TYPE_MENUITEM +",DYNAMICMENU" )
    private String menuType;
    @ApiModelProperty(notes="Finds whether the action to be handled by client or not. An action that\n" +
            "can not be handled by client is handled by server.")
    private String handler;

    @ApiModelProperty(notes=" Gets children actions of this action. Should be called only if the action\n" +
            "represents a menu as indicated by {@link #isCascadedMenu()} or\n" +
            " isDynamicMenu.\n" +
            " \n" +
            " If this action represents a menu, then a valid object is returned,\n" +
            "otherwise, it may be empty, but never null<.")
    private ActionMenuList children;
    @ApiModelProperty(notes="A collection of action url parameters.")
    private ActionMenuParameter[] parameters;
    @ApiModelProperty(notes="Set the visibility contexts that is used to control when this action will\n" +
            "be visible.")
    private ActionMenuVisibilityContext[] visibilityContexts;
    @ApiModelProperty(notes="Gets the list of mode-uicontexts with the action")
    private ActionMenuModeUIContext[] uiContexts;
    @ApiModelProperty(notes="An array of the Properties defined for this menu.   <table>\n" +
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
