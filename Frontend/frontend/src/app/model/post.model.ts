export interface Post {
    id: number;
    userId: number;
    description: string;
    createdAt: string;  
    imagePath: string;
    longitude: number;
    latitude: number;
    likesCount: number;
    comments: Comment[];  
  }
  
  export interface Comment {
    id: number;
    userId: number;
    content: string;
    createdAt: string;
  }