#!/bin/bash

if [[ "$1" == "--import" ]]; then
    echo "Argument --import: Starte Import..."
    mvn -B exec:java -Dexec.mainClass=backend.importer.ImportSpeaker
    mvn -B exec:java -Dexec.mainClass=backend.importer.ImportHandler
    mvn -B exec:java -Dexec.mainClass=backend.importer.ImportPictures
    mvn -B exec:java -Dexec.mainClass=backend.importer.MergeSpeakerInfo
    mvn -B exec:java -Dexec.mainClass=backend.importer.ImportVideos
    mvn -B exec:java -Dexec.mainClass=backend.importer.ImporterPlenarprotocol
    mvn -B exec:java -Dexec.mainClass=backend.nlp.util.ResourceExtractor
    mvn -B exec:java -Dexec.mainClass=backend.nlp.XmiAnnotationImporter
elif [[ "$1" == "--test" ]]; then
    echo "Argument --test: Starte Tests..."
    mvn -B test
else
    echo "Starte Anwendung..."
    mvn -B exec:java -Dexec.mainClass=Main
fi