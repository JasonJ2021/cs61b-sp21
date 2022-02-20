# Gitlet Design Document

**Name**:JasonJ2021

## Classes and Data Structures



### Commit

#### Fields

- String message : commit 的信息
- Date date : commit 的时间，如果是initial commit 则为1970xxx
- String parent : 上一个commit 的sha-1值 ,  
- TreeMap<String , String> blobs : 这个commit 包含的文件 , "Hello.txt" : "0dass0123123asdjxxx"

#### Method

- public Commit(String message , String parent) : 创建一个新的Commit , 如果parent 为null 则创建一个initial commit

- public String getDate() : 以git 的格式输出date . Thu Jan 1 08:00:00 1970 +0800
- public String getMessage() : 获取commit message




### Index

#### Fields

- TreeMap<String, File> addStage : Represents stage for addition
- TreeSet< String > removeStage : Represents stage for remove

#### Methods 

- init() 

### Repository

#### Fields



## Algorithms



## Persistence

