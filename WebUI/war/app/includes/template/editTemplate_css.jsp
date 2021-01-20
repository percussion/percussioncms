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

<%  if(cssProfile == null || cssProfile==""){ %>
<link rel="stylesheet" type="text/css" href="../css/perc_css_editor.css" />
<link rel="stylesheet" type="text/css" href="../css/styles.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_template_layout.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_mcol.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_decoration.css" />

<!-- Stuff needed for finder to work like Editor -->
<link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css" />
<% }else if(cssProfile.equals("1x")){%>
<link rel="stylesheet" type="text/css" href="../css/perc_css_editor.css" />
<link rel="stylesheet" type="text/css" href="../css/styles.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_template_layout.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_mcol.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_decoration.css" />

<!-- Stuff needed for finder to work like Editor -->
<link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css" />
<% }else if(cssProfile.equals("2x")){%>
<link rel="stylesheet" type="text/css" href="../css/perc_css_editor.css" />
<link rel="stylesheet" type="text/css" href="../css/styles.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_template_layout.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_mcol.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_decoration.css" />

<!-- Stuff needed for finder to work like Editor -->
<link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css" />
<link rel="stylesheet" type="text/css" href="../css/placeholder_polyfill.css" />
<%}else if(cssProfile.equals("3x")){%>
<link rel="stylesheet" type="text/css" href="../css/perc_css_editor.css" />
<link rel="stylesheet" type="text/css" href="../css/styles.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_template_layout.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_mcol.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_decoration.css" />

<!-- Stuff needed for finder to work like Editor -->
<link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css" />
<link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css" />
<%}%>