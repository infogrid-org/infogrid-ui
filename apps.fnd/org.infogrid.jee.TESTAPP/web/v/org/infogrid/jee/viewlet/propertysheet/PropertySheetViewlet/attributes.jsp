 <table class="attributes">
  <thead>
   <tr>
    <th class="type">
     <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-bless', { 'mesh.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />' } )" title="Bless this MeshObject"><img src="${CONTEXT}/s/images/add.png" alt="Add type"/></a></div>
     Type
    </th>
    <th>Property</th>
    <th>Value</th>
   </tr>
  </thead>
  <tbody>
   <mesh:blessedByIterate meshObjectName="Subject" blessedByLoopVar="blessedBy">
    <tr>
     <th class="entityType" colspan="3">
      <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-unbless', { 'mesh.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />', 'mesh.subjecttype' : '<mesh:meshTypeId meshTypeName="blessedBy" stringRepresentation="Plain" filter="true" />' } )" title="Unbless this MeshObject"><img src="${CONTEXT}/s/images/trash.png" alt="Delete"/></a></div>
      <mesh:type meshTypeName="blessedBy"/>
     </th>
    </tr>
    <mesh:propertyIterate meshObjectName="Subject" meshTypeName="blessedBy" propertyTypeLoopVar="propertyType" propertyValueLoopVar="propertyValue" skipNullProperty="false">
     <tr>
      <th class="type"></th>
      <td class="name"><mesh:type meshTypeName="propertyType" /></td>
      <td class="value">
       <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-setProperty', { 'mesh.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />', 'mesh.propertytype' : '<mesh:meshTypeId meshTypeName="propertyType" stringRepresentation="Plain" filter="true" />', 'mesh.propertyvalue' : '<mesh:property meshObjectName="Subject" propertyTypeName="propertyType" stringRepresentation="Plain" filter="true" />' } )" title="Change this property"><img src="${CONTEXT}/s/images/edit.png" alt="Edit"/></a></div>
       <mesh:propertyValue propertyValueName="propertyValue" ignore="true"/>
      </td>
     </tr>
    </mesh:propertyIterate>
   </mesh:blessedByIterate>
  </tbody>
 </table>
