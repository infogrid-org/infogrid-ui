<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-unblessRole">
 <u:safeForm method="post" action="${Viewlet.postUrl}">
  <h2>Unbless relationship between two MeshObjects</h2>
  <table>
   <tr>
    <td class="label">MeshObject:</td>
    <td>
     <input class="subject" name="shell.subject" size="32" readonly="readonly" />
    </td>
   </tr>
   <tr>
    <td class="label">RoleType:</td>
    <td>
     <input class="roleType" name="shell.subject.to.object.unblessRole" size="32" />
    </td>
   </tr>
   <tr>
    <td class="label">Neighbor:</td>
    <td><input class="object" name="shell.object" size="32" /></td>
   </tr>
   <tr>
    <td colspan="2">
     <table class="dialog-buttons">
      <tr>
       <td><input type="submit" value="Unbless relationship" /></td>
       <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-unblessRole' )">Cancel</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </u:safeForm>
</candy:overlay>