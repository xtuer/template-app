#ifndef CENTRALWIDGET2_H
#define CENTRALWIDGET2_H

#include <QWidget>

class TopWindow;
class QAbstractButton;
class CentralWidgetPrivate2;

namespace Ui {
class CentralWidget2;
}

class CentralWidget2 : public QWidget {
    Q_OBJECT

public:
    explicit CentralWidget2(QWidget *parent = 0);
    ~CentralWidget2();

    void setTopWindow(TopWindow *topWindow);

private:
    void initializeUi(); // 初始化界面
    void handleEvents(); // 信号槽事件处理
    void createWidgetInContentStackedWidget(QAbstractButton *button); // 创建需要在内容区 stacked widget 中显示的 widget

    Ui::CentralWidget2 *ui;
    CentralWidgetPrivate2 *d;
};

#endif // CENTRALWIDGET2_H
