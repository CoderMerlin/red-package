![抢红包](https://upload-images.jianshu.io/upload_images/7326374-473bf658a3e8a324.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



## 官方实现

**算法原理：**

- 抢红包的期望收益应与先后顺序无关
- 保证每个用户至少能抢到一个预设的最小金额，人民币红包设置的最小金额一般是0.01元。如果需要发其他货币类型的红包，比如区块链货币或者积分，需要自定义一个最小金额。
- 所有抢红包的人领取的子红包的金额之和加起来，等于发红包的人发出的总红包的金额。 

下面实现的方式是一次生成所有的子红包，让用户按顺序领取。也可以每领取一个生成一个，两种方式性能上各有优劣。


### 代码实现

```

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

```

### 运行结果

![打印结果](https://upload-images.jianshu.io/upload_images/7326374-ca81027252f629c4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

（30人抢500块）数据模型如下：

![重复执行1次结果](https://upload-images.jianshu.io/upload_images/7326374-a1d5beaa86ee6006.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![重复执行200次结果](https://upload-images.jianshu.io/upload_images/7326374-9cb9aba616467dab.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![重复执行2000次结果](https://upload-images.jianshu.io/upload_images/7326374-5e6ff0b804fa8838.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


### 微信抢红包架构介绍

**Q1：微信的金额什么时候算？**

答：微信金额是拆的时候实时算出来，不是预先分配的，采用的是纯内存计算，不需要预算空间存储。。
采取实时计算金额的考虑：预算需要占存储，实时效率很高，预算才效率低。

**Q2：实时性：为什么明明抢到红包，点开后发现没有？**

答：最开始2014年的红包一点开就知道金额，分两次操作，先抢到金额，然后再转账。
2015年之后的红包的拆和抢是分离的，需要点两次，因此会出现抢到红包了，但点开后告知红包已经被领完的状况。进入到第一个页面不代表抢到，只表示当时红包还有。

**Q3：分配：红包里的金额怎么算？为什么出现各个红包金额相差很大？**

答：随机，额度在0.01和剩余平均值*2之间。
例如：发100块钱，总共10个红包，那么平均值是10块钱一个，那么发出来的红包的额度在0.01元～20元之间波动。
当前面3个红包总共被领了40块钱时，剩下60块钱，总共7个红包，那么这7个红包的额度在：0.01～（60/7*2）=17.14之间。
注意：这里的算法是每被抢一个后，剩下的会再次执行上面的这样的算法。

这样算下去，会超过最开始的全部金额，因此到了最后面如果不够这么算，那么会采取如下算法：保证剩余用户能拿到最低1分钱即可。
如果前面的人手气不好，那么后面的余额越多，红包额度也就越多，因此实际概率一样的。

**Q4：红包是如何设计的？**

答：微信从财付通拉取金额数据郭莱，生成个数/红包类型/金额放到redis集群里，app端将红包ID的请求放入请求队列中，如果发现超过红包的个数，直接返回。根据红包的裸祭处理成功得到令牌请求，则由财付通进行一致性调用，通过像比特币一样，两边保存交易记录，交易后交给第三方服务审计，如果交易过程中出现不一致就强制回归。

**Q5：并发性处理：红包如何计算被抢完？**

答：cache会抵抗无效请求，将无效的请求过滤掉，实际进入到后台的量不大。cache记录红包个数，原子操作进行个数递减，到0表示被抢光。财付通按照20万笔每秒入账准备，但实际还不到8万每秒。

**Q6：通如何保持8w每秒的写入？**

答：多主sharding，水平扩展机器。

**Q7：一个红包一个队列？**

答：没有队列，一个红包一条数据，数据上有一个计数器字段。

**Q8：有没有从数据上证明每个红包的概率是不是均等？**

答：不是绝对均等，就是一个简单的拍脑袋算法。

**Q9：拍脑袋算法，会不会出现两个最佳？**

答：会出现金额一样的，但是手气最佳只有一个，先抢到的那个最佳。

**Q10：每领一个红包就更新数据么？**

答：每抢到一个红包，就cas更新剩余金额和红包个数。

**Q11：红包如何入库入账？**

数据库会累加已经领取的个数与金额，插入一条领取记录。入账则是后台异步操作。

**Q12：入帐出错怎么办？比如红包个数没了，但余额还有？**

答：最后会有一个take all操作。另外还有一个对账来保障。

**Q13：数据容量多少？**

答：一个红包只占一条记录，有效期只有几天，因此不需要太多空间。

**Q14：查询红包分配，压力大不？**

答：抢到红包的人数和红包都在一条cache记录上，没有太大的查询压力。

以来源于QCon某高可用架构群整理，整理朱玉华。


## 二倍均值法

**算法原理:**

剩余红包金额M，剩余人数N，那么：每次抢到金额=随机(0，M/N*2)
保证了每次随机金额的平均值是公平的
假设10人，红包金额100元
第一人：100/10*2=20，随机范围(0,20)，平均可以抢到10元
第二人：90/9*2=20，随机范围(0,20)，平均可以抢到10元
第三人：80/8*2=20，随机范围(0,20)，平均可以抢到10元
以此类推，每次随机范围的均值是相等的

**缺点：**

除了最后一次，任何一次抢到的金额都不会超过人均金额的两倍，并不是任意的随机

### 代码实现

```

/**
     * 二倍均值法
     * @param totalAmount
     * @param totalPeopleNum
     * @return
     */
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
```

### 运行结果

![二倍均值法打印结果](https://upload-images.jianshu.io/upload_images/7326374-e94800f15e6f1be0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 线段切割法

**算法原理:**

把红包总金额想象成一条很长的线段，而每个人抢到的金额，则是这条主线段所拆分出的若干子线段。
当N个人一起抢红包的时候，就需要确定N-1个切割点。
因此，当N个人一起抢总金额为M的红包时，我们需要做N-1次随机运算，以此确定N-1个切割点。
随机的范围区间是[1，100* M）。当所有切割点确定以后，子线段的长度也随之确定。这样每个人来抢红包的时候，只需要顺次领取与子线段长度等价的红包金额即可。


### 代码实现

```
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
```

### 打印结果

![线段分割法打印结果](https://upload-images.jianshu.io/upload_images/7326374-c8da512efb9b1480.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

相关代码可关注微信公众号，后台回复：抢红包代码。获取下载
也可移步github
下载地址：https://github.com/CoderMerlin/red-package

## 文末

文章收录至
Github: [https://github.com/CoderMerlin/coder-programming](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FCoderMerlin%2Fcoder-programming)
Gitee: [https://gitee.com/573059382/coder-programming](https://links.jianshu.com/go?to=https%3A%2F%2Fgitee.com%2F573059382%2Fcoder-programming)
欢迎**关注**并star~


![微信公众号](https://upload-images.jianshu.io/upload_images/7326374-0c9d0fbd2c954edb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





