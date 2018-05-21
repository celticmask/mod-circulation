package org.folio.circulation.domain.policy;

import api.support.builders.LoanBuilder;
import api.support.builders.LoanPolicyBuilder;
import api.support.builders.Period;
import io.vertx.core.json.JsonObject;
import org.folio.circulation.support.HttpResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static api.support.matchers.FailureMatcher.isValidationFailure;
import static org.junit.Assert.assertThat;

public class InvalidLoanPolicyTests {
  @Test
  public void shouldFailWhenNoLoanPolicyProvided() {
    final JsonObject representation = new LoanPolicyBuilder()
      .rolling(new Period(5, "Unknown"))
      .withName("Invalid Loan Policy")
      .create();

    representation.remove("loansPolicy");

    LoanPolicy loanPolicy = LoanPolicy.from(representation);

    DateTime loanDate = new DateTime(2018, 3, 14, 11, 14, 54, DateTimeZone.UTC);

    JsonObject loan = new LoanBuilder()
      .open()
      .withLoanDate(loanDate)
      .create();

    final HttpResult<DateTime> result = loanPolicy.calculate(loan);

    //TODO: This is fairly ugly, replace with a better message
    assertThat(result, isValidationFailure(
      "Item can't be checked out as profile \"\" in the loan policy is not recognised. " +
        "Please review \"Invalid Loan Policy\" before retrying checking out"));
  }

}