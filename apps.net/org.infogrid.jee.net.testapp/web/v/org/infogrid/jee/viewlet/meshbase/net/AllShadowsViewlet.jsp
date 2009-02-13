<%@    page contentType="text/html"
 %><%@ taglib prefix="set"         uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
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
 <h1>All Shadow MeshBases</h1>
 <ol>
  <c:forEach items="${Subject.meshBase.shadowMeshBases}" var="current" varStatus="currentStatus">
   <li>
    <meshbase:meshBaseLink meshBaseName="current">MeshBase <meshbase:meshBaseId meshBaseName="current"/></meshbase:meshBaseLink>
   </li>
  </c:forEach>
 </ol>
</v:viewlet>
