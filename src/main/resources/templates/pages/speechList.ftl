<#import "../layouts/main.ftl" as layout>
<#import "../components/searchOptions/advancedSearchPanel.ftl" as advancedSearch>
<#import "../components/speechTable/tableHeader.ftl" as tableHeader>
<#import "../components/speechTable/tableRows.ftl" as tableRow>
<#import "../components/charts/visualizationsAll.ftl" as vis>

<#assign content>
    <div class="container py-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="mb-0"><i class="fas fa-microphone-alt me-2"></i>Parlamentsreden</h1>
            <div class="d-flex">
                <a href="/" class="btn btn-outline-secondary me-2">
                    <i class="fas fa-home"></i> <span class="d-none d-md-inline">Startseite</span>
                </a>
            </div>
        </div>

        <div class="row mb-4">
            <div class="col-lg-12">
                <#include "../components/speechList/protocols_export.ftl">
            </div>
        </div>

        <!-- Visualisierungen einfügen -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                        <h5 class="mb-0"><i class="fas fa-chart-bar me-2"></i>Visualisierungen</h5>
                        <button class="btn btn-sm btn-light" type="button" data-bs-toggle="collapse"
                                data-bs-target="#visualizationsCollapse" aria-expanded="true"
                                aria-controls="visualizationsCollapse">
                            <i class="fas fa-chevron-down"></i>
                        </button>
                    </div>
                    <div class="collapse show" id="visualizationsCollapse">
                        <div class="card-body p-0">
                            <!-- Hier die Visualisierungskomponente einbinden -->
                            <@vis.visualizationsAll />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                <h5 class="mb-0"><i class="fas fa-list me-2"></i>Reden Übersicht</h5>
                <span class="badge bg-light text-primary rounded-pill">
                    ${totalSpeeches} Einträge
                </span>
            </div>
            <div class="card-body">
                <!-- Search and Filter Section -->
                <div class="row g-3 mb-4">
                    <div class="col-md-8">
                        <div class="input-group">
                            <span class="input-group-text bg-white">
                                <i class="fas fa-search text-muted"></i>
                            </span>
                            <input type="text" class="form-control border-start-0" id="search-input"
                                   placeholder="Nach Redner suchen...">
                            <button class="btn btn-primary" type="button" id="search-button">
                                Suchen
                            </button>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="d-flex gap-2 h-100">
                            <div class="dropdown flex-grow-1">
                                <button class="btn btn-outline-secondary dropdown-toggle w-100" type="button"
                                        id="filterDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                                    <i class="fas fa-filter me-1"></i> Filter
                                </button>
                                <ul class="dropdown-menu w-100" aria-labelledby="filterDropdown">
                                    <li><h6 class="dropdown-header">Nach Zeitraum</h6></li>
                                    <li><a class="dropdown-item" href="#">Letzte Woche</a></li>
                                    <li><a class="dropdown-item" href="#">Letzter Monat</a></li>
                                    <li><a class="dropdown-item" href="#">Letztes Jahr</a></li>
                                </ul>
                            </div>
                            <button class="btn btn-outline-secondary" type="button" id="advancedSearch">
                                <i class="fas fa-sliders-h"></i>
                            </button>
                        </div>
                    </div>
                </div>


                <!-- Advanced Search Panel (collapsed by default) -->
                <@advancedSearch.advancedSearchPanel selectedFaction=selectedFaction!"" selectedTopics=selectedTopics pageSize=pageSize />


                <!-- Table -->
                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <@tableHeader.tableHeader />
                        <tbody>
                        <#if speeches?? && speeches?size gt 0>
                            <#list speeches as speech>
                                <@tableRow.tableRow speech=speech />
                            </#list>
                        <#else>
                            <tr>
                                <td colspan="6" class="text-center py-4">
                                    <div class="text-muted">
                                        <i class="fas fa-search fa-2x mb-3 d-block"></i>
                                        <p>Keine Reden gefunden</p>
                                        <small>Bitte versuchen Sie andere Suchkriterien.</small>
                                    </div>
                                </td>
                            </tr>
                        </#if>
                        </tbody>
                    </table>
                </div>


                <#setting url_escaping_charset='UTF-8'>
                <!-- Pagination -->
                <div class="d-flex justify-content-between align-items-center mt-4">
                    <div class="d-flex align-items-center">
                        <label class="me-2 text-nowrap">Einträge pro Seite:</label>
                        <select class="form-select form-select-sm" style="width: 60px;" id="pageSizeSelect">
                            <option value="10" <#if pageSize == 10>selected</#if>>10</option>
                            <option value="25" <#if pageSize == 25>selected</#if>>25</option>
                            <option value="50" <#if pageSize == 50>selected</#if>>50</option>
                            <option value="100" <#if pageSize == 100>selected</#if>>100</option>
                        </select>
                    </div>

                    <nav aria-label="Pagination">
                        <ul class="pagination pagination-sm mb-0">
                            <li class="page-item <#if currentPage lte 1>disabled</#if>">
                                <a class="page-link" href="/speeches/paginated?page=${(currentPage - 1)}&size=${pageSize}
                    <#if selectedFaction?? && selectedFaction?has_content>&faction=${selectedFaction?url}</#if>
                    <#if selectedTopics?? && selectedTopics?size gt 0>
                        <#list selectedTopics as topic>&topic=${topic?url}</#list>
                    </#if>" aria-label="Zurück">
                                    <i class="fas fa-chevron-left"></i>
                                </a>
                            </li>

                            <#if totalPages gt 0>
                                <#list 1..totalPages as p>
                                    <#if p == 1 || p == totalPages || (p >= currentPage - 2 && p <= currentPage + 2)>
                                        <li class="page-item <#if p == currentPage>active</#if>">
                                            <a class="page-link" href="/speeches/paginated?page=${p}&size=${pageSize}
                                <#if selectedFaction?? && selectedFaction?has_content>&faction=${selectedFaction?url}</#if>
                                <#if selectedTopics?? && selectedTopics?size gt 0>
                                    <#list selectedTopics as topic>&topic=${topic?url}</#list>
                                </#if>">${p}</a>
                                        </li>
                                    <#elseif p == 2 || p == totalPages - 1>
                                        <li class="page-item disabled">
                                            <a class="page-link" href="#">...</a>
                                        </li>
                                    </#if>
                                </#list>
                            <#else>
                                <li class="page-item active">
                                    <a class="page-link" href="#">1</a>
                                </li>
                            </#if>

                            <li class="page-item <#if currentPage gte totalPages || totalPages == 0>disabled</#if>">
                                <a class="page-link" href="/speeches/paginated?page=${(currentPage + 1)?c}&size=${pageSize}<#if selectedFaction?? && selectedFaction?has_content>&faction=${selectedFaction?trim?url}</#if><#if selectedTopics?? && selectedTopics?size gt 0><#list selectedTopics as topic><#if topic??>&topic=${topic?trim?url}</#if></#list></#if>" aria-label="Weiter">
                                    <i class="fas fa-chevron-right"></i>
                                </a>
                            </li>
                        </ul>
                    </nav>

                    <div class="text-muted small">
                        <#if speeches?? && speeches?size gt 0>
                            Zeige ${((currentPage - 1) * pageSize) + 1} bis ${((currentPage - 1) * pageSize) + speeches?size} von ${totalSpeeches} Reden
                        <#else>
                            Keine Reden gefunden
                        </#if>
                    </div>
                </div>

            </div>
        </div>
    </div>
</#assign>

<#assign extraScripts>
    <script>
        // Übergibt dynamische Variablen als globale JavaScript-Variablen
        window.pageSize = ${pageSize!20};
        window.selectedFaction = "${selectedFaction!}";

        // Visualisierungsdaten vom Server
        <#if visualizationJson??>
        window.visualizationData = ${visualizationJson};
        console.log("Visualisierungsdaten geladen:", window.visualizationData);
        <#else>
        window.visualizationData = null;
        console.log("Keine Visualisierungsdaten verfügbar");
        </#if>
    </script>

    <script src="/js/speechList.js"></script>
    <script src="/js/charts/bubbleChart.js"></script>
    <script src="/js/charts/barChart.js"></script>
    <script src="/js/charts/radarChart.js"></script>
    <script src="/js/charts/sunburstChart.js"></script>
    <script src="/js/filterHandler.js"></script>
    <script src="/js/visualizations.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/7.8.5/d3.min.js"></script>
</#assign>


<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.2.0/css/all.min.css">

<@layout.main title="Reden Übersicht" content=content extraScripts=extraScripts />