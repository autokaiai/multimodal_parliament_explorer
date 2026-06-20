<#include "../imports/imports.ftl">
\title{\textbf{Deutscher Bundestag} \\ \large{Export aller Reden von ${speaker.firstName} ${speaker.name}}}
\date{Exportiert am ${.now?string("dd.MM.yyyy")}}
\begin{document}
\maketitle
\newpage
\tableofcontents
\newpage
\section*{Informationen über <#if speaker.geschlecht == "männlich">den Redner<#else>die Rednerin</#if> - ${speaker.firstName} ${speaker.name}}
\addcontentsline{toc}{section}{Informationen über <#if speaker.geschlecht == "männlich">den Redner<#else>die Rednerin</#if> - ${speaker.firstName} ${speaker.name}}

<#include "speakerInfoSpeaker.ftl">

<#list speeches as speech>
    <#assign sectionTitle = "Rede vom " + speech.protocol.date?date("yyyy.MM.dd HH:mm:ss")?string("dd.MM.yyyy")>
    \section*{${sectionTitle}}
    \addcontentsline{toc}{section}{${sectionTitle}}

    <#include "speechCoreSpeaker.ftl">

    <#assign nlpStats = linguisticService.getLinguisticFeatures(speech._id)!"">
    <#if nlpStats?? && nlpStats?has_content>
        <#if disableTikz>
            <#include "nlpStatsNoTikZSpeaker.ftl">
        <#else>
            <#include "nlpStatsTikZSpeaker.ftl">
        </#if>
    </#if>
</#list>
\end{document}
