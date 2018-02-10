#ifndef MAGICWINDOW_H
#define MAGICWINDOW_H

#include <QWidget>
#include <QPixmap>

class MagicWindowPrivate;

namespace Ui {
class MagicWindow;
}

class MagicWindow : public QWidget {
    Q_OBJECT

public:
    /**
     * @brief 使用 MagicWindow 创建一个自定义边框和标题栏的窗口，centralWidget 作为窗口的主要 widget 显示。如果需要使用自己的边框风格，
     *        则传入 windowPaddings，borderImageBorders 和 borderImagePath。
     *
     * @param centralWidget      窗口的中心 widget
     * @param windowPaddings     窗口的 padding，根据 border image 的边来设置
     * @param borderImageBorders Border image 4 个边的宽度
     * @param borderImagePath    Border image 的路径
     * @param tiled              是否使用平铺的方式绘制边框
     */
    explicit MagicWindow(QWidget *centralWidget,
                         const QMargins &windowPaddings     = QMargins(16, 10, 16, 16),
                         const QMargins &borderImageBorders = QMargins(23, 13, 23, 33),
                         const QString  &borderImagePath    = QString(":/image/MagicWindow/shadow.png"),
                         bool  tiled = false);
    ~MagicWindow();

    /**
     * @brief 设置标题
     *
     * @param title
     */
    void setTitle(const QString &title);

    /**
     * @brief  设置是否显示标题栏，有时候需要自定义复杂的工具栏，并且在工具栏上有关闭等按钮
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
     * @brief 显示为模态对话框
     */
    void showModal();

protected:
    void paintEvent(QPaintEvent *event) Q_DECL_OVERRIDE;
    void mousePressEvent(QMouseEvent *e) Q_DECL_OVERRIDE;
    void mouseReleaseEvent(QMouseEvent *e) Q_DECL_OVERRIDE;
    void mouseMoveEvent(QMouseEvent *e) Q_DECL_OVERRIDE;
    bool eventFilter(QObject *watched, QEvent *event) Q_DECL_OVERRIDE;

private:
    void signalSlot();                   // 处理信号槽
    bool isMouseAtEdge() const;          // 检查鼠标是否在窗口边框上
    bool isMovingWindowMode() const;     // 正在移动窗口
    bool isResizingWindowMode() const;   // 正在修改窗口大小
    void calculateMousePosition() const; // 计算鼠标在窗口的哪一个边框上
    void updateCursor();                 // 根据鼠标的位置更新鼠标的样式
    void reset();                        // 恢复鼠标的标记，鼠标样式等
    QRect centralRect() const;           // 窗口去掉阴影部分的矩形区域，也即真正显示内容的区域

    Ui::MagicWindow *ui;
    MagicWindowPrivate *d;
};


#endif // MAGICWINDOW_H
