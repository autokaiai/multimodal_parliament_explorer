<#list protocol.agendaSpeechesMap as agenda, speeches>
    <#assign sectionTitle = agenda.index>
    \subsection*{${sectionTitle}}
    \addcontentsline{toc}{subsection}{${sectionTitle}}
    <#list speeches as speech>
        <#assign speaker = speech.speakerObject!"">
        <#if speaker?has_content>
            <#assign sectionTitle = "Rede " + (speech?index + 1) + " | " + speaker.firstName + " " + speaker.name>
        <#else>
            <#assign sectionTitle = "Rede " + (speech?index + 1) + " | Gastredner">
        </#if>
        \subsubsection*{${sectionTitle}}
        \addcontentsline{toc}{subsubsection}{${sectionTitle}}

        <#if speaker?has_content>
            \boldsection{Informationen über <#if speaker.geschlecht == "männlich">den Redner<#else>die Rednerin</#if> - ${speaker.firstName} ${speaker.name}}
            <#include "speakerInfoMultipleProtocols.ftl">
        </#if>

        <#include "speechCoreMultipleProtocols.ftl">

        <#assign nlpStats = linguisticService.getLinguisticFeatures(speech._id)!"">
        <#if nlpStats?? && nlpStats?has_content>
            <#if disableTikz>
                <#include "nlpStatsNoTikZMultipleProtocols.ftl">
            <#else>
                <#include "nlpStatsTikZMultipleProtocols.ftl">
            </#if>
        </#if>
    </#list>
    \newpage
</#list>
