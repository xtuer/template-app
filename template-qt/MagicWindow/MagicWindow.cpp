#include "MagicWindow.h"
#include "ui_MagicWindow.h"
#include "NinePatchPainter.h"

#include <QDebug>
#include <QPixmap>
#include <QPainter>
#include <QMouseEvent>

/*-----------------------------------------------------------------------------|
 |                              MagicWindowPrivate                             |
 |----------------------------------------------------------------------------*/
class MagicWindowPrivate {
public:
    QWidget *centralWidget;
    NinePatchPainter *ninePatchPainter;
    QMargins padding; // 根据 border image 的边(一般是阴影的大小)来设置窗口的 padding 以便达到满意的效果

    bool left;      // 为 true 时鼠标在窗口的左边框
    bool right;     // 为 true 时鼠标在窗口的右边框
    bool top;       // 为 true 时鼠标在窗口的上边框
    bool bottom;    // 为 true 时鼠标在窗口的下边框
    bool resizable; // 是否允许改变窗口大小

    QPoint mousePressedPosition;         // 鼠标按下时的全局坐标坐标
    QPoint windowPositionAsMousePressed; // 鼠标按下时窗口左上角的坐标
    QRect  rubberBandRectAsDrag;         // 鼠标按下时 rubberBand 的矩形
    QRubberBand *rubberBand;

    MagicWindowPrivate(QWidget *centralWidget,
                       const QMargins &windowPaddings,
                       const QMargins &borderImageBorders,
                       const QString  &borderImagePath,
                       bool  tiled)
        : centralWidget(centralWidget), padding(windowPaddings), resizable(true),
        rubberBand(new QRubberBand(QRubberBand::Rectangle)) {
        ninePatchPainter = new NinePatchPainter(borderImageBorders.left(),
                                                borderImageBorders.top(),
                                                borderImageBorders.right(),
                                                borderImageBorders.bottom(),
                                                borderImagePath,
                                                tiled);
    }

    ~MagicWindowPrivate() {
        delete rubberBand;
    }
};

/*-----------------------------------------------------------------------------|
 |                                 MagicWindow                                 |
 |----------------------------------------------------------------------------*/
MagicWindow::MagicWindow(QWidget *centralWidget,
                         const QMargins &windowPaddings,
                         const QMargins &borderImageBorders,
                         const QString  &borderImagePath,
                         bool tiled)
    : ui(new Ui::MagicWindow), d(new MagicWindowPrivate(centralWidget, windowPaddings, borderImageBorders, borderImagePath, tiled)) {
    ui->setupUi(this);
    setWindowFlags(Qt::FramelessWindowHint);
    setAttribute(Qt::WA_TranslucentBackground);

    layout()->setSpacing(0); // 去掉标题栏和中心 widget 的间隙
    layout()->setContentsMargins(d->padding); // 留空白显示出阴影
    layout()->addWidget(centralWidget);

    // 几个按钮都靠上贴合到边框上
    ui->titleBarWidget->layout()->setContentsMargins(5, 0, 0, 0);
    ui->titleBarWidget->layout()->setAlignment(ui->minButton,   Qt::AlignTop);
    ui->titleBarWidget->layout()->setAlignment(ui->maxButton,   Qt::AlignTop);
    ui->titleBarWidget->layout()->setAlignment(ui->closeButton, Qt::AlignTop);

    // 设置按钮的时间过滤器，鼠标移动到他们上面时设置他们的 cursor 为箭头样式
    ui->closeButton->installEventFilter(this);
    ui->minButton->installEventFilter(this);
    ui->maxButton->installEventFilter(this);
    ui->restoreButton->installEventFilter(this);

    // 启用 Mouse Tracking
    this->setMouseTracking(true);
    ui->titleBarWidget->setMouseTracking(true);
    d->centralWidget->setMouseTracking(true);

    ui->restoreButton->hide();
    signalSlot();
}

MagicWindow::~MagicWindow() {
    delete ui;
    delete d;
}

void MagicWindow::setTitle(const QString &title) {
    ui->titleLabel->setText(title);
}

void MagicWindow::setTitleBarVisible(bool visible) {
    ui->titleBarWidget->setVisible(visible);
}

void MagicWindow::setTitleBarButtonsVisible(bool min, bool max, bool close) {
    ui->minButton->setVisible(min);
    ui->maxButton->setVisible(max);
    ui->closeButton->setVisible(close);
}

void MagicWindow::setResizable(bool resizable) {
    d->resizable = resizable;
}

void MagicWindow::showMaximized() {
    layout()->setContentsMargins(0,0,0,0); // 最大化窗口时不需要阴影，所以去掉窗口的 padding
    ui->maxButton->hide();
    ui->restoreButton->show();

    QWidget::showMaximized(); // 函数覆盖
}

void MagicWindow::showNormal() {
    layout()->setContentsMargins(d->padding); // 时恢复窗口大小时显示阴影，所以加上窗口的 padding
    ui->maxButton->show();
    ui->restoreButton->hide();

    QWidget::showNormal();
}

void MagicWindow::showModal() {
    setTitleBarButtonsVisible(false, false, true); // 模态对话框没必要显示最小化，最大化按钮
    setWindowFlags(Qt::Dialog | Qt::Popup | Qt::FramelessWindowHint);
    setWindowModality(Qt::ApplicationModal);

    QWidget::show();
}

void MagicWindow::paintEvent(QPaintEvent *) {
    QPainter painter(this);
    d->ninePatchPainter->draw(&painter, rect());
}

void MagicWindow::mousePressEvent(QMouseEvent *e) {
    // 全屏时不允许移动
    if (isMaximized()) {
        return;
    }

    d->mousePressedPosition = e->globalPos();
    d->windowPositionAsMousePressed = this->pos();

    if (isMouseAtEdge()) {
        // 在窗口边框上按下鼠标，并且可以修改窗口大小4时，显示 rubberBand，进入调整窗口大小的模式
        // 鼠标按下时 rubberBand 的大小为 centralRect 的大小(使用的是全局坐标)
        d->rubberBandRectAsDrag = centralRect();
        d->rubberBand->setGeometry(d->rubberBandRectAsDrag);
        d->rubberBand->show();

        // 开始调整窗口大小的时候设置全局的鼠标样式，否则进入 d->centralWidget 后鼠标的样式会改变
        if (d->left || d->right) {
            QApplication::setOverrideCursor(Qt::SizeHorCursor);
        } else if (d->top || d->bottom) {
            QApplication::setOverrideCursor(Qt::SizeVerCursor);
        }
    }
}

void MagicWindow::mouseReleaseEvent(QMouseEvent *) {
    if (isResizingWindowMode()) {
        // 调整窗口大小的模式下松开鼠标时修改窗口的大小
        int x = d->rubberBand->pos().x() - d->padding.left();
        int y = d->rubberBand->pos().y() - d->padding.top();
        int w = d->rubberBand->width() + d->padding.left() + d->padding.right();
        int h = d->rubberBand->height() + d->padding.top() + d->padding.bottom();
        setGeometry(x, y, w, h);

        // 隐藏 rubberBand，恢复鼠标样式
        d->rubberBand->hide();
        QApplication::restoreOverrideCursor();
        reset();
    }

    d->mousePressedPosition = QPoint();
}

void MagicWindow::mouseMoveEvent(QMouseEvent *e) {
    // 最大化时不允许移动、修改窗口大小
    if (isMaximized()) {
        return;
    }

    // 移动鼠标并且没有按下时，计算鼠标在哪个边上
    if (d->mousePressedPosition.isNull()) {
        calculateMousePosition();
        updateCursor();
    }

    if (isMovingWindowMode()) {
        // 移动模式下移动鼠标修改窗口的位置
        QPoint delta = e->globalPos() - d->mousePressedPosition;
        QPoint pos = d->windowPositionAsMousePressed + delta;
        this->move(pos);
    } else if (isResizingWindowMode()) {
        // 调整窗口大小的模式下移动鼠标时改变 rubberBand 的大小
        QRect r(d->rubberBandRectAsDrag);
        int x = QCursor::pos().x();
        int y = QCursor::pos().y();

        if (d->left) {
            r.setLeft(x);
        } else if (d->top) {
            r.setTop(y);
        } else if (d->right) {
            r.setRight(x);
        } else if (d->bottom) {
            r.setBottom(y);
        }

        d->rubberBand->setGeometry(r);
    }
}

bool MagicWindow::eventFilter(QObject *watched, QEvent *event) {
    if (watched == ui->closeButton || watched == ui->minButton || watched == ui->maxButton || watched == d->centralWidget) {
        if (event->type() == QEvent::Enter) {
            QWidget *w = qobject_cast<QWidget*>(watched);
            w->setCursor(Qt::ArrowCursor);
            return true;
        }

        // 避免鼠标移动到 central widget 内后按下鼠标还能够修改窗口大小
        if (watched == d->centralWidget && event->type() == QEvent::MouseButtonPress) {
            return true;
        }
    }

    return QObject::eventFilter(watched, event);
}

void MagicWindow::signalSlot() {
    // 最小化
    connect(ui->minButton, &QPushButton::clicked, [=] {
        showMinimized();
    });

    // 最大化
    connect(ui->maxButton, &QPushButton::clicked, [=] {
        showMaximized();
    });

    // 普通大小
    connect(ui->restoreButton, &QPushButton::clicked, [=] {
        showNormal();
    });

    // 关闭
    connect(ui->closeButton, &QPushButton::clicked, [=] {
        close();
    });
}

// 当鼠标在窗口的 4 个边的任意一边上，并且可以修改窗口大小时返回 true
bool MagicWindow::isMouseAtEdge() const {
    return (d->left || d->top || d->right || d->bottom) && d->resizable;
}

// 移动模式：鼠标按下且不在窗口边上
bool MagicWindow::isMovingWindowMode() const {
    return !d->mousePressedPosition.isNull() && !isMouseAtEdge();
}

// 修改窗口大小模式: 鼠标按下且在窗口边上
bool MagicWindow::isResizingWindowMode() const {
    return !d->mousePressedPosition.isNull() && isMouseAtEdge();
}

void MagicWindow::calculateMousePosition() const {
    // 计算窗口显示区域的 rect
    QRect rect = centralRect();
    int x = rect.x();
    int y = rect.y();
    int w = rect.width();
    int h = rect.height();

    // 4 个边的矩形，宽或高为 2 个像素
    QRect leftRect(x-2, y, 2, h);
    QRect rightRect(x+w, y, 2, h);
    QRect topRect(x, y-2, w, 2);
    QRect bottomRect(x, y+h, w, 2);

    // 计算鼠标在哪个边上
    QPoint pos = QCursor::pos();
    d->left    = leftRect.contains(pos);
    d->right   = rightRect.contains(pos);
    d->top     = topRect.contains(pos);
    d->bottom  = bottomRect.contains(pos);
}

// 根据鼠标的位置更新鼠标的样式
// 在左、右边上时鼠标的样式为可以水平改变大小的样式
// 在上、下边上时鼠标的样式为可以垂直改变大小的样式
// 其他时候为默认的箭头样式
void MagicWindow::updateCursor() {
    if ((d->left || d->right) && d->resizable) {
        setCursor(Qt::SizeHorCursor);
    } else if ((d->top || d->bottom) && d->resizable) {
        setCursor(Qt::SizeVerCursor);
    } else {
        setCursor(Qt::ArrowCursor);
    }
}

void MagicWindow::reset() {
    d->left   = false;
    d->right  = false;
    d->top    = false;
    d->bottom = false;
    d->mousePressedPosition = QPoint();
    setCursor(Qt::ArrowCursor);
}

// 窗口去掉阴影部分的矩形区域，也即真正显示内容的区域
QRect MagicWindow::centralRect() const {
    int x = this->x() + d->padding.left();
    int y = this->y() + d->padding.top();
    int w = width() - d->padding.left() - d->padding.right();
    int h = height() - d->padding.top() - d->padding.bottom();

    return QRect(x, y, w, h);
}
