<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>Edit Task</h1>
    <form method="post" action="/project/${projectId}/story/${storyId}/task/${taskId}/update">
        <#if iterationId??>
            <input type="hidden" name="iterationId" value="${iterationId}">
        </#if>
        <div class="form-group">
            <label for="task-subject">Subject:</label>
            <input id="task-subject" class="form-control" type="text" name="subject" value="${task.subject}">
        </div>
        <div class="form-group">
            <label for="task-description">Description:</label>
            <textarea id="task-description" class="form-control" name="description">${task.description}</textarea>
        </div>
        <div class="form-group">
            <label for="task-estimated-hours">Estimated hours:</label>
            <input id="task-estimated-hours" class="form-control" type="number" name="estimatedHours" value="${task.estimatedHours}">
        </div>
        <div class="form-group">
            <label for="task-stauts">Status:</label>
            <select id="task-status" class="form-control" name="statusId">
                <#list taskStatus as state>
                    <option value="${state.id}"<#if state.id = task.statusId>selected="selected"</#if>>${state.name}</option>
                </#list>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">Save</button>
    </form>
</@layout.layout>
