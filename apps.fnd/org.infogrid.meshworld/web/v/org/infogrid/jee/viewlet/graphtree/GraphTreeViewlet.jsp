<%@    page contentType="text/html"
 %><%@ page import="java.util.Iterator"
 %><%@ page import="java.util.Stack"
 %><%@ page import="org.infogrid.jee.viewlet.graphtree.GraphTreeViewlet"
 %><%@ page import="org.infogrid.jee.viewlet.JeeViewlet"
 %><%@ page import="org.infogrid.mesh.MeshObject"
 %><%@ page import="org.infogrid.mesh.set.MeshObjectSet"
 %><%@ page import="org.infogrid.mesh.set.OrderedMeshObjectSet"
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

MeshObjectSet toplevelSet = v.subItems( v.getSubject(), 0 );
if( !toplevelSet.isEmpty() ) {
    theStack.push( toplevelSet.iterator() );
}

int level = 0;
while( !theStack.isEmpty() ) {
    CursorIterator<MeshObject> currentIter = theStack.peek();

    boolean goToChildren = false;
    while( currentIter.hasNext() ) {
        MeshObject current = currentIter.next();
        request.setAttribute( "current", current );
        // now emit node
%>
       <dt>
       <mesh:meshObjectLink meshObjectName="current" addArguments="lid-appcontext=iframe" target="detail">
        <mesh:meshObjectId meshObjectName="current"/>
       </mesh:meshObjectLink>
      </dt>
<%
        // end emit node
        MeshObjectSet children = v.subItems( current, theStack.size() );
        if( !children.isEmpty() ) {
            theStack.push( children.iterator() );
            goToChildren = true;
            break; // we'll return to that one
        }
    }
    if( goToChildren ) {
        ++level;
        // now emit "start next level"
%>
      <dd>
       <dl class="level<%=level%>">
<%
        // end emit "start next level"
    } else {
        --level;
        theStack.pop();
        if( !theStack.isEmpty() ) {
        // now emit "return to previous level"
%>
       </dl>
      </dd>
<%
        // end emit "return to previous level"
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