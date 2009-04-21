<%@    page contentType="text/html"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectEditViewlet.css"/>
<v:viewletAlternatives />
<v:viewlet>
 <h1>Wiki Editor Viewlet for: <mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" maxLength="30"/></h1>
 <u:safeForm action="${Viewlet.postUrl}" method="post">
  <v:ifState viewletState="edit">
   <div class="viewlet-state"><p>Edit (not saved yet)</p></div>
   <div class="current-content">
    <mesh:property meshObjectName="Subject" propertyType="org.infogrid.model.Wiki#WikiObject/Content"/>
   </div>
   <table class="dialog-buttons">
    <tr>
     <td><button type="submit" name="ViewletStateTransition" value="do-cancel">Discard</button></td>
     <td><button type="submit" name="ViewletStateTransition" value="do-preview">Preview</button></td>
     <td><button type="submit" name="ViewletStateTransition" value="do-commit">Save</button></td>
    </tr>
   </table>
  </v:ifState>
  <v:ifState viewletState="preview">
   <div class="viewlet-state"><p>Preview (not saved yet)</p></div>
   <div class="content">
    <mesh:property meshObjectName="Subject" propertyType="org.infogrid.model.Wiki#WikiObject/Content" stringRepresentation="Plain"/>
   </div>
   <div class="current-content">
    <mesh:property meshObjectName="Subject" propertyType="org.infogrid.model.Wiki#WikiObject/Content" stringRepresentation="Edit"/>
   </div>
   <table class="dialog-buttons">
    <tr>
     <td><button type="submit" name="ViewletStateTransition" value="do-cancel">Discard</button></td>
     <td><button type="submit" name="ViewletStateTransition" value="do-preview">Preview</button></td>
     <td><button type="submit" name="ViewletStateTransition" value="do-commit">Save</button></td>
    </tr>
   </table>
  </v:ifState>
  <v:ifState viewletState="view">
   <div class="content">
    <mesh:property meshObjectName="Subject" propertyType="org.infogrid.model.Wiki#WikiObject/Content" />
   </div>
   <table class="dialog-buttons">
    <tr>
     <td><button type="submit" name="ViewletStateTransition" value="do-edit">Edit</button></td>
    </tr>
   </table>
  </v:ifState>
 </u:safeForm>  
</v:viewlet>
