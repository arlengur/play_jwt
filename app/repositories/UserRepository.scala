package repositories

import model.User
import repositories.UserRepository.{UserExists, UserNotFound}

import scala.concurrent.Future

trait UserRepository {
  def add(deviceId: String, name: String, pass: String): Future[User]

  def find(name: String, pass: String): Future[User]

  def find(id: Long, refreshToken: String): Future[User]

  def update(id: Long, refreshToken: String): Future[User]
}

object UserRepository {

  final case class UserNotFound() extends Exception(s"User with such name or password not found.")
  final case class UserExists() extends Exception(s"User with such name already exists.")

}

class InMemoryUserRepository extends UserRepository {
  private var users: Vector[User] = Vector[User](User(1, "device-id", Option("Bob"), Option("pass1")))

  override def find(name: String, pass: String): Future[User] =
    users.find(u => u.name.getOrElse("") == name && u.password.getOrElse("") == pass) match {
      case Some(user) => Future.successful(user)
      case None => Future.failed(UserNotFound())
    }

  override def find(id: Long, refreshToken: String): Future[User] =
    users.find(u => u.id == id && u.refreshToken.getOrElse("") == refreshToken) match {
      case Some(user) => Future.successful(user)
      case None => Future.failed(UserNotFound())
    }

  override def update(id: Long, refreshToken: String): Future[User] = users.find(_.id == id) match {
    case Some(user) =>
      val newUser = user.copy(refreshToken = Option(refreshToken))
      users = users.map(u => if (u.id == id) newUser else u)
      Future.successful(newUser)
    case None => Future.failed(UserNotFound())
  }

  override def add(deviceId: String, name: String, pass: String): Future[User] =
    users.find(_.name.getOrElse("") == name) match {
      case Some(_) => Future.failed(UserExists())
      case None =>
        users.find(u => u.deviceId == deviceId) match {
          case Some(user) =>
            val newUser = user.copy(name = Option(name), password = Option(pass))
            users = users.map(u => if (u.deviceId == deviceId) newUser else u)
            Future.successful(newUser)
          case None =>
            val newUser = User(users.size+1, deviceId, Option(name), Option(pass))
            users = users :+ newUser
            Future.successful(newUser)
        }
    }

  override def toString = s"InMemoryUserRepository:\n${users.mkString("\n")}"
}