<#macro speakerAgenda speech>
<#if speech.protocol?? || speech.agenda??>
    <div class="content-section">
        <h3 class="section-header">Sitzungsdetails</h3>

        <div class="row">
            <#if speech.protocol??>
                <div class="col-md-6">
                    <div class="card mb-3">
                        <div class="card-header">Protokoll</div>
                        <div class="card-body">
                            <p><strong>Titel:</strong> ${speech.protocol.title!"-"}</p>
                            <p><strong>Datum:</strong> ${speech.protocol.date!"-"}</p>
                            <p><strong>Ort:</strong> ${speech.protocol.place!"-"}</p>
                        </div>
                    </div>
                </div>
            </#if>

            <#if speech.agenda??>
                <div class="col-md-6">
                    <div class="card mb-3">
                        <div class="card-header">Agenda</div>
                        <div class="card-body">
                            <p><strong>Index:</strong> ${speech.agenda.index!"-"}</p>
                            <p><strong>Titel:</strong> ${speech.agenda.title!"-"}</p>
                        </div>
                    </div>
                </div>
            </#if>
        </div>
    </div>
</#if>
</#macro>
