<%@    taglib prefix="set"      uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="mesh"     uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="meshbase" uri="/v/org/infogrid/jee/taglib/meshbase/meshbase.tld"
 %><%@ taglib prefix="logic"    uri="/v/org/infogrid/jee/taglib/logic/logic.tld"
 %><%@ taglib prefix="candy"    uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"        uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"        uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"        uri="http://java.sun.com/jsp/jstl/core"
 %>
<v:viewletAlternatives />
<v:viewlet>
 <h1>All locally known MeshBases</h1>
 <ol>
  <c:forEach items="${Viewlet.iterator}" var="current">
   <li>
    <h4><meshbase:meshBaseLink meshBaseName="current">MeshBase <meshbase:meshBaseId meshBaseName="current"/></meshbase:meshBaseLink></h4>
   </li>
  </c:forEach>
 </ol>
</v:viewlet>