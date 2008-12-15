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
  <tmpl:inline sectionName="html-head"/>
 </head>
 <body>
  <tmpl:ifErrors>
   <div class="errors">
    <tmpl:inlineErrors stringRepresentation="Html"/>
   </div>
  </tmpl:ifErrors>
  <tmpl:inline sectionName="text-default"/>
 </body>
</html>
