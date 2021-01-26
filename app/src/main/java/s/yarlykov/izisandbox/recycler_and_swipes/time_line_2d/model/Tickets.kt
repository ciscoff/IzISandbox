package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model

import s.yarlykov.izisandbox.extensions.minutes

object Tickets {

    private const val DAY_START = 9
    private const val DAY_END = 21

    val model = listOf(
        Ticket(
            "Ticket 1",
            10.minutes,
            11.minutes,
            DAY_START.minutes,
            DAY_END.minutes,
            listOf(9.minutes to 10.minutes)
        ),
        Ticket(
            "Ticket 2",
            9.minutes,
            10.minutes,
            DAY_START.minutes,
            DAY_END.minutes,
            listOf(10.minutes to 11.minutes, 18.minutes to 20.minutes)
        ),
        Ticket(
            "Ticket 3",
            12.minutes,
            14.minutes,
            DAY_START.minutes,
            DAY_END.minutes,
            listOf(10.minutes to 11.minutes)
        ),
        Ticket(
            "Ticket 4",
            19.minutes,
            20.minutes,
            DAY_START.minutes,
            DAY_END.minutes,
            listOf(
                12.minutes to 13.minutes,
                17.minutes to 18.minutes,
                18.minutes to 20.minutes
            )
        ),
        Ticket(
            "Ticket 5",
            16.minutes,
            18.minutes,
            DAY_START.minutes,
            DAY_END.minutes,
            listOf(10.minutes to 11.minutes)
        )
    )
}

