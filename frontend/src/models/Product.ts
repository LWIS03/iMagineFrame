export class Product {
  productId: number;
  name: string;
  description: string;
  tagsName: Set<string>;
  tagsId: Set<number>;
  properties: Record<string, string>;
  batchIds: Set<number>
  imageUrl: string | ArrayBuffer | null;
  showProperties: boolean;
  showBatches: boolean;
  imageBlobUrl?: string;

  constructor(productId: number,
              name: string,
              description: string,
              properties: Record<string, string>,
              batchIds: Set<number>,
              tagsName: Set<string>,
              tagsId: Set<number>,
              imageUrl: string | ArrayBuffer | null = null

  ) {

    this.productId = productId;
    this.name = name;
    this.description = description;
    this.properties = properties;
    this.batchIds = batchIds;
    this.tagsName = tagsName;
    this.imageUrl = imageUrl;
    this.showProperties = false;
    this.showBatches = true;
    this.tagsId = tagsId;
  }



}

export class ProductEditDto {

  name: string;
  description: string;
  properties: Record<string, string>;
  image: File| null;

  constructor(name: string = "",
              description: string = "",
              properties: {} = {},
              image: File | null = null
  ) {
    this.name = name;
    this.description = description;
    this.properties = properties;
    this.image = image;
  }

}

