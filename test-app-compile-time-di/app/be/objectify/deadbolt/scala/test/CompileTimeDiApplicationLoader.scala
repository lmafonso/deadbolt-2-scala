package be.objectify.deadbolt.scala.test

import java.util.regex.Pattern

import _root_.controllers.Assets
import be.objectify.deadbolt.scala.{ExecutionContextProvider, TemplateFailureListener, TemplateFailureListenerProvider, DeadboltComponents}
import be.objectify.deadbolt.scala.cache.{PatternCache, HandlerCache}
import be.objectify.deadbolt.scala.test.dao.{TestSubjectDao, SubjectDao}
import be.objectify.deadbolt.scala.test.security.{MyCustomTemplateFailureListener, MyHandlerCache}
import play.api.libs.ws.ning.NingWSComponents
import play.api.{BuiltInComponentsFromContext, Application, ApplicationLoader}
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import scala.concurrent.ExecutionContext
import be.objectify.deadbolt.scala.test.controllers._
import router.Routes

/**
 * Application loader for enabling compile-time DI.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
class CompileTimeDiApplicationLoader extends ApplicationLoader  {
  override def load(context: Context): Application = new ApplicationComponents(context).application
}

class ApplicationComponents(context: Context) extends BuiltInComponentsFromContext(context) with DeadboltComponents with NingWSComponents {

  lazy val subjectDao: SubjectDao = new TestSubjectDao
  
  override lazy val patternCache: PatternCache = new PatternCache {
    override def apply(v1: String): Option[Pattern] = Some(Pattern.compile(v1))
  }

  override lazy val handlers: HandlerCache = new MyHandlerCache(subjectDao) 

  override lazy val ecContextProvider: ExecutionContextProvider = new ExecutionContextProvider {
    override val get: ExecutionContext = scala.concurrent.ExecutionContext.global
  }

  lazy val builderDynamic: builder.Dynamic = new builder.Dynamic(actionBuilders)
  lazy val builderPattern: builder.Pattern = new builder.Pattern(actionBuilders)
  lazy val builderRestrict: builder.Restrict = new builder.Restrict(actionBuilders)
  lazy val builderSubject: builder.Subject = new builder.Subject(actionBuilders)

  lazy val composedDynamic: composed.Dynamic = new composed.Dynamic(deadboltActions)
  lazy val composedPattern: composed.Pattern = new composed.Pattern(deadboltActions)
  lazy val composedRestrict: composed.Restrict = new composed.Restrict(deadboltActions)
  lazy val composedSubject: composed.Subject = new composed.Subject(deadboltActions)

  override lazy val router: Router = new Routes(httpErrorHandler,
                                                 builderDynamic,
                                                 builderPattern,
                                                 builderRestrict,
                                                 builderSubject,
                                                 composedDynamic,
                                                 composedPattern,
                                                 composedRestrict,
                                                 composedSubject,
                                                 "")

  lazy val assets = new Assets(httpErrorHandler)
}