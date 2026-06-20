document.addEventListener('DOMContentLoaded', function () {
    // Search functionality
    const searchButton = document.getElementById('search-button');
    const searchInput = document.getElementById('search-input');

    if (searchButton && searchInput) {
        searchButton.addEventListener('click', function () {
            const searchTerm = searchInput.value.trim();
            if (searchTerm) {
                // Hier können Sie die Suchfunktion implementieren
                window.location.href = '/speeches/paginated?page=1&size=' + window.pageSize + '&search=' + encodeURIComponent(searchTerm);
            }
        });

        // Enter-Taste für die Suche
        searchInput.addEventListener('keyup', function (event) {
            if (event.key === 'Enter') {
                searchButton.click();
            }
        });
    }

    // Advanced search panel toggle
    const advancedSearchBtn = document.getElementById('advancedSearch');
    const advancedSearchPanel = document.getElementById('advancedSearchPanel');

    if (advancedSearchBtn && advancedSearchPanel) {
        advancedSearchBtn.addEventListener('click', function () {
            advancedSearchPanel.classList.toggle('d-none');
        });
    }

    // Clear filters button
    const clearFiltersBtn = document.getElementById('clearFilters');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', function () {
            window.location.href = '/speeches/paginated?page=1&size=' + window.pageSize;
        });
    }

    // Page size select
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function () {
            const newSize = this.value;
            let url = '/speeches/paginated?page=1&size=' + newSize;
            if (window.selectedFaction && window.selectedFaction.trim().length > 0) {
                url += '&faction=' + encodeURIComponent(window.selectedFaction);
            }
            window.location.href = url;
        });
    }

    // Add sorting indicators
    const sortableHeaders = document.querySelectorAll('th .fa-sort');
    sortableHeaders.forEach(header => {
        header.closest('th').addEventListener('click', function () {
            const headerText = this.textContent.trim().split(" ")[0];
            alert('Sortieren nach: ' + headerText + ' - Diese Funktion ist noch nicht implementiert.');
        });

        header.closest('th').style.cursor = 'pointer';
    });

    // Highlight für aktive Filter
    if (window.location.search.includes('faction=')) {
        advancedSearchPanel.classList.remove('d-none');
    }
});
