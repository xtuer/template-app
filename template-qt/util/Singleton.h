#ifndef SINGLETON_H
#define SINGLETON_H

#include <QMutex>
#include <QScopedPointer>

/**
 * 使用方法:
 * 1. 定义类为单例 (类声明时使用宏 SINGLETON):
 *    class Config {
 *        SINGLETON(Config)
 *    public:
 *
 * 2. 在 cpp 文件中实现类的默认构造函数和析构函数 (已经在宏 SINGLETON 里被声明为 private 的了，所以必须实现)
 *
 * 3. 调用单例的函数，可以任意使用下面三种方式中的一种:
 *    3.1 推荐调用: Config::instance().method();
 *    3.2 原始调用: Singleton<Config>::getInstance().method();
 *    3.3 保存引用，方便多次调用:
 *        Config &config = Singleton<Config>::getInstance();
 *        config.method1();
 *        config.method2();
 *        config.method3();
 *
 * 注意:
 *     如果单例的类需要释放的资源和 Qt 底层的信号系统有关系，例如 QSettings 的数据没有保存，QSqlDatabase 的连接没有关闭等，
 *     需要在 main 函数返回前手动释放，否则有可能在程序退出时报系统底层的信号错误，因为 main 函数返回后 qApp 已经被回收，
 *     而资源的释放在 main 返回后，又和信号槽有关，所以就可能报错。
 *     推荐实现方式: 可以在单例类的构造函数中给 qApp 的 aboutToQuit 信号绑定一个槽函数，在里面处理善后工作、释放资源等。
 */
template <typename T>
class Singleton {
public:
    static T& getInstance(); // 获取单例的唯一实例对象

    Singleton(const Singleton &other) = delete;
    Singleton<T>& operator=(const Singleton &other) = delete;

private:
    static QMutex mutex;
    static QScopedPointer<T> instance;
};

/*-----------------------------------------------------------------------------|
 |                          Singleton Implementation                           |
 |----------------------------------------------------------------------------*/
template <typename T> QMutex Singleton<T>::mutex;
template <typename T> QScopedPointer<T> Singleton<T>::instance;

template <typename T>
T& Singleton<T>::getInstance() {
    if (instance.isNull()) {
        mutex.lock();
        if (instance.isNull()) {
            instance.reset(new T()); // 此指针会在全局变量作用域结束时自动 deleted (main 函数返回后)
        }
        mutex.unlock();
    }

    return *instance.data();
}

/*-----------------------------------------------------------------------------|
 |                               Singleton Macro                               |
 |----------------------------------------------------------------------------*/
#define SINGLETON(Class)                           \
private:                                           \
    Class();                                       \
    ~Class();                                      \
    Class(const Class &other) = delete;            \
    Class& operator=(const Class &other) = delete; \
    friend class  Singleton<Class>;                \
    friend struct QScopedPointerDeleter<Class>;    \
                                                   \
public:                                            \
    static Class& instance() {                     \
        return Singleton<Class>::getInstance();    \
    }

#endif // SINGLETON_H
