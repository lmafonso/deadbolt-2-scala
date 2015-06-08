package be.objectify.deadbolt.scala.views.patternTest

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, SecurityRole, User}
import be.objectify.deadbolt.scala.views.html.patternTest.patternOrContent
import be.objectify.deadbolt.scala.{DeadboltModule, DeadboltHandler, DynamicResourceHandler}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Results, Request, Result}
import play.api.test.{Helpers, FakeRequest, PlaySpecification, WithApplication}
import play.libs.Scala

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class EqualityOrTest extends PlaySpecification {

   "when the subject has a permission that is equal to the pattern, the view" should {
     "show constrained content and hide fallback content" in new WithApplication(new GuiceApplicationBuilder()
                                                                                   .bindings(new DeadboltModule())
                                                                                   .build()) {
       val html = patternOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)
         override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(Some(new DynamicResourceHandler() {
           override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
           override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
         }))
         override def getSubject[A](request: Request[A]): Future[Option[Subject]] = Future(Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List(new SecurityPermission("killer.undead.zombie"))))))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, value = "killer.undead.zombie", patternType = PatternType.EQUALITY)(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

  "when the subject has no permissions that are equal to the pattern, the view" should {
    "hide constrained content and show fallback content" in new WithApplication(new GuiceApplicationBuilder()
                                                                                  .bindings(new DeadboltModule())
                                                                                  .build()) {
      val html = patternOrContent(new DeadboltHandler() {
        override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)
        override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(Some(new DynamicResourceHandler() {
          override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
          override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
        }))
        override def getSubject[A](request: Request[A]): Future[Option[Subject]] = Future(Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List(new SecurityPermission("killer.undead.vampire"))))))
        override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
      }, value = "killer.undead.zombie", patternType = PatternType.EQUALITY)(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is default content in case the constraint denies access to the protected content.")
      content must contain("This is after the constraint.")
    }
  }
 }
