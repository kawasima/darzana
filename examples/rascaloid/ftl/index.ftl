<#import "layout.ftl" as layout>
<@layout.layout>
    <h1>Projects</h1>
    <#list projects>
        <table class="table">
            <tbody>
                <tr>
                    <td>#</td>
                    <td>Name</td>
                    <td>Description</td>
                </tr>
                <#items as project>
                    <tr>
                        <td><a href="/project/${project.id}">${project.id}</a></td>
                        <td>${project.name}</td>
                        <td>${project.description}</td>
                    </tr>
                </#items>
            </tbody>
        </table>
        <#else>
        <p>No projects</p>
    </#list>
    <a href="/projects/new">New project</a>
</@layout.layout>
