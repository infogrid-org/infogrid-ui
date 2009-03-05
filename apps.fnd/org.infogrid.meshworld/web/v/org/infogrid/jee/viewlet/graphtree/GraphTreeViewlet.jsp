<%@    page contentType="text/html"
 %><%@ page import="java.util.Iterator"
 %><%@ page import="java.util.Stack"
 %><%@ page import="org.infogrid.jee.viewlet.graphtree.GraphTreeViewlet"
 %><%@ page import="org.infogrid.jee.viewlet.JeeViewlet"
 %><%@ page import="org.infogrid.mesh.MeshObject"
 %><%@ page import="org.infogrid.mesh.set.MeshObjectSet"
 %><%@ page import="org.infogrid.util.CursorIterator"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/viewlet/graphtree/GraphTreeViewlet.css"/>
<v:viewletAlternatives />
<v:viewlet>
 <h1>Graph Tree for: <mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Html" maxLength="30"/></h1>
 <div class="content" style="height:100%; width:100%;">
  <table>
   <tr>
    <td class="org-infogrid-jee-viewlet-graphtree-GraphTreeViewlet-sidebar">
     <dl class="level0">
<%
// This is basically an iterative version of recursively walking the tree
GraphTreeViewlet                  v        = (GraphTreeViewlet) request.getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
Stack<CursorIterator<MeshObject>> theStack = new Stack<CursorIterator<MeshObject>>();

theStack.push( v.subItems( v.getSubject(), 0 ).iterator() );

while( !theStack.isEmpty() ) {
    int level = theStack.size();
    CursorIterator<MeshObject> currentIter = theStack.peek();
    if( currentIter.hasNext() ) {
        MeshObject current = currentIter.next();
        MeshObject toLink  = v.determineMeshObjectToLinkTo( current, request, "Html" );

        request.setAttribute( "current", current );
        request.setAttribute( "toLink",  toLink );
        // process current

        if( toLink != null ) {
%>
      <dt>
       <mesh:meshObjectLink meshObjectName="toLink" addArguments="lid-appcontext=iframe" target="detail" >
        <%=v.determineCurrentLabel( current, request, "Html" ) %>
       </mesh:meshObjectLink>
      </dt>
<%
        } else {
%>
      <dt>
        <%=v.determineCurrentLabel( current, request, "Html" ) %>
      </dt>
<%
        }
        MeshObjectSet children = v.subItems( current, level );
        if( !children.isEmpty() ) {
            theStack.push( children.iterator() );
            // process "go down to children"
%>
      <dd>
       <dl class="level<%=level %>">
<%
        } else {
            // no children, "try to go sideways to brother"
        }
    } else {
        theStack.pop();
        // process "go up to uncle"
        if( !theStack.isEmpty() ) {
%>
       </dl>
      </dd>
<%
        }
    }
}
%>
     </dl>
    </td>
    <td class="org-infogrid-jee-viewlet-graphtree-GraphTreeViewlet-content">
     <iframe name="detail" width="100%" height="100%"/>
    </td>
   </tr>
  </table>
 </div>
</v:viewlet>