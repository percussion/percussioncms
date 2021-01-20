<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<% String cssProfile = request.getParameter("profile");
    if(cssProfile == null || cssProfile==""){ %>
<link type="text/css" href="../css/layout.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="../css/percFinder.css" />
<link rel="stylesheet" type="text/css" href="../css/percDialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_default.css" />
<link rel="stylesheet" type="text/css" href="../css/PercDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercWorkflowDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercViewDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercMultiDropDown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercDatetimePicker.css"/>
<link rel="stylesheet" type="text/css" href="../css/FontAwesome/css/font-awesome.min.css" />
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercWizard/PercWizard.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercDataTable/PercDataTable.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderTree/PercFinderTree.css" />

<link rel="stylesheet" type="text/css" href="../css/jquery.autocomplete.css" />
<link rel="stylesheet" type="text/css" href="../css/superfish.css"/>
<link type="text/css" rel="stylesheet" href="../css/jquery.dropdown.css" />
<% }else if(cssProfile.equals("1x")){%>
<link rel="stylesheet" type="text/css" href="../jslib/profiles/1x/jquery/plugins/jquery-superfish/css/superfish.css"/>
<link rel="stylesheet" type="text/css" href="../jslib/profiles/1x/jquery/plugins/jquery-layout/layout-default.css"/>
<link type="text/css" rel="stylesheet" href="../jslib/profiles/1x/jquery/plugins/jquery-dropdown/jquery.dropdown.css" />
<link rel="stylesheet" type="text/css" href="../css/percFinder.css" />
<link rel="stylesheet" type="text/css" href="../css/percDialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_default.css" />
<link rel="stylesheet" type="text/css" href="../css/PercDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercWorkflowDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercViewDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercMultiDropDown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercDatetimePicker.css"/>
<link rel="stylesheet" type="text/css" href="../css/FontAwesome/css/font-awesome.min.css" />
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercWizard/PercWizard.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercDataTable/PercDataTable.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderTree/PercFinderTree.css" />
<link rel="stylesheet" type="text/css" href="../css/jquery.autocomplete.css" />
<% }else if(cssProfile.equals("2x")){%>
<link rel="stylesheet" type="text/css" href="../jslib/profiles/2x/jquery/plugins/jquery-superfish/css/superfish.css"/>
<link rel="stylesheet" type="text/css" href="../jslib/profiles/2x/jquery/plugins/jquery-layout/layout-default.css"/>
<link type="text/css" rel="stylesheet" href="../jslib/profiles/2x/jquery/plugins/jquery-dropdown/jquery.dropdown.css" />
<link rel="stylesheet" type="text/css" href="../css/percFinder.css" />
<link rel="stylesheet" type="text/css" href="../css/percDialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_default.css" />
<link rel="stylesheet" type="text/css" href="../css/PercDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercWorkflowDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercViewDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercMultiDropDown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercDatetimePicker.css"/>
<link rel="stylesheet" type="text/css" href="../jslib/profiles/2x/libraries/fontawesome/css/FontAwesome.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercWizard/PercWizard.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercDataTable/PercDataTable.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderTree/PercFinderTree.css" />

<link rel="stylesheet" type="text/css" href="../css/jquery.autocomplete.css" />
<%}else if(cssProfile.equals("3x")){%>
<link rel="stylesheet" type="text/css" href="../jslib/profiles/3x/jquery/plugins/jquery-superfish/css/superfish.css"/>
<link rel="stylesheet" type="text/css" href="../jslib/profiles/3x/jquery/plugins/jquery-layout/layout-default.css"/>
<link type="text/css" rel="stylesheet" href="../jslib/profiles/3x/jquery/plugins/jquery-dropdown/jquery.dropdown.css" />
<link type="text/css" rel="stylesheet" href="../jslib/profiles/3x/libraries/bootstrap/bootstrap.css" />
<link rel="stylesheet" type="text/css" href="../css/percFinder.css" />
<link rel="stylesheet" type="text/css" href="../css/percDialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_default.css" />
<link rel="stylesheet" type="text/css" href="../css/PercDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercWorkflowDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercViewDropdown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercMultiDropDown.css"/>
<link rel="stylesheet" type="text/css" href="../css/PercDatetimePicker.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercWizard/PercWizard.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercDataTable/PercDataTable.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderListView/PercFinderListView.css"/>
<link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderTree/PercFinderTree.css" />

<link rel="stylesheet" type="text/css" href="../css/jquery.autocomplete.css" />
<%}%>