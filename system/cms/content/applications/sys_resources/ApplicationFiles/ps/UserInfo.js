/******************************************************************************
 *
 * [ ps.UserInfo.js ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.UserInfo");
dojo.require("dojo.lang.assert");
dojo.require("dojo.html");
dojo.require("dojo.widget.Menu2");
dojo.require("ps.aa");
dojo.require("ps.aa.controller");
dojo.require("ps.io.Actions");

/**
 * This is used to manage the controls that allow the user to logout and change
 * her community/locale.
 */
ps.UserInfo = new function()
{
	/**
	 * The dialog that displays the users name, roles, etc. Initialized on 
	 * first call to _getUserInfoDialog(), then never null.
	 */
	this._userInfoDlg = null;
	
	/**
	 * The full path (no protocol,server or query) to the page that renders the
	 * user info such as name and role membership.
	 */
	this.USER_INFO_PAGE_URL = "/Rhythmyx/ui/activeassembly/UserStatus.jsp";
	
   this.showInfo = function()
   {
   	var dlg = this._getUserInfoDialog();
   	dlg.show();
   }
   
   this._getUserInfoDialog = function()
   {
  		this._userInfoDlg = ps.createDialog(
      {
         id: "ps.UserInfoDlg",
         title: "User Info"
      }, "420px", "150px");
      var aaUrl = ps.aa.controller.getLinkToCurrentPage();
      aaUrl = aaUrl.replace("/Rhythmyx", "..");
      var url = ps.util.addParamToUrl(this.USER_INFO_PAGE_URL, "sys_redirecturl", 
      	escape(aaUrl));
      this._userInfoDlg.setUrl(url);
  		return this._userInfoDlg;
   }
}