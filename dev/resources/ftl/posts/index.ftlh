<#import "../layout.ftlh" as layout>
<@layout.layout "All posts">
<h1>All posts</h1>
<p>This data is fetched from <a href="https://jsonplaceholder.typicode.com/posts">https://jsonplaceholder.typicode.com/posts</a></p>
<ul>
    <#list posts as post>
    <li><a href="/post/${post.id}">${post.title}</a></li>
    </#list>
</ul>
</@layout.layout>
