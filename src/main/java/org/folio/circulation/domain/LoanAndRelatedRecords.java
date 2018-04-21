package org.folio.circulation.domain;

import io.vertx.core.json.JsonObject;
import org.folio.circulation.support.InventoryRecords;

public class LoanAndRelatedRecords {
  public final JsonObject loan;
  public final InventoryRecords inventoryRecords;
  public final RequestQueue requestQueue;
  public final JsonObject requestingUser;
  public final String loanPolicyId;
  public final JsonObject location;
  public final JsonObject materialType;

  private LoanAndRelatedRecords(
    JsonObject loan,
    InventoryRecords inventoryRecords,
    RequestQueue requestQueue,
    JsonObject requestingUser,
    String loanPolicyId,
    JsonObject location, JsonObject materialType) {

    this.loan = loan;
    this.inventoryRecords = inventoryRecords;
    this.requestQueue = requestQueue;
    this.requestingUser = requestingUser;
    this.loanPolicyId = loanPolicyId;
    this.location = location;
    this.materialType = materialType;
  }

  public LoanAndRelatedRecords(JsonObject loan) {
    this(loan, null, null, null, null, null, null);
  }

  public LoanAndRelatedRecords withItem(JsonObject updatedItem) {
    return new LoanAndRelatedRecords(loan, new InventoryRecords(updatedItem,
      inventoryRecords.getHolding(), inventoryRecords.getInstance()),
      requestQueue, requestingUser, loanPolicyId, location, this.materialType);
  }

  public LoanAndRelatedRecords withLoan(JsonObject newLoan) {
    return new LoanAndRelatedRecords(newLoan, inventoryRecords, requestQueue,
      requestingUser, loanPolicyId, location, this.materialType);
  }

  public LoanAndRelatedRecords withRequestingUser(JsonObject newUser) {
    return new LoanAndRelatedRecords(loan, inventoryRecords, requestQueue,
      newUser, loanPolicyId, location, this.materialType);
  }

  public LoanAndRelatedRecords withLoanPolicy(String newLoanPolicyId) {
    return new LoanAndRelatedRecords(loan, inventoryRecords, requestQueue,
      requestingUser, newLoanPolicyId, location, this.materialType);
  }

  public LoanAndRelatedRecords withRequestQueue(RequestQueue newRequestQueue) {
    return new LoanAndRelatedRecords(loan, inventoryRecords, newRequestQueue,
      requestingUser, loanPolicyId, location, this.materialType);
  }

  public LoanAndRelatedRecords withLocation(JsonObject newLocation) {
    return new LoanAndRelatedRecords(loan, inventoryRecords, requestQueue,
      requestingUser, loanPolicyId, newLocation, this.materialType);
  }

  public LoanAndRelatedRecords withInventoryRecords(InventoryRecords newInventoryRecords) {
    return new LoanAndRelatedRecords(loan, newInventoryRecords, requestQueue,
      requestingUser, loanPolicyId, location, this.materialType);
  }

  public LoanAndRelatedRecords withMaterialType(JsonObject newMaterialType) {
    return new LoanAndRelatedRecords(loan, inventoryRecords, requestQueue,
      requestingUser, loanPolicyId, location, newMaterialType);
  }
}