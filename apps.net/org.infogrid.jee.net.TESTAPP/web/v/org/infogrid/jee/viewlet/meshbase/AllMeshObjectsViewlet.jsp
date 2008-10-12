<%@    page contentType="text/html"
 %><%@ taglib prefix="set"   uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %>
<v:viewlet>
 <h1>All MeshObjects in the MeshBase</h1>
 <ol>
  <c:forEach items="${Viewlet.cursorIterator}" var="current" varStatus="currentStatus">
   <li>
    <mesh:meshObjectLink meshObjectName="current"><mesh:meshObjectId meshObjectName="current"/></mesh:meshObjectLink>
   </li>
  </c:forEach>
 </ol>
</v:viewlet>
