package nl.tue.setschematics.action

import kotlin.random.Random

abstract class RandomStateAction(val random: Random = Random.Default) :
        StateAction
