<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>Iteration: ${iteration.subject} / Project: <a href="/project/${project.id}">${project.name}</a></h1>

    <h2>stories</h2>
    <#list stories>
        <table class="table">
            <thead>
                <th>#</th>
                <th>Name</th>
            </thead>
            <tbody>
                <#items as story>
                    <tr>
                        <td>${story.storyId}</td>
                        <td>${story.subject} (<a href="/project/${project.id}/story/${story.storyId}">${(story.developmentTasks!)?size} tasks</a>)</td>
                    </tr>
                </#items>
            </tbody>
        </table>
        <#else>
        <p>No stories</p>
    </#list>
    <a href="/project/${project.id}/stories/new?iterationId=${iteration.id}">New Story</a>
</@layout.layout>
