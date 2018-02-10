#include "gui/CentralWidget.h"
#include <QApplication>
#include <QFont>

#include "util/UiUtil.h"
#include "util/LogHandler.h"
#include "MagicWindow/MagicWindow.h"

int main(int argc, char *argv[]) {
    QApplication app(argc, argv);
    LogHandlerInstance.installMessageHandler(); // 安装消息处理函数

    // 设置样式
    UiUtil::loadQss();
    UiUtil::installNoFocusRectStyle();
    QFont font = app.font();
    font.setFamily("微软雅黑");
    app.setFont(font);

    // 主窗口
    CentralWidget *centralWidget = new CentralWidget();

    // 使用自定义窗口: 普通窗口，显示最大最小和关闭按钮，可调整窗口大小
    MagicWindow window(centralWidget);
    window.setTitle("普通窗口");
    window.resize(1000, 700);
    window.setResizable(false);
    window.show();

    // 启用加载样式的快捷键 Ctrl + L
    UiUtil::installLoadQssShortcut(centralWidget);
    int code = app.exec();
    LogHandlerInstance.release(); // 程序结束时释放 LogHandler 的资源，例如刷新并关闭日志文件

    return code;
}
