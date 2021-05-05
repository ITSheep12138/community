package com.comunity;

import java.util.*;

public class Main{
    public static void main(String[] args){
        //0-1背包，动态规划求解
        /**
           1. 确定状态
               设dp[i][j] 表示前i个物品，当前背包容量为j，这种情况下可以装的最大值
               是dp[i][j];
               比如说：dp[3][5] = 6,就是对于给定的一系列物品中，若只对前3个物品进行
               选择，当背包容量为5时，最多可以装下的价值为6.
           2. 状态转移方程
               1）假设第i件物品装不下，那么最大收益就是dp[i][j] = dp[i-1][j];
               2) 假设第i件物品装得下，那么最大收益就是
                   dp[i][j] = dp[i-1][j-wt[i-1]] + val[i-1];
                   这个公式的意思就是：如果我们这件物品装得下，那么整个背包的最大收益
                   应该是这件物品的价值加上把这件物品去除之后，背包剩下的容量可以装的最大
                   值！
           3. 初始条件
               dp[i][0] = 0   dp[0][j] = 0;
        */
        //输入数据
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int m = sc.nextInt();
        int wt[] = new int[n];
        int val[] = new int[n];
        int dp[][] = new int[n+1][m+1];
        for(int i = 0;i<n;i++){
            wt[i] = sc.nextInt();
        }
        for(int i = 0;i<n;i++){
            val[i] = sc.nextInt();
        }

        //核心算法
        for(int i=1;i<=n;i++){
            for(int j=1;j<=m;j++){
                if(j - wt[i-1] < 0){
                    //装不下
                    dp[i][j] = dp[i-1][j];
                }else{
                    dp[i][j] = Math.max(dp[i-1][j-wt[i-1]] + val[i-1],dp[i-1][j]);
                }
            }
        }
        System.out.print(dp[n][m]);
    }
}
