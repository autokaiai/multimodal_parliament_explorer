<#if speaker.imageData?has_content>
    \begin{figure}[h!]
    \centering
    \includegraphicsembedded[width=0.8\textwidth]{${speaker.imageData}}
    \caption{Portrait von ${speaker.firstName} ${speaker.name}}
    \end{figure}
</#if>
<#if speaker.title?has_content>${speaker.title} </#if>${speaker.firstName} ${speaker.name}
<#if speaker.geburtsdatum?has_content>
    wurde am ${speaker.geburtsdatum?date}
    <#if speaker.geburtsort?has_content>
        in ${speaker.geburtsort}
    </#if>
    geboren.
</#if>
<#if speaker.sterbedatum?has_content>
    <#if speaker.geschlecht == 'männlich'>Er<#else>Sie</#if> lebte bis zum ${speaker.sterbedatum?date}.
</#if>
<#if speaker.geschlecht == "männlich">Er<#else>Sie</#if>
<#if speaker.beruf?has_content>
    ist ${speaker.beruf} von Beruf und
</#if>
<#if speaker.party?has_content>
    gehört der Partei ${speaker.party} an.
<#else>
    ist parteilos.
</#if>
<#if speaker.familienstand?has_content && speaker.familienstand != "keine Angaben" && speaker.religion?has_content>
    ${speaker.name} ist ${speaker.familienstand} und ${speaker.religion}.
<#elseif speaker.religion?has_content>
    ${speaker.name} ist ${speaker.religion}.
<#elseif speaker.familienstand?has_content && speaker.familienstand != "keine Angaben">
    ${speaker.name} ist ${speaker.familienstand}.
</#if>


<#if speaker.vita?has_content>
    \textbf{Vita von ${speaker.firstName} ${speaker.name}}
    ${speaker.vita}
</#if>