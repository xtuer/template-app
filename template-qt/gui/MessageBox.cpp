#include "ui_MessageBox.h"
#include "MessageBox.h"
#include "TopWindow.h"
#include "util/UiUtil.h"

MessageBox::MessageBox(const QString &message, bool confirm) : ui(new Ui::MessageBox) {
    ui->setupUi(this);

    // 右上角的关闭按钮
    closeButton = new QPushButton(this);
    closeButton->setObjectName("closeButton");
    closeButton->setVisible(!confirm);

    ui->messageLabel->setAttribute(Qt::WA_TransparentForMouseEvents);
    ui->messageLabel->setText(message);
    ui->buttonsWidget->setVisible(confirm); // confirm 为 true 时显示

    setAttribute(Qt::WA_StyledBackground);
    setStyleSheet(".MessageBox { background: white; }");

    // 点击取消按钮关闭窗口
    connect(ui->cancelButton, &QPushButton::clicked, [this] {
        result = false;
        UiUtil::findWindow(this)->close();
    });

    // 点击确定按钮关闭窗口
    connect(ui->okButton, &QPushButton::clicked, [this] {
        result = true;
        UiUtil::findWindow(this)->close();
    });

    // 点击关闭按钮关闭窗口
    connect(closeButton, &QPushButton::clicked, [this] {
        result = false;
        UiUtil::findWindow(this)->close();
    });
}

MessageBox::~MessageBox() {
    delete ui;
}

void MessageBox::message(const QString &msg, int width, int height,
                         const QMargins &windowPaddings, const QMargins &borderImageBorders,
                         const QString &borderImagePath,
                         bool borderImageHorizontalStretch, bool borderImageVerticalStretch) {
    // 使用自定义窗口
    MessageBox *box = new MessageBox(msg, false);
    TopWindow *window = new TopWindow(box, windowPaddings, borderImageBorders, borderImagePath,
                                      borderImageHorizontalStretch, borderImageVerticalStretch);
    MessageBox::setWindowForMessageBox(window, width, height);
    window->setAttribute(Qt::WA_DeleteOnClose);
    window->show();
}

bool MessageBox::confirm(const QString &msg, int width, int height,
                         const QMargins &windowPaddings, const QMargins &borderImageBorders,
                         const QString &borderImagePath,
                         bool borderImageHorizontalStretch, bool borderImageVerticalStretch) {
    // 使用自定义窗口
    MessageBox *box = new MessageBox(msg, true);
    TopWindow window(box, windowPaddings, borderImageBorders, borderImagePath,
                     borderImageHorizontalStretch, borderImageVerticalStretch);
    MessageBox::setWindowForMessageBox(&window, width, height);
    window.showModal();

    return box->result;
}

void MessageBox::resizeEvent(QResizeEvent *event) {
    closeButton->move(width() - closeButton->width() - 0, 0);

    QWidget::resizeEvent(event);
}

void MessageBox::setWindowForMessageBox(TopWindow *window, int width, int height) {
    window->setTitleBarVisible(false);
    window->setResizable(false);
    window->setWindowFlags(Qt::Dialog | Qt::Popup | Qt::FramelessWindowHint);
    window->setWindowModality(Qt::ApplicationModal);
    window->resize(width, height);
}
