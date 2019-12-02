package api.loans;

import static api.support.matchers.LoanMatchers.hasLoanProperty;
import static api.support.matchers.LoanMatchers.hasOpenStatus;
import static api.support.matchers.LoanMatchers.hasStatus;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import api.support.APITests;
import api.support.builders.DeclareItemLostRequestBuilder;
import api.support.http.InventoryItemResource;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.folio.circulation.support.http.client.IndividualResource;
import org.folio.circulation.support.http.client.Response;
import org.joda.time.DateTime;
import org.junit.Test;

public class DeclareLostAPITests extends APITests {

  private InventoryItemResource item;
  private IndividualResource user;
  private JsonObject loanJson;

  @Override
  public void beforeEach()
    throws MalformedURLException, InterruptedException, ExecutionException,
    TimeoutException {

    super.beforeEach();
    item = itemsFixture.basedUponSmallAngryPlanet();
    user = usersFixture.charlotte();

    loanJson = loansFixture.checkOutByBarcode(item, user).getJson();
  }

  @Test
  public void canDeclareItemLostWithComment()
    throws InterruptedException, ExecutionException, TimeoutException,
    MalformedURLException {
    UUID loanID = UUID.fromString(loanJson.getString("id"));
    String comment = "testing";
    DateTime dateTime = DateTime.now();

    Response response = loansFixture.declareItemLost(
      new DeclareItemLostRequestBuilder().forLoanId(loanID).on(dateTime)
        .withComment(comment)

    );

    JsonObject actualLoan = loansClient.getById(loanID).getJson();
    JsonObject actualItem = actualLoan.getJsonObject("item");

    assertThat(response.getStatusCode(), is(204));
    assertThat(actualItem, hasStatus("Declared lost"));
    assertThat(actualLoan, hasOpenStatus());
    assertThat(actualLoan, hasLoanProperty("action", "declaredLost"));
    assertThat(actualLoan, hasLoanProperty("actionComment", comment));
    assertThat(actualLoan, hasLoanProperty("declaredLostDate", dateTime.toString()));
  }

  @Test
  public void canDeclareItemLostWithoutComment()
    throws InterruptedException, ExecutionException, TimeoutException,
    MalformedURLException {
    UUID loanID = UUID.fromString(loanJson.getString("id"));
    DateTime dateTime = DateTime.now();

    Response response = loansFixture.declareItemLost(
      new DeclareItemLostRequestBuilder().forLoanId(loanID).on(dateTime)
        .withNoComment());

    JsonObject actualLoan = loansClient.getById(loanID).getJson();
    JsonObject actualItem = actualLoan.getJsonObject("item");

    assertThat(response.getStatusCode(), is(204));
    assertThat(actualItem, hasStatus("Declared lost"));
    assertThat(actualLoan, hasOpenStatus());
    assertThat(actualLoan, hasLoanProperty("action", "declaredLost"));
    assertThat(actualLoan, hasLoanProperty("actionComment", StringUtils.EMPTY));
    assertThat(actualLoan, hasLoanProperty("declaredLostDate", dateTime.toString()));
  }

  @Test
  public void cannotDeclareItemLostForAClosedLoan()
    throws InterruptedException, ExecutionException, TimeoutException,
    MalformedURLException {
    UUID loanID = UUID.fromString(loanJson.getString("id"));
    DateTime dateTime = DateTime.now();

    loansFixture.checkInByBarcode(item);

    Response response = loansFixture.declareItemLost(
      new DeclareItemLostRequestBuilder().forLoanId(loanID).on(dateTime)
        .withNoComment());

    JsonObject actualLoan = loansClient.getById(loanID).getJson();
    JsonObject actualItem = actualLoan.getJsonObject("item");

    assertThat(response.getStatusCode(), is(422));
    assertThat(actualItem, not(hasStatus("Declared lost")));
    assertThat(actualLoan, not(hasLoanProperty("action", "declaredLost")));
    assertThat(actualLoan, not(hasLoanProperty("actionComment", "declaredLost")));
    assertThat(actualLoan, not(hasLoanProperty("declaredLostDate")));
  }

  @Test
  public void shouldReturn404IfLoanIsNotFound() {

    Response response = loansFixture.declareItemLost(
      new DeclareItemLostRequestBuilder().forLoanId(UUID.randomUUID())
        .on(DateTime.now()).withNoComment());

    assertThat(response.getStatusCode(), is(404));
  }


}