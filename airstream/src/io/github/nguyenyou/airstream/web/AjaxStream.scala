package io.github.nguyenyou.airstream.web

import io.github.nguyenyou.airstream.core.{EventStream, Observer, Transaction, WritableStream}
import io.github.nguyenyou.airstream.web.AjaxStream._
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.|

// @TODO[Test] Needs testing

/**
  * [[AjaxStream]] performs an HTTP request and emits an [[dom.XMLHttpRequest]] on success,
  * or an [[AjaxStreamError]] error (AjaxStatusError | AjaxNetworkError | AjaxTimeout | AjaxAbort) on failure.
  *
  * Acceptable HTTP response status codes are 2xx and 304, others result in AjaxStatusError.
  *
  * The network request is only performed when the stream is started.
  *
  * When stream is restarted, a new request is performed, and the subsequent response is emitted.
  * The previous request is not aborted, but its response will be ignored.
  *
  * Warning: dom.XmlHttpRequest is an ugly, imperative JS construct. We set event callbacks for
  * onload, onerror, onabort, ontimeout, and if requested, also for onprogress and onreadystatechange.
  * Make sure you don't override Airstream's listeners, or this stream will not work properly.
  *
  * @see [[dom.XMLHttpRequest]] for a description of the parameters
  *
  * @param requestObserver          - called just before the request is sent
  * @param progressObserver         - called when download progress is reported
  * @param uploadProgressObserver   - called when upload progress is reported
  * @param readyStateChangeObserver - called when readyState changes
  */
class AjaxStream(
  method: String,
  url: String,
  data: InputData,
  timeoutMs: Int,
  headers: Map[String, String],
  withCredentials: Boolean,
  responseType: String,
  isStatusCodeSuccess: Int => Boolean = defaultIsStatusCodeSuccess,
  requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
  progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
  uploadProgressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
  readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
) extends WritableStream[dom.XMLHttpRequest] {

  override protected val topoRank: Int = 1

  private var maybePendingRequest: js.UndefOr[dom.XMLHttpRequest] = js.undefined

  /** This stream will emit at most one event per request regardless of the outcome.
    *
    * You need to introspect the result to determine whether the request
    * succeeded, failed, timed out, or was aborted.
    */
  lazy val completeEvents: EventStream[dom.XMLHttpRequest] = {
    this.recover {
      case err: AjaxStreamError => Some(err.xhr)
    }
  }

  override protected def onWillStart(): Unit = ()

  override protected def onStart(): Unit = {
    val request = AjaxStream.initRequest(timeoutMs, withCredentials, responseType)

    maybePendingRequest = request

    // As far as I can tell, only one of onload / onerror / onabort / ontimeout events can fire for every request,
    // so the callbacks below are both mutually exhaustive and encompass all possible outcomes.
    // If this is not the case, we have a problem.

    // Note: XMLHttpRequest is a valid target for native JS addEventListener, which is a better API,
    // but that pattern isn't supported on XMLHttpRequest by all browsers (e.g. IE11 doesn't work).

    request.onload = (_: dom.Event) => {
      if (maybePendingRequest.contains(request)) {
        maybePendingRequest = js.undefined
        val status = request.status
        if (isStatusCodeSuccess(status))
          Transaction(fireValue(request, _))
        else
          Transaction(fireError(AjaxStatusError(request, status, s"Ajax request failed: $status ${request.statusText}"), _))
      }
    }

    request.onerror = (_: dom.Event) => {
      if (maybePendingRequest.contains(request)) {
        maybePendingRequest = js.undefined

        // @TODO I can't figure out how to get a detailed error message in this case.
        //  - `ev` is not actually a dom.ErrorEvent, but a useless dom.ProgressEvent
        //  - Reasons could be network, DNS, CORS, etc.

        Transaction(fireError(AjaxNetworkError(request, s"Ajax request failed: unknown network reason."), _))
      }
    }

    request.onabort = (_: js.Any) => {
      if (maybePendingRequest.contains(request)) {
        maybePendingRequest = js.undefined
        Transaction(fireError(AjaxAbort(request), _))
      }
    }

    request.ontimeout = (_: dom.Event) => {
      if (maybePendingRequest.contains(request)) {
        maybePendingRequest = js.undefined
        Transaction(fireError(AjaxTimeout(request), _))
      }
    }

    // The following observers are optional.

    if (progressObserver != Observer.empty) {
      request.onprogress = ev => {
        if (maybePendingRequest.contains(request)) {
          progressObserver.onNext((request, ev))
        }
      }
    }

    if (uploadProgressObserver != Observer.empty) {
      request.upload.onprogress = ev => {
        if (maybePendingRequest.contains(request)) {
          uploadProgressObserver.onNext((request, ev))
        }
      }
    }

    if (readyStateChangeObserver != Observer.empty) {
      request.onreadystatechange = (_: dom.Event) => {
        if (maybePendingRequest.contains(request)) {
          readyStateChangeObserver.onNext(request)
        }
      }
    }

    if (requestObserver != Observer.empty) {
      requestObserver.onNext(request)
    }

    // Actually initiate the network request
    AjaxStream.sendRequest(request, method, url, data, headers)

    super.onStart()
  }

  override protected def onStop(): Unit = {
    maybePendingRequest = js.undefined
    super.onStop()
  }
}

object AjaxStream {

  type InputData = String | js.typedarray.ArrayBufferView | dom.Blob | dom.FormData

  /** A more detailed version of [[dom.ext.AjaxException]] (no relation) */
  sealed abstract class AjaxStreamError(val xhr: dom.XMLHttpRequest, message: String) extends Exception(message)

  final case class AjaxStatusError(override val xhr: dom.XMLHttpRequest, val status: Int, message: String) extends AjaxStreamError(xhr, message)

  final case class AjaxNetworkError(override val xhr: dom.XMLHttpRequest, message: String) extends AjaxStreamError(xhr, message)

  final case class AjaxTimeout(override val xhr: dom.XMLHttpRequest) extends AjaxStreamError(xhr, "Ajax request timed out.")

  final case class AjaxAbort(override val xhr: dom.XMLHttpRequest) extends AjaxStreamError(xhr, "Ajax request was aborted.")

  // @TODO[API] I'm not sure that creating an Ajax request should result in a stream of responses.
  //  - Another alternative is that it should result in an object that exposes several streams, e.g. responseStream,
  //    progressStream, etc. - but it seems that with such an approach the usage would get more complicated as
  //    it would be hard to manage timing and laziness properly (e.g. for progressStream)

  // @TODO[API] Consider API like AjaxEventStream(_.GET, url, ...) using something like dom.experimental.HttpMethod

  /**
    * Returns an [[EventStream]] that performs an HTTP `GET` request.
    *
    * @see [[AjaxStream]]
    */
  def get(
    url: String,
    data: InputData = null,
    timeoutMs: Int = 0,
    headers: Map[String, String] = Map.empty,
    withCredentials: Boolean = false,
    responseType: String = "",
    isStatusCodeSuccess: Int => Boolean = defaultIsStatusCodeSuccess,
    requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
    progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    uploadProgressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  ): AjaxStream = {
    new AjaxStream(
      "GET",
      url,
      data,
      timeoutMs,
      headers,
      withCredentials,
      responseType,
      isStatusCodeSuccess,
      requestObserver,
      progressObserver,
      uploadProgressObserver,
      readyStateChangeObserver
    )
  }

  /**
    * Returns an [[EventStream]] that performs an HTTP `POST` request.
    *
    * @see [[AjaxStream]]
    */
  def post(
    url: String,
    data: InputData = null,
    timeoutMs: Int = 0,
    headers: Map[String, String] = Map.empty,
    withCredentials: Boolean = false,
    responseType: String = "",
    isStatusCodeSuccess: Int => Boolean = defaultIsStatusCodeSuccess,
    requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
    progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    uploadProgressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  ): AjaxStream = {
    new AjaxStream(
      "POST",
      url,
      data,
      timeoutMs,
      headers,
      withCredentials,
      responseType,
      isStatusCodeSuccess,
      requestObserver,
      progressObserver,
      uploadProgressObserver,
      readyStateChangeObserver
    )
  }

  /**
    * Returns an [[EventStream]] that performs an HTTP `PUT` request.
    *
    * @see [[AjaxStream]]
    */
  def put(
    url: String,
    data: InputData = null,
    timeoutMs: Int = 0,
    headers: Map[String, String] = Map.empty,
    withCredentials: Boolean = false,
    responseType: String = "",
    isStatusCodeSuccess: Int => Boolean = defaultIsStatusCodeSuccess,
    requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
    progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    uploadProgressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  ): AjaxStream = {
    new AjaxStream(
      "PUT",
      url,
      data,
      timeoutMs,
      headers,
      withCredentials,
      responseType,
      isStatusCodeSuccess,
      requestObserver,
      progressObserver,
      uploadProgressObserver,
      readyStateChangeObserver
    )
  }

  /**
    * Returns an [[EventStream]] that performs an HTTP `PATCH` request.
    *
    * @see [[AjaxStream]]
    */
  def patch(
    url: String,
    data: InputData = null,
    timeoutMs: Int = 0,
    headers: Map[String, String] = Map.empty,
    withCredentials: Boolean = false,
    responseType: String = "",
    isStatusCodeSuccess: Int => Boolean = defaultIsStatusCodeSuccess,
    requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
    progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    uploadProgressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  ): AjaxStream = {
    new AjaxStream(
      "PATCH",
      url,
      data,
      timeoutMs,
      headers,
      withCredentials,
      responseType,
      isStatusCodeSuccess,
      requestObserver,
      progressObserver,
      uploadProgressObserver,
      readyStateChangeObserver
    )
  }

  /**
    * Returns an [[EventStream]] that performs an HTTP `DELETE` request.
    *
    * @see [[AjaxStream]]
    */
  def delete(
    url: String,
    data: InputData = null,
    timeoutMs: Int = 0,
    headers: Map[String, String] = Map.empty,
    withCredentials: Boolean = false,
    responseType: String = "",
    isStatusCodeSuccess: Int => Boolean = defaultIsStatusCodeSuccess,
    requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
    progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    uploadProgressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
    readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  ): AjaxStream = {
    new AjaxStream(
      "DELETE",
      url,
      data,
      timeoutMs,
      headers,
      withCredentials,
      responseType,
      isStatusCodeSuccess,
      requestObserver,
      progressObserver,
      uploadProgressObserver,
      readyStateChangeObserver
    )
  }

  /** Initializes and configures the XmlHttpRequest. This does not cause any network activity.
    *
    * Note: after initializing the request, you need to openRequest(), and then sendRequest()
    *
    * AjaxEventStream already does this internally. This is provided as a building block for custom logic.
    */
  def initRequest(
    timeoutMs: Int = 0,
    withCredentials: Boolean = false,
    responseType: String = ""
  ): dom.XMLHttpRequest = {
    val request = new dom.XMLHttpRequest
    request.responseType = responseType
    request.timeout = timeoutMs.toDouble
    request.withCredentials = withCredentials
    request
  }

  /** The request should be initialized and configured with all the callbacks by this point.
    *
    * AjaxEventStream already does this internally. This is provided as a building block for custom logic.
    */
  def sendRequest(
    request: dom.XMLHttpRequest,
    method: String,
    url: String,
    data: InputData = null,
    headers: Map[String, String] = Map.empty
  ): Unit = {
    request.open(method, url)
    headers.foreach(Function.tupled(request.setRequestHeader))
    if (data == null) request.send() else request.send(data.asInstanceOf[js.Any])
  }

  def defaultIsStatusCodeSuccess(status: Int): Boolean = {
    (status >= 200 && status < 300) || status == 304
  }
}
