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

package com.percussion.server;


/**
 * The IPSHttpErrors inteface is provided as a convenient mechanism
 * for accessing the various HTTP related error codes. The HTTP error code
 * ranges are:
 * <UL>
 * <LI>1xx: Informational - Request received, continuing process</LI>
 * <LI>2xx: Success - The action was successfully received, understood,
 * and accepted</LI>
 * <LI>3xx: Redirection - Further action must be taken in order to
 * complete the request</LI>
 * <LI>4xx: Client Error - The request contains bad syntax or cannot be
 * fulfilled</LI>
 * <LI>5xx: Server Error - The server failed to fulfill an apparently
 * valid request</LI>
 * </UL>
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSHttpErrors {

   /**
    * The client may continue with its request. This interim response is
    * used to inform the client that the initial part of the request has
    * been received and has not yet been rejected by the server. The client
    * SHOULD continue by sending the remainder of the request or, if the
    * request has already been completed, ignore this response. The server
    * MUST send a final response after the request has been completed.
    */
   public static final int HTTP_CONTINUE                  = 100;

   /**
    * The server understands and is willing to comply with the client's
    * request, via the Upgrade message header field (section 14.41), for a
    * change in the application protocol being used on this connection. The
    * server will switch protocols to those defined by the response's
    * Upgrade header field immediately after the empty line which
    * terminates the 101 response.
    * The protocol should only be switched when it is advantageous to do
    * so.  For example, switching to a newer version of HTTP is
    * advantageous over older versions, and switching to a real-time,
    * synchronous protocol may be advantageous when delivering resources
    * that use such features.
    */
   public static final int HTTP_SWITCHING_PROTOCOLS      = 101;

   /**
    * The request has succeeded. The information returned with the response
    * is dependent on the method used in the request, for example:
    * GET  an entity corresponding to the requested resource is sent in the
    *      response;
    * HEAD the entity-header fields corresponding to the requested resource
    *      are sent in the response without any message-body;
    * POST an entity describing or containing the result of the action;
    * TRACE an entity containing the request message as received by the end
    *      server.
    */
   public static final int HTTP_OK                        = 200;
        
   /**
    * The request has been fulfilled and resulted in a new resource being
    * created. The newly created resource can be referenced by the URI(s)
    * returned in the entity of the response, with the most specific URL
    * for the resource given by a Location header field. The origin server
    * MUST create the resource before returning the 201 status code. If the
    * action cannot be carried out immediately, the server should respond
    * with 202 (Accepted) response instead.
    */
   public static final int HTTP_CREATED                  = 201;

   /**
    * The request has been accepted for processing, but the processing has
    * not been completed. The request MAY or MAY NOT eventually be acted
    * upon, as it MAY be disallowed when processing actually takes place.
    * There is no facility for re-sending a status code from an
    * asynchronous operation such as this.
    * The 202 response is intentionally non-committal. Its purpose is to
    * allow a server to accept a request for some other process (perhaps a
    * batch-oriented process that is only run once per day) without
    * requiring that the user agent's connection to the server persist
    * until the process is completed. The entity returned with this
    * response SHOULD include an indication of the request's current status
    * and either a pointer to a status monitor or some estimate of when the
    * user can expect the request to be fulfilled.
    */
   public static final int HTTP_ACCEPTED                  = 202;

   /**
    * The returned metainformation in the entity-header is not the
    * definitive set as available from the origin server, but is gathered
    * from a local or a third-party copy. The set presented MAY be a subset
    * or superset of the original version. For example, including local
    * annotation information about the resource MAY result in a superset of
    * the metainformation known by the origin server. Use of this response
    * code is not required and is only appropriate when the response would
    * otherwise be 200 (OK).
    */
   public static final int HTTP_NON_AUTHORITATIVE_INFO   = 203;
   
   /**
    * The server has fulfilled the request but there is no new information
    * to send back. If the client is a user agent, it SHOULD NOT change its
    * document view from that which caused the request to be sent. This
    * response is primarily intended to allow input for actions to take
    * place without causing a change to the user agent's active document
    * view. The response MAY include new metainformation in the form of
    * entity-headers, which SHOULD apply to the document currently in the
    * user agent's active view.
    * The 204 response MUST NOT include a message-body, and thus is always
    * terminated by the first empty line after the header fields.
    */
   public static final int HTTP_NO_CONTENT               = 204;

   /**
    * The server has fulfilled the request and the user agent SHOULD reset
    * the document view which caused the request to be sent. This response
    * is primarily intended to allow input for actions to take place via
    * user input, followed by a clearing of the form in which the input is
    * given so that the user can easily initiate another input action. The
    * response MUST NOT include an entity.
    */
   public static final int HTTP_RESET_CONTENT            = 205;

   /**
    * The server has fulfilled the partial GET request for the resource.
    * The request must have included a Range header field (section 14.36)
    * indicating the desired range. The response MUST include either a
    * Content-Range header field (section 14.17) indicating the range
    * included with this response, or a multipart/byteranges Content-Type
    * including Content-Range fields for each part. If multipart/byteranges
    * is not used, the Content-Length header field in the response MUST
    * match the actual number of OCTETs transmitted in the message-body.
    * A cache that does not support the Range and Content-Range headers
    * MUST NOT cache 206 (Partial) responses.
    */
   public static final int HTTP_PARTIAL_CONTENT            = 207;
   
   /**
    * The requested resource corresponds to any one of a set of
    * representations, each with its own specific location, and agent-
    * driven negotiation information (section 12) is being provided so that
    * the user (or user agent) can select a preferred representation and
    * redirect its request to that location.
    * Unless it was a HEAD request, the response SHOULD include an entity
    * containing a list of resource characteristics and location(s) from
    * which the user or user agent can choose the one most appropriate. The
    * entity format is specified by the media type given in the Content-
    * Type header field. Depending upon the format and the capabilities of
    * the user agent, selection of the most appropriate choice may be
    * performed automatically.  However, this specification does not define
    * any standard for such automatic selection.
    * If the server has a preferred choice of representation, it SHOULD
    * include the specific URL for that representation in the Location
    * field; user agents MAY use the Location field value for automatic
    * redirection.  This response is cachable unless indicated otherwise.
    */
   public static final int HTTP_MULTIPLE_CHOICES         = 300;

   /**
    * The requested resource has been assigned a new permanent URI and any
    * future references to this resource SHOULD be done using one of the
    * returned URIs. Clients with link editing capabilities SHOULD
    * automatically re-link references to the Request-URI to one or more of
    * the new references returned by the server, where possible. This
    * response is cachable unless indicated otherwise.
    * If the new URI is a location, its URL SHOULD be given by the Location
    * field in the response. Unless the request method was HEAD, the entity
    * of the response SHOULD contain a short hypertext note with a
    * hyperlink to the new URI(s).
    * If the 301 status code is received in response to a request other
    * than GET or HEAD, the user agent MUST NOT automatically redirect the
    * request unless it can be confirmed by the user, since this might
    * change the conditions under which the request was issued.
    *   Note: When automatically redirecting a POST request after receiving
    *   a 301 status code, some existing HTTP/1.0 user agents will
    *   erroneously change it into a GET request.
    */
   public static final int HTTP_MOVED_PERMANENTLY         = 301;

   /**
    * The requested resource resides temporarily under a different URI.
    * Since the redirection may be altered on occasion, the client SHOULD
    * continue to use the Request-URI for future requests. This response is
    * only cachable if indicated by a Cache-Control or Expires header   field.
    * If the new URI is a location, its URL SHOULD be given by the Location
    * field in the response. Unless the request method was HEAD, the entity
    * of the response SHOULD contain a short hypertext note with a
    * hyperlink to the new URI(s).
    * If the 302 status code is received in response to a request other
    * than GET or HEAD, the user agent MUST NOT automatically redirect the
    * request unless it can be confirmed by the user, since this might
    * change the conditions under which the request was issued.
    *   Note: When automatically redirecting a POST request after receiving
    *   a 302 status code, some existing HTTP/1.0 user agents will
    *   erroneously change it into a GET request.
    */
   public static final int HTTP_MOVED_TEMPORARILY         = 302;

   /**
    * The response to the request can be found under a different URI and
    * SHOULD be retrieved using a GET method on that resource. This method
    * exists primarily to allow the output of a POST-activated script to
    * redirect the user agent to a selected resource. The new URI is not a
    * substitute reference for the originally requested resource. The 303
    * response is not cachable, but the response to the second (redirected)
    * request MAY be cachable.
    * If the new URI is a location, its URL SHOULD be given by the Location
    * field in the response. Unless the request method was HEAD, the entity
    * of the response SHOULD contain a short hypertext note with a
    * hyperlink to the new URI(s).
    */
   public static final int HTTP_SEE_OTHER                  = 303;

   /**
    * If the client has performed a conditional GET request and access is
    * allowed, but the document has not been modified, the server SHOULD
    * respond with this status code. The response MUST NOT contain a
    * message-body.
    * The response MUST include the following header fields:
    *   o  Date
    *   o  ETag and/or Content-Location, if the header would have been sent
    *        in a 200 response to the same request
    *   o  Expires, Cache-Control, and/or Vary, if the field-value might
    *        differ from that sent in any previous response for the same
    *        variant
    * If the conditional GET used a strong cache validator (see section
    * 13.3.3), the response SHOULD NOT include other entity-headers.
    * Otherwise (i.e., the conditional GET used a weak validator), the
    * response MUST NOT include other entity-headers; this prevents
    * inconsistencies between cached entity-bodies and updated headers.
    * If a 304 response indicates an entity not currently cached, then the
    * cache MUST disregard the response and repeat the request without the
    * conditional.
    * If a cache uses a received 304 response to update a cache entry, the
    * cache MUST update the entry to reflect any new field values given in
    * the response.
    * The 304 response MUST NOT include a message-body, and thus is always
    * terminated by the first empty line after the header fields.
    */
   public static final int HTTP_NOT_MODIFIED               = 304;

   /**
    * The requested resource MUST be accessed through the proxy given by
    * the Location field. The Location field gives the URL of the proxy.
    * The recipient is expected to repeat the request via the proxy.
    */
   public static final int HTTP_USE_PROXY                  = 305;

   /**
    * The request could not be understood by the server due to malformed
    * syntax. The client SHOULD NOT repeat the request without modifications.
    */
   public static final int HTTP_BAD_REQUEST               = 400;

   /**
    * The request requires user authentication. The response MUST include a
    * WWW-Authenticate header field (section 14.46) containing a challenge
    * applicable to the requested resource. The client MAY repeat the
    * request with a suitable Authorization header field (section 14.8). If
    * the request already included Authorization credentials, then the 401
    * response indicates that authorization has been refused for those
    * credentials. If the 401 response contains the same challenge as the
    * prior response, and the user agent has already attempted
    * authentication at least once, then the user SHOULD be presented the
    * entity that was given in the response, since that entity MAY include
    * relevant diagnostic information. HTTP access authentication is
    * explained in section 11.
    */
   public static final int HTTP_UNAUTHORIZED               = 401;

   /**
    * This code is reserved for future use.
    */
   public static final int HTTP_PAYMENT_REQUIRED         = 402;

   /**
    * The server understood the request, but is refusing to fulfill it.
    * Authorization will not help and the request SHOULD NOT be repeated.
    * If the request method was not HEAD and the server wishes to make
    * public why the request has not been fulfilled, it SHOULD describe the
    * reason for the refusal in the entity. This status code is commonly
    * used when the server does not wish to reveal exactly why the request
    * has been refused, or when no other response is applicable.
    */
   public static final int HTTP_FORBIDDEN                  = 403;

   /**
    * The server has not found anything matching the Request-URI. No
    * indication is given of whether the condition is temporary or   permanent.
    * If the server does not wish to make this information available to the
    * client, the status code 403 (Forbidden) can be used instead. The 410
    * (Gone) status code SHOULD be used if the server knows, through some
    * internally configurable mechanism, that an old resource is
    * permanently unavailable and has no forwarding address.
    */
   public static final int HTTP_NOT_FOUND                  = 404;

   /**
    * The method specified in the Request-Line is not allowed for the
    * resource identified by the Request-URI. The response MUST include an
    * Allow header containing a list of valid methods for the requested   resource.
    */
   public static final int HTTP_METHOD_NOT_ALLOWED         = 405;

   /**
    * The resource identified by the request is only capable of generating
    * response entities which have content characteristics not acceptable
    * according to the accept headers sent in the request.
    * Unless it was a HEAD request, the response SHOULD include an entity
    * containing a list of available entity characteristics and location(s)
    * from which the user or user agent can choose the one most
    * appropriate.  The entity format is specified by the media type given
    * in the Content-Type header field. Depending upon the format and the
    * capabilities of the user agent, selection of the most appropriate
    * choice may be performed automatically. However, this specification
    * does not define any standard for such automatic selection.
    *   Note: HTTP/1.1 servers are allowed to return responses which are
    *   not acceptable according to the accept headers sent in the request.
    *   In some cases, this may even be preferable to sending a 406
    *   response. User agents are encouraged to inspect the headers of an
    *   incoming response to determine if it is acceptable. If the response
    *   could be unacceptable, a user agent SHOULD temporarily stop receipt
    *   of more data and query the user for a decision on further actions.
    */
   public static final int HTTP_NOT_ACCEPTABLE            = 406;

   /**
    * This code is similar to 401 (Unauthorized), but indicates that the
    * client MUST first authenticate itself with the proxy. The proxy MUST
    * return a Proxy-Authenticate header field (section 14.33) containing a
    * challenge applicable to the proxy for the requested resource. The
    * client MAY repeat the request with a suitable Proxy-Authorization
    * header field (section 14.34). HTTP access authentication is explained
    * in section 11.
    */
   public static final int HTTP_PROXY_AUTHENT_REQD         = 407;

   /**
    * The client did not produce a request within the time that the server
    * was prepared to wait. The client MAY repeat the request without
    * modifications at any later time.
    */
   public static final int HTTP_REQUEST_TIMEOUT            = 408;
   
   /**
    * The request could not be completed due to a conflict with the current
    * state of the resource. This code is only allowed in situations where
    * it is expected that the user might be able to resolve the conflict
    * and resubmit the request. The response body SHOULD include enough
    * information for the user to recognize the source of the conflict.
    * Ideally, the response entity would include enough information for the
    * user or user agent to fix the problem; however, that may not be
    * possible and is not required.
    * Conflicts are most likely to occur in response to a PUT request. If
    * versioning is being used and the entity being PUT includes changes to
    * a resource which conflict with those made by an earlier (third-party)
    * request, the server MAY use the 409 response to indicate that it
    * can't complete the request. In this case, the response entity SHOULD
    * contain a list of the differences between the two versions in a
    * format defined by the response Content-Type.
    */
   public static final int HTTP_CONFLICT                  = 409;
   
   /**
    * The requested resource is no longer available at the server and no
    * forwarding address is known. This condition SHOULD be considered
    * permanent. Clients with link editing capabilities SHOULD delete
    * references to the Request-URI after user approval. If the server does
    * not know, or has no facility to determine, whether or not the
    * condition is permanent, the status code 404 (Not Found) SHOULD be
    * used instead.  This response is cachable unless indicated otherwise.
    * The 410 response is primarily intended to assist the task of web
    * maintenance by notifying the recipient that the resource is
    * intentionally unavailable and that the server owners desire that
    * remote links to that resource be removed. Such an event is common for
    * limited-time, promotional services and for resources belonging to
    * individuals no longer working at the server's site. It is not
    * necessary to mark all permanently unavailable resources as "gone" or
    * to keep the mark for any length of time -- that is left to the
    * discretion of the server owner.
    */
   public static final int HTTP_GONE                     = 410;

   /**
    * The server refuses to accept the request without a defined Content-
    * Length. The client MAY repeat the request if it adds a valid
    * Content-Length header field containing the length of the message-body
    * in the request message.
    */
   public static final int HTTP_LENGTH_REQUIRED            = 411;
   
   /**
    * The precondition given in one or more of the request-header fields
    * evaluated to false when it was tested on the server. This response
    * code allows the client to place preconditions on the current resource
    * metainformation (header field data) and thus prevent the requested
    * method from being applied to a resource other than the one intended.
    */
   public static final int HTTP_PRECONDITION_FAILED      = 412;

   /**
    * The server is refusing to process a request because the request
    * entity is larger than the server is willing or able to process. The
    * server may close the connection to prevent the client from continuing
    * the request.
    * If the condition is temporary, the server SHOULD include a Retry-
    * After header field to indicate that it is temporary and after what
    * time the client may try again.
    */
   public static final int HTTP_REQUEST_ENTITY_TOO_LARGE   = 413;
   
   /**
    * The server is refusing to service the request because the Request-URI
    * is longer than the server is willing to interpret. This rare
    * condition is only likely to occur when a client has improperly
    * converted a POST request to a GET request with long query
    * information, when the client has descended into a URL "black hole" of
    * redirection (e.g., a redirected URL prefix that points to a suffix of
    * itself), or when the server is under attack by a client attempting to
    * exploit security holes present in some servers using fixed-length
    * buffers for reading or manipulating the Request-URI.
    */
   public static final int HTTP_REQUEST_URI_TOO_LARGE      = 414;

   /**
    * The server is refusing to service the request because the entity of
    * the request is in a format not supported by the requested resource
    * for the requested method.
    */
   public static final int HTTP_UNSUPPORTED_MEDIA_TYPE   = 415;

   /**
    * The server encountered an unexpected condition which prevented it
    * from fulfilling the request.
    */
   public static final int HTTP_INTERNAL_SERVER_ERROR      = 500;

   /**
    * The server does not support the functionality required to fulfill the
    * request. This is the appropriate response when the server does not
    * recognize the request method and is not capable of supporting it for
    * any resource.
    */
   public static final int HTTP_NOT_IMPLEMENTED            = 501;
   
   /**
    * The server, while acting as a gateway or proxy, received an invalid
    * response from the upstream server it accessed in attempting to
    * fulfill the request.
    */
   public static final int HTTP_BAD_GATEWAY               = 502;
   
   /**
    * The server is currently unable to handle the request due to a
    * temporary overloading or maintenance of the server. The implication
    * is that this is a temporary condition which will be alleviated after
    * some delay. If known, the length of the delay may be indicated in a
    * Retry-After header.  If no Retry-After is given, the client SHOULD
    * handle the response as it would for a 500 response.
    *   Note: The existence of the 503 status code does not imply that a
    *   server must use it when becoming overloaded. Some servers may wish
    *   to simply refuse the connection.
    */
   public static final int HTTP_SERVICE_UNAVAILABLE      = 503;

   /**
    * The server, while acting as a gateway or proxy, did not receive a
    * timely response from the upstream server it accessed in attempting to
    * complete the request.
    */
   public static final int HTTP_GATEWAY_TIMEOUT            = 504;

   /**
    * The server does not support, or refuses to support, the HTTP protocol
    * version that was used in the request message. The server is
    * indicating that it is unable or unwilling to complete the request
    * using the same major version as the client, as described in section
    * 3.1, other than with this error message. The response SHOULD contain
    * an entity describing why that version is not supported and what other
    * protocols are supported by that server.11 Access Authentication
    */
   public static final int HTTP_VERSION_NOT_SUPPORTED      = 505;
}

