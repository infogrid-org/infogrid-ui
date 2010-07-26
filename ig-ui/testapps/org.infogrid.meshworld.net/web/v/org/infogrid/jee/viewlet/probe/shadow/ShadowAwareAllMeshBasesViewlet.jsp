<%@    taglib prefix="set"      uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="mesh"     uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="meshbase" uri="/v/org/infogrid/jee/taglib/meshbase/meshbase.tld"
 %><%@ taglib prefix="logic"    uri="/v/org/infogrid/jee/taglib/logic/logic.tld"
 %><%@ taglib prefix="probe"    uri="/v/org/infogrid/jee/taglib/probe/probe.tld"
 %><%@ taglib prefix="candy"    uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"        uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"        uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"        uri="http://java.sun.com/jsp/jstl/core"
 %>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/viewlet/probe/shadow/ShadowAwareAllMeshBasesViewlet.css"/>
<v:viewletAlternatives />
<v:viewlet>
 <h1>All locally known MeshBases</h1>
 <ol>
  <c:forEach items="${Viewlet.iterator}" var="current">
   <li>
    <h4><meshbase:meshBaseLink meshBaseName="current">MeshBase <meshbase:meshBaseId meshBaseName="current"/></meshbase:meshBaseLink></h4>
    <probe:ifIsShadowMeshBase meshBaseName="current">
     <table class="org-infogrid-jee-viewlet-probe-shadow-ShadowAwareAllMeshBasesViewlet-shadow">
      <tr>
       <td>
        <table class="org-infogrid-jee-viewlet-probe-shadow-ShadowAwareAllMeshBasesViewlet-shadowinfo">
         <tr>
          <th>&#35;&nbsp;MeshObjects:</th>
          <td>${current.size}</td>
         </tr>
         <tr>
          <th>Last run:</th>
          <td><mesh:property meshObjectName="current.homeObject" propertyType="org.infogrid.model.Probe/ProbeUpdateSpecification_LastProbeRun"/></td>
         </tr>
         <tr>
          <th>Next run:</th>
          <td><mesh:property meshObjectName="current.homeObject" propertyType="org.infogrid.model.Probe/ProbeUpdateSpecification_NextProbeRun"/></td>
         </tr>
         <tr>
          <th>&#35;&nbsp;Runs:</th>
          <td><mesh:property meshObjectName="current.homeObject" propertyType="org.infogrid.model.Probe/ProbeUpdateSpecification_ProbeRunCounter"/></td>
         </tr>
         <tr>
          <th>Writable:</th>
          <td>
           <logic:equal meshObjectName="current.homeObject" propertyType="org.infogrid.model.Probe/ProbeUpdateSpecification_LastRunUsedWritableProbe" value="-TRUE-">
            Yes
           </logic:equal>
           <logic:equal meshObjectName="current.homeObject" propertyType="org.infogrid.model.Probe/ProbeUpdateSpecification_LastRunUsedWritableProbe" value="-FALSE-">
            No
           </logic:equal>
          </td>
         </tr>
        </table>
       </td>
       <td class="org-infogrid-jee-viewlet-probe-shadow-ShadowAwareAllMeshBasesViewlet-shadowcommands">
        <u:safeForm action="${Viewlet.postUrl}" method="POST">
            <input type="hidden" name="MeshBase" value="<meshbase:meshBaseId meshBaseName="current" stringRepresentation="Plain" filter="true" />"/>
         <ul>
          <li><input type="submit" name="RunNowAction" value="Run now"/></li>
          <li><input type="submit" name="StopAction" value="Stop"/></li>
         </ul>
         </u:safeForm>
       </td>
      </tr>
     </table>
    </probe:ifIsShadowMeshBase>
   </li>
  </c:forEach>
 </ol>
</v:viewlet>