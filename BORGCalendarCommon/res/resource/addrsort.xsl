<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes" encoding="ISO-8859-1" omit-xml-declaration="no" />

<xsl:template match="/">
     <ADDRESSES>
          <xsl:for-each select="ADDRESSES/Address">
               <xsl:sort select="LastName" order="ascending" data-type="text" lang="de" case-order="lower-first" />
               <xsl:copy-of select="." />
          </xsl:for-each>
     </ADDRESSES>
</xsl:template>

</xsl:stylesheet>
