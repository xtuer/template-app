#ifndef LOGINWIDGET_H
#define LOGINWIDGET_H

#include <QWidget>

namespace Ui {
class LoginWidget;
}

class LoginWidget : public QWidget {
    Q_OBJECT

public:
    explicit LoginWidget(QWidget *parent = 0);
    ~LoginWidget();

    /**
     * 检查登陆状态
     *
     * @return 登陆成功返回 true，否则返回 false
     */
    bool isLoginSuccess() const;

    /**
     * 显示登陆窗口
     *
     * @return 登陆成功返回 true，否则返回 false
     */
    static bool login();

private:
    void initializeUi(); // 初始化 Ui
    void handleEvents(); // 信号槽事件处理
    bool login(const QString &username, const QString &password); // 登陆逻辑

    Ui::LoginWidget *ui;
    bool loginSuccess = false;
};

#endif // LOGINWIDGET_H
