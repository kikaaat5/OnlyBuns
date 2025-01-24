export interface Post {
    id: number;
    userId: number;
    description: string;
    createdAt: String;  
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

  export interface Like {
    id: number;
    userId: number;
    postId:  number;
    createdAt: string;
  }