import { Product} from "@/models/Product";

export class Tag{
  id: number;
  name:string;
  products: Set<Product>;

  constructor(id: number,
              name:string,
              products: Set<Product>) {

    this.id = id;
    this.name = name;
    this.products = products;
  }

}
