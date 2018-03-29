<#import "../layout.ftlh" as layout>
<@layout.layout "All posts">
    <h1>${post.title}</h1>
    <p>${post.body}</p>
    <p>${post.userId}</p>
    <a href="/">Back</a>
</@layout.layout>
