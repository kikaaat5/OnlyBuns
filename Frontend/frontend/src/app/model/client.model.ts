export interface Client {
    id: number;
    email: string;
    username: string;
    password?: string; 
    firstname: string;
    lastname: string;
    enabled: boolean;
    numberOfPosts: number;
    following: number;
    active: boolean;
  }