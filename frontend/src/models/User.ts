import jwtDecode from "jwt-decode";
import {Group} from "@/models/Group";
import fetcher from "@/exceptionHandler/exceptionHandler";
import {useAuthStore } from "@/stores/AuthStore";

export class User {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string | null;
  repeatPassword: string | null;
  groups: Array<Group>;
  privacyLevel: string;

  constructor(id: number,
              firstName: string,
              lastName: string,
              email: string,
              username: string,
              password: string | null,
              repeatPassword: string | null,
              groups: Array<Group>,
              privacyLevel: string = "PUBLIC") {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
    this.password = password;
    this.repeatPassword = repeatPassword;
    this.groups = groups;
    this.privacyLevel = privacyLevel;
  }
}

export class UserCredentials {
  identifier: string;
  password: string;

  constructor(identifier: string, password: string) {
    this.identifier = identifier;
    this.password = password;
  }
}

export class JWTUser{
  id: string;
  username: string;
  privileges: string[];
  exp: number;
  iat: number;
  iss: string;
  sub: string;
  constructor(id: string, username: string, privileges: string[], exp: number, iat: number, iss: string, sub: string) {
    this.id = id;
    this.username = username;
    this.privileges = privileges;
    this.exp = exp;
    this.iat = iat;
    this.iss = iss;
    this.sub = sub;
  }
}

export class JWTToken {
  token: string;

  constructor(token: string) {
    this.token = token;
  }
  decode(): JWTUser {
    return jwtDecode(this.token);
  }
}



export async function getUser(id: string): Promise<User> {
  const authStore = useAuthStore()
  try {
    const response = await fetcher.get("/users/" + parseInt(id), {headers: authStore.authHeader()})
    return response.data as User;
  } catch (err) {
    console.error("Failed to fetch user ", id)
    return {} as User
  }
}
