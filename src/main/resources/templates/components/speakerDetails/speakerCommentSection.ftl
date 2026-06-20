<#macro speakerCommentSection speech>
    <div class="content-section">
        <h3 class="section-header">Kommentar abgeben</h3>
        <form id="commentForm" method="post" action="/api/speech/${speech._id}/comment">
            <div class="mb-3">
                <label for="commentText" class="form-label">Kommentar</label>
                <textarea class="form-control" id="commentText" name="text" rows="3" required></textarea>
            </div>
            <button type="submit" class="btn btn-primary">Kommentar absenden</button>
        </form>
    </div>

    <script>
        // AJAX-basierte Formularübermittlung, um Seitenreload zu vermeiden
        document.getElementById("commentForm").addEventListener("submit", function(e) {
            e.preventDefault();
            const form = e.target;
            const commentText = document.getElementById("commentText").value;
            const speechId = "${speech._id}";
            // Erstelle das Kommentar-Objekt als JSON
            const commentData = { text: commentText };

            fetch(`/api/speech/${speech._id}/comment`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(commentData)
            })
                    .then(response => {
                        if (!response.ok) {
                            return response.json().then(data => {
                                throw new Error(data.error || "Fehler beim Absenden des Kommentars");
                            });
                        }
                        return response.json();
                    })
                    .then(data => {
                        alert("Kommentar wurde erfolgreich hinzugefügt.");
                        form.reset();
                        // Optional: Hier könntest du den Kommentarbereich aktualisieren
                    })
                    .catch(error => {
                        alert("Fehler: " + error.message);
                    });
        });
    </script>
</#macro>
