package org.folio.circulation.domain;

public class ItemAndRelatedRecords {

  private Item item;
  private Request request;
  private Loan loan;

  public ItemAndRelatedRecords(Item item) {
    this.item = item;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public Request getRequest() {
    return request;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public Loan getLoan() {
    return loan;
  }

  public void setLoan(Loan loan) {
    this.loan = loan;
  }
}
