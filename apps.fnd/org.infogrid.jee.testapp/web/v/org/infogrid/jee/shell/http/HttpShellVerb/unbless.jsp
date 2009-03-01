<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-unbless">
 <u:safeForm method="post" action="${Viewlet.postUrl}">
  <h1>Do you really want to unbless this MeshObject?</h1>
  <table>
   <tr>
    <td class="label">MeshObject:</td>
    <td>
     <input class="subject" name="shell.subject" size="32" readonly="readonly" />
    </td>
   </tr>
   <tr>
    <td class="label">Type:</td>
    <td>
     <input class="subjecttype" name="shell.subject.unbless" size="32" />
    </td>
   </tr>
   <tr>
    <td colspan="2">
     <table class="dialog-buttons">
      <tr>
       <td><input type="submit" value="Unbless" /></td>
       <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-unbless' )">Cancel</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </u:safeForm>
</candy:overlay>