<#include "../imports/imports.ftl">

<#assign protokollName = (speech.protocol?? && speech.protocol.title??)?then(speech.protocol.title, "")>
<#assign rednerName = (speech.speakerObject?? && speech.speakerObject.firstName?? && speech.speakerObject.name??)?then(speech.speakerObject.firstName + " " + speech.speakerObject.name, "")>
<#assign agendaName = (speech.agenda??&& speech.agenda.index??)?then(speech.agenda.index, "")>

<#assign titel = "Export einer Rede"
+ (protokollName?has_content?then(" aus " + protokollName, ""))
+ (rednerName?has_content?then(" von " + rednerName, ""))
+ (agendaName?has_content?then(" - " + agendaName, ""))>

\title{\textbf{Deutscher Bundestag} \\ \large{${titel}}}
\date{Exportiert am ${.now?string("dd.MM.yyyy")}}
\begin{document}
\maketitle
\newpage
\tableofcontents
\newpage
<#assign speaker = speech.speakerObject!"">
<#if speaker?has_content>
    \section*{Informationen über <#if speaker.geschlecht == "männlich">den Redner<#else>die Rednerin</#if> - ${speaker.firstName} ${speaker.name}}
    \addcontentsline{toc}{section}{Informationen über <#if speaker.geschlecht == "männlich">den Redner<#else>die Rednerin</#if> - ${speaker.firstName} ${speaker.name}}

    <#include "speakerInfoSpeech.ftl">
</#if>

\section*{Transkript der Rede}
\addcontentsline{toc}{section}{Transkript der Rede}

\begin{dialogue}
<#list speech.textContent as t>
    <#assign type = t.type>
    <#assign content = t.text>
    <#if type == "comment">
        \speak{Kommentar} {\textit{${content}}}
    <#elseif type == "text">
        \speak{
        <#if speaker?has_content && speaker.name?has_content>
            ${speaker.name
            ?replace("Ä", "AE")
            ?replace("Ö", "OE")
            ?replace("Ü", "UE")
            ?replace("ä", "ae")
            ?replace("ö", "oe")
            ?replace("ü", "ue")
            ?replace("ß", "ss")}
        <#else>Gastredner</#if>}{${content}}
    </#if>
</#list>
\end{dialogue}


<#assign nlpStats = linguisticService.getLinguisticFeatures(speech._id)!"">
<#if nlpStats?? && nlpStats?has_content>
    <#if disableTikz>
        <#include "nlpStatsNoTikZSpeech.ftl">
    <#else>
        <#include "nlpStatsTikZSpeech.ftl">
    </#if>
</#if>
\end{document}
