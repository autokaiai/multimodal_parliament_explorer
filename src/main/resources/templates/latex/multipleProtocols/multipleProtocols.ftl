<#include "../imports/imports.ftl">
\title{\textbf{Deutscher Bundestag} \\ \large{Export <#if isAll>aller Plenarprotokolle<#else>einer Auswahl von ${protocols?size} Plenarprotokollen</#if>}}
\date{Exportiert am ${.now?string("dd.MM.yyyy")}}
\begin{document}
\maketitle
\newpage
\tableofcontents
\newpage
<#list protocols as protocol>
    <#if protocol.protocol?? && protocol.protocol.title?? && protocol.protocol.date??>
        <#assign sectionTitle = protocol.protocol.title + " - " + protocol.protocol.date?date("yyyy.MM.dd HH:mm:ss")?string("dd.MM.yyyy")>
    <#else>
        <#assign sectionTitle = "Protokoll mit ID " + protocol.id>
    </#if>
    \section*{${sectionTitle}}
    \addcontentsline{toc}{section}{${sectionTitle}}
    <#include "protocolCoreMultipleProtocols.ftl">
</#list>
\end{document}
