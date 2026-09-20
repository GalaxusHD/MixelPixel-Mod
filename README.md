# MixelPixel Mod

Clientseitige Fabric-Mod für Minecraft 1.21.8. Sie stellt ein angepasstes MixelPixel-Menü, passende Bildschirmhintergründe und eine lokale Spieler-Zielmarkierung bereit.

## Spieler-Menü

P-Menü und die Zielmarkierung (Glow/Overlay) sind nur auf `mixelpixel.net` und `play.mixelpixel.net` aktiv. Die Prüfung verwendet die eingegebene Verbindungsadresse, ignoriert Groß-/Kleinschreibung und akzeptiert einen gültigen optionalen Port. Einzelspieler, LAN und andere Server sind ausgeschlossen. Beim Verlassen eines erlaubten Servers wird das Ziel verworfen; Menüaktionen prüfen die Freigabe zusätzlich vor dem Senden.

Eine einmal anvisierte Spielerauswahl bleibt beim Wegsehen, bei größerer Entfernung und beim Öffnen von Menüs bis zum Ablauf des Auswahl-Zeitlimits erhalten. Standard: 30 Sekunden. Der Regler in den Einstellungen reicht von 3 bis 120 Sekunden. Dauerhaftes Ansehen verlängert die Frist nicht; nach Ablauf zum erneuten Auswählen kurz wegsehen und erneut anvisieren. Ein neu anvisierter gültiger Spieler ersetzt sie. Beim Verlassen des Servers oder bei deaktiviertem System wird sie gelöscht. NPC-Spielerprofile werden anhand der vom Server als gelistet gemeldeten Spieler mit übereinstimmender UUID und Kontonamen gefiltert. Vollständig als echte Spieler gemeldete NPCs sind ohne zusätzliches Serversignal nicht zuverlässig unterscheidbar.

Mit allen fünf Aktionen aktiv öffnet `P` einen Kreis mit fünf gleich großen Segmenten:

- oben: MSG – Chat mit `/msg <Name> ` öffnen
- links oben: Kick – `/p kick <Name>` sofort senden
- rechts oben: Ban – `/p ban <Name>` sofort senden
- links unten: Report – `/report <Name>` sofort senden

- rechts unten: NameMC – `/namemc <Name>` lokal ausführen

Die Priorität ist MSG, Kick, Ban, Report, NameMC. Eine aktive Aktion steht oben (V), zwei stehen oben/unten (waagerechte Trennung), drei oben/links unten/rechts unten (Y), vier oben/links/rechts/unten (X), fünf oben/links oben/rechts oben/links unten/rechts unten. Ohne aktive Aktion öffnet sich kein P-Menü.
Der Zielname wird beim Öffnen des Menüs festgehalten. Die Aktionen verwenden transparente PNG-Symbole. `tools/generate-radial-icons.ps1` erzeugt das Kick-Motiv aus Braille-Punkten und MSG/Ban aus geometrischen Formen; manuelle Textabstände beeinflussen diese beiden Symbole nicht mehr.

Die Texturen nutzen ihre Fläche stärker aus, sodass die sichtbaren Motive ungefähr 50 % größer erscheinen. Bei kleinen Fenstern skalieren sie mit dem Ring. Der Spielername bleibt im P-Menü weiß. Die bisherige Rangfarberkennung wurde entfernt, weil die ausgewerteten Teamdaten keine verlässliche Unterscheidung von Rängen und Glow-Einstellungen ermöglichen.

## Build

```powershell
./gradlew.bat build
```

Die fertige Datei liegt anschließend unter `build/libs/mixelpixel-mod-1.0.0.jar`.

Der Ladebildschirm ersetzt beide Vanilla-Logohälften in jedem Renderdurchlauf, auch während des abschließenden Ausblendens ohne Fortschrittsbalken. Das eigene Ladebild übernimmt den Transparenzwert der Ladeanimation und blendet vor dem Entfernen des Overlays weich aus. Die normale Lade-, Fehler- und Abschlussverarbeitung sowie der Fortschrittsbalken bleiben erhalten.

Whitelist-Regressionstest (JDK 21):

```powershell
javac -d build/whitelist-tests tools/ServerWhitelistTest.java src/client/java/net/mixelpixel/mod/client/target/ServerWhitelist.java
java -cp build/whitelist-tests ServerWhitelistTest
```

## Konfiguration

Beim ersten Start wird `config/mixelpixel-mod.json` erzeugt. Die Glow-Farbe unterstützt Minecraft-Farbnamen (`white`, `gold`, `dark_purple` usw.) und Hexwerte (`#RRGGBB`).

## Mod-Menu-Einstellungen

Die Einstellungsoberfläche ist über die optionale Mod „Mod Menu“ sowie im Spiel über `#` (deutsche Tastatur) zugänglich. Die Taste lässt sich unter Optionen → Steuerung → Tastenbelegung → MixelPixel Mod → Mod-Einstellungen öffnen ändern. Für den Tastenzugriff ist Mod Menu nicht erforderlich. Sechs Schalter: Spielermarker An/Unsichtbar/Aus sowie MSG, /P KICK, /P BAN , REPORT und NameMC jeweils Ja/Nein. An/Ja sind hellgrün (§a), Aus/Nein hellrot (§c), Unsichtbar gelb (§e). Unsichtbar blendet nur die eigene Markierung aus, Aus deaktiviert Zielauswahl und P-Menü. Einstellungen werden in der JSON-Konfiguration gespeichert. Zurück mit Escape.

Zum Kompilieren dient `libs/modmenu-15.0.0.jar` aus dem offiziellen Terraformers-Maven. Diese Bibliothek wird nicht in die fertige Mod eingebettet. Zur Nutzung der Einstellungen Mod Menu separat installieren.

## Dauerhaftes Serverressourcenpaket

Das mitgelieferte MixelPixel-Paket wird beim Spielstart automatisch geladen, auch außerhalb von MixelPixel. Der aktualisierte Stand liegt unter `config/mixelpixel-server-pack/`. Beim Paketangebot auf jedem MixelPixel-Join wird die Serverprüfsumme mit dem geladenen Stand verglichen. Gleiche Inhalte verursachen kein erneutes Laden; ohne verwertbare Prüfsumme wird heruntergeladen und der tatsächliche Inhalt verglichen. Neue Pakete werden geprüft, geladen und danach als neuer Startstand gespeichert. Ein tatsächliches Update benötigt weiterhin einen Ressourcen-Reload. Zusätzliche Event-Pakete bleiben bei Minecrafts normaler Paketverwaltung. Downloadfehler behalten den bisherigen Stand bei und melden dem Server den Fehler. Alte Cachedateien bleiben als Rückfallstand erhalten.

## Prüfung

`tools/RadialLayoutTest.java` prüft alle 32 Aktionskombinationen. Kompilierung zusammen mit `RadialLayout.java`, danach `java RadialLayoutTest` mit passendem Klassenpfad. Ein Live-Test auf MixelPixel ist zusätzlich erforderlich, insbesondere für NPC-Meldungen und Serverpaket-Updates.


## Auswahl-Zeitlimit

Der Zeitregler speichert ganze Sekunden (3–120, Standard 30). Er verwendet eine monotone Uhr; ein offenes P-Menü schließt nach Ablauf, und abgelaufene Aktionen werden nicht gesendet. Ein neuer anvisierter Spieler startet seine eigene Frist.



`tools/SelectionRankTest.java`: 21 Prüfungen für Fristen, neue Auswahl, Zurücksetzen und Reglergrenzen.


## Integrierte NameMC-Funktion

Übernommen aus der von GalaxusHD bereitgestellten NameMC Mod 1.0.0 (Metadaten: MIT). Die separate `namemcmod-1.0.0.jar` ist nicht mehr erforderlich und sollte aus dem Mods-Ordner genommen werden, damit nur eine Mod den Befehl registriert.

`/namemc <Spielername>` bietet Namensvorschläge und zeigt einen anklickbaren NameMC-Profillink. Die P-Menü-Aktion führt genau diesen lokalen Befehl für das ausgewählte Ziel aus; er wird nicht an den Minecraft-Server gesendet. Der manuell eingegebene Befehl bleibt wie in der ursprünglichen Mod auch außerhalb von MixelPixel verfügbar. Die P-Menü-Aktion unterliegt weiterhin der Server-Whitelist, Zielprüfung und ihrem Ja/Nein-Schalter.

Wie in der ursprünglichen Mod wird der angefragte Spielername zur Namenshistorien-Abfrage an `https://liforra.de/api/namehistory` übermittelt. Die Abfrage erfolgt erst beim Ausführen des Befehls, nicht bei der Zielauswahl. Fehler und Zeitüberschreitungen werden gemeldet; der direkt angezeigte Profillink bleibt verfügbar. Statt der ursprünglichen regulären Expression wird JSON geparst, doppelte Namen werden entfernt und der aktuelle Name wird ausgefiltert. Die Verfügbarkeit und Vollständigkeit dieses externen Dienstes wurde nicht live geprüft.

Build erfolgreich; 11.744 Layout-Prüfungen für alle 32 Schalterkombinationen und 21 Auswahlprüfungen bestanden. In Minecraft und auf MixelPixel muss die neue Version noch praktisch geprüft werden.
## Unsichtbare Spieler

Spieler mit gesetzter Unsichtbarkeit oder bekanntem Unsichtbarkeitseffekt werden von Zielauswahl und Mod-Markierung ausgeschlossen, auch bei Sichtbarkeit durch Teamregeln. Wird das aktuelle Ziel unsichtbar, werden Auswahl und Zeitlimit gelöscht und das P-Menü geschlossen. Die Aktionsprüfung verhindert Befehle auf dieses Ziel bereits vor dem nächsten Tick. Die normale Minecraft-Darstellung wird nicht verändert.


Die NameMC-Grafik wird 20 % kleiner als zuvor gezeichnet. Das Report-Symbol wird anhand des Schwerpunkts seiner sichtbaren Pixel optisch zentriert. Die Anpassungen gelten für jede Anzahl aktiver Menüaktionen.

