#include "gui/CentralWidget.h"
#include "gui/TopWindow.h"
#include "gui/LoginWidget.h"
#include "util/UiUtil.h"
#include "util/LogHandler.h"

#include <QApplication>

static void initialize(); // 程序启动时进行初始化
static void finalize();   // 程序结束时清理工作

int main(int argc, char *argv[]) {
    // 启用 Retina 高分辨率
    QApplication::setAttribute(Qt::AA_UseHighDpiPixmaps);
    QApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
    QApplication app(argc, argv);
    ::initialize();

    // [1] 主窗口使用自定义窗口显示
    TopWindow window(new CentralWidget());
    window.setTitle("普通窗口");
    window.resize(1000, 700);
    UiUtil::centerWindow(&window);
    window.show();

    // [2] 显示登陆对话框，点击取消按钮登陆失败退出程序，登陆成功继续往下运行
    // 输入错误信息虽然登陆不成功，但是不会退出程序，而是提示输入错误，继续输入登陆
    if (!LoginWidget::login()) {
        exit(0);
    }

    // [3] 进入 Qt 事件队列
    int code = app.exec();
    ::finalize();
    return code;
}


/**
 * 程序启动时进行初始化
 */
static void initialize() {
    // 安装日志处理工具
    LogHandlerInstance.installMessageHandler();

    // 设置界面样式
    UiUtil::loadQss();
    UiUtil::installNoFocusRectStyle();

    // 设置默认字体
    QFont font = qApp->font();
    font.setFamily("微软雅黑");
    qApp->setFont(font);
}

/**
 * 程序结束时清理工作
 * 程序结束时释放回收资源，例如释放日志资源，释放数据库连接池资源等
 */
static void finalize() {
    LogHandlerInstance.uninstallMessageHandler(); // 程序结束时释放 LogHandler 的资源，例如刷新并关闭日志文件
}
