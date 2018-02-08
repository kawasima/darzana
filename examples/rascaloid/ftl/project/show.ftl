<#import "../layout.ftl" as layout>
<@layout.layout>
    <h1>Project: ${project.name}</h1>

    <h2>Iterations</h2>
    <#list iterations>
        <table class="table">
            <thead>
                <th>#</th>
                <th>Name</th>
                <th>Date</th>
            </thead>
            <tbody>
                <#items as iteration>
                    <tr>
                        <td><a href="/project/${project.id}/iteration/${iteration.id}">${iteration.id}</a></td>
                        <td>${iteration.subject}</td>
                        <td></td>
                    </tr>
                </#items>
            </tbody>
        </table>
        <#else>
        <p>No iterations</p>
    </#list>
    <a href="/project/${project.id}/iterations/new">New Iteration</a>
</@layout.layout>
