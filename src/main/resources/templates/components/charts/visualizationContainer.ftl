<#macro visualizationContainer selectedRedeIds=[]>
    <div class="visualization-container">

    <!-- Filter-Info-Anzeige mit verbessertem Design -->
    <div class="row mb-3">
        <div class="col-12">
            <div class="alert alert-info d-flex justify-content-between align-items-center" role="alert">
                <div>
                    <i class="fas fa-info-circle me-2"></i>
                    <span id="filter-info">Visualisierung für alle Reden</span>
                </div>
                <button type="button" class="btn btn-sm btn-outline-secondary filter-reset-btn">
                    <i class="fas fa-undo me-1"></i>Filter zurücksetzen
                </button>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6 mb-4">
            <div class="card shadow-sm h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="fas fa-circle me-2"></i>Themen-Bubble-Chart</h5>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary" type="button" id="bubbleChartOptionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="bubbleChartOptionsDropdown">
                            <li><a class="dropdown-item" href="#" id="downloadBubbleChartBtn"><i class="fas fa-download me-2"></i>Als SVG speichern</a></li>
                            <li><a class="dropdown-item" href="#" id="bubbleChartInfoBtn"><i class="fas fa-info-circle me-2"></i>Info</a></li>
                        </ul>
                    </div>
                </div>
                <div class="card-body">
                    <div id="topics-bubble-chart" class="chart-container" style="height: 400px;"></div>
                </div>
            </div>
        </div>

        <div class="col-md-6 mb-4">
            <div class="card shadow-sm h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="fas fa-bars me-2"></i>POS-Barchart</h5>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary" type="button" id="barChartOptionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="barChartOptionsDropdown">
                            <li><a class="dropdown-item" href="#" id="downloadBarChartBtn"><i class="fas fa-download me-2"></i>Als SVG speichern</a></li>
                            <li><a class="dropdown-item" href="#" id="barChartInfoBtn"><i class="fas fa-info-circle me-2"></i>Info</a></li>
                        </ul>
                    </div>
                </div>
                <div class="card-body">
                    <div id="pos-barchart" class="chart-container" style="height: 400px;"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6 mb-4">
            <div class="card shadow-sm h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="fas fa-chart-pie me-2"></i>Sentiment-Radar-Chart</h5>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary" type="button" id="radarChartOptionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="radarChartOptionsDropdown">
                            <li><a class="dropdown-item" href="#" id="downloadRadarChartBtn"><i class="fas fa-download me-2"></i>Als SVG speichern</a></li>
                            <li><a class="dropdown-item" href="#" id="radarChartInfoBtn"><i class="fas fa-info-circle me-2"></i>Info</a></li>
                        </ul>
                    </div>
                </div>
                <div class="card-body">
                    <div id="sentiment-radar-chart" class="chart-container" style="height: 400px;"></div>
                </div>
            </div>
        </div>

        <div class="col-md-6 mb-4">
            <div class="card shadow-sm h-100">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="fas fa-sitemap me-2"></i>Named-Entities-Sunburst</h5>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary" type="button" id="sunburstOptionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="sunburstOptionsDropdown">
                            <li><a class="dropdown-item" href="#" id="downloadSunburstBtn"><i class="fas fa-download me-2"></i>Als SVG speichern</a></li>
                            <li><a class="dropdown-item" href="#" id="sunburstInfoBtn"><i class="fas fa-info-circle me-2"></i>Info</a></li>
                        </ul>
                    </div>
                </div>
                <div class="card-body">
                    <div id="named-entities-sunburst" class="chart-container" style="height: 400px;"></div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Speichern der ausgewählten Rede-IDs
        const selectedRedeIds = ${selectedRedeIds?has_content?then("['" + selectedRedeIds?join("','") + "']", "[]")};
    </script>
</#macro>