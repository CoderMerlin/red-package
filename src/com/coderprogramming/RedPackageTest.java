package com.coderprogramming;

import java.math.BigDecimal;

/**
 * @author Coder编程
 * @Title: RedPackage
 * @ProjectName red-package
 * @Description: 微信、qq 抢红包
 *
 * 抢红包的期望收益应与先后顺序无关
 * 保证每个用户至少能抢到一个预设的最小金额，人民币红包设置的最小金额一般是0.01元，如果需要发其他货币类型的红包，比如区块链货币或者积分，需要自定义一个最小金额。
 * 所有抢红包的人领取的子红包的金额之和加起来，等于发红包的人发出的总红包的金额。 下面实现的方式是一次生成所有的子红包，让用户按顺序领取。也可以每领取一个生成一个，两种方式性能上各有优劣。

 * @date 2020/3/1010:32
 */
public class RedPackageTest {

    public static BigDecimal getRandomMoney(RedPackage _redPackage) {
        // remainSize 剩余的红包数量
        // remainMoney 剩余的钱
        if (_redPackage.remainSize == 1) {
            _redPackage.remainSize--;
            return _redPackage.remainMoney.setScale(2, BigDecimal.ROUND_DOWN);
        }

        BigDecimal random = BigDecimal.valueOf(Math.random());
        BigDecimal min   = BigDecimal.valueOf(0.01);

        BigDecimal halfRemainSize = BigDecimal.valueOf(_redPackage.remainSize).divide(new BigDecimal(2), BigDecimal.ROUND_UP);
        BigDecimal max1 = _redPackage.remainMoney.divide(halfRemainSize, BigDecimal.ROUND_DOWN);
        BigDecimal minRemainAmount = min.multiply(BigDecimal.valueOf(_redPackage.remainSize - 1)).setScale(2, BigDecimal.ROUND_DOWN);
        BigDecimal max2 = _redPackage.remainMoney.subtract(minRemainAmount);
        BigDecimal max = (max1.compareTo(max2) < 0) ? max1 : max2;

        BigDecimal money = random.multiply(max).setScale(2, BigDecimal.ROUND_DOWN);
        money = money.compareTo(min) < 0 ? min: money;

        _redPackage.remainSize--;
        _redPackage.remainMoney = _redPackage.remainMoney.subtract(money).setScale(2, BigDecimal.ROUND_DOWN);;
        return money;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            RedPackage moneyPackage = new RedPackage();
            moneyPackage.remainMoney = BigDecimal.valueOf(100);
            moneyPackage.remainSize = 5;
            System.out.print("（欢迎关注公众号：Coder编程）第 "+(i+1)+" 组数据： ");
            while (moneyPackage.remainSize != 0) {
                System.out.print(getRandomMoney(moneyPackage)  + "   ");
            }

            System.out.println();
        }
    }

    static class RedPackage {
        int    remainSize;
        BigDecimal remainMoney;
    }
}
