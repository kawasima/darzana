<#import "layout.ftl" as layout>
<@layout.layout>
    <h1>Error</h1>

    <#list error?keys as k>
        ${k}
    </#list>
</@layout.layout>
