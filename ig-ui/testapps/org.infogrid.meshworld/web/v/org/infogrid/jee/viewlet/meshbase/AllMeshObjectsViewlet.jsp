<%@    page contentType="text/html"
 %><%@ taglib prefix="set"   uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/viewlet/meshbase/AllMeshObjectsViewlet.css"/>
<v:viewletAlternatives />
<v:viewlet>
 <div class="slide-in-button">
  <a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-create', {} )" title="Create a MeshObject"><img src="${CONTEXT}/s/images/add.png" alt="Create"/></a>
 </div>
 <h1>
<%
    MeshBase mb = ((MeshObject)pageContext.getRequest().getAttribute( "Subject" )).getMeshBase();
    if( mb instanceof IterableMeshBase ) {
        out.print( ((IterableMeshBase)mb).getSize() );
    } else {
        out.print( "All" );
    }
%>
 MeshObjects in the MeshBase</h1>

 <div class="nav">
  <div class="left">
   <c:if test="${Viewlet.navigationStartMeshObject != null}">
    <v:navigateToPage meshObject="${Viewlet.navigationStartMeshObject}" addArguments="lid-format=viewlet:org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet&page-length=${Viewlet.pageLength}">
     <img src="${CONTEXT}/s/images/control_start_blue.png" alt="Go to start" />
    </v:navigateToPage>
   </c:if>
   <c:if test="${Viewlet.navigationStartMeshObject == null}">
    <img src="${CONTEXT}/s/images/control_start.png" alt="Go to start (disabled)" />
   </c:if>
  </div>

  <div class="left">
   <c:if test="${Viewlet.navigationBackMeshObject != null}">
    <v:navigateToPage meshObject="${Viewlet.navigationBackMeshObject}" addArguments="lid-format=viewlet:org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet&page-length=${Viewlet.pageLength}">
     <img src="${CONTEXT}/s/images/control_rewind_blue.png" alt="start" />
    </v:navigateToPage>
   </c:if>
   <c:if test="${Viewlet.navigationBackMeshObject == null}">
    <img src="${CONTEXT}/s/images/control_rewind.png" alt="start" />
   </c:if>
  </div>

  <div class="right">
   <c:if test="${Viewlet.navigationEndMeshObject != null}">
    <v:navigateToPage meshObject="${Viewlet.navigationEndMeshObject}" addArguments="lid-format=viewlet:org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet&page-length=${Viewlet.pageLength}">
     <img src="${CONTEXT}/s/images/control_end_blue.png" alt="start" />
    </v:navigateToPage>
   </c:if>
   <c:if test="${Viewlet.navigationEndMeshObject == null}">
    <img src="${CONTEXT}/s/images/control_end.png" alt="start" />
   </c:if>
  </div>

  <div class="right">
   <c:if test="${Viewlet.navigationForwardMeshObject != null}">
    <v:navigateToPage meshObject="${Viewlet.navigationForwardMeshObject}" addArguments="lid-format=viewlet:org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet&page-length=${Viewlet.pageLength}">
     <img src="${CONTEXT}/s/images/control_fastforward_blue.png" alt="start" />
    </v:navigateToPage>
   </c:if>
   <c:if test="${Viewlet.navigationForwardMeshObject == null}">
    <img src="${CONTEXT}/s/images/control_fastforward.png" alt="start" />
   </c:if>
  </div>
 </div>

 <table class="set">
  <thead>
   <tr>
    <th>Identifier</th>
    <th>Neighbors</th>
    <th>Types and Attributes</th>
    <th>Audit</th>
   </tr>
  </thead>
  <tbody>
   <c:forEach items="${Viewlet.cursorIterator}" var="current" varStatus="currentStatus">
    <u:rotatingTr statusVar="currentStatus" htmlClasses="bright,dark" firstRowHtmlClass="first" lastRowHtmlClass="last">
     <td>
      <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-delete', { 'shell.subject' : '<mesh:meshObjectId meshObjectName="current" stringRepresentation="Plain" filter="true" />' } )" title="Delete this MeshObject"><img src="${CONTEXT}/s/images/bin_closed.png" alt="Delete"/></a></div>
      <mesh:meshObjectLink meshObjectName="current"><mesh:meshObjectId meshObjectName="current" maxLength="30"/></mesh:meshObjectLink>
     </td>
     <td>
      <mesh:neighborIterate meshObjectName="current" neighborLoopVar="neighbor">
       <mesh:meshObjectLink meshObjectName="neighbor"><mesh:meshObjectId meshObjectName="neighbor" maxLength="30"/></mesh:meshObjectLink><br />
      </mesh:neighborIterate>
     </td>
     <td>
      <ul class="types">
       <mesh:blessedByIterate meshObjectName="current" blessedByLoopVar="blessedBy">
        <li>
         <mesh:type meshTypeName="blessedBy"/>
         <ul class="properties">
          <mesh:propertyIterate meshObjectName="current" meshTypeName="blessedBy" propertyTypeLoopVar="propertyType" propertyValueLoopVar="propertyValue">
           <li><mesh:type meshTypeName="propertyType" />:&nbsp;<mesh:property meshObjectName="current" propertyTypeName="propertyType" /></li>
          </mesh:propertyIterate>
         </ul>
        </li>
       </mesh:blessedByIterate>
      </ul>
     </td>
     <td>
      <table class="audit">
       <tr>
        <td class="label">Created:</td><td><mesh:timeCreated meshObjectName="current" /></td>
       </tr>
       <tr>
        <td class="label">Updated:</td><td><mesh:timeUpdated meshObjectName="current" /></td>
       </tr>
       <tr>
        <td class="label">Last&nbsp;read:</td><td><mesh:timeRead meshObjectName="current" /></td>
       </tr>
      </table>
     </td>
    </u:rotatingTr>
   </c:forEach>
  </tbody>
 </table>
 <%@ include file="/v/org/infogrid/jee/shell/http/HttpShellVerb.jsp" %>
</v:viewlet>
