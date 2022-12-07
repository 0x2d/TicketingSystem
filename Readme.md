# TicketingSystem
中国科学院大学2022秋季学期并发数据结构与多核编程作业，使用JAVA实现并发火车售票系统。

## Build and Run
环境：```JAVA 1.8.0```
```bash
cd TicketingSystem
./run_test.sh
```

## Commit logs

### 20221207
1. 完成了Test.java，初步优化了TicketingDS，目前64线程吞吐量可达503 op/ms。

### 20221205
1. 修改了所有错误，通过了verilin检查。
2. 在GenerateHistory的基础上编写Test.java。

### 20221203
1. 实现了TicketingDS的所有方法，通过了replay的检查，但是verilin失败；正在编写Test.java。

### 20221126
1. 实现了buyTicket方法。
