<#macro tableRow speech>
    <tr>
        <td class="text-center">
        <span class="badge bg-light text-secondary fw-normal">
            ${speech._id?substring(0, 8)}...
        </span>
        </td>
        <td>
            <div class="d-flex align-items-center">
                <#if speech.speakerObject?? && speech.speakerObject.imageData?? && speech.speakerObject.imageData?has_content>
                    <div class="speaker-avatar rounded-circle me-2" style="width: 40px; height: 40px; overflow: hidden;">
                        <img src="data:image/jpeg;base64,${speech.speakerObject.imageData}" alt="Speaker"
                             style="width: 100%; height: 100%; object-fit: cover;"
                             onerror="this.onerror=null; this.style.display='none'; this.parentNode.innerHTML='<div class=\'d-flex justify-content-center align-items-center bg-light\' style=\'width: 40px; height: 40px;\'><i class=\'fas fa-user text-secondary\'></i></div>';" />
                    </div>
                <#else>
                    <div class="speaker-avatar bg-light rounded-circle d-flex justify-content-center align-items-center me-2"
                         style="width: 40px; height: 40px;">
                        <i class="fas fa-user text-secondary"></i>
                    </div>
                </#if>
                <div>
                    <#if speech.speakerObject??>
                        <div class="fw-bold">${speech.speakerObject.name!"Unbekannt"}</div>
                        <div class="small text-muted">${speech.speakerObject.firstName!"Unbekannt"}</div>
                    <#else>
                        <div class="fw-bold">${speech.speaker!"Unbekannt"}</div>
                    </#if>
                </div>
            </div>
        </td>
        <td>
            <div class="d-flex align-items-center">
                <i class="far fa-calendar-alt text-muted me-2"></i>
                <span>${speech.protocol.date[0..9]}</span>
            </div>
        </td>
        <td>
            <#if speech.protocol??>
                <span class="d-inline-block text-truncate" style="max-width: 200px;">
                ${speech.protocol.title?replace("Plenarprotokoll ", "")!"Unbekannt"}
            </span>
            <#else>
                <span class="text-muted">Unbekannt</span>
            </#if>
        </td>
        <td class="text-center">
            <#assign topicCount = speechTopicCounts[speech._id]!0>
            <#if topicCount gt 0>
                <span class="badge bg-info rounded-pill">${topicCount}</span>
            <#else>
                <span class="badge bg-light text-secondary rounded-pill">0</span>
            </#if>
        </td>
        <td class="text-center">
            <div class="btn-group btn-group-sm">
                <a href="/speech/${speech._id}" class="btn btn-outline-primary" title="Details anzeigen">
                    <i class="fas fa-eye"></i>
                </a>
                <button type="button" class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split" data-bs-toggle="dropdown" aria-expanded="false">
                    <span class="visually-hidden">Menü</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                    <li>
                        <a class="dropdown-item" href="/export/pdf/speech/?id=${speech._id}" download>
                            <i class="fas fa-download me-2"></i>Exportieren als PDF
                        </a>
                    </li>
                    <li>
                        <a class="dropdown-item" href="/export/xml/speech/?id=${speech._id}" download>
                            <i class="fas fa-download me-2"></i>Exportieren als XML
                        </a>
                    </li>

                    <li><a class="dropdown-item" href="#"><i class="fas fa-share-alt me-2"></i>Teilen</a></li>
                </ul>
            </div>
        </td>
    </tr>
</#macro>
