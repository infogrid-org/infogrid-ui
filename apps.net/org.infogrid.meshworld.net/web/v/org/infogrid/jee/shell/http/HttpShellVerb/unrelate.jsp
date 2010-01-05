<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-unrelate">
 <u:safeForm method="post" action="${Viewlet.postUrl}">
  <h2>Do you really want to unrelate two MeshObjects</h2>
  <table>
   <tr>
    <td class="label">MeshObject:</td>
    <td>
     <input class="subject" name="shell.subject" size="32" readonly="readonly" />
     <input type="hidden" name="shell.subject.access" value="find" />
    </td>
   </tr>
   <tr>
    <td class="label">Neighbor:</td>
    <td>
     <input class="object" name="shell.object" size="32" />
     <input type="hidden" name="shell.object.access" value="find" />
     <input type="hidden" name="shell.subject.to.object.perform" value="unrelate" />
    </td>
   </tr>
   <tr>
    <td colspan="2">
     <div class="dialog-buttons">
      <table class="dialog-buttons">
       <tr>
        <td><input type="submit" value="Unrelate" /></td>
        <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-unrelate' )">Cancel</a></td>
       </tr>
      </table>
     </div>
    </td>
   </tr>
  </table>
 </u:safeForm>
</candy:overlay>