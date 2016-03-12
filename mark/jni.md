# android studio JNI

标签（空格分隔）： android-jni
---
### Java JNI
##### 1.定义
JNI 是 Java Native Interface 的 缩写,中文为 JAVA 本地调用。从 Java 1.1 开始,
Java Native Interface (JNI)标准成为 java 平台的一部分,它允许 Java 代码和其
他语言写的代码进行交互。
JNI 一开始是为了本地已编译语言,尤其是C和C++而设计的,但是它并不妨碍你使用其他语言,
只要调用约定受支持就可以了。
使用 java 与本地已编译的代码交互,通常会丧失平台可移植性。但是,有些情况下这样做是
可以接受的,甚至是必须的,比如,使用一些旧的库,与硬件、操作系统进行交互,或者为了提高
程序的性能。JNI 标准至少保证本地代 码能工 作在任何 Java 虚拟机实现下。
##### 2.目的
* 标准的 java 类库可能不支持你的程序所需的特性。
* 或许你已有了一个用其他语言写成的库或程序,而你希望在 java 程序中使用它。
* 你可能需要用底层语言实现一个小型的时间敏感代码,比如汇编,然后在你的java 程序中调用这些功能。
##### 3. JNI运行流程
![](http://img.blog.csdn.net/20141207100436625?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveHlhbmc4MQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### android studio nativie 基本步骤
在Android开发本地代码时，有两种方式，一种是使用javah生成头文件，然后编辑源代码，另一种不用生成头文件，直接编辑代码后，使用RegisterNatives方法进行注册
方法一：
1. 添加native接口
2. javah生成c头文件
    ```
    //javah -d app/src/main/jni -cp app/src/main/java/<native-class-name>
    //windows 进入class文件夹然后执行
    javah -d app\src\main\jni  -classpath app\build\intermediates\classes\debug -jni com.dd.jni.StringCommon
    //mac
     javah -d app/src/main/jni/ -classpath app/build/intermediates/classes/debug/ com.dd.jni.StringCommon
    ```
3. 添加c逻辑
4. 配置gradle
    ```
        //为其在defaultConfig分支中增加上
        ndk {
            moduleName "JniTest"
            ldLibs "log", "z", "m"
            abiFilters "armeabi", "armeabi-v7a", "x86"
        }
    ```
#### gradle 配置jni出现的问题
1. 出现如下问题：
```
Error:(12, 0) Error: NDK integration is deprecated in the current plugin. Consider trying the new experimental plugin. For details, see http://tools.android.com/tech-docs/new-build-system/gradle-experimental. Set "android.useDeprecatedNdk=true" in gradle.properties to continue using the current NDK integration.
```
在gradle.properties文件中添加`android.useDeprecatedNdk=true`
方法二：
相对于方法一不在使用头文件的方式，而是在JNI_OnLoad的时候通过RegisterNatives来注册，这样的好处是:

* C＋＋中函数命名自由，不必像javah自动生成的函数声明那样，拘泥特定的命名方式；
* 效率高。传统方式下，Java类call本地函数时，通常是依靠VM去动态寻找.so中的本地函数(因此它们才需要特定规则的命名格式)，而使用RegisterNatives将本地函数向VM进行登记，可以让其更有效率的找到函数；
* 运行时动态调整本地函数与Java函数值之间的映射关系，只需要多次call RegisterNatives()方法，并传入不同的映射表参数即可。

### JNI和android vm之间的关系
##### 1. 如何载入*.so文件
* 由于Android的应用层级类别都是以Java撰写的，这些Java类别转译为Dex型式的Bytecode之后，必须仰赖Dalvik虚拟机(VM: Virtual Machine)来执行。
* 另外，当java需要调用c native组件时，VM就会去加载本地的c组件，让java函数能顺利的调用到C函数。此时，VM扮演着桥梁的角色，让java和c组件能通过透明的JNI接口相互沟通。
* 应用层级的Java类是在虚拟机(VM: Vitual Machine)上执行的，而C组件不是在VM上执行，那么Java程序又如何要求VM去加载(Load)所指定的C组件呢? 可使用下述指令：
```
    //这个应该写在类的👄
    //执行在loading time阶段
    static {
        System.loadLibrary("string-handle");
    }
```
##### 2. 如何撰写 *.so 的入口函数
###### JNI_OnLoad()
1. 告诉VM此C组件使用那一个JNI版本。如果你的*.so文件没有提供JNI_OnLoad()函数，VM会默认该*.so檔是使用最老的JNI 1.1版本。由于新版的JNI做了许多扩充，如果需要使用JNI的新版功能，例如JNI 1.4的 java.nio.ByteBuffer, 就必须藉由JNI_OnLoad()函数来告知VM。

2. 由于VM执行到System.loadLibrary()函数时，就会立即先呼叫JNI_OnLoad()，所以C组件的开发者可以藉由JNI_OnLoad()来进行C组件内的初期值之设定(Initialization)。就将此组件提供的各个本地函数(Native Function)登记到VM里，以便能加快后续呼叫本地函数的效率。
```
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    LOGE("onLoad");
    return JNI_VERSION_1_4;
}
```
###### JNI_OnUnload()
JNI_OnUnload()函数与JNI_OnLoad()相对应的。在加载C组件时会立即呼叫JNI_OnLoad()来进行组件内的初期动作；而当VM释放该C组件时，则会呼叫JNI_OnUnload()函数来进行善后清除动作。当VM呼叫JNI_OnLoad()或JNI_Unload()函数时，都会将VM的指标(Pointer)传递给它们，其参数如下：
```
jint JNI_OnUnload(JavaVM* vm, void* reserved) {

}
```
### JNINativeMethod
在通过RegisterNatives进行注册的时候需要一个JNINativeMethod类型的指针，JNINativeMethod就是native函数映射。
```
typedef struct {  
    const char* name; /*Java 中函数的名字*/  
    const char* signature; /*描述了函数的参数和返回值*/  
    void* fnPtr; /*函数指针,指向 C 函数*/  
 } JNINativeMethod;  
```
函数签名：
       在JNINativeMethod的结构体中，有一个描述函数的参数和返回值的签名字段，它是java中对应函数的签名信息，由参数类型和返回值类型共同组成。由于java支持函数重载，也就是说，可以定义同名但不同参数的函数。然而仅仅根据函数名是没法找到具体函数的。为了解决这个问题，JNI技术中就将参数类型和返回值类型的组合作为一个函数的签名信息，有了签名信息和函数名，就能顺利的找到java中的函数了。
JNI规范定义的函数签名信息格式如下：
`(参数1类型标示参数2类型标示......参数n类型标示)返回值类型标示`
实际上这些字符是与函数的参数类型一一对应的。

* “()” 中的字符表示参数,后面的则代表返回值。例如”()V” 就表示 void Func();
* “(II)V” 表示 void Func(int, int);
* 值得注意的一点是，当参数类型是引用数据类型时，
其格式是“L包名；”其中包名中的“.”换成“/”，
所以在上面的例子中(Ljava/lang/String;Ljava/lang/String;)V 表示 void Func(String,String)；
如果 JAVA 函数位于一个嵌入类,则用$作为类名间的分隔符。
例如 “(Ljava/lang/String;Landroid/os/FileUtils$FileStatus;)Z”
|签名|JNI|JAVA|
|----|---|----|
|V|void|void|
|Z|jboolean|boolean|
|I|jint|int|
|J|jlong|long|
|D|jdouble|double|
|F|jfloat|float|
|B|jbyte|byte|
|C|jchar|char|
|S|jshort|short|
|[I|jintArray|int[]|
|[F|jfloatArray|float[]|
|[B|jbyteArray|byte[]|
|[C|jcharArray|char[]|
|[S|jshortArray|short[]|
|[D|jdoubleArray|double[]|
|[J|jlongArray|long[]|
|[Z|jbooleanArray|Boolean[]|
|Ljava/lang/String;|jstring|String|
命令行可以使用以下命令来查看函数签名:
```
javap －s app/build/intermediates/classes/debug/<包>/<类>.class
```
### JNI 基础
#### JNIEnv和jobject
每一个jni函数都传入来一个JNIEnv指针和一个jobject对象。
JNIEnv类型代表来Java环境，通过这个指针就可以对Java端代码进行操作。一般有创建对象(NewObject/NewString/New<TYPE>Array),获取设置Java对象属性(Get/Set<TYPE>Field,Get/SetStatic<TYPE>Field),调用Java方法(Call<TYPE>Method/CallStatic<TYPE>Method)等很多操作。
jobject 是传入的是java的对象实例，如果本地方法是静态方法的话，则传入的是改类的class对象。
#### jclass，jfiledID和jmethodID
为了能在native中使用java类，jni.h中定义了类型来表示Java中到class类。得到jclass的方法有三个`jclass FindClass(const char*clsNmae)` `jclass GetObjectClass(jobject obj)` `jcalss GetSuperClass(jclass obj)`。jni.h中定义来jfieldID, jmethodID来代表java端的属性和方法。因此在natvie方法中调用Java方法的步骤有三个：
1. 获得jclass
2. 获得jfiledID和jmethodID.
3. 得到属性或者调用方法

##### 得到设置java类属性
```
//jni.h
jobject     (*GetObjectField)(JNIEnv*, jobject, jfieldID);
jboolean    (*GetBooleanField)(JNIEnv*, jobject, jfieldID);
jbyte       (*GetByteField)(JNIEnv*, jobject, jfieldID);
jchar       (*GetCharField)(JNIEnv*, jobject, jfieldID);
jshort      (*GetShortField)(JNIEnv*, jobject, jfieldID);
jint        (*GetIntField)(JNIEnv*, jobject, jfieldID);
jlong       (*GetLongField)(JNIEnv*, jobject, jfieldID);
jfloat      (*GetFloatField)(JNIEnv*, jobject, jfieldID) __NDK_FPABI__;
jdouble     (*GetDoubleField)(JNIEnv*, jobject, jfieldID) __NDK_FPABI__;
```
有很多类似上述的API，需要的时候可以在jni.h中查找
##### 调用java类方法
native 调用Java的三种方式：
```
Call<TYPE>Method(JniEnv*, jobject, jmethodID, ...);
Call<TYPE>Method(JniEnv*, jobject, jmethodID, va_list);
Call<TYPE>Method(JniEnv*, jobject, jmethodID, jvalue)
```
第一个是最常用的方法
第二个是指向yigeva_list的参数表
第三个是指向一个jvalue的数组的参数表
##### 调用java父类的方法
有Java代码：
```
public class Father {
    public void function() {
        System.out.println("father:func");
    }
}
public class Child extends Father {
    public void function() {
        System.out.println("Child:func");
    }
}
//这个时候使用
Father p = new Child();
p.function() //调用的是Child的function()方法
```
有C++代码
```
class Father {
public:
    void function() {
        count<<"Father:func"<<endl;
    }
}
class Child: public Father {
public:
    void function() {
        count<<"Child:func"<<endl;
    }
}
//这个时候
Father* p = new Child();
p->function()//调用的是父类的function，除非在Father的Function函数前加上virtual关键字
```
在JNi中定义类CallNonvirtual<TYPE>Method来实现子类对象调用父类方法。首先得到父类方法的jmethodID,然后调用CallNonvirtual<TYPE>Method方法即可。
#### c/c++创建Java对象
##### 1. NewObject
GetMethodID能够取得构造方法的jmethodID,如果传入的要取得的方法为"<init>"就能够获得构造方法，构造方法的返回值签名始终为void。
##### 2. AllocObject
使用函数AllocObject可以根据传人的jclass创建一个Java对象，但是他的状态是非初始化的，在使用这个对象之前必须用CallNonvirtualVoidMethod来调用该jclass的构造函数。

#### 字符串
##### GetStringChars和GetStringUTFChars
用来取得与某个jstring对象相关的Java字符串，分别可以取得UTF－16编码的* 宽字符串(jchar *)跟UTF8的编码的字符串(char *)
* 两个函数的动作
    1. 开新的内存，然后把Java中的String拷贝到这个内存中然后返回指向这个内存地址的指针。
    2. 直接返回Java中String的内存指针。这个时候千万不要改变这个内存的内容，这将破坏String在Java中始终是常量的原则。
* 最后一个参数是用来标示是否对Java的String对象进行拷贝的。
* 使用了这两个函数取得的字符，在不使用之后要使用ReleaseStringChars/ReleaseStringUTFChars来释放拷贝的内存或者释放对Java的String的对象的引用。

##### GetStringCritical
##### GetStringRegion和GetStringUTFRegion
* 把Java字符串的内容直接拷贝到C/C++的自发数组中。
* 由于C/C++中分配的内存开销相对小，而且在Java中的String内容拷贝的开销可以忽略，更好的一点是此函数不分配内存，不会抛出OutOfMemoryError异常。

##### 其他字符串函数
* NewString() 和NewStringUTF()
* GetStringLength()和GetStringUTFLength()

#### 数组
在jni中数组分为基本类型和对象类型(Object[])两种，数组的长度使用GetArrayLength(jarray array)函数来获取。
##### 基本类型数组
1. Get<TYPE>ArrayElements(<TYPE>Array arr, jboolean* isCopied)可以把Java基本类型的数组转换为C/C++中的数组。一种是拷贝一份传到natvie，另一种是把指向Java数组到指针直接传到native。处理完本地代码之后要通过Release<TYPE>ArrayElements来释放。
2. Release<TYPE>ArrayElements(<TYPE>Array arr, <TYPE>* array), jnit mode) 来选择如何处理Java跟C++的数组，是提交，还是撤销，是内存释放还是不释放。`mode: 0->对Java的数组进行更新并释放C/C++的数组；JNI_COMMIT->对Java的数组进行更新但不释放C/C++的数组；JNI_ABORT->对Java的数组不进行更新，释放C/C++的数组`
3. Get<TYPE>ArrayRegion()
4. Set<TYPE>ArrayRegion()
5. <TYPE>Array New<TYPE>Array()

##### 对象类型数组
* 通过Get/SetObjectArrayElement对数组进行操作
* 不需要释放资源
* 通过NewObjectArray来创建数组

#### 引用
在JNI规范中定义了三种引用：局部引用（Local Reference）、全局引用（Global Reference）、弱全局引用（Weak Global Reference）。区别如下： 
1. **局部引用**：通过NewLocalRef和各种JNI接口创建（FindClass、NewObject、GetObjectClass和NewCharArray等）。会阻止GC回收所引用的对象，不在本地函数中跨函数使用，不能跨线前使用。函数返回后局部引用所引用的对象会被JVM自动释放，或调用DeleteLocalRef释放。`(*env)->DeleteLocalRef(env,local_ref)`
2. **全局引用**：调用NewGlobalRef基于局部引用创建，会阻GC回收所引用的对象。可以跨方法、跨线程使用。JVM不会自动释放，必须调用DeleteGlobalRef手动释放`(*env)->DeleteGlobalRef(env,g_cls_string);`
3. **弱全局引用**：调用NewWeakGlobalRef基于局部引用或全局引用创建，不会阻止GC回收所引用的对象，可以跨方法、跨线程使用。引用不会自动释放，在JVM认为应该回收它的时候（比如内存紧张的时候）进行回收而被释放。或调用DeleteWeakGlobalRef手动释放。(*env)->DeleteWeakGlobalRef(env,g_cls_string)
##### 局部引用
局部引用也称本地引用，通常是在函数中创建并使用。会阻止GC回收所引用的对象。比如，调用NewObject接口创建一个新的对象实例并返回一个对这个对象的局部引用。局部引用只有在创建它的本地方法返回前有效，本地方法返回到Java层之后，如果Java层没有对返回的局部引用使用的话，局部引用就会被JVM自动释放。你可能会为了提高程序的性能，在函数中将局部引用存储在静态变量中缓存起来，供下次调用时使用。这种方式是错误的，因为函数返回后局部引很可能马上就会被释放掉，静态变量中存储的就是一个被释放后的内存地址，成了一个野针对，下次再使用的时候就会造成非法地址的访问，使程序崩溃。
释放一个局部引用有两种方式，一个是本地方法执行完毕后JVM自动释放，另外一个是自己调用DeleteLocalRef手动释放。既然JVM会在函数返回后会自动释放所有局部引用，为什么还需要手动释放呢？大部分情况下，我们在实现一个本地方法时不必担心局部引用的释放问题，函数被调用完成后，JVM 会自动释放函数中创建的所有局部引用。尽管如此，以下几种情况下，为了避免内存溢出，我们应该手动释放局部引用：
1、JNI会将创建的局部引用都存储在一个局部引用表中，如果这个表超过了最大容量限制，就会造成局部引用表溢出，使程序崩溃。经测试，Android上的JNI局部引用表最大数量是512个。当我们在实现一个本地方法时，可能需要创建大量的局部引用，如果没有及时释放，就有可能导致JNI局部引用表的溢出，所以，在不需要局部引用时就立即调用DeleteLocalRef手动删除。比如，在下面的代码中，本地代码遍历一个特别大的字符串数组，每遍历一个元素，都会创建一个局部引用，当对使用完这个元素的局部引用时，就应该马上手动释放它。
2、在编写JNI工具函数时，工具函数在程序当中是公用的，被谁调用你是不知道的。上面newString这个函数演示了怎么样在工具函数中使用完局部引用后，调用DeleteLocalRef删除。不这样做的话，每次调用newString之后，都会遗留两个引用占用空间（elemArray和cls_string，cls_string不用static缓存的情况下）。
3、如果你的本地函数不会返回。比如一个接收消息的函数，里面有一个死循环，用于等待别人发送消息过来while(true) { if (有新的消息) ｛ 处理之。。。。｝ else { 等待新的消息。。。}}。如果在消息循环当中创建的引用你不显示删除，很快将会造成JVM局部引用表溢出。
4、局部引用会阻止所引用的对象被GC回收。比如你写的一个本地函数中刚开始需要访问一个大对象，因此一开始就创建了一个对这个对象的引用，但在函数返回前会有一个大量的非常复杂的计算过程，而在这个计算过程当中是不需要前面创建的那个大对象的引用的。但是，在计算的过程当中，如果这个大对象的引用还没有被释放的话，会阻止GC回收这个对象，内存一直占用者，造成资源的浪费。所以这种情况下，在进行复杂计算之前就应该把引用给释放了，以免不必要的资源浪费。
##### 全局引用
全局引用可以跨方法、跨线程使用，直到它被手动释放才会失效。同局部引用一样，也会阻止它所引用的对象被GC回收。与局部引用创建方式不同的是，只能通过NewGlobalRef函数创建。
##### 弱全局引用
弱全局引用使用NewGlobalWeakRef创建，使用DeleteGlobalWeakRef释放。下面简称弱引用。与全局引用类似，弱引用可以跨方法、线程使用。但与全局引用很重要不同的一点是，弱引用不会阻止GC回收它引用的对象。在newString这个函数中，我们也可以使用弱引用来存储String的Class引用，因为java.lang.String这个类是系统类，永远不会被GC回收。当本地代码中缓存的引用不一定要阻止GC回收它所指向的对象时，弱引用就是一个最好的选择。
##### 引用比较
给定两个引用（不管是全局、局部还是弱全局引用），我们只需要调用IsSameObject来判断它们两个是否指向相同的对象。例如：（*env)->IsSameObject(env, obj1, obj2)，如果obj1和obj2指向相同的对象，则返回JNI_TRUE（或者1），否则返回JNI_FALSE（或者0）。有一个特殊的引用需要注意：NULL，JNI中的NULL引用指向JVM中的null对象。如果obj是一个局部或全局引用，使用(*env)->IsSameObject(env, obj, NULL) 或者 obj == NULL 来判断obj是否指向一个null对象即可。但需要注意的是，IsSameObject用于弱全局引用与NULL比较时，返回值的意义是不同于局部引用和全局引用的：
```
jobject local_obj_ref = (*env)->NewObject(env, xxx_cls,xxx_mid);
jobject g_obj_ref = (*env)->NewWeakGlobalRef(env, local_ref);
// ... 业务逻辑处理
jboolean isEqual = (*env)->IsSameObject(env, g_obj_ref, NULL);
```
在上面的IsSameObject调用中，如果g_obj_ref指向的引用已经被回收，会返回JNI_TRUE，如果wobj仍然指向一个活动对象，会返回JNI_FALSE。
```
每一个JNI引用被建立时，除了它所指向的JVM中对象的引用需要占用一定的内存空间外，引用本身也会消耗掉一个数量的内存空间。作为一个优秀的程序员，我们应该对程序在一个给定的时间段内使用的引用数量要十分小心。短时间内创建大量而没有被立即回收的引用很可能就会导致内存溢出。
```
##### 管理引用的规则
前面对三种引用已做了一个全面的介绍，下面来总结一下引用的管理规则和使用时的一些注意事项，使用好引用的目的就是为了减少内存使用和对象被引用保持而不能释放，造成内存浪费。所以在开发当中要特别小心！
通常情况下，有两种本地代码使用引用时要注意：
1、 直接实现Java层声明的native函数的本地代码
当编写这类本地代码时，要当心不要造成全局引用和弱引用的累加，因为本地方法执行完毕后，这两种引用不会被自动释放。
2、被用在任何环境下的工具函数。例如：方法调用、属性访问和异常处理的工具函数等。
编写工具函数的本地代码时，要当心不要在函数的调用轨迹上遗漏任何的局部引用，因为工具函数被调用的场合和次数是不确定的，一量被大量调用，就很有可能造成内存溢出。所以在编写工具函数时，请遵守下面的规则：
1> 一个返回值为基本类型的工具函数被调用时，它决不能造成局部、全局、弱全局引用被回收的累加
2> 当一个返回值为引用类型的工具函数被调用时，它除了返回的引用以外，它决不能造成其它局部、全局、弱引用的累加对于工具函数来说，为了使用缓存技术而创建一些全局引用或者弱全局引用是正常的。如果一个工具函数返回的是一个引用，我们应该写好注释详细说明返回引用的类型，以便于使用者更好的管理它们。下面的代码中，频繁地调用工具函数GetInfoString，我们需要知道GetInfoString返回引用的类型是什么，以便于每次使用完成后调用相应的JNI函数来释放掉它。

在管理局部引用的生命周期中，Push/PopLocalFrame是非常方便且安全的。我们可以在本地函数的入口处调用PushLocalFrame，然后在出口处调用PopLocalFrame，这样的话，在函数内任何位置创建的局部引用都会被释放。而且，这两个函数是非常高效的，强烈建议使用它们。需要注意的是，如果在函数的入口处调用了PushLocalFrame，记住要在函数所有出口（有return语句出现的地方）都要调用PopLocalFrame。

####异常处理
##### Java与JNI处理异常的区别
1、在Java中如果觉得某段逻辑可能会引发异常，用try…catch机制来捕获并处理异常即可
2、如果在Java中发生运行时异常，没有使用try…catch来捕获，会导致程序直接奔溃退出，后续的代码都不会被执行
3、编译时异常，是在方法声明时显示用throw声明了某一个异常，编译器要求在调用的时候必须显示捕获处理
public static void testException() throws Exception {}
上面这几点，写过Java的朋友都知道，而且很简单，但我为什么还要拿出来说呢，其实我想重点说明的是，在JNI中发生的异常和Java完全不一样。我们在写JNI程序的时候，JNI没有像Java一样有try…catch…final这样的异常处理机制，面且在本地代码中调用某个JNI接口时如果发生了异常，后续的本地代码不会立即停止执行，而会继续往下执行后面的代码。
下面是专用的JNI函数，可以对异常进行处理。
```
Throw()：丢弃一个现有的异常对象；在固有方法中用于重新丢弃一个异常。
ThrowNew()：生成一个新的异常对象，并将其丢弃。
ExceptionOccurred()：判断一个异常是否已被丢弃，但尚未清除。
ExceptionDescribe()：打印一个异常和堆栈跟踪信息。
ExceptionClear()：清除一个待决的异常。
FatalError()：造成一个严重错误，不返回。
```
在所有这些函数中，最不能忽视的就是ExceptionOccurred()和ExceptionClear()。大多数JNI函数都能产生异常，而且没有象在Java的try块内的那种语言特性可供利用。所以在每一次JNI函数调用之后，都必须调用ExceptionOccurred()，了解异常是否已被丢弃。若侦测到一个异常，可选择对其加以控制（可能时还要重新丢弃它）。然而，必须确保异常最终被清除。这可以在自己的函数中用ExceptionClear()来实现；若异常被重新丢弃，也可能在其他某些函数中进行。

#### JNI跟多线程
##### Java 启动线程调用native
Java线程调用JNI时候一定要确定是否需要同步锁
##### Java 调用native启动线程
对于JNI线程，需要注意的是JNIEnv不能共用的，一旦返回，JNIEnv将被销毁，所以JNi线程处理函数中不能使用创建线程函数中的JNIEnv，而是应该通过JVM来获取JNIEnv， 因为JVM是进程相关的.
```
jint (*AttachCurrentThread)(JavaVM*, JNIEnv**, void*);
jint (*DetachCurrentThread)(JavaVM*);
```
当在一个线程里面调用AttachCurrentThread后，如果不需要用的时候一定要DetachCurrentThread，否则线程无法正常退出。

#### JNI调用性能测试及优化
网上有朋友针对Java调用本地接口，Java调Java方法做了一次详细的测试，来充分说明在享受JNI给程序带来优势的同时，也要接受其所带来的性能开销，下面请看一组测试数据：
##### Java调用JNI空函数与Java调用Java空方法性能测试
测试环境：JDK1.4.2_19、JDK1.5.0_04和JDK1.6.0_14，测试的重复次数都是一亿次。测试结果的绝对数值意义不大，仅供参考。因为根据JVM和机器性能的不同，测试所产生的数值也会不同，但不管什么机器和JVM应该都能反应同一个问题，Java调用native接口，要比Java调用Java方法性能要低很多。
Java调用Java空方法的性能：
|JDK版本|Java调用Java耗时|平均每秒调用次数|
|-------|----------------|----------------|
|1.6    |329ms  |303951367次|
|1.5    |312ms  |320512820次|
|1.4    |312ms  |27233115次|
Java调用JNI空函数的性能：
|JDK版本|Java调JNI耗时|平均每秒调用次数|
|-------|-------------|----------------|
|1.6    |1531ms     |65316786次|
|1.5    |1891ms     |52882072次|
|1.4    |3672ms     |27233115次|
从上述测试数据可以看出JDK版本越高，JNI调用的性能也越好。在JDK1.5中，仅仅是空方法调用，JNI的性能就要比Java内部调用慢将近5倍，而在JDK1.4下更是慢了十多倍。
##### JNI查找方法ID、字段ID、Class引用性能测试
当我们在本地代码中要访问Java对象的字段或调用它们的方法时，本机代码必须调用FindClass()、GetFieldID()、GetStaticFieldID、GetMethodID() 和 GetStaticMethodID()。对于 GetFieldID()、GetStaticFieldID、GetMethodID() 和 GetStaticMethodID()，为特定类返回的 ID 不会在 JVM 进程的生存期内发生变化。但是，获取字段或方法的调用有时会需要在 JVM 中完成大量工作，因为字段和方法可能是从超类中继承而来的，这会让 JVM 向上遍历类层次结构来找到它们。由于 ID 对于特定类是相同的，因此只需要查找一次，然后便可重复使用。同样，查找类对象的开销也很大，因此也应该缓存它们。下面对调用JNI接口FindClass查找Class、GetFieldID获取类的字段ID和GetFieldValue获取字段的值的性能做的一个测试。缓存表示只调用一次，不缓存就是每次都调用相应的JNI接口：
**java.version = 1.6.0_14**

> JNI 字段读取 (缓存Class=false ,缓存字段ID=false) 耗时 : 79172 ms 平均每秒 : 1263072
> JNI 字段读取 (缓存Class=true ,缓存字段ID=false) 耗时 : 25015 ms 平均每秒 : 3997601 JNI
> 字段读取 (缓存Class=false ,缓存字段ID=true) 耗时 : 50765 ms 平均每秒 : 1969861 JNI
> 字段读取 (缓存Class=true ,缓存字段ID=true) 耗时 : 2125 ms 平均每秒 : 47058823

**java.version = 1.5.0_04**

> JNI 字段读取 (缓存Class=false ,缓存字段ID=false) 耗时 : 87109 ms 平均每秒 : 1147987
> JNI 字段读取 (缓存Class=true ,缓存字段ID=false) 耗时 : 32031 ms 平均每秒 : 3121975 JNI
> 字段读取 (缓存Class=false ,缓存字段ID=true) 耗时 : 51657 ms 平均每秒 : 1935846 JNI
> 字段读取 (缓存Class=true ,缓存字段ID=true) 耗时 : 2187 ms 平均每秒 : 45724737

**java.version = 1.4.2_19**

> JNI 字段读取 (缓存Class=false ,缓存字段ID=false) 耗时 : 97500 ms 平均每秒 : 1025641
> JNI 字段读取 (缓存Class=true ,缓存字段ID=false) 耗时 : 38110 ms 平均每秒 : 2623983 JNI
> 字段读取 (缓存Class=false ,缓存字段ID=true) 耗时 : 55204 ms 平均每秒 : 1811462 JNI
> 字段读取 (缓存Class=true ,缓存字段ID=true) 耗时 : 4187 ms 平均每秒 : 23883448

根据上面的测试数据得知，查找class和ID(属性和方法ID)消耗的时间比较大。只是读取字段值的时间基本上跟上面的JNI空方法是一个数量级。而如果每次都根据名称查找class和field的话，性能要下降高达40倍。读取一个字段值的性能在百万级上，在交互频繁的JNI应用中是不能忍受的。 消耗时间最多的就是查找class，因此在native里保存class和member id是很有必要的。class和member id在一定范围内是稳定的，但在动态加载的class loader下，保存全局的class要么可能失效，要么可能造成无法卸载classloader,在诸如OSGI框架下的JNI应用还要特别注意这方面的问题。在读取字段值和查找FieldID上，JDK1.4和1.5、1.6的差距是非常明显的。但在最耗时的查找class上，三个版本没有明显差距。
通过上面的测试可以明显的看出，**在调用JNI接口获取方法ID、字段ID和Class引用时，如果没用使用缓存的话，性能低至4倍**。所以在JNI开发中，合理的使用缓存技术能给程序提高极大的性能。缓存有两种，分别为*使用时缓存*和*类静态初始化时缓存*，区别主要在于缓存发生的时刻。
##### 使用时缓存

##### 类静态初始化缓存

##### 两种缓存方式比较
如果在写JNI接口时，不能控制方法和字段所在类的源码的话，用使用时缓存比较合理。但比起类静态初始化时缓存来说，用使用时缓存有一些缺点：
1. 使用前，每次都需要检查是否已经缓存该ID或Class引用
2. 如果在用使用时缓存的ID，要注意只要本地代码依赖于这个ID的值，那么这个类就不会被unload。另外一方面，如果缓存发生在静态初始化时，当类被unload或reload时，ID会被重新计算。因为，尽量在类静态初始化时就缓存字段ID、方法ID和类的Class引用。