package org.folio.circulation.support.fetching;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.folio.circulation.support.http.client.CqlQuery.exactMatch;
import static org.folio.circulation.support.http.client.PageLimit.limit;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.folio.circulation.domain.MultipleRecords;
import org.folio.circulation.support.FindWithCqlQuery;
import org.folio.circulation.support.GetManyRecordsClient;
import org.folio.circulation.support.Result;
import org.folio.circulation.support.http.client.CqlQuery;
import org.folio.circulation.support.http.client.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import junitparams.JUnitParamsRunner;

@RunWith(JUnitParamsRunner.class)
public class FindMultipleRecordsUsingCqlTests {
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private GetManyRecordsClient client;

  @Test
  public void shouldFetchRecordsInSinglePage() {
    final FindWithCqlQuery<JsonObject> fetcher = new CqlQueryFinder<>(client,
      "records", identity(), limit(10));

    final Result<CqlQuery> query = exactMatch("Status", "Open");

    when(client.getMany(eq(query.value()), eq(limit(10))))
      .thenReturn(cannedResponse(10));

    MultipleRecords<JsonObject> records = getRecords(fetcher.findByQuery(query));

    assertThat(records.getTotalRecords(), is(10));
    assertThat(records.getRecords().size(), is(10));

    verify(client, times(1)).getMany(eq(query.value()), eq(limit(10)));
  }

  private CompletableFuture<Result<Response>> cannedResponse(
      int numberOfRecordsInPage) {

    return CompletableFuture.completedFuture(Result.of(() -> {
      final JsonObject body = new JsonObject();

      body.put("records", new JsonArray(generateRecords(numberOfRecordsInPage)));
      body.put("totalRecords", numberOfRecordsInPage);

      return new Response(200, body.encodePrettily(), "application/json");
    }));
  }

  private List<JsonObject> generateRecords(int numberOfRecords) {
    return Stream.generate(UUID::randomUUID)
      .limit(numberOfRecords)
      .map(id -> new JsonObject().put("id", id.toString()))
      .collect(toList());
  }

  private MultipleRecords<JsonObject> getRecords(
    CompletableFuture<Result<MultipleRecords<JsonObject>>> fetchedRecords) {

    try {
      final Result<MultipleRecords<JsonObject>> result = fetchedRecords
        .get(1, SECONDS);

      return result.value();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
