# Kommunicator — aplikacja multiplatformowa w Kotlin Multiplatform

## Główne założenia (wymagania?) funkcjonalne projektu

Założeniem projektu (nazwa kodowa "Kommunicator") jest stworzenie komunikatora w formie aplikacji
mobilnej na Androida i iOS przy wykorzystaniu technologii Kotlin Multiplatform. Aplikacja będzie
wykorzystywać REST API udostępniane przez cześć serwerową wykonaną we frameworku Ktor.
Dane wszystkich użytkowników z wiadomościami przechowywane będą w bazie danych PostgreSQL.
Sposobem na zabezpieczenie komunikacji między klientem a serwerem oprócz SSL będzie JWT.

Podstawowe funkcjonalności komunikatora:

- rejestracja i logowanie
- dodawanie kontaktów
- wysyłanie i odbieranie wiadomości tekstowych
- statusy (odczytana wiadomość, pisanie)
- historia wiadomości dostępna offline
- powiadomienia push

## Postawienie projektu (2023-06-06)

Potrzebne nam będą:

- Android Studio (w momencie tworzenia AS Flamingo)
- plugin do AS https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile
- macOS, bo bez niego nie zbudujemy apki na iOS

Tworzymy nowy pusty projekt aplikacji (do wyboru jest jeszcze biblioteka).

Nie zmieniamy nic oprócz nazwy apki i paczki (które ustawiamy kolejno na "Kommunicator"
i "io.github.mklkj.kommunicator).

Niestety nazwa aplikacji nie ma odzwierciedlenia na iOS i musimy wykonać dodatkowo to
https://stackoverflow.com/a/239006/6695449.

Dodatkowo z jakiegoś powodu na moim komputerze jest problem z rozpoznawaniem przez AS skryptów .kts.
Rozwiązaniem jest użycie innego JDK niż Embedded JDK (np. Corretto JDK).

## Aktualizacja bibliotek (2023-11-14)

Trochę mnie tu nie było... aktualizujemy zależności i upewniamy się, że wszystko działa. Oprócz
podbicia wersji kotlina i AGP podbiłem też wersję javy tak jako target, tak i source compatibility.

W międzyczasie zainstalowałem plugin https://touchlab.co/xcodekotlin.

W międzyczasie pojawiła się możliwość pisania UI aplikacji na iOS w Compose UI oraz generator
projektu (https://kmp.jetbrains.com/). Przerabiam ten projekt według tego, co wypluje ten generator.
Ten wizard wygenerował też moduł z backendem (<3) więc chętnie tego użyję.

## Zbudowanie mock-upu listy czatów, view modele i DI (2023-11-17)

Zacząłem tworzyć ekran z listą czatów (ChatsScreen). Na te potrzeby utworzyłem model `Chat`,
umieszczając go we współdzielonym między apką a serwerem module `shared`.

Aby od razu zadbać  o jego serializację, dodałem we współdzielonym module `kotlinx.serialization`.
Do zapisu czasu wysłania ostatniej wiadomości w czacie użyłem typu z biblioteki `kotlin.datetime`,
która też znalazła się we wspólnych zależnościach.

Wracając do tworzenia layoutu, chcąc wyświetlić obrazek (awatar) czatu, użyłem biblioteki `Kamel`.
Ta wymaga bezpośredniego zdefiniowania odpowiedniego silnika HTTP dla biblioteki `Ktor` dla każdego
z targetów, co zrobiłem, a co przyda się przy tworzeniu części sieciowej.

Następnie zacząłem prace nad architekturą — chciałem dodać view model, by tam trzymać stan listy
chatów, jak i pobierać je z API. Użyłem więc view modeli od mokko oraz koin do DI. 
Czy jest idealnie? Nie jest. Ale jako tako na razie działa.

Idąc za ciosem zacząłem kombinować z konfiguracją sieci. Przeniosłem mockowe dane z common do server
i zwracam je teraz z endpointu `/chats`. Apka przy użyciu Ktorfita z Ktorem pod spodem i odpowiednim
silnikiem na Androidzie i iOS pobiera listę czatów z adresu 0.0.0.0. Tak na Androidzie tak i na iOS
wymagane było połączenie https i musiałem to obejść, by móc developać apkę na localhoście.

## Kombinowanie z KSP do libki Koin, żeby nie pisać ręcznie kodu na żadnym targecie (2023-11-26)

Bardzo nie podobało mi się to, że poprzednio android używał ksp do generowania kodu głównego modułu
Koin, a w iOS musiałem ręcznie wpisywać, co jest factory, co jest single, etc
(Hilt przyzwyczaił do dobrego). Z tego powodu poszperałem i ustawiłem Koina dla wszystkich
interesujących nas targetów (czyli wszystkie iOSowe + Android). Brakującą implementację
rozszerzenia `org.koin.androidx.viewmodel.dsl.viewModel` dopisałem na pałę w iosMain sources, tak
by zamiast używać `koinViewModel` dostępnego tylko dla androida, użyć `rememberKoinInject()`
(jako w zasadzie jedynego mi znanego sposobu na to).

I to działa!

Z jednym minusem — Intellij ma problemy z wykryciem wygenerowanego kodu dla iosX64Main w iosMain
i przez `org.koin.ksp.generated.defaultModule` wyświetla się tak, jakby go brakowało, chociaż tak
naprawdę to jest i wszystko się normalnie kompiluje. Jako _obejście_ dodałem expect/actual
w rozbiciu na `iosMain` vs iosX64Main itd., by nie oglądać tego errora :)
To chyba ten issue: https://github.com/google/ksp/issues/963

Dodatkowo chwilowo jesteśmy uwiązani na wersji .13 ksp, ze względu na jakiś dziwny error przy
budowaniu iOSa (dodałem komentarz nad wersją).

### Część druga

Chciałem skonfigurować wcześniej kwestie wielu środowisk (o czym mam nadzieję za chwilę), ale
nie mogłem uruchomić XCode... Pobrałem wersję beta i lecimy dalej!

Patrząc na ten artykuł https://tooploox.com/kotlin-multiplatform-handling-different-environments
i używając tego pluginu https://github.com/yshrsmz/BuildKonfig ustawiłem dwa flavory: dev i prod,
gdzie pierwszy to lokalne środowisko, a prod to mój przyszły (nie skonfigurowany jeszcze) backend
na pich.ovh.
W artykule opisane są osobne konfiguracje .xcconfig dla dev i prod, ale nie wiem po co, kiedy
obie apki mogę przesetawiać przez gradle.properties.

Z paroma przeszkodami, ale udało mi się to zrobić tak, żeby mi w miarę odpowiadało. Największym
minusem jest potrzeba edycji śledzonego przez gita pliku gradle.properties, ale niech na razie tak
zostanie.

Ciekawostka: jeśli Xcode project configuration nie jest ustawione (bo akurat zmienialiśmy dostępne konfiguracje przez XCode)
to build się zawiesza na tej części z gradlem i nic się nie dzieje przez wiele minut.

## Materiały

- biblioteki KMM 1 - https://github.com/terrakok/kmm-awesome
- biblioteki KMM 2 - https://github.com/AAkira/Kotlin-Multiplatform-Libraries
- biblioteki KMP od IceRock - https://moko.icerock.dev/
- tutorial tworzenia konsolowej aplikacji czatu - https://ktor.io/docs/creating-web-socket-chat.html
- JWT do autoryzacji - https://ktor.io/docs/jwt.html
- moze jakaś integracja z ChatGPT? - https://github.com/yml-org/ychat
- przykładowa aplikacja KM w Compose UI i Swift UI - https://github.com/getspherelabs/cosmo-kmp


https://github.com/tunjid/Tiler
https://github.com/getspherelabs/cosmo-kmp
https://atscaleconference.com/messaging-at-scale/
https://www.donnfelker.com/why-kotlin-multiplatform-wont-succeed/
https://github.com/theapache64/rebugger
