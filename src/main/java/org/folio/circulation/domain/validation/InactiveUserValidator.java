package org.folio.circulation.domain.validation;

import org.folio.circulation.domain.LoanAndRelatedRecords;
import org.folio.circulation.domain.User;
import org.folio.circulation.support.HttpResult;
import org.folio.circulation.support.ServerErrorFailure;
import org.folio.circulation.support.ValidationErrorFailure;

import java.util.function.Function;

import static org.folio.circulation.domain.representations.CheckOutByBarcodeRequest.PROXY_USER_BARCODE;
import static org.folio.circulation.support.HttpResult.failed;
import static org.folio.circulation.support.HttpResult.succeeded;

public class InactiveUserValidator {
  private final Function<String, ValidationErrorFailure> inactiveUserErrorFunction;
  private final String inactiveUserMessage;
  private final String cannotDetermineMessage;
  private final Function<LoanAndRelatedRecords, User> userFunction;

  public InactiveUserValidator(
    Function<LoanAndRelatedRecords, User> userFunction, String inactiveUserMessage, String cannotDetermineMessage, Function<String, ValidationErrorFailure> inactiveUserErrorFunction) {

    this.inactiveUserErrorFunction = inactiveUserErrorFunction;
    this.inactiveUserMessage = inactiveUserMessage;
    this.cannotDetermineMessage = cannotDetermineMessage;
    this.userFunction = userFunction;
  }

  public HttpResult<LoanAndRelatedRecords> refuseWhenUserIsInactive(
    HttpResult<LoanAndRelatedRecords> loanAndRelatedRecords) {

    return loanAndRelatedRecords.next(records -> {
      try {
        final User user = userFunction.apply(records);

        if(user == null) {
          return loanAndRelatedRecords;
        }
        else if (user.canDetermineStatus()) {
          return failed(inactiveUserErrorFunction.apply(
            cannotDetermineMessage));
        }
        if (user.isInactive()) {
          return failed(inactiveUserErrorFunction.apply(
            inactiveUserMessage));
        } else {
          return succeeded(records);
        }
      } catch (Exception e) {
        return failed(new ServerErrorFailure(e));
      }
    });
  }

  public HttpResult<LoanAndRelatedRecords> refuseWhenProxyingUserIsInactive(
    HttpResult<LoanAndRelatedRecords> loanAndRelatedRecords) {

    return loanAndRelatedRecords.next(loan -> {
      final User proxyingUser = loan.getProxyingUser();

      if(proxyingUser == null) {
        return loanAndRelatedRecords;
      }
      else if (proxyingUser.canDetermineStatus()) {
        return failed(ValidationErrorFailure.failure(
          "Cannot determine if proxying user is active or not",
          PROXY_USER_BARCODE, proxyingUser.getBarcode()));
      }
      else if(proxyingUser.isInactive()) {
        return failed(ValidationErrorFailure.failure(
          "Cannot check out via inactive proxying user",
          PROXY_USER_BARCODE, proxyingUser.getBarcode()));
      }
      else {
        return loanAndRelatedRecords;
      }
    });
  }
}