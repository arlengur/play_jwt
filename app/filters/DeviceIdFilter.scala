package filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class DeviceIdFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    requestHeader.headers.get("device-id")
      .map(_ => nextFilter(requestHeader))
      .getOrElse(Future.successful(Results.Unauthorized("Device-id has not been sent.")))
  }
}


