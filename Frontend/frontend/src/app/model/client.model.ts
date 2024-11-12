export interface Client {
    id: number;
    email: string;
    username: string;
    password?: string; 
    firstName: string;
    lastName: string;
    enabled: boolean;
    numberOfPosts: number;
    following: number;
    active: boolean;
  }