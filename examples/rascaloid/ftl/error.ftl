<#import "layout.ftl" as layout>
<@layout.layout>
    <h1>Error</h1>
    <#list error as k, v>
        ${k} = ${v.message}
    </#list>
</@layout.layout>
