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
 %><%@ page import="org.infogrid.meshbase.IterableMeshBase"
 %><%@ page import="org.infogrid.meshbase.MeshBase"
 %><%@ page import="org.infogrid.mesh.MeshObject"
 %>
<v:viewletAlternatives />
<v:viewlet>
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
 <ol>
  <c:forEach items="${Viewlet.cursorIterator}" var="current" varStatus="currentStatus">
   <li>
    <mesh:meshObjectLink meshObjectName="current"><mesh:meshObjectId meshObjectName="current"/></mesh:meshObjectLink>
   </li>
  </c:forEach>
 </ol>
 <p>Go to <a href="?lid-format=viewlet:org.infogrid.jee.viewlet.meshbase.net.AllShadowsViewlet">Shadows</a>.</p>
</v:viewlet>
