# Randomizer

Implementacja korzysta z:
- Inventory Framework 3.7.1 — GUI wyboru trybu,
- Okaeri Configs 5.0.13 — `plugins/MiniGames/randomizer.yml`,
- LiteCommands 3.11.0 — `/randomizer`, `/randomizer dolacz <1v1|2v2|4v4>`, `/randomizer opusc`.

## Konfiguracja
Po pierwszym uruchomieniu ustaw `arenaCenter`, `lobby` oraz odpowiednio 2/4/8 lokalizacji spawnów. Border zaczyna maleć po 180 sekundach, a każdy żywy gracz otrzymuje osobno losowany przedmiot co 60 sekund.

Przedmioty Nexo wpisuje się do `nexoItems`. Integracja jest miękka — bez Nexo plugin nadal działa.

## Budowanie
`./gradlew clean shadowJar`
Gotowy JAR: `build/libs/MiniGames-1.0-SNAPSHOT.jar`.
