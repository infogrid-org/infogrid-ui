<h2>Replica Info</h2>
<p>Is&nbsp;home&nbsp;replica:&nbsp;
 <netmesh:ifHomeReplica meshObjectName="Subject">Yes.</netmesh:ifHomeReplica>
 <netmesh:notIfHomeReplica meshObjectName="Subject">No.</netmesh:notIfHomeReplica>
</p>
<p>Has&nbsp;update&nbsp;rights:&nbsp;
 <netmesh:ifHasLock meshObjectName="Subject">Yes.</netmesh:ifHasLock>
 <netmesh:notIfHasLock meshObjectName="Subject">No.</netmesh:notIfHasLock>
</p>
<p>Will&nbsp;give&nbsp;up&nbsp;lock:&nbsp;
 <netmesh:ifWillGiveUpLock meshObjectName="Subject">Yes.</netmesh:ifWillGiveUpLock>
 <netmesh:notIfWillGiveUpLock meshObjectName="Subject">No.</netmesh:notIfWillGiveUpLock>
</p>
<ul>
 <netmeshbase:proxyIterate meshObjectName="Subject" loopVar="currentProxy">
  <li>
   <netmeshbase:proxyLink proxyName="currentProxy"><netmeshbase:proxy proxyName="currentProxy" /></netmeshbase:proxyLink>
  </li>
 </netmeshbase:proxyIterate>
</ul>