# Kommunicator — aplikacja multiplatformowa w Kotlin Multiplatform

## Główne założenia (wymagania?) funkcjonalne projektu

Założeniem projektu (nazwa kodowa "Kommunicator") jest stworzenie komunikatora w formie aplikacji
mobilnej na Androida i iOS przy wykorzystaniu technologii Kotlin Multiplatform. Aplikacja będzie
wykorzystywać REST API udostępniane przez cześć serwerową wykonaną we frameworku Ktor.
Dane wszystkich użytkowników z wiadomościami przechowywane będą w bazie danych PostgreSQL.
Sposobem na zabezpieczenie komunikacji między klientem a serwerem oprócz SSL będzie JWT.

Podstawowe funkcjonalności komunikatora:

- [x] rejestracja i logowanie
- [ ] dodawanie kontaktów
- [x] wysyłanie i odbieranie wiadomości tekstowych
- [ ] statusy (odczytana wiadomość, pisanie)
- [ ] historia wiadomości dostępna offline
- [ ] powiadomienia push

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

Ciekawostka: jeśli Xcode project configuration nie jest ustawione (bo akurat zmienialiśmy dostępne
konfiguracje przez XCode) to build się zawiesza na tej części z gradlem i nic się nie dzieje przez
wiele minut.

## Deploy API (:server) na VPSa (2023-12-07)

Żeby móc się gdziekolwiek pochwalić tym, co tu robię, potrzebna jest możliwość połączenia się z API
nawet bez komputera pod ręką. Dlatego wykorzystałem te rzeczy, które już znam i opakowałem moduł
:server w kontener Dockera, który następnie jest używany przez Docker Compose (to na przyszłość,
żeby móc łatwo bazę danych razem deployować), a to z kolei jest deployowane na VPS w Oracle Cloud
i wystawione pod moją domeną kommunicator.pich.ovh.

Jakie były trudności? Ze względu na to, że moduł serwerowy, moduł współdzielony, jak i apka mobilna
są połączone w jeden wielki projekt, a VPS w Oracle jest na ARM (VM.Standard.A1.Flex, czyli
Arm processor from Ampere) i przez brak prebuildu kotlina na tę architekturę
(kotlin-native-prebuilt-linux-aarch64 https://youtrack.jetbrains.com/issue/KT-36871/Support-Aarch64-Linux-as-a-host-for-the-Kotlin-Native)
to musiałem hackować i wywalić na potrzeby zbudowania tego modułu, modułu composeApp oraz targetów
iOSowych z modułu shared.

## Aktualizacja zależności, przykładowy test, etc (2023-12-22)

Zaktualizowałem parę zależności i dodałem przykładowy test w commonTest. Żeby uniknąć jakichś
dziwnych errorów, że jednej wartości nie ma to dodałem dla BuildKonfigu domyślną wartość `baseUrl`.

## Ekran konwersacji — nawigacja między ekranami (2023-12-23)

https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html

Podjąłem decyzję — do nawigacji użyłem PreCompose. Bo ma swoją implementację view modeli
i integrację z Koinem. Nawet ma wbudowaną nawigację, która przypomina tę z Navigation Component
od Google'a. Ale chwilę później zrezygnowałem z tego wyboru. Chyba właśnie przez ten ostatni punkt.
Navigation Component dla Compose UI jest... siermiężny. Chciałem czegoś nowego.

Dlatego swój wzrok skierowałem ku bibliotece Voyager — posiadającej podobne funkcje (a nawet
więcej), ale zrealizowane trochę inaczej.

Dlaczego nie Decompose albo Appyx? Decompose wymaga pisania sporej ilości boilerplate'u, którego
wolałbym uniknąć. Do tego nie ma wbudowanej integracji czegoś, co by przypominało view modeli
ani integracji z Koinem. Appyx również.

## Ikonka apki, UUID, JWT, ekrany rejestracji i logowania (2023-12-25)

1. Zrobiłem (ukradłem) ikonkę https://uxwing.com/chat-icon/.
2. Zamiast nietypowanych stringów jako UUID machnąłem biblioteczkę od tego, która ma od razu
wsparcie dla SQLDelight i Jetbrains Exposed.
3. Na podstawie https://codersee.com/secure-rest-api-with-ktor-jwt-access-tokens/ zrobiłem
(skopiowałem) część serwerową rejestracji (na razie tylko in-memory), pobierania tokenu i szczegółów
usera
4. Machnąłem na szybko ekrany welcome, rejestracji i logowania. Obsługa błędów jest słaba, dużo
trzeba jeszcze zrobić w okolicach obsługi różnych kodów HTTP z API, ale da się zarejestrować
i potem zalogować takim kontem.

## Firebase i Crashlytics (2023-12-26)

Jako kolejny krok pomyślałem, że zrobię sobie logowanie ruchu sieciowego. A przy okazji chciałem też
dodać jakąś bibliotekę do zarządzania logami (jakiegoś Timbera). Wybór stanął na Kemricie od
Touchlab. Tam zauważyłem, że jest dostępna integracja z Crashlytics. Super. Przy okazji skonfiguruję
Firebase'a — pomyślałem. O jakże byłem głupi...

Żeby kompleksowo do tego podejść, chciałem od razu użyć biblioteki do obsługi całego Firebase'owego
API. Oczywistym wyborem jest Firebase od gitlive. Tylko że tam trzeba osobno zainstalować
i skonfigurować Firebase na poszczególnych platformach. Z Androidem poszło gładko — jedna zależność,
jeden pliczek konfiguracyjny i wsio. Na iOS to trochę bardziej zagmatwane.

W świecie iOS istnieją teraz w zasadzie dwa package managery — Cocoapods i Swift Package Manager.
Z tym pierwszym KMM ma nawet działającą integrację poprzez gradle'a! Tylko że jej podpięcie znacznie
wydłuża synchronizację projektu. Gdyby to działało od razu, to nawet bym się nie zastanawiał.
Niestety nie działało. Choć `iosMain` widział klasy z paczki Firebase, to apka się nie kompilowała
(linker nie mógł znaleźć frameworku). Kombinowałem z flagami `linkOnly`, ale wtedy, choć apka się
budowała, to crashowała się zaraz przy starcie.

Dlatego zacząłem kombinować z SPMem. Objawy były w zasadzie podobne, nic nie dzialało.
Następnego dnia (2023-12-27) pomyślałem, że spróbuję jeszcze wygenerować inny projekt i porównać
`.xcodeproj`. Moją uwagę zwrócił brak jednego bloku z kopiowaniem frameworków, biblioteką
Crashlyics (czyli tą brakującą!) w sekcji z `Frameworks, Libraries and Embedded Content` oraz
innymi flagami (`${inherited}` czy coś takiego) kompilatora. Przywróciłem flagi i przez Xcode
dodałem brakujący framework. Zadziałało!!!

## Logging (2023-12-27)

Podpiąłem Kermita pod view modele i ktora, żeby w debugu wszystko się ładnie na logcata/to coś w iOS
logowało, a na produkcji by wszystko szło do crashlytics.

Później zacząłem kombinować z lokalną bazą danych SQDelight, żeby zapisywać w niej zalogowanego
usera razem z JWT, ale nie skończyłem, bo jakiś dziwny problem z wylogowywaniem był. Miałem
wrażenie, że Flow nie emituje listy userów po wylogowaniu, ale następnego dnia okazało się, że...

https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-ktor-sqldelight.html

## Baza danych na serwerze (2023-12-28)

... okazało się, że baza danych działała dobrze, tylko ja to napisałem tak, że to nie miało prawa
działać. Myślałem, że jak zmienię startowy ekran on runtime w App.kt, to się to ładnie
przekomponuje, ale to tak nie działa. Dodałem `replaceAll` na WelcomeScreen przy wylogowywaniu
i wszystko gra.

Dalej — usprawniłem ekran z listą czatów oraz z ekranem konwersacji.

Dalej — połączenie z bazą PostgreSQL w części serwerowej. Używamy Jetbrains Exposed. Mały problem
ze zmiennymi środowiskowymi. Fajny plugin https://plugins.jetbrains.com/plugin/7861-envfile/versions/stable
ale nie dostępny niby dla Android Studio. Na szczęście można to ominąć, pobierając zip ręcznie.

Przewijając do godziny prawie 03:00 - połączenie działa. Udało mi się zarejestrować usera
i zalogować się na niego.
Do zrobienia na pewno będzie zrobienie jakiegoś upserta przy istniejącym
już uuid (problem dwóch generałów) oraz wyjątku przy już istniejącym username.
Będzie trzeba też ogarnąć hashowanie hasła przy tworzeniu kont i porównywanie takiego hasła później.
Celuję tutaj raczej w bcrypt ze Spring security (celuję w coś, co znam, a bcrypta znam z PHP).

Z "sukcesów" (a raczej rzeczy, które się jakoś ładnie złożyły) to użyłem integracji kotlinx.uuid
z SQLDelight i Jetbrains Exposed, dzięki czemu się to jakoś tam ładnie teraz prawie samo
serializuje.

Z ciekawostek to do migracji bazy na backendzie użyłem Flyway. Do zapisania płci użyłem enuma, ale
żeby go zapisać w bazie postgresowej, trzeba było ręcznie utworzyć tam wcześniej takiego enuma
(ja nawet nie wiedziałem, że postgres takie rzeczy ma). Oczywiście, można było to załatwić zwykłym
stringiem, ale to by nie było to samo :)

## Baza danych - deploy, usprawnienia (2023-12-29)

Do późnych godzin wieczornych (a właściwie do 4 w nocy lub nad ranem — co kto woli) próbowałem
ogarnąć bazę danych na VPSie przez docker compose. Teoretycznie wszystko powinno zadzialać od
strzała. Teoretycznie. Jednak ciągle wywalało błąd autoryzacji na usera, którego podałem w envach.
Zmieniłem je na domyślne, tj. `postgres` jako login i hasło. Wtedy znowu z brakiem bazy danych był
problem. Z tego, co kojarzyłem, to baza danych tworzy się tylko przy pierwszym uruchomieniu. Tylko
że wyrzucenie kontenera nie wystarczyło, trzeba było jeszcze wyczyścić volumen (co w sumie dobrze).

Po drodze testowałem też te konenery u siebie i trafiłem na dziwny błąd z nie wykrywaniem migracji
przez Flyway. Taka historia https://stackoverflow.com/a/77237118/6695449. Wystarczyło zrobić jak ten
gość i pykło.

Kontynuując kwestie z wczoraj związane z samym backendem — zrobiłem hashowanie hasła.
Użyłem bcrypta. Zastanawiałem się nad https://github.com/patrickfav/bcrypt, ale jako że to bardzo
delikatna sprawa, to ostatecznie użyłem implementacji ze spring security
(`org.springframework.security:spring-security-crypto`).

Dodałem lepszą (a w zasadzie jakąkolwiek) walidację przy tworzeniu konta i logowaniu. Teraz od razu
jest jasne, że dane konto już istnieje (a raczej, że username jest już użyty) albo że wpisało się
nieprawidłowe dane logowania.

Ciekawostka — żeby wysłać w Ktorze sam kod http to https://ktor.io/docs/responses.html#status.
Co do samego wyboru kodu http to https://stackoverflow.com/q/3825990/6695449.

Do końca dnia bawiłem się z naprawą crasha `java.io.NotSerializableException: kotlinx.uuid.UUID`.
Spowodowany jest on tym, że jako argument w `ConversationScreen` przekazuję `kotlinx.uuid.UUID`,
który to niestety nie jest serializowalny na androidzie (tj. nie implementuje
`java.lang.Serializable`). Więc żeby to zrobić ładnie, bez wycofywania się z wyboru tego typu
pomyślałem, że zmodyfikuję oryginalną bibliotekę. Najpierw sforkowałem oryginalą bibliotekę, która
jak się okazało... już dawno nie jest rozwijana i ja sam używam tutaj już forku *facepalm*.
Straciłem na to kilka godzin, bo przez to, że biblioteka 3 lata nie była ruszana, to nawet zbudować
się nie chciała. Kiedy już wziąłem tego forka i zrobiłem z niego swojego forka, dodałem według
instrukcji z docsów Voyagera https://voyager.adriel.cafe/state-restoration#multiplatform-state-restoration
JavaSerializable. Do tego teścik z serializacją i deserializacją według https://www.baeldung.com/java-serialization
i w ten sposób powstał taki PR https://github.com/hfhbd/kotlinx-uuid/pull/282. Sam crash raczej
nie jest mocno dotkliwy na tym etapie projektu, więc mogę poczekać, aż zostanie zmergowany do
*upstreamu*.

## Poszerzenie sprawdzania JWT na endpointy chatu (2023-12-30)

W końcu to trzeba było zrobić — czaty i wiadomości muszą być zabezpieczone. Nawet nie wiecie, jak
się ucieszyłem, gdy zobaczyłem, że jest gotowy plugin pełniący funkcję interceptora dodającego JWT
do nagłówka przy każdym requeście. Zachwyt nie trwał jednak zbyt długo, bo się okazało, że ten token
się tam zapisuje na wieki... aż nie trafi się request z 401. Ale znalazłem obejście, więc jakoś
to przeżyjemy https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death.

A tu ładne screeny jak cały mechanizm z refresh tokenami mógłby wyglądać
https://medium.com/@lahirujay/token-refresh-implementation-with-ktor-in-kotlin-multiplatform-mobile-f4d77b33b355

## Refresh tokenu (2023-12-31)

Zrobiłem refresh tokenu. Na backendzie mam tabelkę `usertokens` z wszystkimi refresh tokenami i przy
logowaniu tworzę userowi jednego (losowy stiring) i odsyłam razem z walidnym access tokenem (JWT).
Po tym, jak access token przestanie być ważny, to user ma puknąć pod `/api/auth/refresh`, gdzie
dostaje nowy refresh token (poprzedni jest usuwany) i nowy access token. Na razie ustawiłem ważność
access tokenu na minutę. Zastanawiam się, jak powszedni będzie problem z odebraniem nowego access
tokenu przez klienta. Bo jeśli backend go usunie, a user nie dostanie nowego, to efektywnie zostanie
wywalony z apki.

## Obsługa wygasłych refresh tokenów, ekran konta (2024-01-07)

Tydzień mi uciekł... 

Zrobiłem obsługę HTTP 401 w apce. Jak refresh token wygaśnie (po miesiącu) to apka raz, że w ogóle
skończy ładować dane (a wieszała się przez mój błąd — blokowała się po otrzymaniu 401 po refreshu)
to teraz wyloguje aktualnie zalogowanego usera i dodatkowo przeniesie go na WelcomeScreen, gdzie
będzie mógł się zalogować ponownie.

Dodałem też sobie ekran konta (z nazwą usera i przyciskiem do wylogowywania), bo czemu nie. Przyda
się.

## Dodawanie kontaktów (2024-01-08/10)

Voyager, voyager, voyager... niby masz te swoje nawigowanie między ekranami z integracją z tabami,
ale jak chce zrobić i to, i to, to działa to słabo!

## Materiały

- biblioteki KMM 1 - https://github.com/terrakok/kmm-awesome
- biblioteki KMM 2 - https://github.com/AAkira/Kotlin-Multiplatform-Libraries
- biblioteki KMP od IceRock - https://moko.icerock.dev/
- tutorial tworzenia konsolowej aplikacji czatu - https://ktor.io/docs/creating-web-socket-chat.html
- JWT do autoryzacji - https://ktor.io/docs/jwt.html
- moze jakaś integracja z ChatGPT? - https://github.com/yml-org/ychat
- przykładowa aplikacja KM w Compose UI i Swift UI - https://github.com/getspherelabs/cosmo-kmp
- przykładowy schemat bazy danych - https://github.com/yoosuf/Messenger
- aplikacja chatu z Gemini - https://github.com/chouaibMo/ChatGemini/tree/main


https://github.com/tunjid/Tiler
https://github.com/getspherelabs/cosmo-kmp
https://atscaleconference.com/messaging-at-scale/
https://www.donnfelker.com/why-kotlin-multiplatform-wont-succeed/
https://github.com/theapache64/rebugger
https://stackoverflow.com/questions/28907831/how-to-use-jti-claim-in-a-jwt
https://github.com/JetBrains/Exposed/wiki/Transactions
https://github.com/yveskalume/gemini-chat
