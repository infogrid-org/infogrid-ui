<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-create" htmlClass="org-infogrid-jee-shell-http-HttpShellVerb">
 <u:safeForm method="post" action="${Viewlet.postUrl}">
  <h2>Create a MeshObject</h2>
  <table>
   <tr>
    <td rowspan="2" class="label">Identifier:</td>
    <td>
     <input class="subject" name="shell.subject" size="32"/>
     <input type="hidden" name="shell.subject.access" value="create" />
    </td>
   </tr>
   <tr>
    <td class="advice">(Empty Identifier will auto-create one)</td>
   </tr>
   <tr>
    <td colspan="2">
     <div class="dialog-buttons">
      <table class="dialog-buttons">
       <tr>
        <td><input type="submit" value="Create" /></td>
        <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-create' )">Cancel</a></td>
       </tr>
      </table>
     </div>
    </td>
   </tr>
  </table>
 </u:safeForm>
</candy:overlay>