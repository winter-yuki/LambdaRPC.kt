package basic

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.toSid

val serviceId1 = "f74127d2-d27f-4271-b46e-10b79143260e".toSid()
val endpoint1 = Endpoint("localhost:8088")

val serviceId2 = "72ba6c7d-0953-44e2-a4c6-2feb3582c24d".toSid()
val endpoint2 = Endpoint("localhost:8089")
