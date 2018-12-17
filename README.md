
## Fuzzing-API

构建flag矩阵，需要两个阶段：

第一阶段：构建代理类（子类），对非final，非抽象类，非接口的原始类构建代理类，实现copy方法以及比较方法。
	copy方法：如果有实现Serializable接口，那将对象序列化为字节数组，再将字节数组反序列化为对象，从而实现对象的copy方法。
		 如果有实现Cloneale接口，那就使用类提供的clone方法来进行克隆。
	比较方法：需要同时实现equals和hashcode两个方法.
	如果原始类有实现copy方法和比较方法，则代理类不再重写相应的方法。如果没有，则重写这些方法。

第二阶段：执行目标方法，构建fLag矩阵
	如果目标方法是final方法，执行原始类对象的方法，但原始类对象（除static方法之外）要实现比较方法，用于后期的比较。
	如果目标方法是非final方法，执行代理类对象的方法。



### 程序实现

第一阶段：
	preworks:普通java工程，需要使用Soot工具。

第二阶段： fuzzing:android应用程序
### 运行时注意事项
1.第一阶段的程序需要使用Soot工具。

1.第一阶段写的代理类在preworks/Proxys下，需要将下面的文件放到./Fuzzing/app/src/main/java/overidedSubclass下面。

