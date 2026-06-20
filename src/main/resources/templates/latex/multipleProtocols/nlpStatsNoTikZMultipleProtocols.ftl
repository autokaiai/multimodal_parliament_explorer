<#assign nlp = nlpStats>

<#function getTopItems items maxItems>
    <#assign itemList = []>
    <#list items as key, value>
        <#assign itemList = itemList + [{"key": key, "value": value}]>
    </#list>
    <#assign sortedItems = itemList?sort_by("value")?reverse>
    <#if ((sortedItems?size) > maxItems)>
        <#return sortedItems[0..(maxItems-1)]>
    <#else>
        <#return sortedItems>
    </#if>
</#function>

\boldsection{NLP-Analyse der Rede}

\boldsection{Sentimentanalyse}
<#assign minSentiment = nlp.sentiments?first.sentiment>
<#assign maxSentiment = nlp.sentiments?first.sentiment>
<#list nlp.sentiments as sentimentWert>
    <#if (minSentiment > sentimentWert.sentiment)>
        <#assign minSentiment = sentimentWert.sentiment>
    </#if>
    <#if (maxSentiment < sentimentWert.sentiment)>
        <#assign maxSentiment = sentimentWert.sentiment>
    </#if>
</#list>
Das Gesamtsentiment der Rede ist ${nlp.overallSentiment} und es liegt zwischen ${minSentiment} und ${maxSentiment}.

<#assign topTopics = getTopItems(nlp.topicCounts, 3)>
\boldsection{Top ${topTopics?size} Topics}

Die Top ${topTopics?size} Topics sind:
\begin{itemize}
<#list topTopics as topic>
    \item ${topic.key} (${topic.value}-Mal)
</#list>
\end{itemize}


<#assign topEntities = getTopItems(nlp.namedEntityCounts, 5)>
\boldsection{Top ${topEntities?size} Named Entities}

Die Top ${topEntities?size} Named Entities sind:
\begin{itemize}
<#list topEntities as entity>
    \item ${entity.key} (${entity.value}-Mal)
</#list>
\end{itemize}


<#assign topPos = getTopItems(nlp.posCounts, 10)>
\boldsection{Top ${topPos?size} POS-Typen}

\begin{itemize}
<#list topPos as pos>
    \item{${pos.key?replace("$", "\\$")} (${pos.value}-Mal)}
</#list>
\end{itemize}
