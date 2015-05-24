 <table class="replica">
  <thead>
   <tr>
    <th colspan="2">Replication Info</th>
   </tr>
  </thead>
  <tbody>   
   <tr>
    <td colspan="2">
     <span class="infobit">Is&nbsp;home&nbsp;replica:&nbsp;
      <netmesh:ifHomeReplica meshObjectName="Subject">Yes.</netmesh:ifHomeReplica>
      <netmesh:notIfHomeReplica meshObjectName="Subject">No.</netmesh:notIfHomeReplica></span>
     <span class="infobit">Has&nbsp;update&nbsp;rights:&nbsp;
      <netmesh:ifHasLock meshObjectName="Subject">Yes.</netmesh:ifHasLock>
      <netmesh:notIfHasLock meshObjectName="Subject">No.</netmesh:notIfHasLock></span>
     <span class="infobit">Will&nbsp;give&nbsp;up&nbsp;lock:&nbsp;
      <netmesh:ifWillGiveUpLock meshObjectName="Subject">Yes.</netmesh:ifWillGiveUpLock>
      <netmesh:notIfWillGiveUpLock meshObjectName="Subject">No.</netmesh:notIfWillGiveUpLock></span>
    </td>
   </tr>
   <tr>
    <netmesh:ifMeshObjectHasProxies meshObjectName="Subject" min="1">
     <td width="33%">
      Proxies towards:
     </td>
     <td>
      <ul>
       <netmeshbase:proxyIterate meshObjectName="Subject" loopVar="currentProxy">
        <li>
         <netmeshbase:proxyLink proxyName="currentProxy"><netmeshbase:proxy proxyName="currentProxy" /></netmeshbase:proxyLink>
        </li>
       </netmeshbase:proxyIterate>
      </ul>
     </td>
    </netmesh:ifMeshObjectHasProxies>
    <netmesh:ifMeshObjectHasProxies meshObjectName="Subject" max="0">
     <td colspan="2" style="text-align: center">No Proxies.</td>
    </netmesh:ifMeshObjectHasProxies>
   </tr>
  </tbody>
 </table>
