#ifndef CENTRALWIDGET_H
#define CENTRALWIDGET_H

#include <QWidget>

class QAbstractButton;
class CentralWidgetPrivate;

namespace Ui {
class CentralWidget;
}

class CentralWidget : public QWidget {
    Q_OBJECT

public:
    explicit CentralWidget(QWidget *parent = 0);
    ~CentralWidget();

private:
    void initializeUi(); // 初始化界面
    void handleEvents(); // 信号槽事件处理
    void createWidgetInContentStackedWidget(QAbstractButton *button); // 创建需要在内容区 stacked widget 中显示的 widget

    Ui::CentralWidget *ui;
    CentralWidgetPrivate *d;
};

#endif // CENTRALWIDGET_H
