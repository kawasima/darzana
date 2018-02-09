<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>Story: ${story.subject} / Project: <a href="/project/${project.id}">${project.name}</a></h1>

    <h2>Tasks</h2>
    <#list tasks>
        <table class="table">
            <thead>
                <th>#</th>
                <th>subject</th>
                <th>status</th>
            </thead>
            <tbody>
                <#items as task>
                    <tr>
                        <td><a href="/project/${project.id}/iteration/${task.id}">${task.id}</a></td>
                        <td>${task.subject}</td>
                        <td>${task.statusName}</td>
                    </tr>
                </#items>
            </tbody>
        </table>
        <#else>
        <p>No tasks</p>
    </#list>
    <a href="/project/${project.id}/story/${story.id}/tasks/new">New Task</a>
</@layout.layout>
