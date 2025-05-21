import fetcher from "@/exceptionHandler/exceptionHandler";
import { useAuthStore } from "@/stores/AuthStore";
import { Group } from "./Group";

export class RegistrationProposal {
    firstName: string | null;
    lastName: string | null;
    username: string | null;
    email: string | null;
    password: string | null;
    repeatPassword: string | null;

    constructor(
        firstName: string | null = null,
        lastName: string | null = null,
        email: string | null = null,
        username: string | null = null,
        password: string | null = null,
        repeatPassword: string | null = null,
    ){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.repeatPassword = repeatPassword;
    }
}

export class Registration {
    id: number;
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    repeatPassword: string;
    accepted: boolean;
    dateCreated: string;
    dateUpdated: string;

    constructor(
        id: number,
        firstName: string,
        lastName: string,
        username: string,
        email: string,
        password: string,
        repeatPassword: string,
        accepted: boolean,
        dateCreated: string,
        dateUpdated: string
    ){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.accepted = accepted;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;      
    }
}

export class RegistrationResponse{
    id: number;
    accepted: boolean;
    groups: Group[];
    
    constructor(
        id: number, 
        accepted: boolean,
        groups: Group[]
    ){
        this.id=id;
        this.accepted=accepted;
        this.groups=groups
    }
}

export async function getAllRegistrations(): Promise<Registration[]> {
    const authStore = useAuthStore()
    try {
        const response = await fetcher.get("/register" , {headers: authStore.authHeader()})  
        return response.data as Registration[];
    } catch (err) {
        console.error("Failed to fetch registration requests")
        return {} as Registration[]
    }
    }