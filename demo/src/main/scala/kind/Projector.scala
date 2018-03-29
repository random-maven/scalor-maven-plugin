package kind

/**
 * https://github.com/non/kind-projector
 */
object Projector {

  trait Functor[ F[ _ ] ]

  type Test1 = Functor[ Map[ Int, ? ] ]

  type Test2 = Functor[ ( { type T[ A ] = Map[ Int, A ] } )#T ]

  trait PF[ -F[ _ ], +G[ _ ] ] {
    def run[ A ]( fa : F[ A ] ) : G[ A ]
  }

  val f = Lambda[ PF[ List, Option ] ].run( _.headOption )

}
