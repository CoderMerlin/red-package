package com.coderprogramming;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Coder编程
 * @Title: SegmentRedPackage
 * @ProjectName red-package
 * @Description: 线段分割法
 * @date 2020/3/1013:46
 */
public class SegmentRedPackage {

    /**
     * 线段分割法
     * @param money
     * @param n
     * @return
     */
    private static List<Integer> segmentRedPackage(double money, int n) {
        //验证参数合理校验
        //为了使用random.nextInt(Integer)方法不得不先把红包金额放大100倍，最后在main函数里面再除以100
        //这样就可以保证每个人抢到的金额都可以精确到小数点后两位
        int fen = (int) (money * 100);
        if (fen < n || n < 1) {
            System.out.println("红包个数必须大于0，并且最小红包不少于1分");
        }
        List<Integer> boards = new ArrayList<>();
        boards.add(0);
        boards.add(fen);
        //红包个数和板砖个数的关系
        while (boards.size() <= n) {
            int index = new Random().nextInt(fen - 1) + 1;
            if (boards.contains(index)) {
                //保证板子的位置不相同
                continue;
            }
            boards.add(index);
        }

        //计算每个红包的金额，将两个板子之间的钱加起来
        Collections.sort(boards);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < boards.size() - 1; i++) {
            Integer e = boards.get(i + 1) - boards.get(i);
            list.add(e);
        }
        return list;

    }
    public static void main(String[] args) {

        for (int i = 0; i < 50; i++) {
            List<Integer> totalRedPackage = segmentRedPackage(100, 5);
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
