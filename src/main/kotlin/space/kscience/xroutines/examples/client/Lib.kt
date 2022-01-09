package space.kscience.xroutines.examples.client

import space.kscience.lambdarpc.dsl.f1
import space.kscience.lambdarpc.dsl.frontend.MutableConfiguration
import space.kscience.lambdarpc.dsl.frontend.def
import space.kscience.lambdarpc.dsl.s

val conf = MutableConfiguration()

val square by conf.def(s<Int>(), s<Int>())

val eval by conf.def(f1<Int, Int>(), s<Int>())
