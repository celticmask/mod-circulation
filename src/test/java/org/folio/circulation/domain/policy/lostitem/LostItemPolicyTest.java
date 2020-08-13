package org.folio.circulation.domain.policy.lostitem;

import static org.folio.circulation.domain.policy.Period.from;
import static org.folio.circulation.domain.policy.Period.minutes;
import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.folio.circulation.domain.policy.Period;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class LostItemPolicyTest {

  @Test
  @Parameters({
    "Minutes, 78",
    "Hours, 9",
    "Days, 66",
    "Weeks, 23",
    "Months, 13",
  })
  public void shouldNotAgeItemToLostIfDueDateAfterNow(String interval, int duration) {
    final Period period = from(duration, interval);
    final LostItemPolicy lostItemPolicy = lostItemPolicyWithAgePeriod(period);

    final DateTime loanDueDate = now(UTC).plus(period.timePeriod())
      .plus(minutes(1).timePeriod());

    assertFalse(lostItemPolicy.canAgeLoanToLost(loanDueDate));
  }

  @Test
  @Parameters({
    "Minutes, 43",
    "Hours, 12",
    "Days, 29",
    "Weeks, 1",
    "Months, 5",
  })
  public void shouldAgeItemToLostIfDueDateBeforeNow(String interval, int duration) {
    final Period period = from(duration, interval);
    final LostItemPolicy lostItemPolicy = lostItemPolicyWithAgePeriod(period);

    final DateTime loanDueDate = now(UTC).minus(period.timePeriod())
      .minus(minutes(3).timePeriod());

    assertTrue(lostItemPolicy.canAgeLoanToLost(loanDueDate));
  }

  @Test
  @Parameters({
    "Minutes, 123",
    "Hours, 99",
    "Days, 64",
    "Weeks, 2",
    "Months, 3",
  })
  public void shouldAgeItemToLostIfDueDateIsNow(String interval, int duration) {
    final Period period = from(duration, interval);
    final LostItemPolicy lostItemPolicy = lostItemPolicyWithAgePeriod(period);

    final DateTime loanDueDate = now(UTC).minus(period.timePeriod());

    assertTrue(lostItemPolicy.canAgeLoanToLost(loanDueDate));
  }

  @Test
  public void shouldNotAgeItemToLostIfPeriodIsMissingInPolicy() {
    final LostItemPolicy lostItemPolicy = lostItemPolicyWithAgePeriod(null);

    assertFalse(lostItemPolicy.canAgeLoanToLost(now(UTC)));
  }

  private LostItemPolicy lostItemPolicyWithAgePeriod(Period period) {
    final JsonObject representation = new JsonObject();
    if (period != null) {
      representation.put("itemAgedLostOverdue", period.asJson());
    }

    return LostItemPolicy.from(representation);
  }
}
