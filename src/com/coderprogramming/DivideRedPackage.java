package com.coderprogramming;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Coder编程
 * @Title: RedPackageTestTwo
 * @ProjectName red-package
 * @Description: 二倍均值法
 * @date 2020/3/1013:25
 */
public class DivideRedPackage {

    //二倍均值法
    public static List<Integer> divideRedPackage(Integer totalAmount,
                                                 Integer totalPeopleNum) {
        List<Integer> amountList = new ArrayList<Integer>();

        //为了使用random.nextInt(Integer)方法不得不先把红包金额放大100倍，最后在main函数里面再除以100
        //这样就可以保证每个人抢到的金额都可以精确到小数点后两位

        Integer restAmount = totalAmount * 100;

        Integer restPeopleNum = totalPeopleNum;

        Random random = new Random();

        for (int i = 0; i < totalPeopleNum - 1; i++) {

            // 随机范围：[1，剩余人均金额的两倍)，左闭右开

            int amount = random.nextInt(restAmount / restPeopleNum * 2 - 1) + 1;
            restAmount -= amount;
            restPeopleNum--;
            amountList.add(amount);
        }
        amountList.add(restAmount);

        return amountList;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            List<Integer> totalRedPackage = DivideRedPackage.divideRedPackage(100, 5);
            BigDecimal count = new BigDecimal(0);
            System.out.print("（欢迎关注公众号：Coder编程）第 " + (i + 1) + " 组数据： ");
            for (Integer amount : totalRedPackage) {
                BigDecimal tmpcount = new BigDecimal(amount).divide(new BigDecimal(100));
                count = count.add(tmpcount);
                System.out.print(tmpcount + "  ");
            }
            System.out.println();
        }
    }
}
