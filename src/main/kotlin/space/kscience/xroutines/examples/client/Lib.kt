package space.kscience.xroutines.examples.client

import space.kscience.xroutines.backend.MutableConfiguration
import space.kscience.xroutines.backend.def1

val conf = MutableConfiguration()

val square by conf.def1<Int, Int>()
