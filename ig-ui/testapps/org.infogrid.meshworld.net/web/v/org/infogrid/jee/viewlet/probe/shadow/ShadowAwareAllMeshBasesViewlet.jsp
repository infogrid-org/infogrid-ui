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
 <h1>All locally known NetMeshBases</h1>
 <ol>
  <c:forEach items="${Viewlet.iterator}" var="current">
   <li>
    <h4>
     <meshbase:meshBaseLink meshBaseName="current">
      <probe:ifIsShadowMeshBase meshBaseName="current">
      ShadowMeshBase
      </probe:ifIsShadowMeshBase>
      <probe:notIfIsShadowMeshBase meshBaseName="current">
      NetMeshBase
      </probe:notIfIsShadowMeshBase>
      <meshbase:meshBaseId meshBaseName="current" colloquial="false" />
     </meshbase:meshBaseLink>
    </h4>
    <c:if test="${current.dead}">
     <p class="error">This MeshBase is dead.</p>
    </c:if>
    <table class="mb">
     <tr>
      <td>
       <table class="mbinfo">
        <tr>
         <th>&#35;&nbsp;MeshObjects:</th>
         <td>${current.size}</td>
        </tr>
        <probe:ifIsShadowMeshBase meshBaseName="current">
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
        </probe:ifIsShadowMeshBase>
       </table>
      </td>
      <td class="commands">
       <u:safeForm action="${Viewlet.postUrl}" method="POST">
        <input type="hidden" name="MeshBase" value="<meshbase:meshBaseId meshBaseName="current" stringRepresentation="HttpShell" colloquial="false" />"/>
        <ul>
         <probe:ifIsShadowMeshBase meshBaseName="current">
          <li><input type="submit" name="RunNowAction" value="Probe now"/></li>
          <li><input type="submit" name="StopAction" value="Stop probing"/></li>
          <li><input type="submit" name="KillAction" value="Kill ShadowMeshBase"/></li>
         </probe:ifIsShadowMeshBase>
         <probe:notIfIsShadowMeshBase meshBaseName="current">
          <li><input type="submit" name="SweepNowAction" value="Sweep now"/></li>
         </probe:notIfIsShadowMeshBase>
        </ul>
       </u:safeForm>
      </td>
     </tr>
    </table>
   </li>
  </c:forEach>
 </ol>
</v:viewlet>