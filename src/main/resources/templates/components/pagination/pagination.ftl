<#macro pagination currentPage totalPages pageSize selectedFaction selectedTopics totalSpeeches speeches>
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
                <!-- Pagination-Buttons (zurück, Seitenzahlen, weiter) -->
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
</#macro>
