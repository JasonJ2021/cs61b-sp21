# Gitlet Design Document

**Name**:JasonJ2021

## Classes and Data Structures



Hints:

- 只有String / byte[] 才能使用Util.writeContent，其他的object使用writeObject
- log 还没有考虑merge的情况
- checkout 还没有考虑branch的point

CheckPoints

1. [10) init-err (0.0/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#init-err)
2. [101) ec-untracked (0.0/32.0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#ec-untracked)
3. [102) ec-remote-fetch-push (0.0/24.0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#ec-remote-fetch-push)
4. [103) ec-remote-fetch-pull (0.0/24.0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#ec-remote-fetch-pull)
5. [104) ec-bad-remotes-err (0.0/16.0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#ec-bad-remotes-err)
6. [11) basic-status (0.0/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#basic-status)
7. [12) add-status (0.0/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#add-status)
8. [13) remove-status (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#remove-status)
9. [14) add-remove-status (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#add-remove-status)
10. [15) remove-add-status (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#remove-add-status)
11. [17) empty-commit-message-err (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#empty-commit-message-err)
12. [18) nop-add (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#nop-add)
13. [19) add-missing-err (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#add-missing-err)
14. [20) status-after-commit (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#status-after-commit)
15. [21) nop-remove-err (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#nop-remove-err)
16. [22) remove-deleted-file (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#remove-deleted-file)
17. [23) global-log (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#global-log)
18. [24) global-log-prev (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#global-log-prev)
19. [25) successful-find (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#successful-find)
20. [26) successful-find-orphan (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#successful-find-orphan)
21. [27) unsuccessful-find-err (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#unsuccessful-find-err)
22. [28) checkout-detail (0.0/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#checkout-detail)
23. [29) bad-checkouts-err (0.0/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#bad-checkouts-err)
24. [30) branches (0.0/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#branches)
25. [30a) rm-branch (0.0/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#rm-branch)
26. [31) duplicate-branch-err (0.0/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#duplicate-branch-err)
27. [31a) rm-branch-err (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#rm-branch-err)
28. [32) file-overwrite-err (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#file-overwrite-err)
29. [33) merge-no-conflicts (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#merge-no-conflicts)
30. [34) merge-conflicts (0.0/74.074)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#merge-conflicts)
31. [35) merge-rm-conflicts (0.0/74.074)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#merge-rm-conflicts)
32. [36) merge-err (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#merge-err)
33. [36a) merge-parent2 (0.0/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#merge-parent2)
34. [37) reset1 (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#reset1)
35. [38) bad-resets-err (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#bad-resets-err)
36. [40) special-merge-cases (0.0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#special-merge-cases)
37. [41) no-command-err (0.0/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#no-command-err)
38. [42) other-err (0.0/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#other-err)
39. [43) bai-merge (0.0/88.889)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/112578422#bai-merge)

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



#### Methods 



### Repository

#### Fields



## Algorithms



## Persistence

