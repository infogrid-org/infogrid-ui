<%@    page contentType="text/html"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="tree"  uri="/v/org/infogrid/jee/taglib/mesh/tree/tree.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/viewlet/graphtree/GraphTreeViewlet.css"/>
<v:viewletAlternatives />
<v:viewlet>
 <h1>Graph Tree for: <mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Html" maxLength="30"/></h1>

 <div class="content">
  <table>
   <tr>
    <td class="org-infogrid-jee-viewlet-graphtree-GraphTreeViewlet-sidebar">
     <dl class="level1">
      <tree:treeIterate startObjectName="Subject" traversalSpecification="*" levelVar="level" meshObjectLoopVar="current">
       <tree:down>
         <dd><dl class="level${level}">
       </tree:down>
       <tree:up>
         </dl></dd>
       </tree:up>
       <tree:nodeBefore>
        <dt>
         <mesh:meshObjectLink meshObjectName="current" addArguments="lid-appcontext=iframe" target="detail" >
          <mesh:meshObject meshObjectName="current"/>
         </mesh:meshObjectLink>
        </dt>
       </tree:nodeBefore>
      </tree:treeIterate>
     </dl>
    </td>
    <td class="org-infogrid-jee-viewlet-graphtree-GraphTreeViewlet-content">
     <iframe name="detail" width="100%" height="100%"/>
    </td>
   </tr>
  </table>
 </div>
</v:viewlet>
