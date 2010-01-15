<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-relate">
 <u:safeForm method="post" action="${Viewlet.postUrl}">
  <h2>Relate two MeshObjects</h2>
  <table>
   <tr>
    <td class="label">MeshObject:</td>
    <td>
     <input class="subject" name="shell.subject" size="32" readonly="readonly" />
     <input type="hidden" name="shell.subject.to.object.perform" value="relate" />
    </td>
   </tr>
   <tr>
    <td class="label">New&nbsp;Neighbor:</td>
    <td>
     <input class="object" name="shell.object" size="32" />
    </td>
   </tr>
   <tr>
    <td colspan="2">
     <div class="dialog-buttons">
      <table class="dialog-buttons">
       <tr>
        <td><input type="submit" value="Relate" /></td>
        <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-relate' )">Cancel</a></td>
       </tr>
      </table>
     </div>
    </td>
   </tr>
  </table>
 </u:safeForm>
</candy:overlay>