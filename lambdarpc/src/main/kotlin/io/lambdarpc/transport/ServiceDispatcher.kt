package io.lambdarpc.transport

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

interface ServiceDispatcher : Map<ServiceId, Endpoint>
