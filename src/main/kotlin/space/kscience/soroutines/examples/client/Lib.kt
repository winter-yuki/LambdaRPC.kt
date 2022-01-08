package space.kscience.soroutines.examples.client

import space.kscience.soroutines.frontend.MutableConfiguration
import space.kscience.soroutines.frontend.def

val conf = MutableConfiguration()

val square by conf.def<Int, Int>()
