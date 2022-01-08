
//
//inline fun <reified A1, reified A2, reified R>
//        MutableConfiguration.def2(accessName: String? = null) =
//    def<R, Soroutine2<A1, A2, R>>(accessName) { name, block ->
//        Soroutine2(name, Serializer.of(), Serializer.of(), Serializer.of(), block)
//    }
//
//inline fun <reified A1, reified A2, reified A3, reified R>
//        MutableConfiguration.def3(accessName: String? = null) =
//    def<R, Soroutine3<A1, A2, A3, R>>(accessName) { name, block ->
//        Soroutine3(
//            name,
//            Serializer.of(), Serializer.of(),
//            Serializer.of(), Serializer.of(),
//            block
//        )
//    }
//
//inline fun <reified A1, reified A2, reified A3, reified A4, reified R>
//        MutableConfiguration.def4(accessName: String? = null) =
//    def<R, Soroutine4<A1, A2, A3, A4, R>>(accessName) { name, block ->
//        Soroutine4(
//            name,
//            Serializer.of(), Serializer.of(), Serializer.of(),
//            Serializer.of(), Serializer.of(),
//            block
//        )
//    }
