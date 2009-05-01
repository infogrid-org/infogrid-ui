<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-setProperty">
 <u:safeForm method="post" action="${Viewlet.postUrl}">
  <h2>Set a property of a MeshObject</h2>
  <table>
   <tr>
    <td class="label">MeshObject:</td>
    <td>
     <input class="subject" name="shell.subject" size="32" readonly="readonly" />
    </td>
   </tr>
   <tr>
    <td class="label">Property Type:</td>
    <td><input class="propertytype" name="shell.subject.propertytype.1" size="32" /></td>
   </tr>
   <tr>
    <td class="label">Property Value:</td>
    <td><input class="propertyvalue" name="shell.subject.propertyvalue.1" size="32" /></td>
   </tr>
   <tr>
    <td colspan="2">
     <table class="dialog-buttons">
      <tr>
       <td><input type="submit" value="Set property" /></td>
       <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-setProperty' )">Cancel</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </u:safeForm>
</candy:overlay>