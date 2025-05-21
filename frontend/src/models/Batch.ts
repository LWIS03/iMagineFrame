export class Batch {
  id: number;
  addedDate: Date;
  productId: number;
  quantity: number;
  unitPrice: number;
  expirationDate?: Date;

  constructor(
    id: number,
    addedDate: Date,
    productId: number,
    quantity: number,
    unitPrice: number,
    expirationDate?: Date
  ) {
    this.id = id;
    this.productId = productId;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.addedDate = addedDate;
    this.expirationDate = expirationDate;
  }
}

export class BatchAddDto {
  productId: number;
  quantity: number;
  unitPrice: number;
  expirationDate: string | null = null

  constructor(
    productId: number,
    quantity: number,
    unitPrice: number,
    expirationDate: string | null = null
  ) {
    this.productId = productId;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.expirationDate = expirationDate;
  }
}
