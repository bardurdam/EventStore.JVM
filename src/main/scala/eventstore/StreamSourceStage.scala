package eventstore

import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Status.Failure
import akka.stream._
import akka.stream.stage._
import akka.stream.{ Attributes, SourceShape }
import eventstore.ReadDirection.Forward
import EventNumber._

private[eventstore] class StreamSourceStage(
    connection:          ActorRef,
    streamId:            EventStream.Id,
    fromNumberExclusive: Option[EventNumber],
    credentials:         Option[UserCredentials],
    settings:            Settings,
    infinite:            Boolean                 = true
) extends GraphStage[SourceShape[Event]] {

  val out: Outlet[Event] = Outlet("StreamSource")
  val shape: SourceShape[Event] = SourceShape(out)

  def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new SourceStageLogic[Event, EventNumber, Exact](
      shape, out, streamId, connection, credentials, settings, infinite
    ) {

      import settings._

      final val first: Exact = First
      final val eventFrom: IndexedEvent ⇒ Event = _.event
      final val pointerFrom: Exact ⇒ Long = _.value
      final val positionFrom: Event ⇒ Exact = _.record.number

      final def operation: ReadFrom = fromNumberExclusive match {
        case Some(Last)     ⇒ ReadFrom.End
        case Some(e: Exact) ⇒ ReadFrom.Exact(e)
        case None           ⇒ ReadFrom.Beginning
      }

      final def buildReadEventsFrom(next: Exact): Out = ReadStreamEvents(
        streamId, next, readBatchSize, Forward, resolveLinkTos, requireMaster
      )

      final def rcvRead(onRead: (List[Event], Exact, Boolean) => Unit, onNotExists: => Unit): Receive = {
        case ReadStreamEventsCompleted(e, n: Exact, _, eos, _, Forward) ⇒ onRead(e, n, eos)
        case Failure(_: StreamNotFoundException)                        ⇒ onNotExists
      }

      final def rcvSubscribed(onSubscribed: Option[Exact] ⇒ Unit): Receive = {
        case SubscribeToStreamCompleted(_, subscriptionNumber) ⇒ onSubscribed(subscriptionNumber)
      }

    }
}