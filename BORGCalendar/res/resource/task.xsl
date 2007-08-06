<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" encoding="ISO-8859-1" />

<xsl:template match="/">

     <!--################# START CONFIG #################-->
          <xsl:variable name="columns" select="1" />             <!-- change column-number here -->
          <xsl:variable name="baseFontSize" select="8" />        <!-- change baseFontSize here -->
          <xsl:variable name="font-family" select="'Verdana'" /> <!-- change font-family here -->
     <!--################## END CONFIG ##################-->
     
     <xsl:text disable-output-escaping="yes"><![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]></xsl:text>
     <html><head><title>Task List</title></head><body>          
          <table border="1">
          <tr>          
               <xsl:for-each select="TASKS/Task">               
               
                    <td>
                         
                         <table border="0">
                              <xsl:attribute name="style">font-size:<xsl:value-of select="$baseFontSize" />pt; font-family:<xsl:value-of select="$font-family" />;</xsl:attribute>
                              <xsl:for-each select="./*[not(self::KEY or self::TodoList or self::UserTask1 or self::UserTask2 or self::UserTask3 or self::UserTask4 or self::UserTask5)]"> <!-- the KEY isn't interesting; if you don't want all nodes to be printed, change this condition -->
                                   <tr>
                                        <td><xsl:value-of select="name(.)" />: </td>
                                        <td>
                                             <xsl:if test="self::TaskNumber or self::State or self::DueDate"> <!-- print names bold and big -->
                                                  <xsl:attribute name="style">font-size:<xsl:value-of select="$baseFontSize+2" />pt; text-decoration:underline; font-weight:bold; font-family:<xsl:value-of select="$font-family" />;</xsl:attribute>
                                             </xsl:if>
                                             <xsl:value-of select="." />
                                        </td>
                                   </tr>
                              </xsl:for-each>
                         </table>               
                    </td>
                    <xsl:if test="(position() mod $columns)=0">                         
                         <xsl:text disable-output-escaping="yes"><![CDATA[</tr><tr>]]></xsl:text> <!-- new column -->
                    </xsl:if>
               </xsl:for-each>               
          </tr>
          </table>          
          <p>
               <a href="http://validator.w3.org/">
                    <img border="0" src="http://www.w3.org/Icons/valid-html401" alt="Valid HTML 4.01!" height="31" width="88" />
               </a>
          </p>
     </body></html>
</xsl:template>

</xsl:stylesheet>
