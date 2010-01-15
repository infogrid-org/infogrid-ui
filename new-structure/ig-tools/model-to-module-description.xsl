<?xml version="1.0"?>
<!--
    This file is part of InfoGrid(tm). You may not use this file except in
    compliance with the InfoGrid license. The InfoGrid license and important
    disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
    have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
    or you do not consent to all aspects of the license and the disclaimers,
    no license is granted; do not use this file.
 
    For more information about InfoGrid go to http://infogrid.org/

    Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
    All rights reserved.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect" extension-element-prefixes="redirect">
 <xsl:output method="html" indent="yes"/>

 <xsl:template match="/">
  <html>
   <head>
    <title>Module Overview for Module <xsl:apply-templates select="model/subjectarea/name"/></title>
   </head>
   <body>
    <xsl:choose>
     <xsl:when test='model/subjectarea/userdescription'>
      <p>
       <xsl:apply-templates select="model/subjectarea/userdescription"/>
      </p>
     </xsl:when>
    </xsl:choose>
   </body>
  </html>
 </xsl:template>

</xsl:stylesheet>
