<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                name="main"
                version="3.0">

<p:input port="source"/>
<p:output port="result"
          serialization="map { 'omit-xml-declaration': true(),
                               'indent': false() }"/>

<p:identity/>

</p:declare-step>
