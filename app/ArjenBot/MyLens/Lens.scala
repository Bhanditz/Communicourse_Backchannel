package MyLens

/**
  * Created by robertMueller on 10.02.17.
  */
case class MyLens[A,B](get: A => B, set: (A,B) => A) extends Function1[A, B] with Immutable{
  //f: A -> B
  def apply(a: A): B = get(a)
  def mod(a: A)(f: B => B) = set(a, f(this(a)))
  def updated(a: A, b: B): A = set(a, b)
  def compose[C](that: MyLens[C,A]) = MyLens[C,B](
    c => this(that(c)), //get
    (c, b) => that.mod(c)(set(_, b)) //set
  )
  // get -> get/set
  def andThen[C](that: MyLens[B,C]) = that compose this
}

object MyLens {

  def containsKey[A,B](key: A) = MyLens[Map[A,B], Option[B]](
    get = (m: Map[A,B]) => m.get(key),
    set = (m:Map[A,B], opt: Option[B]) =>
      opt match {
        case None => m - key
        case Some(value) => m + (key -> value)
      })
}
