<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:param name="width" select="400"/>
<xsl:param name="height" select="200"/>
<xsl:template name="main" match="/">  
  <div> <!-- Set Style -->    
  <style type="text/css" media="all"> 
    td.at {        width:  <xsl:value-of select="$width * 0.142"/>px; height: <xsl:value-of select="$height * 0.125"/>px;        border: 2px solid #000;        background-color: #9999CC;        align: center;  valign: middle;    }      
    table.board {   background-color: #000000;  }      img.piece {        width:   <xsl:value-of select="$width * 0.9 * 0.142"/>px;        height:   <xsl:value-of select="$height * 0.9 * 0.142"/>px;              }   </style>        
  <!-- Draw Board -->    
  <xsl:call-template   name="board">      
    <xsl:with-param name="cols" select="15"/>   
    <xsl:with-param name="rows" select="8"/>   
  </xsl:call-template>     
 </div>   
</xsl:template>
  <xsl:template name="at" match="state/fact"> 
  <xsl:param name="row" select="1"/>  <xsl:param name="col"   select="1"/> 
  <td class="at">  
  <xsl:attribute name="id">  
    <xsl:value-of select="'at_'"/>   
      <xsl:value-of   select="$row"/>   <xsl:value-of select="$col"/>   
  </xsl:attribute>    
  
  <xsl:choose>   
    <xsl:when test="//fact[relation='rock' and argument[1] = $row and argument[2] = $col]"> 
      <xsl:attribute   name="style">background-color: #99FFFF</xsl:attribute> 
    </xsl:when>   
  </xsl:choose>  

  <center>   

  <xsl:choose>   
    <xsl:when test="//fact[relation='at' and   argument[2]=$row and argument[3]=$col and argument[1]='red']"> 
      <img   class="piece" src="&ROOT;/resources/images/discs/red.png"/>   
    </xsl:when>   
    <xsl:when test="//fact[relation='at' and   argument[2]=$row and argument[3]=$col and argument[1]='yellow']">  
      <img class="piece"   src="&ROOT;/resources/images/discs/yellow.png"/> 
    </xsl:when>  
    <xsl:when test="//fact[relation='at' and argument[2]=$row and   argument[3]=$col and argument[1]='blue']"> 
       <img class="piece"   src="&ROOT;/resources/images/discs/blue.png"/> 
    </xsl:when> 
    <xsl:when test="//fact[relation='at' and argument[2]=$row and   argument[3]=$col and argument[1]='green']"> 
         <img class="piece"   src="&ROOT;/resources/images/discs/green.png"/> 
    </xsl:when>   
  </xsl:choose>

  </center>  
  </td>  
  </xsl:template>
  
  <xsl:template   name="board_row">  
    <xsl:param name="cols" select="1"/>   <xsl:param name="rows" select="1"/>
    <xsl:param name="row"  select="1"/>  <xsl:param name="col" select="1"/>
    <xsl:call-template name="at">    
      <xsl:with-param name="row" select="$row"/>  
      <xsl:with-param name="col" select="$col"/>   
    </xsl:call-template>  
    <xsl:if test="$col &lt; $cols">
      <xsl:call-template name="board_row">     
        <xsl:with-param   name="cols" select="$cols"/>     
        <xsl:with-param name="rows"   select="$rows"/>  
        <xsl:with-param name="row"   select="$row"/> 
        <xsl:with-param name="col" select="$col + 1"/>  
      </xsl:call-template>  
    </xsl:if>
  </xsl:template>
  <xsl:template name="board_rows">  
    <xsl:param name="cols" select="1"/>  <xsl:param name="rows"   select="1"/>  
    <xsl:param name="row" select="1"/>  
    <tr>   
      <xsl:call-template name="board_row"> 
      <xsl:with-param   name="cols" select="$cols"/> 
      <xsl:with-param name="rows"   select="$rows"/>  
      <xsl:with-param name="row" select="$row"/>  
      </xsl:call-template>  
    </tr> 
    <xsl:if test="$row &lt; $rows"> 
      <xsl:call-template name="board_rows">  
        <xsl:with-param   name="cols" select="$cols"/>   
        <xsl:with-param name="rows"   select="$rows"/>  
        <xsl:with-param name="row" select="$row + 1"/>   
      </xsl:call-template>  
     </xsl:if>
  </xsl:template>
  <xsl:template name="board"> 
    <xsl:param name="cols" select="1"/>
    <xsl:param name="rows" select="1"/> 
    <table class="board"> 
    <xsl:call-template   name="board_rows">  
      <xsl:with-param name="cols"   select="$cols"/>  
      <xsl:with-param name="rows"   select="$rows"/> 
    </xsl:call-template> 
    </table>
  </xsl:template>
</xsl:stylesheet> 
