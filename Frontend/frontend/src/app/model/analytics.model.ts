export interface PostCommentStats {
  weeklyPosts: number;
  monthlyPosts: number;
  yearlyPosts: number;
  weeklyComments: number;
  monthlyComments: number;
  yearlyComments: number;
}

export interface ActivityCategory {
  name: string;
  value: number; 
}

export interface ClientActivityStats {
  userActivity: ActivityCategory[];
}