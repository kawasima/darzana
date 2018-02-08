<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>New Story</h1>
    <form method="post" action="/project/${projectId}/stories/create">
        <input type="hidden" name="iterationId" value="${iterationId}">
        <div class="form-group">
            <label for="story-subject">Subject:</label>
            <input id="story-subject" class="form-control" type="text" name="subject">
        </div>
        <div class="form-group">
            <label for="story-description">Description:</label>
            <textarea id="story-description" class="form-control" name="description"></textarea>
        </div>
        <div class="form-group">
            <label for="story-point">Point:</label>
            <input id="story-point" class="form-control" type="number" name="point">
        </div>
        <button type="submit" class="btn btn-primary">Save</button>
    </form>
</@layout.layout>
