<h2>Neighbors</h2>
<ol>
 <mesh:neighborIterate meshObjectName="Subject" neighborLoopVar="neighbor">
  <li>
   <p>
    <mesh:meshObjectLink meshObjectName="neighbor"><mesh:meshObjectId meshObjectName="neighbor" stringRepresentation="Plain" filter="true" /></mesh:meshObjectLink>
   </p>
   <ul>
    <mesh:roleIterate startObjectName="Subject" destinationObjectName="neighbor" roleTypeLoopVar="roleType">
     <li>
      <mesh:type meshTypeName="roleType" filter="false"/>
     </li>
    </mesh:roleIterate>
   </ul>
  </li>
 </mesh:neighborIterate>
</ol>
