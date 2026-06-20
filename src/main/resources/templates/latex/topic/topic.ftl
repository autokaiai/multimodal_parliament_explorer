<#include "../imports/imports.ftl">
\title{\textbf{Deutscher Bundestag} \\ \large{Export aller Reden des Topics "${topicId}".}}
\date{Exportiert am ${.now?string("dd.MM.yyyy")}}
\begin{document}
\maketitle
\newpage
\tableofcontents
\newpage

<#list speeches as speech>
    <#assign speaker = speech.speakerObject!"">
    <#if speaker?has_content>
        <#assign sectionTitle = "Rede vom " + speech.protocol.date?date("yyyy.MM.dd HH:mm:ss")?string("dd.MM.yyyy") + " | " + speaker.firstName + " " + speaker.name>
    <#else>
        <#assign sectionTitle = "Rede vom " + speech.protocol.date?date("yyyy.MM.dd HH:mm:ss")?string("dd.MM.yyyy") + " | Gastredner">
    </#if>
    \section*{${sectionTitle}}
    \addcontentsline{toc}{section}{${sectionTitle}}

    <#if speaker?has_content>
        \subsection*{Informationen über <#if speaker.geschlecht == "männlich">den Redner<#else>die Rednerin</#if> - ${speaker.firstName} ${speaker.name}}
        <#include "speakerInfoTopic.ftl">
    </#if>

    <#include "speechCoreTopic.ftl">

    <#assign nlpStats = linguisticService.getLinguisticFeatures(speech._id)!"">
    <#if nlpStats?? && nlpStats?has_content>
        <#if disableTikz>
            <#include "nlpStatsNoTikZTopic.ftl">
        <#else>
            <#include "nlpStatsTikZTopic.ftl">
        </#if>
    </#if>
</#list>
\end{document}
