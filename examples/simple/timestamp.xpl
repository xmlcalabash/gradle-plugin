<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                name="main"
                version="3.0">

<p:input port="source"/>
<p:output port="result"/>
<p:option name="timestamp" select="current-dateTime()"/>

<p:add-attribute match="/*" attribute-name="timestamp"
                 attribute-value="{$timestamp}"/>

</p:declare-step>
