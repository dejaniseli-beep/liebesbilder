# Liebesbilder ❤️

Android-App: 20 Fotos sind fest eingebaut, jedes mit eigenem Liebestext. Zusätzlich: eigene Bilder wählen oder an die App teilen, Liebestexte
werden automatisch aufs Bild gesetzt – speichern oder direkt an die Liebste senden.

## Auf GitHub hochladen & APK bekommen
1. Auf github.com ein neues Repository anlegen (z. B. `liebesbilder`).
2. Den kompletten Inhalt dieses Ordners hochladen (inkl. versteckter Ordner `.github`).
3. Reiter **Actions** öffnen – der Build „APK bauen“ startet automatisch (ca. 3–5 Min.).
4. Danach unter **Releases** die Datei `Liebesbilder.apk` herunterladen.
5. Auf dem Handy die APK öffnen und „Installation aus unbekannten Quellen“ erlauben.

## Texte ändern
Datei `app/src/main/java/com/liebe/app/Messages.kt` bearbeiten. `{n}` wird durch den Namen ersetzt.
