export interface Post {
    id: number;
    userId: number;
    description: string;
    createdAt: String;  
    imagePath: string;
    compressedImagePath: string;
    longitude: number;
    latitude: number;
    likesCount: number;
    comments: Comment[];  
    hasLiked?: boolean; 
    showComments?: boolean;
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

   export interface PostComment {
    postId:number,
    userId: number;
    username: string,
    content: string;
    createdAt: Date |null;
  }

  export interface PostResponseDto {
  id: number;
  userId: number;
  description: string;
  longitude: number;
  latitude: number;
  imagePath: string;
  likesCount: number;
  createdAt: string;
  comments: PostComment[];
}