<%@
   page contentType="text/html"%><%@
   page pageEncoding="UTF-8"%><%@
   taglib prefix="c"    uri='http://java.sun.com/jsp/jstl/core'%><%@
   taglib prefix="u"    uri="/v/org/infogrid/jee/taglib/util/util.tld"%><%@
   taglib prefix="tmpl" uri="/v/org/infogrid/jee/taglib/templates/templates.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 <head>
  <title><tmpl:inline name="html-title"/></title>
  <link rel="stylesheet" href="${CONTEXT}/s/templates/bland/bland.css" type="text/css" />
  <tmpl:inline name="html-head"/>
 </head>
 <body>
  <div id="canvas-top">
   <div id="canvas-app-row">
    <div class="canvas-main">
     <h1><a href="${CONTEXT}/">The Mesh World</a></h1>
    </div>
   </div>
  </div>
  <div id="canvas-middle">
   <div class="canvas-main">
    <tmpl:ifErrors>
     <div class="errors">
      <tmpl:inlineErrors stringRepresentation="Html"/>
     </div>
    </tmpl:ifErrors>
    <tmpl:inline name="text-default"/>
   </div>
  </div>
  <div id="canvas-bottom">
   <div class="canvas-main footnote">&copy; 2001-2008 NetMesh Inc. All rights
    reserved. NetMesh and InfoGrid are trademarks or registered trademarks of NetMesh Inc.
    Learn more about <a href="http://infogrid.org/">InfoGrid</a>.</div>
  </div>
 </body>
</html>
