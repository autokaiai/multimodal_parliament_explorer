<#include "../imports/imports.ftl">
\title{\textbf{Deutscher Bundestag} \\ \large{<#if protocol.protocol?? && protocol.protocol.title??>Export von ${protocol.protocol.title}<#else>Export von Protokoll mit ID ${protocol.id}</#if>}}
\date{Exportiert am ${.now?string("dd.MM.yyyy")}}
\begin{document}
\maketitle
\newpage
\tableofcontents
\newpage
<#include "protocolCoreSingleProtocol.ftl">
\end{document}
