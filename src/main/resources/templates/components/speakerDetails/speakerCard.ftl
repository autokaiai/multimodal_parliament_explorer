
<#macro speakerCard speaker>
    <div class="speaker-info-card content-section">
        <div class="speaker-header">
            <#if speech.speakerObject.imageData??>
                <img src="data:image/jpeg;base64,${speech.speakerObject.imageData}"
                     alt="Bild des Speakers" class="speaker-image"/>
            <#else>
                <div class="speaker-image bg-light d-flex justify-content-center align-items-center">
                    <i class="fas fa-user fa-3x text-secondary"></i>
                </div>
            </#if>

            <div class="speaker-basic-info">
                <h3 class="speaker-name">
                    ${speech.speakerObject.title!""} ${speech.speakerObject.firstName!""} ${speech.speakerObject.name!""}
                </h3>
                <span class="speaker-party">${speech.speakerObject.party!"-"}</span>
                <div class="d-flex flex-wrap">
                    <#if speech.speakerObject.beruf?? && speech.speakerObject.beruf != "">
                        <span class="badge bg-light text-dark me-2 mb-2">${speech.speakerObject.beruf}</span>
                    </#if>
                    <#if speech.speakerObject.akademischertitel?? && speech.speakerObject.akademischertitel != "">
                        <span class="badge bg-light text-dark me-2 mb-2">${speech.speakerObject.akademischertitel}</span>
                    </#if>
                </div>
            </div>
        </div>

        <div class="section-toggle" data-target="speaker-details">
            <div class="d-flex justify-content-between align-items-center p-3 bg-light">
                <h5 class="mb-0">Persönliche Informationen</h5>
                <i class="fas fa-chevron-down"></i>
            </div>
        </div>

        <div id="speaker-details" class="toggle-section">
            <div class="info-grid">
                <#if speech.speakerObject.geburtsdatum?? && speech.speakerObject.geburtsdatum != "">
                    <div class="info-item">
                        <div class="info-item-label">Geburtsdatum</div>
                        <div class="info-item-value">${speech.speakerObject.geburtsdatum!"-"}</div>
                    </div>
                </#if>

                <#if speech.speakerObject.geburtsort?? && speech.speakerObject.geburtsort != "">
                    <div class="info-item">
                        <div class="info-item-label">Geburtsort</div>
                        <div class="info-item-value">${speech.speakerObject.geburtsort!"-"}</div>
                    </div>
                </#if>

                <#if speech.speakerObject.sterbedatum?? && speech.speakerObject.sterbedatum != "">
                    <div class="info-item">
                        <div class="info-item-label">Sterbedatum</div>
                        <div class="info-item-value">${speech.speakerObject.sterbedatum!"-"}</div>
                    </div>
                </#if>

                <#if speech.speakerObject.geschlecht?? && speech.speakerObject.geschlecht != "">
                    <div class="info-item">
                        <div class="info-item-label">Geschlecht</div>
                        <div class="info-item-value">${speech.speakerObject.geschlecht!"-"}</div>
                    </div>
                </#if>

                <#if speech.speakerObject.familienstand?? && speech.speakerObject.familienstand != "">
                    <div class="info-item">
                        <div class="info-item-label">Familienstand</div>
                        <div class="info-item-value">${speech.speakerObject.familienstand!"-"}</div>
                    </div>
                </#if>

                <#if speech.speakerObject.religion?? && speech.speakerObject.religion != "">
                    <div class="info-item">
                        <div class="info-item-label">Religion</div>
                        <div class="info-item-value">${speech.speakerObject.religion!"-"}</div>
                    </div>
                </#if>
            </div>

            <#if speech.speakerObject.vita?? && speech.speakerObject.vita != "">
                <div class="px-4 pb-4">
                    <h6>Vita</h6>
                    <p>${speech.speakerObject.vita!"-"}</p>
                </div>
            </#if>
        </div>

        <#if speech.speakerObject.memberships?? && (speech.speakerObject.memberships?size > 0)>
            <div class="section-toggle" data-target="memberships-section">
                <div class="d-flex justify-content-between align-items-center p-3 bg-light">
                    <h5 class="mb-0">Mitgliedschaften (${speech.speakerObject.memberships?size})</h5>
                    <i class="fas fa-chevron-down"></i>
                </div>
            </div>

            <div id="memberships-section" class="toggle-section collapsed">
                <div class="p-3">
                    <#list speech.speakerObject.memberships as membership>
                        <div class="membership-card">
                            <div class="membership-header">${membership.label!"-"}</div>
                            <div class="membership-period">
                                Von: ${membership.begin?string("yyyy-MM-dd")!"-"}
                                <#if membership.end??>
                                    Bis: ${membership.end?string("yyyy-MM-dd")!"-"}
                                </#if>
                            </div>
                            <div>${membership.member!"-"}</div>
                        </div>
                    </#list>
                </div>
            </div>
        <#else>
            <div class="p-3 text-muted">
                <i class="fas fa-info-circle me-2"></i>Keine Mitgliedschaften vorhanden
            </div>
        </#if>
    </div>
</#macro>
