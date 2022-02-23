@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.ml.dataservice.facade

import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.j
import io.lambdarpc.examples.ml.mlservice.Data
import io.lambdarpc.utils.toSid

val dataServiceId = "45b33c9c-ae42-4835-b27c-ad6f57b6a82d".toSid()

val dataLoader by dataServiceId.def(j<Data>())
