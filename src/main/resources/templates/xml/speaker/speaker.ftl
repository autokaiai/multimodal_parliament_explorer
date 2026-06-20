<?xml version="1.0" encoding="UTF-8" ?>
<export type="speaker">
    <speaker>
        <info>
            <#if speaker._id??>
                <id>${speaker._id}</id>
            </#if>
            <#if speaker.firstName??>
                <firstName>${speaker.firstName}</firstName>
            </#if>
            <#if speaker.name??>
                <lastName>${speaker.name}</lastName>
            </#if>
            <#if speaker.title??><title>${speaker.title}</title></#if>
            <#if speaker.geburtsdatum??>
                <birthdate>${speaker.geburtsdatum}</birthdate>
            </#if>
            <#if speaker.geburtsort??>
                <birthplace>${speaker.geburtsort}</birthplace>
            </#if>
            <#if speaker.sterbedatum??>
                <deathdate>${speaker.sterbedatum}</deathdate>
            </#if>
            <#if speaker.geschlecht??>
                <gender>${speaker.geschlecht}</gender>
            </#if>
            <#if speaker.beruf??>
                <profession>${speaker.beruf}</profession>
            </#if>
            <#if speaker.akademischertitel??>
                <academicTitle>${speaker.akademischertitel}</academicTitle>
            </#if>
            <#if speaker.familienstand??>
                <familyStatus>${speaker.familienstand}</familyStatus>
            </#if>
            <#if speaker.religion??>
                <religion>${speaker.religion}</religion>
            </#if>
            <#if speaker.vita??>
                <vita>${speaker.vita}</vita>
            </#if>
            <#if speaker.party??>
                <party>${speaker.party}</party>
            </#if>
            <#if speaker.imageUrl??>
                <imageUrl>${speaker.imageUrl}</imageUrl>
            </#if>
            <#if speaker.memberships??>
                <memberships>
                    <#list speaker.memberships as membership>
                        <membership>
                            <#if membership.member??>
                                <memberId>${membership.member}</memberId>
                            </#if>
                            <#if membership.begin??>
                                <begin>${membership.begin?date}</begin>
                            </#if>
                            <#if membership.end??>
                                <end>${membership.end?date}</end>
                            </#if>
                            <#if membership.label??>
                                <label>${membership.label}</label>
                            </#if>
                        </membership>
                    </#list>
                </memberships>
            </#if>
        </info>

        <#if speeches??>
            <speeches>
                <#list speeches as speech>
                    <speech order="${(speech?index + 1)}">
                        <#if speech.textContent??>
                            <content>
                                <#list speech.textContent as t>
                                    <#if t.type?? && t.text??>
                                        <#assign type = t.type>
                                        <#assign content = t.text>
                                        <#if type == "comment">
                                            <comment>${content}</comment>
                                        <#elseif type == "text">
                                            <text>${content}</text>
                                        </#if>
                                    </#if>
                                </#list>
                            </content>
                        </#if>
                        <#assign nlp = linguisticService.getLinguisticFeatures(speech._id)!"">
                        <#if nlp?? && nlp?has_content>
                            <nlpStats>
                                <#if nlp.sentiments??>
                                    <sentiment>
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
                                        <min>${minSentiment}</min>
                                        <max>${maxSentiment}</max>
                                        <overall>${nlp.overallSentiment}</overall>
                                    </sentiment>
                                </#if>
                                <#if nlp.topicCounts??>
                                    <topics>
                                        <#list nlp.topicCounts as key, value>
                                            <topic name="${key}" amount="${value}"/>
                                        </#list>
                                    </topics>
                                </#if>
                                <#if nlp.entityCounts??>
                                    <namedEntities>
                                        <#list nlp.entityCounts as key, value>
                                            <namedEntity name="${key}" amount="${value}"/>
                                        </#list>
                                    </namedEntities>
                                </#if>
                                <#if nlp.posCounts??>
                                    <posTag>
                                        <#list nlp.posCounts as key, value>
                                            <posTag name="${key}" amount="${value}"/>
                                        </#list>
                                    </posTag>
                                </#if>
                            </nlpStats>
                        </#if>
                    </speech>
                </#list>
            </speeches>
        </#if>
    </speaker>
</export>