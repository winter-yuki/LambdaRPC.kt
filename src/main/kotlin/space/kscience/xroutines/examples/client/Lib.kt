package space.kscience.xroutines.examples.client

import space.kscience.xroutines.frontend.MutableConfiguration
import space.kscience.xroutines.frontend.def
import space.kscience.xroutines.serialization.f1
import space.kscience.xroutines.serialization.s

val conf = MutableConfiguration()

val square by conf.def(s<Int>(), s<Int>())

val eval by conf.def(f1<Int, Int>(), s<Int>())
