package com.roxballs.bikebuild

data class BikePart(
    val id: String,
    val title: String,
    val description: String,
    val purpose: String,
)

data class AssemblyStep(
    val part: BikePart,
    val instruction: String,
)

data class BikeAssemblyScenario(
    val title: String,
    val subtitle: String,
    val intro: String,
    val steps: List<AssemblyStep>,
)

fun defaultBikeScenario(): BikeAssemblyScenario = BikeAssemblyScenario(
    title = "Сборка велосипеда",
    subtitle = "Простой сценарий для первого знакомства",
    intro = "Собери велосипед по шагам. На каждом шаге приложение подсказывает, какая деталь нужна сейчас.",
    steps = listOf(
        AssemblyStep(
            part = BikePart(
                id = "frame",
                title = "Рама",
                description = "Главная основа велосипеда.",
                purpose = "Рама держит всю конструкцию и соединяет остальные узлы.",
            ),
            instruction = "Начни с рамы. С нее начинается вся сборка.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "fork",
                title = "Передняя вилка",
                description = "Передняя деталь между рулем и колесом.",
                purpose = "Передняя вилка держит переднее колесо и соединяет его с рамой.",
            ),
            instruction = "Теперь поставь переднюю вилку. Она находится спереди: между рулем и передним колесом.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "handlebar",
                title = "Руль",
                description = "Элемент управления велосипедом.",
                purpose = "Руль нужен, чтобы направлять велосипед и удерживать баланс.",
            ),
            instruction = "После вилки поставь руль.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "seat",
                title = "Седло",
                description = "Место для посадки велосипедиста.",
                purpose = "Седло делает посадку удобной и помогает держать правильное положение тела.",
            ),
            instruction = "Поставь седло на верхнюю часть рамы.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "front_wheel",
                title = "Переднее колесо",
                description = "Переднее колесо велосипеда.",
                purpose = "Переднее колесо помогает входить в поворот и держать направление.",
            ),
            instruction = "Переднее колесо крепится к вилке.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "rear_wheel",
                title = "Заднее колесо",
                description = "Заднее колесо велосипеда.",
                purpose = "Заднее колесо принимает тягу от педалей и катит велосипед вперед.",
            ),
            instruction = "Теперь поставь заднее колесо.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "pedals",
                title = "Педали",
                description = "Платформы для ног.",
                purpose = "Педали передают усилие ног в привод велосипеда.",
            ),
            instruction = "После колес поставь педали в центр велосипеда.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "chain",
                title = "Цепь",
                description = "Соединяет педали и заднее колесо.",
                purpose = "Цепь передает вращение от педалей на заднее колесо.",
            ),
            instruction = "Теперь поставь цепь между педалями и задним колесом.",
        ),
        AssemblyStep(
            part = BikePart(
                id = "brakes",
                title = "Тормоза",
                description = "Система остановки велосипеда.",
                purpose = "Тормоза нужны, чтобы безопасно снижать скорость и останавливаться.",
            ),
            instruction = "В конце поставь тормоза. После этого сборка закончена.",
        ),
    ),
)
