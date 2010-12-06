<%@    taglib prefix="set"         uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="mesh"        uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="netmesh"     uri="/v/org/infogrid/jee/taglib/mesh/net/netmesh.tld"
 %><%@ taglib prefix="meshbase"    uri="/v/org/infogrid/jee/taglib/meshbase/meshbase.tld"
 %><%@ taglib prefix="netmeshbase" uri="/v/org/infogrid/jee/taglib/meshbase/net/netmeshbase.tld"
 %><%@ taglib prefix="candy"       uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"           uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"           uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="c"           uri="http://java.sun.com/jsp/jstl/core"
 %>
<v:viewletAlternatives />
<v:viewlet>
 <h1>Proxies for NetMeshBase at: <meshbase:meshBaseId meshBaseName="Subject.MeshBase" stringRepresentation="Plain" filter="true" maxLength="30"/></h1>
 <table class="proxies">
  <tbody>   
   <tr>
    <netmeshbase:ifMeshBaseHasProxies meshBaseName="Subject.MeshBase">
     <td width="33%">
      Proxies towards:
     </td>
     <td>
      <ul>
       <netmeshbase:proxyIterate meshBaseName="Subject.MeshBase" loopVar="currentProxy">
        <li>
         <netmeshbase:proxyLink proxyName="currentProxy"><netmeshbase:proxy proxyName="currentProxy" /></netmeshbase:proxyLink>
        </li>
       </netmeshbase:proxyIterate>
      </ul>
     </td>
    </netmeshbase:ifMeshBaseHasProxies>
    <netmeshbase:notIfMeshBaseHasProxies meshBaseName="Subject.MeshBase">
     <td colspan="2" style="text-align: center">No Proxies.</td>
    </netmeshbase:notIfMeshBaseHasProxies>
   </tr>
  </tbody>
 </table>
</v:viewlet>