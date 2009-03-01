<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-accessLocally">
 <u:safeForm method="post" action="${Viewlet.postUrl}">
  <h2>Open a remote MeshObject</h2>
  <table>
   <tr>
    <td class="label">Identifier:</td>
    <td>
     <input class="subject" name="shell.subject" size="32"/>
     <input type="hidden" name="shell.subject.access" value="find" />
    </td>
   </tr>
   <tr>
    <td colspan="2">
     <table class="dialog-buttons">
      <tr>
       <td><input type="submit" value="Open" /></td>
       <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-accessLocally' )">Cancel</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </u:safeForm>
</candy:overlay>