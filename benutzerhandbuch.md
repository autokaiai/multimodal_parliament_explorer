# Benutzerhandbuch

## Benutzung mit Docker

Wir empfehlen die Benutzung mit Docker, da damit automatisch nur die nötigsten Abhängigkeiten installiert werden.

Außerdem kann das Programm so vom Rest des Systems getrennt werden und überschreibt keine anderen Installationen von
LaTeX etc.

Die Installation kann sich je nach Betriebssystem unterscheiden, deshalb hier die Schritte für Ubuntu Linux.

### 1. Docker herunterladen und Installieren

Falls Docker noch nicht installiert ist,
ist [hier eine Anleitung zur Installation](https://docs.docker.com/engine/install/ubuntu/) unter Ubuntu Linux.

### 2. Git installieren

Git sollte normalerweise schon vorinstalliert sein, aber falls nicht, kann man es mit folgendem Befehl installieren:

```bash
sudo apt update && sudo apt install git
```

### 3. Repository klonen

```bash
git clone https://github.com/kuuhhl/multimodal_parliament_explorer
```

### 4. Hinein-Navigieren

```bash
cd multimodal_parliament_explorer
```

### 5. Docker-Image erstellen

Das Erstellen des Docker-Image kann je nach Internetverbindung etwas dauern, da eine LaTeX-Distribution heruntergeladen
werden muss.

```bash
docker build -t multimodal_parliament_explorer .
```

### 6. Docker-Container starten

Um den Container jetzt zu starten und das Frontend auf Port 7001 zu öffnen, führe folgenden Befehl aus:

```bash
docker run --rm -d -p 7001:7001 multimodal_parliament_explorer
```

Wenn der Port bereits belegt ist, kann man einen anderen Port wählen, in dem man einfach die Portnummer vor dem `:`
ändert.

### 7. Im Browser öffnen

Nach einer kurzen Wartezeit (zum Kompilieren des Projektes) sollte jetzt
auf [http://localhost:7001](http://localhost:7001) das Frontend erreichbar sein.

> **Bitte Beachten:** Um das Projekt zu nutzen, muss der Rechner mit dem Docker-Container im VPN-Netzwerk der
> Universität
> sein. Ansonsten kann das Backend nicht auf die Datenbank zugreifen.

## Benutzung ohne Docker

Falls Docker nicht installiert werden kann, ist hier eine Anleitung zur Nutzung ohne Docker.

### 1. Voraussetzungen installieren

```bash
sudo apt update && sudo apt install bash git texlive-full openjdk-17-jdk maven
```

### 2. Repository klonen

```bash
git clone https://github.com/kuuhhl/multimodal_parliament_explorer
```

### 3. Hinein-Navigieren

```bash
cd multimodal_parliament_explorer
```

### 4. Konfigurationsdatei in LaTeX bearbeiten

Mit

```bash
kpsewhich texmf.cnf
```

finden wir den Pfad der Konfigurationsdatei `texmf.cnf`.

Damit wir große Dateien kompilieren können, müssen wir die Datei bearbeiten.

Angenommen, der Pfad, der ausgegeben wird ist `/usr/local/texlive/2024/texmf.cnf`.
Dann fügen wir mit folgendem Befehl die Zeile `buf_size=1000000` in die Datei ein:

```bash
echo "buf_size=1000000" >> /usr/local/texlive/2024/texmf.cnf
```

Je nachdem was `kpsewhich texmf.cnf` ausgibt, muss der Pfad angepasst werden.

### 5. Projekt kompilieren

Jetzt können wir das Projekt kompilieren:

```bash
mvn clean compile
```

### 6. Webserver starten

```bash
mvn exec:java -Dexec.mainClass="Main"
```

### 7. Im Browser öffnen

Jetzt sollte das Frontend auf [http://localhost:7001](http://localhost:7001) erreichbar sein.

> **Bitte Beachten:** Um das Projekt zu nutzen, muss der Rechner mit dem Docker-Container im VPN-Netzwerk der
> Universität
> sein. Ansonsten kann das Backend nicht auf die Datenbank zugreifen.

## Ändern der Datenbank

Standardmäßig wird die uns zugewiesene MongoDB-Instanz verwendet, die im Uni-Netzwerk gehostet wird.
In dieser sind die Daten bereits vorhanden und müssen nicht neu hochgeladen werden.

Zum Ändern der Instanz geht man folgendermaßen vor:

### 1. Datenbank-Zugangsdaten überschreiben

In `src/main/resources/config.properties` liegen die Datenbank-Zugangsdaten, die Giuseppe Abrami uns in das
Repository geschrieben hat.
Diese müssen durch die eigenen Zugangsdaten ersetzt werden.

### 2. Vorbearbeitete NLP-Daten herunterladen

Da es ohne einen leistungsfähigen Computer sehr lange dauert, alle Reden zu analysieren, hat uns Abrami diese Analysen
teilweise bereits zur Verfügung gestellt.
Sie sind mehrere Gigabyte groß, weshalb wir sie nicht in dieses Repository hinein kopiert haben; deshalb müssen sie
zunächst von
GitLab heruntergeladen werden:

```bash
git clone --depth 1 https://ppr.gitlab.texttechnologylab.org/abrami/materialienabschlussprojekt
```

Die `.xmi.gz`-Dateien müssen in den Ordner `src/main/resources/import/xmiAnnotations` kopiert werden.

### 3. Docker-Image neu erstellen (nur bei Nutzung von Docker)

Damit das Backend die neuen Zugangsdaten verwendet, muss das Docker-Image neu erstellt werden, wenn wir Docker nutzen.

```bash
docker build -t multimodal_parliament_explorer .
```

### 4. Projekt kompilieren (nur bei Nutzung ohne Docker)

```bash
mvn clean compile
```

### 5. Datenbank initialisieren

Da die neue Datenbank noch leer ist, muss sie noch initialisiert werden.
Dazu scrapen wir die Seite des Bundestags und diverse XML-Protokolle und laden die Daten strukturiert in die Datenbank.
Wir rufen dazu das `entrypoint.sh`-Skript mit dem `--import`-Argument auf:

```bash
chmod +x ./entrypoint.sh && ./entrypoint.sh --import
```

Oder wenn wir Docker nutzen:

```bash
docker run --rm multimodal_parliament_explorer --import
```

Der Import kann sehr lange dauern, da wir sehr viele Daten herunterladen und verarbeiten müssen.

Wenn er aber fertig ist, können wir aber wie schon beschrieben die Applikation starten und die neue Datenbank nutzen.
