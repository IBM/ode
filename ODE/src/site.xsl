<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes"/>

  <!--
    =========================================================================
    Parameters:  new_elements      The file containing the default target
                                   definitions.  This value is the default
                                   when not passed into the stylesheet.
    =========================================================================
  -->
  <xsl:param name="new_elements">ship/site3.xml</xsl:param>
  
  <!--
    =========================================================================
    Get all feature, archive, and category-def nodes from existing site.xml.
    =========================================================================
  -->

  <xsl:variable name="existing_features"
                select="/site/feature/@url"/>
                
  <xsl:variable name="existing_archives"
                select="/site/archive/@path"/>
                
  <xsl:variable name="existing_categories"
                select="/site/category-def/@name"/>
                
  <!--
    =========================================================================
    Get all feature, archive, and category-def nodes from new site.xml.
    =========================================================================
  -->
  
  <xsl:variable name="new_features"
                select="document($new_elements)/site/feature"/>
                
  <xsl:variable name="new_archives"
                select="document($new_elements)/site/archive"/>
                
  <xsl:variable name="new_categories"
                select="document($new_elements)/site/category-def"/>
                
  <!--
    =========================================================================
    Remove all duplicate nodes in the new site.xml that appear in the
    existing site.xml.
    =========================================================================
  -->
  
  <xsl:variable name="added_features"
                select="$new_features[not(/site/feature/@url=$existing_features)]"/>
                
  <xsl:variable name="added_archives"
                select="$new_archives[not(/site/archive/@path=$existing_archives)]"/>
                
  <xsl:variable name="added_categories"
                select="$new_categories[not(/site/category-def/@name=$existing_categories)]"/>
                
  <!--
    =========================================================================
      Purpose:  Copy the unique nodes from the new site.xml to the output
                tree.
    =========================================================================
  -->
                
  <xsl:template match="/site">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
      
      <xsl:for-each select="$added_features">
        <xsl:copy>
          <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
      </xsl:for-each>
      
      <xsl:for-each select="$added_archives">
        <xsl:copy>
          <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
      </xsl:for-each>
      
      <xsl:for-each select="$added_categories">
        <xsl:copy>
          <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
      </xsl:for-each>
      
    </xsl:copy>
  </xsl:template>
  
  <!--
    =========================================================================
      Purpose:  Copy each node and attribute, exactly as found, to the output
                tree.
    =========================================================================
  -->
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
