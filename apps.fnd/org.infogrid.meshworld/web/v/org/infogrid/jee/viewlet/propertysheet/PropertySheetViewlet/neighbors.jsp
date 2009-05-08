 <table class="neighbors">
  <thead>
   <tr>
    <th class="role">Roles</th>
    <th class="neighbor">
     <v:ifState viewletState="edit">
      <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-relate', { 'shell.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />' } )" title="Add a neighbor"><img src="${CONTEXT}/s/images/link_add.png" alt="Add neighbor"/></a></div>
     </v:ifState>
     Neighbor
    </th>
   </tr>
  </thead>
  <tbody>
   <mesh:neighborIterate meshObjectName="Subject" neighborLoopVar="neighbor">
    <tr>
     <td class="role">
      <v:ifState viewletState="edit">
       <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-blessRole', { 'shell.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />', 'shell.object' : '<mesh:meshObjectId meshObjectName="neighbor" stringRepresentation="Plain" filter="true" />' } )" title="Add a role"><img src="${CONTEXT}/s/images/medal_silver_add.png" alt="Add role"/></a></div>
      </v:ifState>
      <ul>
       <mesh:roleIterate startMeshObjectName="Subject" destinationMeshObjectName="neighbor" roleTypeLoopVar="roleType">
        <li>
         <v:ifState viewletState="edit">
          <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-unblessRole', { 'shell.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />', 'shell.object' : '<mesh:meshObjectId meshObjectName="neighbor" stringRepresentation="Plain" filter="true" />', 'shell.subject.to.object.unblessRole' : '<mesh:meshTypeId meshTypeName="roleType" stringRepresentation="Plain" filter="true" />' } )" title="Remove a role"><img src="${CONTEXT}/s/images/medal_silver_delete.png" alt="Delete a role"/></a></div>
         </v:ifState>
         <mesh:type meshTypeName="roleType" filter="false"/>
        </li>
       </mesh:roleIterate>
      </ul>
     </td>
     <td class="neighbor">
      <v:ifState viewletState="edit">
       <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-unrelate', { 'shell.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />', 'shell.object' : '<mesh:meshObjectId meshObjectName="neighbor" stringRepresentation="Plain" filter="true" />' } )" title="Remove a neighbor"><img src="${CONTEXT}/s/images/link_delete.png" alt="Delete neighbor"/></a></div>
      </v:ifState>
      <mesh:meshObjectLink meshObjectName="neighbor"><mesh:meshObjectId meshObjectName="neighbor" stringRepresentation="Plain" filter="true" maxLength="35" /></mesh:meshObjectLink>
     </td>
    </tr>
   </mesh:neighborIterate>
  </tbody>
 </table>
