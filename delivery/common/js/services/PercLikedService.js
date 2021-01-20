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

(function($){
	
    let likesServiceUrl = "/perc-comments-services/likes/";
	
	const constants = {
        // Set Constants
        "PERC_TYPE_PAGE"                          : "PAGE",
        "PERC_TYPE_COMMENT"                       : "COMMENT",
        "PERC_TYPE_IMAGE"                         : "IMAGE",
		
		"PERC_ONE_YEAR"                            : 365
	};
	
    /**
     * Increments the number of times this item has been liked.
     * @param callback (function) function to call with response from server
     */
    let likeThis = function(callback) {
		if("undefined" === typeof (callback) || null === callback) {return false};
		
		let likePathname = likeId(window.location.pathname);

		like(this.siteName, likePathname, constants.PERC_TYPE_PAGE, callback);
    };

    /**
     * Increments the number of times the given item has been liked.
     * @param site (string) domain/host where this item is hosted
     * @param id (string) unique identifier of item within the host
     * @param type (string) type of the item, for now it's only "PAGE", "COMMENTS" and "IMAGE"
     * @param callback (function) function to call with response from server
     */
    var like = function(site, id, type, callback) {
        let likeServiceUrl = likesServiceUrl + "like/" + site + "/" + type + "/" + id;
        $.PercServiceUtils.makeXdmJsonRequest(null, likeServiceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                let data = {url : site, totalLikes : results.data};
				setThisLiked(true, data.url);
                callback(true, data);
            }
            else{
                callback(false, null);
            }
        });
    };
    /**
     * Decrements the number of times this item has been liked.
     * @param callback (function) function to call with response from server
     */
    var unlikeThis = function(callback) {
		if("undefined" === typeof (callback) || null === callback){ return false};
	
		let likePathname = likeId(window.location.pathname);
		/*if(likePathname[0] == "/")
		{
			likePathname = likePathname.substring(1);
		}*/
		
		unlike(this.siteName, likePathname, constants.PERC_TYPE_PAGE, callback);
    };
	/**
     * Decrements the number of times this item has been liked.
     * @param site (string) domain/host where this item is hosted
     * @param id (string) unique identifier of item within the host
     * @param type (string) type of the item, for now it's only "PAGE", "COMMENTS" and "IMAGE"
     * @param callback (function) function to call with response from server
     */
	let unlike = function(site, id, type, callback) {
        let likeServiceUrl = likesServiceUrl + "unlike/" + site + "/" + type + "/" + id;
        $.PercServiceUtils.makeXdmJsonRequest(null, likeServiceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                let data = {url : site, totalLikes : results.data};
				setThisLiked(false, data.url);
                callback(true, data);
            }
            else{
                callback(false, null);
            }
        });
    };

    /**
     * Gets the total number of times this page has been liked.
     * @param callback (function) function to call with response from server
     */
    let getTotalLikesForThisPage = function(callback) {
		let likePathname = likeId(window.location.pathname);

		getTotalLikes(this.siteName, likePathname, constants.PERC_TYPE_PAGE, callback);
    };
        
	/**
     * Gets the total number of times the given path has been liked.
     * @param site (string) domain/host where this item is hosted
     * @param id (string) unique identifier of item within the host
     * @param type (string) type of the item, for now it's only "PAGE", "COMMENTS" and "IMAGE"
     * @param callback (function) function to call with response from server
     */
    let getTotalLikes = function(site, id, type, callback) {
        var likeServiceUrl = likesServiceUrl + "total/" + site + "/" + type + "/" + id;
        $.PercServiceUtils.makeXdmJsonRequest(null, likeServiceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                let data = {url : site, totalLikes : results.data};
                callback(true, data);
            }
            else{
                callback(false, null);
            }
        });
    };
	/**
	 * Returns true or false for whether or not the current page is liked.
	 * @return isLiked boolean true if liked, boolean false if not liked.
	 */
	let isThisLiked = function() {
		
		let pathname = window.location.pathname;
		return isLiked(window.location.protocol + "//" + this.siteName + likeId(pathname));
	};
	/**
	 * Returns true or false for whether or not the selected page is liked.
	 * @param url fully qualified URI (not url) of page to query for.
	 * @return isLiked boolean true if liked, boolean false if not liked.
	 */
	let isLiked = function(url) {
		if("undefined" === typeof (url) || null === url) {
            return false;
        }
		
		let likedState = $.cookie(url);
		if (null === likedState) {
            return false;
        }

		// Not strictly necessary, but I belive it makes for more readable code.
		if('true' === likedState.toLowerCase()) {
            return true;
        }
		else {
            return false;
        }
	};
	
	/**
	 * Resolves /index.*, /home.*, /default.* or  / to always be /  and if anything else just return the provided pathname
	 * @param pathname
	 * @return resolved pathname
	 */
	let likeId = function(pathname){
		let id = "";
		if(pathname.lastIndexOf('.') !== -1)
        {
            id = pathname.substring(0, pathname.lastIndexOf('.'));
        }
		if(("/" === pathname) || ("/index" === pathname) ||("/index" === id) || ("/home" === pathname) || ("/default" === pathname))
        {
            id = "/";
            return id;    
        }
        else
        {   
            let indexValue = pathname.lastIndexOf('index');
            if(indexValue !== -1) {
                id = pathname.substring(0, indexValue);               
            }
            else {    
                id = pathname;
            }
        }
        return id;
	};
	
	/**
	 * Sets whether or not the current page is liked.
	 * @param liked boolean true if liked, boolean false if not liked.
     * @param siteName
	 * @return success boolean true if setting of liked succeded, boolean false if it failed.
	 */
	let setThisLiked = function(liked,siteName) {
		if("undefined" === typeof (liked) || null === liked) {
            return false;
        }
		return setLiked(window.location.protocol + "//" + siteName + likeId(window.location.pathname), liked);
	};
	/**
	 * Sets whether or not the selected page is liked.
	 * @param url fully qualified URI (not url) of page to query for.  URI MUST NOT CONTAIN QUERY STRING OR HASH VALUE.
	 * @param liked boolean true if liked, boolean false if not liked.
	 * @return success boolean true if setting of liked succeded, boolean false if it failed.
	 */
	var setLiked = function(url, liked) {
		if("undefined" === typeof (url) || null === url){
		    return false;
        }
		if("undefined" === typeof (liked) || null === liked) {
		    return false;
        }
		let likedString = null;
		if(true === liked) {
            likedString = "true";
        }
		else if(false === liked) {
            likedString = null;
        }
		
		$.cookie(url,likedString, {"expires" : constants.PERC_ONE_YEAR});
		
	};
    const PercLikedService = {
        "constants" : constants,
        "like" : like,
        "likeThis" : likeThis,
        "unlike" : unlike,
        "unlikeThis" : unlikeThis,
		"getTotalLikesForThisPage" : getTotalLikesForThisPage,
		"getTotalLikes" : getTotalLikes,
		"isThisLiked" : isThisLiked,
		"isLiked" : isLiked,
		"setThisLiked" : setThisLiked,
		"setLiked" : setLiked		
    };
    
    $.PercLikedService = PercLikedService;
})(jQuery);

