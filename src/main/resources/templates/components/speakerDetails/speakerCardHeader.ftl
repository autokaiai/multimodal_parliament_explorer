<#macro speakerCardHeader speech>
    <div class="card-header d-flex justify-content-between align-items-center">
        <h2 class="mb-0">Rede Details</h2>
        <div>
            <button type="button" class="btn btn-outline-secondary me-2" id="export-pdf-btn">
                Export als PDF
            </button>
            <button type="button" class="btn btn-outline-secondary me-2" id="export-xml-btn">
                Export als XML
            </button>
            <button type="button" class="btn btn-outline-danger me-2" id="delete-btn">
                Rede löschen
            </button>
            <button type="button" class="btn btn-outline-primary" onclick="window.location.href='/'">
                Zurück zur Übersicht
            </button>
        </div>
    </div>

    <script>
        // Funktion zum Downloaden via fetch und Blob
        function triggerDownload(url, defaultFileName) {
            fetch(url)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Network response was not ok");
                        }
                        return response.blob();
                    })
                    .then(blob => {
                        const urlBlob = window.URL.createObjectURL(blob);
                        const a = document.createElement("a");
                        a.style.display = "none";
                        a.href = urlBlob;
                        a.download = defaultFileName || "download";
                        document.body.appendChild(a);
                        a.click();
                        window.URL.revokeObjectURL(urlBlob);
                    })
                    .catch(error => console.error("Download error:", error));
        }

        // URLs für den Export
        const pdfUrl = `/export/pdf/speech/?id=${speech._id}`;
        const xmlUrl = `/export/xml/speech/?id=${speech._id}`;

        // Event-Handler für Export-Buttons
        document.getElementById('export-pdf-btn').addEventListener('click', function() {
            triggerDownload(pdfUrl, "Rede_${speech._id}.pdf");
        });

        document.getElementById('export-xml-btn').addEventListener('click', function() {
            triggerDownload(xmlUrl, "Rede_${speech._id}.xml");
        });

        // Event-Handler für den Delete-Button
        document.getElementById('delete-btn').addEventListener('click', function() {
            if (confirm("Möchten Sie diese Rede wirklich löschen?")) {
                fetch(`/api/speech/${speech._id}`, {
                    method: "DELETE"
                })
                        .then(response => {
                            if (response.ok) {
                                alert("Rede wurde erfolgreich gelöscht.");
                                window.location.href = "/";
                            } else {
                                return response.json().then(data => {
                                    throw new Error(data.error || "Fehler beim Löschen");
                                });
                            }
                        })
                        .catch(error => {
                            console.error("Delete error:", error);
                            alert("Fehler beim Löschen: " + error.message);
                        });
            }
        });
    </script>
</#macro>
