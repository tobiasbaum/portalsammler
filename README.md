portalsammler
=============

Programm zum Abgreifen und sicheren lokalen Ablegen von Dokumenten (Kontoauszüge, Rechnungen) 
aus div. Portalen (Online-Banking, Online-Rechnung, ...).


Motivation
----------

Dokumente wie Rechnungen, Kontoauszüge und Depotabrechnungen werden zunehmend Online bereitgestellt.
Leider setzen viele Anbieter dabei auf proprietäre Portale, sodass ein Kunde sich auf vielen Webseiten
einloggen muss, um alle Dokumente zu sehen und kein Gesamtüberblick existiert. Außerdem unterliegen
die Dokumenten auf den Portalen häufig Haltefristen, nach denen sie automatisch gelöscht werden. Da nicht
zu erwarten ist, dass die proprietären Portale in näherer Zukunft an Bedeutung verlieren werden, bietet
"Portalsammler" eine Möglichkeit, aus dieser Situation das Beste zu machen.

Features
--------

- zentrale Dokumentenverwaltung
 - nur ein Anlaufpunkt für alle Dokumente
 - alle Dokumente werden lokal auf deinem Rechner abgelegt, d.h. du hast die volle Kontrolle
 - lokales Suchen und Filtern von Dokumenten
- Sicherheit
 - alle Daten sind verschlüsselt (AES-256)
 - die Daten liegen niemals entschlüsselt auf Platte vor, außer dies ist explizit gewünscht
- Anbindung diverser Portale
 - die Portale werden durch Fernsteuerung der Webseiten angebunden, d.h. jedes Portal kann potentiell angebunden werden
 - wenn eine Portal-Anbindung fehlt: Das Hinzufügen einer neuen Anbindung ist relativ einfach
- einfache Benutzung
 - es wird automatisch ein sicherer Schlüssel generiert
 - der Schlüssel kann als QR-Code ausgedruckt werden, damit er nicht gemerkt werden muss
 - alle Daten liegen verschlüsselt in einem einfachen Verzeichnis vor und können deshalb z.B. problemlos in der Cloud gesichert werden
