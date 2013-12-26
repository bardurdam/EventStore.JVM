package eventstore
package j

import Builder._

class ReadAllEventsBuilder extends Builder[ReadAllEvents]
    with MaxCountSnippet[ReadAllEventsBuilder]
    with DirectionSnippet[ReadAllEventsBuilder]
    with ResolveLinkTosSnippet[ReadAllEventsBuilder]
    with RequireMasterSnippet[ReadAllEventsBuilder] {

  private var _fromPosition: Position = Position.First

  def fromFirstPosition: ReadAllEventsBuilder = set {
    _fromPosition = Position.First
  }

  def fromLastPosition: ReadAllEventsBuilder = set {
    _fromPosition = Position.Last
    backward
  }

  def fromPosition(commitPosition: Long, preparePosition: Long): ReadAllEventsBuilder = set {
    _fromPosition = Position(commitPosition, preparePosition)
  }

  def maxCount(x: Int): ReadAllEventsBuilder = MaxCountSnippet.maxCount(x)

  def forward: ReadAllEventsBuilder = DirectionSnippet.forward
  def backward: ReadAllEventsBuilder = DirectionSnippet.backward

  def resolveLinkTos(x: Boolean): ReadAllEventsBuilder = ResolveLinkTosSnippet.resolveLinkTos(x)

  def requireMaster(x: Boolean): ReadAllEventsBuilder = RequireMasterSnippet.requireMaster(x)

  def build: ReadAllEvents = ReadAllEvents(
    fromPosition = _fromPosition,
    maxCount = MaxCountSnippet.value,
    direction = DirectionSnippet.value,
    resolveLinkTos = ResolveLinkTosSnippet.value,
    requireMaster = RequireMasterSnippet.value)
}
