<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>Story: ${story.subject} / Project: ${project.name}</h1>

    <h2>Tasks</h2>
    <#list tasks>
        <table class="table">
            <thead>
                <th>#</th>
                <th>subject</th>
                <th>description</th>
            </thead>
            <tbody>
                <#items as task>
                    <tr>
                        <td><a href="/project/${project.id}/iteration/${task.id}">${task.id}</a></td>
                        <td>${task.subject}</td>
                        <td></td>
                    </tr>
                </#items>
            </tbody>
        </table>
        <#else>
        <p>No tasks</p>
    </#list>
    <a href="/project/${project.id}/story/${story.id}/tasks/new">New Task</a>
</@layout.layout>
