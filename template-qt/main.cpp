#include "gui/CentralWidget.h"
#include "gui/TopWindow.h"
#include "gui/LoginWidget.h"
#include "util/UiUtil.h"
#include "util/LogHandler.h"

#include <QApplication>

int main(int argc, char *argv[]) {
    // 启用 Retina 高分辨率
    QApplication::setAttribute(Qt::AA_UseHighDpiPixmaps);
    QApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
    QApplication app(argc, argv);

    LogHandlerInstance.installMessageHandler(); // 安装日志处理工具

    // 设置样式和默认字体
    UiUtil::loadQss();
    UiUtil::installNoFocusRectStyle();
    {
        QFont font = app.font();
        font.setFamily("微软雅黑");
        app.setFont(font);
    }

    // [1] 创建程序真正的主窗口
    CentralWidget *centralWidget = new CentralWidget();

    // 使用自定义窗口显示主窗口: 普通窗口，显示最大最小和关闭按钮，可调整窗口大小
    TopWindow window(centralWidget);
    window.setTitle("普通窗口");
    window.resize(1000, 700);
    UiUtil::centerWindow(&window);
    window.show();

    // [2] 显示登陆窗口
    {
        LoginWidget *loginWidget = new LoginWidget();
        TopWindow dialog(loginWidget);
        dialog.setTitleBarVisible(false);
        dialog.setResizable(false);
        dialog.showModal();

        // 点击取消登陆按钮，isLoginSuccess() 返回 false，退出程序
        if (!loginWidget->isLoginSuccess()) {
            exit(0);
        }
    }


    // 启用加载样式的快捷键 Ctrl + L，方便调试，修改样式文件后按下快捷键即可加载，不需要重启程序
    UiUtil::installLoadQssShortcut(centralWidget);

    // 进入 Qt 事件队列
    int code = app.exec();

    // 程序结束时释放回收资源，例如释放日志资源，释放数据库连接池资源等
    LogHandlerInstance.release(); // 程序结束时释放 LogHandler 的资源，例如刷新并关闭日志文件

    return code;
}
