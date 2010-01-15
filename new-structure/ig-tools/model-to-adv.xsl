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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes"/>
<xsl:template match="model">
<modelmodule>
  <xsl:apply-templates select="subjectarea"/>
</modelmodule>
</xsl:template>
<xsl:template match="subjectarea">
  <name>
<xsl:value-of select='name'/>
  </name>
  <version>
<xsl:value-of select='version'/>
  </version>
  <username>
<xsl:value-of select='username'/>
  </username>
  <provides>
    <jar>
<xsl:value-of select='name'/><xsl:text>.jar</xsl:text>
<!-- <xsl:value-of select='name'/>.V<xsl:value-of select='version'/><xsl:text>.jar</xsl:text> -->
    </jar>
  </provides>
  <dependencies>
    <requires name="org.infogrid.kernel"/>
<xsl:apply-templates select="dependson/subjectareareference"/>
<xsl:apply-templates select="dependson/modulereference"/>
  </dependencies>
</xsl:template>
<xsl:template match="subjectareareference|modulereference">
  <requires>
    <xsl:attribute name="name">
<xsl:value-of select='name'/>
    </xsl:attribute>
    <xsl:attribute name="version">
<xsl:value-of select='minversion'/>
    </xsl:attribute>
  </requires>
</xsl:template>
</xsl:stylesheet>
