<%@    page contentType="text/html"
 %><%@ page pageEncoding="UTF-8"
 %><%@ taglib prefix="c"    uri='http://java.sun.com/jsp/jstl/core'
 %><%@ taglib prefix="u"    uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="tmpl" uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
 <head>
  <title><tmpl:inline sectionName="html-title"/></title>
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default/master.css" type="text/css" />
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default-iframe/layout.css" type="text/css" />
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default-iframe/color.css"  type="text/css" />
  <tmpl:inline sectionName="html-head"/>
 </head>
 <body>
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
 </body>
</html>
