\subsubsection*{Transkript der Rede}

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

