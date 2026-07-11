package me.haroldmartin.golwallpaper.domain

/**
 * Decides whether the board should be reseeded with fresh cells after an update.
 *
 * Reseeding kicks in when the board is either:
 *  - dead: no live cells remain ([population] is 0), or
 *  - stagnant: the [currentState] is identical to [prevState] (a still life) or to
 *    [prevPrevState] (a period-2 oscillator), i.e. it equals either of the last two iterations.
 *
 * A null previous state (e.g. on the first couple of generations) never matches.
 */
fun shouldReseed(
    population: Int,
    currentState: String,
    prevState: String?,
    prevPrevState: String?,
): Boolean =
    population == 0 ||
        currentState == prevState ||
        currentState == prevPrevState
