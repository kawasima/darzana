<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>New Iteration</h1>
    <form method="post" action="/project/${projectId}/iterations/create">
        <div class="form-group">
            <label for="iteration-subject">Subject:</label>
            <input id="iteration-subject" class="form-control" type="text" name="subject">
        </div>
        <div class="form-group">
            <label for="iteration-description">Description:</label>
            <textarea id="iteration-description" class="form-control" name="description"></textarea>
        </div>
        <div class="form-group">
            <label for="iteration-period">Period:</label>
            <input id="iteration-start-on" class="form-control" type="date" name="startOn" placeholder="start">
            -
            <input id="iteration-end-on" class="form-control" type="date" name="endOn" placeholder="end">
        </div>
        <button type="submit" class="btn btn-primary">Save</button>
    </form>
</@layout.layout>
