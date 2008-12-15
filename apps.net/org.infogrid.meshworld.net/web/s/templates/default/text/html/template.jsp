<%@
   page contentType="text/html"%><%@
   page pageEncoding="UTF-8"%><%@
   taglib prefix="c"    uri='http://java.sun.com/jsp/jstl/core'%><%@
   taglib prefix="u"    uri="/v/org/infogrid/jee/taglib/util/util.tld"%><%@
   taglib prefix="tmpl" uri="/v/org/infogrid/jee/taglib/templates/templates.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 <head>
  <title><tmpl:inline sectionName="html-title"/></title>
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default/master.css" type="text/css" />
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default/layout.css" type="text/css" />
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default/color.css"  type="text/css" />
  <tmpl:inline sectionName="html-head"/>
 </head>
 <body>
  <div id="canvas-top">
   <div id="canvas-app-row">
    <div class="canvas-main">
     <a class="infogrid" href="http://infogrid.org/"><img src="${CONTEXT}/s/images/infogrid-medium.png" alt="[InfoGrid logo]" /></a>
     <a href="${CONTEXT}/"><img id="app-logo" src="${CONTEXT}/s/images/meshworld.png" alt="[Logo]" /></a>
     <h1><a href="${CONTEXT}/">The NetMesh World</a></h1>
    </div>
   </div>
  </div>
  <div id="canvas-middle">
   <div class="canvas-main">
    <tmpl:ifErrors>
     <div class="errors">
      <h2>Errors:</h2>
      <tmpl:inlineErrors stringRepresentation="Html"/>
     </div>
    </tmpl:ifErrors>
    <tmpl:inline sectionName="text-default"/>
   </div>
  </div>
  <div id="canvas-bottom">
   <div class="canvas-main footnote">
    &copy; 2001-2008 NetMesh Inc. All rights reserved. NetMesh and InfoGrid are trademarks or registered
    trademarks of NetMesh Inc. Learn more about <a href="http://infogrid.org/">InfoGrid&trade;</a>.
   </div>
  </div>
 </body>
</html>
