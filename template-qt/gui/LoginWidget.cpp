#include "LoginWidget.h"
#include "ui_LoginWidget.h"
#include "util/UiUtil.h"
#include "TopWindow.h"

LoginWidget::LoginWidget(QWidget *parent) : QWidget(parent), ui(new Ui::LoginWidget) {
    initializeUi();
    handleEvents();
}

LoginWidget::~LoginWidget() {
    delete ui;
}

bool LoginWidget::isLoginSuccess() const {
    return loginSuccess;
}

void LoginWidget::initializeUi() {
    ui->setupUi(this);
    setAttribute(Qt::WA_StyledBackground);

    ui->avatarLabel->setText("");
    UiUtil::setWidgetPaddingAndSpacing(this, 20, 20);
    UiUtil::setWidgetPaddingAndSpacing(ui->formWidget, 0, 0);
}

void LoginWidget::handleEvents() {
    // 点击关闭按钮关闭登陆窗口
    connect(ui->cancelButton, &QPushButton::clicked, [this] {
        UiUtil::topLevelWidget(this)->close();
    });

    // 点击登陆按钮登陆
    connect(ui->loginButton, &QPushButton::clicked, [this] {
        QString username = ui->usernameLineEdit->text().trimmed();
        QString password = ui->passwordLineEdit->text();
        loginSuccess = login(username, password);

        if (loginSuccess) {
            UiUtil::topLevelWidget(this)->close();
        } else {
            TopWindow::message("登陆失败\n请输入正确的帐号和密码\n\n默认帐号: admin\n默认密码: admin");
        }
    });
}

bool LoginWidget::login(const QString &username, const QString &password) {
    if ("admin" == username && "admin" == password) {
        return true;
    } else {
        return false;
    }
}
