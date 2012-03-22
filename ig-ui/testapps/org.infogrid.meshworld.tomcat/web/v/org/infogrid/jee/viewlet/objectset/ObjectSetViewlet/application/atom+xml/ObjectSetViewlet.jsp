<%@    page contentType="application/atom+xml"
 %><%@ taglib prefix="mesh"      uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="objectset" uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="candy"     uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"         uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"         uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"      uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"         uri="http://java.sun.com/jsp/jstl/core"
 %>
<tmpl:requestedTemplate name="verbatim" />
<feed version="0.3"
      xmlns:ig="http://infogrid.org/xmlns/1"
      id="${Viewlet.fullRequestURI}">
 <title type="text">Objects found by traversing ${Viewlet.traversalSpecification} 
     from: <mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Html" maxLength="30"/>
 </title>

 <mesh:blessedByIterate meshObjectName="Subject" blessedByLoopVar="blessedBy">
  <ig:MeshType><mesh:meshTypeId meshTypeName="blessedBy" stringRepresentation="Plain" /></ig:MeshType>
  <mesh:propertyIterate meshObjectName="Subject" meshTypeName="blessedBy" propertyTypeLoopVar="propertyType" propertyValueLoopVar="propertyValue" skipNullProperty="false">
   <ig:Property type="<mesh:meshTypeId meshTypeName="propertyType" stringRepresentation="Plain" />">${propertyValue}</ig:Property>
  </mesh:propertyIterate>
 </mesh:blessedByIterate>

 <objectset:iterate meshObjectSetName="Viewlet.viewedMeshObjects.reachedObjects" loopVar="current" ignore="true">
  <objectset:iteratecontentrow>
   <entry>
    <id><mesh:meshObjectId meshObjectName="current" /></id>
    <mesh:blessedByIterate meshObjectName="current" blessedByLoopVar="blessedBy">
     <ig:MeshType><mesh:meshTypeId meshTypeName="blessedBy" stringRepresentation="Plain" /></ig:MeshType>
     <mesh:propertyIterate meshObjectName="current" meshTypeName="blessedBy" propertyTypeLoopVar="propertyType" propertyValueLoopVar="propertyValue" skipNullProperty="false">
       <ig:Property type="<mesh:meshTypeId meshTypeName="propertyType" stringRepresentation="Plain" />">${propertyValue}</ig:Property>
     </mesh:propertyIterate>
    </mesh:blessedByIterate>
   </entry>
  </objectset:iteratecontentrow>
 </objectset:iterate>
</feed>
