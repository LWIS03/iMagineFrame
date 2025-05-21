import {User} from "@/models/User";
import fetcher from "@/exceptionHandler/exceptionHandler";

export class Group {
  id: number;
  name: string;
  privileges: Privilege[];
  users: User[];

  constructor(id: number, name: string, privileges: Privilege[], users: User[]) {
    this.id = id;
    this.name = name;
    this.privileges = privileges;
    this.users = users;
  }
}

export class Privilege {
  id: number
  name: string;
  description: string;

  constructor(id: number, name: string, description: string) {
    this.id = id;
    this.name = name;
    this.description = description;
  }
}

export function checkPrivileges(requiredPrivileges: string[] | undefined, userPrivileges: string[] = []): boolean {
  if (requiredPrivileges == undefined) {
    return true;
  }
  const intersection = userPrivileges.filter(priv => requiredPrivileges.includes(priv));
  if (!intersection) {
    return false;
  }
  return intersection.length > 0;
}

export async function getGroups(authHeader = {}){
  try {
    const res = await fetcher.get("/groups", {headers: authHeader});
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

