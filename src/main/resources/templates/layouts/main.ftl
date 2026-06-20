<#macro main title="" content="" extraScripts="" extraHead="">
    <!DOCTYPE html>
    <html lang="de">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Multimodaler Parlament-Explorer<#if title?has_content> - ${title}</#if></title>

        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
        <link rel="stylesheet" href="/css/styles.css">

        <script src="https://d3js.org/d3.v7.min.js"></script>

        <#if extraHead?has_content>${extraHead}</#if>
    </head>
    <body>
    <header class="bg-primary text-white p-3">
        <div class="container">
            <h1>Multimodaler Parlament-Explorer</h1>
        </div>
    </header>

    <main class="container my-4">
        <#if content?has_content>${content}</#if>
    </main>

    <footer class="bg-dark text-white p-3">
        <div class="container">
            <p class="mb-0">© 2025 Multimodaler Parlament-Explorer</p>
        </div>
    </footer>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>

    <#if extraScripts?has_content>${extraScripts}</#if>
    </body>
    </html>
</#macro>