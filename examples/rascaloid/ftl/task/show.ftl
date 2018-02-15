<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>Task: ${task.subject} / Story: <a href="/project/${project.id}/story/${story.id}">${story.subject}</a> / Project: <a href="/project/${project.id}">${project.name}</a></h1>

    <h2>Tasks</h2>

    <a href="/project/${project.id}/story/${story.id}/task/${task.id}/edit">Edit</a>
    <a href="/project/${project.id}/story/${story.id}/task/${task.id}/delete">Delete</a>
</@layout.layout>
