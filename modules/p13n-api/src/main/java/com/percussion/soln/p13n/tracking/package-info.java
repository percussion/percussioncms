/**
 * Tracks visitors by associating a 
 * {@link com.percussion.soln.p13n.tracking.VisitorProfile}
 * with each visitor of a web site(s).
 * 
 * The system will actively create and update {@link com.percussion.soln.p13n.tracking.VisitorProfile VisitorProfiles} 
 * by processing {@link com.percussion.soln.p13n.tracking.VisitorRequest VisitorRequests}.
 * <p>
 * Most of the time the system will be updating the 
 * {@link com.percussion.soln.p13n.tracking.VisitorProfile#getSegmentWeights() VisitorProfile's segment weights}.
 * As far as tracking is concerned segment ids can be any string although its recommend that you use 
 * real {@link com.percussion.soln.segment.Segment} ids if you
 * plan on using the {@link com.percussion.soln.p13n.delivery p13n delivery system}.
 * Segment ids can either be the folder path of the segment or the content id of the segment.
 * 
 * <h1>How it works</h1>
 * When a visitor visits a web page a tracking request is made.
 * This request includes:
 * <ul>
 * <li>
 *  Network, time, locale and browser Information about the visitor.
 * </li>
 * <li>
 *  The type of tracking action to perform.
 * </li>
 * <li>
 *  Segment weighting for the page that was visited.
 *  Usually this is just a list of segments that have an implicit weight of 1.
 * </li>
 * </ul>
 * When the tracking request is received:
 * <ol>
 * <li> A profile is either retrieved or created. Created if its a new visitor and retrieved
 * if the visitor has been to the web site before. See {@link com.percussion.soln.p13n.tracking.web.IVisitorTrackingHttpService}
 * for more details on how that happens.
 * </li>
 * <li>
 * The action is run on the profile. Usually the  {@link com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionUpdate default action} is
 * run which increments the {@link com.percussion.soln.p13n.tracking.VisitorProfile#getSegmentWeights() Vistor segments weights} with the
 * {@link com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest#getSegmentWeights() weights in the request}. 
 * </li>
 * <li>A {@link com.percussion.soln.p13n.tracking.VisitorTrackingResponse tracking response } 
 * is returned and contains {@link com.percussion.soln.p13n.tracking.VisitorTrackingResponse#getErrorId() error id} 
 * and {@link com.percussion.soln.p13n.tracking.VisitorTrackingResponse#getErrorMessage() error message} if there is one.</li>
 * </ol>
 * So in most cases if a Visitor has segments weights with:
 * <pre>a=1,b=4,c=2</pre>
 * And they visit a page with weights set to:
 * <pre>a=1,d=1</pre>
 * the visitors weights will now be:
 * <pre>a=2,b=4,c=2,d=1</pre>
 * 
 * <h1>Service Description</h1>
 * 
 * <p>
 * There are two services that allow manipulation and retrieval of {@link com.percussion.soln.p13n.tracking.VisitorProfile VisitorProfiles}.
 * <table>
 * <thead><tr><th>Service</th><th>Description</th></tr></thead>
 * <tr>
 *     <td>{@link com.percussion.soln.p13n.tracking.IVisitorTrackingService}</td>
 *     <td>This service is for tracking visitors and is what should be used by web applications to track visitors.</td>
 * </tr>
 * <tr>
 * <td>{@link com.percussion.soln.p13n.tracking.IVisitorProfileDataService}</td>
 *      <td>This is a lower level service to manipulate {@link com.percussion.soln.p13n.tracking.VisitorProfile VisitorProfiles} directly.
 *  </td>
 * </tr>
 * </table> 
 * 
 * <h1>Developer Guide</h1>
 * A brief guide to implementing and extending tracking.
 * 
 * <h2>Implementing Tracking</h2>
 * The following section will describe how to setup tracking for a website.
 * <h3>Setup Tracking for Static web pages using DHTML</h3>
 * To setup tracking on static pages you will need to include a Javascript file in your HTML through a script tag.
 * You will also need to have your page tagged with the correct <code>META</code> tag.
 * <p>
 * If you are using Percussion CM System then the required Javascript and META tags will be put on your pages so long as the
 * corresponding page templates are using the personalization velocity macros.
 * <p>
 * If you are not then you will need to place something like the following in the head portion of your page:
 * <pre>
 * &lt;meta content="1,2,3" name="segments"/&gt;
 * &lt;script type="text/javascript"  src="/soln-p13n/solution/resources/scripts/p13n/perc_p13n_delivery.js"&gt;&lt;/script&gt;
 * </pre>
 * The meta tag with name attribute of segments is where you can put your segment weights for the page.
 * The example above has three segments all with an implicit weight of 1.
 * The following is an explicit weighting of segments.
 * <pre>
 * &lt;meta content="a=10,b=20,c=30" name="segments"/&gt;
 * </pre>
 * In this case the page will be tagged with segment 'a' with weight of 10, and segment 'b' with a weight of 20.
 * <p>
 * 
 * <h3>Setup Tracking for Dynamic web pages</h3>
 * For Dynamic pages you can still use the Javascript/DHTML method mentioned above but there maybe reasons
 * that you would like your web application to do the tracking directly instead of the browser.
 * 
 * <h4>Using the JSP/Tag Libraries</h4>
 * See:
 * <ul>
 * <li>{@link com.percussion.soln.p13n.tracking.web.taglib}</li>
 * <li><a href="{@docRoot}/taglib/index.html" target="_blank">p13n-tracking.tld</a></li>
 * </ul>
 * 
 * <h4>Using the Visitor Tracking JSON REST Service</h4>
 * You can make tracking request by using the JSON REST Service. This service is designed for AJAX applications
 * but can easily be used by other Technologies such as .NET or PHP.
 * To use the JSON REST Service you make requests to the URL:
 * <p>
 * <pre>http://HOSTNAME/soln-p13n/track/track</pre> 
 * <p>
 * with the following HTTP Parameters set:
 * <table border="1">
 * <thead><tr><th>HTTP Parameter Name</th><th>Description</th></tr></thead>
 * <tr><td>visitorProfileId</td>
 * <td>The profile id is not provided, the profile will be retrieved through other means. For security this parameter may be turned off through config.</td></tr>
 * <tr><td>actionName</td><td>The name of the action to execute. <strong>See the {@link com.percussion.soln.p13n.tracking.action.impl default actions}.</strong></td></tr>
 * <tr><td>segmentWeights[ID]</td><td>The segment weight map where id is the segment id. Example: segmentWeights[a]=10&segmentWeights[b]=20</td></tr>
 * <tr><td>userId</td><td>{@link com.percussion.soln.p13n.tracking.VisitorRequest#getUserId() The user id of the profile}</td></tr>
 * </table>
 * <em>The parameters that need to be set will be dependent on the tracking action.</em>
 * <p>
 * The response of the request will be a JSON version of the {@link com.percussion.soln.p13n.tracking.VisitorTrackingResponse} object:
 * <pre>
   {
    "errorMessage" :"",
    "status" :"OK",
    "visitorProfileId" :-3883615407105618730
    }
    </pre>
 * <p>
 * There is an example Java REST client that is included in the soln-p13n-tracking-web.jar.
 * See {@link com.percussion.soln.p13n.tracking.web.TrackLoginRestClient} and {@link com.percussion.soln.p13n.tracking.web.TrackRestClient}.
 * <p>
 * If you would you like to use the Java REST client you will need the following jars that are in the <strong>soln-p13n.war</strong>:
 * <ul>
 * <li>soln-p13n-tracking-api.jar</li>
 * <li>soln-p13n-tracking-web.jar</li>
 * <li>commons-lang.jar</li>
 * <li>commons-logging.jar</li>
 * <li>json-lib.jar</li>
 * <li>ezmorph.jar</li>
 * <li>commons-httpclient.jar</li>
 * <li>servlet-api.jar (your servlet container may already provide this)</li>
 * </ul>
 * <p>
 * <h4>Connecting to an existing user registration system</h4>
 * If you have an existing <a href="http://en.wikipedia.org/wiki/Registered_user">registration system</a> for
 * your web site visitors you can explicitly associate these users with visitor profiles and set explicit 
 * weighting on segments.
 * Because you generally have more information with registered users such as their locale and interests you might want to explicitly
 * set the weight of a locale or interest segments to a very high value.
 * The best way to do this is through the REST API:
 *  
 * <h5>Java</h5>
 * If you are using Java in your user registration system you can use 
 * {@link com.percussion.soln.p13n.tracking.web.TrackLoginRestClient login rest client} mentioned
 * above. You could also use the tag library mentioned above.
 * 
 * <h5>Other technologies</h5>
 * If you are using a different technology than Java you can use the
 * REST API directly by calling the {@link com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionLogin login action}
 * in your user registration system. The response to the tracking action will contain the
 *  {@link com.percussion.soln.p13n.tracking.VisitorTrackingResponse#getVisitorProfileId() visitor profile id}. You
 * will need to manually set the visitor profile id from the response to a cookie with name: 
 * {@value com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils#VISITOR_PROFILE_ID_COOKIE_NAME}.
 * 
 * <h4>Using the Visitor Profile Data SOAP Web Service</h4>
 * <em>It is recommended that you do not use this SOAP API and instead use the Tracking REST API.</em>
 * <p>
 * The WSDL can be found here: <code>http://hostname/soln-p13n/xfire/VisitorProfileDataService?WSDL</code>
 * Use of web services varies from platform and Web service library so see your Web Service documentation on how to setup
 * using a WSDL.
 * 
 * <h2>Extending Tracking</h2>
 * The tracking system uses the Spring framework to register new extensions.
 * Consequently to extend tracking you will have to create your own spring bean file and put it into your jar with
 * the following path:
 * <code>META-INF/p13n/spring/my-groups-name/track-beans.xml</code>.
 * Where <code>my-groups-name</code> is your company name or some unique identifier.
 * Next you will extend an extension class and put the code in the same jar.
 * The jar should be placed in P13N WAR <code>WEB-INF/lib</code>.
 * 
 * The following sections will cover the format of the Spring file and which classes to extend.
 * 
 * <h3>Writing your own Custom Tracking Actions</h3>
 * 
 * Before implementing your own tracking action please see the {@link com.percussion.soln.p13n.tracking.action.impl default actions}
 * that come with P13N. 
 * Tracking can be extended by extending {@link com.percussion.soln.p13n.tracking.action.AbstractVisitorTrackingAction}.
 * Once implemented you must wire the actions into the previously mentioned Spring bean file.
 * <pre>
 * {@code
 
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:lang="http://www.springframework.org/schema/lang"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
        
    <bean class="mycompany.package.name.MyTrackingActionOne" autowire="byType"/>
    <bean class="mycompany.package.name.MyTrackingActionTwo" autowire="byType"/>
    
</beans>
 
 * }
 * </pre>
 * 
 * <h3>Implementing your own Environment Detection Service</h3>
 * While {@link com.percussion.soln.p13n.tracking.IVisitorTrackingAction tracking actions} typically update
 * {@link com.percussion.soln.p13n.tracking.VisitorProfile#getSegmentWeights() visitor profile segment weights}
 * you can also change what  {@link com.percussion.soln.p13n.tracking.VisitorLocation environment} 
 * data is {@link com.percussion.soln.p13n.tracking.VisitorProfile#getLocation() associated with the profile}  
 * by implementing your own
 * {@link com.percussion.soln.p13n.tracking.location.IVisitorLocationService Environment Detection Service}.
 * <p>
 * This usually done for Custom Geo-Location services.
 * <p>
 * To do this extend {@link com.percussion.soln.p13n.tracking.location.AbstractVisitorLocationService} and
 * register your service just like the tracking actions using the previously mentioned Spring Bean file:
 * <pre>
 * {@code
 
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:lang="http://www.springframework.org/schema/lang"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
        
    <bean class="mycompany.package.name.MyLocationService" autowire="byType"/>
    
</beans>
 
 * }
 * </pre>
 * 
 * <h3>Differences between Tracking Actions and Location Service</h3>
 * While it may seem like a good idea to implement a Geo-Location service adapter as tracking action since 
 * tracking actions have more power than a location services it is recommend that you do not do this.
 * Tracking actions are not executed on every request but the visitor location service is. Thus if a 
 * visitor visits a page in your site and it does not fire off a tracking action but does 
 * {@link com.percussion.soln.p13n.delivery deliver dynamic} personalized content the location will still be updated.
 *  
 * 
 * @see com.percussion.soln.p13n.tracking.IVisitorTrackingService
 * @author adamgent
 */
package com.percussion.soln.p13n.tracking;