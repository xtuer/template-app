#include "SettingWidget.h"
#include "ui_SettingWidget.h"

SettingWidget::SettingWidget(QWidget *parent) : QWidget(parent), ui(new Ui::SettingWidget) {
    ui->setupUi(this);
    setAttribute(Qt::WA_StyledBackground);
}

SettingWidget::~SettingWidget() {
    delete ui;
}
