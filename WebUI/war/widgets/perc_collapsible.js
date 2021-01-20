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

/**
   Simple-minded 'collapsible' implementation for prototyping.
   Jason Priestley
**/

(function($) {
var Collapsible = {
    _init : function() {
	var toggle = $("<span class='perc_collapsible_toggle'/>");
	var self=this;
	var toggle_on = function () { 
	    return $("<span/>")
		.append( $("<span class=\"ui-icon ui-icon-plus ui-state-disabled ui-state-default\">+</span>") )
		.append( $("<span class=\"ui-icon ui-icon-minus ui-state-default\">-</span>")
			 .hover( function() { $(this).addClass("ui-state-hover"); }, function() { $(this).removeClass("ui-state-hover");} )
			 .click( function() { 
				     self.collapse(); 
				     toggle.empty().append( toggle_off() ); } ) );
	};

	var toggle_off = function () { 
	    return $("<span/>")
		.append( $("<span class=\"ui-icon ui-icon-plus ui-state-default\">+</span>")
			 .hover( function() { $(this).addClass("ui-state-hover"); }, function() { $(this).removeClass("ui-state-hover");} )
			 .click( function() { 
				     self.uncollapse(); 
				     toggle.empty().append( toggle_on() ); } ) )
		.append( $("<span class=\"ui-icon ui-icon-minus ui-state-default ui-state-disabled\">-</span>") );
	};

	this.element.wrap( $("<div/>") );
	this.element
	    .addClass("perc_collapsible_header ui-widget ui-widget-header")
	    .prepend(toggle.append(toggle_on())).addClass("ui-helper-clearfix");
	this.element.parent()
	    .append( $("<div/>").addClass("perc_collapsible_body ui-widget ui-widget-content")
		     .append(this._getData('bodyContent')) );
	return this.element;
    },

    header : function() {
	return this.element;
    },
    body : function() {
	return this.element.next();
    },

    collapse : function () {
	this.body().hide();
    },

    uncollapse : function () {
	this.body().show();
    }
};

$.widget( "ui.perc_collapsible", Collapsible );

$.ui.perc_collapsible.getters = "header body";

})(jQuery);


