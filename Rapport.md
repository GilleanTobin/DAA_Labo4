
# Question Labo 4 DAA

## 6.1 

> Quelle est la meilleure approche pour sauver, même après la fermeture de l’app, le choix de l’option de tri de la liste des notes ? Vous justifierez votre réponse et l’illustrez en présentant le code mettant en œuvre votre approche

On enregistre le nom de l’option (CREATION ou SCHEDULE) sous une clé typée, puis on afiche la valeur sous forme de "Flow". La valeur est conservée après la fermeture du process, contrairement à une propriété en mémoire ou à "onSaveInstanceState".
```kotlin
private val SORT_OPTION = stringPreferencesKey("sort_option")

val sortOption: Flow<SortOption> =
    context.sortDataStore.data.map { preferences ->
        SortOption.valueOf(
            preferences[SORT_OPTION] ?: SortOption.CREATION.name
        )
    }

suspend fun setSortOption(option: SortOption) {
    context.sortDataStore.edit { preferences ->
        preferences[SORT_OPTION] = option.name
    }
}
```


## 6.2
> ’accès à la liste des notes issues de la base de données Room se fait avec une LiveData. Est-ce  que  cette  solution  présente  des  limites ?  Si  oui,  quelles  sont-elles  et  quelles  seraient  les approches mieux adaptées ?


LiveData est utile pour exposé une valeur observable à une Activity ou un Fragment en respectant son cycle de vie.

On peut aussi utiliser un "Flow" Room et le ViewModel le convertir en "StateFlow". L’interface le collecte avec "repeatOnLifecycle" ou avec les API de cycle de vie de Compose. Les modifications de données restent des fonctions "suspend" exécutées dans une routine. 

Dans ce projet, les lectures Room sont observées avec LiveData, tandis que les insertions et suppressions sont des fonctions suspendues lancées depuis "viewModelScope".

## 6.3

>  Les notes affichées dans la RecyclerView ne sont pas sélectionnables ni cliquables. Comment procéderiez-vous  si  vous  souhaitiez  proposer  une  interface  permettant  de  sélectionner une note pour l’éditer ?

Avec la classe "RecyclerItemClickListener" qui indique quand on clique sur le RecyclerView. Quand ça arrive on peut calaculer la position du curseur et en déduire ou on clique puis on fait les action qui permettent d'éditer si besoin
