package pl.mojealterego.prawda

import kotlin.random.Random

enum class GameMode { CLASSIC, CHAOS, ROYAL, RED_ROOM }
enum class ChallengePace(val label: String, val seconds: Int) { RELAXED("Spokojny", 90), STANDARD("Standard", 60), RUSH("Panic", 45) }
enum class Intensity(val label: String) { EASY("Lekki"), MEDIUM("Średni"), BOLD("Odważny") }
enum class PromptKind { TRUTH, DARE }

data class Prompt(
    val id: String,
    val text: String,
    val kind: PromptKind,
    val intensity: Intensity,
    val points: Int = 1,
    val royal: Boolean = false
)

data class Player(val name: String, val score: Int = 0, val completed: Int = 0, val skipped: Int = 0)

data class GameState(
    val players: List<Player>,
    val activePlayer: Int = 0,
    val mode: GameMode = GameMode.CLASSIC,
    val intensity: Intensity = Intensity.MEDIUM,
    val currentPrompt: Prompt? = null,
    val history: Set<String> = emptySet(),
    val round: Int = 0,
    val customPrompts: List<Prompt> = emptyList(),
    val pace: ChallengePace = ChallengePace.STANDARD,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val totalSkipped: Int = 0
) {
    val currentPlayer: Player? get() = players.getOrNull(activePlayer)
}

object GameEngine {
    private val prompts = listOf(
        Prompt("t01","Jaka decyzja z ostatniego roku dała Ci najwięcej satysfakcji?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t02","Jaki talent chciał(a)byś mieć przez jeden dzień?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t03","Co najbardziej Cię rozśmiesza, choć trudno to wyjaśnić?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t04","Jakie pierwsze wrażenie zwykle robisz na innych?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t05","Jaka była Twoja najbardziej spontaniczna decyzja?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t06","Czego chciał(a)byś spróbować, ale jeszcze nie było okazji?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t07","Opowiedz o sytuacji, w której udało Ci się wyjść ze strefy komfortu.",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t08","Jaką cechę u siebie najbardziej doceniasz?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("d01","Przez następną rundę mów jak prezenter wiadomości.",PromptKind.DARE,Intensity.EASY),
        Prompt("d02","Wymyśl krótkie hasło reklamowe dla osoby po lewej.",PromptKind.DARE,Intensity.EASY),
        Prompt("d03","Pokaż bez słów swój ulubiony sposób spędzania wolnego czasu.",PromptKind.DARE,Intensity.EASY),
        Prompt("d04","Zrób 10-sekundową reklamę przedmiotu, który masz pod ręką.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d05","Wymyśl i wykonaj trzy ruchy taneczne.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d06","Opowiedz krótką historię, używając trzech słów wskazanych przez grupę.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d07","Zaśpiewaj refren dowolnej piosenki przez 15 sekund albo wybierz inne zadanie.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d08","Przez jedną rundę prowadź komentarz sportowy do zwykłych czynności grupy.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("t09","Jakiej umiejętności nauczyłeś się samodzielnie?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t10","Jaki drobiazg potrafi poprawić Ci humor?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t11","Które miejsce chcesz kiedyś odwiedzić?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t12","Jaka piosenka najlepiej opisuje Twój dzisiejszy nastrój?",PromptKind.TRUTH,Intensity.EASY),
        Prompt("t13","Z czego ostatnio jesteś szczególnie dumny lub dumna?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t14","Jaką radę z przeszłości pamiętasz do dziś?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t15","Co najczęściej odkładasz na później?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t16","Jaki zwyczaj chciałbyś lub chciałabyś wprowadzić do codzienności?",PromptKind.TRUTH,Intensity.MEDIUM),
        Prompt("t17","Jakie przekonanie zmieniło się u Ciebie z biegiem lat?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t18","W jakiej sytuacji najtrudniej poprosić Ci o pomoc?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t19","Co chciałbyś lub chciałabyś częściej mówić bliskim osobom?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("t20","Jaki błąd nauczył Cię czegoś ważnego?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("d09","Wymyśl tytuł filmu o dzisiejszym spotkaniu.",PromptKind.DARE,Intensity.EASY),
        Prompt("d10","Przez 20 sekund przedstaw prognozę pogody dla tego pokoju.",PromptKind.DARE,Intensity.EASY),
        Prompt("d11","Wskaż przedmiot w pobliżu i opisz go jak eksponat muzealny.",PromptKind.DARE,Intensity.EASY),
        Prompt("d12","Zrób minę przedstawiającą wybraną emocję, a reszta zgaduje.",PromptKind.DARE,Intensity.EASY),
        Prompt("d13","Wymyśl krótki jingiel dla drużyny.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d14","Opowiedz bajkę w trzech zdaniach, w której występuje kubek.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d15","Przez następną kolejkę odpowiadaj jak detektyw z filmu noir.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d16","Narysuj palcem w powietrzu logo fikcyjnej firmy i je zaprezentuj.",PromptKind.DARE,Intensity.MEDIUM),
        Prompt("d17","Zagraj bez słów scenę z filmu wybranego przez grupę; możesz odmówić wyboru.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d18","Wymyśl i wygłoś 15-sekundową mowę motywacyjną dla drużyny.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d19","Przedstaw krótką scenkę, w której zwykły przedmiot ratuje świat.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("d20","Zrób improwizowany wywiad z wybranym przedmiotem.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pt01","Co w drugiej osobie najbardziej przyciąga Twój wzrok?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt02","Jaki komplement od partnera działa na Ciebie najmocniej?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt03","Jaki rodzaj bliskości najbardziej buduje między Wami napięcie?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt04","Który wspólny moment wspominasz jako najbardziej romantyczny?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt05","Co partner robi zupełnie nieświadomie, a Ty uważasz to za bardzo atrakcyjne?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt06","Gdzie najbardziej chciał(a)byś pojechać tylko we dwoje?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt07","Jaki strój partnera najbardziej Ci się podoba?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt08","Co najbardziej lubisz w Waszych pocałunkach?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt09","Jaka cecha partnera najmocniej podkręca chemię między Wami?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt10","Jaki był najbardziej ekscytujący moment Waszej znajomości?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt11","Jak wyglądałaby Twoja idealna randka bez żadnych ograniczeń budżetowych?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt12","Co chciał(a)byś częściej słyszeć od partnera?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt13","Który pocałunek pamiętasz najlepiej?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt14","Co sprawia, że czujesz się najbardziej pożądany lub pożądana?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt15","Jaka atmosfera najbardziej sprzyja Waszej bliskości?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt16","Co jest bardziej pociągające: pewność siebie, tajemniczość czy czułość?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt17","Jaki gest partnera natychmiast poprawia Ci nastrój?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt18","Jaka rzecz między Wami daje Ci największe poczucie chemii?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt19","Jakie miejsce byłoby idealne na bardzo romantyczny wieczór?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt20","Jaki rodzaj flirtu najbardziej na Ciebie działa?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt21","Co chciał(a)byś zrobić razem po raz pierwszy podczas wyjątkowej randki?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt22","Wolisz spontaniczność czy budowanie napięcia przez cały wieczór?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt23","Która cecha wyglądu partnera najbardziej Ci się podoba?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt24","Jaki zapach najbardziej kojarzy Ci się z partnerem?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt25","Jaki sposób okazywania zainteresowania najbardziej Cię uwodzi?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt26","Co sprawia, że randka staje się dla Ciebie naprawdę niezapomniana?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt27","Jaki moment pierwszego spotkania pamiętasz najlepiej?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt28","Co chciał(a)byś, żeby partner częściej inicjował między Wami?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt29","Jakie trzy słowa najlepiej opisują chemię między Wami?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pt30","Jaki sekret udanej randki działa na Ciebie zawsze?",PromptKind.TRUTH,Intensity.BOLD,2),
        Prompt("pd01","Patrz partnerowi w oczy przez 30 sekund bez odwracania wzroku.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd02","Powiedz partnerowi szeptem trzy rzeczy, które uważasz w nim za atrakcyjne.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd03","Daj partnerowi długi, romantyczny pocałunek.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd04","Przez minutę flirtuj z partnerem tak, jakbyście właśnie się poznali.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd05","Wybierz piosenkę i zatańczcie razem przez minutę bardzo blisko siebie.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd06","Szepnij partnerowi, gdzie zabrał(a)byś go na idealną nocną randkę.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd07","Pozwól partnerowi wybrać miejsce na jeden czuły pocałunek: policzek, szyja albo dłoń.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd08","Powiedz partnerowi najbardziej bezpośredni, ale elegancki komplement, jaki przychodzi Ci do głowy.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd09","Usiądźcie obok siebie i przez 30 sekund trzymajcie się za ręce, patrząc sobie w oczy.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd10","Odtwórz Wasze pierwsze flirtujące zdanie albo wymyśl takie, które mogłoby nim być.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd11","Partner wybiera: pocałunek w czoło, policzek albo szyję.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd12","Powiedz partnerowi jednym zdaniem, dlaczego jest dla Ciebie pociągający.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd13","Zrób partnerowi 30-sekundowy masaż ramion.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd14","Przez jedną rundę zwracaj się do partnera wyłącznie czułym przezwiskiem.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd15","Wymyśl dla Was hasło, które mogłoby być tytułem romantycznego filmu.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd16","Podejdź do partnera i zaproś go na randkę tak, jakbyście widzieli się pierwszy raz.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd17","Powiedz partnerowi, co najbardziej podoba Ci się w jego spojrzeniu.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd18","Wybierz jedno wspomnienie i opowiedz partnerowi, dlaczego było dla Ciebie wyjątkowe.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd19","Przytul partnera przez 20 sekund bez mówienia.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd20","Zróbcie wspólne zdjęcie jak z okładki romantycznego filmu.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd21","Powiedz partnerowi szeptem jedno zdanie, które ma go zawstydzić w dobrym sensie.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd22","Partner wybiera piosenkę, a Ty zapraszasz go do krótkiego tańca.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd23","Wymień trzy rzeczy, które chciał(a)byś zrobić razem podczas weekendu tylko we dwoje.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd24","Przez 30 sekund trzymaj partnera za rękę i mów mu wyłącznie komplementy.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd25","Zamknij oczy; partner może dać Ci czuły pocałunek w wybrane przez siebie neutralne miejsce.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd26","Powiedz partnerowi, jaki jego gest najbardziej buduje między Wami napięcie.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd27","Wymyśl spontaniczny plan randki zaczynającej się dokładnie teraz.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd28","Stańcie naprzeciw siebie bardzo blisko i wytrzymajcie 20 sekund bez pocałunku.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd29","Powiedz partnerowi jedno zdanie, którym spróbował(a)byś go uwieść na pierwszej randce.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("pd30","Wybierz: długi uścisk, romantyczny pocałunek albo minutę wspólnego tańca.",PromptKind.DARE,Intensity.BOLD,2),
        Prompt("px31","Przez 60 sekund całuj partnera tak, jakbyście mieli zaraz się rozstać na miesiąc.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px32","Usiądź partnerowi na kolanach i przez 30 sekund utrzymuj kontakt wzrokowy.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px33","Zawiąż partnerowi oczy i daj mu trzy delikatne pocałunki w różne, uzgodnione miejsca.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px34","Szepnij partnerowi do ucha, czego najbardziej pragniesz podczas romantycznego wieczoru.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px35","Przez minutę masuj partnerowi kark i ramiona, stojąc bardzo blisko.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px36","Pozwól partnerowi wskazać trzy miejsca, w które chce dostać pocałunek.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px37","Zatańcz z partnerem bardzo blisko przez całą jedną piosenkę.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px38","Powoli pocałuj partnera w szyję, jeśli oboje macie na to ochotę.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px39","Przez minutę flirtuj bez słów, używając wyłącznie spojrzenia i gestów.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px40","Powiedz partnerowi szeptem trzy rzeczy, które najbardziej Cię w nim pociągają.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px41","Zdejmij partnerowi jeden uzgodniony element garderoby w najbardziej teatralny sposób.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px42","Pozwól partnerowi wybrać: długi pocałunek, masaż albo minutę bliskiego tańca.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px43","Przez 30 sekund stójcie bardzo blisko siebie bez dotykania.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px44","Pocałuj partnera pięć razy, za każdym razem w inne uzgodnione miejsce.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px45","Zrób partnerowi minutowy masaż pleców.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px46","Powiedz partnerowi, jaki jego gest najmocniej buduje między Wami napięcie.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px47","Zamknij oczy i pozwól partnerowi zaskoczyć Cię czułym, uzgodnionym gestem.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px48","Przyciągnij partnera do siebie i pocałuj go przez 20 sekund.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px49","Przez jedną rundę każde zdanie do partnera kończ komplementem.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px50","Usiądźcie naprzeciwko siebie i przez minutę trzymajcie dłonie na swoich udach lub dłoniach — wybór należy do Was.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px51","Szepnij partnerowi swoją najbardziej romantyczną fantazję o wspólnym wieczorze.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px52","Pozwól partnerowi wybrać miejsce na 30-sekundowy masaż: kark, ramiona, plecy albo dłonie.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px53","Zrób partnerowi powolny taniec bez muzyki przez 30 sekund.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px54","Powiedz partnerowi dokładnie, co zrobił(a), że pierwszy raz poczułeś lub poczułaś chemię.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px55","Przez minutę siedźcie przytuleni bez używania telefonów i bez rozmowy.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px56","Pocałuj partnera w sposób, który najlepiej pokazuje Twój dzisiejszy nastrój.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px57","Zagraj scenę uwodzenia z filmu noir — partner jest tajemniczym nieznajomym.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px58","Podejdź do partnera od tyłu i obejmij go przez 20 sekund.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px59","Pozwól partnerowi wybrać jeden czuły gest, który wykonasz natychmiast.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px60","Przez 45 sekund masuj partnerowi dłonie, patrząc mu w oczy.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px61","Powiedz partnerowi jedno zdanie, które chciał(a)byś usłyszeć podczas wyjątkowo romantycznej chwili.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px62","Zrób partnerowi serię pięciu coraz dłuższych pocałunków.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px63","Przez 30 sekund partner może prowadzić Wasz taniec, a Ty całkowicie podążasz za jego ruchem.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px64","Dotknij delikatnie twarzy partnera i powiedz, co najbardziej Ci się w niej podoba.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px65","Zamieńcie się rolami: osoba zwykle bardziej nieśmiała ma przez minutę prowadzić flirt.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px66","Zaproponuj partnerowi jedną rzecz, której chcielibyście spróbować na przyszłej randce.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px67","Przez 30 sekund całuj partnera wyłącznie w policzki, czoło i szyję — zgodnie z jego wyborem.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px68","Usiądźcie tak blisko, jak jest Wam komfortowo, i opowiedzcie sobie po jednym pragnieniu dotyczącym Waszej relacji.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px69","Partner wybiera: przytulenie od tyłu, pocałunek w szyję albo długi pocałunek.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("px70","Przez minutę zachowuj się tak, jakbyś próbował(a) uwieść partnera na pierwszym spotkaniu.",PromptKind.DARE,Intensity.BOLD,3),
        Prompt("r01","Królewski przywilej: wybierz osobę, która zdecyduje, czy następna runda będzie prawdą czy wyzwaniem.",PromptKind.DARE,Intensity.MEDIUM,2,true),
        Prompt("r02","Królewska karta: wskaż gracza, który otrzyma dodatkowy punkt za wykonanie wybranego przez siebie zadania.",PromptKind.DARE,Intensity.BOLD,2,true)
    )

    fun draw(state: GameState, kind: PromptKind, random: Random = Random): GameState {
        fun available(history: Set<String>) = (prompts + state.customPrompts).filter { prompt ->
            prompt.kind == kind &&
                (state.mode == GameMode.CHAOS || state.mode == GameMode.RED_ROOM || prompt.intensity.ordinal <= state.intensity.ordinal) &&
                (state.mode != GameMode.RED_ROOM || prompt.intensity == Intensity.BOLD) &&
                (state.mode == GameMode.ROYAL || !prompt.royal) &&
                prompt.id !in history
        }

        // History is shared by truth/dare cards. Recycle only IDs belonging to the
        // requested kind so drawing one deck never erases progress in the other.
        var history = state.history
        var eligible = available(history)
        if (eligible.isEmpty()) {
            val idsForKind = (prompts + state.customPrompts)
                .filter { it.kind == kind }
                .mapTo(mutableSetOf()) { it.id }
            history = history - idsForKind
            eligible = available(history)
        }
        if (eligible.isEmpty()) return state.copy(currentPrompt = null, history = history)

        // Royal cards act as a 15% wildcard draw, rather than being duplicated in the pool.
        val royalDraw = state.mode == GameMode.ROYAL &&
            kind == PromptKind.DARE &&
            eligible.any { it.royal } &&
            random.nextFloat() < 0.15f
        val pool = if (royalDraw) eligible.filter { it.royal } else eligible.filterNot { it.royal }.ifEmpty { eligible }
        val chosen = pool.random(random)
        return state.copy(currentPrompt = chosen, history = history + chosen.id, round = state.round + 1)
    }

    fun award(state: GameState, points: Int): GameState {
        if (state.players.isEmpty()) return state
        val updated = state.players.toMutableList()
        val player = updated[state.activePlayer]
        updated[state.activePlayer] = player.copy(score = (player.score + points).coerceAtLeast(0), completed = player.completed + 1)
        val streak = state.streak + 1
        return state.copy(players = updated, streak = streak, bestStreak = maxOf(state.bestStreak, streak), totalCompleted = state.totalCompleted + 1)
    }

    fun skip(state: GameState): GameState {
        if (state.players.isEmpty()) return state.copy(currentPrompt = null, streak = 0, totalSkipped = state.totalSkipped + 1)
        val updated = state.players.toMutableList()
        val player = updated[state.activePlayer]
        updated[state.activePlayer] = player.copy(skipped = player.skipped + 1)
        return state.copy(players = updated, currentPrompt = null, streak = 0, totalSkipped = state.totalSkipped + 1)
    }

    fun nextPlayer(state: GameState): GameState {
        if (state.players.isEmpty()) return state
        return state.copy(activePlayer = (state.activePlayer + 1) % state.players.size, currentPrompt = null)
    }

    fun resetHistory(state: GameState): GameState = state.copy(history = emptySet(), currentPrompt = null)
}
