<#macro advancedSearchPanel selectedTopics pageSize selectedFaction="">
    <div class="card mb-4 <#if !selectedFaction?? || !selectedFaction?has_content>d-none</#if>" id="advancedSearchPanel">
        <div class="card-body bg-light">
            <h6 class="mb-3">Erweiterte Suche</h6>
            <form id="advancedSearchForm" action="/speeches/paginated" method="get">
                <div class="row g-3">
                    <!-- Zeitraum -->
                    <div class="col-md-4">
                        <label class="form-label">Zeitraum</label>
                        <div class="input-group input-group-sm">
                            <input type="date" name="dateFrom" class="form-control" placeholder="Von">
                            <span class="input-group-text">bis</span>
                            <input type="date" name="dateTo" class="form-control" placeholder="Bis">
                        </div>
                    </div>

                    <!-- Fraktion -->
                    <div class="col-md-3">
                        <label class="form-label">Fraktion</label>
                        <select class="form-select form-select-sm" name="faction">
                            <option value="">Alle Fraktionen</option>
                            <option value="SPD" <#if selectedFaction?? && selectedFaction == "SPD">selected</#if>>SPD</option>
                            <option value="CDU" <#if selectedFaction?? && selectedFaction == "CDU">selected</#if>>CSU</option>
                            <option value="CSU" <#if selectedFaction?? && selectedFaction == "CSU">selected</#if>>CSU</option>
                            <option value="FDP" <#if selectedFaction?? && selectedFaction == "FDP">selected</#if>>FDP</option>
                            <option value="BSW" <#if selectedFaction?? && selectedFaction == "BSW">selected</#if>>BSW</option>
                            <option value="SSW" <#if selectedFaction?? && selectedFaction == "SSW">selected</#if>>SSW</option>
                            <option value="Plos" <#if selectedFaction?? && selectedFaction == "Plos">selected</#if>>Plos</option>
                            <option value="DIE LINKE." <#if selectedFaction?? && selectedFaction == "DIE LINKE.">selected</#if>>Die Linke</option>
                            <option value="BÜNDNIS 90/DIE GRÜNEN" <#if selectedFaction?? && selectedFaction == "BÜNDNIS 90/DIE GRÜNEN">selected</#if>>Bündnis 90/Die Grünen</option>
                            <option value="AfD" <#if selectedFaction?? && selectedFaction == "AfD">selected</#if>>AfD</option>
                        </select>
                    </div>

                    <!-- Themen (Checkbox-Ansicht) -->
                    <div class="col-md-3">
                        <label class="form-label">Themen</label>
                        <div class="d-flex flex-wrap gap-2">
                            <#assign allTopics = [
                            "Social", "Foreign", "Law", "Government", "Health", "Transportation",
                            "Public", "Labor", "Housing", "Defense", "Civil", "Education",
                            "Technology", "Domestic", "Immigration", "Environment", "Agriculture",
                            "Macroeconomics", "Culture", "International"
                            ]>
                            <#list allTopics as topic>
                                <div class="form-check">
                                    <input
                                            class="form-check-input"
                                            type="checkbox"
                                            name="topic"
                                            value="${topic}"
                                            id="topic_${topic}"
                                            <#if selectedTopics?? && selectedTopics?seq_contains(topic)>checked</#if>
                                    />
                                    <label class="form-check-label" for="topic_${topic}">${topic}</label>
                                </div>
                            </#list>
                        </div>
                        <div class="mt-1">
                            <button type="button" class="btn btn-sm btn-outline-secondary" id="selectAllTopics">Alle</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" id="deselectAllTopics">Keine</button>
                        </div>
                    </div>

                    <!-- Submit & Hidden Fields -->
                    <div class="col-md-2 d-flex align-items-end">
                        <input type="hidden" name="page" value="1">
                        <input type="hidden" name="size" value="${pageSize}">
                        <button type="submit" class="btn btn-primary btn-sm w-100">Anwenden</button>
                    </div>
                </div>

                <div class="row mt-3">
                    <div class="col-12">
                        <button type="button" class="btn btn-outline-secondary btn-sm" id="clearFilters">
                            <i class="fas fa-times me-1"></i> Filter zurücksetzen
                        </button>
                        <#if selectedFaction?? && selectedFaction?has_content>
                            <span class="badge bg-info ms-2">
                                Filter aktiv: Fraktion = ${selectedFaction}
                                <a href="/speeches/paginated?page=1&size=${pageSize}" class="text-white ms-1">
                                    <i class="fas fa-times"></i>
                                </a>
                            </span>
                        </#if>
                    </div>
                </div>
            </form>
        </div>
    </div>
</#macro>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const selectAllBtn = document.getElementById('selectAllTopics');
        const deselectAllBtn = document.getElementById('deselectAllTopics');

        if (selectAllBtn) {
            selectAllBtn.addEventListener('click', function() {
                document.querySelectorAll('input[name="topic"]').forEach(checkbox => {
                    checkbox.checked = true;
                });
            });
        }

        if (deselectAllBtn) {
            deselectAllBtn.addEventListener('click', function() {
                document.querySelectorAll('input[name="topic"]').forEach(checkbox => {
                    checkbox.checked = false;
                });
            });
        }
    });
</script>