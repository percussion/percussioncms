<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2021 Percussion Software, Inc.
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
    String mainNavTab = request.getParameter("mainNavTab");
    String thesite = request.getParameter("site");
    if(thesite == null)
        thesite = "";
    isDebug = "true".equals(debug);
    boolean isAdmin = (Boolean)request.getAttribute("isAdmin");
    boolean isDesigner = (Boolean)request.getAttribute("isDesigner");
    String wdgBuilderParam = (String)request.getAttribute("isWidgetBuilderActive");
    boolean isWdgActive = "true".equalsIgnoreCase(wdgBuilderParam.trim());
%>
<nav class="navbar perc-nav-title perc-navbar-dark navbar-expand-xs fixed-top">
    <span class="float-left perc-page-title"><%=currentPage%></span>
    <button aria-hidden title="Toggle Navigation" role="button" id="percToggleNavigation" class="btn perc-nav-toggle perc-btn-primary mt-2 mt-md-0">
        <i aria-hidden class="fas fa-ellipsis-v fa-2x"></i>
    </button>
</nav>
<div aria-label='<i18n:message key="perc.ui.navMenu.admin@Navigation Options"/>' role="navigation" tabindex="-1" aria-modal="true" style="display:none;" id="percNavigationBody" class="perc-nav-dialog">
    <div class="container mt-5">
        <div class="row">
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_HOME" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="fas fa-home fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.home@Home"/></span>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_DASHBOARD" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="perc-nav-icon fas fa-tachometer-alt fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.dashboard@Dashboard"/></span>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_EDITOR" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="perc-nav-icon fas fa-edit fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.webmgt@Editor"/></span>
                </div>
            </div>
            <% if (isAdmin || isDesigner) { %>
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_SITE_ARCH" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="perc-nav-icon fas fa-sitemap fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.architecture@Architecture"/></span>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_DESIGN" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="perc-nav-icon fas fa-paint-brush fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.design@Design"/></span>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_PUBLISH" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="perc-nav-icon fas fa-newspaper fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.publish@Publish"/></span>
                </div>
            </div>
            <% } %><% if (isAdmin) { %>
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_WORKFLOW" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="perc-nav-icon fas fa-users-cog fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.admin@Administration"/></span>
                </div>
            </div>
            <% } %>
            <% if (isWdgActive && (isAdmin || isDesigner)) { %>
            <div class="col-6 col-md-4 col-lg-3">
                <div role="button" tabindex="0" data-navmgr="VIEW_WIDGET_BUILDER" class="perc-nav-item perc-actions-menu-item text-center">
                    <i aria-hidden class="perc-nav-icon fas fa-wrench fa-5x fa-fw"></i>
                    <span class="perc-nav-description"><i18n:message key="perc.ui.navMenu.admin@Widget Builder"/></span>
                </div>
            </div>
            <% } %>
        </div>
        <div class="row">
            <div class="col">
                <hr>
            </div>
        </div>
        <section id="percNavMenuButtons">
            <div class="row">
                <div class="col">
                    <button title="<i18n:message key="perc.ui.change.pw@Change Password" />" role="button" class="perc-toggle-password btn btn-block perc-btn-inverse perc-nav-menu-button text-left">
                  <span>
                    <i aria-hidden class="fas fa-user-cog  fa-fw"></i>
                  </span>
                        <span class="align-middle">
                      &nbsp;&nbsp;<i18n:message key="perc.ui.change.pw@Change Password" />
                    </span>
                    </button>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <a href="https://help.percussion.com" target="_blank" rel="noopener noreferrer" title="<i18n:message key="perc.ui.common.label@Help" />" role="button" class="btn btn-block perc-btn-inverse perc-nav-menu-button text-left">
                  <span>
                    <i aria-hidden class="fas fa-question-circle fa-fw"></i>
                  </span>
                        <span class="align-middle">
                    &nbsp;&nbsp;<i18n:message key="perc.ui.common.label@Help" />
                  </span>
                    </a>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <a href="https://community.percussion.com/" target="_blank" rel="noopener noreferrer" title="<i18n:message key="perc.ui.common.label@Percussion Community" />" role="button" class="btn btn-block perc-btn-inverse perc-nav-menu-button text-left">
                  <span>
                    <i aria-hidden class="fas fa-hands-helping fa-fw"></i>
                  </span>
                        <span class="align-middle">
                    &nbsp;&nbsp;<i18n:message key="perc.ui.common.label@Percussion Community" />
                  </span>
                    </a>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <button title="<i18n:message key="perc.ui.common.label@About" />" role="button" class="perc-toggle-about btn btn-block perc-btn-inverse perc-nav-menu-button text-left">
                  <span>
                    <i aria-hidden class="fas fa-info-circle fa-fw"></i>
                  </span>
                        <span class="align-middle">
                    &nbsp;&nbsp;<i18n:message key="perc.ui.common.label@About" />
                  </span>
                    </button>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <a href="/Rhythmyx/logout" title="<i18n:message key="perc.ui.common.label@Log Out" />" role="button" class="btn btn-block perc-btn-inverse perc-nav-menu-button text-left">
                  <span>
                    <i aria-hidden class="fas fa-sign-out-alt fa-fw"></i>
                  </span>
                        <span class="align-middle">
                    &nbsp;&nbsp;<i18n:message key="perc.ui.common.label@Log Out" />
                  </span>
                    </a>
                </div>
            </div>
        </section>
    </div>
</div>

<%@ page import="java.util.Calendar" %>
<%@ page import="com.percussion.server.PSServer" %>
<%
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    String ver = PSServer.getVersionString();
%>
<div role="dialog" tabindex="-1" aria-modal="true" style="display:none;" id="percAbout">
    <div role="document" class="container d-flex h-100">
        <div class="row align-items-center">
            <div class="col text-left">
                <span class="perc-dialog-title"><i18n:message key="perc.ui.change.pw@About Percussion" /></span>
                <hr class="perc-divider-white mb-5">
                <p><img class="perc-about-logo" src="/cm/images/logos/percussion-logo-white.png" alt="Percussion Logo" title="Percussion"></p>
                <p><%= ver %></p>
                <p>Copyright &copy; <%= year %> by Percussion&nbsp;Software&nbsp;Inc.</p>
                <p><a title="Percussion Software" href="https://www.percussion.com" target="_blank" rel="noopener noreferrer" class="perc-about-link">https://www.percussion.com</a></p>
                <p><button class="perc-toggle-about perc-close-about btn btn-block perc-confirmation-button perc-confirmation-button-dark"><i18n:message key="perc.ui.common.label@Close" /></button></p>
            </div>
        </div>
    </div>
</div>

<div role="dialog" tabindex="-1" aria-modal="true" style="display: none;" id="percPasswordDialogTarget">
    <div role="document" class="container d-flex h-100">
        <div class="row align-items-center">
            <div class="col-12">
                <div id="percChangePasswordTarget">
                    <div class="row">
                        <div class="col-12 perc-password-form-container">
                            <span class="perc-dialog-title"><i18n:message key="perc.ui.change.pw@Change Password" /></span>
                            <hr class="perc-divider-white mb-5">
                            <csrf:form>
                                <div class="form-group">
                                    <label for="percNewPassword"><i18n:message key="perc.ui.change.pw@Enter New Password" /></label>
                                    <input name="password" type="password" class="form-control perc-change-password-field" id="percNewPassword" placeholder='<i18n:message key="perc.ui.change.pw@Enter New Password" />'>
                                </div>
                                <div class="form-group">
                                    <label for="percConfirmNewPassword"><i18n:message key="perc.ui.change.pw@Confirm New Password" /></label>
                                    <input name="confirmPassword" type="password" class="form-control perc-change-password-field" id="percConfirmNewPassword" placeholder='<i18n:message key="perc.ui.change.pw@Confirm New Password" />'>
                                </div>
                            </csrf:form>
                            <p class="perc-change-password-error"><span>&nbsp;<span></p>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-6">
                            <button role="button" class="perc-toggle-password btn btn-block perc-confirmation-button perc-confirmation-button-dark"><i18n:message key="perc.ui.common.label@Cancel" /></button>
                        </div>
                        <div class="col-6">
                            <button role="button" class="btn btn-block perc-confirmation-button perc-confirmation-button-dark perc-submit-password-change"><i18n:message key="perc.ui.common.label@Submit" /></button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
