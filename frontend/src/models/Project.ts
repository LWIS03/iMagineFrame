export class Project {
  id: number;
  name: string;
  description: string;
  mediaUrl: string | null;
  status: string;
  users: any[];
  owner: any;
  public: boolean = true;

  constructor(
    id: number = 0,
    name: string = "",
    description: string = "",
    mediaUrl: string | null = null,
    status: string = "PLANNING",
    users: any[] = [],
    isPublic: boolean = true,
    owner: any = null
  ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.mediaUrl = mediaUrl;
    this.status = status;
    this.users = users;
    this.public = isPublic;
    this.owner = owner;
  }
}

export class ProjectEditDto {
  name: string;
  description: string;
  mediaUrl: string | null;
  status: string;
  users: any[] | null;
  owner: any;
  public: boolean = true;

  constructor(
    name: string = "",
    description: string = "",
    mediaUrl: string | null = null,
    status: string = "PLANNING",
    users: any[] | null = [],
    isPublic: boolean = true,
    owner: any = null
  ) {
    this.name = name;
    this.description = description;
    this.mediaUrl = mediaUrl;
    this.status = status;
    this.users = users;
    this.public = isPublic;
    this.owner = owner;
  }
}

import fetcher from "@/exceptionHandler/exceptionHandler";

export async function getPublicProjects(){
  try {
    const res = await fetcher.get("/projects/public");
    if(res && res.data) {
      return res.data;
    } else {
      console.log("No data returned from API: " + res.status);
      return [];
    }
  } catch (error) {
    return [];
  }
}

export async function getProjects(authHeader = {}){
  try {
    const res = await fetcher.get("/projects", {headers: authHeader});
    if(res && res.data) {
      return res.data;
    } else {
      console.log("No data returned from API: " + res.status);
      return [];
    }
  } catch (error) {
    return [];
  }
}

export function getStatusColor(status: string): string {
  switch (status) {
    case 'PLANNING':
      return 'blue';
    case 'IN_PROGRESS':
      return 'amber';
    case 'COMPLETED':
      return 'green';
    case 'ON_HOLD':
      return 'orange';
    case 'CANCELLED':
      return 'red';
    default:
      return 'grey';
  }
}