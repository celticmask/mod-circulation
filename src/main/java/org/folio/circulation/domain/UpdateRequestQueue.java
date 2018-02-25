package org.folio.circulation.domain;

import io.vertx.core.json.JsonObject;
import org.folio.circulation.resources.RelatedRecords;
import org.folio.circulation.support.Clients;
import org.folio.circulation.support.HttpResult;
import org.folio.circulation.support.ServerErrorFailure;

import java.util.concurrent.CompletableFuture;

public class UpdateRequestQueue {
  private final Clients clients;

  public UpdateRequestQueue(Clients clients) {
    this.clients = clients;
  }

  public CompletableFuture<HttpResult<JsonObject>> onCheckIn(RelatedRecords relatedRecords) {
    CompletableFuture<HttpResult<JsonObject>> requestUpdated = new CompletableFuture<>();

    if (relatedRecords.requestQueue().hasOutstandingRequests()) {
      JsonObject firstRequest = relatedRecords.requestQueue().getFirst();

      firstRequest.put("status", RequestStatus.OPEN_AWAITING_PICKUP);

      clients.requestsStorage().put(firstRequest.getString("id"), firstRequest,
        updateRequestResponse -> {
          if (updateRequestResponse.getStatusCode() == 204) {
            requestUpdated.complete(HttpResult.success(firstRequest));
          } else {
            requestUpdated.complete(HttpResult.failure(new ServerErrorFailure(
              String.format("Failed to update request: %s: %s",
                updateRequestResponse.getStatusCode(), updateRequestResponse.getBody()))));
          }
        });
    } else {
      requestUpdated.complete(HttpResult.success(null));
    }

    return requestUpdated;
  }

  public CompletableFuture<HttpResult<JsonObject>> onCheckOut(RequestQueue requestQueue) {
    CompletableFuture<HttpResult<JsonObject>> requestUpdated = new CompletableFuture<>();

    if (requestQueue.hasOutstandingRequests()) {
      JsonObject firstRequest = requestQueue.getFirst();

      firstRequest.put("status", RequestStatus.CLOSED_FILLED);

      clients.requestsStorage().put(firstRequest.getString("id"), firstRequest,
        updateRequestResponse -> {
          if (updateRequestResponse.getStatusCode() == 204) {
            requestUpdated.complete(HttpResult.success(firstRequest));
          } else {
            requestUpdated.complete(HttpResult.failure(new ServerErrorFailure(
              String.format("Failed to update request: %s: %s",
                updateRequestResponse.getStatusCode(), updateRequestResponse.getBody()))));
          }
      });
    } else {
      requestUpdated.complete(HttpResult.success(null));
    }

    return requestUpdated;
  }
}
