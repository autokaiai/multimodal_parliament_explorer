<#list protocol.agendaSpeechesMap as agenda, speeches>
    <#assign sectionTitle = agenda.index>
    \section*{${sectionTitle}}
    \addcontentsline{toc}{section}{${sectionTitle}}
    <#list speeches as speech>
        <#assign speaker = speech.speakerObject!"">
        <#if speaker?has_content>
            <#assign sectionTitle = "Rede " + (speech?index + 1) + " | " + speaker.firstName + " " + speaker.name>
        <#else>
            <#assign sectionTitle = "Rede " + (speech?index + 1) + " | Gastredner">
        </#if>
        \subsection*{${sectionTitle}}
        \addcontentsline{toc}{subsection}{${sectionTitle}}

        <#if speaker?has_content>
            \subsubsection*{Informationen über <#if speaker.geschlecht == "männlich">den Redner<#else>die Rednerin</#if> - ${speaker.firstName} ${speaker.name}}
            <#include "speakerInfoSingleProtocol.ftl">
        </#if>

        <#include "speechCoreSingleProtocol.ftl">

        <#assign nlpStats = linguisticService.getLinguisticFeatures(speech._id)!"">
        <#if nlpStats?? && nlpStats?has_content>
            <#if disableTikz>
                <#include "nlpStatsNoTikZSingleProtocol.ftl">
            <#else>
                <#include "nlpStatsTikZSingleProtocol.ftl">
            </#if>
        </#if>
    </#list>
    \newpage
</#list>
