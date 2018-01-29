<#import "../layout.ftl" as layout>
<@layout.layout>
    <form method="post" action="/projects/create">
        <div class="form-group">
            <label for="project-name">Name:</label>
            <input id="project-name" class="form-control" type="text" name="name">
        </div>
        <div class="form-group">
            <label for="project-description">Description:</label>
            <textarea id="project-description" class="form-control" name="description"></textarea>
        </div>
        <button type="submit" class="btn btn-primary">Save</button>
    </form>
</@layout.layout>
