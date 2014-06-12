package model

trait Identity {
  val id: String
}

trait Builder[T] {
  def build: T
}
