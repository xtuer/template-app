# 电子书包 移动团队 Objective-C 规范指南

这份规范指南概括了电子书包 iOS 团队的代码约定。

## 介绍

关于这个编程语言的所有规范，如果这里没有写到，请参考苹果官方文档：

* [Objective-C 编程语言][Introduction_1]
* [Cocoa 编码指南][Introduction_2]
* [iOS 应用编程指南][Introduction_3]

[Introduction_1]:https://developer.apple.com/library/content/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/Introduction/Introduction.html

[Introduction_2]:https://developer.apple.com/library/content/documentation/Cocoa/Conceptual/CodingGuidelines/CodingGuidelines.html

[Introduction_3]:https://developer.apple.com/library/content/documentation/iPhone/Conceptual/iPhoneOSProgrammingGuide/Introduction/Introduction.html


## 目录

* [点语法](#点语法)
* [间距](#间距)
* [条件判断](#条件判断)
* [三目运算符](#三目运算符)
* [错误处理](#错误处理)
* [方法](#方法)
* [变量](#变量)
* [命名](#命名)
* [注释](#注释)
* [Init 和 Dealloc](#init-和-dealloc)
* [字面量](#字面量)
* [CGRect 函数](#CGRect-函数)
* [常量](#常量)
* [枚举类型](#枚举类型)
* [位掩码](#位掩码)
* [私有属性](#私有属性)
* [图片命名](#图片命名)
* [布尔](#布尔)
* [单例](#单例)
* [导入](#导入)
* [文件夹](#文件夹)
* [参考指南](#参考指南)

## 点语法

应该 **始终** 使用点语法来访问或者修改属性，访问其他实例时首选括号。

**推荐：**

```c
view.backgroundColor = [UIColor orangeColor];
[UIApplication sharedApplication].delegate;
```

**反对：**

```c
[view setBackgroundColor:[UIColor orangeColor]];
UIApplication.sharedApplication.delegate;
```

## 间距

* 一个缩进使用 4 个空格，永远不要使用制表符（tab）缩进。请确保在 Xcode 中设置了此偏好。
* 方法的大括号和其他的大括号（`if`/`else`/`switch`/`while` 等等）始终和声明在同一行开始，在新的一行结束。

**推荐：**

```c
if (user.isHappy) {
    // Do something
}
else {
    // Do something else
}
```

* 方法之间应该正好空一行，这有助于视觉清晰度和代码组织性。在方法中的功能块之间应该使用空白分开，但往往可能应该创建一个新的方法。
* `@synthesize` 和 `@dynamic` 在实现中每个都应该占一个新行。


## 条件判断

条件判断主体部分应该始终使用大括号括住来防止出错，即使它可以不用大括号（例如它只需要一行）。这些错误包括添加第二行（代码）并希望它是 if 语句的一部分时。还有另外一种更危险的情况：当 if 语句里面的一行被注释掉，下一行就会在不经意间成为了这个 if 语句的一部分。此外，这种风格也更符合所有其他的条件判断，因此也更容易检查。

**推荐：**

```c
if (!error) {
    return success;
}
```

**反对：**

```c
if (!error)
    return success;
```

或

```c
if (!error) return success;
```

### 三目运算符

三目运算符，? ，只有当它可以增加代码清晰度或整洁时才使用。单一的条件都应该优先考虑使用。多条件时通常使用 if 语句会更易懂，或者重构为实例变量。

**推荐：**

```c
result = a > b ? x : y;
```

**反对：**

```c
result = a > b ? x = c > d ? c : d : y;
```

## 错误处理

当引用一个返回错误参数（error parameter）的方法时，应该针对返回值，而非错误变量。

**推荐：**

```c
NSError *error;
if (![self trySomethingWithError:&error]) {
    // 处理错误
}
```

**反对：**

```c
NSError *error;
[self trySomethingWithError:&error];
if (error) {
    // 处理错误
}
```
一些苹果的 API 在成功的情况下会写一些垃圾值给错误参数（如果非空），所以针对错误变量可能会造成虚假结果（以及接下来的崩溃）。

## 方法

在方法签名中，在 -/+ 符号后应该有一个空格。方法片段之间也应该有一个空格。

**推荐：**

```c
- (void)setExampleText:(NSString *)text image:(UIImage *)image;
```

## 变量

变量名应该尽可能命名为描述性的。除了 `for()` 循环外，其他情况都应该避免使用单字母的变量名。
星号表示指针属于变量，例如：`NSString *text` 不要写成 `NSString* text` 或者 `NSString * text` ，常量除外。
尽量定义属性来代替直接使用实例变量。除了初始化方法（`init`， `initWithCoder:`，等）， `dealloc` 方法和自定义的 setters 和 getters 内部，应避免直接访问实例变量。更多有关在初始化方法和 dealloc 方法中使用访问器方法的信息，参见[这里][Variables_1]。


**推荐：**

```c
@interface NYTSection: NSObject

@property (nonatomic) NSString *headline;

@end
```

**反对：**

```c
@interface NYTSection : NSObject {
    NSString *headline;
}
```

[Variables_1]:https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/MemoryMgmt/Articles/mmPractical.html#//apple_ref/doc/uid/TP40004447-SW6

#### 变量限定符

当涉及到[在 ARC 中被引入][Variable_Qualifiers_1]变量限定符时，
限定符 (`__strong`, `__weak`, `__unsafe_unretained`, `__autoreleasing`) 应该位于星号和变量名之间，如：`NSString * __weak text`。

[Variable_Qualifiers_1]:https://developer.apple.com/library/ios/releasenotes/objectivec/rn-transitioningtoarc/Introduction/Introduction.html#//apple_ref/doc/uid/TP40011226-CH1-SW4

## 命名

尽可能遵守苹果的命名约定，尤其那些涉及到[内存管理规则][Naming_1]，（[NARC][Naming_2]）的。

长的和描述性的方法名和变量名都不错。

**推荐：**

```c
UIButton *settingsButton;
```

**反对：**

```c
UIButton *setBut;
```
类名和常量应该始终使用三个字母的前缀（例如 `NYT`），但 Core Data 实体名称可以省略。为了代码清晰，常量应该使用相关类的名字作为前缀并使用驼峰命名法。

**推荐：**

```c
static const NSTimeInterval NYTArticleViewControllerNavigationFadeAnimationDuration = 0.3;
```

**反对：**

```c
static const NSTimeInterval fadetime = 1.7;
```

属性和局部变量应该使用驼峰命名法并且首字母小写。

为了保持一致，实例变量应该使用驼峰命名法命名，并且首字母小写，以下划线为前缀。这与 LLVM 自动合成的实例变量相一致。
**如果 LLVM 可以自动合成变量，那就让它自动合成。**

**推荐：**

```c
@synthesize descriptiveVariableName = _descriptiveVariableName;
```

**反对：**

```c
id varnm;
```

[Naming_1]:https://developer.apple.com/library/mac/#documentation/Cocoa/Conceptual/MemoryMgmt/Articles/MemoryMgmt.html

[Naming_2]:http://stackoverflow.com/a/2865194/340508

## 注释

当需要的时候，注释应该被用来解释特定代码做了哪些事情。自定义函数都必须要加注释，头文件中不允许出现不加注释的函数名。

**推荐：**

```c
// 控制图片在UIButton里的位置，默认为QMUIButtonImagePositionLeft
typedef NS_ENUM(NSUInteger, QMUIButtonImagePosition) {
    QMUIButtonImagePositionTop,             // imageView在titleLabel上面
    QMUIButtonImagePositionLeft,            // imageView在titleLabel左边
    QMUIButtonImagePositionBottom,          // imageView在titleLabel下面
    QMUIButtonImagePositionRight,           // imageView在titleLabel右边
};

/**
 *  提供以下功能：
 *  1. highlighted、disabled 状态均通过改变整个按钮的alpha来表现，无需分别设置不同 state 下的 titleColor、image。alpha 的值可在配置表里修改 ButtonHighlightedAlpha、ButtonDisabledAlpha。
 *  2. 支持点击时改变背景色颜色（highlightedBackgroundColor）
 *  3. 支持点击时改变边框颜色（highlightedBorderColor）
 *  4. 支持设置图片相对于 titleLabel 的位置（imagePosition）
 *  5. 支持设置图片和 titleLabel 之间的间距，无需自行调整 titleEdgeInests、imageEdgeInsets（spacingBetweenImageAndTitle）
 *  @warning QMUIButton 重新定义了 UIButton.titleEdgeInests、imageEdgeInsets、contentEdgeInsets 这三者的布局逻辑，sizeThatFits: 里会把 titleEdgeInests 和 imageEdgeInsets 也考虑在内（UIButton 不会），以使这三个接口的使用更符合直觉。
 */

@interface QMUIButton : UIButton

/**
 * 让按钮的文字颜色自动跟随tintColor调整（系统默认titleColor是不跟随的）<br/>
 * 默认为NO
 */
@property(nonatomic, assign) IBInspectable BOOL adjustsTitleTintColorAutomatically;

/**
 * 让按钮的图片颜色自动跟随tintColor调整（系统默认image是需要更改renderingMode才可以达到这种效果）<br/>
 * 默认为NO
 */
@property(nonatomic, assign) IBInspectable BOOL adjustsImageTintColorAutomatically;

/**
 *  初始化函数
 *  @param image 按钮的image
 */
- (instancetype)initWithImage:(nullable UIImage *)image;

@end

```

## init 和 dealloc

`dealloc` 方法应该放在实现文件的最上面，并且刚好在 `@synthesize` 和 `@dynamic` 语句的后面。在任何类中，`init` 都应该直接放在 `dealloc` 方法的下面。

`init` 方法的结构应该像这样：

```c
- (instancetype)init {
    self = [super init]; // 或者调用指定的初始化方法
    if (self) {
        // Custom initialization
    }

    return self;
}
```

## 字面量

每当创建 `NSString`， `NSDictionary`， `NSArray`，和 `NSNumber` 类的不可变实例时，都应该使用字面量。要注意 `nil` 值不能传给 `NSArray` 和 `NSDictionary` 字面量，这样做会导致崩溃。

**推荐：**

```c
NSArray *names = @[@"Brian", @"Matt", @"Chris", @"Alex", @"Steve", @"Paul"];
NSDictionary *productManagers = @{@"iPhone" : @"Kate", @"iPad" : @"Kamal", @"Mobile Web" : @"Bill"};
NSNumber *shouldUseLiterals = @YES;
NSNumber *buildingZIPCode = @10018;
```

**反对：**

```c
NSArray *names = [NSArray arrayWithObjects:@"Brian", @"Matt", @"Chris", @"Alex", @"Steve", @"Paul", nil];
NSDictionary *productManagers = [NSDictionary dictionaryWithObjectsAndKeys: @"Kate", @"iPhone", @"Kamal", @"iPad", @"Bill", @"Mobile Web", nil];
NSNumber *shouldUseLiterals = [NSNumber numberWithBool:YES];
NSNumber *buildingZIPCode = [NSNumber numberWithInteger:10018];
```

## CGRect 函数

当访问一个 `CGRect` 的 `x`， `y`， `width`， `height` 时，应该使用`CGGeometry`代替直接访问结构体成员。苹果的 `CGGeometry` 参考中说到：

> All functions described in this reference that take CGRect data structures as inputs implicitly standardize those rectangles before calculating their results. For this reason, your applications should avoid directly reading and writing the data stored in the CGRect data structure. Instead, use the functions described here to manipulate rectangles and to retrieve their characteristics.

**推荐：**

```c
CGRect frame = self.view.frame;

CGFloat x = CGRectGetMinX(frame);
CGFloat y = CGRectGetMinY(frame);
CGFloat width = CGRectGetWidth(frame);
CGFloat height = CGRectGetHeight(frame);
```

**反对：**

```c
CGRect frame = self.view.frame;

CGFloat x = frame.origin.x;
CGFloat y = frame.origin.y;
CGFloat width = frame.size.width;
CGFloat height = frame.size.height;
```

## 常量

常量首选内联字符串字面量或数字，因为常量可以轻易重用并且可以快速改变而不需要查找和替换。常量应该声明为 `static` 常量而不是 `#define` ，除非非常明确地要当做宏来使用。

**推荐：**

```c
static NSString * const NYTAboutViewControllerCompanyName = @"The New York Times Company";

static const CGFloat NYTImageThumbnailHeight = 50.0;
```

**反对：**

```c
#define CompanyName @"The New York Times Company"

#define thumbnailHeight 2
```

## 枚举类型

当使用 `enum` 时，建议使用新的基础类型规范，因为它具有更强的类型检查和代码补全功能。现在 SDK 包含了一个宏来鼓励使用使用新的基础类型 - `NS_ENUM()`

**推荐：**

```c
typedef NS_ENUM(NSInteger, NYTAdRequestState) {
    NYTAdRequestStateInactive,
    NYTAdRequestStateLoading
};
```

## 位掩码

当用到位掩码时，使用 `NS_OPTIONS` 宏。

**举例：**

```c
typedef NS_OPTIONS(NSUInteger, NYTAdCategory) {
    NYTAdCategoryAutos      = 1 << 0,
    NYTAdCategoryJobs       = 1 << 1,
    NYTAdCategoryRealState  = 1 << 2,
    NYTAdCategoryTechnology = 1 << 3
};
```

## 私有属性

私有属性应该声明在类实现文件的延展（匿名的类目）中。

**推荐：**

```c
@interface NYTAdvertisement ()

@property (nonatomic, strong) GADBannerView *googleAdView;
@property (nonatomic, strong) ADBannerView *iAdView;
@property (nonatomic, strong) UIWebView *adXWebView;

@end
```

## 图片命名

图片名称应该被统一命名以保持组织的完整。它们应该被命名为一个说明它们用途的字符串，其次是自定义类或属性的无前缀名字（如果有的话），然后进一步说明颜色 和/或 展示位置，最后是它们的状态。推荐使用小写字母加下划线拼接。

**推荐：**

`emotion_delete@2x.png` `icloud_download_fault@2x.png`

使用Asset Catalog管理图片资源。必须添加2x和3x图片。

## 布尔

因为 `nil` 解析为 `NO`，所以没有必要在条件中与它进行比较。永远不要直接和 `YES` 进行比较，因为 `YES` 被定义为 1，而 `BOOL` 可以多达 8 位。

这使得整个文件有更多的一致性和更大的视觉清晰度。

**推荐：**

```c
if (!someObject) {
}
```

**反对：**

```c
if (someObject == nil) {
}
```

-----

**对于 `BOOL` 来说, 这有两种用法:**

```c
if (isAwesome)
if (![someObject boolValue])
```

**反对：**

```c
if ([someObject boolValue] == NO)
if (isAwesome == YES) // 永远别这么做
```

-----

如果一个 `BOOL` 属性名称是一个形容词，属性可以省略 “is” 前缀，但为 get 访问器指定一个惯用的名字，例如：

```c
@property (assign, getter=isEditable) BOOL editable;
```

内容和例子来自 [Cocoa 命名指南][Booleans_1] 。

[Booleans_1]:https://developer.apple.com/library/mac/#documentation/Cocoa/Conceptual/CodingGuidelines/Articles/NamingIvarsAndTypes.html#//apple_ref/doc/uid/20001284-BAJGIIJE


## 单例

单例对象应该使用线程安全的模式创建共享的实例。

```c
+ (instancetype)sharedInstance {
    static id sharedInstance = nil;

    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });

    return sharedInstance;
}
```

## 导入

如果有一个以上的 import 语句，推荐对这些语句进行分组。每个分组的注释是可选的。   

```c   
// Frameworks
@import QuartzCore;

// Models
#import "NYTUser.h"

// Views
#import "NYTButton.h"
#import "NYTUserView.h"
```

## 文件夹

为了避免文件杂乱，物理文件有时需要保持和 Xcode 项目文件同步。Xcode 创建的功能相对独立的组（group）都必须在文件系统有相应的映射。例如自定义工具、第三方SDK等需要创建文件夹。而UIController、view、model就没必须要了。主要考虑到git对文件夹是敏感的，修改文件夹、移动文件夹都会引起大规模git提交，从而丢失提交历史，得不偿失。

# 参考指南

* [Google](https://google.github.io/styleguide/objcguide.xml)
* [GitHub](https://github.com/github/objective-c-conventions)
* [NYTimes](https://github.com/NYTimes/objective-c-style-guide)
* [Adium](https://trac.adium.im/wiki/CodingStyle)
* [Sam Soffes](https://gist.github.com/soffes/812796)
* [CocoaDevCentral](http://cocoadevcentral.com/articles/000082.php)
* [Luke Redpath](http://lukeredpath.co.uk/blog/my-objective-c-style-guide.html)
* [Marcus Zarra](http://www.cimgf.com/zds-code-style-guide/)

