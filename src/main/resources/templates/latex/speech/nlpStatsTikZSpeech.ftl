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

\section*{NLP-Analyse der Rede}
\addcontentsline{toc}{section}{NLP-Analyse der Rede}

\subsection*{Sentimentanalyse}
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

\begin{center}
\begin{tikzpicture}
\def\radius{2cm}

% basic arc-form zeichnen
% von links (minus radius) bis rechts (plus radius) und mit cycle verbinden
\draw[fill=gray!20] (-\radius,0) arc (180:0:\radius) -- cycle;

% winkel berechnen für sentiments
% und komma in punkt umwandeln damit es tikz versteht
\def\angle{${(180 - ((nlp.overallSentiment + 1) / 2) * 180)?replace(",", ".")}}
\def\minAngle{${(180 - ((minSentiment + 1) / 2) * 180)?replace(",", ".")}}
\def\maxAngle{${(180 - ((maxSentiment + 1) / 2) * 180)?replace(",", ".")}}

% minimum bis maximum einfärben als arc
\draw[line width=5pt, blue!30] (\minAngle:\radius*0.8) arc (\minAngle:\maxAngle:\radius*0.8);

% overall sentiment nadel
\draw[line width=1pt, blue] (0,0) -- (\angle:\radius);

% markierungen und labels
\draw[line width=1pt] (-\radius,0) -- (-\radius,0.2); % links strich
\draw[line width=1pt] (0,\radius) -- (0,\radius+0.2); % mitte strich
\draw[line width=1pt] (\radius,0) -- (\radius,0.2); % rechts strich

\node at (-\radius,-0.3) {Negativ};
\node[yshift=0.5cm] at (0,\radius+0.5) {Neutral};
\node at (\radius,-0.3) {Positiv};

\node[blue!50, font=\small] at (\minAngle:\radius*1.1) {Min};
\node[blue!50, font=\small] at (\maxAngle:\radius*1.1) {Max};
\node[blue, font=\small] at (\angle:\radius*1.1) {Overall};
\end{tikzpicture}
\end{center}

<#assign topTopics = getTopItems(nlp.topicCounts, 3)>
\subsection*{Topics}
Die Topics der Rede sind:

\begin{center}
\begin{tikzpicture}

\pie[sum=auto]{
<#-- top topics-->
<#list topTopics as topic>
    ${topic.value}/${topic.key}<#sep>,</#sep>
</#list>
<#-- kleinere topics kombinieren zu sonstiges-->
<#assign sonstigesCount = 0>
<#list nlp.topicCounts as key, value>
    <#if (topTopics?seq_contains(key) == false)>
        <#assign sonstigesCount = sonstigesCount + value>
    </#if>
</#list>
<#if (sonstigesCount > 0)>
    ,${sonstigesCount}/Sonstiges
</#if>
}
\end{tikzpicture}
\end{center}

<#assign topEntities = getTopItems(nlp.namedEntityCounts, 5)>
\subsection*{Top ${topEntities?size} Named Entities}
Die Top ${topEntities?size} Named Entities sind:

\begin{center}
\begin{tikzpicture}
\begin{axis}[
xbar,
width=12cm,
height=6cm,
xlabel={Häufigkeit},
symbolic y coords={<#list topEntities as entity>${entity.key?replace("_", "\\_")}<#sep>,</#sep></#list>},
ytick=data,
nodes near coords,
enlarge y limits=0.1,
bar width=0.5cm,
]
\addplot coordinates {<#list topEntities as entity>(${entity.value},${entity.key?replace("_", "\\_")})</#list>};
\end{axis}
\end{tikzpicture}
\end{center}

<#assign topPos = getTopItems(nlp.posCounts, 10)>
\subsection*{Top ${topPos?size} POS-Typen}

\begin{center}
\begin{tikzpicture}
\begin{axis}[
xbar,
width=12cm,
height=8cm,
xlabel={Häufigkeit},
symbolic y coords={<#list topPos as pos>${pos.key?replace(",", "\\,")?replace("$", "\\$")?replace("_", "\\_")}<#sep>,</#sep></#list>},
ytick=data,
nodes near coords,
enlarge y limits=0.1,
bar width=0.5cm,
]
\addplot coordinates {<#list topPos as pos>(${pos.value},${pos.key?replace(",", "\\,")?replace("$", "\\$")?replace("_", "\\_")})</#list>};
\end{axis}
\end{tikzpicture}
\end{center}
