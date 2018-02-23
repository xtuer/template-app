#ifndef TOPWINDOW_H
#define TOPWINDOW_H

#include <QWidget>

class TopWindowPrivate;

namespace Ui {
class TopWindow;
}

/**
 * TopWindow 用于创建自定义边框窗口，使用九宫格技术也就是 Border-Image 技术实现。
 * 默认提供了标题栏、边框，可以在构造函数中传入对应的参数自定义它们，也可以调用对应的成员函数进行自定义。
 *
 * 使用非侵入式的集成方式，只需要把要显示的 widget 作为构造函数的第一个参数传入即可，不需要修改 widget 的代码。
 * 有 3 种使用方式:
 * 1. 普通对窗口:
 *    DemoWidget *c = new DemoWidget();
 *    TopWindow window(c);
 *    window.show();
 * 2. 模态对话框，会阻塞当前线程
 *    DemoWidget *c = new DemoWidget();
 *    TopWindow window(c);
 *    window.showModal();
 *    c->getStatus(); // 获取操作结果
 * 3. 显示消息对话框:
 *    TopWindow::message("Hello God");
 *
 * 此外提供了自定义标题栏的功能，这样就能够自定义复杂的标题栏了
 *    DemoWidget *c = new DemoWidget();
 *    TopWindow window(c);
 *    window.setTitleBar(new YourTitleBar());
 *    window.show();
 */
class TopWindow : public QWidget {
    Q_OBJECT

public:
    /**
     * @brief 创建一个自定义边框和标题栏窗口 的 TopWindow 对象，centralWidget 显示在窗口的中心。
     *        已经提供了默认的边框，如果想使用自定义的边框，则传入 windowPaddings，borderImageBorders 和 borderImagePath 等参数创建。
     * @param centralWidget      显示在窗口中心的 widget
     * @param windowPaddings     窗口的 padding，根据 border image 的边来设置
     * @param borderImageBorders 根据背景图得出的 border image 的 4 个边的宽度
     * @param borderImagePath    背景图的路径，可以是资源文件里的路径，绝对路径，也可以是相对于工程可自行文件的路径
     * @param borderImageHorizontalStretch 水平边框是否拉伸显示
     * @param borderImageVerticalStretch   垂直边框是否拉伸显示
     */
    explicit TopWindow(QWidget *centralWidget,
                       const QMargins &windowPaddings     = QMargins(16, 10, 16, 16),
                       const QMargins &borderImageBorders = QMargins(23, 13, 23, 33),
                       const QString  &borderImagePath    = QString(":/image/top-window/shadow.png"),
                       bool  borderImageHorizontalStretch = true,
                       bool  borderImageVerticalStretch   = true);
    ~TopWindow();

    /**
      * @brief 设置窗口的标题
      *
      * @param title 标题
      */
     void setTitle(const QString &title);

     /**
      * @brief 使用自定义的标题栏(会隐藏默认的标题栏)
      * @param titleBar 自定义的标题栏
      */
     void setTitleBar(QWidget *titleBar);

     /**
      * @brief  设置是否显示标题栏，有时候需要自定义复杂的标题栏，并且在上面显示不仅仅是最小化，关闭按钮等，
      *         还要显示设置按钮，自定义皮肤按钮等，这时就需要隐藏默认的标题栏，然后使用自定义的标题栏。
      *
      * @param visible 为 true 时显示标题栏，为 false 时不显示
      */
     void setTitleBarVisible(bool visible = true);

     /**
      * @brief 设置是否显示最小化，最大化，关闭按钮
      *
      * @param min   为 true 时显示最小化按钮，为 false 时不显示
      * @param max   为 true 时显示最大化按钮，为 false 时不显示
      * @param close 为 true 时显示关闭按钮，为 false 时不显示
      */
     void setTitleBarButtonsVisible(bool min, bool max, bool close);

    /**
     * @brief 设置是否可以修改窗口的大小，默认为可以修改窗口大小
     *
     * @param resizable 为 true 时可以修改窗口的大小，为 false 时不可以
     */
    void setResizable(bool resizable);

     /**
      * @brief 最大化窗口
      */
     void showMaximized();

     /**
      * @brief 从最大化恢复普通窗口大小
      */
     void showNormal();

     /**
      * @brief 显示为模态对话，会使用一个局部事件队列阻塞当前线程，阻塞代码继续往下运行
      */
     void showModal();

signals:
     /**
      * @brief 即将关闭的信号
      */
     void aboutClose();

protected:
    void paintEvent(QPaintEvent *event) Q_DECL_OVERRIDE;
    void mousePressEvent(QMouseEvent *event) Q_DECL_OVERRIDE;
    void mouseReleaseEvent(QMouseEvent *event) Q_DECL_OVERRIDE;
    void mouseMoveEvent(QMouseEvent *event) Q_DECL_OVERRIDE;
    void closeEvent(QCloseEvent *event) Q_DECL_OVERRIDE;

private:
    void handleEvents(); // 信号槽事件处理

    Ui::TopWindow *ui;
    TopWindowPrivate *d;
};

#endif // TOPWINDOW_H
