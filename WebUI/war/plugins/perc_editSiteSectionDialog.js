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

/**
 * Edit Site Section Metadata
 */
(function($){



    $.perc_editSiteSectionDialog = function() {
        function openDialog(site, callback) {
            var taborder = 30;
            var v;

            var dialog;

            var self = this;

            dialog = $("<div>" +
                //+ "<p class='perc-field-error' id='perc-save-error'></p><br/>"
                "<form action='' method='GET'> " +
                "<div style='background: #E6E6E9; padding-top: 5px; padding-right: 10px; text-align:right;'><label>" + I18N.message("perc.ui.general@Denotes Required Field") + "</label></div>" +
                '<div class="fieldGroup">' +
                "<div id='perc-site-general-container'>" +
                "<label for='perc-site-hostname' class='perc-required-field'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Site hostname" ) + ":</label> <br/> " +
                "<input type='text' class='required' aria-required='true' tabindex='0' id='perc-site-hostname' maxlength='80' name='site_hostname'/> <br/>" +
                "<label for='perc-page-title-link' class='perc-required-field'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Home page link text" ) + ":</label> <br/> " +
                "<input type='text' aria-required='true' tabindex='0' id='perc-page-title-link' class='required' name='page_title_link'/><br/> " +
                "<label for='perc-site-desc'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Description" ) + ":</label> <br/> " +
                "<textarea type='text' tabindex='0' id='perc-site-desc' name='site_desc'></textarea><br/> " +
                "<label for='perc-site-navigation-cssclassnames'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Navigation class names") + ":</label> <br/> " +
                "<input type='text' tabindex='0' id='perc-site-navigation-cssclassnames' maxlength='255' name='perc-site-navigation-cssclassnames'/> <br/>" +
                "<label for='perc-site-pagefile-extention-default'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Default file extension") + ":</label> <br/> " +
                "<input type='text' tabindex='0' id='perc-site-pagefile-extention-default' maxlength='30' name='perc-site-pagefile-extention-default' value='html'/> <br/>" +

                '<input type="checkbox" id="perc-enable-mobile-preview" name="perc-enable-mobile-preview" style="width:20px" tabindex=0"/>' +
                '<label for="perc-enable-mobile-preview">' + I18N.message("perc.ui.editSiteSectionDialog.label@Enable Mobile Preview") + '</label> <br/>' +

                '<input type="checkbox" id="perc-enable-canonical-url" name="perc-enable-canonical-url" style="width:20px" tabindex="0"/>' +
                '<label for="perc-enable-canonical-url">' + I18N.message("perc.ui.editSiteSectionDialog.label@Generate Canonical URLs") + '</label> <br/>' +

                '<input type="radio" id="perc-canonical-url-dist-sections" name="perc-canonical-url-dist" value="sections" style="width:20px" disabled tabindex="0"/>&nbsp;&nbsp;' +
                '<label for="perc-canonical-url-dist-sections" id="perc-canonical-url-dist-sections-label">' + I18N.message("perc.ui.editSiteSectionDialog.label@Sections") + '</label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' +
                '<input type="radio" id="perc-canonical-url-dist-pages" name="perc-canonical-url-dist" value="pages" checked style="width:20px" disabled tabindex="0"/>&nbsp;&nbsp;' +
                '<label for="perc-canonical-url-dist-pages" id="perc-canonical-url-dist-pages-label">' + I18N.message("perc.ui.editSiteSectionDialog.label@Landing Pages") + '</label> <br/>' +
                "<label for='perc-arrow-key-message'(>" + I18N.message( "perc.ui.users@ArrowKeys") + ")</label> <br/> " +

                '<input type="checkbox" id="perc-replace-canonical-tags" name="perc-replace-canonical-tags" style="width:20px" disabled tabindex="0"/>' +
                '<label for="perc-replace-canonical-tags" id="perc-replace-canonical-tags-label">' + I18N.message("perc.ui.editSiteSectionDialog.label@Replace existing Canonical Tags if present") + '</label> <br/>' +

                "<input type='checkbox' style=\"width:20px\" tabindex='0' id='perc-site-gen-sitemap' name='perc-site-gen-sitemap'/>" +
                "<label for='perc-site-gen-sitemap' id='perc-site-gen-sitemap-label'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Generate sitemap") + "</label> <br/> " +

                "<input type='checkbox' style=\"width:20px\" tabindex='0' id='perc-site-gen-sitemap-exclude-image' name='perc-site-gen-sitemap-exclude-image'/>" +
                "<label for='perc-site-gen-sitemap-exclude-image' id='perc-site-gen-sitemap-label-exclude-image'>" + "Exclude Image" + "</label> <br/> " +

                "<label id='perc-site-protocol-label' for='perc-site-protocol'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Protocol" ) + ":</label> <br/> " +
                // refactored from radio buttons to drop downs
                "<select tabindex='0' name='perc-site-protocol' id='perc-site-protocol'>" +
                "     <option id='perc-site-protocol-http' value='http'>http</option>" +
                "     <option id='perc-site-protocol-https' value='https' selected>https</option>" +
                "</select> <br/> " +
                "<label for='perc-site-default-document' id='perc-site-default-document-label'>" + I18N.message( "perc.ui.editSiteSectionDialog.label@Default document") + ":</label> <br/> " +
                "<input type='text' tabindex='0' id='perc-site-default-document' maxlength='255' name='perc-site-default-document' value='index.html'/> <br/>" +


                '</div>' +
                '</div>' +

                '<div class="fieldGroup">' +
                "<div id='perc-site-users-container' style='display:inline-block;' >" +
                "<label id='perc-site-folder-permission-label' for='perc-site-folder-permission'>" + I18N.message( "perc.ui.folderPropsDialog.label@Permission" ) + ":</label> <br/> " +
                // refactored from radio buttons to drop downs
                "<select name='perc-site-folder-permission' tabindex='0' id='perc-site-folder-permission'>" +
                "     <option id='perc-folder-permission-option-read' value='" + $.PercFolderHelper().PERMISSION_READ  + "'>" + I18N.message( "perc.ui.folderPropsDialog.permissionValue@Read"  ) + "</option>" +
                "     <option id='perc-folder-permission-option-write' value='" + $.PercFolderHelper().PERMISSION_WRITE + "'>" + I18N.message( "perc.ui.folderPropsDialog.permissionValue@Write" ) + "</option>" +
                "</select>" +
                "<input type='hidden' id='perc-site-id' name='site_id'/>" +
                // render the list editor widget in the following div
                "<div id='perc-site-permission-users'>" +
                "</div>" +
                '</div></div>' +


                getSecureSectionFieldGroups() +

                "<div class='ui-layout-south'>" +
                "<div id='perc_buttons' style='z-index: 100;'></div>" +
                "</div>" +
                "</form> </div>").perc_dialog( {
                resizable : false,
                title: I18N.message( "perc.ui.editSiteSectionDialog.title@Site Preferences" ),
                modal: true,
                dragStart:function() {
                    $("div.ac_results").hide();
                },
                closeOnEscape : false,
                percButtons:   {
                    "Save":     {
                        click: function()   {
                            //Check if the user turn off the secure site and warn him
                            var secureSiteCheck = $("#perc-enable-site-security");
                            if (secureSiteCheck.data('originalValue') && !secureSiteCheck.is(':checked'))
                            {
                                var selectedSite = $('#perc-site-hostname').val();
                                var options = {
                                    id:"perc_disable_site_security",
                                    title: I18N.message( "perc.ui.edit.site.section.dialog@Disable Site Security" ),
                                    question:"<span id='perc-delete-dialog-warning'>" + I18N.message( "perc.ui.edit.site.section.dialog@Warning" ) + "</span> <BR\> " +
                                        "<span id='perc-warning-red'>" + I18N.message( "perc.ui.edit.site.section.dialog@Disable Security" ) + "'"+selectedSite+"' </span><BR\> " +
                                        "<strong>" + I18N.message( "perc.ui.edit.site.section.dialog@Remove Files" ) + "</strong><BR\><BR\> " +
                                        "<span id='perc-delete-warn-msg'>" + I18N.message( "perc.ui.edit.site.section.dialog@Stop Security" ) + "<BR\> <BR\> " +
                                        I18N.message( "perc.ui.edit.site.section.dialog@Continue Question" ) + "</span>",
                                    cancel: function(){},
                                    success: function(){_save();},
                                    width: "500px",
                                    modal: true
                                };
                                $.perc_utils.confirm_dialog(options);
                            }
                            else {
                                _save();
                            }
                        },
                        id: "perc-site-save"
                    },
                    "Cancel":       {
                        click: function()   {
                            _remove();
                        },
                        id: "perc-site-cancel"
                    }
                },
                id: "perc-edit-site-section-dialog",
                width: 520,
                height: 618
            });

            function _save(){
                // get users from the list widget and build a comma separated string
                var writePrincipals = "";
                if(self.listEditor.isEnabled()) {
                    var users = self.listEditor.getListItems();
                    for(u=0; u<users.length; u++)
                        writePrincipals += u===0?users[u]:","+users[u];
                }

                // add a hidden field to the form to pass the users
                dialog.find('form').append("<input type='hidden' name='writePrincipals' value='"+writePrincipals+"'>");

                // Clear the white spaces from the class names field
                var cssClassNamesField = dialog.find('#perc-site-navigation-cssclassnames');
                cssClassNamesField.val(cssClassNamesField.val().replace(/ +/g, " "));

                // submit the form
                _submit();
            }

            function err( str ) {
                $('#perc-save-error').text( str ).effect('pulsate', {times: 1});
            }
            function _remove()  {
                dialog.remove();
                callback("cancel");
            }
            function _submit()  {
                dialog.find('form').trigger("submit");
            }
            // A private helper method to group the fields and create collapsible sections
            function _addFieldGroups() {
                var dialog = $('#perc-edit-site-section-dialog');
                var fieldGroups = [
                    { groupName : "perc-site-general-container", groupLabel : I18N.message("perc.ui.editSiteSectionDialog.label@Site")},
                    { groupName : "perc-site-users-container", groupLabel : I18N.message("perc.ui.editSiteSectionDialog.label@Users")},
                    { groupName : "perc-site-security-container", groupLabel : I18N.message("perc.ui.editSiteSectionDialog.label@Security")},
                    { groupName : "perc-site-membership-container", groupLabel : I18N.message("perc.ui.editSiteSectionDialog.label@Membership")}
                ];
				var tabindexArr = [0, 0, 0, 0];
				var counter = 0;

                $.each(fieldGroups, function(index) {
                    // Create HTML markup with the groupName minimizer/maximizer and
                    // insert it before the 1st field in each group
                    var minmaxClass = (index === 0) ? "perc-items-minimizer" : "perc-items-maximizer";
                    var groupHtml =
                        "<div class='perc-section-header'>" +
                        "<div class='perc-section-label' data-group='" + this.groupName + "'>" +
                        "<span tabindex='"+tabindexArr.at(counter)+"' title='"+this.groupLabel+"' class='perc-min-max " + minmaxClass + "' ></span>" + this.groupLabel +
                        "</div>" +
                        "</div>";
						counter++;
                    dialog.find('#' + this.groupName).before(groupHtml);
                    // The first group will be the only one expanded (hide all others)
                    if(index !== 0){
                        dialog.find('#' + this.groupName).hide();
                    }
                });

                // Bind collapsible event
                dialog.find(".perc-section-label").off("click").on("click",function() {
                    var self = $(this);
                    self.find(".perc-min-max")
                        .toggleClass('perc-items-minimizer')
                        .toggleClass('perc-items-maximizer');
                    dialog.find('#' + self.attr('data-group')).toggle();
                });

				dialog.find(".perc-section-label").off("keydown").on("keydown",function() {
                    if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
					}
                });
            }


            /**
             * Function to handle click on browse button.
             * @param dlgTitle String Title that will show the page chooser
             * @param inputElemName Strig Input elem that will be selected by jQuery to apply the page path
             */
            function handleBrowseButtonClick(dlgTitle, inputElemName) {
                $.perc_browser({
                    title: dlgTitle,
                    displayed_containers: "CustomSite",
                    customRootSite : site,
                    save_class: 'perc-save',
                    asset_name: I18N.message( "Selected Page:" ),
                    new_asset_option: false,
                    new_folder_opt: false,
                    selectable_object: "leaf",
                    on_save: function(spec, closer, show_error) {
                        var pagePath = spec.path;
                        var chopStartPosition;
                        pagePath = pagePath.replace("/Sites/", "");
                        chopStartPosition = pagePath.indexOf("/");
                        pagePath = pagePath.substring(chopStartPosition);
                        $('[name="' + inputElemName + '"]').val(pagePath);
                        closer();
                    }
                });
            }

            function _handleSecurityOption(){
                var useSecurity = $("#perc-enable-site-security");
                if (useSecurity.is(':checked')){
                    $("#perc-site-login-page").addClass("required");
                    $("#perc-site-login-page-label").addClass('perc-required-field');

                }
                else {
                    $("#perc-site-login-page").removeClass("required");
                    $("#perc-site-login-page-label").removeClass('perc-required-field');
                }
                // Add Browse buttons functionality
                $("#perc-site-login-page-browse").off("click").on("click",function(){
                    var dlgTitle = 'Select login page';
                    var inputElemName = 'perc-site-login-page';
                    handleBrowseButtonClick(dlgTitle, inputElemName );
                });

				 $("#perc-site-login-page-browse").off("keydown").on("keydown",function(){
                     if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
					}
                });
            }

            // Enabling/disabling fields depending on check/uncheck option
            function _handleCanonicalOption(){
                if ($("#perc-enable-canonical-url").is(':checked')){
                    $("#perc-site-protocol").addClass("required");
                    $("#perc-site-default-document").addClass("required");
                    $("#perc-canonical-url-dist-sections").addClass("required");
                    $("#perc-canonical-url-dist-pages").addClass("required");
                    $("#perc-site-protocol-label").addClass('perc-required-field');
                    $("#perc-site-default-document-label").addClass('perc-required-field');
                    $("#perc-canonical-url-dist-sections-label").addClass('perc-required-field');
                    $("#perc-canonical-url-dist-pages-label").addClass('perc-required-field');

                    $("#perc-site-protocol").prop("disabled", false);
                    $("#perc-site-default-document").prop("disabled", false);
                    $("#perc-canonical-url-dist-sections").prop("disabled", false);
                    $("#perc-canonical-url-dist-pages").prop("disabled", false);
                    $("#perc-replace-canonical-tags").prop("disabled", false);
                }
                else {
                    $("#perc-site-protocol").removeClass("required");
                    $("#perc-site-default-document").removeClass("required");
                    $("#perc-canonical-url-dist-sections").removeClass("required");
                    $("#perc-canonical-url-dist-pages").removeClass("required");
                    $("#perc-site-protocol-label").removeClass('perc-required-field');
                    $("#perc-site-default-document-label").removeClass('perc-required-field');
                    $("#perc-canonical-url-dist-sections-label").removeClass('perc-required-field');
                    $("#perc-canonical-url-dist-pages-label").removeClass('perc-required-field');

                    $("#perc-site-protocol").prop("disabled", true);
                    $("#perc-site-default-document").prop("disabled", true);
                    $("#perc-canonical-url-dist-sections").prop("disabled", true);
                    $("#perc-canonical-url-dist-pages").prop("disabled", true);
                    $("#perc-replace-canonical-tags").prop("disabled", true);
                }
            }

            function getSecureSectionFieldGroups(){
                var fieldGroups = '';
                // render Login and Membership site configuration fields
                fieldGroups = '<div class="fieldGroup">' +
                    '<div id="perc-site-security-container">' +
                    '<input type="checkbox" id="perc-enable-site-security" name="perc-enable-site-security" style="width:20px" tabindex="0"/>' +
                    '<label for="perc-enable-site-security">' + I18N.message("perc.ui.editSiteSectionDialog.label@Use site security") + '</label> <br/>' +
                    '<label for="perc-site-login-page" id="perc-site-login-page-label">' + I18N.message("perc.ui.editSiteSectionDialog.label@Login page") + ':</label><br/>' +
                    '<table class="perc-site-login-page-table"><tr><td><input id="perc-site-login-page" name="perc-site-login-page" readonly = "readonly" maxlength="2000" type="text" tabindex="0"/></td><td style="vertical-align: top;"> <span id="perc-site-login-page-browse" tabindex="0">Browse</span></td></tr></table>' +
                    '</div>' +
                    '</div>' +

                    '<div class="fieldGroup">' +
                    '<div id="perc-site-membership-container">' +
                    '<label for="perc-site-registration-confirmation-page">' + I18N.message("perc.ui.editSiteSectionDialog.label@Confirmation page") + ':</label><br/>' +
                    '<input id="perc-site-registration-confirmation-page" name="perc-site-registration-confirmation-page" maxlength="2000" type="text" tabindex="0"/><span id="perc-site-registration-confirmation-page-browse" tabindex="0">Browse</span><br/>' +
                    '<label for="perc-site-reset-pw-request-page">' + I18N.message("perc.ui.editSiteSectionDialog.label@Password reset request page") + ':</label><br/>' +
                    '<input id="perc-site-reset-pw-request-page" name="perc-site-reset-pw-request-page" maxlength="2000" type="text" tabindex="0"/><span id="perc-site-reset-pw-request-page-browse" tabindex="0">Browse</span><br/>' +
                    '<label for="perc-site-reset-password-page">' + I18N.message("perc.ui.editSiteSectionDialog.label@Password reset page") + ':</label><br/>' +
                    '<input id="perc-site-reset-password-page" name="perc-site-reset-password-page" maxlength="2000" type="text" tabindex="0"/><span id="perc-site-reset-password-page-browse" tabindex="0">Browse</span><br/>' +
                    '</div>' +
                    '</div>';
                return fieldGroups;
            }

            v = dialog.find('form').validate({
                errorClass: "perc-field-error",
                validClass: "perc-field-success",
                wrapper: "p",
                validateHiddenFields: false,
                debug: false,
                submitHandler: function(form) {
                    dialog.remove();
                    callback("ok", $(form,this).serializeArray());
                }
            });

            $("#perc-enable-canonical-url").on("click",function(){_handleCanonicalOption();});

            $("#perc-site-registration-confirmation-page-browse").on("click",function() {
                var dlgTitle = 'Select registration confirmation page';
                var inputElemName = 'perc-site-registration-confirmation-page';
                handleBrowseButtonClick(dlgTitle, inputElemName );
            });
            $("#perc-site-reset-pw-request-page-browse").on("click",function() {
                var dlgTitle = 'Select password reset request page';
                var inputElemName = 'perc-site-reset-pw-request-page';
                handleBrowseButtonClick(dlgTitle, inputElemName );
            });
            $("#perc-site-reset-password-page-browse").on("click",function() {
                var dlgTitle = 'Select password reset page';
                var inputElemName = 'perc-site-reset-password-page';
                handleBrowseButtonClick(dlgTitle, inputElemName );
            });
            $("#perc-enable-site-security").off("click").on("click", function() {
                _handleSecurityOption();
            });



            $("#perc-site-registration-confirmation-page-browse").on("keydown",function() {
               if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
					}
            });
            $("#perc-site-reset-pw-request-page-browse").on("keydown",function() {
               if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
					}
            });
            $("#perc-site-reset-password-page-browse").on("keydown",function() {
               if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
					}
            });
            $("#perc-enable-site-security").off("keydown").on("keydown", function() {
                if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
					}
            });

            $("#perc-site-login-page").on('paste', function(evt){evt.preventDefault();})
                .on('keypress keydown', function(evt){
                    if(evt.keyCode === 46 || evt.keyCode === 8 )
                    {
                        var field = evt.target;
                        field.value = "";
                        evt.preventDefault();
                        return;
                    }
                    if(evt.charCode === 0 || typeof(evt.charCode) == 'undefined')
                        return;
                    evt.preventDefault();
                });

            _addFieldGroups();

            var $hostname = $('#perc-site-hostname');
            var $titlelink = $('#perc-page-title-link');
            var $siteid = $('#perc-site-id');
            var $description = $('#perc-site-desc');
            var $loginPage = $('#perc-site-login-page');

            var $registrationConfirmationPage = $('#perc-site-registration-confirmation-page');
            var $pwResetRequestPage = $('#perc-site-reset-pw-request-page');
            var $pwResetPage = $('#perc-site-reset-password-page');
            var secureSiteCheck = $("#perc-enable-site-security");

            $.perc_filterField($hostname, $.perc_textFilters.HOSTNAME);

            // Get site info and populate form
            $.PercSiteService.getSiteProperties(site, function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    var props = result.SiteProperties;
                    $hostname.val(props.name);
                    $titlelink.val(props.homePageLinkText);
                    // Bind the filter to the class names field and retrieve its value (clear multiple whitespaces)
                    $.perc_filterFieldText($('#perc-site-navigation-cssclassnames'), $.perc_autoFillTextFilters.IDNAMECDATAALPHA, ' ');
                    $("#perc-site-navigation-cssclassnames").val(props.cssClassNames);
                    $siteid.val(props.id);
                    $loginPage.val(props.loginPage);

                    $registrationConfirmationPage.val(props.registrationConfirmationPage);
                    $pwResetRequestPage.val(props.resetRequestPasswordPage);
                    $pwResetPage.val(props.resetPage);

                    $.perc_filterFieldText($('#perc-site-pagefile-extention-default'), $.perc_autoFillTextFilters.IDNAMECDATAALPHA, ' ');
                    $("#perc-site-pagefile-extention-default").val(props.defaultFileExtention);

                    //Set the Canonical option.
                    if (props.isCanonical)
                        $("#perc-enable-canonical-url").prop("checked", true);
                    else
                        $("#perc-enable-canonical-url").prop("checked", false);
                    // Enabling/disabling other "canonical" options depending on check/uncheck option
                    _handleCanonicalOption();

                    //Set the protocol.
                    if (props.siteProtocol) $("#perc-site-protocol").val(props.siteProtocol);

                    $.perc_filterField($('#perc-site-default-document'), $.perc_textFilters.URL);
                    if (props.defaultDocument) $("#perc-site-default-document").val(props.defaultDocument);

                    //Generate sitemap
                    if(props.generateSiteMap){
                        $("#perc-site-gen-sitemap").prop("checked", true);
                    }else{
                        $("#perc-site-gen-sitemap").prop("checked", false);
                    }

                    //Generate sitemap exclude images
					if(props.generateSiteMapOptions.generateSitemapExcludeImage == true){
                        $("#perc-site-gen-sitemap-exclude-image").prop("checked", true);
                    }else{
                        $("#perc-site-gen-sitemap-exclude-image").prop("checked", false);
                    }

                    // Set Canonical dist option
                    if (props.canonicalDist) $("[name='perc-canonical-url-dist'][value='" + props.canonicalDist + "']").prop("checked",true);

                    //Set the Canonical replace option.
                    if (props.isCanonicalReplace)
                        $("#perc-replace-canonical-tags").prop("checked", true);
                    else
                        $("#perc-replace-canonical-tags").prop("checked", false);

                    //Set mobile preview enable
                    if (props.mobilePreviewEnabled){
                        $("#perc-enable-mobile-preview").prop("checked", true);
                    }else{
                        $("#perc-enable-mobile-preview").prop("checked", false);
                    }


                    if(typeof(props.description) != 'undefined')
                        $description.val(props.description);

                    if(props.folderPermission.accessLevel === $.PercFolderHelper().PERMISSION_READ)
                        $("#perc-folder-permission-option-read").prop("selected",true);
                    else
                        $("#perc-folder-permission-option-write").prop("selected",true);

                    // get the writePrincipals from the JSON object comming from the server
                    // make sure it's an array even if it's a single object
                    var writePrincipals = [];
                    if(props.folderPermission.writePrincipals) {
                        writePrincipals = props.folderPermission.writePrincipals;
                        writePrincipals = Array.isArray(writePrincipals) ? writePrincipals : [writePrincipals];
                    }

                    //keep the original value to determine in the onsave event if the user change the secure site option.
                    secureSiteCheck.data('originalValue', props.isSecure);
                    //Set the security option.
                    if (props.isSecure)
                        secureSiteCheck.prop('checked', true);
                    else
                        secureSiteCheck.prop('checked',false);

                    _handleSecurityOption();

                    // build an array of users from the principals to populate the list editor widget
                    var users = [];
                    for(u=0; u<writePrincipals.length; u++)
                        users[u] = writePrincipals[u].name;

                    $.PercUserService.getUsers(function(status, usersJson) {
                        if(status === $.PercUserService.STATUS_ERROR) {
                            $.PercUserView.alertDialog('Error while loading users', usersJson);
                            return;
                        }

                        // render the list editor widget in the div declared earlier in the dialog
                        self.listEditor = $.PercListEditorWidget({
                            "container" : "perc-site-permission-users",
                            "items"     : users,
                            "results"   : $.perc_utils.convertCXFArray(usersJson.UserList.users),
                            // element that will toggle enable/disable of this component
                            "toggler"   : $("#perc-site-folder-permission"),

                            // values of toggler that enable this component
                            "toggleron" : [$.PercFolderHelper().PERMISSION_READ],

                            // values of toggler that disable this component
                            "toggleroff": [$.PercFolderHelper().PERMISSION_WRITE, $.PercFolderHelper().PERMISSION_ADMIN],

                            "title1" : I18N.message( "perc.ui.folderPropsDialog.title@User Properties" )+":",
                            "title2" : I18N.message( "perc.ui.folderPropsDialog.permissionValue@Write" )
                        });
                    });
                }
                else
                {
                    $.perc_utils.alert_dialog({title: 'Error', content: result});
                }
            });

        }// End open dialog

        return {"open": openDialog};

    };

})(jQuery);
